package com.onb.eventHowler.domain;

public class EventHowlerParticipant {

	private final String phoneNumber;
	private String transactionID;
	private String status;
	
	public EventHowlerParticipant(String phoneNumber, String status) {
		this.phoneNumber = phoneNumber;
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getTransactionID() {
		return transactionID;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public void setTransactionID(String transactionID) {
		this.transactionID = transactionID;
	}
	
	
}
