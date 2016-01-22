package com.example.localadministrator.runtime;

import android.util.Log;

/**
 * Created by Zhy on 2016/1/22.
 */
public class ChannelManager {
	private static final String TAG = ChannelManager.class.getCanonicalName();

	public ChannelManager(){

	}
	public void ChannelChooseStrategy(){
		if(!ParameterManager.assistName.equals("any")){
			Log.d(TAG,"will choose BTLE channel to execute this task!" );
			ParameterManager.isWifi = false;
			ParameterManager.isBTLE = true;
		}else{
			ParameterManager.isBTLE = false;
			ParameterManager.isWifi = true;
			Log.d(TAG,"will choose Wifi channel to execute this task!");
			//choose wifi
		}
	}
}
