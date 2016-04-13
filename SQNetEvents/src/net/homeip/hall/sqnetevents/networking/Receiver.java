package net.homeip.hall.sqnetevents.networking;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import net.homeip.hall.sqnetevents.packet.EventPacket;




public class Receiver implements Closeable {
	
	private BindThread bindThread;
	
	private ListenThread listenThread;
	
	private SocketAddress bindAddress;
	
	private ServerSocketChannel server;
	
	private SocketChannel client;
	
	//Binds and listens
	public Receiver(String listenAddress) {
		System.out.println("[NetEvents] Creating receiver. ListenAt address: " + listenAddress);
		String[] address = listenAddress.split(":");
		bindAddress = new InetSocketAddress(address[0], Integer.parseInt(address[1]));
		//attempts to bind
		bindAndListen();
	}
	private void bindAndListen() {
		//begins attempting to bind to the port
		bindThread = new BindThread();
		getBindThread().start();
		//when bound, will call listen()
	}
	//begins to listen on the bound port
	private void listen() {
		//begins listening on the port
		listenThread = new ListenThread();
		getListenThread().start();
	}
	
	public SocketAddress getBindAddress() {
		return bindAddress;
	}
	
	public ServerSocketChannel getServer() {
		return server;
	}
	
	public SocketChannel getClient() {
		return client;
	}
	
	public BindThread getBindThread() {
		return bindThread;
	}
	
	public ListenThread getListenThread() {
		return listenThread;
	}
	//Closes the session
	@Override
	public void close() throws IOException {
		if(getServer() != null) {
			getServer().close();
		}
		if(getClient() != null) {
			getClient().close();
		}
	}
	//Will attempt to bind to correct port until successful
	private class BindThread extends Thread {
		public BindThread() {
			super("NetEvents-Bind");
		}
		@Override
		public synchronized void run() {
			//opens the socket channel
			System.out.println("[NetEvents] Attempting to bind to address " + getBindAddress());
			try {
				server = ServerSocketChannel.open();
				getServer().configureBlocking(true);
				//binds to the proper port
				while(!(getServer().socket().isBound())) {
					getServer().bind(getBindAddress());
				}
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			System.out.println("[NetEvents] Bound to address " + getBindAddress());
			//begins or resumes listening
			if(getListenThread() == null) {
				listen();
			}
			else {
				getListenThread().notify();
			}
			//waits until unexpected close, whence notify() will be called from listenThread
		}
	}
	//listens until session closed
	private class ListenThread extends Thread {
		public ListenThread() {
			super("NetEvents-Listener");
	    }
		//waits to receive an event from another port, then processes
	    @Override
	    public synchronized void run() {
	    	System.out.println("[NetEvents] Listening at address" + getBindAddress());
	    	try {
	    		//if not already open
	    		if((getServer() != null) && (getServer().isOpen()) && (!(getClient() == null))) {
	    			client = getServer().accept();
	    			System.out.println("[NetEvents] The server socket is open, waiting for connections...");
		            System.out.println("[NetEvents] Received connection from: " + getClient().getRemoteAddress());
	    		}
	    		//waits until opened
	    		else {
	    			try {
	    				//notify called from bindthread
	    				wait();
	    			}
	    			catch(InterruptedException e) {
	    				e.printStackTrace();
	    			}
	    		}
	    		//when everything's working fine, listen for incoming messages
	    		while ((getServer().isOpen()) && (client.isOpen())) {
	                //Reads the object and reconstructs from byteinputstream
	    			//TODO: Remove magic number and add breakdown + EOF detection
	                byte[] bytes = new byte[4096];
	                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
	                client.read(byteBuffer);
	                System.out.println("[NetEvents] Reading");
	                EventPacket event = (EventPacket) EventPacket.read(byteBuffer);
	                System.out.println("[NetEvents] Read EventPacket from bytebuffer");
	                //fires the event
	                event.handle();
	                System.out.println("[NetEvents] Fired event");
	            }
	        }
	    	catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}
}
