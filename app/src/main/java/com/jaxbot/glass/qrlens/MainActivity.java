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
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.jaxbot.glass.barcode.scan.CaptureActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity implements GestureDetector.FingerListener {
	final int SCAN_QR = 4;
    final String TAG = "app";

    final String FOOTER = "QR text content";

    private List<CardBuilder> mCards;
    private CardScrollView mCardScrollView;
    private MyCardScrollAdapter mAdapter;

    boolean mNeedsReadMore;
    boolean invalid = false;

    String mCardData;

    Context context;

    boolean allowDestroy = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        this.setResult(RESULT_CANCELED);

        context = this;
        invalid = false;

        Intent data = getIntent();
        Bundle res = data.getExtras();

        String qrtype = res.getString("qr_type");
        String qrdata = res.getString("qr_data");

        Log.w(TAG, qrtype);
        Log.w(TAG, qrdata);

        if (qrtype.equals("-1")) {
            showInvalid();
            createView();
            allowDestroy = true;
            return;
        }
        if (qrtype.equals("URI")) {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(qrdata));
                startActivity(browserIntent);

                finish();
            } catch (Exception e) {
                showInvalid();
                createView();
                allowDestroy = true;
            }
        } else {

            createCards(qrdata);
            createView();

            allowDestroy = true;
        }
	}

    @Override
    protected void onPause()
    {
        super.onPause();

        if (allowDestroy) {
            this.setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SCAN_QR) {
			if (resultCode == RESULT_OK) {
			} else {
                finish();
            }
		}
	}

    void createView() {
        mCardScrollView = new CardScrollView(this);
        mAdapter = new MyCardScrollAdapter();
        mCardScrollView.setAdapter(mAdapter);
        mCardScrollView.activate();

        mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                audio.playSoundEffect(Sounds.TAP);
                if (invalid)
                    finish();
                else
                    openOptionsMenu();
            }
        });
        setContentView(mCardScrollView);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        if (mNeedsReadMore)
            inflater.inflate(R.menu.readmore, menu);
        else
            inflater.inflate(R.menu.defaultmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.menu_item_1:
                createCardsPaginated();
                return true;
            case R.id.menu_item_2:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showInvalid() {
        mCards = new ArrayList<CardBuilder>();
        mCards.add(new CardBuilder(this, CardBuilder.Layout.ALERT)
                        .setIcon(R.drawable.ic_alert)
                        .setText(R.string.unable_to_read)
                        .setFootnote(R.string.tap_to_try_again)
        );
        invalid = true;

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.ERROR);
    }

    private void createCards(String data) {
        mCardData = data;
        mCards = new ArrayList<CardBuilder>();

        if (data.length() > 200 || data.split("\n").length > 7)
            mNeedsReadMore = true;

        mCards.add(new CardBuilder(this, CardBuilder.Layout.TEXT)
            .setText(data)
            .setFootnote(FOOTER)
        );

    }

    int numNewlines(String str)
    {
        Matcher m = Pattern.compile("(\n)|(\r)|(\r\n)").matcher(str);
        int lines = 0;
        while (m.find())
        {
            lines ++;
        }
        return lines;
    }

    private void createCardsPaginated() {
        mCards = new ArrayList<CardBuilder>();

        String[] chunks = mCardData.split("\\b");

        int lines = 0;
        String line = "";

        for (int i = 0; i < chunks.length; i++) {
            String hunk = "";
            line = "";
            lines = 0;
            for (; i < chunks.length; i++) {
                if ((line + chunks[i]).length() > 23) {
                    line = "";
                    lines++;
                }
                line += chunks[i];
                if (numNewlines(chunks[i]) > 0)
                {
                    line = "";
                    lines++;
                }
                if (lines > 6)
                {
                    i--;
                    break;
                }
                hunk += chunks[i];
            }
            if (hunk.substring(0, 2).equals("\r\n"))
                hunk = hunk.substring(2);
            if (hunk.substring(0, 1).equals(" ") || hunk.substring(0, 1).equals("\n") || hunk.substring(0, 1).equals("\r"))
                hunk = hunk.substring(1);
            mCards.add(new CardBuilder(this, CardBuilder.Layout.TEXT_FIXED)
                            .setText(hunk)
            );
        }

        mCardScrollView.setAdapter(mAdapter);
        mCardScrollView.activate();

        mNeedsReadMore = false;
    }

    @Override
    public void onFingerCountChanged(int i, int i2) {

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

