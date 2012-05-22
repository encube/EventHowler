package com.onb.eventHowler.domain;

public class EventHowlerParticipant {

	private final String phoneNumber;
	private String status;
	private String transactionId;
	
	public EventHowlerParticipant(String phoneNumber, String transactionId, String status) {
		this.phoneNumber = phoneNumber;
		this.transactionId = transactionId;
		this.status = status;
	}

	/**
	 * 
	 * @return participants status
	 */
	
	public String getStatus() {
		return status;
	}

	/**
	 * 
	 * @return transaction id of the participant
	 */
	
	public String getTransactionId() {
		return transactionId;
	}
	
	/**
	 * 
	 * @return participants phoneNumber
	 */
	
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * sets the status of the participant
	 * 
	 * @param status status to be set
	 */
	
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * sets the transaction id of the participant
	 * 
	 * @param transactionId		transaction id to be set
	 */
	
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	
}
