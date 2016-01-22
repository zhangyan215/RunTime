package com.example.localadministrator.runtime;

import android.net.wifi.p2p.WifiP2pDevice;

import java.util.Map;

/**
 * Created by LocalAdministrator on 7/16/2015.
 */
public class ResourceManager {
    public ResourceManager(){}

    public void processTask(Map<String, String> record,WifiP2pDevice device){

        //judge whether we want to handle this task as the offload target.

    }

    public class Response{
        public String incentiveRequirement;
        public String deviceName;
        public int task_id;
        public Response(){
            incentiveRequirement="0";
        }

    }
}
