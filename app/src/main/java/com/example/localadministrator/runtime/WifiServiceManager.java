package com.example.localadministrator.runtime;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.widget.Toast;

public class WifiServiceManager extends Service {
	private WifiManager wifiManager = null;
	private Context mContext;
	public WifiServiceManager(Context c){
		mContext = c;
	}
	public void setWifiState(){
		wifiManager= (WifiManager)getSystemService(mContext.WIFI_SERVICE);
		wifiManager.setWifiEnabled(true);
		System.out.println("wifi state------->" + wifiManager.getWifiState());
		Toast.makeText(WifiServiceManager.this, "当前WIFI网卡的状态为"+wifiManager.getWifiState(),Toast.LENGTH_LONG).show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
