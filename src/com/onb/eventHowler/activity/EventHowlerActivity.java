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
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class EventHowlerActivity extends Activity {
	
	private EventHowlerApplication application;
	private ImageView idle, howling;
	private ToggleButton toggleButton;
	private EditText eventId, secretKey;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        application = (EventHowlerApplication)getApplication();
        
        toggleButton = (ToggleButton)findViewById(R.id.howl_toggle_button);
    	eventId = (EditText)findViewById(R.id.event_id_edit_text);
		secretKey = (EditText)findViewById(R.id.secret_key_edit_text);
		howling = (ImageView)findViewById(R.id.howling);
		idle = (ImageView)findViewById(R.id.idle);
		howling.setBackgroundResource(R.drawable.event_howling_animation);
		
		AnimationDrawable frameAnimation = (AnimationDrawable) howling.getBackground();
		frameAnimation.start();
        
    }
    
    public void onSwitchToggled(View view){
		
		BroadcastReceiver forceStopActionReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				Toast.makeText(context, "forced stop",
    					Toast.LENGTH_SHORT).show();
				Log.d("receiver", "ui update on force stop");
				setIdleUI(true);
				toggleButton.setChecked(false);
				context.unregisterReceiver(this);
			}
		};
		
		if(eventId.getText().toString().equals("") || secretKey.getText().toString().equals("")){
			Toast.makeText(getApplicationContext(), "please input event id and secret key",
					Toast.LENGTH_SHORT).show();
			toggleButton.setChecked(false);
		}
		else if(toggleButton.isChecked()){
			
			setIdleUI(false);
			
    		application.setEventId(eventId.getText().toString());
    		application.setSecretKey(secretKey.getText().toString());
    		application.startRetrievingToURL();
    		application.setEventHowlerURLRetrieverServiceStatus(ServiceStatus.START);
    		
    		registerReceiver(forceStopActionReceiver, new IntentFilter("FORCE_STOP"));
    		Runnable urlRetreiverChecker = webQueryStatusChecker();
    		new Thread(urlRetreiverChecker).start();
    	}
    	else{
    		application.stopEvent();
    		
    		Runnable finishingChecker = progressDialogSpawner();
    		new Thread(finishingChecker).start();
    		setIdleUI(true);
    	}
    }

	private void setIdleUI(boolean isIdle) {
		eventId.setEnabled(isIdle);
		secretKey.setEnabled(isIdle);
		if(isIdle){
			howling.setVisibility(8);
			idle.setVisibility(0);
		}
		else{
			howling.setVisibility(0);
			idle.setVisibility(8);
		}
	}

	private Runnable webQueryStatusChecker() {
		Runnable urlRetreiverChecker = new Runnable(){
			@Override
			public void run(){
				while(application.getEventHowlerWebQueryServiceStatus() == ServiceStatus.START){
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {}
					Log.d("webQueryStatusChecker", "not free from loop");
				}
				Log.d("webQueryStatusChecker", "free from loop");
				if(application.getEventHowlerWebQueryServiceStatus() == ServiceStatus.RUNNING){
					application.startEvent();
				}
			}
		};
		return urlRetreiverChecker;
	}

	private Runnable progressDialogSpawner() {
		final ProgressDialog dialog = ProgressDialog.show(this,
				"finishing", "finishing last cycle\n please wait...");
		Runnable finishingChecker = new Runnable(){
			@Override
			public void run(){
				while(!application.allServicesFinished()){
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