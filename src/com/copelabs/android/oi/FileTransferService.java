// Copyright 2011 Google Inc. All Rights Reserved.

package com.copelabs.android.oi;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String HOST_ADDRESS = "go_host";
    public static final String HOST_PORT = "go_port";

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }
    
//    private byte[] getLocalIPAddress() {
//        try { 
//            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) { 
//                NetworkInterface intf = en.nextElement(); 
//                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) { 
//                    InetAddress inetAddress = enumIpAddr.nextElement(); 
//                     
//                } 
//            } 
//        } catch (SocketException ex) { 
//            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex); 
//        } catch (NullPointerException ex) { 
//            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex); 
//        } 
//        return null; 
//    }
//
//    private String getDottedDecimalIP(byte[] ipAddr) {
//        //convert to dotted decimal notation:
//        String ipAddrStr = "";
//        for (int i=0; i<ipAddr.length; i++) {
//            if (i > 0) {
//                ipAddrStr += ".";
//            }
//            ipAddrStr += ipAddr[i]&0xFF;
//        }
//        return ipAddrStr;
//    }
//
    
    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
    
//            String ip = getDottedDecimalIP(getLocalIPAddress());
//            Log.d(WiFiDirectActivity.TAG, "Local Device IP-"+ip);
            
            String host = intent.getExtras().getString(HOST_ADDRESS);
            Socket socket = new Socket();
            
            int port = intent.getExtras().getInt(HOST_PORT);

            try {
                Log.d(MainActivity.TAG, "Opening client socket - ");
                socket.bind(null);
                socket.setReuseAddress(true);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                Log.d(MainActivity.TAG, "Client socket - " + socket.isConnected());
                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;
                try {
                    is = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    Log.d(MainActivity.TAG, e.toString());
                }
                DeviceDetailFragment.copyFile(is, stream);
                Log.d(MainActivity.TAG, "Client: Data written");
            } catch (IOException e) {
                Log.e(MainActivity.TAG, e.getMessage());
            } finally {
                   if (socket!=null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                
            }

        }
    }
    
}
