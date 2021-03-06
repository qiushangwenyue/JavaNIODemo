package com.nio.channels.b_classes;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author y15079
 * @create 2017-11-13 16:51
 * @desc
 *
 * 抽象类
 *
 * 客户端
 *
 **/
public class SocketChannel_NioClient_Demo {
	//管道管理器
	private Selector selector;

	public SocketChannel_NioClient_Demo init(String serverIp, int port) throws Exception{
		//获取socket通道
		SocketChannel channel=SocketChannel.open();
		//设置为非阻塞模式
		channel.configureBlocking(false);
		//获得通道管理器
		selector=Selector.open();

		//客户端连接服务器，需要调用channel.finishConnect();才能实际完成连接。
		channel.connect(new InetSocketAddress(serverIp,port));
		//为该通道注册SelectionKey.OP_CONNECT事件
		channel.register(selector, SelectionKey.OP_CONNECT);
		return this;
	}

	public void listen() throws Exception{
		System.out.println("客户端启动");
		//轮询访问selector
		while (true){
			//选择注册过的io操作的事件（第一次为SelectionKey.OP_CONNECT）
			selector.select();
			Iterator<SelectionKey> ite=selector.selectedKeys().iterator();
			while (ite.hasNext()){
				SelectionKey key=ite.next();
				//删除已选的key，防止重复处理
				ite.remove();
				if (key.isConnectable()){
					SocketChannel channel=(SocketChannel)key.channel();

					//如果正在连接，则完成连接
					if (channel.isConnectionPending()){
						channel.finishConnect();
					}
					//设置为非阻塞模式
					channel.configureBlocking(false);
					//向服务器发送消息
					channel.write(ByteBuffer.wrap(new String("send message to server.").getBytes()));

					//连接成功后，注册接收服务器消息的事件
					channel.register(selector,SelectionKey.OP_READ);
					System.out.println("客户端连接成功");
				}else if (key.isReadable()){
					//有可读数据事件。
					SocketChannel channel=(SocketChannel)key.channel();

					ByteBuffer buffer=ByteBuffer.allocate(30);
					channel.read(buffer);
					byte[] data=buffer.array();
					String message=new String(data);

					System.out.println("recevice message from server:, size:"+buffer.position()+" "+message);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception{
		new SocketChannel_NioClient_Demo().init("127.0.0.1",9981).listen();
	}
}
