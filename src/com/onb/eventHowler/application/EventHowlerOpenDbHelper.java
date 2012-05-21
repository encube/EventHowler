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
	private static final String DATABASE = "eventHowler.db";
	private static final String TABLE_PARTICIPANTS = "participants";
	
	private static final String PARTICIPANT_COLUMN_PNUMBER = "phone_number";
	private static final String PARTICIPANT_COLUMN_STATUS = "status";
	private static final String PARTICIPANT_COLUMN_TRANSACTION_ID = "transactionId";
	private static final String PARTICIPANT_COLUMN_MESSAGE = "message";

	
	
	public EventHowlerOpenDbHelper(Context context) {
		super(context, DATABASE, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d("onCreate", "creating database");
		String query = "CREATE TABLE "+ TABLE_PARTICIPANTS
				+ " (" + PARTICIPANT_COLUMN_PNUMBER + " TEXT, "
				+ PARTICIPANT_COLUMN_STATUS + " TEXT, "
				+ PARTICIPANT_COLUMN_TRANSACTION_ID + " TEXT, "
				+ PARTICIPANT_COLUMN_MESSAGE  + " TEXT)";
		db.execSQL(query);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
	public Cursor getAllParticipants() {
		Log.d("getAllParticipants", "getAllParticipants");
		return getReadableDatabase().rawQuery("SELECT * FROM " 
									+ TABLE_PARTICIPANTS, null);
	}
	
	public Cursor getAllParticipantsWithUnsentMessages(){
		Log.d("getAllParticipantsWithUnsentMessages", "getAllParticipantsWithUnsentMessages");
		return getReadableDatabase().rawQuery("SELECT * FROM " 
									+ TABLE_PARTICIPANTS + " WHERE " 
									+ PARTICIPANT_COLUMN_STATUS 
									+ " LIKE 'FOR_SEND_%'", null);
	} 
	
	public Cursor getAllReplyStatus() {
		Log.d("openHelper", "getAllReplyStatus");
		return getReadableDatabase().rawQuery("SELECT * FROM "
									+ TABLE_PARTICIPANTS + " WHERE "
									+ PARTICIPANT_COLUMN_MESSAGE + " AND "
									+ PARTICIPANT_COLUMN_STATUS
									+ " LIKE 'FOR_SEND_%'", null);
	}
	
	public void resetDatabase() {
		Log.d("resetDatabase", "resetting database");
		getWritableDatabase().delete(TABLE_PARTICIPANTS, "1", null);
	}
	
	public void insertParticipant(EventHowlerParticipant participant, String message){
		Log.d("insertParticipant", "inserting participant");
		ContentValues contentValues = new ContentValues();
		contentValues.put(PARTICIPANT_COLUMN_PNUMBER, participant.getPhoneNumber());
		contentValues.put(PARTICIPANT_COLUMN_STATUS, participant.getStatus());
		contentValues.put(PARTICIPANT_COLUMN_TRANSACTION_ID, participant.getTransactionId());
		contentValues.put(PARTICIPANT_COLUMN_MESSAGE, message);

		Log.d("UPDATE", participant.getPhoneNumber() + participant.getStatus() + participant.getTransactionId() + message);
		
		getWritableDatabase().insert(TABLE_PARTICIPANTS, null, contentValues);
	}
	
	public void updateStatus(EventHowlerParticipant participant, String message){
		Log.d("updateStatus", "updating status");
		ContentValues contentValues = new ContentValues();
		contentValues.put(PARTICIPANT_COLUMN_PNUMBER, participant.getPhoneNumber());
		contentValues.put(PARTICIPANT_COLUMN_STATUS, participant.getStatus());
		contentValues.put(PARTICIPANT_COLUMN_TRANSACTION_ID, participant.getTransactionId());
		contentValues.put(PARTICIPANT_COLUMN_MESSAGE, message);
		
		Log.d("UPDATE", participant.getPhoneNumber() + participant.getStatus() + participant.getTransactionId() + message);
		
		getWritableDatabase().update(TABLE_PARTICIPANTS, contentValues, PARTICIPANT_COLUMN_PNUMBER + " = " + participant.getPhoneNumber(), null);
	}

	public boolean checkNumberIfExist(String phoneNumber) {
		Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " 
				+ TABLE_PARTICIPANTS + " WHERE " 
				+ PARTICIPANT_COLUMN_PNUMBER 
				+ " = " + phoneNumber, null);
		if(cursor.getCount() == 0){
			Log.d("checkNumberIfExist", "false");
			cursor.close();
			return false;
		}
		cursor.close();
		Log.d("checkNumberIfExist", "true");
		return true;
		
	}

	public String findNumberWithTransactionId(String transactionId) {
		String phoneNumber;
		Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " 
				+ TABLE_PARTICIPANTS + " WHERE " 
				+ PARTICIPANT_COLUMN_TRANSACTION_ID 
				+ " = " + transactionId, null);
		if(cursor.getCount() == 0){
			Log.d("findNumberWithTransactionId", "Got NONE");
			return "NONE";
		}
		cursor.moveToFirst();
		phoneNumber = cursor.getString(0);
		cursor.close();
		Log.d("findNumberWithTransactionId", "Got " + phoneNumber);

		return phoneNumber;
	}

}