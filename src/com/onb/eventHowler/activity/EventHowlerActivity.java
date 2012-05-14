package com.onb.eventHowler.activity;

import com.onb.eventHowler.R;
import com.onb.eventHowler.application.EventHowlerApplication;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

public class EventHowlerActivity extends Activity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void onSwitchToggled(View view){
    	EventHowlerApplication application = (EventHowlerApplication)getApplication();
    	ToggleButton toggleButton = (ToggleButton)view.findViewById(R.id.howl_toggle_button);
    	if(toggleButton.isChecked()){
    		application.startEvent();
    	}
    	else{
    		application.stopEvent();
    	}
    }
}