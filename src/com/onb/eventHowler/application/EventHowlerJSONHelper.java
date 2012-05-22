package com.onb.eventHowler.application;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.onb.eventHowler.domain.EventHowlerParticipant;

public class EventHowlerJSONHelper {

	public static final String ATTRIBUTE_CONTENT = "contacts";
	public static final String ATTRIBUTE_PHONE_NUMBER = "contactNumber";
	public static final String ATTRIBUTE_TRANS_ID = "transactionId";
	public static final String ATTRIBUTE_MESSAGE = "message";
	
	/**
	 * @param url	URL to the JSON-formatted web page
	 * @return		list containing JSONArrays generated from the JSON formatted file
	 * @throws IOException 
	 */
	public static List<JSONObject> extractFromURL(String url) throws MalformedURLException, ProtocolException, IOException {
		List<JSONObject> jsonList = new ArrayList<JSONObject>();
			Scanner jsonReader = new Scanner(new InputStreamReader(EventHowlerURLHelper.getInputStreamFromURL(url)));
			
			while(jsonReader.hasNextLine()) {
				String content = jsonReader.nextLine();
				Log.d("content of JSON", content);
				jsonList.add( getJSONObject(content) );
			}
			jsonReader.close();
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
	 * @param jObject		the JSONObject to be converted
	 * @return					EventHowlerParticipant object derived from JSONObject
	 * @throws JSONException
	 */
	public static EventHowlerParticipant convertJSONObjectToParticipant(JSONObject jObject) throws JSONException {
		String phoneNumber = jObject.getString(ATTRIBUTE_PHONE_NUMBER);
		Log.d("convertJSONObjectToParticipant", phoneNumber);
		String transactionId = jObject.getString(ATTRIBUTE_TRANS_ID);
		Log.d("convertJSONObjectToParticipant", transactionId);
		
		return new EventHowlerParticipant(phoneNumber, transactionId, MessageStatus.FOR_SEND.toString());
	}
	
}
