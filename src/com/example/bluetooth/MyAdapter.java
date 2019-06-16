package com.example.bluetooth;

import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/*
 * 为ListView提供内容的自定义Adapter
 * 卖萌专用..其实用ArrayAdapter就能完成功能了
 */

public class MyAdapter extends BaseAdapter {

	private Context context;
	private List<BluetoothDevice> mBluetoothDeviceList;
	List<String> Rssi;
	boolean bool;
	public MyAdapter(Context context, List<BluetoothDevice> mBluetoothDeviceList,boolean bool,List<String> Rssi) {
		this.context = context;
		this.mBluetoothDeviceList = mBluetoothDeviceList;
		this.Rssi = Rssi;
		this.bool = bool;
	}

	@Override
	public int getCount() {
		return mBluetoothDeviceList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout=(LinearLayout) inflater.inflate(R.layout.list_item, null);
		TextView name=(TextView)layout.findViewById(R.id.name);
		TextView address=(TextView)layout.findViewById(R.id.address);
		TextView rssi = (TextView)layout.findViewById(R.id.rssi);
		name.setTextSize(20f);
		name.setText(mBluetoothDeviceList.get(arg0).getName());
		address.setText(mBluetoothDeviceList.get(arg0).getAddress());
		if(bool)
		rssi.setText(Rssi.get(arg0).toString());
		return layout;
	}
}
