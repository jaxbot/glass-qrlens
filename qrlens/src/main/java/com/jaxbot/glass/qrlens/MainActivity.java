package com.jaxbot.glass.qrlens;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.jaxbot.glass.barcode.scan.CaptureActivity;

public class MainActivity extends Activity {
	final int SCAN_QR = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = new Intent(this, CaptureActivity.class);
		startActivityForResult(intent, SCAN_QR);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SCAN_QR) {
			if (resultCode == RESULT_OK) {
				Bundle res = data.getExtras();
				Log.w("app", res.getString("qr_type").toString());
				Log.w("app", res.getString("qr_data").toString());
			}
		}
	}
}

