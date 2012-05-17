package com.onb.eventHowler.service;

import java.util.Hashtable;

import com.onb.eventHowler.application.EventHowlerOpenDbHelper;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

public class EventHowlerStatusInformerService extends android.app.Service {

	EventHowlerOpenDbHelper database;
	Cursor replyStatusCursor;
	Hashtable<Integer, String> confirmedParticipantsInTable;
	
	@Override
	public android.os.IBinder onBind(android.content.Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		Toast.makeText(this, "Informing server for SMS status...",
				Toast.LENGTH_SHORT).show();
		super.onCreate();
		
	}
	
	@Override 
	public int onStartCommand(Intent intent, int flags, int startId) {
		database = new EventHowlerOpenDbHelper(this);
		startSendingReply();
		return Service.START_NOT_STICKY;
	}
	
	public void startSendingReply() {
		Runnable startSendingReplyRunnable = new Runnable() {
			@Override
			public void run() {
				while (true) {
					threadSleep(500);
					replyStatusCursor = database.getAllReplyStatus();
					replyStatusCursor.moveToFirst();
					do {
						int id = replyStatusCursor.getInt(0);
						String status = replyStatusCursor.getString(1);
						String reply = replyStatusCursor.getString(2);
						if (isAnswer(reply)) {
							confirmedParticipantsInTable.put(id, status);
						}
					} while (replyStatusCursor.moveToNext());
					// TODO send reply status
				}
			}

			private void threadSleep(int msec) {
				try {
					Thread.sleep(msec);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			private boolean isAnswer(String string) {
				if (string.toLowerCase().equals("yes")) {
					return false;
				} else if (string.toLowerCase().equals("no")) {
					return false;
				} else {
					return true;
				}
			}
		};
		Thread startThreadSendingReply = new Thread(startSendingReplyRunnable);
		startThreadSendingReply.start();
	}

}
