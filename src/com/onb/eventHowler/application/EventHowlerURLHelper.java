package com.onb.eventHowler.application;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.HttpURLConnection;

import android.util.Log;

/**
 * @author xander
 * TODO CHECK IF THIS THING WORKS
 */
public class EventHowlerURLHelper {
	
	/**
	 * Connects to a specified URL
	 * @param url	URL connected to
	 */
	public static void goToURL(String url) {
		try{
			URL serverAddress = new URL(url);
			Log.d("Reply goToURL", url);
			HttpURLConnection connection = (HttpURLConnection) serverAddress.openConnection();
			connection.connect();
			InputStream stream = connection.getInputStream();
			stream.close();
		} catch (MalformedURLException e) {
			Log.d("MalformedURLException","Maybe checking if URL is valid.");
		} catch (ProtocolException e) {
			Log.d("ProtocolException", "Do check if request is valid.");
		} catch (IOException e) {
			Log.d("IOException", "Connection didn't successfully bind.");
		}
	}
	
	/**
	 * Gets an input stream from specified URL.
	 * 
	 * @param url	URL to get input stream from.
	 * @return		InputStream from URL
	 * @throws MalformedURLException
	 * @throws ProtocolException
	 * @throws IOException
	 */
	public static InputStream getInputStreamFromURL(String url) throws MalformedURLException, ProtocolException, IOException {
		URL serverAddress = new URL(url);
		Log.d("Reply goToURL", url);
		HttpURLConnection connection = (HttpURLConnection) serverAddress.openConnection();
		connection.connect();
		
		return connection.getInputStream();
	}
	
}
