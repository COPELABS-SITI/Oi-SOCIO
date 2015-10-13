/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.copelabs.android.oi;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothClass.Device;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.copelabs.android.oi.DeviceListFragment.DeviceActionListener;
import com.copelabs.android.oi.Item;
import com.copelabs.android.oi.MainActivity;
import com.copelabs.android.oi.FileTransferService;
import com.copelabs.android.oi.R;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.conn.scheme.LayeredSocketFactory;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {
	
	public static final String TAG = "DeviceDetailFragment";
	protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    public static WifiP2pInfo info;
    ProgressDialog progressDialog = null;
    static InetAddress mClientAdd;
    List<Item> OiMessages= new ArrayList<Item>();
    
    
//    Button mButtonxml;
//	EditText mMail;
//	EditText mMessage;
	
//	List<Item> OiMessages= new ArrayList<Item>(); 
    


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	

        mContentView = inflater.inflate(R.layout.device_detail, null);
        
        
         
        
//     mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

//            @Override
//            public void onClick(View v) {
//                WifiP2pConfig config = new WifiP2pConfig();
//                config.deviceAddress = device.deviceAddress;
//                config.wps.setup = WpsInfo.PBC;
//                if (progressDialog != null && progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }
//                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
//                        "Connecting to :" + device.deviceAddress, true, true
////                        new DialogInterface.OnCancelListener() {
////
////                            @Override
////                            public void onCancel(DialogInterface dialog) {
////                                ((DeviceActionListener) getActivity()).cancelDisconnect();
////                            }
////                        }
//                        );
//                ((DeviceActionListener) getActivity()).connect(config);
//
//            }
//        });

        
        /* 2/10/15*/
//        mContentView.findViewById(R.id.btn_xml).setOnClickListener(
//        		new View.OnClickListener() {
//					
//					@Override
//					public void onClick(View v) {
//						// TODO Auto-generated method stub
//						try {
//							writeFile(mMessage.getText().toString(),mMail.getText().toString());
//							
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}finally{
//
//							File file = new File(getActivity().getApplicationContext().getFilesDir(),"OiSending.xml");
//		                     Uri uri= Uri.fromFile(file);
//	                         TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
//	                         statusText.setText("Sending: " + uri);
//	                         Log.d(MainActivity.TAG, "Intent----------- " + uri);
//
//						}
//						
//						
//					}
//				}
//        		);
        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceActionListener) getActivity()).disconnect();
                        Log.d(TAG, "Disconnected");
                    }
                });
//
//        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
//                new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                        intent.setType("image/*");
//                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
//                    }
//                });
//        mContentView.findViewById(R.id.btn_xml).setOnClickListener(
//                new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        // Allow user to pick an image from Gallery or other
//                        // registered apps
//                    	try {
//							writeFile(mMessage.getText().toString(),mMail.getText().toString());
//							
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						} finally{
//						
//							File file = new File(getActivity().getApplicationContext().getFilesDir(),"OiSending.xml");
//		                     Uri uri= Uri.fromFile(file);
//		                     
//		                     Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
//	                         serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
//	                         serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
//	                         
//	                         if(info.isGroupOwner)
//	                         {
//	                         serviceIntent.putExtra(FileTransferService.HOST_ADDRESS,mClientAdd.getHostAddress());
//	                         Log.d(MainActivity.TAG, "Sending to Group Client:"+mClientAdd.getHostAddress().toString());
//	                         serviceIntent.putExtra(FileTransferService.HOST_PORT, 8988);
//	                         }
//	                         else{
//	                         	Log.d(MainActivity.TAG, "Sending to Group Owner:"+info.groupOwnerAddress.getHostAddress().toString());
//	                         	serviceIntent.putExtra(FileTransferService.HOST_ADDRESS, info.groupOwnerAddress.getHostAddress());
//	                         	serviceIntent.putExtra(FileTransferService.HOST_PORT, 8988);
//	                      	
//	                         }
//	                       
//	                         getActivity().startService(serviceIntent);
//	        //                 startActivityForResult(serviceIntent,CHOOSE_FILE_RESULT_CODE );
//							
////                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
////                        intent.setType("text/xml");
////                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
////                        
//                    }
//                    } 	
//                });
//
        return mContentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
        Uri uri = data.getData();
        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
        statusText.setText("Sending: " + uri);
        Log.d(TAG, "Intent- " + uri);
        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        Log.d(TAG, "Sending File Intent Choosen");

        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
        Log.d(TAG, "Sending to Host's Address Step 1...");
        
        if(info.isGroupOwner)
        {
        serviceIntent.putExtra(FileTransferService.HOST_ADDRESS,mClientAdd.getHostAddress());
        Log.d(TAG, "Sending to Group Client: "+mClientAdd.getHostAddress().toString());
        serviceIntent.putExtra(FileTransferService.HOST_PORT, 8988);
        }
        else{
        	Log.d(TAG, "Sending to Group Owner: "+info.groupOwnerAddress.getHostAddress().toString());
        	serviceIntent.putExtra(FileTransferService.HOST_ADDRESS, info.groupOwnerAddress.getHostAddress());
        	serviceIntent.putExtra(FileTransferService.HOST_PORT, 8988);
     	
        }
        
        getActivity().startService(serviceIntent);
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);
        
        

        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                        : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());

        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        if (info.groupFormed /*&& info.isGroupOwner*/) {
            new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text))
                    .execute();
            
            
      //      mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
      //      mContentView.findViewById(R.id.btn_xml).setVisibility(View.VISIBLE);
        } 
