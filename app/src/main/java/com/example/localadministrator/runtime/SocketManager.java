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
	public DataInputStream isClient = null;
	public DataOutputStream osClient = null;
	public DataInputStream isServer = null;
	public DataOutputStream osServer = null;
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
		System.out.println("the scanResults is:"+wifiManager.getScanResults());
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
		Log.d(TAG,"the broadcast info is:"+ParameterManager.taskInfo);

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
			mServerSocket = new ServerSocket(TCP_PORT);
			Log.d(TAG,"the TCP_PORT on Server is:"+TCP_PORT);

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
			//ioStreamOnServer();
			/*dos = new DataOutputStream(client.getOutputStream());
			is = new DataInputStream(client.getInputStream());*/

			String value = readInputData(isServer);
			System.out.println("the value is:" + value);
			/*str = readString(is);
			if(parseBidInfo(str)){
				dos.write(getAssureInfo().getBytes("UTF-8"));
				dos.flush();
			}
			for(int i = 0;i<num;i++){
				str =readString(is);
				if(str!=null&&parseImageRequest(str)&&headInfoObject.optString("T")=="M"){
					sendingImage(i);
				}else{
					dos.write(getFinishInfo().getBytes("UTF-8"));
					dos.flush();
				}

			}

			str= readString(is);
			*//*Intent mIntent = new Intent();
			mIntent.putExtra("result",str);
			mIntent.setAction("resultInfo.zhy");
			sendBroadcast(mIntent);*//*
			System.out.println(str);
			parseResult(str);
			mTextView.post(new Runnable() {
				@Override
				public void run() {
					mTextView.append("\nthe number of the images have been sent is:"+num);
					mTextView.append("\nall images have been sent!");
					mTextView.append("\nthe results of the face detection is:"+str);
					//mTextView.append("\nthe face number is " + faceNumber);
				}
			});
			System.out.println("the face number is:" + faceNumber);

			if(dos!=null){
				dos.close();

			}
			if(is!= null){
				is.close();
			}*/
		}catch(Exception e){
			e.printStackTrace();
		}
		try {
			if(osServer != null){
				osServer.close();
			}
			if (isServer != null) {
				isServer.close();
				client.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	/**
	 * deal with the DataInputStream
	 * @param is
	 */
	private String readInputData(DataInputStream is){
		byte[] buffer = new byte[1024];
		int transLen =0;
		while(true){
			int read =0;
			try {
				read = is.read(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(read == -1)
				break;
			transLen += read;

		}
		String str = new String (buffer);
		Log.d(TAG,"the received info is:"+str);
		return str;

	}
	class ServerThread extends Thread{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(allowRun){
				try {
					client = mServerSocket.accept();
					Log.i(TAG, "Service accept the request from the client..");
					ioStreamOnServer();
					//chatProcessOnServer();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	//parseHead
	public JSONObject parseHead(byte[] buffer){
		JSONObject headInfoObject = new JSONObject();
		int length = buffer.length;
		System.out.println(length);
		System.out.println(new String(buffer));
		int maxHeadLength = 2+12+128;//最大的头长度
		int lookRange = maxHeadLength > length ? length : maxHeadLength;
		String stringBuffer = new String(buffer);
		int location = stringBuffer.indexOf("##");
		System.out.println("location:" + location);
		//int location = buffer.indexOf("##",lookRange);
		if(location == 0){
			//非法的头信息
			return null;
		}else{
			//获取消息类型
			String headString = stringBuffer.substring(0, location);
			//System.out.println("headString:"+headString);
			String[]components = headString.split("#");
			//System.out.println(components.length+"component");
			if(components.length< 2){
				//非法的头信息 缺少消息类型或者内容长度
				return null;
			}

			int contentLen = Integer.parseInt(components[1]);//获取消息或者文件内容的长度
			//System.out.println("message length:"+contentLen);
			if(components[0].equals("M")){//准备接收消息
				//JSONObject mjs = new JSONObject();
				try {
					headInfoObject.put("T", "M");
					headInfoObject.put("L",contentLen);
					headInfoObject.put("CS",location+2);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return headInfoObject;
				//返回类型，长度，内容开始位置，文件名
				//return {"T":"M","L":contentLen,"CS":location+2};

			}else if(components[0].equals("F")){//准备接收文件
				if(components.length < 3){
					//非法的头信息，缺少文件名
					return null;
				}
				//fileName = components[2];
				//System.out.println("fileName:"+fileName);

				try {
					headInfoObject.put("T", "F");
					headInfoObject.put("L",contentLen);
					headInfoObject.put("CS",location+2);
				//	headInfoObject.put("N",fileName);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return headInfoObject;
				//返回类型，长度，内容开始位置，文件名
				//return {"T":"F","L":contentLen,"CS":location+2,"N":fileName};
			}else{
				return null;
			}

		}
	}

	/**
	 * get the Bidinfo
	 */
	public String getBidInfo(){
		JSONObject outJSONObject = new JSONObject();
		JSONObject inJSONObject = new JSONObject();
		String bidinfo = null;
		try {
			outJSONObject.put("MSG_TYPE",0);
			inJSONObject.put("CPUhz",1440);
			inJSONObject.put("BLev",0.5);
			inJSONObject.put("BDNAME","server_offload");
			outJSONObject.put("MSG_CONY",inJSONObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		int length = outJSONObject.toString().length();
		bidinfo ="M#"+length+"##"+outJSONObject.toString();
		return bidinfo;
	}
	/**
	 * get the image request
	 */
	public String getImageRequest(){
		JSONObject outJSONObject = new JSONObject();
		JSONObject inJSONObject = new JSONObject();
		String imageInfo = null;
		try {
			outJSONObject.put("MSG_TYPE",2);
			//inJSONObject.put("DEV_ID",devID);
			outJSONObject.put("MSG_CONY",inJSONObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		imageInfo ="M#"+outJSONObject.toString().length()+"##"+outJSONObject.toString();
		return imageInfo;

	}
	/**
	 * parse the assure information
	 */
	public boolean parseAssInfo(String str){
		JSONObject jObject = null;

		try {
			jObject = new JSONObject(str);
			int type = jObject.optInt("MSG_TYPE");
			System.out.println(type);
			JSONObject mjObject =jObject.optJSONObject("MSG_CONY");
			//devID = mjObject.optInt("DEV_ID");
			if(type==1) {
			//	imageNum = mjObject.optInt("PS_PICN");
				return true;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * parse the finish information
	 */
	public boolean parseFinishInfo(String str){
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(str);
			int type = jObject.optInt("MSG_TYPE");
			System.out.println(type);
			String value = jObject.optString("MSG_CONY");
			if(type==3){
				return true;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * get the result information
	 */
	public String getResult(){
		JSONObject outJSONObject = new JSONObject();
		JSONObject inJSONObject = new JSONObject();
		String result = null;
		try {
			outJSONObject.put("MSG_TYPE",4);
			//inJSONObject.put("DEV_ID", devID);
			//inJSONObject.put("RELS", innJSONObject);
			outJSONObject.put("MSG_CONY",inJSONObject);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		result = "M#"+outJSONObject.toString().length()+"##"+outJSONObject.toString();
		return result;
	}
}
/*class ServerThread extends Thread {
		private Socket client;
		private BufferedReader bufferedReader;
		private PrintWriter printWriter;

		public ServerThread(Socket s)throws IOException {
			client = s;

			bufferedReader =new BufferedReader(new InputStreamReader(client.getInputStream()));

			printWriter =new PrintWriter(client.getOutputStream(),true);
			System.out.println("Client(" + getName() +") come in...");

			start();
		}

		public void run() {
			try {
				String line = bufferedReader.readLine();

				while (!line.equals("bye")) {
					printWriter.println("continue, Client(" + getName() +")!");
					line = bufferedReader.readLine();
					System.out.println("Client(" + getName() +") say: " + line);
				}
				printWriter.println("bye, Client(" + getName() +")!");

				System.out.println("Client(" + getName() +") exit!");
				printWriter.close();
				bufferedReader.close();
				client.close();
			}catch (IOException e) {
			}
		}
	}*/