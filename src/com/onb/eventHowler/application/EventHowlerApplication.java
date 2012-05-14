package com.onb.eventHowler.application;

import com.onb.eventHowler.service.*;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;


public class EventHowlerApplication extends Application{
	
	private boolean hasOnHoingEvent;
	
	private final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private EventHowlerBroadcastReceiver eventHowlerBraoaBroadcastReceiver = new EventHowlerBroadcastReceiver();
	private IntentFilter SMS_RECEIVED_FILTER = new IntentFilter(SMS_RECEIVED);
	

	@Override
	public void onCreate() {
		super.onCreate();
	}
		
	public boolean hasOnGoingEvent(){
		return hasOnHoingEvent;
	}
	
	public void startEvent(){
		Log.d("application", "starting event");
		registerReceiver(eventHowlerBraoaBroadcastReceiver, SMS_RECEIVED_FILTER);
		startService(new Intent(this, EventHowlerSenderService.class));
		hasOnHoingEvent = true;
	}
	
	public void stopEvent(){
		Log.d("application", "stopping event");
		unregisterReceiver(eventHowlerBraoaBroadcastReceiver);
		hasOnHoingEvent = false;
	}
}
