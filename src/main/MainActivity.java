package main;

import com.example.bluetooth.*;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	// 消息处理器使用的常量
	private static final int FOUND_DEVICE = 1; // 发现设备
	private static final int START_DISCOVERY = 2; // 开始查找设备
	private static final int FINISH_DISCOVERY = 3; // 结束查找设备
	private static final int CONNECT_FAIL = 4; // 连接失败
	private static final int CONNECT_SUCCEED_P = 5; // 主动连接成功
	private static final int CONNECT_SUCCEED_N = 6; // 收到连接成功
	private static final int RECEIVE_MSG = 7; // 收到消息
	private static final int SEND_MSG = 8; // 发送消息
	private static final int CONNECT_BREAKDOWN = 9;//连接中断
	

	ConnectedThread connectedThread; // 与远程蓝牙连接成功时启动
	ConnectThread connectThread; // 用户点击列表中某一项，要与远程蓝牙连接时启动
	AcceptThread acceptThread; // 手机连接手机进行数据通信时启动

	// 布局控件
	private Button bLink, bSend, bShow,bClear,bQuery1,bQuery2,bQuery3,bQuery4;

	private TextView stateText,textView;
	private EditText sendText;
	private ScrollView scrollView;
	private LinearLayout chatContent;

	// 连接设备对话框相关控件
	private Dialog dialog;
	private ProgressBar discoveryPro;
	private ListView foundList;
	private ListView bondedList;
	List<BluetoothDevice> foundDevices;

	
	BluetoothSocket socket;
	BluetoothAdapter bluetoothAdapter = null;
	boolean is = false;
	// 广播接收器，主要是接收蓝牙状态改变时发出的广播
	private BroadcastReceiver mReceiver;
	List<String> Rssi;
	private String buff = "";
    int weightDec = 0;

	// 消息处理器..日理万鸡的赶脚...
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case START_DISCOVERY:
				discoveryPro.setVisibility(View.VISIBLE);
				break;
			case FINISH_DISCOVERY:
				discoveryPro.setVisibility(View.INVISIBLE);
				break;
			case CONNECT_FAIL:
				Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
				break;
			case CONNECT_BREAKDOWN:
				stateText.setText("未连接");
				stateText.setTextColor(0xffff0000);
				acceptThread.cancel();
				if(bluetoothAdapter!=null){
					acceptThread=new AcceptThread(mHandler, bluetoothAdapter);
					acceptThread.start();
				}
				break;
			case CONNECT_SUCCEED_P:
			case CONNECT_SUCCEED_N:
				System.out.println("连接成功-----");
				if (msg.what == CONNECT_SUCCEED_P) {
					if (acceptThread != null) {
						acceptThread.interrupt();
					}
					socket = connectThread.getSocket();
					connectedThread = new ConnectedThread(socket, mHandler);
					connectedThread.start();
				} else {
					if (connectThread != null) {
						connectThread.interrupt();
					}
					socket = acceptThread.getSocket();
					connectedThread = new ConnectedThread(socket, mHandler);
					connectedThread.start();
				}

				String stateStr = msg.getData().getString("name") + "：已连接";
				
				stateText.setText(stateStr);
				stateText.setTextColor(0xff00ff00);
				Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();

				break;
			case RECEIVE_MSG:
			case SEND_MSG:
				String chatStr = msg.getData().getString("str");//接收数据
                buff += chatStr;
                if (buff.length() > 120) {
                    int len = buff.length();
                    int index = buff.indexOf("g");
                    if(index > 0 && index + 18 < len) {
                        String weight = buff.substring(index + 14, index + 18);
                        String hex = weight.substring(2, 4);
                        char bitH = hex.charAt(0);
                        char bitL = hex.charAt(1);
                        if (Character.isDigit(bitH) && Character.isDigit(bitL)){  // 判断是否是数字
                            weightDec = 16 * Integer.parseInt(String.valueOf(bitH));
                            weightDec += Integer.parseInt(String.valueOf(bitL));
                        }
                        String weightStr = String.valueOf(weightDec);
//                        weight = weight + "\n";
////                        textView.append(weight);
                        weightStr = weightStr + "kg\n";
                        textView.append(weightStr);
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                        buff = "";
                    }
                }
