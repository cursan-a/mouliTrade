package mouliTradeV2.server;

import java.io.IOException;
import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class MoreController {
	@FXML
	private VBox vb_1;
	@FXML
	private VBox vb_2;
	@FXML
	private VBox vb_3;
	@FXML
	private VBox vb_4;

	public void setData(ArrayList<User> usersByScore) {
		int i = 0;
		try {
			for (User user : usersByScore) {
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(MainServer.class.getResource("/mouliTradeV2/server/MoreViewItem.fxml"));
				AnchorPane item = (AnchorPane) loader.load();
				MoreItemController itemController = loader.getController();
				itemController.setUser(user);
				switch (i % 4) {
				case 0:
					vb_1.getChildren().add(item);
					break;
				case 1:
					vb_2.getChildren().add(item);
					break;
				case 2:
					vb_3.getChildren().add(item);
					break;
				case 3:
					vb_4.getChildren().add(item);
					break;
				}
				i++;
			}
		} catch (IOException e) {
			// TODO CATCH : Un itemView n'a pas pu être crée.
			e.printStackTrace();
		}
	}
}
