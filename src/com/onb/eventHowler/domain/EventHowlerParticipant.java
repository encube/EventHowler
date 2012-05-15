package com.onb.eventHowler.domain;

public class EventHowlerParticipant {

	private final String firstName;
	private final String lastName;
	private final String phoneNumber;
	private String status;
	
	public EventHowlerParticipant(String firstName, String lastName, String phoneNumber, String status) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.phoneNumber = phoneNumber;
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	
}
