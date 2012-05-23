package com.onb.eventHowler.service;

import com.onb.eventHowler.application.EventHowlerApplication;
import com.onb.eventHowler.application.EventHowlerOpenDbHelper;
import com.onb.eventHowler.application.EventHowlerURLHelper;
import com.onb.eventHowler.application.MessageStatus;
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
	private static final long REPLY_INTERVAL = 10000;
	private EventHowlerApplication application;
	private boolean done;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.d("onStartCommand", "starting Web Query (former URL Retriever)");
		
		openHelper = new EventHowlerOpenDbHelper(getApplicationContext());
		application = (EventHowlerApplication)(getApplication());
		application.setWebReplyServiceFinished(false);
		
		startReplying();
		
		return Service.START_NOT_STICKY;
	}
	
	/**
	 * Start notifying web application for replies.
	 */
	private void startReplying()
	{		
		done = false;
		Thread replyThread = new Thread( new Runnable() {
			public void run(){						
				while(!done){
					Log.d("startReplying", "while loop of web reply");
					
					if(!openHelper.isParticipantEmpty()) {
						fetchRepliesFromWebApp();
						Log.d("startReplying", "if part web reply");
					}
					
					threadSleep(REPLY_INTERVAL);
				}
				stopSelf();
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
	private void fetchRepliesFromWebApp(){
		
		Cursor participants = openHelper.getAllParticipantsWithReplies();
		boolean cursorEmpty = !participants.moveToFirst(); // moveToFirst() returns false of empty
		do {			
			if(cursorEmpty) {
				Log.d("REPLIES EMPTY", "cursor with all participant replies is empty");
				break;
			}
			
			EventHowlerParticipant participant = EventHowlerOpenDbHelper.getParticipantFromCursor(participants);
			String status = participant.getStatus();
			
			String message = EventHowlerOpenDbHelper.getMessageFromCursor(participants);
			
			if(status.equalsIgnoreCase(MessageStatus.REPLY_RECEIVED.toString())) {
				prepareParticipantReply(participant, message);
			}
		} while (participants.moveToNext());
		
		if(participants.getCount() == 0 && !application.hasOngoingEvent()){
			Log.d("hasOngoing", "false");
			done = true;
		}
		Log.d("reply service done ", "reply: "+done );
		
		participants.close();
	}
	
	/**
	 * Relay a single participant's RSVP 
	 * 
	 * @param participant 	participant who replied to the invitation
	 * @param message		participant's current reply
	 */
	private void prepareParticipantReply(EventHowlerParticipant participant, String message){
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
		Log.d("generateReplyURL", String.format(REPLY_URL_FORMAT, 
													application.DOMAIN, application.PORT, 
													phoneNumber.replace("+", ""), replyMessage, 
													application.getEventId(), application.getSecretKey()));
		return String.format(REPLY_URL_FORMAT, 
								application.DOMAIN, application.PORT, 
								phoneNumber, replyMessage, 
								application.getEventId(), application.getSecretKey());
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "event Howler query service destroyed",
				Toast.LENGTH_SHORT).show();
		application.setWebReplyServiceFinished(true);
		application.resetDatabaseIfAllServicesAreFinished();
		openHelper.close();
		super.onDestroy();
	}
}
