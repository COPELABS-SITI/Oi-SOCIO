
package com.copelabs.android.oi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.impl.entity.StrictContentLengthStrategy;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;



import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;




import com.copelabs.android.oi.DeviceListFragment.DeviceActionListener;
import com.copelabs.android.oi.router.GetMessages;
import com.copelabs.android.oi.router.OiRouter;

import com.example.android.wifidirect.R;
import com.copelabs.android.oi.DeviceListFragment;


/**
 * An activity that uses WiFi Direct APIs to discover and connect with available
 * devices. WiFi Direct APIs are asynchronous and rely on callback mechanism
 * using interfaces to notify the application of operation success or failure.
 * The application should also register a BroadcastReceiver for notification of
 * WiFi state related events.
 */
public class MainActivity extends Activity implements ChannelListener, DeviceActionListener {

    public static final String TAG = "oidemo";
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private BroadcastReceiver receiver = null;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    WifiP2pDnsSdServiceInfo serviceInfo;
        
    
    public static final String TXTRECORD = "available";
    public static final String MA = "MAC";
    public static final String SW = "Social Weight";

    public static final String SERVICE_INSTANCE = "_oidemo";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
    
    public String friend="";
    
    
    EditText mMessage;
    Button mButtonSendTo;
    
    List<Item> OiMessages= new ArrayList<Item>();
    
    
    public static final HashMap<String, String> newRecord = new HashMap<String, String>();
    
    public static final HashMap<String, String> myDev = new HashMap<String, String>();
    
    

    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // add necessary intent values to be matched.

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        startRegistrationAndDiscovery();
        
        Spinner dropdown=(Spinner)findViewById(R.id.contacts);
        ArrayList<String> contactItems=new ArrayList<String>();
        
