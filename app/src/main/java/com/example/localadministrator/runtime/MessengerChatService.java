package com.example.localadministrator.runtime;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
/**
 * Created by Zhy on 2015/12/23.
 */
public class MessengerChatService extends Service {
	public MessengerChatService() {
	}
	public final static String TAG = "MessengerChatService";
	public String str = "you are a big doby from service!";
	public ArrayList<String> imagePath =new ArrayList<String>();
	public final static int SERVICEID = 0x0001;
	public final static int STRINGID = 0x0011;
	String strFromClient = null;
	public String result = "";
	Message msgTo =null;
	Bundle bundle =null;
	public void setImagePath(){
		imagePath.add("storage/emulated/0/DCIM/Camera/20151016104236.png");
		imagePath.add("storage/emulated/0/DCIM/Camera/SoccerPlayer.jpg");
		imagePath.add("storage/emulated/0/DCIM/Camera/fd.jpg");
		imagePath.add("storage/emulated/0/DCIM/Camera/fkw.jpg");
		imagePath.add("storage/emulated/0/DCIM/Camera/random.jpg");
		imagePath.add("storage/emulated/0/DCIM/Camera/singers.jpg");
	}
	private Messenger messenger = new Messenger(new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Log.d(TAG, "the message from the client===>>>>>>");
			strFromClient = (String) msg.getData().get("content");
			Log.d(TAG, strFromClient);
			msgTo = Message.obtain();
			bundle = new Bundle();
			if (msg.arg1 == SERVICEID) {
				//接受从客户端传来的消息
				msgTo.arg1 = 0X0002;
				setImagePath();
				//System.out.println("the imagePath is"+imagePath.toString());
				bundle.putStringArrayList("content", imagePath);
			} else if(msg.arg1 == STRINGID){
				msgTo.arg1 = 0X0022;
				String image = strFromClient.replace("pull image ","Done&&");
				bundle.putString("content",image);
			}
				msgTo.setData(bundle);
				try {
					//注意，这里把数据从服务器发出了
					msg.replyTo.send(msgTo);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}

	});
	public void setMessage(Message msg,String reply){
		//发送数据给客户端
		Message msgTo = Message.obtain();
		msgTo.arg1 = 0X0002;
		Bundle bundle = new Bundle();
		setImagePath();
		//System.out.println("the imagePath is"+imagePath.toString());
		bundle.putString("content", reply);
		msgTo.setData(bundle);
		try {
			//注意，这里把数据从服务器发出了
			msg.replyTo.send(msgTo);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate");
	}
	public String dealResultMessage(){
		if(result==""){
			return null;
		}
		return result;
	}
	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}

}
