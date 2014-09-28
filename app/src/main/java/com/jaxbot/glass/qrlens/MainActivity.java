package com.jaxbot.glass.qrlens;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.android.glass.app.Card;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.jaxbot.glass.barcode.scan.CaptureActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
	final int SCAN_QR = 4;

    private List<CardBuilder> mCards;
    private CardScrollView mCardScrollView;
    private ExampleCardScrollAdapter mAdapter;

    Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = new Intent(this, CaptureActivity.class);
		startActivityForResult(intent, SCAN_QR);

        context = this;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SCAN_QR) {
			if (resultCode == RESULT_OK) {
				Bundle res = data.getExtras();
				Log.w("app", res.getString("qr_type").toString());
				Log.w("app", res.getString("qr_data").toString());

				String qrtype = res.getString("qr_type").toString();
				String qrdata = res.getString("qr_data").toString();

				if (qrtype.equals("URI")) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(res.getString("qr_data").toString()));
					startActivity(browserIntent);

					finish();
				} else {

                    createCards(qrdata);

                    mCardScrollView = new CardScrollView(this);
                    mAdapter = new ExampleCardScrollAdapter();
                    mCardScrollView.setAdapter(mAdapter);
                    mCardScrollView.activate();
                    mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                            audio.playSoundEffect(Sounds.DISALLOWED);
                        }
                    });
                    setContentView(mCardScrollView);
				}
			} else {
                finish();
            }
		}
	}

    private void createCards(String data) {
        mCards = new ArrayList<CardBuilder>();

        mCards.add(new CardBuilder(this, CardBuilder.Layout.TEXT)
                .setText(data)
                .setFootnote("QR Text Content"));

    }

    private class ExampleCardScrollAdapter extends CardScrollAdapter {

        @Override
        public int getPosition(Object item) {
            return mCards.indexOf(item);
        }

        @Override
        public int getCount() {
            return mCards.size();
        }

        @Override
        public Object getItem(int position) {
            return mCards.get(position);
        }

        @Override
        public int getViewTypeCount() {
            return CardBuilder.getViewTypeCount();
        }

        @Override
        public int getItemViewType(int position){
            return mCards.get(position).getItemViewType();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mCards.get(position).getView(convertView, parent);
        }
    }
}

