package com.jaxbot.glass.qrlens;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jonathan on 10/24/14.
 */
public class ReadMoreActivity extends Activity {
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

        mCardData = res.getString("qr_data");

        mCardScrollView = new CardScrollView(this);

        createCardsPaginated();
        createView();

        allowDestroy = true;
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
    void createView() {
        mAdapter = new MyCardScrollAdapter();
        mCardScrollView.setAdapter(mAdapter);
        mCardScrollView.activate();

        mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                audio.playSoundEffect(Sounds.TAP);
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
        inflater.inflate(R.menu.defaultmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.menu_item_2:
                this.setResult(RESULT_OK);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

        int lines;
        String line;

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
