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

public class EventHowlerWebReplyService extends Service {

	private static EventHowlerOpenDbHelper openHelper;
	private static final String REPLY_URL_FORMAT = "http://%s:%s/EventHowlerApp/query?number=%s&message=%s&id=%s&secretKey=%s";
	private static final String WEB_DOMAIN = "10.10.6.83";
	private static final String PORT_NO = "8080";
	private static final long REPLY_INTERVAL = 10000;
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
		
		startReplying();
		
		return Service.START_NOT_STICKY;
	}
	
	/**
	 * Start notifying web application for replies.
	 */
	public void startReplying()
	{		
		Thread replyThread = new Thread( new Runnable() {
			public void run(){						
				while(application.hasOngoingEvent()){
					Log.d("while loop of web reply", "gugugugu");
					
					if(!participantIsEmpty() && application.getEventHowlerURLRetrieverServiceStatus().equals(ServiceStatus.RUNNING)) {
						fetchRepliesFromWebApp();
						Log.d("if part web reply", "gugugugu");
					}
					
					threadSleep(REPLY_INTERVAL);
				}
			}

			//TODO transfer to OpenDBHelper
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
				catch (Exception e) {Log.d("startReplying", "UNABLE TO SLEEP");}
			}
		});
		
		replyThread.start();
	}
		
	/**
	 * Relay SMS replies to web application and notify for response
	 */
	public void fetchRepliesFromWebApp(){
		
		Cursor participants = openHelper.getAllParticipantsWithReplies();
		participants.moveToFirst();
		do {
			if(participants.getCount() == 0) {
				break;
			}
			
			EventHowlerParticipant participant = EventHowlerOpenDbHelper.getParticipantFromCursor(participants);
			String status = participant.getStatus();
			
			String message = EventHowlerOpenDbHelper.getMessageFromCursor(participants);
			
			if(status.equalsIgnoreCase(MessageStatus.REPLY_RECEIVED.toString())) {
				prepareParticipantReply(participant, message);
			}
		} while (participants.moveToNext());
		participants.close();
	}
	
	/**
	 * Relay a single participant's RSVP 
	 * 
	 * @param participant 	participant who replied to the invitation
	 * @param message		participant's current reply
	 */
	public void prepareParticipantReply(EventHowlerParticipant participant, String message){
		EventHowlerURLHelper.goToURL(generateReplyURL(participant.getPhoneNumber(), message));
		participant.setStatus(MessageStatus.FOR_REPLY.toString());
		openHelper.updateStatus(participant, message);
	}
	
	/**
	 * Generates a URL for relaying an entry's reply to the web application
	 * 
	 * @param phoneNumber	unique transaction id
	 * @param replyMessage	current invitation status
	 * @return				generated query URL
	 */
	public String generateReplyURL(String phoneNumber, String replyMessage) {
		Log.d("generateReplyURL", String.format(REPLY_URL_FORMAT, WEB_DOMAIN, PORT_NO, phoneNumber.replace("+", ""), replyMessage, application.getEventId(), application.getSecretKey()));
		return String.format(REPLY_URL_FORMAT, WEB_DOMAIN, PORT_NO, phoneNumber, replyMessage, application.getEventId(), application.getSecretKey());
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "event Howler query service destroyed",
				Toast.LENGTH_SHORT).show();
		openHelper.close();
		super.onDestroy();
	}
}
