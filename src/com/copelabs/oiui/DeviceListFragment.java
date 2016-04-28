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

package com.copelabs.oiui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.copelabs.android.oi.R;
import com.copelabs.oiaidllibrary.UserDevice;

/**
 * A ListFragment that displays available peers on discovery and requests the
 * parent activity to handle user interaction events
 */
public class DeviceListFragment extends ListFragment implements PeerListListener, FragCommunicatorDevices{
	
	public static final String TAG = "DeviceListFragment";
    private List<UserDevice> peers = new ArrayList<UserDevice>();
    ProgressDialog progressDialog = null;
    View mContentView = null;
    private WifiP2pDevice device;
    private WiFiPeerListAdapter deviceListAdapter;

    
    @Override
    public void onAttach(Activity activity){
    	super.onAttach(activity);
    	Context mContext = getActivity();
    	((MainActivity)mContext).fragmentCommunicatorDevices = this;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.device_list, null);
        return mContentView;
    }

    /**
     * @return this device
     */
    public WifiP2pDevice getDevice() {
    	
        return device;
    }

    private static String getDeviceStatus(int deviceStatus) {
        Log.d(TAG, "Peer Status: " + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }

 
    /**
     * Array adapter for ListFragment that maintains WifiP2pDevice list.
     */
    private class WiFiPeerListAdapter extends ArrayAdapter<UserDevice> {

        private List<UserDevice> items;

        public WiFiPeerListAdapter(Context context, int textViewResourceId,
            List<UserDevice> peers) {
            super(context, textViewResourceId, peers);
            items = peers;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_devices, null);
            }
            UserDevice device = items.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                top.setTextColor(Color.parseColor("#449458"));
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null) {
                    top.setText(device.getDevName());
                }
                if (bottom != null) {
                    bottom.setText(device.getDevAdd());
                }
            }

            return v;

        }
    }

    /**
     * Update UI for this device.
     * 
     * @param device WifiP2pDevice object
     */
    public void updateThisDevice(UserDevice device) {

    	for (UserDevice entry : peers) {
    		if (entry.getDevAdd().equalsIgnoreCase(device.getDevAdd()))
    			return;
    	}
    	peers.add(device);
    	((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    	if (peers.size() == 0) {
            Log.d(TAG, "No devices found");
            return;
        }
    	
    }
    
    public void removeThisDevice(UserDevice device) {

    	for (UserDevice entry : peers) {
    		if (entry.getDevAdd().equalsIgnoreCase(device.getDevAdd())) {
    			peers.remove(entry);
    			break;
    		}
    	}
    	((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    	if (peers.size() == 0) {
            Log.d(TAG, "No devices found");
            return;
        }
    	
    }
    
    public void showMsgFromFramework(int mError) {
    	if (mError == 0) {
    		//interface is off
    		TextView emptyText = (TextView) mContentView.findViewById(android.R.id.empty);
    		emptyText.setText(getString(R.string.p2p_off_warning));
    		peers.clear();
    		((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        	AlertDialog alertDialog = new AlertDialog.Builder(this.getActivity())
	    		.setTitle(getString(R.string.p2p_off_title))
	    		.setMessage(getString(R.string.p2p_off_message))
	    		.setPositiveButton("OK", null)
	    		.create();
	    	alertDialog.show();
    	}else if(mError == 1) {
    		//interface is on
    		TextView emptyText = (TextView) mContentView.findViewById(android.R.id.empty);
    		emptyText.setText(getString(R.string.empty_message));
    	}
	}

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
       
    }

    public void clearPeers() {
        peers.clear();
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    /**
     * 
     */
    public void onInitiateDiscovery() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
//        progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel", "finding peers", true,
//                true, new DialogInterface.OnCancelListener() {
//
//                    @Override
//                    public void onCancel(DialogInterface dialog) {
//                        
//                    }
//                });
    }

    /**
     * An interface-callback for the activity to listen to fragment interaction
     * events.
     */
    public interface DeviceActionListener {

        void showDetails(WifiP2pDevice device);

        void cancelDisconnect();

        void connect(WifiP2pConfig config);

        void disconnect();
    }

	@Override
	public void newDeviceInfo(final UserDevice mDevice) {
		getActivity().runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	        	updateThisDevice(mDevice);
	        }
	    });
	}

	@Override
	public void lostDevice(final UserDevice mDevice) {
		getActivity().runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	        	removeThisDevice(mDevice);
	        }
	    });	
	}

	@Override
	public void showError(final int mError) {
		getActivity().runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	        	showMsgFromFramework(mError);
	        }
	    });	
		
	}
}