        for (int a=0;a<GetMessages.getMsg().size();a++)
        {
        contactItems.add(GetMessages.getMsg().get(a).getMsgDest());
        }
        Log.d(TAG,"Contacts in dropdown: "+contactItems.toString());
        
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, contactItems);
        dropdown.setAdapter(adapter);
        
        
        dropdown.setOnItemSelectedListener(new OnItemSelectedListener() {
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View view, int position, long arg3) {
                // On selecting a spinner item
        		friend = parent.getItemAtPosition(position).toString();
            // Showing selected spinner item
        //Toast.makeText(parent.getContext(), "You selected: " + friend,Toast.LENGTH_LONG).show();
        Log.d(TAG,"You Selected: "+friend.toString());
        
        
            }

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
        
        mMessage=(EditText)findViewById(R.id.edit_msg);
        mButtonSendTo=(Button)findViewById(R.id.sendTo);
        
        mButtonSendTo.setOnClickListener(new View.OnClickListener()
			        {
			            public void onClick(View view)
			            {
			              //  Log.v("Message", mMessage.getText().toString()+mMail.getText().toString());
			                try {
								writeFile(mMessage.getText().toString(),friend);
								Log.d(TAG,"File created with destination: "+friend.toString());
								
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			       //         Toast.makeText(MainActivity.this, "message queued to be sent!",Toast.LENGTH_SHORT).show();
		                    
			            }
			        });
        
        getListFiles();
        try {
			readFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        
    }
    
    
    
    
    
    

    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirect(manager, channel, this);
        registerReceiver(receiver, intentFilter);
        startRegistrationAndDiscovery();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
    
    
        
    
    /**
     * Registers a local service and then initiates a service discovery
     */
      private void startRegistrationAndDiscovery() {
    	

        //	double sw1=5.0;
        	// For Usense1
        	myDev.put("8a:32:9b:c4:2c:07", "5.0");
        	
        	// For Usense2
         // myDev.put("8a:32:9b:c4:2c:07","10.0");
    	
       
    	Map<String, String> record = new HashMap<String, String>();
        record.put(BluetoothAdapter.getDefaultAdapter().getAddress(), "-1.1");
        record.put("8a:32:9b:c4:2c:07", "10.1");
    //    record.put("8b:32:9b:c4:2c:07", "25.1");
    //    record.put("8c:32:9b:c4:2c:07", "35.1");
        // Only 4 records are transmitted in the announcement
    //    record.put("8d:32:9b:c4:2c:07", "5.0");
    //    record.put("8e:32:9b:c4:2c:07", "5.0");
    //    record.put("8f:32:9b:c4:2c:07", "5.0");
    //    record.put("8g:32:9b:c4:2c:07", "5.0");
    //    record.put("8h:32:9b:c4:2c:07", "5.0");
    //    record.put("8i:32:9b:c4:2c:07", "5.0");
        
        
             
        
        this.serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
        this.manager.addLocalService(this.channel, this.serviceInfo, new WifiP2pManager.ActionListener() 
        {
            @Override
            public void onSuccess() {}
            @Override
            public void onFailure(int error) {}
        });
        discoverService();
        
        //        Log.d(TAG,"Mac Address Peer: "+getPeerMacAdd());
  
//        String macAddrPeer=getPeerMacAdd();
//        
//        Log.d(TAG,"macAddrPeer: "+macAddrPeer);
//        if (macAddrPeer=="0")
//        	macAddrPeer="1a:32:2b:c4:2c:89";
//        
//        if(newRecord.isEmpty())
//        {
//        	newRecord.put("1a:32:2b:c4:2c:89", "-2");
//        }
        
//        List<GetMessages> msginstorage=GetMessages.getMsg();
//      Log.d(TAG,"Message in Storage");
//      for(int x=0;x<msginstorage.size();x++)
//      
//      	{
//      	Log.d(TAG,"Message ID: "+msginstorage.get(x).getMsgId());
//      	Log.d(TAG,"Message Dest: "+msginstorage.get(x).getMsgDest());
//      	}
        
//        List<Integer> lom=OiRouter.getListMsg(myDev, newRecord, macAddrPeer, msginstorage);
    }
    
    
    private String getPeerMacAdd()
    {
    	Map<String,String> tempListofDevSW = new HashMap<String, String>();
        tempListofDevSW.putAll(newRecord);
        
        Log.d(TAG,"New Record in getPeerMacAddr "+newRecord.toString());
		
        Log.d(TAG,"Received record "+tempListofDevSW.toString());
        
		Iterator<String> devMacAdd = tempListofDevSW.keySet().iterator();
		
		//Getting highest social weight value
		while (devMacAdd.hasNext()){
			String dev = devMacAdd.next();
			
			String addr=tempListofDevSW.get(dev);
			Double.parseDouble(addr);
			if(Double.parseDouble(addr)==-1.1)
				return dev;
			
		}
		return "0";
    }
    
    
    private void discoverService() {

    	this.manager.setDnsSdResponseListeners(this.channel, new DnsSdServiceResponseListener() {

                    @Override
                    public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) 
                    {
         //          	srcDevice.deviceName=newRecord.containsKey(srcDevice.deviceAddress) ? newRecord.get(srcDevice.deviceAddress) : srcDevice.deviceName;
                   	
                    }
                }, new DnsSdTxtRecordListener() {
                   
                    @Override
                    public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> record, WifiP2pDevice device) {
                    	Toast.makeText(MainActivity.this, /*"DnsSdTxtRecordAvailable " + fullDomainName + */" Device- " + device.deviceName + " record: " + record.toString(), 1).show();
             //       	newRecord.put(BluetoothAdapter.getDefaultAdapter().getAddress(), record.get(BluetoothAdapter.getDefaultAdapter().getAddress()));
             //       	newRecord.put("8a:32:9b:c4:2c:07", record.get("8a:32:9b:c4:2c:07"));
                        
                    	newRecord.clear();
                    	newRecord.putAll(record);
                    	
                    	
                    	Log.d(TAG, "The arrived record contains:" + newRecord.keySet().toString());
                    	
                    	
                     }
                });

        // After attaching listeners, create a service request and initiate
        // discovery.
        this.serviceRequest = WifiP2pDnsSdServiceRequest.newInstance(SERVICE_INSTANCE, SERVICE_REG_TYPE);
        this.manager.addServiceRequest(this.channel, this.serviceRequest, new ActionListener() {

                    @Override
                    public void onSuccess() {
                    	Log.d("oidemo", "ServiceRequest yes");
                    }

                    @Override
                    public void onFailure(int arg0) {
                    	Log.d("oidemo", "Not ServiceRequest onfailure " + arg0);
                    }
                });
        this.manager.discoverServices(this.channel, new ActionListener() {

            @Override
            public void onSuccess() {
            	Log.d("oidemo", "Service discovery initiated");
            }

            @Override
            public void onFailure(int arg0) {
            	Log.d("oidemo", "Service discovery failed");
            }
        });
    }
    
    
    public void getListFiles(){
    	
    	String path = Environment.getExternalStorageDirectory().toString()+"/Oi1.1";
    	Log.d("Files", "Path: " + path);
    	File f = new File(path);        
    	File file[] = f.listFiles();
    	Log.d("Files", "Size: "+ file.length);
    	for (int i=0; i < file.length; i++)
    	{
    	    Log.d(TAG, "File Name:" + file[i].getName());
    	    
    	    
    	}
    }

	/**
	 * Writes the messages to be sent to an xml file in internal storage (private)
	 * File is named OiSending.xml and stored in the files dir
	 * @param message
	 * @param nameReceiver
	 * @throws IOException
	 */
	  
	public void writeFile(String message, String nameReceiver) throws IOException {
		// stores the message to be sent
		Item OiMessage = new Item(getApplicationContext());
		Log.d("main Write File mac address is", OiMessage.getDeviceSender());
		String recordOiMessage="";
		//time stamp for the message
		double currentTime=System.currentTimeMillis();
		byte[] contentInBytes;
		
		File folder=new File(Environment.getExternalStorageDirectory().toString()+"/Oi1.1");
		folder.mkdir();
		String dirPath=folder.toString();
		Log.v("Path: ", Environment.getExternalStorageDirectory()+"/Oi1.1");
        File file = new File(dirPath,"OiSending.xml");
        
        Log.v("Files stored in", dirPath);
        
   //     Log.v("Files stored in", getFilesDir().getAbsolutePath()+"/Oi1.1");
        //grabs text for the message content
        Log.d("Main Write File", "2");
        OiMessage.setAttributes("nomac", nameReceiver,message, currentTime);
        
        Log.d(TAG, "Message: "+OiMessage.getMessage()+" For Destination: "+OiMessage.nameReceiver);
                   
        
        try  {
        	// opens the output stream to file, to append content
        	FileOutputStream fop = new FileOutputStream(file);
        // if file doesn't exists, then create it
        		if (!file.exists()) {
        			file.createNewFile();
        			// creates the xml header in the file
        			// @TODO fix file header, xml line not being recorded
        			recordOiMessage="<?xml version='1.0' encoding='UTF-8'?>";
        			contentInBytes = recordOiMessage.getBytes();
        			fop.write(contentInBytes);
        			Log.v("WriteFile","file created");
        			
        		}
      /*  		else 
        		{
        			file.delete();
        			file.createNewFile();
        			// creates the xml header in the file
        			// @TODO fix file header, xml line not being recorded
        			recordOiMessage="<?xml version='1.0' encoding='UTF-8'?>";
        			contentInBytes = recordOiMessage.getBytes();
        			fop.write(contentInBytes);
        			Log.v("WriteFile","file created");
        		}
       */ 			
        		
        	// gets the OiMessage content in bytes
            
            //@TODO: message MUST be stored encrypted
        	recordOiMessage=OiMessage.getXmlEntry();
            contentInBytes=recordOiMessage.getBytes();
        	fop.write(contentInBytes);
            fop.flush();
            fop.close();
              
            System.out.println("Done");
             
        	} catch (IOException e) {
        	e.printStackTrace();
        	}
        TextView view = (TextView) findViewById(R.id.edit_message);
        view.setText(R.string.empty);
        view = (TextView) findViewById(R.id.friend_by_mail);
        view.setText(R.string.empty);
    
	}
	
	
	/**
	 * readFile()
	 * reads a file (internal storage) holding the messages
	 * received via Oi, and places them in a List<Item>
	 * Messages are then shown in Fragment Receiver, once the 
	 *user clicks on Read Messages
	 * @throws IOException
	 */

	
	public void readFile() throws IOException {
		XmlPullParserHandler Messages = new XmlPullParserHandler();
		
		Item OiMsg=new Item(getApplicationContext());
		String OiMsgFormat="";
		
		//getFilesDir is /data/data/com.example.oi/Files
		//@TODO File file = new File(getFilesDir(),"OiReceiving.xml");	
		File file = new File(Environment.getExternalStorageDirectory().toString()+"/Oi1.1");	
		
		try {
		    FileInputStream fin = new FileInputStream(file);
		    //creates a list of OiMessages to show in fragment_receiver
		    OiMessages=Messages.parse(fin);
		   /*
		    int i=0;  
		    while((i=fin.read())!=-1){  
		     System.out.println((char)i);  
		    } */ 
		    fin.close();  
		  
		  }catch(IOException e) {
	        	e.printStackTrace();
	        	} 
	//	TextView t = (TextView)findViewById(R.id.receivedMsg);
		for (int i=0; i<OiMessages.size();i=i+1){
			OiMsg=OiMessages.get(i);
			OiMsgFormat=OiMsg.getMessage();
			Log.v("message read\n",OiMsgFormat);
			Log.v("message destination\n",OiMsg.nameReceiver);
	//		t.append(OiMsgFormat);
	//		t.append("\n.................\n");
			
		}
	
		
	}
    
    

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.atn_direct_enable:
                if (manager != null && channel != null) {

                    // Since this is the system wireless settings activity, it's
                    // not going to send us a result. We will be notified by
                    // WiFiDeviceBroadcastReceiver instead.

                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                } else {
                    Log.e(TAG, "channel or manager is null");
                }
                return true;

            case R.id.atn_direct_discover:
                if (!isWifiP2pEnabled) {
                    Toast.makeText(MainActivity.this, R.string.p2p_off_warning,
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                        .findFragmentById(R.id.frag_list);
                fragment.onInitiateDiscovery();
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "Discovery Initiated",
                                Toast.LENGTH_SHORT).show();
                        startRegistrationAndDiscovery();
                        String macAddrPeer=getPeerMacAdd();
                        List<GetMessages> msginstorage=GetMessages.getMsg();
                        List<Integer> lom=OiRouter.getListMsg(myDev, newRecord, macAddrPeer, msginstorage);
                        Log.e(TAG, "Message in storage: "+lom.toString());
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(MainActivity.this, "Discovery Failed : " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.showDetails(device);

    }
    

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
            	
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Connect failed. Retry...",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.resetViews();
        manager.removeGroup(channel, new ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
            }

            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }

        });
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void cancelDisconnect() {

        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (manager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {

                manager.cancelConnect(channel, new ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "Aborting connection",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(MainActivity.this,
                                "Connect abort request failed. Reason Code: " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }
}
