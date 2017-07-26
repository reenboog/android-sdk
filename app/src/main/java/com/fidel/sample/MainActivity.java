package com.fidel.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.fidel.fidel.Fidel;
import com.fidel.fidel.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
        Fidel.programId = "690ae72c-9f9f-4c82-b6be-37285b3dd576";
        Fidel.apiKey = "pk_test_fb56a52e-2f13-4bc1-9e1f-14f4ae7548f6";
        Fidel.autoScan = false;


        Button btn = (Button)findViewById(R.id.btn_show);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fidel.present(MainActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Fidel.FIDEL_LINK_CARD_REQUEST_CODE) {
            if(data != null && data.hasExtra(Fidel.FIDEL_LINK_CARD_RESULT_CARD_ID)) {
                String cardId = data.getStringExtra(Fidel.FIDEL_LINK_CARD_RESULT_CARD_ID);

                Log.d("d", "CARD ID = " + cardId);
            }
        }
    }
}
