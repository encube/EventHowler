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
	
	public static final String PARTICIPANT_COLUMN_PNUMBER = "phone_number";
	public static final String PARTICIPANT_COLUMN_STATUS = "status";
	public static final String PARTICIPANT_COLUMN_TRANSACTION_ID = "transactionId";
	public static final String PARTICIPANT_COLUMN_MESSAGE = "message";

	
	
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
	
	/**
	 * 
	 * @return 	true is participant table is empty, false otherwise.
	 */
	public boolean isParticipantEmpty(){
		Cursor cursor = getAllParticipants();
		boolean result = (cursor.getCount() == 0);
		cursor.close();
		return result;
	}
	
	/**
	 * 
	 * @return 	Cursor representing the current state of the participant table
	 */
	
	public Cursor getAllParticipants() {
		Log.d("getAllParticipants", "getAllParticipants");
		return getReadableDatabase().rawQuery("SELECT * FROM " 
									+ TABLE_PARTICIPANTS, null);
	}
	
	/**
	 * 
	 * @return		Cursor representing all the participant with FOR_SEND status
	 */
	
	public Cursor getAllParticipantsWithUnsentMessages(){
		Log.d("getAllParticipantsWithUnsentMessages", "getAllParticipantsWithUnsentMessages");
		return getReadableDatabase().rawQuery("SELECT * FROM " 
									+ TABLE_PARTICIPANTS + " WHERE " 
									+ PARTICIPANT_COLUMN_STATUS 
									+ " = '" + MessageStatus.FOR_SEND.toString() + "'", null);
	} 
	
	public Cursor getAllReplyStatus() {
		Log.d("openHelper", "getAllReplyStatus");
		return getReadableDatabase().rawQuery("SELECT * FROM "
									+ TABLE_PARTICIPANTS + " WHERE "
									+ PARTICIPANT_COLUMN_MESSAGE + " AND "
									+ PARTICIPANT_COLUMN_STATUS
									+ " LIKE 'FOR_SEND_%'", null);
	}
	
	
	/**
	 * insert a participant in participant table
	 * 
	 * @param participant		the participant
	 * @param message			message to/from participant
	 */
	
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
	
	/**
	 * update a participant in participant table
	 * 
	 * @param participant		the participant
	 * @param message			message to/from participant
	 */
	
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

	/**
	 * check if a number occur in the table
	 * 
	 * @param phoneNumber		number of the participant
	 * @return					true is phoneNumber exist
	 */
	
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

	/**
	 * return the phoneNumber of a participant with transaction ID
	 * 
	 * @param transactionId			the transaction ID of the participant
	 * @return						the phoneNumber of the participant
	 */
	
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

	public Cursor getAllParticipantsWithMessageSendingAttempts(){
		Log.d("getAllParticipantsWithReplies", "getAllParticipantsWithReplies");
		return getReadableDatabase().rawQuery("SELECT * FROM " 
									+ TABLE_PARTICIPANTS + " WHERE " 
									+ PARTICIPANT_COLUMN_STATUS 
									+ " LIKE '%SENT' OR "
									+ PARTICIPANT_COLUMN_STATUS 
									+ " LIKE '%DELIVERY'", null);
	}
	
	/**
	 * 
	 * @return Cursor representing all the participants the already reply
	 */
	
	public Cursor getAllParticipantsWithReplies(){
		Log.d("getAllParticipantsWithReplies", "getAllParticipantsWithReplies");
		return getReadableDatabase().rawQuery("SELECT * FROM " 
									+ TABLE_PARTICIPANTS + " WHERE " 
									+ PARTICIPANT_COLUMN_STATUS 
									+ " LIKE 'REPLY_RECEIVED'", null);
	}
	
	/**
	 *  gets the participant from the participant table
	 * @param participants			a participant row
	 * @return						a {@link EventHowlerParticipant}
	 */
	
	public static EventHowlerParticipant getParticipantFromCursor(Cursor participants) {
		String phoneNumber = participants.getString(participants.getColumnIndex(PARTICIPANT_COLUMN_PNUMBER));
		String transactionId = participants.getString(participants.getColumnIndex(PARTICIPANT_COLUMN_TRANSACTION_ID));
		String status = participants.getString(participants.getColumnIndex(PARTICIPANT_COLUMN_STATUS));
		
		return new EventHowlerParticipant(phoneNumber, transactionId, status);
	}
	
	/**
	 * gets the message of a participant
	 * 
	 * @param participants			a row in the participant table
	 * @return						message to/from the participant
	 */
	
	public static String getMessageFromCursor(Cursor participants) {
		return participants.getString(participants.getColumnIndex(PARTICIPANT_COLUMN_MESSAGE));
	}
	
	/**
	 * resets the database. deleting all rows in participant table.
	 */
	
	public void resetDatabase() {
		Log.d("resetDatabase", "resetting database");
		getWritableDatabase().delete(TABLE_PARTICIPANTS, "1", null);
	}
}