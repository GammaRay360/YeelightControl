package com.amuday.yeelightcontrol;


import android.os.Handler;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchYeelight {
    private String TAG = "SearchYeelight";


    // Handler:
    Handler handler =null;
    private static final int MSG_FOUND_DEVICE = 11;
    private static final int MSG_DISCOVER_FINISH = 12;

    private DatagramSocket mDSocket;
    private static final String message = "M-SEARCH * HTTP/1.1\r\n" +
            "HOST:239.255.255.250:1982\r\n" +
            "MAN:\"ssdp:discover\"\r\n" +
            "ST:wifi_bulb\r\n";

    private static final String UDP_HOST = "239.255.255.250";
    private static final int UDP_PORT = 1982;
    private boolean mSeraching = true;
    List<HashMap<String, String>> mDeviceList = new ArrayList<HashMap<String, String>>();

    private boolean mNotify = true;

    public void startLestening(){
        mNotify = true;
        new ListeningBulbThread();
    }
    public void stopLestening(){
        mNotify = false;
    }

    private void addBulb(HashMap<String, String> bulbInfo){
        mDeviceList.add(bulbInfo);
    }

    public List<HashMap<String, String>> getBulbs(){
        return mDeviceList;
    }




    public SearchYeelight(Handler handler){
        this.handler = handler;

    }

    private class ListeningBulbThread extends Thread {

        @Override
        public void run() {

            try {
                //DatagramSocket socket = new DatagramSocket(UDP_PORT);
                InetAddress group = InetAddress.getByName(UDP_HOST);
                MulticastSocket socket = new MulticastSocket(UDP_PORT);
                socket.setLoopbackMode(true);
                socket.joinGroup(group);
                Log.d(TAG, "join success");
                mNotify = true;
                while (mNotify){
                    byte[] buf = new byte[1024];
                    DatagramPacket receiveDp = new DatagramPacket(buf,buf.length);
                    Log.d(TAG, "waiting device....");
                    socket.receive(receiveDp);
                    byte[] bytes = receiveDp.getData();
                    StringBuffer buffer = new StringBuffer();
                    for (int i = 0; i < receiveDp.getLength(); i++) {
                        // parse /r
                        if (bytes[i] == 13) {
                            continue;
                        }
                        buffer.append((char) bytes[i]);
                    }
                    if (!buffer.toString().contains("yeelight")){
                        Log.d(TAG,"Listener receive msg:" + buffer.toString()+" but not a response");
                        return;
                    }
                    String[] infos = buffer.toString().split("\n");
                    HashMap<String, String> bulbInfo = new HashMap<String, String>();
                    for (String str : infos) {
                        int index = str.indexOf(":");
                        if (index == -1) {
                            continue;
                        }
                        String title = str.substring(0, index);
                        String value = str.substring(index + 1);
                        Log.d(TAG, "title = " + title + " value = " + value);
                        bulbInfo.put(title, value);
                    }
                    if (!hasAdd(bulbInfo)){
                        addBulb(bulbInfo);
                    }
                    handler.sendEmptyMessage(MSG_FOUND_DEVICE);
                    Log.d(TAG, "get message:" + buffer.toString());
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private class SearchBulbThread extends Thread{
        class Stopper extends Thread{
            private final int time;
            Stopper(int time){
                this.time = time;
            }

            @Override
            public void run() {
                try {
                    Thread.sleep(time);
                    stopSeraching();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        private void stopSeraching(){
            mSeraching = false;
        }

        @Override
        public void run() {
            try {
                mDSocket = new DatagramSocket();
                DatagramPacket dpSend = new DatagramPacket(message.getBytes(),
                        message.getBytes().length, InetAddress.getByName(UDP_HOST),
                        UDP_PORT);
                mDSocket.send(dpSend);
                new Stopper(2000).start();
                while (mSeraching) {
                    byte[] buf = new byte[1024];
                    DatagramPacket dpRecv = new DatagramPacket(buf, buf.length);
                    mDSocket.receive(dpRecv);
                    byte[] bytes = dpRecv.getData();
                    StringBuffer buffer = new StringBuffer();
                    for (int i = 0; i < dpRecv.getLength(); i++) {
                        // parse /r
                        if (bytes[i] == 13) {
                            continue;
                        }
                        buffer.append((char) bytes[i]);
                    }
                    Log.d("socket", "got message:" + buffer.toString());
                    if (!buffer.toString().contains("yeelight")) {
                        Log.d("socket", "You received a message that is not a Yeelight lamp");
                        //handler.obtainMessage(MSG_TOAST, "You received a message that is not a Yeelight lamp").sendToTarget();
                        return;
                    }
                    String[] infos = buffer.toString().split("\n");
                    HashMap<String, String> bulbInfo = new HashMap<String, String>();
                    for (String str : infos) {
                        int index = str.indexOf(":");
                        if (index == -1) {
                            continue;
                        }
                        String title = str.substring(0, index);
                        String value = str.substring(index + 1);
                        bulbInfo.put(title, value);
                    }
                    if (!hasAdd(bulbInfo)){
                        addBulb(bulbInfo);
                    }

                }
                handler.sendEmptyMessage(MSG_DISCOVER_FINISH);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    private boolean hasAdd(HashMap<String,String> bulbinfo){
        for (HashMap<String,String> info : mDeviceList){
            Log.d(TAG, "location params = " + bulbinfo.get("Location"));
            if (info.get("Location").equals(bulbinfo.get("Location"))){
                return true;
            }
        }
        return false;
    }
}
