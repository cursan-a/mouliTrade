package mouliTradeV2.server;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ItemController {
	@FXML
	private Label lbl_userName;
	@FXML
	private Label lbl_current;
	@FXML
	private Label lbl_total;

	public Label getLbl_userName() {
		return lbl_userName;
	}

	public Label getLbl_current() {
		return lbl_current;
	}

	public Label getLbl_total() {
		return lbl_total;
	}

}
