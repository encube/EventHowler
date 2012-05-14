package com.onb.eventHowler.application;

import com.onb.eventHowler.domain.EventHowlerParticipant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EventHowlerOpenDbHelper extends SQLiteOpenHelper{

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE = "event_howler.db";
	private static final String TABLE_PARTICIPANTS = "participants";
	private static final String TABLE_MESSAGES = "messages";
	
	private static final String PARTICIPANT_COLUMN_PNUMBER = "phone_number";
	private static final String PARTICIPANT_COLUMN_NAME = "name";
	private static final String PARTICIPANT_COLUMN_STATUS = "status";
	
	private static final String MESSAGE_COLUMN_ID = "message_id";
	private static final String MESSAGE_COLUMN_MESSAGE = "message";
	
	
	public EventHowlerOpenDbHelper(Context context) {
		super(context, DATABASE, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		String query = "CREATE TABLE "+ TABLE_PARTICIPANTS
				+ " (" + PARTICIPANT_COLUMN_PNUMBER + " TEXT, "
				+ PARTICIPANT_COLUMN_NAME + " TEXT, "
				+ PARTICIPANT_COLUMN_STATUS + " TEXT)";
		db.execSQL(query);
		
		query = "CREATE TABLE "+ TABLE_MESSAGES
				+ " (" + MESSAGE_COLUMN_ID + " INT,"
				+ MESSAGE_COLUMN_MESSAGE + " TEXT)";
		db.execSQL(query);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	
	public Cursor getRow(String phoneNumber){
		return getReadableDatabase().rawQuery("SELECT * FROM " 
									+ TABLE_PARTICIPANTS 
									+ " WHERE " 
									+ PARTICIPANT_COLUMN_PNUMBER 
									+ " = " + phoneNumber, null);
	}
	
	public Cursor getAllParticipant() {
		Log.d("openHelper", "getAllParticipant");
		return getReadableDatabase().rawQuery("SELECT * FROM " 
									+ TABLE_PARTICIPANTS, null);
	}
	
	public Cursor getAllMesssages(){
		Log.d("openHelper", "getAllMessages");
		return getReadableDatabase().rawQuery("SELECT * FROM " 
									+ TABLE_MESSAGES, null);
	}
	
	public Cursor getAllParticipantToBeSend(){
		Log.d("openHelper", "getAllParticipantToBeSend");
		return getReadableDatabase().rawQuery("SELECT * FROM " 
									+ TABLE_PARTICIPANTS + " WHERE " 
									+ PARTICIPANT_COLUMN_STATUS 
									+ " NOT LIKE 'SENT'", null);
	} 
	
	public void resetDatabase() {
		Log.d("openHelper", "resetting database");
		getWritableDatabase().delete(TABLE_PARTICIPANTS, "1", null);
		getWritableDatabase().delete(TABLE_MESSAGES, "1", null);
	}
	
	public void insertParticipant(EventHowlerParticipant participant){
		Log.d("openHelper", "inserting participant");
		ContentValues contentValues = new ContentValues();
		contentValues.put(PARTICIPANT_COLUMN_NAME, participant.getName());
		contentValues.put(PARTICIPANT_COLUMN_PNUMBER, participant.getPhoneNumber());
		contentValues.put(PARTICIPANT_COLUMN_STATUS, participant.getStatus());
		
		getWritableDatabase().insert(TABLE_PARTICIPANTS, null, contentValues);
	}
	
	public void populateMessages(String invitationMessage,
								String replyMessage, 
								String confirmationCode,
								String negationCode){
		
		Log.d("opanHelper", "populating messages");
		ContentValues invitationMessageValues = new ContentValues();
		invitationMessageValues.put(MESSAGE_COLUMN_MESSAGE, invitationMessage);
		
		ContentValues replyMessageValues = new ContentValues();
		replyMessageValues.put(MESSAGE_COLUMN_MESSAGE, replyMessage);
		
		ContentValues confirmationCodeValues = new ContentValues();
		confirmationCodeValues.put(MESSAGE_COLUMN_MESSAGE, confirmationCode);
		
		ContentValues negationCodeValues = new ContentValues();
		negationCodeValues.put(MESSAGE_COLUMN_MESSAGE, negationCode);
		
		getWritableDatabase().insert(TABLE_MESSAGES, null, invitationMessageValues);
		getWritableDatabase().insert(TABLE_MESSAGES, null, replyMessageValues);
		getWritableDatabase().insert(TABLE_MESSAGES, null, confirmationCodeValues);
		getWritableDatabase().insert(TABLE_MESSAGES, null, negationCodeValues);
	}
	
	public void updateStatus(EventHowlerParticipant participant){
		Log.d("openHelper", "updating status");
		ContentValues contentValues = new ContentValues();
		contentValues.put(PARTICIPANT_COLUMN_PNUMBER, participant.getPhoneNumber());
		contentValues.put(PARTICIPANT_COLUMN_NAME, participant.getName());
		contentValues.put(PARTICIPANT_COLUMN_STATUS, participant.getStatus());
		
		getWritableDatabase().update(TABLE_PARTICIPANTS, contentValues, PARTICIPANT_COLUMN_PNUMBER + " = " + participant.getPhoneNumber(), null);
	}

	public String findNumber(String phoneNumber) {
		Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " 
				+ TABLE_PARTICIPANTS + " WHERE " 
				+ PARTICIPANT_COLUMN_PNUMBER 
				+ " = " + phoneNumber, null);
		if(cursor.getCount() == 0){
			cursor.close();
			return "NONE";
		}
		cursor.moveToFirst();
		String name = cursor.getString(1);
		cursor.close();
		return name;
		
	}
}