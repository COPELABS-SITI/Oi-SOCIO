package com.copelabs.oiui;

import java.util.ArrayList;
import java.util.List;

import com.copelabs.android.oi.R;
import com.copelabs.oiaidllibrary.IRemoteOiFramework;
import com.copelabs.oiaidllibrary.IRemoteOiFrameworkCallback;
import com.copelabs.oiaidllibrary.UserDevice;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity{

    public static final String TAG = "OiMainActivity";
 
	ContactListAdapter mAdapter;
    private UserDevice mFriendSelected;
    
    private IRemoteOiFramework service;
    private RemoteOiFrameworkConnection serviceConnection;
        
    // List of contacts received from the framework
    List<UserDevice> mListContacts = new ArrayList<UserDevice>();
    
    //To comunicate with MessageFragment
    public FragCommunicatorMessages fragmentCommunicatorMessages;
    public FragCommunicatorDevices fragmentCommunicatorDevices;
    
    private void setFriendSelected(UserDevice mFriendSelected) {
    	this.mFriendSelected = mFriendSelected;
    }
    
    private UserDevice getFriendSelected() {
    	return this.mFriendSelected;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //set visibility
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(intent);

              
        /* Connect to Oi Framework */
        connectService();
        
        mAdapter = new ContactListAdapter(this, mListContacts);
        Spinner dropdownContacts=(Spinner)findViewById(R.id.contactSpinner);
        dropdownContacts.setAdapter(mAdapter);
        dropdownContacts.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				//User Selected a friend. Save this info, so it can be used to deliver the messages
				TextView myText=(TextView)view;
        		Log.d(TAG,"You Selected: "+myText.getText().toString());        		
        		setFriendSelected(new UserDevice(myText.getTag().toString(), myText.getText().toString()));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Log.d(TAG, "Nothing selected as contact");
			}
		});
        
        //When Send To button is pressed
        ((Button)findViewById(R.id.sendTo)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (service == null)
					return;
				try {
					EditText mTxtBox = (EditText)findViewById(R.id.edit_msg);
					UserDevice mDestination= getFriendSelected();
					if (mDestination != null) {
						service.send(mDestination, mTxtBox.getText().toString());
						fragmentCommunicatorMessages.passDataToFragment(null, mDestination, mTxtBox.getText().toString());
						mTxtBox.setText("");
					} else {
						Toast.makeText(getApplicationContext(), getString(R.string.nocontactselected), Toast.LENGTH_SHORT).show();
					}		
				} catch (RemoteException e) {
					alertAidlError();
				}
			}
        });
    }    

    /** Register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // Back?
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Back
            moveTaskToBack(true);
            return true;
        }
        else {
            // Return
            return super.onKeyDown(keyCode, event);
        }
    }
    /*
     * AIDL Framework connection
     */
    
    private void alertAidlError() {
    	//Toast.makeText(MainActivity.this, getString(R.string.aidlnotfound), Toast.LENGTH_LONG).show();
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Framework missing");
        builder.setMessage("Communication framework is missing. This framework must be installed to use Oi!. "
        		+ "Do you want to install the Communication Framework?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
				 Intent intent = new Intent(Intent.ACTION_VIEW);
				 intent.setData(Uri.parse("market://details?id=com.copelabs.oiframework"));
				 startActivity(intent);
                 dialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                 dialog.dismiss();
                 finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void connectService() {
        serviceConnection = new RemoteOiFrameworkConnection();
        Intent i = new Intent("com.copelabs.oiframework.contentmanager");
        i.setPackage("com.copelabs.oiframework");
        boolean ret = bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
        if (!ret)  
        	alertAidlError();
    }
    
    class RemoteOiFrameworkConnection implements ServiceConnection {

        public void onServiceConnected(ComponentName name, IBinder boundService) {
            service = IRemoteOiFramework.Stub.asInterface((IBinder) boundService);
            try {
				service.registerCallback(mCallback, "oi");
				List<UserDevice> mContactList = service.getContactList();
				if (mContactList == null)
					return;
				String mLocalName = service.getLocalName();
				if(fragmentCommunicatorDevices != null) {
					fragmentCommunicatorDevices.showError(service.isInterfaceEnabled());
	            }
				
				TextView mLocalNameView = (TextView)findViewById(R.id.local_name);
				mLocalNameView.setText("Local Device Name: " + mLocalName);
				mListContacts.clear();
				mListContacts.addAll(mContactList);
				if (!mListContacts.isEmpty() && mAdapter != null)
					mAdapter.notifyDataSetChanged();
			} catch (RemoteException e) {
				alertAidlError();
			}
            Toast.makeText(MainActivity.this, "Service connected", Toast.LENGTH_LONG)
                    .show();
        }

        public void onServiceDisconnected(ComponentName name) {
            service = null;
            Toast.makeText(MainActivity.this, "Service disconnected", Toast.LENGTH_LONG)
                    .show();
            connectService();
        }
    }
    
    /**
     * This implementation is used to receive callbacks from the remote
     * service.
     */
    private IRemoteOiFrameworkCallback mCallback = new IRemoteOiFrameworkCallback.Stub() {
        /**
         * This is called by the remote service regularly to tell us about
         * new values.  Note that IPC calls are dispatched through a thread
         * pool running in each process, so the code executing here will
         * NOT be running in our main thread like most other things -- so,
         * to update the UI, we need to use a Handler to hop over there.
         */
       
		@Override
		public void receive(UserDevice mSource, String mMessage) throws RemoteException {
			Log.i(TAG, "Message From Framework: " + mMessage);
			if(fragmentCommunicatorMessages != null) {
				fragmentCommunicatorMessages.passDataToFragment(mSource, null, mMessage);
            }
		}

		@Override
		public void newDeviceFound(UserDevice mDevice) throws RemoteException {
			Log.i(TAG, "New Device From Framework: " + mDevice.getDevName());
			if(fragmentCommunicatorDevices != null) {
				fragmentCommunicatorDevices.newDeviceInfo(mDevice);
            }
		}

		@Override
		public void contactListUpdated(final List<UserDevice> mUpdatedList)
				throws RemoteException {
			
			if (mUpdatedList == null)
				return;
			
			runOnUiThread(new Runnable() {
				@Override
		        public void run() {
					Log.i(TAG, "Contact List Updated");
					mListContacts.clear();
					mListContacts.addAll(mUpdatedList);
					if (!mListContacts.isEmpty() && mAdapter != null)
						mAdapter.notifyDataSetChanged();
		        }
			});
		}

		@Override
		public void deviceLost(UserDevice mDevice) throws RemoteException {
			Log.i(TAG, "New Device From Framework: " + mDevice.getDevName());
			if(fragmentCommunicatorDevices != null) {
				fragmentCommunicatorDevices.lostDevice(mDevice);
            }
		}

		@Override
		public void error(int mError) throws RemoteException {
			if(fragmentCommunicatorDevices != null) {
				fragmentCommunicatorDevices.showError(mError);
            }
		}
		
		
    };
}
