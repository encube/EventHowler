package com.onb.eventHowler.service;

import com.onb.eventHowler.application.EventHowlerApplication;
import com.onb.eventHowler.application.EventHowlerOpenDbHelper;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class EventHowlerWebUpdateService extends Service {

	private static EventHowlerOpenDbHelper openHelper;
	private static final String QUERY_URL_FORMAT = "http://%s:%s/EventHowlerApp/query?transId=%s&status=%s";
	private static final String WEB_DOMAIN = "10.10.6.83";
	private static final String PORT_NO = "8080";
	private static final String FOR_SEND_REPLY = "FOR_SEND_REPLY";
	private EventHowlerApplication application;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Generates a URL for updating an entry's status to the web application
	 * 
	 * @param transId			unique transaction id
	 * @param status			current invitation status
	 * @return					generated query URL
	 */
	public String generateUpdateURL(String transId, String status) {
		Log.d("generateUpdateURL", String.format(QUERY_URL_FORMAT, WEB_DOMAIN, PORT_NO, transId, status));
		return String.format(QUERY_URL_FORMAT, WEB_DOMAIN, PORT_NO, transId, status);
	}
}
