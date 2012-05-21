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
			PARTICIPANT_COLUMN_MESSAGE  = 3,
			COLUMN_MESSAGES = 1,
			INITIAL_POSITION = 0;
	
	private Cursor participantCursor;
	private Cursor messageCursor;
	private String invitationMessage;
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
		
		Toast.makeText(this, "event Howler Sender service started",
				Toast.LENGTH_SHORT).show();
		
		initializeCursors();
		startSeekingForDataToBeSentToParticipant();
		
		return Service.START_NOT_STICKY;
		
	}

	private void initializeCursors() {
		participantCursor = openHelper.getAllParticipantsWithUnsentMessages();
		participantCursor.moveToFirst();
		messageCursor = openHelper.getAllMesssages();
		messageCursor.moveToPosition(INITIAL_POSITION);
		invitationMessage = messageCursor.getString(COLUMN_MESSAGES);
	}

	private void startSeekingForDataToBeSentToParticipant() {
		Runnable forSendSeeker = new Runnable() {
			
			public void run() {
				
				while(true){
					if(participantCursor.getCount() == 0){
						Log.d("startSeekingForDataToBeSent", "loop running if part");
						threadSleep();
						
						if(!application.hasOngoingEvent()){
							participantCursor.close();
							messageCursor.close();
							openHelper.resetDatabase();
							break;
						}
						participantCursor.close();
						participantCursor = openHelper.getAllParticipantsWithUnsentMessages();
						
					}
					else{
						Log.d("startSeekingForDataToBeSent", "loop running else part");
						
						registerReceiver(deliveredSMSActionReceiver, 
								new IntentFilter(DELIVERED_SMS_ACTION + "_" 
										+ participantCursor.getString(PARTICIPANT_COLUMN_TRANSACTION_ID)));
						registerReceiver(sentSMSActionReceiver, 
								new IntentFilter(SENT_SMS_ACTION + "_" 
										+ participantCursor.getString(PARTICIPANT_COLUMN_TRANSACTION_ID)));
						
						if(participantCursor.getString(PARTICIPANT_COLUMN_STATUS).equals("FOR_SEND_INVITATION")){
							
							sendSMS(participantCursor.getString(PARTICIPANT_COLUMN_PNUMBER),
									invitationMessage, 
									participantCursor.getString(PARTICIPANT_COLUMN_TRANSACTION_ID));
							Log.d("startSeekingForDataToBeSent", 
									"sending invitation to " + participantCursor.getString(PARTICIPANT_COLUMN_PNUMBER));
							
						}
						else if(participantCursor.getString(PARTICIPANT_COLUMN_STATUS).equals("FOR_SEND_REPLY")){
							
							sendSMS(participantCursor.getString(PARTICIPANT_COLUMN_PNUMBER),
									participantCursor.getString(PARTICIPANT_COLUMN_MESSAGE), 
									participantCursor.getString(PARTICIPANT_COLUMN_TRANSACTION_ID));
							Log.d("startSeekingForDataToBeSent", 
									"sending reply to " + participantCursor.getString(PARTICIPANT_COLUMN_PNUMBER));
						}
						
						threadSleep();
						
						if(!participantCursor.isLast()){
							Log.d("participant number", participantCursor.getString(0));
							participantCursor.moveToNext();
						}
						else if(application.hasOngoingEvent() && participantCursor.isLast()){
							participantCursor.close();
							participantCursor = openHelper.getAllParticipantsWithUnsentMessages();
							participantCursor.moveToFirst();
						}
						else{
							participantCursor.close();
							messageCursor.close();
							openHelper.resetDatabase();
							break;
						}
					}
				}
				unregisterReceiver(deliveredSMSActionReceiver);
				unregisterReceiver(sentSMSActionReceiver);
				application.setRunning(false);
				stopSelf();
			}

		};
		new Thread(forSendSeeker).start();
	}

	private void threadSleep() {
		try {
			Thread.sleep(2000);
		}
		catch (Exception e) {Log.d("startSeekingForDataToBeSent", "UNABLE TO SLEEP");}
	}
		
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
		openHelper.close();
		super.onDestroy();
	}
	
}