package com.example.localadministrator.runtime;

/**
 * Created by LocalAdministrator on 7/14/2015.
 */
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.TimerTask;


// ???????
public class TaskManager{
    // shall we move 1. the processing logic, 2. the task array here?
    Handler handler;
    public LinkedList<RQLParser> outgoingTasks;
    public LinkedList<RQLParser> innerTasks;
    final String TAG = "TaskProcessor";
    static final int BTLE = 0;
    static final int WiFiP2P=1;
    static final int WiFi = 2;
    static final int Internet=3;
    Intent intent = new Intent();
    public TaskManager(){
        handler =  new Handler();
        outgoingTasks = new LinkedList<RQLParser>();

        Runnable runnable = new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                // ??????????
                Log.d(TAG, "running");
                //checkUndergoingTasks();
               // checkNewTasks();

              //  handler.postDelayed(this, 100);// 100ms???this??runable
            }
        };
      //  handler.postDelayed(runnable, 100);// ??????100ms???runnable??
    }

    public void checkUndergoingTasks(){
        if(outgoingTasks.size()>0){
            int iterator ;
            for(iterator= 0; iterator<outgoingTasks.size(); iterator++ ){
                if(outgoingTasks.get(iterator).status == RQLParser.UNDERGOING){
                    //  here we need some strategy to decide which undergoing tasks should be stopped and resent to another device.
                }
            }
        }
    }

    /**
     * the communication channels choose strategy.
     * @return  the network that will  choose
     */
    public int taskNetworkSelection(){
        //to-be-done
        //based on the task, the requirement and the device's status to select the network status.

        return WiFiP2P;
    }

    /**
     *
     * @param
     */
    public void channelDeal(){
        switch (taskNetworkSelection()){
            case 0:
            case 1:
            case 2:
            case 3:


        }
    }
    public void checkNewTasks(){
        Log.d(TAG,"start to check new tasks");
        if(outgoingTasks.size()>0){
            int iterator ;
            for(iterator= 0; iterator<outgoingTasks.size(); iterator++ ){
                if(outgoingTasks.get(iterator).status == RQLParser.UNPROCESSED){
                    Log.d(TAG, "new task found");

                    //outgoingTasks.get(iterator).print();
                    //here we need some strategy to decide which communication layer we want to use......
                    //CommunicationLayer.set(WIFIP2P);
                    //communicationLayer.broadcastRequirement();
                    //wait...
                    //according to feedback, decide which device to offload...
                }
            }
        }
    }
    public void collectResponse(Map<String, String> record,WifiP2pDevice device){

        //judge whether we want to handle this task as the offload target.
        //record the response into a container, waiting for the checkNewTasks's timer to gather feedbacks.
    }

    void onDestroy(){
        handler.removeCallbacks((Runnable) this);// ???????
    }


    public static String randomTaskID(int n) {
        if (n < 1 || n > 10) {
            throw new IllegalArgumentException("cannot random " + n + " bit number");
        }
        Random ran = new Random();
        if (n == 1) {
            return String.valueOf(ran.nextInt(10));
        }
        int bitField = 0;
        char[] chs = new char[n];
        for (int i = 0; i < n; i++) {
            while(true) {
                int k = ran.nextInt(10);
                if( (bitField & (1 << k)) == 0) {
                    bitField |= 1 << k;
                    chs[i] = (char)(k + '0');
                    break;
                }
            }
        }
        return new String(chs);
    }
}
