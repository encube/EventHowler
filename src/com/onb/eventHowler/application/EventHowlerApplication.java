package com.onb.eventHowler.application;

import com.onb.eventHowler.service.*;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;


public class EventHowlerApplication extends Application{
	
	private EventHowlerOpenDbHelper openHelper; 
	private static boolean withOngoingEvent;
	private boolean runningLastCycle;
	private boolean sendingServiceRunning;
	
	private ServiceStatus eventHowlerWebQueryServiceStatus;
	
	private boolean eventHowlerWebReplyServiceFinished = true;
	private boolean eventHowlerWebUpdateServiceFinished = true;
	private boolean eventHowlerSenderServiceFinished = true;
	public final String DOMAIN = "10.10.6.126";
	public final String PORT = "8080";

	private final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private EventHowlerBroadcastReceiver eventHowlerBroadcastReceiver = new EventHowlerBroadcastReceiver();
	private IntentFilter SMS_RECEIVED_FILTER = new IntentFilter(SMS_RECEIVED);
	
	private String eventId, secretKey;
	

	@Override
	public void onCreate() {
		openHelper = new EventHowlerOpenDbHelper(this);
		super.onCreate();
	}
	
	public void resetDatabaseIfAllServicesAreFinished(){
		if(allServicesFinished()){
			openHelper.resetDatabase();
		}
	}
	
	public boolean allServicesFinished(){
		Log.d("allservice finished",
				"update: " + eventHowlerWebUpdateServiceFinished +
				" Reply: " + eventHowlerWebReplyServiceFinished +
				" Sender: " + eventHowlerSenderServiceFinished);
		
		return eventHowlerWebUpdateServiceFinished &&
				eventHowlerWebReplyServiceFinished &&
				eventHowlerSenderServiceFinished; 
	}
	
	/**
	 * starts the {@link EventHowlerWebQueryService}
	 */
	public void startRetrievingToURL(){
		sendingServiceRunning = false;
		withOngoingEvent = true;
		startService(new Intent(this, EventHowlerWebQueryService.class));
	}
	
	/**
	 * starts the {@link EventHowlerSenderService}, {@link EventHowlerWebUpdateService} and {@link EventHowlerWebReplyService} in NOT_STICKY mode.
	 * also register {@link EventHowlerBroadcastReceiver} to start filtering incoming messages.
	 */
	public void startEvent(){
		sendingServiceRunning = true;
		Log.d("startEvent", "starting event");
		registerReceiver(eventHowlerBroadcastReceiver, SMS_RECEIVED_FILTER);
		startService(new Intent(this, EventHowlerSenderService.class));
		startService(new Intent(this, EventHowlerWebUpdateService.class));
		startService(new Intent(this, EventHowlerWebReplyService.class));
	}
	
	/**
	 * sets withOngoingEvent to false to stop services and runningLastCycle to true for 
	 * {@link EventHowlerSenderService} to run last cycle to avoid skipping message to be sent. 
	 */
	public void stopEvent(){
		setEventHowlerURLRetrieverServiceStatus(ServiceStatus.STOP);
		Log.d("stopEvent", "stopping event");
		runningLastCycle = true;
		withOngoingEvent = false;
		if(sendingServiceRunning){
			unregisterReceiver(eventHowlerBroadcastReceiver);
		}
		else{
			runningLastCycle = false;
		}
	}
	
	/**
	 * 
	 * @return the state of hasOnGoingEvent field
	 */
	public boolean hasOngoingEvent(){
		return withOngoingEvent;
	}
	
	/**
	 * 
	 * @return the state of runningLastCycle field
	 */
	public boolean isRunningLastCycle() {
		return runningLastCycle;
	}

	/**
	 * 
	 * @param isLastCycle		state to be set to runningLstCyle.
	 */
	public void setRunningLastCycle(boolean isLastCycle) {
		this.runningLastCycle = isLastCycle;
	}

	/**
	 * 
	 * @return		event id of the current event
	 */
	public String getEventId() {
		return eventId;
	}

	/**
	 * 
	 * @param eventId		event id to be set
	 */
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	/**
	 * 
	 * @return		secret key of the current event
	 */
	public String getSecretKey() {
		return secretKey;
	}

	/**
	 * 
	 * @param secretKey		secret key to be set
	 */
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	
	/**
	 * 
	 * @return		status of {@link EventHowlerWebQueryService}
	 */
	public ServiceStatus getEventHowlerWebQueryServiceStatus() {
		return eventHowlerWebQueryServiceStatus;
	}

	/**
	 * sets the status
	 * 
	 * @param eventHowlerURLRetrieverService		Status to be set
	 */
	public void setEventHowlerURLRetrieverServiceStatus(
			ServiceStatus eventHowlerURLRetrieverService) {
		this.eventHowlerWebQueryServiceStatus = eventHowlerURLRetrieverService;
	}

	public void setSenderServiceFinished(boolean isFinished) {
		eventHowlerSenderServiceFinished = isFinished;
	}

	public void setWebReplyServiceFinished(boolean isFinished) {
		eventHowlerWebReplyServiceFinished = isFinished;
		
	}

	public void setWebUpdateServiceFinished(boolean isFinished) {
		eventHowlerWebUpdateServiceFinished = isFinished;
		
	}
}
