
package com.copelabs.android.oi;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
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
import android.os.Handler;
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


/**
 * An activity that uses WiFi Direct APIs to discover and connect with available
 * devices. WiFi Direct APIs are asynchronous and rely on callback mechanism
 * using interfaces to notify the application of operation success or failure.
 * The application should also register a BroadcastReceiver for notification of
 * WiFi state related events.
 */
public class MainActivity extends Activity implements ChannelListener, DeviceActionListener {

    public static final String TAG = "OiDemo";
    private WifiP2pManager manager;
    private WifiP2pInfo info;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private BroadcastReceiver receiver = null;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    WifiP2pDnsSdServiceInfo serviceInfo;
    public boolean connection=false;
        
    
    public static final String TXTRECORD = "available";
    public static final String MA = "MAC";
    public static final String SW = "Social Weight";

    public static final String SERVICE_INSTANCE = "_oidemo";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
    
    Spinner dropdownContacts;    
    public String friend="";
    private String mac;
    
    int min = 10000;
    int max = 20000;

    Random r = new Random();
    int i1 = r.nextInt(max - min + 1) + min;
    
    ArrayList<HashMap<String, String>> record = new ArrayList<HashMap<String, String>>();
    HashMap<String,String> wsd = new HashMap<String, String>();
    
    EditText mMessage;
    Button mButtonSendTo;
    
    List<Item> OiMessages= new ArrayList<Item>();
    
    
    public static final HashMap<String, String> newRecord = new HashMap<String, String>();
    
    public static final HashMap<String, String> myDev = new HashMap<String, String>();
    
    private Handler msgHandler =new Handler();
    
    
    
    private Runnable msgTransfer= new Runnable() {
		
		@Override
		public void run() {
			if(connection==true){
				
					Log.d(TAG, "Peer connected(From the runnable)");
					sendFile();
					File x=lastFileModified();
					try {
						readFile(x);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				
			}else{
			try {
				connectToDest(readMAC());
				File x=lastFileModified();
				readFile(x);
				while(connection==true){
					Log.d(TAG, "Peer connected(From the runnable)");
					
					sendFile();
					
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
			msgHandler.postDelayed(this, i1);
				
	}
	};
	
	 
        
    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }
    
    public void setConnection(boolean connection){
    	this.connection=connection;
    }
    
    
	
	
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.d(TAG, "Test Main Activity onCreate");
        

        // add necessary intent values to be matched.

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        
        /* To auto-accept WiFi-Direct Connection Request. */
        WiFiDirectAutoAccept mAccept = new WiFiDirectAutoAccept(getApplicationContext());
        mAccept.intercept(true);
        
        mac=getWFDMacAddress();
            Log.d(TAG, "Test WiFiDirect MAC: "+mac);
        startRegistrationAndDiscovery();
            
        List<String> contactItems=new ArrayList<String>();
        
        int b=0;
        while(b<GetMessages.getMsg().size())
        {
        	contactItems.add(GetMessages.getMsg().get(b).getMsgDest());
        	b++;
        }
        
        Log.d(TAG,"Contacts in Dropdown: "+contactItems.toString());
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, contactItems);
        this.dropdownContacts=(Spinner)findViewById(R.id.contactSpinner);
        dropdownContacts.setAdapter(adapter);
        
        dropdownContacts.setOnItemSelectedListener(new OnItemSelectedListener() {
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View view, int position, long arg3) {
        		TextView myText=(TextView)view;
        		Log.d(TAG, "To select contact");
                // On selecting a spinner item
        		friend = myText.getText().toString();
        		Log.d(TAG,"You Selected: "+friend);
            }

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				Log.d(TAG, "Nothing selected as contact");
				
			}
		});
        
        
        mMessage=(EditText)findViewById(R.id.edit_msg);
        mButtonSendTo=(Button)findViewById(R.id.sendTo);
        
