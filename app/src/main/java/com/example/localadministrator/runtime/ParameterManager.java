package com.example.localadministrator.runtime;

/**
 * Created by Zhy on 2016/1/08.
 */
public class ParameterManager {
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
}
