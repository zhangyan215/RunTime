package com.example.localadministrator.runtime;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Zhy on 2016/1/22.
 */
public class ChannelManager {
	private static final String TAG = ChannelManager.class.getCanonicalName();

	public ChannelManager(){

	}

	/**
	 * the channelChooseStrategy according to the task to choose BLE of Wifi
	 */
	public void channelChooseStrategy(){
		if(!ParameterManager.assistName.equals("any")){
			Log.d(TAG,"will choose BLE channel to execute this task!" );
			ParameterManager.isWifi = false;
			ParameterManager.isBTLE = true;
		}else{
			ParameterManager.isBTLE = false;
			ParameterManager.isWifi = true;
			Log.d(TAG,"will choose Wifi channel to execute this task!");
			//choose wifi
		}
	}

	/**
	 * get the broadcastInfo
	 * used in the request device
	 * @return
	 */
	public String getBroadcastInfo(){
		JSONObject outJSONObject = new JSONObject();
		JSONObject inJSONObject = new JSONObject();
		JSONObject innerJSONObject = new JSONObject();
		JSONObject innJSONObject = new JSONObject();
		try {
			outJSONObject.put("MT",0);
			innerJSONObject.put("V",ParameterManager.verbValue);  //rql �ж���
			innerJSONObject.put("DD",ParameterManager.assistName); //rql ���豸����:any/certain
			innerJSONObject.put("RC","sensor");                    //����  data/app/text
			if(ParameterManager.serviceType.equals("sensor/gps")){
				//���Ϊgps ��gps����RN����
				innerJSONObject.put("RN",ParameterManager.serviceType);  // rql ��service���� gps��facedetection
			}else if(ParameterManager.serviceType.equals("image/facedetection")){
				//��Ϊfacedetection ���һ��ͼƬ��Ŀ¼����RN����
				innerJSONObject.put("RN",ParameterManager.imagePath);
			}

			innerJSONObject.put("ADV",null);                      // ��ִ��app������
			innerJSONObject.put("AN","null");   //������App����
			inJSONObject.put("RI",innerJSONObject);
			inJSONObject.put("TI",ParameterManager.taskID);           // Task id
			outJSONObject.put("MC",inJSONObject);
			innJSONObject.put("DN","Nexus 6"); 		//�㲥�ߵ���Ϣ ���豸����
			outJSONObject.put("MH",innJSONObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		ParameterManager.broadcastInfo= outJSONObject.toString();
		return ParameterManager.broadcastInfo;
	}

	/**
	 * parse broadcast info
	 * used in the assist device
	 * intent to get the task id and the broadcast device name to get the bidInfo
	 * @param broadcastInfo
	 */

	public void parseBroadcastInfo(String broadcastInfo){ //����broadcastInfo ��Ϊ �յ�request device������
		try {
			JSONObject outJSONObject = new JSONObject(broadcastInfo);
			JSONObject innerJSONObject = outJSONObject.getJSONObject("MC");
			ParameterManager.taskID = innerJSONObject.getInt("TI");                           //task is should
			JSONObject innJSONObject = outJSONObject.getJSONObject("MH");
			ParameterManager.broadcastName = innerJSONObject.getString("DN");

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	/**
	 * get the Bidinfo
	 * used in the assist device
	 * @return
	 */
	public String getBidInfo(){
		JSONObject outJSONObject = new JSONObject();
		JSONObject inJSONObject = new JSONObject();
		JSONObject innJSONObject = new JSONObject();
		try {
			outJSONObject.put("MT",1);
			inJSONObject.put("DL",0.5);            //�豸����
			inJSONObject.put("DP",0.8);            //�豸����  need to get from the device
			inJSONObject.put("TI",ParameterManager.taskID);             //Task id
			outJSONObject.put("MC",inJSONObject);
			innJSONObject.put("DN",ParameterManager.broadcastName);            //�㲥����Ϣ ���豸����
			outJSONObject.put("MH",innJSONObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		int length = outJSONObject.toString().length();
		ParameterManager.bidInfo ="M#"+length+"##"+outJSONObject.toString();
		return ParameterManager.bidInfo;
	}

	/**
	 * parse Bid info
	 * used in request device
	 * intent to get the assist device  power level and device load //��ȡ�豸�������豸������Ϣ
	 * @param bidInfo
	 */
	public void parseBidInfo(String bidInfo){  //����bidInfo ��Ϊ�յ�assist device�ľ�����Ϣ
		try {
			JSONObject outJSONObject = new JSONObject(bidInfo);
			JSONObject innerJSONObject = outJSONObject.getJSONObject("MC");
			ParameterManager.deviceLoad = innerJSONObject.getDouble("DL");                           //��ȡ�豸����
			ParameterManager.devicePower = innerJSONObject.getDouble("DP");

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * get the AssureInfo
	 * used in request device
	 * @return
	 */
	public String getAssureInfo(){
		JSONObject outJSONObject = new JSONObject();
		JSONObject inJSONObject = new JSONObject();
		JSONObject innJSONObject = new JSONObject();
		try {
			outJSONObject.put("MT",2);
			inJSONObject.put("DID",1);   //�豸id������ַ����ṩ�������Ժ�Ľ���
			inJSONObject.put("TI",ParameterManager.taskID);   // Task id
			outJSONObject.put("MC",inJSONObject);
			innJSONObject.put("DN","Nexus 6");   //�㲥�ߵ���Ϣ ���豸����
			outJSONObject.put("MH",innJSONObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		int length = outJSONObject.toString().length();
		ParameterManager.assureInfo ="M#"+length+"##"+outJSONObject.toString();
		return ParameterManager.assureInfo;
	}

	/**
	 * parse Assure info
	 * used in assist device
	 * intent to get the assist device id //��ȡ��Э���豸�����id
	 * @param assureInfo
	 */
	public void parseAssureInfo(String assureInfo){
		try {
			JSONObject outJSONObject = new JSONObject(assureInfo);
			JSONObject innerJSONObject = outJSONObject.getJSONObject("MC");
			ParameterManager.assistID = innerJSONObject.getInt("DID");          // ��ȡ��Э���豸��id
			//ParameterManager.deviceLoad = innerJSONObject.getDouble("DL");        //��ȡ�豸����
			//ParameterManager.devicePower = innerJSONObject.getDouble("DP");

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * get the result info
	 * used in assist device
	 * @return
	 */
	public String getResult(){
		JSONObject outJSONObject = new JSONObject();
		JSONObject inJSONObject = new JSONObject();
		JSONObject innJSONObject = new JSONObject();
		try {
			outJSONObject.put("MT",3);
			inJSONObject.put("DID",ParameterManager.assistID);      //�������id ��ȷ����Ϣ�л�ȡ
			inJSONObject.put("TI",ParameterManager.taskID);   		// Task id
			inJSONObject.put("TRELT",ParameterManager.resultFromApp);
			outJSONObject.put("MC",inJSONObject);
			innJSONObject.put("DN","Nexus 6");   //�㲥�ߵ���Ϣ ���豸����
			outJSONObject.put("MH",innJSONObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		int length = outJSONObject.toString().length();
		ParameterManager.resultFromApp ="M#"+length+"##"+outJSONObject.toString();
		return ParameterManager.resultFromApp;
	}

	/**
	 * parse the result info
	 * used in request device
	 * intent to get the value of "TRELT"    ���ڲ�ͬ����Ľ������Ҫ�ٷֱ�Դ�
	 * @param result
	 */
	public void parseResult(String result){
		try {
			JSONObject outJSONObject = new JSONObject(result);
			JSONObject innJSONObject = outJSONObject.getJSONObject("MC");
			JSONObject innerJSONObject = innJSONObject.getJSONObject("TRELT");
			//ParameterManager.resultFromOther = innerJSONObject.toString();
			ParameterManager.latitude = innerJSONObject.getString("lat");          // �����GPS��Ϣ
			ParameterManager.longitude = innerJSONObject.getString("lng");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	/**
	 * send image
	 * @param dos
	 */
	public void sendingImage(DataOutputStream dos){

		File file = new File(ParameterManager.imagePath);
		FileInputStream fileStream = null;
		try {
			fileStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		byte[] buffer = new byte[8192];
		try {
			String headStr = "F#"+file.length()+"#"+file.getName()+"##";
			dos.write(headStr.getBytes("UTF-8"));
			System.out.println(file.length());
			int length = 0;
			int translen = 0;
			while((length=fileStream.read(buffer,0,buffer.length))!=-1){
				dos.write(buffer,0,length);
				dos.flush();
			}

			System.out.println("write finished");

			if (fileStream != null) {
				fileStream.close();
				//client.close();
			}
		}catch (Exception e){
			e.printStackTrace();
		}
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

}
