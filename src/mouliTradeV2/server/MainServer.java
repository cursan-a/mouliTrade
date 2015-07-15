package mouliTradeV2.server;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainServer  extends Application  {

	public static void main(String[] args) {
		Application.launch(MainServer.class, args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(MainServer.class.getResource("/mouliTradeV2/server/MainView.fxml"));
		AnchorPane rootLayout = (AnchorPane) loader.load();
		((MainController) loader.getController()).setPrimaryStage(primaryStage);
		Scene scene = new Scene(rootLayout);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

}
