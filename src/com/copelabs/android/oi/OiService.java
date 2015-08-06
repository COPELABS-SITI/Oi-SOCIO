package com.copelabs.android.oi;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


import android.app.Service;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class OiService {
	WifiP2pDevice device;
    String instanceName = null;
    String serviceRegistrationType = null;
		
}
