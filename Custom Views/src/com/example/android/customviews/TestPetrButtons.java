package com.example.android.customviews;

import phs.views.PetrButton;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import dtd.phs.lib.utils.Helpers;

public class TestPetrButtons extends Activity {
	private PetrButton btnTransform;
	private PetrButton btnMute;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_petr_buttons);
		btnTransform = (PetrButton) findViewById(R.id.btYes);
		btnTransform.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Helpers.showToast(TestPetrButtons.this, "Transform");
			}
		});
		
		btnMute = (PetrButton) findViewById(R.id.btNo);
		btnMute.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Helpers.showToast(TestPetrButtons.this, "Mute");
			}
		});
		
	}
}