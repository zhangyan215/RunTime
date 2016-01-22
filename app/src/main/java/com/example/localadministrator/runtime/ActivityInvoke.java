package com.example.localadministrator.runtime;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;



/**
 * Created by Zhy on 2016/1/12.
 * the class is used to determine which execution app will be invoked.
 */
public class ActivityInvoke {
	private static final String TAG = ActivityInvoke.class.getCanonicalName();
	Context mContext =null;
	public ActivityInvoke(Context mContext){
		this.mContext = mContext;

	}
	public void activityStart() {
		ComponentName comp = null;
		Log.d(TAG, "the serviceType is:" + ParameterManager.serviceType);
		Intent intent = new Intent();
		if (ParameterManager.serviceType.contains("gps")) {
			comp = new ComponentName("com.example.testb.testb", "com.example.testb.testb.MainActivity");
			Log.d(TAG,"com.example.testb.testb will be started");
			//Toast.makeText(mContext,"the execution app is testb which will get the location info",Toast.LENGTH_SHORT).show();
		}
		if (ParameterManager.serviceType.contains("face")) {
			comp = new ComponentName("com.example.servicetest.facedetectiontest",
					"com.example.servicetest.facedetectiontest.MainActivity");
			Log.d(TAG,"com.example.servicetest.facedetectiontest will be started");
			//Toast.makeText(mContext,"the execution app is face_detection which will calculate the number of the faces",Toast.LENGTH_SHORT).show();
		} else {
			//
		}
		intent.setComponent(comp);
		//getPackageManager().getLaunchIntentForPackage("com.example.test.newprocess");
		intent.setAction("android.intent.action.MAIN");
		//intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);*//*
		if (intent == null) {
			Log.d(TAG,"please install this app!");
			//Toast.makeText(mContext,"please install this app ",Toast.LENGTH_SHORT).show();

		} else {
			//*intent.setAction("android.intent.zhy.action.IMAGE");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		}
	}
}
