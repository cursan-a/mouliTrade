package mouliTradeV2.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mouliTradeV2.packet.DailyValuePacket;
import mouliTradeV2.packet.EndingPacket;
import mouliTradeV2.packet.HandCheckPacket;
import mouliTradeV2.packet.LaunchTradePacket;
import mouliTradeV2.packet.UserOrderPacket;

import org.apache.logging.log4j.LogManager;

import com.networkAsyncLib.common.SocketSenderReceiver;
import com.networkAsyncLib.server.ClientDisconnectedEvent;
import com.networkAsyncLib.server.SpecificPacketReceivedEvent;
import com.networkAsyncLib.server.TCPServer;

public class MainController {
	@FXML
	private Label lbl_nbConnected;
	@FXML
	private Label lbl_lineRemaining;
	@FXML
	private LineChart<Integer, Integer> lc_action;
	@FXML
	private StackedBarChart<String, Integer> bc_top5;
	@FXML
	private VBox vb_user0;
	@FXML
	private VBox vb_user1;
	@FXML
	private VBox vb_user2;
	@FXML
	private VBox vb_user3;
	@FXML
	private Label lbl_actionFile;
	@FXML
	private TextField tf_traderNumber;
	@FXML
	private TextField tf_port;
	@FXML
	private TextField tf_capital;
	@FXML
	private Button btn_chooseActionFile;
	@FXML
	private Button btn_openConnection;
	@FXML
	private TextField tf_numberToSend;
	@FXML
	private Button btn_send;
	@FXML
	private Slider sl_speed;
	@FXML
	private Label lbl_speed;

	private File actionFile;
	private int traderNumber;
	private int port;
	private int capital;
	private TCPServer server;
	private ArrayList<Integer> actionValues;
	private long timestamp;
	private XYChart.Series<Integer, Integer> actionValuesGui = new XYChart.Series<Integer, Integer>();

	private Stage primaryStage;
	private HashMap<SocketSenderReceiver, User> users = new HashMap<SocketSenderReceiver, User>();
	private ArrayList<User> usersByScore = new ArrayList<User>();
	private int limit = 0;

	@FXML
	private void initialize() {
		// ### DEBUG ###
		actionFile = new File("/Users/cursan_a/trade/AMAZON1_MIN.TXT");
		traderNumber = 1;
		port = 4242;
		capital = 300000;
		// #############
		for (int i = 0; i < 5; i++) {
			XYChart.Series<String, Integer> serie = new XYChart.Series<String, Integer>();
			serie.getData().add(new Data<String, Integer>(String.valueOf(i + 1), 0));
			bc_top5.getData().add(serie);
			//serie = new XYChart.Series<String, Integer>();
			//serie.getData().add(new Data<String, Integer>(String.valueOf(i + 1), 0));
			//bc_top5.getData().add(serie);
		}
	}

	@FXML
	public void chooseActionFile(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose Action File");
		actionFile = fileChooser.showOpenDialog(primaryStage);
		if (actionFile != null) {
			lbl_actionFile.setText(actionFile.getAbsolutePath());
		}
	}

