package com.onb.eventHowler.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import com.onb.eventHowler.application.EventHowlerApplication;
import com.onb.eventHowler.application.EventHowlerOpenDbHelper;
import com.onb.eventHowler.application.MessageStatus;
import com.onb.eventHowler.domain.EventHowlerParticipant;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

public class EventHowlerWebUpdateService extends Service {

	private static EventHowlerOpenDbHelper openHelper;
	private static final String QUERY_URL_FORMAT = "http://%s:%s/EventHowlerApp/query?transId=%s&status=%s";
	private static final String WEB_DOMAIN = "10.10.6.83";
	private static final String PORT_NO = "8080";
	private static final String SUCCESS = "SUCCESS";
	private static final String FAILED = "FAILED";
	private static final long UPDATE_INTERVAL = 10000;
	private EventHowlerApplication application;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.d("onStartCommand", "starting Web Query (former URL Retriever)");
		
		openHelper = new EventHowlerOpenDbHelper(getApplicationContext());
		application = (EventHowlerApplication)(getApplication());
		
		return Service.START_NOT_STICKY;
	}
	
	public void startQuerying()
	{		
		Thread queryThread = new Thread( new Runnable() {
			public void run(){				
				//boolean serviceStarted = false;
		
				while(application.hasOngoingEvent()){
					//application.setEventHowlerURLRetrieverServiceStatus(Status.RUNNING); if errors are present.
					
					
					if(!participantIsEmpty()) {
						updateWebApp();
						
						//Log.d("not empty participant", "here here");
						//startRunning();
						//serviceStarted = true;
					}
					threadSleep(UPDATE_INTERVAL);
				}
			}

			private boolean participantIsEmpty() {
				Cursor participants = openHelper.getAllParticipants();
				boolean result = (participants.getCount() == 0);
				participants.close();
				return result;
			}
		});
		
		queryThread.start();
	}
	
	private void threadSleep(long msec) {
		try {
			Thread.sleep(msec);
		}
		catch (Exception e) {Log.d("startQuerying", "UNABLE TO SLEEP");}
	}
	
	
	
	public void updateWebApp(){
		
		Cursor participants = openHelper.getAllParticipantsWithMessageSendingAttempts();
		participants.moveToFirst();
		do {
			EventHowlerParticipant participant = EventHowlerOpenDbHelper.getParticipantFromCursor(participants);
			
			String status = participant.getStatus();
			
			if(status.equalsIgnoreCase(MessageStatus.UNSENT.toString()) || 
					status.equalsIgnoreCase(MessageStatus.UNSUCCESSFUL_DELIVERY.toString())) {
				updateParticipantStatus(participant, FAILED);
			}
			
			else if(status.equalsIgnoreCase(MessageStatus.SUCCESSFUL_DELIVERY.toString())) {
				updateParticipantStatus(participant, SUCCESS);
			}
		
		} while (participants.moveToNext());
	}
	
	public void updateParticipantStatus(EventHowlerParticipant participant, String status){
		goToURL(generateUpdateURL(participant.getTransactionId(), status));
		openHelper.updateStatus(participant, "");
	}
	
	public void goToURL(String url) {
		try{
			URL serverAddress = new URL(url);
			URLConnection connection = serverAddress.openConnection();
			connection.setReadTimeout(10000);
			connection.connect();	
		} catch (MalformedURLException e) {
			Log.d("MalformedURLException","Maybe checking if URL is valid.");
		} catch (ProtocolException e) {
			Log.d("ProtocolException", "Do check if request is valid.");
		} catch (IOException e) {
			Log.d("IOException", "Connection didn't successfully bind.");
		}
	}
	
	/**
	 * Generates a URL for updating an entry's status to the web application
	 * 
	 * @param transId			unique transaction id
	 * @param status			current invitation status
	 * @return					generated query URL
	 */
	public String generateUpdateURL(String transId, String status) {
		Log.d("generateUpdateURL", String.format(QUERY_URL_FORMAT, WEB_DOMAIN, PORT_NO, transId, status));
		return String.format(QUERY_URL_FORMAT, WEB_DOMAIN, PORT_NO, transId, status);
	}
}
