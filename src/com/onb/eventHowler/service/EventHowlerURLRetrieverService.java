package com.onb.eventHowler.service;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.onb.eventHowler.application.EventHowlerJSONHelper;
import com.onb.eventHowler.application.EventHowlerOpenDbHelper;
import com.onb.eventHowler.domain.EventHowlerParticipant;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class EventHowlerURLRetrieverService extends Service{

	private static EventHowlerOpenDbHelper openHelper;
	private static final String QUERY_URL_FORMAT = "http://%s:%s/EventHowlerApp/query?id=%s&secretKey=%s";
	private static final String WEB_DOMAIN = "10.10.6.80";
	private static final String PORT_NO = "8080";
	
	@Override
	public void onCreate() {
		openHelper = new EventHowlerOpenDbHelper(getApplicationContext());
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Retrieves participant data from the web application 
	 * using the corresponding event ID and secret key
	 * 
	 * @param id			unique event id
	 * @param secretKey		corresponding event key
	 */
	public void retrieveAndStoreParticipantsFromIdAndKey(String id, String secretKey){
		String query = generateQueryURL(id, secretKey);
		retrieveAndStoreParticipantListFromURL(query);
	}
	
	/**
	 * Generates a URL for querying from the web application
	 * 
	 * @param id			unique event id
	 * @param secretKey		corresponding event key
	 * @return				generated query URL
	 */
	public String generateQueryURL(String id, String secretKey) {
		Log.d("generateQueryURL", String.format(QUERY_URL_FORMAT, WEB_DOMAIN, PORT_NO, id, secretKey));
		return String.format(QUERY_URL_FORMAT, WEB_DOMAIN, PORT_NO, id, secretKey);
	}

	/**
	 * Retrieves a JSON formatted String from a specified String url.
	 * Stores the participant data from the JSON formatted String into
	 * the local database.
	 * 
	 * @param url location of webpage to retrieve and store data from
	 */
	public void retrieveAndStoreParticipantListFromURL(String url)
	{
		List<JSONArray> list = EventHowlerJSONHelper.extractFromURL(url);
		
		for(JSONArray jArray: list) {
			for(int i = 0; i < jArray.length(); i++){
				try {
					JSONObject jObject = jArray.getJSONObject(i);
					storeAsParticipant(jObject);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Creates an EventHowlerParticipant object from a JSONObject and
	 * stores it as a row in the database.
	 * 
	 * @param jObject		JSONObject to be stored as an EventHowlerParticipant
	 * @throws JSONException
	 */
	public void storeAsParticipant(JSONObject jObject) throws JSONException
	{
		EventHowlerParticipant participant = EventHowlerJSONHelper.convertJSONObjectToParticipant(jObject);
		if(openHelper.checkNumberIfExist(participant.getPhoneNumber())) {
			openHelper.updateStatus(participant, "");
		}
		else {
			openHelper.insertParticipant(participant);	
		}
	}
}
