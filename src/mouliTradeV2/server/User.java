package mouliTradeV2.server;

import java.util.ArrayList;

import javafx.application.Platform;

public class User implements Comparable<User> {
	private ItemController controller;
	private String name;
	private int capital;
	private int nbActions;
	private ArrayList<Integer> orders = new ArrayList<Integer>();
	private int score;
	private int lastCurrentValue;

	public User(String name, int capital) {
		super();
		this.name = name;
		this.capital = capital;
	}

	public void setController(ItemController controller) {
		this.controller = controller;
	}

	private void actualizeItemController(Integer currentPrice) {
		controller.getLbl_current().setText(capital + "€ and " + nbActions + " actions");
		controller.getLbl_total().setText((capital + nbActions * currentPrice) + "€");
	}

	public void addOrder(int value, int currentPrice) {
		if (value > 0 && capital - value * currentPrice * 1.0015 >= 0) {
			capital -= value * currentPrice * 1.0015;
			nbActions += value;
			orders.add(value);
		} else if (value < 0 && -value <= nbActions) {
			capital += -value * currentPrice * 0.9985;
			nbActions -= -value;
			orders.add(value);
		} else
			orders.add(0);
		score = capital + nbActions * currentPrice;
		lastCurrentValue = currentPrice;
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				actualizeItemController(currentPrice);
			}
		});
	}

	public ItemController getController() {
		return controller;
	}

	public String getName() {
		return name;
	}

	public int getCapital() {
		return capital;
	}

	public void setCapital(int capital) {
		this.capital = capital;
	}

	public int getNbActions() {
		return nbActions;
	}

	public void setNbActions(int nbActions) {
		this.nbActions = nbActions;
	}

	public ArrayList<Integer> getOrders() {
		return orders;
	}

	@Override
	public int compareTo(User user) {
		return user.score - score;
	}

	public int getScore() {
		return score;
	}

	public int getLastCurrentValue() {
		return lastCurrentValue;
	}
	
	public int getActionsValues() {
		return lastCurrentValue * nbActions;
	}

}
