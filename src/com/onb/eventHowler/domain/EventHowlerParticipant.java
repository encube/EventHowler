package com.onb.eventHowler.domain;

public class EventHowlerParticipant {

	private final String phoneNumber;
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

	public void setStatus(String status) {
		this.status = status;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	
	public String getTransactionId() {
		return transactionId;
	}
}
