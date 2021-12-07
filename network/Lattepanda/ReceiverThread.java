package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

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
	}
	
}
