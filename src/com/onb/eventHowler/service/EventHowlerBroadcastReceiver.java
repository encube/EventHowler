package com.onb.eventHowler.service;

import com.onb.eventHowler.application.EventHowlerOpenDbHelper;
import com.onb.eventHowler.domain.EventHowlerParticipant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class EventHowlerBroadcastReceiver extends BroadcastReceiver{
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		EventHowlerOpenDbHelper openHelper = new EventHowlerOpenDbHelper(context);
		
		//test
		Cursor participant = openHelper.getAllParticipants();
		Log.d("broadcastReceiver", participant.getCount() + " broadcast receiver");
		participant.close();
		//test
		
	        Bundle bundle = intent.getExtras();        
	        SmsMessage[] msgs = null;
	        String str = "";
	        String name;
	        if (bundle != null)
	        {
	            Object[] pdus = (Object[]) bundle.get("pdus");
	            msgs = new SmsMessage[pdus.length];            
	            for (int i=0; i<msgs.length; i++){
	            	
	            	msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
	            	name = openHelper.findNumber(msgs[i].getOriginatingAddress());
	            	Log.d("onReceiver", msgs[i].getOriginatingAddress());
	            	
	            	if(name != "NONE"){
	            		
	            		openHelper.updateStatus(new EventHowlerParticipant(name,
	            				msgs[i].getDisplayOriginatingAddress(),
	            				msgs[i].getDisplayMessageBody()));
	            		
		                str += "SMS from " + msgs[i].getOriginatingAddress();                     
		                str += " :";
		                str += msgs[i].getMessageBody().toString();
		                str += "\n";
		                Log.d("broadcastReceiver", "receive message from " + msgs[i].getOriginatingAddress());
	            	}
	            }

	            Toast.makeText(context, str, Toast.LENGTH_SHORT).show(); //for test
	        }}

}
