package com.onb.eventHowler.service;

import com.onb.eventHowler.application.EventHowlerOpenDbHelper;
import com.onb.eventHowler.domain.EventHowlerParticipant;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;


public class EventHowlerBroadcastReceiver extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		EventHowlerOpenDbHelper openHelper = new EventHowlerOpenDbHelper(context);
		
        Bundle bundle = intent.getExtras();        
        SmsMessage[] msgs = null;
        if (bundle != null){
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];            
            for (int i=0; i<msgs.length; i++){
            	
            	msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
	            	
            	if(openHelper.checkNumberIfExist(msgs[i].getOriginatingAddress())){
	            		
            		openHelper.updateStatus(new EventHowlerParticipant(
       				msgs[i].getDisplayOriginatingAddress(), "", "REPLY_RECEIVED"), msgs[i].getDisplayMessageBody());

	            	Log.d("EventHowlerBroadcastReceiver", "receive message from " + msgs[i].getOriginatingAddress());
	         	}
            	else{
            		Log.d("EventHowlerBroadcastReceiver", "unknown address " + msgs[i].getOriginatingAddress());
            	}
	        }
            openHelper.close();
	    }
    }

}
