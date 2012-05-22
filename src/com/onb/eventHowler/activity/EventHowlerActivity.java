package com.onb.eventHowler.activity;

import com.onb.eventHowler.R;
import com.onb.eventHowler.application.EventHowlerApplication;
import com.onb.eventHowler.application.ServiceStatus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class EventHowlerActivity extends Activity {
	
	EventHowlerApplication application;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        application = (EventHowlerApplication)getApplication();
        
    }
    
    public void onSwitchToggled(View view){
    	
    	final ToggleButton toggleButton = (ToggleButton)findViewById(R.id.howl_toggle_button);
    	final EditText eventId = (EditText)findViewById(R.id.event_id_edit_text);
		final EditText secretKey = (EditText)findViewById(R.id.secret_key_edit_text);
		final ImageView howling = (ImageView)findViewById(R.id.howling);
		final ImageView idle = (ImageView)findViewById(R.id.idle);
		
		BroadcastReceiver forceStopActionReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				Toast.makeText(context, "forced stop",
    					Toast.LENGTH_SHORT).show();
				Log.d("receiver", "ui update on force stop");
				eventId.setEnabled(true);
				secretKey.setEnabled(true);
				toggleButton.setChecked(false);
				howling.setVisibility(8);
				idle.setVisibility(0);
				context.unregisterReceiver(this);
			}
		};
		
		if(eventId.getText().toString().equals("") || secretKey.getText().toString().equals("")){
			Toast.makeText(getApplicationContext(), "please input event id and secret key",
					Toast.LENGTH_SHORT).show();
			toggleButton.setChecked(false);
		}
		else if(toggleButton.isChecked()){
			
			eventId.setEnabled(false);
			secretKey.setEnabled(false);
			howling.setVisibility(0);
			idle.setVisibility(8);
			
    		application.setEventId(eventId.getText().toString());
    		application.setSecretKey(secretKey.getText().toString());
    		application.startRetrievingToURL();
    		application.setEventHowlerURLRetrieverServiceStatus(ServiceStatus.START);
    		
    		registerReceiver(forceStopActionReceiver, new IntentFilter("FORCE_STOP"));
    		Runnable urlRetreiverChecker = webQueryStatusChecker();
    		new Thread(urlRetreiverChecker).start();
    	}
    	else{
    		howling.setVisibility(8);
			idle.setVisibility(0);
    		application.stopEvent();
    		
    		Runnable finishingChecker = progressDialogSpawner();
    		new Thread(finishingChecker).start();
    		eventId.setEnabled(true);
			secretKey.setEnabled(true);
    	}
    }

	private Runnable webQueryStatusChecker() {
		Runnable urlRetreiverChecker = new Runnable(){
			@Override
			public void run(){
				while(application.getEventHowlerURLRetrieverServiceStatus() == ServiceStatus.START){
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {}
					Log.d("webQueryStatusChecker", "not free from loop");
				}
				Log.d("webQueryStatusChecker", "free from loop");
				if(application.getEventHowlerURLRetrieverServiceStatus() == ServiceStatus.RUNNING){
					application.startEvent();
				}
			}
		};
		return urlRetreiverChecker;
	}

	private Runnable progressDialogSpawner() {
		final ProgressDialog dialog = ProgressDialog.show(this,
				"finishing", "please wait until the last process finish");
		Runnable finishingChecker = new Runnable(){
			@Override
			public void run(){
				while(application.isRunningLastCycle()){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
				}
				dialog.dismiss();
			}
		};
		return finishingChecker;
	}
}