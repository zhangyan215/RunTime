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
    // BluetoothReceiver bluetoothReceiver =null;
    BluetoothAdapter bluetoothAdapter =null;
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

    private Handler mHandler = new Handler();
    MessengerChatService mChatService = null;
    SocketManager manager = null;
    int connectState;
    ActivityInvoke mActivityInvoke = null;
    ChannelManager mChannelManager = null;


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
            if(ParameterManager.isBTLE==true){
                bleInitialize();
                Timer timer = new Timer(false);
                timer.schedule(new BLEScan(), ParameterManager.delay, ParameterManager.period);
            }
            if(ParameterManager.isWifi==true){
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
    public void bleInitialize() {
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(innerDeviceService.this, "ble_not_supported", Toast.LENGTH_LONG).show();
            stopSelf();
        }
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = mBluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            Toast.makeText(innerDeviceService.this, "bluetooth_not_supported", Toast.LENGTH_LONG).show();
            stopSelf();
        }
        if (!bluetoothAdapter.enable()) {
            bluetoothAdapter.enable();
        }

        mBLE = new BluetoothLeClass(innerDeviceService.this);
        if (!mBLE.initialize()) {
            Log.e(TAG, "Unable to initialize Bluetooth");
            stopSelf();
        }
        //callback when find the services on the ble devices;
        mBLE.setOnServiceDiscoverListener(mOnServiceDiscover);
        //events when the data is communicated;
        mBLE.setOnDataAvailableListener(mOnDataAvailable);

    }

    /**
     * make the ble scan save energy
     */
    class BLEScan extends TimerTask{
        @Override
        public void run() {
            scanLeDevice(true);
            //bluetoothAdapter.startLeScan(mLeScanCallback);
        }
    }

    /**
     *
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, ParameterManager.SCAN_PERIOD);

            mScanning = true;
            bluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }
    // Device scan callback.
        private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                mLeDevices.add(device);
                if(device.getAddress()!=null){
                    if(device.getName().contains("GPS")) {
                        System.out.println("the address is :"+device.getAddress());

                        //device.getName().contains("GPS")
                        //device.getAddress().equals("88:0F:10:9F:42:4C")

                        System.out.println("hehe");
                        //device.getAddress().equals("61:ED:75:1A:47:3A")
                        //device.getAddress().equals("7C:EC:79:D6:84:A6")

                        bluetoothAdapter.stopLeScan(mLeScanCallback);
                        mBLE.connect(device.getAddress());
                        //0000180F-0000-1000-8000-00805f9b34fb
                        //ScanRecord mScanRecord = (ScanRecord) scanRecord;
                        String str1 = new String(scanRecord);
                        System.out.println("the scanRecord is :" + scanRecord + "the length is:" + scanRecord.length + "the str is:" + str1 + "the data is:" + scanRecord.toString());

                        //Utils.bytesToHexString(scanRecord);// 数组反转
                        // 将Byte数组的数据以十六进制表示并拼接成字符串
                        StringBuffer str = new StringBuffer();
                        int i = 0;
                        for (byte b : scanRecord) {
                            i = (b & 0xff);
                            str.append(Integer.toHexString(i));
                        }
                        String discoveryServceID = str.toString();
                        Log.d(TAG, device.getName() + " scanRecord:\n" + discoveryServceID);

                        // 查询是否含有指定的Service UUID信息
                        if (discoveryServceID.indexOf("2161aff4c0215e621e1f8c36c495a93f"
                                .replace("-", "")) != -1) {
                            Log.d(TAG, device.getName() + " has available service UUID");
                        }
                        //Log.d(TAG,"the scanRecord is :"+parse+"\n"+parse.length());
                        bluetoothAdapter.stopLeScan(mLeScanCallback);
                        Log.d(TAG, "attempt to connect the device!");
                        Log.d(TAG, "find the device :" + device.getName()+" "+device.getAddress());
                       }

                    }
                }


        };

    /**
         * find the services on the ble devices;
         */
        private BluetoothLeClass.OnServiceDiscoverListener mOnServiceDiscover = new BluetoothLeClass.OnServiceDiscoverListener() {

            @Override
            public void onServiceDiscover(BluetoothGatt gatt) {
                displayGattServices(mBLE.getSupportedGattServices());
            }

        };
        /**
         * find the data communication
         */
        private BluetoothLeClass.OnDataAvailableListener mOnDataAvailable = new BluetoothLeClass.OnDataAvailableListener() {

            /**
             * read the data on the ble devices
             */

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic, int status) {
                System.out.println("will read the value of the char");

                if (status == BluetoothGatt.GATT_SUCCESS)

                   //String num1 = new String(characteristic.getValue());
                    Log.e(TAG, "onCharRead " + gatt.getDevice().getName()
                            + " read "
                            + characteristic.getUuid().toString()
                            + " -> "
                            + Utils.bytesToHexString(characteristic.getValue()));
            }

            /**
             * write date to the ble devices
             */

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic characteristic) {
                Log.e(TAG, "onCharWrite " + gatt.getDevice().getName()
                        + " write "
                        + characteristic.getUuid().toString()
                        + " -> "
                        + new String(characteristic.getValue()));
            }
        };

        private void displayGattServices(List<BluetoothGattService> gattServices) {
            if (gattServices == null) return;

            for (BluetoothGattService gattService : gattServices) {
                //-----Service information-----//
                int type = gattService.getType();
                Log.e(TAG, "-->service type:" + Utils.getServiceType(type));
                Log.e(TAG, "-->includedServices size:" + gattService.getIncludedServices().size());
                Log.e(TAG, "-->service uuid:" + gattService.getUuid());

                //-----Characteristics information-----//
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    Log.e(TAG, "---->char uuid:" + gattCharacteristic.getUuid());

                    int permission = gattCharacteristic.getPermissions();
                    Log.e(TAG, "---->char permission:" + Utils.getCharPermission(permission));

                    int property = gattCharacteristic.getProperties();
                    Log.e(TAG, "---->char property:" + Utils.getCharPropertie(property));
                    // get the value of the characteristic
                    byte[] data = gattCharacteristic.getValue();
                    if (data != null && data.length > 0) {
                        Log.e(TAG, "---->char value:" + new String(data));
                    }

                    //UUID_KEY_DATA is the uuid which can interact with ble Characteristic
                    if (gattCharacteristic.getUuid().toString().equals(UUID_KEY_DATA)) {
                        //read the data of Characteristic will strike mOnDataAvailable.onCharacteristicRead()
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mBLE.readCharacteristic(gattCharacteristic);
                            }
                        }, 500);

                        //receive the notice of Characteristic will be written,On receiving the data from bt will strike mOnDataAvailable.onCharacteristicWrite()
                        mBLE.setCharacteristicNotification(gattCharacteristic, true);
                        //set the value of the characteristic
                        gattCharacteristic.setValue("send data->");
                        //write data into the bT model
                        mBLE.writeCharacteristic(gattCharacteristic);
                    }

                    //-----Descriptors information-----//
                    List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                    for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                        Log.e(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
                        int descPermission = gattDescriptor.getPermissions();
                        Log.e(TAG, "-------->desc permission:" + Utils.getDescPermission(descPermission));

                        byte[] desData = gattDescriptor.getValue();
                        if (desData != null && desData.length > 0) {
                            Log.e(TAG, "-------->desc value:" + new String(desData));
                        }
                    }
                }
            }

        }



    }