	@FXML
	public void openMore(ActionEvent event) {
		try {
			AnchorPane rootLayout;
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainServer.class.getResource("/mouliTradeV2/server/MoreView.fxml"));
			rootLayout = (AnchorPane) loader.load();
			MoreController moreController = loader.getController();
			moreController.setData(usersByScore);
			Scene scene = new Scene(rootLayout);
			Stage stage = new Stage();
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace(); // Impossible d'ouvrir le FXML
		}
	}

	@FXML
	public void openConnection() {
		if (!tf_traderNumber.getText().isEmpty())
			traderNumber = Integer.parseInt(tf_traderNumber.getText());
		if (!tf_capital.getText().isEmpty())
			capital = Integer.parseInt(tf_capital.getText());
		if (!tf_port.getText().isEmpty())
			port = Integer.parseInt(tf_port.getText());
		if (actionFile != null && traderNumber > 0 && capital > 0 && port > 0 && server == null) {
			btn_chooseActionFile.setDisable(true);
			tf_traderNumber.setDisable(true);
			tf_port.setDisable(true);
			tf_capital.setDisable(true);
			btn_openConnection.setDisable(true);
			lc_action.setCreateSymbols(false);
			actionValuesGui.setName(actionFile.getName());
			lc_action.getData().add(actionValuesGui);
			lbl_nbConnected.setText("Waiting : " + users.size() + "/" + traderNumber + " connected");
			initNetwork();
		}
	}

	private void initNetwork() {
		server = new TCPServer(port);
		server.addSpecificPacketReceivedEvent(new SpecificPacketReceivedEvent<HandCheckPacket>() {
			@Override
			public void specificPacketReceived(SocketSenderReceiver socket, HandCheckPacket packet) {
				if (!packet.getVersionNumber().equals("0.0.1")) {
					// TODO il faut déconnecter le client
					return;
				}
				if (users.size() < traderNumber) {
					User user = new User(packet.getName(), capital);
					users.put(socket, user);
					usersByScore.add(user);
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							linkUserToGUI(user);
						}
					});
					if (users.size() == traderNumber) {
						startTrading();
					} else {
						Platform.runLater(new Runnable() {

							@Override
							public void run() {
								lbl_nbConnected.setText("Waiting : " + users.size() + "/" + traderNumber + " connected");
							}
						});
					}
				}
			}
		});
		server.addSpecificPacketReceivedEvent(new SpecificPacketReceivedEvent<UserOrderPacket>() {
			private int count = 0;

			@Override
			public void specificPacketReceived(SocketSenderReceiver socket, UserOrderPacket packet) {
				User user = users.get(socket);
				if (user == null)
					return; // TODO - il faut déconnecter le gadjo
				user.addOrder(packet.getValue(), actionValues.get(cursor - 1));
				count++;
				if (count == users.size()) {
					actualizeGUI();
					if (cursor < actionValues.size()) {
						waitDelay();
						if (--limit > 0)
							putDataToUsers();
					} else {
						for (Entry<SocketSenderReceiver, User> entry : users.entrySet()) {
							entry.getKey().put(new EndingPacket());
						}
					}
					count = 0;
					timestamp = System.currentTimeMillis();
				}

			}
		});
		server.setClientDisconnectedEvent(new ClientDisconnectedEvent() {

			@Override
			public void clientDisconnected(SocketSenderReceiver socket) {
				// TODO Gérer la déconnection du client

			}
		});
		new Thread(server).start();
	}

	private void waitDelay() {
		if (timestamp + (long) sl_speed.getValue() > System.currentTimeMillis())
			try {
				Thread.sleep(timestamp + (long) sl_speed.getValue() - System.currentTimeMillis());
			} catch (InterruptedException e) {
				e.printStackTrace(); // TODO Le sleep ne fonctionne pas
			}
	}

	public void startTrading() {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				lbl_nbConnected.setText("Ready : " + users.size() + "/" + traderNumber + " connected");
				tf_numberToSend.setDisable(false);
				btn_send.setDisable(false);
				sl_speed.setDisable(false);
				lbl_speed.setDisable(false);
			}
		});
		try {
			readActionFile();
			for (Entry<SocketSenderReceiver, User> entry : users.entrySet()) {
				entry.getKey().put(new LaunchTradePacket(capital, actionValues.size()));
			}
			timestamp = System.currentTimeMillis();
		} catch (IOException e) {
			e.printStackTrace(); // TODO Impossible de démarrer le fichier est
									// mal formaté
		}
	}

	@FXML
	public void sendData(ActionEvent event) {
		if (limit > 0)
			return;
		try {
			limit = Integer.valueOf(tf_numberToSend.getText());
			LogManager.getLogger(MainController.class).info("Send " + limit + " values");
			putDataToUsers();
		} catch (NumberFormatException e) {
			limit = 0;
		}
	}

	private int cursor = 0;

	private void actualizeGUI() {
		Platform.runLater(new Runnable() {
			private int currentValue = actionValues.get(cursor - 1);
			private int currentCursor = cursor - 1;

			@Override
			public void run() {
				actionValuesGui.getData().add(new Data<Integer, Integer>(currentCursor, currentValue));
				lbl_lineRemaining.setText(actionValues.size() - currentCursor - 1 + " values remaining...");
				updateTop5();
			}
		});
	}

	private void putDataToUsers() {
		for (Entry<SocketSenderReceiver, User> entry : users.entrySet()) {
			entry.getKey().put(new DailyValuePacket(actionValues.get(cursor)));
		}
		cursor++;
	}

	private void updateTop5() {
		Collections.sort(usersByScore);
		for (int i = 0; i < 5 && i < usersByScore.size(); i++) {
			bc_top5.getData().get(i).setName(usersByScore.get(i).getName());
			//bc_top5.getData().get(i * 2 + 1).setName("");
			bc_top5.getData().get(i).getData().get(0).setYValue(usersByScore.get(i).getScore());
			//bc_top5.getData().get(i * 2 + 1).getData().get(0).setYValue(usersByScore.get(i).getActionsValues());
		}
	}

	private void readActionFile() throws IOException {
		actionValues = new ArrayList<Integer>();
		BufferedReader br = new BufferedReader(new FileReader(actionFile));
		String line;
		while ((line = br.readLine()) != null && !line.equals("--end--") && !line.equals("--END--")) {
			actionValues.add(Integer.parseInt(line));
		}
		br.close();
		if (actionValues.isEmpty())
			throw new IOException("Le fichier est vide");
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				lbl_lineRemaining.setText(actionValues.size() + " values remaining...");

			}
		});
	}

	private void linkUserToGUI(User user) {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(MainServer.class.getResource("/mouliTradeV2/server/Item.fxml"));
		try {
			AnchorPane item = (AnchorPane) loader.load();
			ItemController itemController = loader.getController();
			user.setController(itemController);
			itemController.getLbl_userName().setText(user.getName());
			itemController.getLbl_current().setText(String.valueOf(capital));
			itemController.getLbl_total().setText(String.valueOf(capital));
			switch ((users.size() - 1) % 4) {
			case 0:
				vb_user0.getChildren().add(item);
				break;
			case 1:
				vb_user1.getChildren().add(item);
				break;
			case 2:
				vb_user2.getChildren().add(item);
				break;
			case 3:
				vb_user3.getChildren().add(item);
				break;
			}
		} catch (IOException e) {
			// TODO CATCH : Un itemView n'a pas pu être crée.
			e.printStackTrace();
		}
	}

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			
			@Override
			public void handle(WindowEvent arg0) {
				for (Entry<SocketSenderReceiver, User> user : users.entrySet())
					user.getKey().put(new EndingPacket());;
				//server.stop();
			}
		});
	}

}
