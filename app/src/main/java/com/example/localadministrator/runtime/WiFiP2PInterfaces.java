
package com.example.localadministrator.runtime;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The main activity for the sample. This activity registers a local service and
 * perform discovery over Wi-Fi p2p network. It also hosts a couple of fragments
 * to manage chat operations. When the app is launched, the device publishes a
 * chat service and also tries to discover services published by other peers. On
 * selecting a peer published service, the app initiates a Wi-Fi P2P (Direct)
 * connection with the peer. On successful connection with a peer advertising
 * the same service, the app opens up sockets to initiate a chat.
 * {@code WiFiChatFragment} is then added to the the main activity which manages
 * the interface and messaging needs for a chat session.
 */
public class WiFiP2PInterfaces extends Activity implements
        ConnectionInfoListener, Handler.Callback {

    public static final String TAG = "WiFiP2PCommunication";

    // TXT RECORD properties
    //public static final String TXTRECORD_PROP_AVAILABLE = "available";
    //Service Name and Service type:"_< protocol >._< transportlayer >" "_http._tcp" and Service Port.
    public static final String SERVICE_INSTANCE = "RQLRequest";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
    static final int SERVER_PORT = 4545;
    //service_action should be {new|response}
    public static final String SERVICE_ACTION_NEW = "NEWTASK";
    public static final String SERVICE_ACTION_RESPONSE = "RESPONSE";

    public static final int MESSAGE_READ = 0x400 + 1;
    public static final int MY_HANDLE = 0x400 + 2;
    private WifiP2pManager manager;
    public TaskManager taskManager;
    public ResourceManager resourceManager;



    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private BroadcastReceiver receiver = null;
    private WifiP2pDnsSdServiceRequest serviceRequest;

    private Handler handler;



    /**
     * PART : set IntentFilter and P2p manager.
     * About broadcastReceiver is departed in single file.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // When the broadcast intent needs to listen issued when an event occurs.
        // Instantiate a IntentFilter, and set to listen for the following events:
        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        // set the WifiP2p manager.
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        handler = new Handler(this);
        // start to discover services.
        startDiscoverService();
    }
    public void setTaskManager(TaskManager tm){
        taskManager = tm;
    }
    public void setResourceManager(ResourceManager rm){
        resourceManager =rm;
    }
    public Handler getHandler(){
        return this.handler;
    }
    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        if (manager != null && channel != null) {
            manager.removeGroup(channel, new ActionListener() {

                @Override
                public void onFailure(int reasonCode) {
                    Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG,"Disconnect successful.");
                }

            });
        }
        super.onStop();
    }

    /**
     * PART: Register a local service.
     * Registers a local service and then initiates a service discovery
     * do we need to remove the existing services and ...when to start to remove broadcasts??
     *
     */
    private void startBroadcast(Map<String, String> record) {
        //Map<String, String> record = new HashMap<String, String>();
        //record.put(TXTRECORD_PROP_AVAILABLE, "visible");
        // VERY IMPORTANT HERE: we need to separate close broadcasts!!!

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
        manager.addLocalService(channel, service, new ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Added Local Service");
            }

            @Override
            public void onFailure(int error) {
                Log.d(TAG, "Failed to add a local service");
            }
        });

    }

    public void startBroadcastTask(RQLParser task, int task_id){
        Map<String, String> record = new HashMap<String, String>();
        record.put("action", SERVICE_ACTION_NEW);
        record.put("deviceName",task.DeviceName);
        record.put("RQL",task.pureRQL);
        record.put("task_id", Integer.toString(task_id));
        startBroadcast(record);
    }

    public void startBroadcastResponse(ResourceManager.Response response){
        Map<String, String> record = new HashMap<String, String>();
        record.put("action", SERVICE_ACTION_RESPONSE);
        record.put("deviceName",response.deviceName);
        record.put("incentiveRequirement",response.incentiveRequirement);
        record.put("task_id", Integer.toString(response.task_id));
        startBroadcast(record);
    }

    /**
     * PART: discover Service configuration
     * 1、new a DnsSdTxtRecordListener to receive the records which can be broadcast from other devices.
     * 2、new a DnsSdResponseListener to receive the actual description of the service and the connection info.
     * 3、put the above listeners into the WifiP2pManager by the setDnsSdResponseListeners() .
     * 4、call addServiceRequest() to make the serviceRequest.
     * 5、finally, call discoverServices() .
     */
    private void startDiscoverService() {

        /**
         * Register listeners for DNS-SD services. These are callbacks invoked
         * by the system when a service is actually discovered.
         *
         * This object will receive the actual description of the service and connection information
         */

        manager.setDnsSdResponseListeners(channel,
                new DnsSdServiceResponseListener() {

                    @Override
                    public void onDnsSdServiceAvailable(String instanceName,
                            String registrationType, WifiP2pDevice srcDevice) {

                        // A service has been discovered. Is this our app?

                        if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {

                            // update the UI and add the item the discovered
                            // device.

                                WiFiP2pService service = new WiFiP2pService();
                                service.device = srcDevice;
                                service.instanceName = instanceName;
                                service.serviceRegistrationType = registrationType;

                                Log.d(TAG, "onBonjourServiceAvailable "
                                        + instanceName);

                        }

                    }
                }, new DnsSdTxtRecordListener() {

                    /**
                     * A new TXT record is available. Pick up the advertised
                     * buddy name.
                     * Receive real-time monitor records
                     * These records can be broadcast from other devices. When you receive a record�� copy of
                     * which device address and other relevant information to an external data structure
                     *  than the current method, for use later.
                     */
                    @Override
                    public void onDnsSdTxtRecordAvailable(
                            String fullDomainName, Map<String, String> record,
                            WifiP2pDevice device) {
                        Log.d(TAG, device.deviceName + " is " + record.toString());
                        if(record.get("action") == SERVICE_ACTION_NEW ){
                            // when a device receives new task requirement broadcast...
                            resourceManager.processTask(record,device);
                            // need to be modified, we need to specific what parameters that can be passed into resourceManager, for it to decide whether to accept this task or not.
                        }else if(record.get("action") == SERVICE_ACTION_RESPONSE){
                            taskManager.collectResponse(record, device);
                        }
                    }
                });

        // After attaching listeners, create a service request and initiate
        // discovery.
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        manager.addServiceRequest(channel, serviceRequest,
                new ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Added service discovery request");
                    }

                    @Override
                    public void onFailure(int arg0) {
                        Log.d(TAG, "Failed adding service discovery request");
                    }
                });
        manager.discoverServices(channel, new ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Service discovery initiated");
            }

            @Override
            public void onFailure(int arg0) {
                if (arg0 == WifiP2pManager.P2P_UNSUPPORTED) {
                    Log.d(TAG, "P2P isn't supported on this device.");
                }else if(arg0 ==WifiP2pManager.BUSY){
                    Log.d(TAG,"The system is busy to deal the request.");
                }else if(arg0 == WifiP2pManager.ERROR){
                    Log.d(TAG,"inner error.");
                }

                Log.d(TAG, "Service discovery failed");

            }
        });
    }


    //here always the client(who have the task requirement start up the connection......
    public void connectP2p(WiFiP2pService service) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = service.device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        if (serviceRequest != null)
            manager.removeServiceRequest(channel, serviceRequest,
                    new ActionListener() {

                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onFailure(int arg0) {
                        }
                    });

        manager.connect(channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Connecting to service");
            }

            @Override
            public void onFailure(int errorCode) {
                Log.d(TAG,"Failed connecting to service");
            }
        });
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG, readMessage);
                //(chatFragment).pushMessage("Buddy: " + readMessage);
                break;

            case MY_HANDLE:
                Object obj = msg.obj;
                //(chatFragment).setChatManager((ChatManager) obj);

        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
        Thread handler = null;
        /*
         * The group owner accepts connections using a server socket and then spawns a
         * client socket for every client. This is handled by {@code
         * GroupOwnerSocketHandler}
         */

        if (p2pInfo.isGroupOwner) {
            Log.d(TAG, "Connected as group owner");
            try {
                handler = new WiFiGroupOwnerSocketHandler(this.getHandler());
                handler.start();
            } catch (IOException e) {
                Log.d(TAG,
                        "Failed to create a server thread - " + e.getMessage());
                return;
            }
        } else {
            Log.d(TAG, "Connected as peer");
            handler = new WiFiClientSocketHandler(this.getHandler(),
                    p2pInfo.groupOwnerAddress);
            handler.start();
        }
    }
    public class WiFiP2pService {
        WifiP2pDevice device;
        String instanceName = null;
        String serviceRegistrationType = null;
    }


}
