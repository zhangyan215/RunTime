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
	//if use BTLE
	public static boolean isBTLE;
	//if use Wifi
	public static boolean isWifi;
	//the result
	public static String resultValue = "";
	//if support peripheral mode
	public static boolean isPeripheral ;
	//if this device as request device;
	public static boolean isRequest = true;

}
