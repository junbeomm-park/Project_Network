package test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class SendAndReceiveSerial implements SerialPortEventListener, MqttCallback {

	private BufferedInputStream bin;
	private InputStream in;
	private OutputStream out;
	private OutputStream out2;
	private SerialPort serialPort;
	private SerialPort serialPort2;
	private CommPortIdentifier portIdentifier;
	private CommPort commPort;
	SendHttp http;
	
	private MqttClient mqttclient;
	private MqttConnectOptions mqttOption;
	
	private ServerSocket server;
	
	public void connect() {
		try {
			server = new ServerSocket(12345);
			Thread t2 = new Thread(new Runnable() {
				@Override
				public void run() {
					while(true) {
					try {
						Socket 	client = server.accept();
						String ip = client.getInetAddress().getHostName();
						System.out.println(ip+"사용자 접속");
						
						new ReceiverThread(client, out2).start();
					} catch (IOException e) {
						e.printStackTrace();
						}
					}
				}
			});
			t2.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public OutputStream getOutput() {
		System.out.println("out2?:"+out2);
		return out2;
	}

	
	
	
	public SendAndReceiveSerial(String portName, boolean mode) {
		try {
			if (mode == true) {
				portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
				System.out.printf("Port Connect : %s\n", portName);
				connectSerial();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SendAndReceiveSerial() {
		super();
		
	}


	public void connectSerial() throws Exception {
		
		if (portIdentifier.isCurrentlyOwned()) {
			System.out.println("Error: Port is currently in use");
		} else {
			commPort = portIdentifier.open(this.getClass().getName(), 3000);
			if (commPort instanceof SerialPort) {
				serialPort = (SerialPort) commPort;
				serialPort.addEventListener(this);
				serialPort.notifyOnDataAvailable(true);
				serialPort.setSerialPortParams(9600,
						SerialPort.DATABITS_8, 
						SerialPort.STOPBITS_1, 
						SerialPort.PARITY_NONE);
				in = serialPort.getInputStream();
				bin = new BufferedInputStream(in);
				out = serialPort.getOutputStream();
				
				serialPort2 = (SerialPort) commPort;
				serialPort2.setSerialPortParams(9600,
						SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);
				System.out.println("out2 ?");
				out2 = serialPort2.getOutputStream();
				
				
			} else {
				System.out.println("Error: Only serial ports are handled by this example.");
			}
		}
	}
	
	// Asynchronized Receive Data
	// --------------------------------------------------------
	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:
			byte[] readBuffer = new byte[256];

			try {
				while (bin.available() > 4) {
					int numBytes = bin.read(readBuffer);
					System.out.println(numBytes+"bytes");
				}
				String ss = new String(readBuffer);
				
				String[] ssarr = ss.split(" ");
				if(ssarr.length>1) {
					
					String ss1 = ssarr[0];
					String ss2 = ssarr[1];
					if(ss1!=null & ss2!=null) {
						http = new SendHttp();
						System.out.println(ss1+","+ss2);
						http.sendData(ss1,ss2);
				}
				
					
					System.out.println("Receive Low Data:" + ss1+","+ss2 + "||");
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
	}
	
	public void close() throws IOException {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (in != null) {
			in.close();
		}
		if (out != null) {
			out.close();
		}
		if (commPort != null) {
			commPort.close();
		}

	}
	
	//MQTT (LED정보 mqtt로 받아오기)==========================================
	public SendAndReceiveSerial init(String server, String clientId) {
		mqttOption = new MqttConnectOptions();
		mqttOption.setCleanSession(true);
		mqttOption.setKeepAliveInterval(30);
		try {
			mqttclient = new MqttClient(server,  clientId);
			mqttclient.setCallback(this);
			mqttclient.connect(mqttOption);
		} catch (MqttException e) {
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public void connectionLost(Throwable arg0) {
		
	}


	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		
	}

	
	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.println("===============메시지 받아오기===========");
		System.out.println(message+","+"topic: "+topic+", id: "+message.getId()+", Payload: "+
																		new String(message.getPayload()));
		String msg = new String(message.getPayload());
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					if(msg.equals("led_on")) {
						out.write('o');
					}else if(msg.equals("led_off")){
						out.write('f');
					}
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
		public boolean subscribe(String topic) {
			try {
			if(topic!=null) {
					mqttclient.subscribe(topic, 0); 
				}
			} catch (MqttException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		

	public static void main(String[] args) {
		SendAndReceiveSerial ss = new SendAndReceiveSerial("COM5", true);
		ss.init("tcp://192.168.0.29:1883", "myid").subscribe("led");
		ss.connect();
		
	}



	

	
}
