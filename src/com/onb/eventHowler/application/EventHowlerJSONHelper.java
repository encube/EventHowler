package com.onb.eventHowler.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

	private static final String ATTRIBUTE_PHONE_NUMBER = "phoneNumber";
	private static final String ATTRIBUTE_STATUS = "status";
	
	/**
	 * @param url	URL to the JSON-formatted web page
	 * @return		list containing JSONArrays generated from the JSON formatted file
	 */
	public static List<JSONArray> extractFromURL(String url) {
		List<JSONArray> jsonList = new ArrayList<JSONArray>();
		try {
			URL oracle = new URL(url);
			URLConnection yc = oracle.openConnection();
			Scanner jsonReader = new Scanner(new InputStreamReader(
			                            yc.getInputStream()));
			
			while(jsonReader.hasNextLine()) {
				String content = jsonReader.nextLine();
				jsonList.add( getJSONArray(content) );
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
	 * @param filePath	location of the JSON-formatted file
	 * @return			list containing JSONArrays generated from the JSON formatted file
	 */
	public static List<JSONArray> extractJSONFile(String filePath) {
		List<JSONArray> jsonList = new ArrayList<JSONArray>();
		
		try {
			Scanner jsonReader = new Scanner(new FileInputStream(new File(filePath)));
		
			while(jsonReader.hasNextLine()) {
				String content = jsonReader.nextLine();
				jsonList.add( getJSONArray(content) );
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return Collections.unmodifiableList(jsonList);
	}
	
	/**
	 * @param JSONString	JSON-formatted String containing multiple entries
	 * @return				a JSONArray containing multiple entries extracted from the JSON formatted String
	 */
	public static JSONArray getJSONArray(String JSONString) {
		JSONArray jsonArray = new JSONArray();
		
		try {
			jsonArray = new JSONArray(JSONString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return jsonArray;
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
	 * @param jsonArray		the JSONArray containing the entries
	 * @param index			index of the desired JSONObject in the JSONArray
	 * @return				JSONObject of a single entry from the JSONArray
	 */
	public static JSONObject extractJSONArray(JSONArray jsonArray, int index)	{
		JSONObject jsonObject = new JSONObject();
		
		try {
			jsonObject = jsonArray.getJSONObject(index);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return jsonObject;
	}
	
	/**
	 * @param jsonObject		the JSONObject to be converted
	 * @return					EventHowlerParticipant object derived from JSONObject
	 * @throws JSONException
	 */
	public static EventHowlerParticipant convertJSONObjectToParticipant(JSONObject jsonObject) throws JSONException {
		String phoneNumber = jsonObject.getString(ATTRIBUTE_PHONE_NUMBER);
		String status = jsonObject.getString(ATTRIBUTE_STATUS);
		EventHowlerParticipant participant = new EventHowlerParticipant(phoneNumber, status);
		
		return participant;
	}
	
}
