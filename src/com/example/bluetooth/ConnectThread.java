package com.example.bluetooth;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/*
 * 主动连接远程蓝牙的线程，当用户点击列表中某个蓝牙设备时，启动该线程
 */
public class ConnectThread extends Thread {

	
	private static final UUID UUID_COM = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//00001101-0000-1000-8000-00805F9B34FB
	private static final UUID UUID_CHAT = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	private static final int CONNECT_FAIL = 4;
	private static final int CONNECT_SUCCEED_P = 5;

	private BluetoothDevice device;
	private Handler handler;
	private BluetoothSocket socket;



	public ConnectThread(BluetoothDevice d, Handler h) {
		device = d;
		handler = h;
		
	}

	public void run() {
		try {
		
				socket = device.createRfcommSocketToServiceRecord(UUID_COM);//选择UUID_COM表示启用串口调试工具，可以连接蓝牙模块，
			                                                             	//选择UUID_CHAT表示两个手机可以连接通信
			socket.connect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.toString());
			handler.sendEmptyMessage(CONNECT_FAIL);
			socket = null;
		}
		if (socket != null) {
			Message msg = new Message();
			Bundle bundle = new Bundle();
			bundle.putString("name", device.getName());
			msg.setData(bundle);
			msg.what = CONNECT_SUCCEED_P;
			handler.sendMessage(msg);
		}
	}

	public BluetoothSocket getSocket() {
		return socket;
	}
}
