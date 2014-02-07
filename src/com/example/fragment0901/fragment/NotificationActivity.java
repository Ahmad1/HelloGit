package com.example.fragment0901.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class NotificationActivity extends Activity {
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		
		// Now finish, which will drop the user in to the activity that was at the top
	    // of the task stack
		Intent note = new Intent(this, PodExpandActivity.class);
		note.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(note);
	    finish();
	    
	}

}
