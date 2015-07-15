package mouliTradeV2.packet;

import com.networkAsyncLib.common.APacket;

public class DailyValuePacket extends APacket {
	private static final long serialVersionUID = -7967059863490492063L;
	private int value;

	public DailyValuePacket(int value) {
		super();
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
