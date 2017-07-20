package com.fidel.fidel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class EnterCardDetailsActivity extends AppCompatActivity {

    private CreditCard[] cards = new CreditCard[]{CreditCardUtil.VISA, CreditCardUtil.MASTERCARD, CreditCardUtil.UNKNOWN};

    private CreditCardUtil cardUtil = new CreditCardUtil(cards);

    //
    EditText cardNumberEditText;
    EditText expiryEditText;
    ImageView cardIconImageView;

    TextView invalidCardNumberTextView;

    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_card_details);

        //
        invalidCardNumberTextView = (TextView)findViewById(R.id.fdl_card_form_row_card_number_invalid_caption);
        invalidCardNumberTextView.setVisibility(View.INVISIBLE);

        cardIconImageView = (ImageView)findViewById(R.id.fdl_card_form_card);
        //

        cardNumberEditText = (EditText)findViewById(R.id.fdl_card_form_card_edit_text);
        cardNumberEditText.addTextChangedListener(new CreditCardTextWatcher(cardUtil) {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                super.onTextChanged(s, start, before, count);

                invalidCardNumberTextView.setVisibility(View.INVISIBLE);

                boolean isVisaOrMastercard = false;

                 if(CreditCardUtil.equalTypeCard(s, CreditCardUtil.VISA)) {
                     isVisaOrMastercard = true;

                     Bitmap ic = BitmapFactory.decodeResource(getResources(), R.drawable.fdl_ic_card_visa);
                     cardIconImageView.setImageBitmap(ic);
                 } else if(CreditCardUtil.equalTypeCard(s, CreditCardUtil.MASTERCARD)) {
                     isVisaOrMastercard = true;

                     Bitmap ic = BitmapFactory.decodeResource(getResources(), R.drawable.fdl_ic_card_mastercard);
                     cardIconImageView.setImageBitmap(ic);
                 } else {
                     //} else if(CreditCardUtil.equalTypeCard(s, unknownCard)) {

                     invalidCardNumberTextView.setVisibility(View.VISIBLE);

                     Bitmap ic = BitmapFactory.decodeResource(getResources(), R.drawable.fdl_ic_card_unknown);
                     cardIconImageView.setImageBitmap(ic);
                 }

                 if(isVisaOrMastercard) {
                     if(cardUtil.isCardReal(s)) {
                         askForExpiry();
                     } else {
                         if(cardUtil.validateCard(s)) {
                             // if it's a full length card, it means it does not comply to Luhn's algorithm
                             invalidCardNumberTextView.setVisibility(View.VISIBLE);
                         }
                     }
                 }
            }
        });

        cardNumberEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(v == cardNumberEditText && hasFocus) {
                    Log.d("d", "FOCUS On Card");

                    // todo: play animation here
                }
            }
        });

        //
        expiryEditText = (EditText)findViewById(R.id.fdl_card_form_expiry_edit_text);

        expiryEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(v == expiryEditText && hasFocus) {
                    Log.d("d", "FOCUS On Expiry");

                    // todo: play animation here
                }
            }
        });

        expiryEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_DEL) {

                    if(expiryEditText.getText().length() == 0) {
                        askForCardNumber();
                    }
                }

                return false;
            }
        });

        //
        askForCardNumber();
    }

    //
    private void askForCardNumber() {
        cardNumberEditText.requestFocus();

        int textLength = cardNumberEditText.getText().length();
        if(textLength != 0) {
            cardNumberEditText.append("");
        }
    }

    private void askForExpiry() {
        expiryEditText.requestFocus();
    }
}
