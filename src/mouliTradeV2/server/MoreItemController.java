package mouliTradeV2.server;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Label;

public class MoreItemController {
	@FXML
	private LineChart<Integer, Integer> lc_orders;
	@FXML
	private Label lbl_current;
	@FXML
	private Label lbl_total;
	@FXML
	private Label lbl_userName;
	private XYChart.Series<Integer, Integer> orders = new XYChart.Series<Integer, Integer>();
	
	@SuppressWarnings("unused")
	private User user;
	
	public void setUser(User user) {
		this.user = user;
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				lbl_userName.setText(user.getName());
				lbl_current.setText(user.getCapital() + "€ (" + user.getNbActions() + ")");
				lbl_total.setText(user.getScore() + "€");
				lc_orders.setCreateSymbols(false);
				lc_orders.getData().add(orders);
				orders.getData().add(new Data<Integer, Integer>(0, 0));
				int i = 1;
				int count = 0;
				for (Integer order : user.getOrders()) {
					count += order;
					orders.getData().add(new Data<Integer, Integer>(i++, count));
				}
				orders.nodeProperty().get().setStyle("-fx-stroke-width: 1px;");
			}
		});
	}
}
