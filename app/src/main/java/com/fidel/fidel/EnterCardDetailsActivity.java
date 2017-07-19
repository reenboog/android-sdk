package com.fidel.fidel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

import com.prolificinteractive.creditcard.CreditCard;
import com.prolificinteractive.creditcard.CreditCardUtil;
import com.prolificinteractive.creditcard.android.CreditCardTextWatcher;

import java.util.regex.Pattern;

import static com.prolificinteractive.creditcard.CreditCardUtil.AMERICAN_EXPRESS;
import static com.prolificinteractive.creditcard.CreditCardUtil.DINERS_CLUB;
import static com.prolificinteractive.creditcard.CreditCardUtil.DISCOVER;
import static com.prolificinteractive.creditcard.CreditCardUtil.JCB_15;
import static com.prolificinteractive.creditcard.CreditCardUtil.JCB_16;
import static com.prolificinteractive.creditcard.CreditCardUtil.MASTERCARD;
import static com.prolificinteractive.creditcard.CreditCardUtil.TYPE_JCB_16;
import static com.prolificinteractive.creditcard.CreditCardUtil.VISA;

public class EnterCardDetailsActivity extends AppCompatActivity {

    private class UnknownCard implements CreditCard {
        final Pattern typePattern;
        final Pattern verifyPattern;
        final int[] format;

        public UnknownCard(Pattern verifyPattern, Pattern typePattern, int[] format) {
            this.verifyPattern = verifyPattern;
            this.typePattern = typePattern;
            this.format = format;
        }

        @Override public int[] getFormat() { return format; }

        @Override public Pattern getTypePattern() { return typePattern; }

        @Override public Pattern getVerifyPattern() { return verifyPattern; }
    }

    UnknownCard unknownCard = new UnknownCard(Pattern.compile("^[0-9]{16}?"), Pattern.compile("^[0-9]{4}"), new int[] { 4, 4, 4, 4 });

    private CreditCard[] cards = new CreditCard[]{CreditCardUtil.VISA, CreditCardUtil.MASTERCARD, unknownCard};

    private CreditCardUtil cardUtil = new CreditCardUtil(cards);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_card_details);

        EditText editText = (EditText) findViewById(R.id.fdl_card_form_card_edit_text);
        editText.addTextChangedListener(new CreditCardTextWatcher(cardUtil) {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                super.onTextChanged(s, start, before, count);

                 if(CreditCardUtil.equalTypeCard(s, CreditCardUtil.VISA)) {
                     Log.d("d", "VISA");
                 } else if(CreditCardUtil.equalTypeCard(s, CreditCardUtil.MASTERCARD)) {
                     Log.d("d", "MASTERCARD");
                 } else if(CreditCardUtil.equalTypeCard(s, unknownCard)) {
                     Log.d("d", "UNKNOWN");
                 }

            }
        });
    }
}
