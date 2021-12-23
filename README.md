# Network protocol

### 개요

- **아두이노, 웹, 안드로이드를 이용한 네트워크 시스템 구축**



### 목표

- 각 구간 별 통신(HTTP, TCP/IP, MQTT, Serial, FCM)을 이용해서 데이터를 전달
- Web & App을 연동해 web과app에서 데이터 통신
- DB연동을 통해 데이터 값 저장



### 시스템 구성도

<img src="img\system.PNG"/>



### 프로젝트 환경 및 기술스택

- 언어 
  -  Java
  -  JavaScript
  -  HTML
- 프로그램 
  - Spring MVC
  - Eclipse 4.16.0
  - Android Studio 4.2.0
  - Apache Tomcat 9.0
  - Google FireBase
  - Arduino 1.8.16

- 통신
  - HTTP
  - MQTT
  - TCP/IP
  - Serial
- 협업도구 
  - Allo
  - GitHub
  - Zoom

### 프로젝트 맴버

<img src="img\member.PNG"/>

## web

- HTTP 통신 및 FCM

```java
@RequestMapping("/data.mc")
	@ResponseBody
	public void data(HttpServletRequest request) throws IOException{
		String btn = request.getParameter("btn");
		System.out.println("Button Status : "+btn);
		String temp = request.getParameter("temp");
		double f_temp = Double.parseDouble(temp);
		System.out.println("Temp Status : "+temp+"ºC");
		data_log.debug(f_temp+" : "+btn);
		try {
			DataVO datainfo = new DataVO(btn,temp);
			service.modify(datainfo);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(btn!=null && temp!=null) {
			if(btn.equals(1+"")) { 
				try {
					FcmUtil_btn.sendServer(btn);
					} catch (Exception e) {
						e.printStackTrace(); 
						}
					}else if(f_temp >= 25) { 
						try {
							FcmUtil_temp.sendServer(temp);
							} catch (Exception e) {
								e.printStackTrace(); 
								}
							}
			}
	}

```



- Main Server -> Latte Panda / MQTT 통신

```java
//MQTT통신으로 메시지를 전송하기 위한 객체
public class MyMqtt_Pub_client {
	private MqttClient client;
	public MyMqtt_Pub_client(){
		try {
			client = new MqttClient("tcp://192.168.0.29:1883", "myid2");
			client.connect();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	public boolean send(String topic, String msg) {
		MqttMessage message = new MqttMessage();
		message.setPayload(msg.getBytes()); // 전송할 메시지
		try {
			client.publish(topic, message); // 토픽을 설정해서 메시지를 보낸다.
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		} 
		return true;
	}
	//종료
	public void close() {
		try {
		if(client != null) {
			client.disconnect();
				client.close(); 
			} 
		} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}

	public static void main(String[] args) {
		MyMqtt_Pub_client sender = new MyMqtt_Pub_client();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				int i = 1;
				String msg = "";
				while(true) {
					if(i==5) {
						break;
					}else {
					if(i%2==1) {
						msg = "led_on";
					}else {
						msg = "led_off";
					}
				}
				sender.send("led", msg);
				i++;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
				sender.close();
			}
		}).start();
	}

}

```



- Latte Panda / Send & Receive ( TCP/IP, MQTT)

  

```java
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
```

- Latte Panda / 센서값 HTTP통신으로 보내기

```java
public class SendHttp {
	public SendHttp() {
		super();
		
	}
	
	class SendThread extends Thread{

		String temp;
		String btn;
		
		//String urlstr = "http://192.168.0.158/semi/iot2.mc";
		String urlstr = "http://192.168.0.29/np/data.mc";
		URL url = null;
		HttpURLConnection con = null;

		BufferedReader br = null;
		
		public SendThread() {
			
		}
		
		public SendThread(String temp, String btn) {
			this.temp = temp;
			this.btn = btn;
		}
		
		@Override
		public void run() {
			//request $ response
			try {
				
				url = new URL(urlstr + "?temp="+temp + "&btn="+btn);
				con = (HttpURLConnection) url.openConnection();
				con.setReadTimeout(5000);
				con.setRequestMethod("POST");
				

				br = new BufferedReader(
						new InputStreamReader(
								con.getInputStream()));

				String str = "";


			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				con.disconnect();
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		}
		
		
	}
	
	
	
	public void sendData(String temp,String btn) {
		SendThread st = new SendThread(temp, btn);
		st.start();
	
	}
```

- Latte Panda / Receive Thread (TCP/IP)

```java
public class ReceiverThread extends Thread{
	Socket client;
	BufferedReader clientin;
	PrintWriter clientout;
	SendAndReceiveSerial serialobj;
	OutputStream serialout;
	
	public ReceiverThread(Socket client, OutputStream serialout) {
		super();
		this.client = client;
		this.serialout = serialout;
		try {
			clientin = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
			clientout = new PrintWriter(this.client.getOutputStream(),true);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		super.run();
		while(true) {
			try {
				String msg = clientin.readLine();
				System.out.println("클라이언트가 보낸 메세지 : "+msg);
				if(msg.equals("led_on")) {
					serialout.write('o');
				}else if(msg.equals("led_off")) {
					serialout.write('f');
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
```





## Android

#### 구성

<img src="img\home.PNG" style="zoom:80%;" />

- 앱실행시 초기화면



<img src="img\led.PNG" style="zoom: 75%;" /> <img src="img\ledon.PNG" style="zoom: 75%;" />

- LED의 데이터값을 받아와서 현 상태 출력

<img src="img\temp.PNG" style="zoom:80%;" />

- 온도의 데이터값을 실시간으로 FCM을 이용하여 출력



#### FCM

- IOT장비의 버튼 및 LED센서의 동작여부를 FCM 상단바 알림을 통하여 전송
- IOT장비의 온도 센서의 데이터값을 FCM을 통하여 App 화면에 출력
- HTTP통신을 이용하여 LED센서를 제어



``` java
public void onMessageReceived(@NonNull  RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived 호출됨.");

        String from = remoteMessage.getFrom();
        Map<String, String> data = remoteMessage.getData();
        String contents1 = data.get("c1");
        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        Log.d(TAG, "from : " + from + ", contents : " + contents1+" " +title+" "+body);
        if(body.equals("Button ON")){
            sendToActivity(getApplicationContext(), from, title, body);
        }else if(body.equals("LED ON")){
            sendToActivity(getApplicationContext(), from, title, body);

        }else{
            sendToActivity2(getApplicationContext(), from, contents1);
        }
    }

    private void sendToActivity2(Context context, String from, String contents1) {
        Intent intent = new Intent(context, SensorActivity.class);
        intent.putExtra("from", from);
        intent.putExtra("c1",contents1);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    private void sendToActivity(Context context, String from, String title, String body) {
        Intent intent = new Intent(context, semi_web.class);
        intent.putExtra("from", from);
        intent.putExtra("title", title);
        intent.putExtra("body", body);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
```

- FCM 상단바 알림을 수신 하기 위해 코드 구성



<img src="img\fcmled.png"/>

- FCM_LED

<img src="img\fcmtemp.png"/>

- FCM_Temp

<img src="img\fcmbtn.png"/>

- FCM_btn
- IOT장비에서 송신한 데이터 값을 메인서버에서 수신하여 FireBase Server로 전송



#### TCP/IP

<img src="img\tcpip.PNG"/>

- 장비의 센서를 제어 하기 위해 코드작성
