package com.onb.eventHowler.domain;

public class EventHowlerParticipant {

	private final String phoneNumber;
	private String transactionID;
	private String status;
	private String transactionId;
	
	public EventHowlerParticipant(String phoneNumber, String transactionId, String status) {
		this.phoneNumber = phoneNumber;
		this.setTransactionId(transactionId);
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

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	
	
}
