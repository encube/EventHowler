package com.onb.eventHowler.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import com.onb.eventHowler.service.EventHowlerURLRetrieverService;;
public class EventHowlerURLRetrieverTest{
	
	@Test
	public void generateURLTest()
	{
		EventHowlerURLRetrieverService retriever = new EventHowlerURLRetrieverService();
		String eventId = "q";
		String secretKey = "q";
		String queryURL = retriever.generateQueryURL(eventId, secretKey);
		assertEquals("EventHowlerApp/query?id=qsecretKey=q",queryURL);
	}
	
}
