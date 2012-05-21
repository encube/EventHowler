package com.onb.eventHowler.service;

import com.onb.eventHowler.application.EventHowlerOpenDbHelper;
import com.onb.eventHowler.domain.EventHowlerParticipant;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class EventHowlerSentIntentReceiver extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent intent) {
		EventHowlerOpenDbHelper openHelper = new EventHowlerOpenDbHelper(context);
		
		String transactionId = intent.getAction().toString().substring(16);
		String phoneNumber = openHelper.findNumberWithTransactionId(transactionId);
		switch (getResultCode()){
		case Activity.RESULT_OK:
			openHelper.updateStatus(new EventHowlerParticipant(
					phoneNumber, transactionId, "SENT"), "");
			break;
		case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			openHelper.updateStatus(new EventHowlerParticipant(
					phoneNumber, transactionId, "RESULT_ERROR_GENERIC_FAILURE"), "");
			break;
		case SmsManager.RESULT_ERROR_RADIO_OFF:
			openHelper.updateStatus(new EventHowlerParticipant(
					phoneNumber, transactionId, "RESULT_ERROR_RADIO_OFF"), "");
			break;
		case SmsManager.RESULT_ERROR_NULL_PDU:
			openHelper.updateStatus(new EventHowlerParticipant(
					phoneNumber, transactionId, "RESULT_ERROR_NULL_PDU"), "");
			break;
		}
		openHelper.close();
	}

}
