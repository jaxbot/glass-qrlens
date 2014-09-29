package com.jaxbot.glass.qrlens;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.jaxbot.glass.barcode.scan.CaptureActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
	final int SCAN_QR = 4;
    final String TAG = "app";

    final String FOOTER = "QR Text Content";

    private List<CardBuilder> mCards;
    private CardScrollView mCardScrollView;
    private MyCardScrollAdapter mAdapter;

    boolean mNeedsReadMore;

    String mCardData;

    Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, SCAN_QR);

        context = this;
	}

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SCAN_QR) {
			if (resultCode == RESULT_OK) {
				Bundle res = data.getExtras();

                String qrtype = res.getString("qr_type");
                String qrdata = res.getString("qr_data");

				Log.w(TAG, qrtype);
				Log.w(TAG, qrdata);

				if (qrtype.equals("URI")) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(qrdata));
					startActivity(browserIntent);

					finish();
				} else {

                    createCards(qrdata);

                    mCardScrollView = new CardScrollView(this);
                    mAdapter = new MyCardScrollAdapter();
                    mCardScrollView.setAdapter(mAdapter);
                    mCardScrollView.activate();

                    mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            if (mNeedsReadMore) {
                                openOptionsMenu();
                            } else {
                                AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                                audio.playSoundEffect(Sounds.DISALLOWED);
                            }
                        }
                    });
                    setContentView(mCardScrollView);
				}
			} else {
                finish();
            }
		}
	}

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.readmore, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.menu_item_1:
                createCardsPaginated();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createCards(String data) {
        mCardData = data;
        mCards = new ArrayList<CardBuilder>();

        if (data.length() > 225 || data.split("\\n").length > 7)
            mNeedsReadMore = true;

        mCards.add(new CardBuilder(this, CardBuilder.Layout.TEXT)
            .setText(data)
            .setFootnote(FOOTER)
        );

    }

    private void createCardsPaginated() {
        mCards = new ArrayList<CardBuilder>();

        String[] chunks = mCardData.split("\\b");

        for (int i = 0; i < chunks.length; i++) {
            String hunk = "";
            for (; i < chunks.length; i++) {
                if ((hunk + chunks[i]).length() < 225) {
                    hunk += chunks[i];
                } else {
                    i--;
                    break;
                }
                if (hunk.split("\\n").length > 6) break;
            }
            mCards.add(new CardBuilder(this, CardBuilder.Layout.TEXT)
                .setText(hunk)
                .setFootnote(FOOTER)
            );
        }

        mCardScrollView.setAdapter(mAdapter);
        mCardScrollView.activate();

        mNeedsReadMore = false;
    }

    private class MyCardScrollAdapter extends CardScrollAdapter {

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

