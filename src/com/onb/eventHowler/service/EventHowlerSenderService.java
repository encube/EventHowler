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
			COLUMN_STATUS  = 1,
			COLUMN_REPLY  = 2,
			COLUMN_MESSAGES = 1,
			INITIAL_POSITION = 0;
	
	private Intent sentIntent = new Intent("SENT_SMS_ACTION");;
	private PendingIntent sentPI;
	
	private Cursor participantCursor;
	private Cursor messageCursor;
	private String invitationMessage;
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
		
		EventHowlerURLRetrieverService retriever = new EventHowlerURLRetrieverService();
		String id = "2";
		String secretKey = "q";
		retriever.retrieveAndStoreParticipantsFromIdAndKey(id, secretKey);
		
		/*/test data
		openHelper.insertParticipant(new EventHowlerParticipant("15555215556", "2134", "FOR_SEND_INVITATION"));
		openHelper.insertParticipant(new EventHowlerParticipant("15555215558", "3314", "FOR_SEND_INVITATION"));
		openHelper.updateStatus(new EventHowlerParticipant("15555215558", "6839", "FOR_SEND_REPLY"), "thank you for attending you confirmation code is 35dh2h");
		openHelper.insertParticipant(new EventHowlerParticipant("15555215560", "9863", "FOR_SEND_INVITATION"));
		openHelper.insertParticipant(new EventHowlerParticipant("15555215562", "5324", "FOR_SEND_INVITATION"));*/
		openHelper.populateMessages("Hello fella, i would like to invite for a pack party ");
		//test data 
		
		Toast.makeText(this, "event Howler Sender service started",
				Toast.LENGTH_SHORT).show();
		application = (EventHowlerApplication)getApplication();
		
		participantCursor = openHelper.getAllParticipantsWithUnsentInvites();
		messageCursor = openHelper.getAllMesssages();
		messageCursor.moveToPosition(INITIAL_POSITION);
		invitationMessage = messageCursor.getString(COLUMN_MESSAGES);
		
		startSeekingForDataToBeSent();
		
		return Service.START_NOT_STICKY;
	}

	private void startSeekingForDataToBeSent() {
		Runnable forSendSeeker = new Runnable() {
			
			public void run() {
				int currentPosition = 0;
				
				while(true){
					if(participantCursor.getCount() == 0){
						Log.d("in loop", "running if part");
						try {
							Thread.sleep(2000);
						}
						catch (Exception e) {Log.d("sender service", "nag-pupuyat, ayaw magsleep");}
						
						if(!application.hasOngoingEvent()){
							participantCursor.close();
							messageCursor.close();
							openHelper.resetDatabase();
							break;
						}
						participantCursor.close();
						participantCursor = openHelper.getAllParticipantsWithUnsentInvites();
						
					}
					else{
						Log.d("in loop", "running else part");
						participantCursor.moveToPosition(currentPosition);
						
						if(participantCursor.getString(COLUMN_STATUS).equals("FOR_SEND_INVITATION")){
							sendSMS(participantCursor.getString(COLUMN_PNUMBER), invitationMessage);
							
							registerReceiver(sentSMSActionReceiver, new IntentFilter("SENT_SMS_ACTION"));
						}
						else if(participantCursor.getString(COLUMN_STATUS).equals("FOR_SEND_REPLY")){
							sendSMS(participantCursor.getString(COLUMN_PNUMBER), participantCursor.getString(COLUMN_REPLY));
							
							registerReceiver(sentSMSActionReceiver, new IntentFilter("SENT_SMS_ACTION"));
						}
						
						try {
							Thread.sleep(2000);
						}
						catch (Exception e) {Log.d("sender service", "nag-pupuyat, ayaw magsleep");}
						
						if(currentPosition+1<participantCursor.getCount()){
							currentPosition++;
						}
						else if(application.hasOngoingEvent()){
							currentPosition=0;
							participantCursor.close();
							participantCursor = openHelper.getAllParticipantsWithUnsentInvites();
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
		String phoneNumber = participantCursor.getString(COLUMN_PNUMBER);
		String transactionId = openHelper.getTransactionIdOfPhoneNumber(phoneNumber);
		switch(resultCode){
		case 1:
			Log.d("BR assistant", "code 1, RESULT_OK");
			//TODO need to keep track of the transaction ID
			openHelper.updateStatus(new EventHowlerParticipant(
					phoneNumber, transactionId, "SENT"), "");
			unregisterReceiver(sentSMSActionReceiver);
			break;
		case 2:
			Log.d("BR assistant", "code 2");
			openHelper.updateStatus(new EventHowlerParticipant(
					phoneNumber, "", "RESULT_ERROR_GENERIC_FAILURE"), "");
			unregisterReceiver(sentSMSActionReceiver);
			break;
		case 3:
			Log.d("BR assistant", "code 3");
			openHelper.updateStatus(new EventHowlerParticipant(
					phoneNumber, "", "RESULT_ERROR_RADIO_OFF"), "");
			unregisterReceiver(sentSMSActionReceiver);
			break;
		case 4:
			Log.d("BR assistant", "code 4");
			openHelper.updateStatus(new EventHowlerParticipant(
					phoneNumber, "", "RESULT_ERROR_NULL_PDU"), "");
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
		openHelper.close();
		super.onDestroy();
	}
	
}