package com.onb.eventHowler.application;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.onb.eventHowler.domain.EventHowlerParticipant;

public class EventHowlerJSONHelper {

	public static final String ATTRIBUTE_CONTENT = "contacts";
	public static final int ATTRIBUTE_PHONE_NUMBER = 0;//"phoneNumber";
	public static final int ATTRIBUTE_TRANS_ID = 1;//"transactionId";
	public static final String ATTRIBUTE_MESSAGE = "invitationMessage";
	private static final String STATUS_FOR_SENDING = "FOR_SEND_INVITATION";
	
	/**
	 * @param url	URL to the JSON-formatted web page
	 * @return		list containing JSONArrays generated from the JSON formatted file
	 */
	public static List<JSONObject> extractFromURL(String url) {
		List<JSONObject> jsonList = new ArrayList<JSONObject>();
		try {
			URL oracle = new URL(url);
			URLConnection yc = oracle.openConnection();
			Scanner jsonReader = new Scanner(new InputStreamReader(
			                            yc.getInputStream()));
			
			while(jsonReader.hasNextLine()) {
				String content = jsonReader.nextLine();
				jsonList.add( getJSONObject(content) );
			}
			jsonReader.close();
		} catch (MalformedURLException e) {
			
			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return Collections.unmodifiableList(jsonList);
	}
	
	/**
	 * @param JSONString	JSON-formatted String containing a single entry
	 * @return				a JSONObject containing the details from the JSON formatted string
	 */
	public static JSONObject getJSONObject(String JSONString)	{
		JSONObject jsonObject = new JSONObject();
		
		try {
			jsonObject = new JSONObject(JSONString);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return jsonObject;
	}
	
	/**
	 * @param jArray		the JSONObject to be converted
	 * @return					EventHowlerParticipant object derived from JSONObject
	 * @throws JSONException
	 */
	public static EventHowlerParticipant convertJSONArrayToParticipant(JSONArray jArray) throws JSONException {
		String phoneNumber = jArray.getString(ATTRIBUTE_PHONE_NUMBER);
		String transactionId = jArray.getString(ATTRIBUTE_TRANS_ID);
		
		return new EventHowlerParticipant(phoneNumber, transactionId, STATUS_FOR_SENDING);
	}
	
}
