package com.onb.eventHowler.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Hashtable;

import com.onb.eventHowler.application.EventHowlerApplication;
import com.onb.eventHowler.application.EventHowlerOpenDbHelper;
import com.onb.eventHowler.domain.EventHowlerParticipant;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

public class EventHowlerSendingFailedInformerService extends android.app.Service {

	EventHowlerOpenDbHelper database;
	Cursor replyStatusCursor;
	Hashtable<Integer, String> confirmedParticipantsInTable;

	private static final String QUERY_URL_FORMAT = "http://%s:%s/EventHowlerApp/query?transId=%s&status=%s";
	private static final String WEB_DOMAIN = "10.10.6.83";
	private static final String PORT_NO = "8080";

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
		startSendingReplyIfFailed();
		return Service.START_NOT_STICKY;
	}

	public void startSendingReplyIfFailed() {
		Runnable startSendingReplyIfFailedRunnable = new Runnable() {

			public void run() {
				HttpURLConnection connection = null;
				URL serverAddress = null;
				boolean notFinishedProcessedDatabase = true;
				while (notFinishedProcessedDatabase
						|| EventHowlerApplication.hasOngoingEventGlobal()) {
					threadSleep(500);
					replyStatusCursor = database.getAllReplyStatus();
					replyStatusCursor.moveToFirst();
					do {
						String id = replyStatusCursor.getString(2);
						String status = replyStatusCursor.getString(1);
						if (isError(status)) {
							try {
								serverAddress = new URL(String.format(QUERY_URL_FORMAT, WEB_DOMAIN, PORT_NO, id, "FAILED"));
								connection = (HttpURLConnection)serverAddress
										.openConnection();
								connection.setRequestMethod("GET");
								connection.setDoOutput(true);
								connection.setReadTimeout(10000);
								connection.connect();
								database.updateStatus(new EventHowlerParticipant(replyStatusCursor.getString(0), status, id), "FOR_SEND_INVITATION");
							} catch (MalformedURLException e) {
								Log.d("MalformedURLException","Maybe checking if URL is valid.");
							} catch (ProtocolException e) {
								Log.d("ProtocolException", "Do check if request is valid.");
							} catch (IOException e) {
								Log.d("IOException", "Connection didn't successfully bind.");
							}
						}
					} while (notFinishedProcessedDatabase = replyStatusCursor
							.moveToNext());
				}
			}

			private void threadSleep(int msec) {
				try {
					Thread.sleep(msec);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			private boolean isError(String string) {
				if (string.contains("RESULT_ERROR_")) {
					return true;
				} else {
					return false;
				}
			}
		};
		Thread startThreadSendingReply = new Thread(startSendingReplyIfFailedRunnable);
		startThreadSendingReply.start();
	}

}
