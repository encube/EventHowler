package com.onb.eventHowler.domain;

public class EventHowlerParticipant {

	private final String phoneNumber;
	private String status;
	
	public EventHowlerParticipant(String phoneNumber, String status) {
		this.phoneNumber = phoneNumber;
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	
}
