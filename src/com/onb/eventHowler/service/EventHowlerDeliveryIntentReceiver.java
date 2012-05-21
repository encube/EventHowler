package com.onb.eventHowler.service;

import com.onb.eventHowler.application.EventHowlerOpenDbHelper;
import com.onb.eventHowler.application.MessageStatus;
import com.onb.eventHowler.domain.EventHowlerParticipant;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class EventHowlerDeliveryIntentReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		EventHowlerOpenDbHelper openHelper = new EventHowlerOpenDbHelper(context);

		String transactionId =intent.getAction().toString().substring(21); // paki-private static final yung 21 please :D
		String phoneNumber = openHelper.findNumberWithTransactionId(transactionId);
		switch (getResultCode()) {
		case Activity.RESULT_OK:
			Log.d("delevery intent", "delivery intent received, successful");
			openHelper.updateStatus(new EventHowlerParticipant(
					phoneNumber, transactionId, MessageStatus.SUCCESSFUL_DELIVERY.toString()), "");
			break;
		case Activity.RESULT_CANCELED:
			Log.d("delivery intent", "delivery intent received, unsuccessful");
			openHelper.updateStatus(new EventHowlerParticipant(
					phoneNumber, transactionId, MessageStatus.UNSUCCESSFUL_DELIVERY.toString()), "");
			break;
		}
		openHelper.close();
	}

}
