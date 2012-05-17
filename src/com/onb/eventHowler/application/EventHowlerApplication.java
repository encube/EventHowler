package com.onb.eventHowler.application;

import com.onb.eventHowler.service.*;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;


public class EventHowlerApplication extends Application{
	
	private static boolean withOngoingEvent;
	private boolean runningLastCycle;

	private final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private EventHowlerBroadcastReceiver eventHowlerBraoaBroadcastReceiver = new EventHowlerBroadcastReceiver();
	private IntentFilter SMS_RECEIVED_FILTER = new IntentFilter(SMS_RECEIVED);
	

	@Override
	public void onCreate() {
		super.onCreate();
	}
		
	public boolean hasOngoingEvent(){
		return withOngoingEvent;
	}
	
	public void startEvent(){
		Log.d("startEvent", "starting event");
		withOngoingEvent = true;
		registerReceiver(eventHowlerBraoaBroadcastReceiver, SMS_RECEIVED_FILTER);
		startService(new Intent(this, EventHowlerSenderService.class));
	}
	
	public void stopEvent(){
		Log.d("stopEvent", "stopping event");
		runningLastCycle = true;
		withOngoingEvent = false;
		unregisterReceiver(eventHowlerBraoaBroadcastReceiver);
	}
	
	public boolean isRunning() {
		return runningLastCycle;
	}

	public void setRunning(boolean finishing) {
		this.runningLastCycle = finishing;
	}
}
