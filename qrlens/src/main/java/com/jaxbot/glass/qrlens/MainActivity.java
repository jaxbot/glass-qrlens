package com.jaxbot.glass.qrlens;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.glass.app.Card;

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

				if (res.getString("qr_type").toString().equals("URI")) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(res.getString("qr_data").toString()));
					startActivity(browserIntent);

					finish();
				} else {
					Card card = new Card(this);
					card.setText(res.getString("qr_data").toString());
					setContentView(card.getView());
				}
			}
		}
	}
}

