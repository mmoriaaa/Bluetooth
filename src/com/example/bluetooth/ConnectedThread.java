package com.example.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
/*
已建立连接后启动的线程，需要传进来两个参数
socket用来获取输入流，读取远程蓝牙发送过来的消息
handler用来在收到数据时发送消息
*/
public class ConnectedThread extends Thread {
	
	private static final int CONNECT_BREAKDOWN = 9;//连接中断
	private static final int RECEIVE_MSG = 7;
	private static final int SEND_MSG=8;
	private boolean isStop;
	private BluetoothSocket socket;
	private Handler handler;
	private InputStream is;
	private OutputStream os;
	int size;
	public ConnectedThread(BluetoothSocket s,Handler h){
		socket=s;
		handler=h;
		isStop=false;
	}
	public void run(){
		System.out.println("connectedThread.run()");
		byte[] buf;
		
		while(!isStop){
			size=0;
			buf=new byte[4096];
			try {
				is=socket.getInputStream();
				System.out.println("等待数据");
				size=is.read(buf);
				System.out.println("读取了一次数据");
			} catch (IOException e) {
				e.printStackTrace();
				isStop=true;

				handler.sendEmptyMessage(CONNECT_BREAKDOWN);
				
			}
			if(size>0){
				byte[] buf_read = new byte[size]; 
				for(int i = 0;i<size;i++)buf_read[i] = buf[i];
				sendMessageToHandler_Ascii(buf_read, RECEIVE_MSG);//把读取到的数据放进Bundle再放进Message，然后发送到Activity11.java的mHandler中
			}
		}
	}
	
	public void write(String s){
		
		byte[] buf_send = null;
		try {
			buf_send = s.getBytes("gbk");//解决串口通信编码问题
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		try {
			os=socket.getOutputStream();
			os.write(buf_send);
		} catch (IOException e) {
			e.printStackTrace();
		}
		sendMessageToHandler_Ascii(buf_send, SEND_MSG);
		
	}
	
	private void sendMessageToHandler_Hex(byte[] buf,int mode){//用HEX显示数据
		
		String msgStr = "";
		for (int i = 0; i < size; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			msgStr += hex.toUpperCase();

		}
		
		
		
		Bundle bundle=new Bundle();
		bundle.putString("str", msgStr);
		Message msg=new Message();
		msg.setData(bundle);
		msg.what=mode;
		handler.sendMessage(msg);
		

	}
	
	
	
	
private void sendMessageToHandler_Ascii(byte[] buf,int mode){//用ASCII码显示数据
		
		String msgStr = null;
		try {
			msgStr = new String(buf,"gbk");//buf的编码格式系统自动判断，但是需要指定编码后的格式
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Bundle bundle=new Bundle();
		bundle.putString("str", msgStr);
		Message msg=new Message();
		msg.setData(bundle);
		msg.what=mode;
		handler.sendMessage(msg);
		

	}


}









	




