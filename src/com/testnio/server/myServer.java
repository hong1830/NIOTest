package com.testnio.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.testnio.util.MyRequestObject;
import com.testnio.util.MyResponseObject;
import com.testnio.util.SerializableUtil;

public class myServer {
	/*
	 * NIO server
	 */
	private final static Logger logger = Logger.getLogger(myServer.class
			.getName());

	public static void main(String[] args) {
		Selector selector = null;
		ServerSocketChannel serverSocketChannel = null;
		try {
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.socket().setReuseAddress(true);
			serverSocketChannel.socket().bind(new InetSocketAddress(10000));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

			while (selector.select() > 0) {
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SelectionKey readKey = it.next();
					it.remove();
					execute((ServerSocketChannel) readKey.channel());
				}
			}

		} catch (ClosedChannelException e) {
			logger.log(Level.SEVERE, null, e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, null, e);
		} finally {
			try {
				selector.close();
			} catch (Exception e) {
			}
			try {
				serverSocketChannel.close();
			} catch (Exception e) {
			}
		}
	}

	public static void execute(ServerSocketChannel serverSocketChannel)
			throws IOException {
		SocketChannel socketChannel = null;
		try{
			socketChannel = serverSocketChannel.accept();
			MyRequestObject myRequestObject = receiveData(socketChannel);
			logger.log(Level.INFO,myRequestObject.toString());
			
			MyResponseObject myResponseObject = new MyResponseObject(
					"response for " + myRequestObject.getName(),
					"response for " + myRequestObject.getValue());
			sendData(socketChannel, myResponseObject);
			logger.log(Level.INFO,myResponseObject.toString());
		}finally{
			try {
				socketChannel.close();
			} catch (Exception e) {}
		}

		
	}

	public static MyRequestObject receiveData(SocketChannel socketChannel)
			throws IOException {
		MyRequestObject myRequestObject = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try {
			byte[] bytes;
			int size = 0;
			while ((size = socketChannel.read(buffer)) >= 0) {
				buffer.flip();
				bytes = new byte[size];
				buffer.get(bytes);
				baos.write(bytes);
				buffer.clear();
			}
			bytes = baos.toByteArray();
			Object obj = SerializableUtil.toObject(bytes);
			myRequestObject = (MyRequestObject) obj;
		} finally {
			try {
				baos.close();
			} catch (Exception e) {
			}
		}
		return myRequestObject;
	}

	public static void sendData(SocketChannel socketChannel,
			MyResponseObject myResponseObject) throws IOException {
		byte[] bytes = SerializableUtil.toBytes(myResponseObject);
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		socketChannel.write(buffer);
	}
}
