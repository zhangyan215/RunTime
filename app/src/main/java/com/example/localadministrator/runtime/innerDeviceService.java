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
    TaskManager tm;
    ResourceManager rm;
    WiFiP2PInterfaces wifiP2PInterface;
    IntentFilter intentFilter = null;
    String str;
    public static Handler mHandler1 = new Handler();
    public static Handler mHandler2 = new Handler();
    MessengerChatService mChatService = null;
    SocketManager socketManager = null;
    int connectState;
    ActivityInvoke mActivityInvoke = null;
    ChannelManager mChannelManager = null;

    BLEGattManager bleGattManager;


    public innerDeviceService() {

        Log.d("rt", "initialized");
        tm = new TaskManager();
        rm = new ResourceManager();
        wifiP2PInterface = new WiFiP2PInterfaces();
        wifiP2PInterface.setResourceManager(rm);
        wifiP2PInterface.setTaskManager(tm);

      //  mWifi = new WifiServiceManager();

    }

    innerDeviceAIDL.Stub binder=new innerDeviceAIDL.Stub() {
        @Override
        public int sendRQL(String RQL) {
            RQLParser parser = new RQLParser(RQL);
            //  ParameterManager.serviceType = parser.getServices();
            if(parser.checkGrammar()==false){
                return -1;
            }
            str= RQL;

            //put the rql into the outgoingTask ArrayList
            tm.outgoingTasks.addLast(parser);
           // TaskManager.randomTaskID(2);
            ParameterManager.taskID = Integer.parseInt(TaskManager.randomTaskID(2));
            Log.d(TAG,"the task id is:"+ParameterManager.taskID);
            System.out.println(parser.getDeviceName());

            //manager.clientManage();
            // System.out.println("the received udp value is"+ParameterManager.receive);

            //mChatService = new MessengerChatService();

            // deviceScan.scanLeDevice(true);

            mChannelManager = new ChannelManager();
            //mChannelManager.channelChooseStrategy();

            /*if(ParameterManager.isBTLE==true){
                //  mBTLEManager = new BTLEManager();
                bleGattManager = new BLEGattManager(innerDeviceService.this);
                bleGattManager.bleInitialize();
                bleGattManager.bleScan();

            }*/
            if(ParameterManager.isWifi==false){
                if(socketManager==null){
                    socketManager = new SocketManager(innerDeviceService.this);
                    socketManager.setTCP_PORT(2400);
                    socketManager.setUDP_PORT(24000);
                    socketManager.setWifiState();
                }

                ParameterManager.taskInfo = mChannelManager.getBroadcastInfo();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        socketManager.serverManage();
                    }
                }).start();

            }
            mActivityInvoke = new ActivityInvoke(innerDeviceService.this);
            //Log.d()
           /* if(ParameterManager.resultValue!=null){

                Log.d(TAG,"the resultValue is not null, so will broadcast resultValue!");
                mActivityInvoke.broadcastResult();
            }*/
            Log.d(TAG, "1111");

          //  mActivityInvoke.activityStart();

            // there should first be a task queue here,
            // then a thread process all the tasks.


            //activityStart();
           // System.out.println(mLeDevices.size());
           // scanLeDevice(true);
           // tm.outgoingTasks.removeFirst();
            if(ParameterManager.resultFromOther!=null){
                return tm.outgoingTasks.size()-1;
            }

            return tm.outgoingTasks.size();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

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


    }

}

