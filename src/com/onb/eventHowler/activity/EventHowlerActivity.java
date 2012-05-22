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
import android.widget.Toast;
import android.widget.ToggleButton;

public class EventHowlerActivity extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
    }
    
    public void onSwitchToggled(View view){
    	
    	final EventHowlerApplication application = (EventHowlerApplication)getApplication();
    	
    	final ToggleButton toggleButton = (ToggleButton)view.findViewById(R.id.howl_toggle_button);
    	final EditText eventId = (EditText)findViewById(R.id.event_id_edit_text);
		final EditText secretKey = (EditText)findViewById(R.id.secret_key_edit_text);
		
		BroadcastReceiver forceStopActionReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				Toast.makeText(context, "forced stop",
    					Toast.LENGTH_SHORT).show();
				eventId.setEnabled(true);
				secretKey.setEnabled(true);
				toggleButton.setChecked(false);
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
			
    		application.setEventId(eventId.getText().toString());
    		application.setSecretKey(secretKey.getText().toString());
    		application.startRetrievingToURL();
    		application.setEventHowlerURLRetrieverServiceStatus(ServiceStatus.START);
    		
    		registerReceiver(forceStopActionReceiver, new IntentFilter("FORCE_STOP"));
    		Runnable urlRetreiverChecker = new Runnable(){
    			public void run(){
    				while(application.getEventHowlerURLRetrieverServiceStatus() == ServiceStatus.START){
    					try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {}
    					Log.d("not free from loop", "gugugugugug");
    				}
    				Log.d("free from loop", "fhgfhjfjhg");
    				if(application.getEventHowlerURLRetrieverServiceStatus() == ServiceStatus.RUNNING){
						application.startEvent();
					}
    			}
    		};
    		new Thread(urlRetreiverChecker).start();
//    		if(application.getEventHowlerURLRetrieverServiceStatus() == ServiceStatus.STOP){
//    			Toast.makeText(getApplicationContext(), "forced stop",
//    					Toast.LENGTH_SHORT).show();
//    			toggleButton.setChecked(false);
//    			eventId.setEnabled(true);
//    			secretKey.setEnabled(true);
//    		}
    	}
    	else{
    		application.stopEvent();
    		final ProgressDialog dialog = ProgressDialog.show(this,
    				"finishing", "please wait until the last process finish");
    		
    		Runnable finishingChecker = new Runnable(){
    			public void run(){
    				while(application.isRunningLastCycle()){
    					try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {}
    				}
    				dialog.dismiss();
    			}
    		};
    		new Thread(finishingChecker).start();
    		eventId.setEnabled(true);
			secretKey.setEnabled(true);
    	}
    }
}