package com.example.localadministrator.runtime;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.ScanRecord;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class innerDeviceService extends Service {
    private static String TAG = innerDeviceService.class.getCanonicalName();
    private final static String UUID_KEY_DATA = "00002a19-0000-1000-8000-00805f9b34fb";
    TaskManager tm;
    ResourceManager rm;
    WiFiP2PInterfaces wifiP2PInterface;

    private ArrayList<BluetoothDevice> mLeDevices = new ArrayList<>();

    BluetoothAdapter bluetoothAdapter =null;
    BluetoothLeAdvertiser bluetoothLeAdvertiser = null;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    IntentFilter intentFilter = null;
    //IntentFilter intentFilterEnd =null;
    private boolean mScanning = true;
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    String str;
    //String serviceType;
    private BluetoothLeClass mBLE;
    private WifiServiceManager mWifi ;

    public static Handler mHandler1 = new Handler();
    public static Handler mHandler2 = new Handler();
    MessengerChatService mChatService = null;
    SocketManager manager = null;
    int connectState;
    ActivityInvoke mActivityInvoke = null;
    ChannelManager mChannelManager = null;
    BLEPeripheral blePeripheral;
    BLEGattManager bleGattManager;


    public innerDeviceService() {
        Log.d("rt", "initialized");
        tm = new TaskManager();
        rm = new ResourceManager();
        wifiP2PInterface = new WiFiP2PInterfaces();
        wifiP2PInterface.setResourceManager(rm);
        wifiP2PInterface.setTaskManager(tm);

        //taskProcesser.run();
      //  mWifi = new WifiServiceManager();

    }

    innerDeviceAIDL.Stub binder=new innerDeviceAIDL.Stub() {
        @Override
        public int sendRQL(String RQL) {

            // there should first be a task queue here,
            // then a thread process all the tasks.
            RQLParser parser = new RQLParser(RQL);
          //  ParameterManager.serviceType = parser.getServices();
            if(parser.checkGrammar()==false){
                return -1;
            }
            str= RQL;
            manager = new SocketManager();
            //put the rql into the outgoingTask ArrayList
            tm.outgoingTasks.addLast(parser);
            System.out.println(parser.getDeviceName());

            //manager.clientManage();
           // System.out.println("the received udp value is"+ParameterManager.receive);

            //mChatService = new MessengerChatService();

           // deviceScan.scanLeDevice(true);

            mChannelManager = new ChannelManager();
            mChannelManager.ChannelChooseStrategy();

            if(ParameterManager.isBTLE==false){
              //  mBTLEManager = new BTLEManager();
                bleGattManager = new BLEGattManager(innerDeviceService.this);
                bleGattManager.bleInitialize();
                bleGattManager.bleScan();
               // bleInitialize();
                /*Timer timer = new Timer(false);
                timer.schedule(new BLEScan(), ParameterManager.delay, ParameterManager.period);*/
            }
            if(ParameterManager.isWifi==false){
                manager.setTCP_PORT(2400);
                manager.setUDP_PORT(24000);
                ParameterManager.taskInfo = "this is a  task!";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        manager.serverManage();
                    }
                }).start();

            }
            Log.d(TAG, "1111");
            mActivityInvoke = new ActivityInvoke(innerDeviceService.this);
            mActivityInvoke.activityStart();
            //activityStart();
           // System.out.println(mLeDevices.size());
           // scanLeDevice(true);
           // tm.outgoingTasks.removeFirst();
            return tm.outgoingTasks.size()-1;

        }
    };

    /**
     * @int getWifiState()
     * 0: WIFI_STATE_DISABLING
     * 1: WIFI_STATE_DISABLED
     * 2: WIFI_STATE_ENABLING
     * 3: WIFI_STATE_ENABLED
     * 4: WIFI_STATE_UNKNOWN  when launched in Simulator.
     */
    public void setWifiState(){
        wifiManager= (WifiManager)getSystemService(WIFI_SERVICE);

        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
        wifiManager.startScan();
        wifiManager.getScanResults();
        System.out.println("the scanresults is:"+wifiManager.getScanResults());
        //wifiInfo.getIpAddress();
       // wifiInfo.getRssi();
        //System.out.println("the wifiInfo is :"+wifiInfo.getRssi()+wifiInfo.getIpAddress());

        System.out.println("wifi state------->" + wifiManager.getWifiState());
        Log.d(TAG, "the wifi state is :"+wifiManager.getWifiState());
       // Toast.makeText(innerDeviceService.this, "��ǰWIFI������״̬Ϊ"+wifiManager.getWifiState(),Toast.LENGTH_LONG).show();
    }

    public void send() {
        Intent intent = new Intent("android.intent.action.MY_BROADCAST");
        intent.putExtra("msg", str);
        sendBroadcast(intent);
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("rt",intent.toString());
        Log.d("rt", "binded");
        // TODO: Return the communication channel to the service.
        return binder;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
       // mBLE.close();

    }

}

