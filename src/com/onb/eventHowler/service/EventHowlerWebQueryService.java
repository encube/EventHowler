package com.onb.eventHowler.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.onb.eventHowler.application.EventHowlerApplication;
import com.onb.eventHowler.application.EventHowlerJSONHelper;
import com.onb.eventHowler.application.EventHowlerOpenDbHelper;
import com.onb.eventHowler.application.ServiceStatus;
import com.onb.eventHowler.domain.EventHowlerParticipant;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class EventHowlerWebQueryService extends Service{

	private static EventHowlerOpenDbHelper openHelper;
	private static final String QUERY_URL_FORMAT = "http://%s:%s/EventHowlerApp/query?id=%s&secretKey=%s";
	private static final long QUERY_INTERVAL = 10000;
	private EventHowlerApplication application;
			
	@Override
	public void onCreate() {
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.d("onStartCommand", "starting Web Query (former URL Retriever)");
		
		openHelper = new EventHowlerOpenDbHelper(getApplicationContext());
		application = (EventHowlerApplication)(getApplication());
		
		String id = application.getEventId();
		String secretKey = application.getSecretKey();

		startQuerying(id,secretKey);
		return Service.START_NOT_STICKY;
	}
	
	/**
	 * Start querying invitations from Web application 
	 * using unique event id and corresponding secret key.
	 * 
	 * @param id			event id
	 * @param secretKey		corresponding secret key
	 */
	private void startQuerying(final String id, final String secretKey)
	{		
		Thread queryThread = new Thread( new Runnable() {
			public void run(){				
				boolean serviceStarted = false;
		
				while(application.hasOngoingEvent()){
					retrieveAndStoreEventInfoFromIdAndKey(id, secretKey);
					
					if(!openHelper.isParticipantEmpty() && !serviceStarted) {
						Log.d("not empty participant", "pass here");
						startRunning();
						serviceStarted = true;
					}
					
					threadSleep(QUERY_INTERVAL);
				}
				stopSelf();
			}
			
			private void threadSleep(long msec) {
				try {
					Thread.sleep(msec);
				}
				catch (Exception e) {Log.d("startQuerying", "UNABLE TO SLEEP");}
			}
		});
		
		queryThread.start();
	}	
	
	/**
	 * Retrieves participant data from the web application 
	 * using the corresponding event ID and secret key
	 * 
	 * @param id			unique event id
	 * @param secretKey		corresponding event key
	 */
	private void retrieveAndStoreEventInfoFromIdAndKey(String id, String secretKey) {
		String query = generateQueryURL(id, secretKey);
		retrieveAndStoreEventInfoFromURL(query);
	}
	
	/**
	 * Generates a URL for querying from the web application
	 * 
	 * @param id			unique event id
	 * @param secretKey		corresponding event key
	 * @return				generated query URL
	 */
	public String generateQueryURL(String id, String secretKey) {
		Log.d("generateQueryURL", String.format(QUERY_URL_FORMAT, 
												application.DOMAIN, application.PORT, 
												id, secretKey));
		
		return String.format(QUERY_URL_FORMAT, 
								application.DOMAIN, application.PORT, 
								id, secretKey);
	}

	/**
	 * Retrieves a JSON formatted String from a specified URL.
	 * Stores the participant data from the JSON formatted String into
	 * the local database.
	 * 
	 * @param url location of web page to retrieve and store data from
	 */
	private void retrieveAndStoreEventInfoFromURL(String url) {
		List<JSONObject> list;
		EventHowlerApplication app = (EventHowlerApplication)getApplication();
		try {
			list = EventHowlerJSONHelper.extractFromURL(url);
			for(JSONObject entry: list) {
				extractParticipants(entry);
			}
		} catch (MalformedURLException e1) {
			// TODO 
			this.stopRunning();
			app.stopEvent();
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO
			this.stopRunning();
			app.stopEvent();
			e1.printStackTrace();
		}
		
	}
	
	private void stopRunning() {
		sendBroadcast(new Intent("FORCE_STOP"));
		application.setEventHowlerURLRetrieverServiceStatus(ServiceStatus.STOP);
	}
	
	private void startRunning() {
		Log.d("make it run", "na change ko na");
		application.setEventHowlerURLRetrieverServiceStatus(ServiceStatus.RUNNING);
	}

	/**
	 * Extracts and stores all participant data from a single JSONObject 
	 * representing a JSON formatted query result.
	 * 
	 * @param entry				JSONObject representing a single query result
	 * @throws JSONException
	 */
	private void extractParticipants(JSONObject entry) {
		try{
			JSONArray participants = entry.getJSONArray(EventHowlerJSONHelper.ATTRIBUTE_CONTENT);
			
			for(int i = 0; i < participants.length(); i++) {
				try {
					JSONObject participant = participants.getJSONObject(i);
					String message = participant.getString(EventHowlerJSONHelper.ATTRIBUTE_MESSAGE);
					storeAsParticipant(participant, message);
				} catch (JSONException e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		catch (JSONException e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Creates an EventHowlerParticipant from a JSONObject and
	 * stores it in the database.
	 * 
	 * @param jObject		JSONObject to be stored as an EventHowlerParticipant
	 * @throws JSONException
	 */
	private void storeAsParticipant(JSONObject jObject, String message) throws JSONException {
		EventHowlerParticipant participant = EventHowlerJSONHelper.convertJSONObjectToParticipant(jObject);
		
		Log.d("STORING PARTICIPANT", "Phone: " + participant.getPhoneNumber() 
				+ "Trans_id: " + participant.getTransactionId());
		
		if(openHelper.checkNumberIfExist(participant.getPhoneNumber())) {
			openHelper.updateStatus(participant, message);
		}
		else {
			openHelper.insertParticipant(participant, message);	
		}
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "event Howler query service destroyed",
				Toast.LENGTH_SHORT).show();
		openHelper.close();
		super.onDestroy();
	}
}
