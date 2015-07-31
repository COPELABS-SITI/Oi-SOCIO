/**
 * @file XMLPullParserHandler.java
 *  COPELABS - Oi!
 * @author Rute Sofia (COPELABS/ULHT)
 * @date 20/05/2015
 * @brief this class corresponds to the core
 * of Oi!, an instant messaging tool for delay tolerant networking
 * Oi can work without an infrastructure
 * Oi performs routing of messages based on the dLife opportunistic solution
 *
 * @version v1.0 - pre-prototype
 */

package com.copelabs.android.oi;

  
import java.io.IOException;  
import java.io.InputStream;  
import java.util.ArrayList;  
import java.util.List;  

import org.xmlpull.v1.XmlPullParser;  
import org.xmlpull.v1.XmlPullParserException;  
import org.xmlpull.v1.XmlPullParserFactory;  

import android.util.Log;
   
  /**
   * Handles the list of messages associated  to an xml file
   * @param OiMessages list of messages. Each OiMessage holds a set of objects of type Item
   * @author rute
   * @return OiMessages
   *
   */
public class XmlPullParserHandler {  
    private List<Item> OiMessages= new ArrayList<Item>();  
    private Item OiMessage;  
    private String text;  
   
    public List<Item> getOiMessages() {  
        return OiMessages;  
    }  
   
    /**
     * 
     * @param is
     * Parses messages stored in an xml file descriptor is
     * @return
     */
    public List<Item> parse(InputStream is) {  
           try {  
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();  
            factory.setNamespaceAware(true);  
            XmlPullParser  parser = factory.newPullParser();  
            String currentTag=null;
   
            parser.setInput(is, null);  
   
            int eventType = parser.getEventType();  
            while (eventType != XmlPullParser.END_DOCUMENT) {  
                 
               
                switch (eventType) {  
                case XmlPullParser.START_TAG:
                	currentTag = parser.getName();
                	//Log.v("Start_tag",currentTag.toString());
                    if (currentTag.equalsIgnoreCase("oimessage")) {  
                        // create a new instance of an Oi Message  
                        OiMessage = new Item();  
                    }  
                    break;  
   
                case XmlPullParser.TEXT:  
                	//Log.v("TEXT  ",currentTag.toString());
                	text = parser.getText(); 
                //	Log.v("getting text",text.toString());
                    break;  
   
                case XmlPullParser.END_TAG: 
                	currentTag=parser.getName();
                	//Log.v("end_tag   ",currentTag.toString());
                    if (currentTag.equalsIgnoreCase("oimessage")) {  
                        // add OiMessage object to list  
                        OiMessages.add(OiMessage);  
                    }else if (currentTag.equalsIgnoreCase("deviceSender")) {
                    	OiMessage.setDeviceIds(text.toString(), 1);
                    }
                    else if (currentTag.equalsIgnoreCase("nameSender")) {  
                        OiMessage.setNameSender(text.toString());  
                    }else if (currentTag.equalsIgnoreCase("deviceReceiver")) {  
                        OiMessage.setDeviceIds(text.toString(),0);  
                    }  else if (currentTag.equalsIgnoreCase("nameReceiver")) {  
                        OiMessage.setNameReceiver(text.toString());  
                    } else if (currentTag.equalsIgnoreCase("messagecontent")) {  
                        OiMessage.setMessage(text.toString());  
                    } else if (currentTag.equalsIgnoreCase("timestamp")) {  
                        OiMessage.setTimestamp(Double.parseDouble(text.toString()));  
                    }   
                    break;  
   
                default:  
                    break;  
                }  
                eventType = parser.next();  
            }  
   
        } catch (XmlPullParserException e) {e.printStackTrace();}   
        catch (IOException e) {e.printStackTrace();}  
   
        return OiMessages;  
    } 
    
    
}  
