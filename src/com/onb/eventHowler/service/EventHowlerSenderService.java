package com.onb.eventHowler.service;

import com.onb.eventHowler.application.*;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class EventHowlerSenderService extends Service{
	 
	private static EventHowlerApplication application;
	private static EventHowlerOpenDbHelper openHelper;
	
	private static final int PARTICIPANT_COLUMN_PNUMBER  = 0, 
			PARTICIPANT_COLUMN_STATUS  = 1,
			PARTICIPANT_COLUMN_TRANSACTION_ID =2,
			PARTICIPANT_COLUMN_MESSAGE  = 3;
	
	private Cursor participantCursor;
	private BroadcastReceiver sentSMSActionReceiver;
	private BroadcastReceiver deliveredSMSActionReceiver;
	
	// DO NOT TOUCH THE VALUES, IT IS USE IN SUBSTRINGS. CAN CAUSE RUNTIME ERROR
	private String SENT_SMS_ACTION = "SENT_SMS_ACTION";
	private String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		Toast.makeText(this, "creating event Howler Sender service",
				Toast.LENGTH_SHORT).show();
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
				
		application = (EventHowlerApplication)getApplication();
		openHelper = new EventHowlerOpenDbHelper(getApplicationContext());
		
		sentSMSActionReceiver = new EventHowlerSentIntentReceiver();
		deliveredSMSActionReceiver = new EventHowlerDeliveryIntentReceiver();
		
		application.setSenderServiceFinished(false);
		
		Toast.makeText(this, "event Howler Sender service started",
				Toast.LENGTH_SHORT).show();
		
		initializeCursors();
		startSeekingForDataToBeSentToParticipant();
		
		return Service.START_NOT_STICKY;
		
	}

	private void initializeCursors() {
		participantCursor = openHelper.getAllParticipantsWithUnsentMessages();
		participantCursor.moveToFirst();
	}

	private void startSeekingForDataToBeSentToParticipant() {
		Runnable forSendSeeker = new Runnable() {
			
			public void run() {
				
				while(true){
					if(participantCursor.getCount() == 0){
						Log.d("startSeekingForDataToBeSent", "loop running if part");
						threadSleep();
						
						if(!application.hasOngoingEvent()){
							break;
						}
						refreshParticipantCursor();
					}
					else{
						Log.d("startSeekingForDataToBeSent", "loop running else part");
						
						if(participantCursor.getString(PARTICIPANT_COLUMN_STATUS).equals(MessageStatus.FOR_SEND.toString())){
							
							registerEventHowlerReceiver(deliveredSMSActionReceiver, DELIVERED_SMS_ACTION);
							registerEventHowlerReceiver(sentSMSActionReceiver, SENT_SMS_ACTION);
							
							sendSMS(participantCursor.getString(PARTICIPANT_COLUMN_PNUMBER),
									participantCursor.getString(PARTICIPANT_COLUMN_MESSAGE), 
									participantCursor.getString(PARTICIPANT_COLUMN_TRANSACTION_ID));
							Log.d("startSeekingForDataToBeSent", 
									"sending invitation to " + participantCursor.getString(PARTICIPANT_COLUMN_PNUMBER));
							
						}
						
						threadSleep();
						
						if(!participantCursor.isLast()){
							Log.d("participant number", participantCursor.getString(0));
							participantCursor.moveToNext();
						}
						else if(application.hasOngoingEvent() && participantCursor.isLast()){
							refreshParticipantCursor();
						}
						else{
							break;
						}
					}
				}
				stopSelf();
			}

			private void threadSleep() {
				try {
					Thread.sleep(2000);
				}
				catch (Exception e) {Log.d("startSeekingForDataToBeSent", "UNABLE TO SLEEP");}
			}
		};
		new Thread(forSendSeeker).start();
	}

	private void registerEventHowlerReceiver(BroadcastReceiver receiver, String intent){
		registerReceiver(receiver, 
				new IntentFilter(intent + "_" 
						+ participantCursor.getString(PARTICIPANT_COLUMN_TRANSACTION_ID)));
	}
	
	private void refreshParticipantCursor() {
		participantCursor.close();
		participantCursor = openHelper.getAllParticipantsWithUnsentMessages();
		if(participantCursor.getCount()>0){
			participantCursor.moveToFirst();
		}
	}		
	
	/**
	 * Sends SMS to a phone number
	 * 
	 * @param phoneNumber		participant phone number
	 * @param message			message to send
	 * @param transactionId		message transaction id
	 */
	private void sendSMS(String phoneNumber, String message, String transactionId) {
		
		Log.d("sendSMS", phoneNumber);
		PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0,
				new Intent(SENT_SMS_ACTION + "_" + transactionId), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(getApplicationContext(), 0,
				new Intent(DELIVERED_SMS_ACTION + "_" + transactionId), 0);
	    SmsManager sms = SmsManager.getDefault();
	    sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "event Howler sending service destroyed",
				Toast.LENGTH_SHORT).show();
		application.setSenderServiceFinished(true);
		application.resetDatabaseIfAllServicesAreFinished();
		finalizeCursorAndReceiver();
		super.onDestroy();
	}
	
	private void finalizeCursorAndReceiver() {
		participantCursor.close();
		unregisterReceiver(deliveredSMSActionReceiver);
		unregisterReceiver(sentSMSActionReceiver);
		application.setRunningLastCycle(false);
		openHelper.close();
	}
	
}