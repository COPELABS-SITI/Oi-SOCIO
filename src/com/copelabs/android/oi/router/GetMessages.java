package com.copelabs.android.oi.router;
import java.util.ArrayList;
import java.util.List;

import com.copelabs.android.oi.*;

public class GetMessages {
	
	int msgId;
	String msgDest;

	public GetMessages(int msg, String str) {
		// TODO Auto-generated constructor stub
		msgId=msg;
		msgDest=str;
	}
	
	public int getMsgId() {  
        return msgId;  
    }
	
	public String getMsgDest() {  
        return msgDest;  
    }
	
	public static List<GetMessages> getMsg(){
	List<GetMessages> msgList=new ArrayList<GetMessages>();
	
	GetMessages u1=new GetMessages(1,"78:F7:BE:C8:3D:97");
	GetMessages u2=new GetMessages(2,"8a:32:9b:c4:2c:07");
	
	msgList.add(u1);
	msgList.add(u2);
	
	
	return msgList;
	}
	
//	public void setMsgId(List<GetMessages> l) {  
//        this.msgId=l.get(1).msgId; 
//    }
	
	

}
