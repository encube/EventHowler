package com.onb.eventHowler.domain;

public class EventHowlerParticipant {

	private final String name;
	private final String phoneNumber;
	private String status;
	
	public EventHowlerParticipant(String name, String phoneNumber, String status) {
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	
}
