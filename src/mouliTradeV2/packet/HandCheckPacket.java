package mouliTradeV2.packet;

import com.networkAsyncLib.common.APacket;

public class HandCheckPacket extends APacket {
	private static final long serialVersionUID = -1072723741762682842L;
	private String versionNumber;
	private String name;

	public HandCheckPacket(String versionNumber, String name) {
		super();
		this.versionNumber = versionNumber;
		this.name = name;
	}

	public String getVersionNumber() {
		return versionNumber;
	}

	public String getName() {
		return name;
	}

}
