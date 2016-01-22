
package com.example.localadministrator.runtime;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class WiFiClientSocketHandler extends Thread {

    private static final String TAG = "ClientSocketHandler";
    private Handler handler;
    private WiFiP2PChatManager chat;
    private InetAddress mAddress;

    public WiFiClientSocketHandler(Handler handler, InetAddress groupOwnerAddress) {
        this.handler = handler;
        this.mAddress = groupOwnerAddress;
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        try {
            socket.bind(null);
            socket.connect(new InetSocketAddress(mAddress.getHostAddress(),
                    WiFiP2PInterfaces.SERVER_PORT), 5000);
            Log.d(TAG, "Launching the I/O handler");
            chat = new WiFiP2PChatManager(socket, handler);
            new Thread(chat).start();//start = runable . run?
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
    }

    public WiFiP2PChatManager getChat() {
        return chat;
    }

}