        mButtonSendTo.setOnClickListener(new View.OnClickListener()
			        {
			            public void onClick(View view)
			            {
			                try {
								writeFile(mMessage.getText().toString(),friend);
								Log.d(TAG,"File created with destination: "+friend.toString());
								Log.d(TAG, "Connecting to Destination: "+readMAC());
								msgTransfer.run();
								
								
								
							//	receivedMsg.run();
								
			//					if(connection==true)
			//					{
			//						sendFile();
			//					}
			//					else
			//					connectToDest(readMAC());
								
								
//								File file = new File(Environment.getExternalStorageDirectory().toString()+"/Oi1.1","OiSending.xml");
//								Uri uri= Uri.fromFile(file);
//								Log.d(TAG, "Test File(To be sent) path: "+uri);
							//	Log.d(TAG, "Test info.GroupOwner"+info.groupOwnerAddress.toString());
								
//								Log.d(TAG, "Test connection before while: "+connection);
								
			//					while (connection==true) 
			//					{
			//						Log.d(TAG, "Test connection: "+connection);
									//Log.d(TAG, "Test inside while");
									//Log.d(TAG, "Test GO Inet address"+DeviceDetailFragment.info.groupOwnerAddress.getHostAddress().toString());
//									while(DeviceDetailFragment.info.groupFormed)
//									{
			//						sendFile();
			//						break;
			//						}
//								};
																	
								
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			            }
			        });
        
 //       getListFiles();
//        try {
//			readFile();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        
        
        
    }
    
    
    
    
    public void sendFile(){
    	while(DeviceDetailFragment.info.groupFormed)
    	{
    	File file = new File(Environment.getExternalStorageDirectory().toString()+"/Oi1.1"+"/Sent","OiSending.xml");
		Uri uri= Uri.fromFile(file);
		Log.d(TAG, "File(To be sent) path: "+uri);
    	Intent serviceIntent = new Intent(getApplicationContext(), FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
        serviceIntent.putExtra(FileTransferService.HOST_ADDRESS,DeviceDetailFragment.info.groupOwnerAddress.getHostAddress());
        Log.d(TAG, "GroupOwner Address: "+DeviceDetailFragment.info.groupOwnerAddress.getHostAddress().toString());
        Log.d(TAG, "Passing GroupOwner Address To Host Address");
        
        if(DeviceDetailFragment.info.isGroupOwner)
        {
        serviceIntent.putExtra(FileTransferService.HOST_ADDRESS,DeviceDetailFragment.mClientAdd.getHostAddress());
        Log.d(TAG, "Client Address:"+DeviceDetailFragment.mClientAdd.getHostAddress().toString());
        serviceIntent.putExtra(FileTransferService.HOST_PORT, 8988);
        }
        else{
        	Log.d(TAG, "Sending to Group Owner:"+DeviceDetailFragment.info.groupOwnerAddress.getHostAddress().toString());
        	serviceIntent.putExtra(FileTransferService.HOST_ADDRESS, DeviceDetailFragment.info.groupOwnerAddress.getHostAddress());
        	serviceIntent.putExtra(FileTransferService.HOST_PORT, 8988);
        	}
        startService(serviceIntent);
        
        break;
    	}
    	
    }
    
    /* Initiate Peer Connection to a Specific Node*/
    public void connectToDest(String mac)
    {
    	String peer=mac;
    	Log.d(TAG, "Peer Address To Connect"+peer);
    	WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = peer;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 0;
        connect(config);
     }
    
    

    /** Register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirect(manager, channel, this);
        registerReceiver(receiver, intentFilter);
  //      startRegistrationAndDiscovery();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
    
    
        
    
    
     /* Registers a local service and then initiates a service discovery */
      private void startRegistrationAndDiscovery() {
    	
    	  //	double sw1=5.0;
        	// For Usense1
        	myDev.put("8a:32:9b:c4:2c:07", "5.0");
        	
        	
        	// For Usense2
         // myDev.put("8a:32:9b:c4:2c:07","10.0");
        
    	Map<String, String> record = new HashMap<String, String>();
        record.put(BluetoothAdapter.getDefaultAdapter().getAddress(), "-1.1");
        record.put(mac, "10.1");
        Log.d(TAG, "MAC to Send in Service Registration: "+mac);
        
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
                    	
                    	Log.d(TAG, "The Arrived Record Contains:" + newRecord.keySet().toString());
                    	
                    	
                     }
                });

        /* After attaching listeners, create a service request and initiate Discovery */
        this.serviceRequest = WifiP2pDnsSdServiceRequest.newInstance(SERVICE_INSTANCE, SERVICE_REG_TYPE);
        this.manager.addServiceRequest(this.channel, this.serviceRequest, new ActionListener() {

                    @Override
                    public void onSuccess() {
                    	Log.d(TAG, "ServiceRequest yes");
                    }

                    @Override
                    public void onFailure(int arg0) {
                    	Log.d(TAG, "ServiceRequest onfailure " + arg0);
                    }
                });
        this.manager.discoverServices(this.channel, new ActionListener() {

            @Override
            public void onSuccess() {
            	Log.d(TAG, "Service discovery initiated");
            }

            @Override
            public void onFailure(int arg0) {
            	Log.d(TAG, "Service discovery failed");
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
		Log.d("Main Write File mac address is", OiMessage.getDeviceSender());
		String recordOiMessage="";
		//time stamp for the message
		double currentTime=System.currentTimeMillis();
		byte[] contentInBytes;
		
		File folder=new File(Environment.getExternalStorageDirectory().toString()+"/Oi1.1"+"/Sent");
		folder.mkdir();
		String dirPath=folder.toString();
		Log.v("Path: ", Environment.getExternalStorageDirectory()+"/Oi1.1"+"/Sent");
        File file = new File(dirPath,"OiSending.xml");
        
        Log.v("Files stored in", dirPath);
   
        // Grabs text for the message content
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
//        TextView view = (TextView) findViewById(R.id.edit_message);
//        view.setText(R.string.empty);
//        view = (TextView) findViewById(R.id.friend_by_mail);
//        view.setText(R.string.empty);
    
	}
	
	
	/**
	 * readFile()
	 * reads a file (internal storage) holding the messages
	 * received via Oi, and places them in a List<Item>
	 * Messages are then shown in Fragment Receiver, once the 
	 *user clicks on Read Messages
	 * @throws IOException
	 */

	
	public void readFile(File input) throws IOException {
		XmlPullParserHandler Messages = new XmlPullParserHandler();
		
		Item OiMsg=new Item(getApplicationContext());
		String OiMsgFormat="";
		
		//getFilesDir is /data/data/com.example.oi/Files
		//@TODO File file = new File(getFilesDir(),"OiReceiving.xml");	
		File file = input;	
		
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
		TextView t = (TextView)findViewById(R.id.receivedMsg);
		for (int i=0; i<OiMessages.size();i=i+1){
			OiMsg=OiMessages.get(i);
			OiMsgFormat=OiMsg.getMessage();
			Log.v("message read\n",OiMsgFormat);
			Log.v("message destination\n",OiMsg.nameReceiver);
			t.append(OiMsgFormat);
			t.append("\n--------------------\n");
			
		}
	
		
	}
	
	
	public static File lastFileModified() {
        File fl = new File(Environment.getExternalStorageDirectory().toString()+"/Oi1.1"+"/Received");
        File[] f1 = fl.listFiles(new FileFilter() {
            public boolean accept(File f1) {
                return f1.isFile();
            }
        });
        long lastMod = Long.MIN_VALUE;
        File choice = null;
        for (File file : f1) {
            if (file.lastModified() > lastMod) {
                choice = file;
                lastMod = file.lastModified();
            }
        }
        Log.d(TAG,"Last Modified: "+choice.toString());
        return choice;
    }
    
	
	public String readMAC() throws IOException
	{
		XmlPullParserHandler Messages = new XmlPullParserHandler();
		Item OiMsg = new Item(getApplicationContext());
		String OiMsgFormat="";
		File file = new File(Environment.getExternalStorageDirectory().toString()+"/Oi1.1"+"Sent");
		try{
		FileInputStream fin = new FileInputStream(file);
		OiMessages=Messages.parse(fin);
		fin.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		for (int i=0; i<OiMessages.size();i=i+1){
			OiMsg=OiMessages.get(i);
			OiMsgFormat=OiMsg.getMessage();
			Log.v("Message destination\n",OiMsg.nameReceiver);
		
		}
		
		return OiMsg.nameReceiver;
	}
	
	public String getWFDMacAddress(){
	    try {
	        List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
	        for (NetworkInterface ntwInterface : interfaces) {

	            if (ntwInterface.getName().equalsIgnoreCase("p2p0")) {
	                byte[] byteMac = ntwInterface.getHardwareAddress();
	                if (byteMac==null){
	                    return null;
	                }
	                StringBuilder strBuilder = new StringBuilder();
	                for (int i=0; i<byteMac.length; i++) {
	                    strBuilder.append(String.format("%02x:", byteMac[i]));
	                }

	                if (strBuilder.length()>0){
	                    strBuilder.deleteCharAt(strBuilder.length()-1);
	                }

	                return strBuilder.toString();
	            }

	        }
	    } catch (Exception e) {
	        Log.d(TAG, e.getMessage());
	    }
	    return null;
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
                        Toast.makeText(MainActivity.this, "Discovery Initiated",Toast.LENGTH_SHORT).show();
//                        startRegistrationAndDiscovery();
//                        String macAddrPeer=getPeerMacAdd();
//                        List<GetMessages> msginstorage=GetMessages.getMsg();
//                        List<Integer> lom=OiRouter.getListMsg(myDev, newRecord, macAddrPeer, msginstorage);
//                        Log.e(TAG, "Message in storage: "+lom.toString());
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(MainActivity.this, "Discovery Failed : " + reasonCode,Toast.LENGTH_SHORT).show();
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
            	Log.d(TAG, "Connecting...");
            	
            	
            	
            	
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