//        else if (info.groupFormed) {
//            // The other device acts as the client. In this case, we enable the
//            // get file button.
//            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
//            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
//                    .getString(R.string.client_text));
//        }

        // hide the connect button
       // mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
        
   //     sendFile();
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
    
    public void sendFile(){
        File file=new File(Environment.getExternalStorageDirectory().toString()+"/Oi1.1"+"/Sent","OiSending.xml");
        
        Uri uri=Uri.fromFile(file);
        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
        
        if(info.isGroupOwner)
        {
        serviceIntent.putExtra(FileTransferService.HOST_ADDRESS,mClientAdd.getHostAddress());
        Log.d(TAG, "Sending to Group Client: "+mClientAdd.getHostAddress().toString());
        serviceIntent.putExtra(FileTransferService.HOST_PORT, 8988);
        }
        else{
        	Log.d(TAG, "Sending to Group Owner: "+info.groupOwnerAddress.getHostAddress().toString());
        	serviceIntent.putExtra(FileTransferService.HOST_ADDRESS, info.groupOwnerAddress.getHostAddress());
        	serviceIntent.putExtra(FileTransferService.HOST_PORT, 8988);
     	
        }
      
        getActivity().startService(serviceIntent);
        }

    
    
    

    /**
     * Updates the UI with device data
     * 
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
  //      view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());
        

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
 //       mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
 //       view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
//        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private TextView statusText;
        /**
         * @param context
         * @param statusText
         */
        public FileServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }
       
        
                
        @Override
		protected void onProgressUpdate(Void... values) {
			statusText.setText("Receiving File...");
			super.onProgressUpdate(values);
		}



		@Override
        protected String doInBackground(Void... params) {
            try {
            	Log.d(TAG, "FileServerSyncTask doInBackground Started");
            	
                ServerSocket serverSocket = new ServerSocket(8988);
                serverSocket.setReuseAddress(true);
                Log.d(TAG, "Server Socket opened");
                Socket client = serverSocket.accept();
                Log.d(TAG, "Server Accepted Client Socket");
                mClientAdd=client.getInetAddress();
                Log.d(TAG, "Client's IP: "+mClientAdd.toString());
                final File f = new File(Environment.getExternalStorageDirectory() + "/Oi1.1"
                        + "/Received" + "/Oi1.1-" + System.currentTimeMillis()
                        + ".xml");

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();

                Log.d(TAG, "Copying Files " + f.toString());
                InputStream inputstream = client.getInputStream();
                Log.d(TAG, "Received data from Client's InputStream ");
                copyFile(inputstream, new FileOutputStream(f));
                serverSocket.close();
                Log.d(TAG, "Server Socket closed - "+serverSocket.isClosed());
                return f.getAbsolutePath();
            } catch (IOException e)
            	{
                	Log.e(TAG, e.getMessage());
                	return null;
            	}           	
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                statusText.setText("File Received - " + result);
                new FileServerAsyncTask(context, this.statusText)
                .execute();
                
                
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                intent.setDataAndType(Uri.parse("file://" + result), "text/xml");
                context.startActivity(intent);
            }

        }

        

    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        long startTime=System.currentTimeMillis();
        Log.d(TAG,"Inside copyFile");
        Log.d(TAG,"InputStream Opened");
        Log.d(TAG,"OutputStream Opened");
        
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            Log.d(TAG,"OutputStream Closed");
            inputStream.close();
            Log.d(TAG,"InputStream Closed");
            long endTime=System.currentTimeMillis()-startTime;
            Log.d(TAG,"Time taken to transfer all bytes is : "+endTime);
            
            
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public void writeFile(String message, String nameReceiver) throws IOException {
		// stores the message to be sent
		Item OiMessage = new Item(getActivity().getApplicationContext());
		Log.d(TAG,"Main Write File mac address is: "+ OiMessage.getDeviceSender());
		String recordOiMessage="";
		//time stamp for the message
		double currentTime=System.currentTimeMillis();
		byte[] contentInBytes;
		
        File file = new File(getActivity().getFilesDir().getAbsolutePath(),"OiSending.xml");
        Log.d(TAG,"Files stored in "+ getActivity().getFilesDir().getAbsolutePath().toString());       
        //grabs text for the message content
        Log.d("Main Write File", "2");
        OiMessage.setAttributes("nomac", nameReceiver,message, currentTime);
                     
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
        
    
	}
	

}
