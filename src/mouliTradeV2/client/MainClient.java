package mouliTradeV2.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.logging.log4j.LogManager;

import mouliTradeV2.packet.DailyValuePacket;
import mouliTradeV2.packet.EndingPacket;
import mouliTradeV2.packet.HandCheckPacket;
import mouliTradeV2.packet.LaunchTradePacket;
import mouliTradeV2.packet.UserOrderPacket;

import com.networkAsyncLib.client.ConnectedToServerEvent;
import com.networkAsyncLib.client.SpecificPacketReceivedFromServerEvent;
import com.networkAsyncLib.client.TCPClient;
import com.networkAsyncLib.common.SocketSenderReceiver;

public class MainClient {
	private String tradeBin;
	private String name;
	private String host;
	private int port;
	private int days;
	private TCPClient client;
	private boolean isRunning = false;
	private BufferedReader binOut;
	private BufferedWriter binIn;

	public MainClient(String tradeBin, String name, String host, int port) {
		super();
		this.tradeBin = tradeBin;
		this.name = (name.equals("default")) ? "user_" + (int) (Math.random() * 100000) : name;
		this.host = host;
		this.port = port;
	}
	
	public MainClient(String tradeBin) {
		super();
		this.tradeBin = tradeBin;
	}

	public void runOnlineMode() {
		client = new TCPClient(this.host, this.port);
		client.setConnectedToServerEvent(new ConnectedToServerEvent() {

			@Override
			public void connectedToServer(SocketSenderReceiver socket) {
				socket.put(new HandCheckPacket("0.0.1", MainClient.this.name));
				LogManager.getLogger(MainClient.class).info("Connected to server");
			}
		});
		client.addSpecificPacketReceivedFromServerEvent(new SpecificPacketReceivedFromServerEvent<LaunchTradePacket>() {

			@Override
			public void specificPacketReceivedFromServer(SocketSenderReceiver socket, LaunchTradePacket packet) {
				try {
					if (!isRunning)
						runTrade(packet.getCapital(), packet.getDays());
				} catch (IOException e) {
					// TODO Impossible de lancer l'exécutable fourni en
					// paramètre
					e.printStackTrace();
				}

			}
		});
		client.addSpecificPacketReceivedFromServerEvent(new SpecificPacketReceivedFromServerEvent<DailyValuePacket>() {
			private int count = 0;

			@Override
			public void specificPacketReceivedFromServer(SocketSenderReceiver socket, DailyValuePacket packet) {
				try {
					System.out.println("\"" + packet.getValue() + "\"");
					binIn.write(String.valueOf(packet.getValue()));
					binIn.newLine();
					binIn.flush();
					if (count < days) {
						String line = binOut.readLine();
						System.out.println(line);
						if (line == null)
							line = "wait";
						String[] order = line.split(" ");
						int value = 0;
						if (order[0].equals("buy"))
							value = Integer.parseInt(order[1]);
						else if (order[0].equals("sell"))
							value = -Integer.parseInt(order[1]);
						System.out.println(">>> " + value);
						socket.put(new UserOrderPacket(value));
					} else {
						// TODO il faut terminer
					}
				} catch (IOException e) {
					// TODO Impossible de communiquer avec le programme
					e.printStackTrace();
				}

			}
		});
		client.addSpecificPacketReceivedFromServerEvent(new SpecificPacketReceivedFromServerEvent<EndingPacket>() {

			@Override
			public void specificPacketReceivedFromServer(SocketSenderReceiver arg0, EndingPacket arg1) {
				try {
					binIn.write("--END--");
					binIn.newLine();
					binIn.flush();
					isRunning = false;
					binIn.close();
					binOut.close();
					binIn = null;
					binOut = null;
					client.stop();
				} catch (IOException e) {
					// TODO Impossible de communiquer avec le programme
					e.printStackTrace();
				}

			}
		});
		client.run();
	}

	public void runTestMode() {
		try {
			runTrade(100000, 200);
			int begValue = 1000;
			for (int i = 0; i < 200; i++) {
				begValue += (int)(Math.random() * 20 - 10);
				binIn.write(String.valueOf(begValue));
				binIn.newLine();
				binIn.flush();
				System.out.println("<<< " + begValue);
				System.out.println(">>> " + binOut.readLine());
			}
			System.out.println("<<< --END--");
			binIn.write("--END--");
			binIn.newLine();
			binIn.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void runTrade(Integer capital, Integer days) throws IOException {
		this.days = days;
		ProcessBuilder builder = new ProcessBuilder(tradeBin);
		builder.redirectErrorStream(true);
		Process process = builder.start();
		System.out.println("### run " + tradeBin + " with " + String.valueOf(capital) + " € and " + String.valueOf(days) + " Days ###");
		OutputStream stdinStream = process.getOutputStream();
		InputStream stdoutStream = process.getInputStream();
		binOut = new BufferedReader(new InputStreamReader(stdoutStream));
		binIn = new BufferedWriter(new OutputStreamWriter(stdinStream));
		binIn.write(String.valueOf(capital));
		binIn.newLine();
		binIn.flush();
		binIn.write(String.valueOf(days));
		binIn.newLine();
		binIn.flush();
		isRunning = true;
	}

	public static void main(String[] args) {
		if (args.length == 4) {
			MainClient main = new MainClient(args[0], args[1], args[2], Integer.parseInt(args[3]));
			main.runOnlineMode();
		} else if (args.length == 2 && args[1].equals("--test")) {
			MainClient main = new MainClient(args[0]);
			main.runTestMode();
		} else {
			System.out.println("usage : java -jar client.jar trade_path name ip port | java -jar client.jar trade_path --test");
		}
	}

}
