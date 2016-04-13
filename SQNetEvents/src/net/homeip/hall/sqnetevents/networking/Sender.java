package net.homeip.hall.sqnetevents.networking;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import net.homeip.hall.sqnetevents.packet.Packet;

public class Sender implements Closeable {
	
	private SocketAddress sendAddress;
	
	private SocketChannel channel;
	
	public Sender(String remoteAddress) {
		String[] address = remoteAddress.split(":");
		sendAddress = new InetSocketAddress(address[0], Integer.parseInt(address[1]));
		System.out.println("[NetEvents] Creating sender. SendTo address: " + getSendAddress());
		//establishes connection with remote address
		connect();
	}
	//Sends a packet to the sendAddress, called externally
	public void send(Packet packet) {
		if(getChannel().isOpen()) {
			System.out.println("[NetEvents] Attempting to write packet...");
			try {
				//writes bytebuffer from packet and sends
				//TODO: Add breakdown and EOF implementation
				ByteBuffer byteBuffer = packet.write();
				System.out.println("Sending packet with length of " + byteBuffer.array().length);
				getChannel().write(byteBuffer);
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("[NetEvents] Sent packet to address " + getSendAddress());
		}
		//Will attempt to reestablish connection
		else {
			System.out.println("[NetEvents] Attempting to reestablish connection with address " + getSendAddress());
			ConnectThread connectThread = new ConnectThread();
			connectThread.start();
		}
	}
	
	public SocketAddress getSendAddress() {
		return sendAddress;
	}
	
	public SocketChannel getChannel() {
		return channel;
	}
	//attempts to connect
	private void connect() {
		System.out.println("[NetEvents] Attempting to establish connection to address " + getSendAddress());
		//Will try to connect until it can successfully do so
		ConnectThread connectThread = new ConnectThread();
		connectThread.start();
	}
	//Establishes connection
	private class ConnectThread extends Thread {
		public ConnectThread() {
			super("NetEvents-Connect");
		}
		@Override
		public synchronized void run() {
			//Will attempt to connect until a connection is established/reestablished
			if(getChannel() == null) {
				System.out.println("channel is null");
			}
			//while not yet connected, attempt to connect every 2 seconds
			while((getChannel() == null) || (!(getChannel().isConnected()))) {
				try {
					channel = SocketChannel.open(getSendAddress());
					try {
						sleep(2000L);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("[NetEvents] Successfully established connection to address " + getSendAddress());
		}
	}
	//closes the session
	@Override
	public void close() throws IOException {
		if(getChannel() != null) {
			getChannel().close();
		}
	}
}
