package com.example.localadministrator.runtime;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.FaceDetector;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Zhy on 2015/12/13.
 */
public class SocketManager {
	private static String TAG = SocketManager.class.getCanonicalName();
	private  String ipAddress = "";
	private  int TCP_PORT = 0;
	private  int UDP_PORT = 0;
	private Socket socket =null;
	private ServerSocket mServerSocket = null;
	private Socket client = null;
	private DataInputStream isClient = null;
	private DataOutputStream osClient = null;
	private DataInputStream isServer = null;
	private DataOutputStream osServer = null;
	public JSONObject mfjs = new JSONObject();
	public JSONObject headInfoObject;
	public ChannelManager manager = new ChannelManager();
	public int location;
	public String fileName;
	public byte[] buffer1;
	//public byte[] send_data = new byte[1024];
	byte[] data = new byte[8192];
	public String str = "send udp";
	public String ss;
	MulticastSocket ms = null;
	DatagramPacket dp;
	private boolean allowRun = true;
	private ServerThread mServerThread = null;
	private WifiManager wifiManager;
	private Context mContext;
	public SocketManager(Context mContext){
		this.mContext = mContext;
	}
	public void setTCP_PORT(int TCP_PORT){
		this.TCP_PORT = TCP_PORT;

	}
	public void setUDP_PORT(int UDP_PORT){
		this.UDP_PORT = UDP_PORT;

	}
	public int getTCP_PORT(){
		return TCP_PORT;
	}
	public  int getUDP_PORT(){
		return UDP_PORT;
	}
	//the ipAddress can get from the udpBroadcast;
	public void setIpAddress(String ipAddress){
		this.ipAddress = ipAddress;
	}
	public String getIpAddress(){
		return ipAddress;
	}
	public void clientManage(){
		udpReceive();
		setClientSocket();
	}
	public void serverManage(){
		try {
			udpBroadcast();
			setServerSocket();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * @int getWifiState()
	 * 0: WIFI_STATE_DISABLING
	 * 1: WIFI_STATE_DISABLED
	 * 2: WIFI_STATE_ENABLING
	 * 3: WIFI_STATE_ENABLED
	 * 4: WIFI_STATE_UNKNOWN  when launched in Simulator.
	 */
	public void setWifiState(){
		wifiManager= (WifiManager)mContext.getSystemService(mContext.WIFI_SERVICE);

		if(!wifiManager.isWifiEnabled()){
			wifiManager.setWifiEnabled(true);
		}
		wifiManager.startScan();
		wifiManager.getScanResults();
		System.out.println("the scanResults is:" + wifiManager.getScanResults());
		//wifiInfo.getIpAddress();
		// wifiInfo.getRssi();
		//System.out.println("the wifiInfo is :"+wifiInfo.getRssi()+wifiInfo.getIpAddress());

		System.out.println("wifi state------->" + wifiManager.getWifiState());
		Log.d(TAG, "the wifi state is :" + wifiManager.getWifiState());

	}


	/**
	 * using UDP to broadcast the task info.
	 * @throws IOException
	 */
	private void udpBroadcast() throws IOException {
		DatagramSocket client_socket = new MulticastSocket();
		InetAddress IPAddress =  InetAddress.getByName("255.255.255.255");
		ParameterManager.send_data = ParameterManager.taskInfo.getBytes();
		Log.d(TAG, "the broadcast info is:" + ParameterManager.taskInfo);

		System.out.println(ParameterManager.send_data);
		DatagramPacket send_packet = new DatagramPacket(ParameterManager.send_data,ParameterManager.taskInfo.length(),IPAddress , UDP_PORT);
		try {
			client_socket.send(send_packet);
			Log.d(TAG, "the broadcast task info has been sent successfully!");
		}catch(Exception e){
			e.printStackTrace();
		}
		client_socket.close();
	}
	/**
	 *receive the UDP broadcast info
	 */
	private void udpReceive() {
		try {
			InetAddress groupAddress = InetAddress.getByName("255.255.255.255");
			ms = new MulticastSocket(UDP_PORT);
			ms.joinGroup(groupAddress);
		} catch (Exception e) {
			e.printStackTrace();
		}
		dp = new DatagramPacket(ParameterManager.receive_data, ParameterManager.receive_data.length);
		System.out.println("start receiving task.....");
		try {
			ms.receive(dp);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("receive task successfully!");
		ParameterManager.receive = new String(dp.getData(), dp.getOffset(), dp.getLength());
		System.out.println("the udp broadcast value is"+ParameterManager.receive);
		ipAddress = dp.getAddress().toString().substring(1);
		System.out.println(ipAddress);

		/*receive.post(new Runnable() {
			@Override
			public void run() {
				receive.append("\nReceive the udp broadcast infor:"+ss);

			}
		});*/
		ms.close();

	}
	private void setClientSocket(){
		try {
			socket = new Socket(ipAddress,TCP_PORT);
			chatProcessOnClient();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void setServerSocket(){
		try {
			if(mServerSocket==null){
				mServerSocket = new ServerSocket(TCP_PORT);
			}

			if (mServerSocket == null){
				Log.e(TAG,"the serversocket is null");
				return;
			}
			Log.d(TAG, "the TCP_PORT on Server is:" + TCP_PORT);

		} catch (IOException e) {
			e.printStackTrace();
		}
		if(mServerThread == null){
			try {
				mServerThread = new ServerThread();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("the serviceThread will be built!");
			mServerThread.start();
			System.out.println("the serviceThread is built!");


		}
	}

	private void ioStreamOnClient(){
		try {
			osClient = new DataOutputStream(socket.getOutputStream());
			isClient = new DataInputStream(socket.getInputStream());
			chatProcessOnClient();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	private void ioStreamOnServer(){
		try {
			isServer = new DataInputStream(client.getInputStream());
			osServer = new DataOutputStream(client.getOutputStream());
			chatProcessOnServer();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void chatProcessOnClient(){
		try {
			//ioStreamOnClient();
			osClient.write("you are doby".getBytes("UTF-8"));
			osClient.flush();
			//System.out.println(getBidInfo());
		} catch (IOException e) {
			e.printStackTrace();
		}
		//readInputData(isClient);

		//System.out.println(getResult());
		System.out.println("all work is down!");


		try {
			if(osClient != null){
				osClient.close();
			}
			if (isClient != null) {
				isClient.close();
				socket.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 *
	 */

	private void chatProcessOnServer() throws IOException {

		try{
			String value = readString(isServer);
			System.out.println("the value is:" + value);
			if(value != null){
				//there need the device choose strategy
				//....
				//....
				osServer.write(manager.getAssureInfo().getBytes("UTF-8"));
				Log.d(TAG, "the assure info is:" + manager.getAssureInfo());
				Log.i(TAG, "the assure info has been written to the client");
				if(ParameterManager.serviceType.equals("sensor/gps")){
					ParameterManager.resultFromOther= readString(isServer);
					Log.d(TAG, "the result value is :" + ParameterManager.resultFromOther);
					if(ParameterManager.resultFromOther!=null){
						Log.d(TAG, "the resultValue is not null, so will broadcast resultValue!");
						ActivityInvoke mActivityInvoke = new ActivityInvoke(mContext);
						manager.parseResult(ParameterManager.resultFromOther);
						mActivityInvoke.broadcastResult();
						ParameterManager.resultFromOther=null;
						//mContext.sendBroadcast();
					}
				}else if(ParameterManager.serviceType.equals("image/facedetection")){
					manager.sendingImage(osServer);
				}



			}
		}catch(Exception e){
			e.printStackTrace();
		}
		try {
			if(osServer != null){
				osServer.close();
			}
			if (isServer != null) {
				isServer.close();
				//client.close();
			}
			if(client!=null){
				client.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}



	/**
	 * deal with the DataInputStream  but it used when the length of is  is short.
	 * @param is
	 */

	public String readString(DataInputStream is){

		int read = 0 ;
		int readLen=0;
		int curRead=0;
		try {
			while (is.available()==0);
			buffer1 = new byte[is.available()];
			System.out.println("available:"+is.available());
		} catch (IOException e) {
			e.printStackTrace();
		}
		str = null;
		try {
			read = is.read(buffer1,0,buffer1.length);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("read:"+read);
		//System.out.println(.toString(buffer1).toString());
		mfjs= parseHead(buffer1);
		if(mfjs!=null){
			byte[] buffer2 = new byte[mfjs.optInt("L")];
			System.out.println("length is :"+mfjs.optInt("L"));
			curRead = read -location-2;
			System.out.println("curRead:"+curRead);
			System.arraycopy(buffer1, location + 2, buffer2, 0, curRead);
			try {
				while(readLen<(mfjs.optInt("L"))) {
					readLen +=curRead;

					curRead =is.read(buffer2,readLen,is.available());

				}
				System.out.println("readLen="+readLen);
				if(readLen == (mfjs.optInt("L"))){
					System.out.println("read fully");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			String type = mfjs.optString("T");
			if(type.equals("M")){
				str = new String(buffer2);
			}
		}
		return str;
	}
	//parseHead
	public JSONObject parseHead(byte[] buffer){
		headInfoObject = new JSONObject();
		int length = buffer.length;
		System.out.println(length);
		System.out.println(new String(buffer));
		int maxHeadLength = 2+12+128;//
		int lookRange = maxHeadLength > length ? length : maxHeadLength;
		String stringBuffer = new String(buffer);
		location = stringBuffer.indexOf("##");
		System.out.println("location:"+location);
		//int location = buffer.indexOf("##",lookRange);
		if(location == 0){

			return null;
		}else{
			//��ȡ��Ϣ����
			String headString = stringBuffer.substring(0, location);
			//System.out.println("headString:"+headString);
			String[]components = headString.split("#");
			//System.out.println(components.length+"component");
			if(components.length< 2){

				return null;
			}

			int contentLen = Integer.parseInt(components[1]);//
			//System.out.println("message length:"+contentLen);
			if(components[0].equals("M")){//׼
				//JSONObject mjs = new JSONObject();
				try {
					headInfoObject.put("T", "M");
					headInfoObject.put("L",contentLen);
					headInfoObject.put("CS",location+2);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return headInfoObject;

				//return {"T":"M","L":contentLen,"CS":location+2};

			}else if(components[0].equals("F")){
				if(components.length < 3){

					return null;
				}
				fileName = components[2];
				System.out.println("fileName:"+fileName);

				try {
					headInfoObject.put("T", "F");
					headInfoObject.put("L",contentLen);
					headInfoObject.put("CS",location+2);
					headInfoObject.put("N",fileName);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return headInfoObject;

				//return {"T":"F","L":contentLen,"CS":location+2,"N":fileName};
			}else{
				return null;
			}

		}
	}



	class ServerThread extends Thread{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(allowRun){
				try {
					client = mServerSocket.accept();
					Log.i(TAG, "Service start accepting the request from the client..");
					ioStreamOnServer();
					//chatProcessOnServer();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}



}
