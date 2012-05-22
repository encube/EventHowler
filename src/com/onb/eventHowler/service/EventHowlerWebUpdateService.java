package com.onb.eventHowler.service;

import com.onb.eventHowler.application.EventHowlerApplication;
import com.onb.eventHowler.application.EventHowlerOpenDbHelper;
import com.onb.eventHowler.application.EventHowlerURLHelper;
import com.onb.eventHowler.application.MessageStatus;
import com.onb.eventHowler.application.ServiceStatus;
import com.onb.eventHowler.domain.EventHowlerParticipant;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class EventHowlerWebUpdateService extends Service {

	private static EventHowlerOpenDbHelper openHelper;
	private static final String UPDATE_URL_FORMAT = "http://%s:%s/EventHowlerApp/query?transId=%s&status=%s&id=%s&secretKey=%s";
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
		
		Log.d("onStartCommand", "starting Web Update");
		
		openHelper = new EventHowlerOpenDbHelper(getApplicationContext());
		application = (EventHowlerApplication)(getApplication());
		
		startUpdating();
		
		return Service.START_NOT_STICKY;
	}
	
	/**
	 * Start updating participant status to web application.
	 */
	public void startUpdating()
	{		
		Thread queryThread = new Thread( new Runnable() {
			public void run(){						
				while(application.hasOngoingEvent()){
					
					if(!participantIsEmpty() && application.getEventHowlerURLRetrieverServiceStatus().equals(ServiceStatus.RUNNING)) {
						updateWebApp();
					}
					
					threadSleep(UPDATE_INTERVAL);
				}
				stopSelf();
			}

			//TODO transfer to openDBHelper
			private boolean participantIsEmpty() {
				Cursor participants = openHelper.getAllParticipants();
				boolean result = (participants.getCount() == 0);
				participants.close();
				return result;
			}			

			private void threadSleep(long msec) {
				try {
					Thread.sleep(msec);
				}
				catch (Exception e) {Log.d("startUpdating", "UNABLE TO SLEEP");}
			}
		});
		
		queryThread.start();
	}
	
	
	
	
	/**
	 * Update participants' message sending status in Web application
	 */
	public void updateWebApp(){
		
		Cursor participants = openHelper.getAllParticipantsWithMessageSendingAttempts();
		participants.moveToFirst();
		do {
			
			if(participants.getCount() == 0) {
				break;
			}
			
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
		participants.close();
	}
	
	/**
	 * Update a single participants message sending status to web application
	 * @param participant	participant to be updated
	 * @param status		current message sending status, either SUCCESS or FAILED
	 */
	public void updateParticipantStatus(EventHowlerParticipant participant, String status){
		EventHowlerURLHelper.goToURL(generateUpdateURL(participant.getTransactionId(), status));
		participant.setStatus(MessageStatus.STATUS_REPORTED.toString());
		openHelper.updateStatus(participant, "");
	}
	
	/**
	 * Generates a URL for updating an entry's status to the web application
	 * 
	 * @param transId	unique transaction id
	 * @param status	current invitation status
	 * @return			generated query URL
	 */
	public String generateUpdateURL(String transId, String status) {
		Log.d("generateUpdateURL", String.format(UPDATE_URL_FORMAT, WEB_DOMAIN, PORT_NO, transId, status,application.getEventId(), application.getSecretKey()));
		return String.format(UPDATE_URL_FORMAT, WEB_DOMAIN, PORT_NO, transId, status, application.getEventId(), application.getSecretKey());
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "event Howler query service destroyed",
				Toast.LENGTH_SHORT).show();
		openHelper.close();
		super.onDestroy();
	}
}
