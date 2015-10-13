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
	

	public static final String TAG = "FileTransferService";
    private static final int SOCKET_TIMEOUT = 500;
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
            Log.d(TAG, "Client Socket Created");
            int port = intent.getExtras().getInt(HOST_PORT);
            Log.d(TAG, "Host Address: "+host+"& Port: "+port);

            try {
            	Log.d(TAG, "Socket Connecting");
                socket.setReuseAddress(true);
                Log.d(TAG, "Client Socket isConnected- " + socket.isConnected());
                
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
                Log.d(TAG, "Client Socket isConnected- " + socket.isConnected());
                OutputStream stream = socket.getOutputStream();
                Log.d(TAG, "Received Data from OutputSteram of Socket");
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;
                try {
                    is = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    Log.d(TAG, e.toString());
                }
                DeviceDetailFragment.copyFile(is, stream);
                Log.d(TAG, "Client: Data written");
                
                
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } finally {
                   if (socket!=null) {
                        try {
                            socket.close();
                            Log.d(TAG, "Client Socket Closed");
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                
            }

        }
    }
    
}
