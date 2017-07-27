package com.fidel.fidel;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

public class TOSWebViewActivity extends AppCompatActivity {

    public static final String URL_EXTRAS = "url_extras";
    public static final String TITLE_EXTRAS = "title_extras";

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tosweb_view);

        webView = (WebView)findViewById(R.id.fdl_tos_web_view);

        Intent intent = getIntent();

        String url = intent.getStringExtra(URL_EXTRAS);

        if(url != null) {
            webView.loadUrl(url);
        }

        String caption = intent.getStringExtra(TITLE_EXTRAS);

        if(caption != null) {
            ActionBar bar = getSupportActionBar();

            if(bar != null) {
                bar.setTitle(caption);

                bar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
