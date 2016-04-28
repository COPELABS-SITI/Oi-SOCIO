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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.copelabs.android.oi.R;
import com.copelabs.oiaidllibrary.UserDevice;



/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceMessageFragment extends Fragment implements FragCommunicatorMessages{
	
	public static final String TAG = "DeviceDetailFragment";
	protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private static View mContentView = null;
    private WifiP2pDevice device;
    public static WifiP2pInfo info;
    ProgressDialog progressDialog = null;
    public static boolean transferStatus=false;
    
    private ListView listView;
    ChatMessageAdapter adapter = null;
    private List<Message> items = new ArrayList<Message>();
    
    private Context mContext;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	
        mContentView = inflater.inflate(R.layout.device_detail, null);
        listView = (ListView) mContentView.findViewById(android.R.id.list);
        adapter = new ChatMessageAdapter(getActivity(), R.layout.message_row,
                items);
        listView.setAdapter(adapter);
        
        return mContentView;
    }
    
    @Override
    public void onAttach(Activity activity){
    	super.onAttach(activity);
    	mContext = getActivity();
    	((MainActivity)mContext).fragmentCommunicatorMessages = this;
   }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
        
    	Uri uri = data.getData();
        
        Log.d(TAG, "Intent- " + uri);
        
    }

    
    /**
     * Updates the UI with device data
     * 
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
 //       TextView view = (TextView) mContentView.findViewById(R.id.device_address);
 //       view.setTextColor(Color.parseColor("#186467"));
        

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {

        this.getView().setVisibility(View.GONE);
    }

	@Override
	public void passDataToFragment(final UserDevice mSource, final UserDevice mDestination, final String mMessage) {
		/*
		TextView t = (TextView) mContentView.findViewById(R.id.receivedMsg);
		t.setTextColor(Color.parseColor("#000000"));
		for (int i=0; i<OiMessages.size();i=i+1){
			OiMsg=OiMessages.get(i);
			OiMsgFormat=OiMsg.getMessage();
//			Log.v("message read\n",OiMsgFormat);
//			Log.v("message destination\n",OiMsg.nameReceiver);
//			if(OiMsg.nameReceiver==MainActivity.getWFDMacAddress())
//			{
			t.append("\n---------------------------------\n");
			t.append(OiMsgFormat);
//			}
			
			
		}*/
		getActivity().runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	        	/*TextView mTextBox = (TextView) mContentView.findViewById(R.id.receivedMsg);
	    		mTextBox.setTextColor(Color.parseColor("#000000"));	    		
	    		String sourceString = "<b>" + mSource.getDevName() + ":" + "</b> " + mMessage; 
	    		mTextBox.append(Html.fromHtml(sourceString) + "\n");
	    		*/
	        	
	        	Message mNewMessage = new Message();
	        	mNewMessage.setDestination(mDestination);
	        	mNewMessage.setSource(mSource);
	        	mNewMessage.setMessage(mMessage);
	        	pushMessage(mNewMessage);
	        	
	        }
	    });
	}
	
	public void pushMessage(Message mNewMessage) {
        adapter.add(mNewMessage);
        adapter.notifyDataSetChanged();
    }
	
	/**
     * ArrayAdapter to manage chat messages.
     */
    public class ChatMessageAdapter extends ArrayAdapter<Message> {

        List<Message> messages = null;
        int layoutResourceId;
        Context context;
        
        public ChatMessageAdapter(Context context, int layoutResourceId,
                List<Message> items) {
            super(context, layoutResourceId, items);
            this.context = context;
            this.layoutResourceId = layoutResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
            	LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                v = inflater.inflate(layoutResourceId, parent, false);
            }
            Message message = items.get(position);
            if (message != null) {
                TextView nameText = (TextView) v
                        .findViewById(R.id.message);
                TextView fromText = (TextView) v
                        .findViewById(R.id.from);
                if (nameText != null && fromText != null) {
                    nameText.setText(message.getMessage());
                    nameText.setTextSize(20);
                    if (message.getSource() == null) {
                        nameText.setTextAppearance(getActivity(),
                                R.style.MeText);
                        nameText.setGravity(Gravity.END);
                        fromText.setText("To " + message.getDestination().getDevName());
                        fromText.setGravity(Gravity.END);
                    } else {
                        nameText.setTextAppearance(getActivity(),
                                R.style.OthersText);
                        nameText.setGravity(Gravity.START);
                        fromText.setText("From " + message.getSource().getDevName());
                        fromText.setGravity(Gravity.START);
                    }
                }
            }
            return v;
        }
    }
    
    private class Message {
    	private UserDevice mSource;
    	private UserDevice mDestination;
    	private String mMessage;

    	public void setSource(UserDevice mSource) {
    		this.mSource = mSource;
    	}
    	
    	public void setDestination(UserDevice mDestination) {
    		this.mDestination = mDestination;
    	}
    	
    	public void setMessage(String mMessage) {
    		this.mMessage = mMessage;
    	}
    	
    	public UserDevice getSource() {
    		return mSource;
    	}
    	
    	public UserDevice getDestination() {
    		return mDestination;
    	}
    	
    	public String getMessage() {
    		return mMessage;
    	}
    }
}
