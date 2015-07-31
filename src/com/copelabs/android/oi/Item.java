/**
 * @file Item.java
 * COPELABS - Oi!
 * @author Rute Sofia (COPELABS/ULHT)
 * @date 20/05/2015
 * @brief Item  corresponds to the object messages sent by Oi
 *  * @version v1.0 - pre-prototype
 */
package com.copelabs.android.oi;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;




/**
 * @param idSender the identifier of the owner's contact (contact URI)
 * @param idReceiver the identifier of the selected contact
 * @param nameSender the name of the Sender contact
 * @param idReceiver the identifier of the selected contact (based on the contact URI)
 * @param nameReceiver the name of the Receiver contact
 * @param message the message content
 * @param timestamp the timestamp for writing the message
 * 
 *
 */
public class Item {  
	// MAC addresses extracted from WifiP2pDevice 
	 private String deviceSender;
	 private String deviceReceiver;
	 // emails
	 private String nameSender="";
     private String nameReceiver="";
     //message content
     private String message="";
     //timestamp for sending the message
     private double timestamp;
     
     public Item(){
    	 super();
     }
     public Item(Context context) {
    	// super();
    	 // sets the local device info
    	 WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    	    WifiInfo wifiInfo = manager.getConnectionInfo();
    	    String mac = wifiInfo.getMacAddress();
    //	   Log.d("Item, mac is:",mac);
    
    	 this.deviceSender = mac;
    	
    	 this.nameSender="me";
     }
     
     
     public String getDeviceSender() {  
        return deviceSender;  
    } 
     public String getDeviceReceiver() {  
         return deviceReceiver;  
     }
    public void setDeviceIds(String deviceId,int s) {
    	// sender id
    	if (s==1) {
    		this.deviceSender = deviceId;  
    	}
    	else {
    		this.deviceReceiver = deviceId;
    	}
    }
      
    public String getNameSender() {  
        return nameSender;  
    }  
    
    public void setNameSender(String name) {  
        this.nameSender = name;  
    }
    
    public void setNameReceiver(String name) {  
        this.nameReceiver = name;  
    }
    
    public void setTimestamp(double ts) {  
        this.timestamp = ts;  
    }
    public void setAttributes(String receiverDeviceId, String nameReceiver, String Message, double timestamp) {
    	  	
    	this.deviceReceiver= receiverDeviceId;
    	//Log.v("setAttrs", Integer.toString(this.idReceiver));
    	this.nameReceiver=nameReceiver;
    	//Log.v("setAttrs", this.nameReceiver);
    	   	
    	this.message=Message;
    	//Log.v("setAttrs", this.message);
    	
    	this.timestamp=timestamp;
    	//Log.v("setAttrs", Double.toString(this.timestamp));
    	
    }
    public void setMessage(String msg) {  
        this.message = msg;  
    } 
    /**
     * Writes an OiMessage (List) in xml format
     * @return String an entry containing the xml format for an OiMessage
     */
    public String getXmlEntry() {
    	
		String format =
	            "<oimessage>\n" +
	            "         <deviceSender>%s</deviceSender>\n" +
	            "         <nameSender>%s</nameSender>\n" +
	            "         <deviceReceiver>%s</deviceReceiver>\n" + 
	            "         <nameReceiver>%s</nameReceiver>\n" +
	            "         <messagecontent>%s</messagecontent>\n" +
	            "         <timestamp>%f</timestamp>\n" + 
	            "</oimessage>\n";
		
		
		return String.format(format,this.deviceSender, this.nameSender, this.deviceReceiver, this.nameReceiver, this.message, this.timestamp);
		
	}
    /**
     * reads an Item Oi Message and provides
     * a string ready to be shown in Oi
     * @return
     * 
     */
    public String getMessage()  {
		String format = "From: %s, %s\nTo: %s, %s\n%s\n%s\n";
		//@TODO check the date, it seems to be badly converted
		Calendar calendar = Calendar.getInstance();
		TimeZone tz = TimeZone.getDefault();
		calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    sdf.setTimeZone(tz);//set time zone.
	    String localTime = sdf.format(new Date((long)this.timestamp * 1000));
	 
		
		return String.format(format,this.deviceSender, this.nameSender, this.deviceReceiver, this.nameReceiver, this.message, localTime);
		
	}
   
   
    
  
 
}  
