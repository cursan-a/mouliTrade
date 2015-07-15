package mouliTradeV2.packet;

import com.networkAsyncLib.common.APacket;

public class LaunchTradePacket extends APacket {
	private static final long serialVersionUID = -2571714163527702589L;
	private Integer capital;
	private Integer days;

	public LaunchTradePacket(Integer capital, Integer days) {
		this.capital = capital;
		this.days = days;
	}

	public Integer getCapital() {
		return capital;
	}

	public void setCapital(Integer capital) {
		this.capital = capital;
	}

	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

}
