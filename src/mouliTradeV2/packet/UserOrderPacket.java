package mouliTradeV2.packet;

import com.networkAsyncLib.common.APacket;

public class UserOrderPacket extends APacket {
	private static final long serialVersionUID = 4194254951933476330L;
	private int value;

	public UserOrderPacket(int value) {
		super();
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
