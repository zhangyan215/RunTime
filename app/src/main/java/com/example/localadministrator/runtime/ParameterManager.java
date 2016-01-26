package com.example.localadministrator.runtime;

/**
 * Created by Zhy on 2016/1/08.
 */
public class ParameterManager {
	//the uuid of Runtime
	public static final String RUNTIME = "1c0019ce-91b4-4eb2-a387-64329ac5b4d8";
	//the uuid of gps
	public static final String GPS = "69dc428c-4f0d-47e6-918c-68a98e6576d3";
	//the uuid of http
	public static final String HTTP = "69dc428c-4f0d-47e6-918c-68a98e6576d5";
	//the uuid of file
	public static final String FILE = "69dc428c-4f0d-47e6-918c-68a98e6576d2";
	//store task data which will be broadcast
	public static byte [] send_data = new byte[2048];
	//task info that will be broadcast
	public static String taskInfo = "";
	//store the broadcast task info
	public static byte[] receive_data = new byte[2048];
	//get the task info in String
	public static String receive = "";
	//Ble scan period
	public static final long SCAN_PERIOD = 10000;
	//Ble scan Interval period
	public static long period = 30000;
	//Ble delay
	public static long delay = 1000;
	//the task type
	public static String serviceType = "";
	//the name in the rql
	public static String assistName = "";
	//the verb of rql
	public static String verbValue = "";
	//the
	//if use BTLE
	public static boolean isBTLE;
	//if use Wifi
	public static boolean isWifi;
	//the result value get from the other device
	public static String resultValue ;
	//the result get from the app on the device
	public static String resultInfo;
	//if support peripheral mode
	public static boolean isPeripheral ;
	//if this device as request device;
	public static boolean isRequest = true;
	//the broadcast info
	public static String broadcastInfo = "";
	//the bid info
	public static String bidInfo = "";
	//the assure Info
	public static String assureInfo = "";
	//the task id
	public static int taskID ;
	//the broadcast device's name
	public static String broadcastName = "";
	//the assist device load
	public static double deviceLoad ;      //协助设备的负载用于设备选择时的参考  参数值从竞价消息中获取
	//the assist device power
	public static double devicePower;      //协助设备的电量 同上  parseBidInfo
	//the assist device id
	public static int assistID;           // 协助设备被分配到的id  用于返回结果时调用  从确认消息中获取
	//the latitude
	public static String latitude;        // 纬度信息 用于在请求GPS时向APP广播的结果  从结果中获取
	//the longitude
	public static String longitude;       // 经度信息 同上    parseResult


}
