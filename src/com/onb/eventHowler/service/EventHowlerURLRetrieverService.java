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
	private static final String WEB_DOMAIN = "10.10.6.83";
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
	public void retrieveAndStoreEventInfoFromIdAndKey(String id, String secretKey){
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
		Log.d("generateQueryURL", String.format(QUERY_URL_FORMAT, WEB_DOMAIN, PORT_NO, id, secretKey));
		return String.format(QUERY_URL_FORMAT, WEB_DOMAIN, PORT_NO, id, secretKey);
	}

	/**
	 * Retrieves a JSON formatted String from a specified URL.
	 * Stores the participant data from the JSON formatted String into
	 * the local database.
	 * 
	 * @param url location of web page to retrieve and store data from
	 */
	public void retrieveAndStoreEventInfoFromURL(String url)
	{
		List<JSONObject> list = EventHowlerJSONHelper.extractFromURL(url);
		
		
		for(JSONObject entry: list) {
			try {
				extractParticipants(entry);
				extractMessage(entry);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
	
	/**
	 * Extracts and stores all participant data from a single JSONObject 
	 * representing a JSON formatted query result.
	 * 
	 * @param entry				JSONObject representing a single query result
	 * @throws JSONException
	 */
	public void extractParticipants(JSONObject entry) throws JSONException {
		JSONArray participants = entry.getJSONArray(EventHowlerJSONHelper.ATTRIBUTE_CONTENT);
		
		for(int i = 0; i < participants.length(); i++) {
			JSONObject participant = participants.getJSONObject(i);
			storeAsParticipant(participant);
		}
	}
	
	/**
	 * Extracts and stores message from a single JSONObject 
	 * representing a JSON formatted query.
	 * 
	 * @param entry				JSONObject representing a single query result
	 * @throws JSONException
	 */
	public void extractMessage(JSONObject entry) throws JSONException {
		String message = entry.getString(EventHowlerJSONHelper.ATTRIBUTE_MESSAGE);
		openHelper.populateMessages(message);
	}
	
	/**
	 * Creates an EventHowlerParticipant from a JSONObject and
	 * stores it in the database.
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