//                switch (isWeight) {
//                    case 1:
//                        weight += chatStr;
//                        isWeight = 2;
//                        break;
//                    case 2:
//                        weight += chatStr;
//                        isWeight = 0;
//                        textView.append(weight);
//                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
//                        weight = "";
//                        break;
//                    default:
//                        if(chatStr.startsWith("Total Weight")) {
//                            isWeight = 1;
//                        }
//                }

//				textView.append(chatStr);
//				scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
//				TextView text = new TextView(Activity11.this);
//				text.setText(chatStr);
//				if (msg.what == SEND_MSG) {
//					text.setBackgroundResource(R.drawable.chatfrom_bg_normal);
//					text.setPadding(40, 10, 30, 10);
//					text.setTextColor(0xff0000ff);
//				} else {
//					text.setBackgroundResource(R.drawable.chatto_bg_normal);
//					text.setPadding(80, 10, 30, 10);
//					text.setTextColor(0xff000000);
//				}
//				text.setTextSize(20);
//				chatContent.addView(text);
//				scrollView.scrollTo(0, chatContent.getHeight());
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			Toast.makeText(this, "本机没有蓝牙功能！", Toast.LENGTH_SHORT).show();
			finish();
		}
		// 判断本机是否有蓝牙和是否处于可用状态
		if (!bluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, 1);
		}		
		acceptThread=new AcceptThread(mHandler, bluetoothAdapter);
		acceptThread.start();

		bLink = (Button) findViewById(R.id.bLink);
		bSend = (Button) findViewById(R.id.bSend);
		bShow = (Button) findViewById(R.id.bShow);
		bClear = (Button) findViewById(R.id.bClear);
		stateText = (TextView) findViewById(R.id.stateText);
		sendText = (EditText) findViewById(R.id.sendText);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		chatContent = (LinearLayout) findViewById(R.id.chatContent);
		bQuery1 = (Button) findViewById(R.id.query1);
		bQuery2 = (Button) findViewById(R.id.query2);
		bQuery3 = (Button) findViewById(R.id.query3);
		bQuery4 = (Button) findViewById(R.id.query4);
		textView = (TextView)findViewById(R.id.show);

		// 连接设备按钮监听器
		bLink.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (connectThread != null) {
					connectThread.interrupt();
				}
				if (connectedThread != null) {
					connectedThread.interrupt();
				}
				if (acceptThread != null) {
					acceptThread.interrupt();
				}
				bluetoothAdapter.cancelDiscovery();
				
				foundDevices = new ArrayList<BluetoothDevice>();
				Rssi = new ArrayList<String>();

				bluetoothAdapter.startDiscovery();

				/*
				 * 通过LayoutInflater得到对话框中的三个控件
				 * 第一个ListView为局部变量，因为它显示的是已配对的蓝牙设备，不需随时改变
				 * 第二个ListView和ProgressBar为全局变量
				 */
				LayoutInflater inflater = getLayoutInflater();
				View dialogView = inflater.inflate(R.layout.dialog, null);
				discoveryPro = (ProgressBar) dialogView.findViewById(R.id.discoveryPro);
				bondedList = (ListView) dialogView.findViewById(R.id.bondedList);
				foundList = (ListView) dialogView.findViewById(R.id.foundList);

				// 将已配对的蓝牙设备显示到第一个ListView中
				Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
				final List<BluetoothDevice> bondedDevices = new ArrayList<BluetoothDevice>();
				if (deviceSet.size() > 0) {
					for (Iterator<BluetoothDevice> it = deviceSet.iterator(); it.hasNext();) {
						BluetoothDevice device = (BluetoothDevice) it.next();
						bondedDevices.add(device);
					}
				}bondedList.setAdapter(new MyAdapter(MainActivity.this,bondedDevices,false,Rssi));

				//两个ListView绑定监听器
				bondedList.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
						BluetoothDevice device = bondedDevices.get(arg2);
						connect(device);
					}
				});
				foundList.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
						BluetoothDevice device = foundDevices.get(arg2);
						connect(device);

					}
				});

				
				dialog = new Dialog(MainActivity.this,R.style.Dialog);
				//dialog.setCancelable(false);//调用这个方法时，按对话框以外的地方不起作用。按返回键也不起作用
				dialog.setOnDismissListener(new OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
						bluetoothAdapter.cancelDiscovery();
					}
					
				});
				LayoutInflater mInflater = LayoutInflater.from(MainActivity.this);
				Window window = dialog.getWindow();
				window.setContentView(dialogView);
				dialog.show();
				
				
				
				

			}
		});

		// 发送消息按钮监听器
		bSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (socket == null)
					Toast.makeText(MainActivity.this, "请先连接蓝牙设备",Toast.LENGTH_SHORT).show();
				else if(sendText.getText().toString().equals(""))
					Toast.makeText(MainActivity.this, "发送内容不能为空",Toast.LENGTH_SHORT).show();
				else {
					String sendStr = sendText.getText().toString();
					connectedThread.write(sendStr);//发送数据
				}
			}
		});
		
		
		
		bQuery1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (socket == null)
					Toast.makeText(MainActivity.this, "请先连接蓝牙设备",Toast.LENGTH_SHORT).show();
				else {
					String sendStr = "查看空气质量\n";
					connectedThread.write(sendStr);//发送数据
				}
			}
		});
		bQuery2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (socket == null)
					Toast.makeText(MainActivity.this, "请先连接蓝牙设备",Toast.LENGTH_SHORT).show();
				else {
					String sendStr = "你好\n";
					connectedThread.write(sendStr);//发送数据
				}
			}
		});
		bQuery3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (socket == null)
					Toast.makeText(MainActivity.this, "请先连接蓝牙设备",Toast.LENGTH_SHORT).show();
				else {
					String sendStr = "湿度校准\n";
					connectedThread.write(sendStr);//发送数据
				}
			}
		});
		bQuery4.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (socket == null)
					Toast.makeText(MainActivity.this, "请先连接蓝牙设备",Toast.LENGTH_SHORT).show();
				else {
					String sendStr = "温度校准\n";
					connectedThread.write(sendStr);//发送数据
				}
			}
		});
		
		
		// 使蓝牙可被发现
		bShow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				startActivity(intent);
			}
		});
		// 清除传输显示的数据
		bClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//chatContent.removeAllViews();
				textView.setText("");
			}
		});
		
		// 注册广播接收器
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				String actionStr = arg1.getAction();
				if (actionStr.equals(BluetoothDevice.ACTION_FOUND)) {
					BluetoothDevice device = arg1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					Toast.makeText(MainActivity.this,"找到蓝牙设备：" + device.getName(), Toast.LENGTH_SHORT).show();
					if(device.getName()!=null && device.getBondState() != BluetoothDevice.BOND_BONDED){
						
						
						if(foundDevices.size()>0) {//过滤掉蓝牙地址重复的蓝牙设备
							for(int i = 0;i<foundDevices.size();i++) {
								if(device.getAddress().equals(foundDevices.get(i).getAddress()))return;
							}
							
						}
						Rssi.add(""+arg1.getExtras().getShort(BluetoothDevice.EXTRA_RSSI));
						foundDevices.add(device);
						foundList.setAdapter(new MyAdapter(MainActivity.this,foundDevices,true,Rssi));
					}
					
				} else if (actionStr.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
					mHandler.sendEmptyMessage(START_DISCOVERY);
				} else if (actionStr.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
					mHandler.sendEmptyMessage(FINISH_DISCOVERY);
				} 
			}

		};
		IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		IntentFilter filter3 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

		registerReceiver(mReceiver, filter1);
		registerReceiver(mReceiver, filter2);
		registerReceiver(mReceiver, filter3);
	}

	
	/*
	 * 退出程序时处理一下后事，取消注册广播接收器，中止线程，关闭socket
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		acceptThread.cancel();
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (connectThread != null) {
			connectThread.interrupt();
		}
		if (connectedThread != null) {
			connectedThread.interrupt();
		}
		if (acceptThread != null) {
			acceptThread.interrupt();
		}
		if(bluetoothAdapter.isEnabled()){
			Toast.makeText(this, "请手动关闭蓝牙", Toast.LENGTH_SHORT).show();
		}
		this.finish();
	}
	public void connect(BluetoothDevice device) {
		bluetoothAdapter.cancelDiscovery();
		dialog.dismiss();
		Toast.makeText(this, "正在与 " + device.getName() + " 连接 .... ",Toast.LENGTH_LONG).show();
		connectThread = new ConnectThread(device, mHandler);
		connectThread.start();
	}
	// 请求打开蓝牙的回调函数，若无法打开，则关闭程序
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 1:
			if (resultCode != Activity.RESULT_OK) {
				Toast.makeText(this, "无法打开蓝牙，程序关闭", Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
		}
	}

}













