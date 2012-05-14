package com.onb.eventHowler.service;

import com.onb.eventHowler.application.*;
import com.onb.eventHowler.domain.EventHowlerParticipant;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
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
	
	private static final int COLUMN_PNUMBER  = 0, 
			COLUMN_NAME  = 1, 
			COLUMN_STATUS  = 2,
			COLUMN_MESSAGES = 1,
			INITIAL_POSITION = 0;
	
	private Intent sentIntent = new Intent("SENT_SMS_ACTION");;
	private PendingIntent sentPI;
	
	private Cursor participantCursor;
	private Cursor messageCursor;
	private String invitationMessage;
	private String replyMessage;
	private String confirmationCode;
	private String negationCode;
	private BroadcastReceiver sentSMSActionReceiver;

	
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
				
		openHelper = new EventHowlerOpenDbHelper(getApplicationContext());
		sentSMSActionReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context _context, Intent _intent){
				switch (getResultCode()){
				case Activity.RESULT_OK:
					broadcastReceiverAssistant(1);
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					broadcastReceiverAssistant(2);
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					broadcastReceiverAssistant(3);
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					broadcastReceiverAssistant(4);
					break;
				}
			}
		};
		sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0, sentIntent, 0);
		
		//test data
		openHelper.insertParticipant(new EventHowlerParticipant("dexie", "5558", "FOR_SEND"));
		openHelper.insertParticipant(new EventHowlerParticipant("novo", "5560", "FOR_SEND"));
		openHelper.insertParticipant(new EventHowlerParticipant("nino", "5562", "FOR_SEND"));
		openHelper.insertParticipant(new EventHowlerParticipant("naga", "5556", "FOR_SEND"));
		openHelper.populateMessages("Hello fella, i would like to invite for a pack party ", 
				"thank you, we receive your reply ", "yes", "no");
		//test data
		
		Toast.makeText(this, "event Howler Sender service started",
				Toast.LENGTH_SHORT).show();
		application = (EventHowlerApplication)getApplication();
		
		participantCursor = openHelper.getAllParticipantToBeSend();
		messageCursor = openHelper.getAllMesssages();
		messageCursor.moveToPosition(INITIAL_POSITION);
		invitationMessage = messageCursor.getString(COLUMN_MESSAGES);
		messageCursor.moveToNext();
		replyMessage = messageCursor.getString(COLUMN_MESSAGES);
		
		startSeekingForDataToBeSend();
		
		return Service.START_NOT_STICKY;
	}

	private void startSeekingForDataToBeSend() {
		Runnable forSendSeeker = new Runnable() {
			
			@Override
			public void run() {
				int currentPosition = 0;
				
				while(true){
					if(participantCursor.getCount() == 0){
						Log.d("in loop", "running if part");
						try {
							Thread.sleep(2000);
						}
						catch (Exception e) {Log.d("sender service", "nag-pupuyat, ayaw magsleep");}
						
						if(!application.hasOnGoingEvent()){
							participantCursor.close();
							messageCursor.close();
							openHelper.resetDatabase();
							break;
						}
						participantCursor.close();
						participantCursor = openHelper.getAllParticipantToBeSend();
						
					}
					else{
						Log.d("in loop", "running else part");
						participantCursor.moveToPosition(currentPosition);
						
						if(participantCursor.getString(COLUMN_STATUS).equals("FOR_SEND")){
							sendSMS(participantCursor.getString(COLUMN_PNUMBER),
									invitationMessage + participantCursor.getString(COLUMN_NAME));
							
							registerReceiver(sentSMSActionReceiver, new IntentFilter("SENT_SMS_ACTION"));
						}
						else{
							sendSMS(participantCursor.getString(COLUMN_PNUMBER),
									replyMessage + participantCursor.getString(COLUMN_STATUS));
							
							registerReceiver(sentSMSActionReceiver, new IntentFilter("SENT_SMS_ACTION"));
						}
						
						try {
							Thread.sleep(2000);
						}
						catch (Exception e) {Log.d("sender service", "nag-pupuyat, ayaw magsleep");}
						
						if(currentPosition+1<participantCursor.getCount()){
							currentPosition++;
						}
						else if(application.hasOnGoingEvent()){
							currentPosition=0;
							participantCursor.close();
							participantCursor = openHelper.getAllParticipantToBeSend();
						}
						else{
							messageCursor.close();
							openHelper.resetDatabase();
							break;
						}
					}
				}
				stopSelf();
			}
		};
		new Thread(forSendSeeker).start();
	}
	
	private void broadcastReceiverAssistant(int resultCode) {
		switch(resultCode){
		case 1:
			Log.d("BR assistant", "code 1, RESULT_OK");
			openHelper.updateStatus(new EventHowlerParticipant(participantCursor.getString(COLUMN_NAME),
					participantCursor.getString(COLUMN_PNUMBER),
					"SENT"));
			unregisterReceiver(sentSMSActionReceiver);
			break;
		case 2:
			Log.d("BR assistant", "code 2");
			unregisterReceiver(sentSMSActionReceiver);
			break;
		case 3:
			Log.d("BR assistant", "code 3");
			unregisterReceiver(sentSMSActionReceiver);
			break;
		case 4:
			Log.d("BR assistant", "code 4");
			unregisterReceiver(sentSMSActionReceiver);
			break;
		}
	}
	
	private void sendSMS(String phoneNumber, String message) {                
	    SmsManager sms = SmsManager.getDefault();
	    sms.sendTextMessage(phoneNumber, null, message, sentPI, null);     
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "event Howler sending service destroyed",
				Toast.LENGTH_SHORT).show();
		super.onDestroy();
	}
	
}