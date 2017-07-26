package com.fidel.fidel;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.Space;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.ref.WeakReference;

import io.card.payment.CardIOActivity;

public class EnterCardDetailsActivity extends AppCompatActivity implements Fidel.OnCardOperationDelegate {

    public static final class FidelServiceAuthorization {
        private FidelServiceAuthorization() {}
        public void isAuthorized() {}
    }

    enum LinkButtonType {
        LBT_LINK,
        LBT_LOADER,
        LBT_CHECK
    }

    private static final int FIDEL_SCAN_REQUEST_CODE = 2343;

    private CreditCard[] cards = new CreditCard[]{CreditCardUtil.VISA, CreditCardUtil.MASTERCARD, CreditCardUtil.UNKNOWN};

    private CreditCardUtil cardUtil = new CreditCardUtil(cards);

    //
    EditText cardNumberEditText;
    EditText expiryEditText;
    ImageView cardIconImageView;

    TextView invalidCardNumberTextView;
    TextView invalidExpiryTextView;

    ImageView btnCamera;

    TextView countryTextView;

    ImageView btnTOSCheckBox;
    ImageView btnLinkCard;

    ProgressBar linkProgress;
    ImageView linkMark;
    TextView linkTitle;

    View inputBlocker;

    //
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == FIDEL_SCAN_REQUEST_CODE) {
            if(data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                io.card.payment.CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

                String cardNumber = scanResult.cardNumber;
                cardNumberEditText.setText(cardNumber);

                String expiry = ExpiryDateUtil.expiryStringForMonthAndYear(scanResult.expiryMonth, scanResult.expiryYear);

                expiryEditText.setText(expiry);
            }
            else {
//                resultDisplayStr = "Scan was canceled.";
            }

        }
    }

    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_card_details);

        //
        invalidCardNumberTextView = (TextView)findViewById(R.id.fdl_card_form_row_card_number_invalid_caption);
        invalidCardNumberTextView.setVisibility(View.INVISIBLE);

        invalidExpiryTextView = (TextView)findViewById(R.id.fdl_card_form_row_expiry_invalid_caption);
        invalidExpiryTextView.setVisibility(View.INVISIBLE);

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
                         disableTOSCheckbox();

                         if(cardUtil.validateCard(s)) {
                             // if it's a full length card, it means it does not comply to Luhn's algorithm
                             invalidCardNumberTextView.setVisibility(View.VISIBLE);
                         }
                     }
                 } else {
                     disableTOSCheckbox();
                 }
            }
        });

        cardNumberEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(v == cardNumberEditText && hasFocus) {


                    // todo: play animation here
                    slideCardFormToValue(-50);
                }
            }
        });

        //
        expiryEditText = (EditText)findViewById(R.id.fdl_card_form_expiry_edit_text);

        expiryEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(v == expiryEditText && hasFocus) {


                    // todo: play animation here

                    slideCardFormToValue(-50);
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

        expiryEditText.addTextChangedListener(new ExpiryDateTextWatcher(expiryEditText) {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);

                if(s.length() == EXPIRY_TEXT_MAX_LENGTH) {
                    if(!ExpiryDateUtil.isDateExpired(s.toString())) {

                        askForCountry();

                        invalidExpiryTextView.setVisibility(View.INVISIBLE);
                    } else {
                        disableTOSCheckbox();

                        invalidExpiryTextView.setVisibility(View.VISIBLE);
                    }
                } else {
                    disableTOSCheckbox();

                    invalidExpiryTextView.setVisibility(View.INVISIBLE);
                }
            }
        });

        //

        btnCamera = (ImageView)findViewById(R.id.fdl_card_form_camera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableTOSCheckbox();

                resignAllFirstResponders();

                showCardIOActivity();
            }
        });

        //
        countryTextView = (TextView)findViewById(R.id.fdl_card_form_row_country_item);
        countryTextView.setText(CountriesUtil.countryByIndex(0));

        countryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askForCountry();
            }
        });

        //
        btnLinkCard = (ImageView)findViewById(R.id.fdl_card_form_btn_link_image_view);

        btnLinkCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((boolean)btnTOSCheckBox.getTag() == false) {
                    return;
                }

                resignAllFirstResponders();

                blockInput();

                setLinkButtonType(LinkButtonType.LBT_LOADER);

                linkCardWithCurrentFormData();
            }
        });

        //
        btnTOSCheckBox = (ImageView)findViewById(R.id.fdl_card_form_tos_checkbox);

        btnTOSCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!cardUtil.isCardReal(cardNumberEditText.getText())) {
                    askForCardNumber();

                    invalidCardNumberTextView.setVisibility(View.VISIBLE);

                    return;
                } else {
                    if(ExpiryDateUtil.isDateExpired(expiryEditText.getText().toString())) {
                        askForExpiry();

                        invalidExpiryTextView.setVisibility(View.VISIBLE);

                        return;
                    }
                }

                slideCardFormBack();

                boolean tag = (boolean)btnTOSCheckBox.getTag();

                if(!tag) {
                    resignAllFirstResponders();
                }

                setBtnTOSCheckBoxSelected(!tag);
            }
        });

        //
        setCardFormBottomGuide();

        //
        linkProgress = (ProgressBar)findViewById(R.id.fdl_card_form_btn_link_progress);
        linkMark = (ImageView)findViewById(R.id.fdl_card_form_btn_link_checkbox);
        linkTitle = (TextView)findViewById(R.id.fdl_card_form_btn_link_title);

        setLinkButtonType(LinkButtonType.LBT_LINK);

        //
        inputBlocker = findViewById(R.id.fdl_card_form_input_blocker);

        //

        setBtnTOSCheckBoxSelected(false);
        //

        setupToolBar();

        unblockInput();

        //
        askForCardNumberOrAutoScan();
    }

    private void setupToolBar() {
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        Toolbar toolBar = (Toolbar)getLayoutInflater().inflate(R.layout.fdl_custom_toolbar, null);

        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.WRAP_CONTENT);

        actionBar.setCustomView(toolBar, layout);

        Toolbar parent = (Toolbar)toolBar.getParent();
        parent.setContentInsetsAbsolute(0, 0);

        ImageView btnDismiss = (ImageView)toolBar.findViewById(R.id.fdl_action_bar_btn_close);

        btnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setBtnLinkCardEnabled(boolean e) {
        if(e) {
            btnLinkCard.setAlpha(1.0f);
        } else {
            btnLinkCard.setAlpha(0.5f);
        }
    }

    private void setBtnTOSCheckBoxSelected(boolean s) {
        btnTOSCheckBox.setTag(s);

        btnLinkCard.setClickable(s);

        setBtnLinkCardEnabled(s);

        if(s) {
            btnTOSCheckBox.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.fdl_checkbox_selected));
        } else {
            btnTOSCheckBox.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.fdl_checkbox));
        }
    }

    private void showCardIOActivity() {
        Intent scanIntent = new Intent(this, CardIOActivity.class);

        // customize these values to suit your needs.
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); // default: false

        // MY_SCAN_REQUEST_CODE is arbitrary and is only used within this activity.
        startActivityForResult(scanIntent, FIDEL_SCAN_REQUEST_CODE);
    }


    //
    private void askForCardNumber() {
        cardNumberEditText.requestFocus();

        int textLength = cardNumberEditText.getText().length();
        if(textLength != 0) {
            cardNumberEditText.append("");
        }
    }

    private void askForCardNumberOrAutoScan() {
        if(Fidel.autoScan) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showCardIOActivity();
                }
            }, 100);
        } else {
            askForCardNumber();
        }
    }

    private void askForExpiry() {
        expiryEditText.requestFocus();
    }

    private void askForCountry() {
        new MaterialDialog.Builder(this)
                .title("Choose your country")
                .items(CountriesUtil.countries())
                .itemsCallbackSingleChoice(CountriesUtil.indexForCountry(countryTextView.getText().toString()),
                        new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        countryTextView.setText(text);

                        return true;
                    }
                })
                .positiveText("OK")
                .negativeText("CANCEL")
                .show();
    }

    private void setCardFormBottomGuide() {
        final Space guide  = (Space)findViewById(R.id.fdl_card_form_guide);
        final ConstraintLayout.LayoutParams srcParams = (ConstraintLayout.LayoutParams)guide.getLayoutParams();

        final int bannerHeight = (int)getResources().getDimension(R.dimen.fdl_banner_height);

        final ConstraintLayout formLayout = (ConstraintLayout)findViewById(R.id.fdl_card_form_layout);

        final ViewTreeObserver treeObserver = formLayout.getViewTreeObserver();

        ViewTreeObserver.OnGlobalLayoutListener treeListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int formHeight = formLayout.getMeasuredHeight();

                int formInitialSize = formHeight + bannerHeight;

                srcParams.topMargin = formInitialSize;

                guide.setLayoutParams(srcParams);
            }
        };

        treeObserver.addOnGlobalLayoutListener(treeListener);

    }

    private void slideCardFormToValue(int value) {
//        final ConstraintLayout formLayout = (ConstraintLayout)findViewById(R.id.fdl_card_form_layout);
//        ConstraintLayout.LayoutParams srcParams = (ConstraintLayout.LayoutParams)formLayout.getLayoutParams();
//
//        int startValue = srcParams.topMargin;
//
//        ValueAnimator animation = ValueAnimator.ofInt(startValue, value);
//        animation.setDuration(500);
//        animation.start();
//        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator updatedAnimation) {
//                int animatedValue = (int)updatedAnimation.getAnimatedValue();
//
//                ConstraintLayout.LayoutParams constraintLayoutParams = (ConstraintLayout.LayoutParams)formLayout.getLayoutParams();
//                constraintLayoutParams.topMargin = animatedValue;
//
//                formLayout.setLayoutParams(constraintLayoutParams);
//            }
//        });
    }

    private void slideCardFormBack() {
        // todo: implement
    }

    private void setLinkButtonType(LinkButtonType type) {
        linkMark.setVisibility(View.INVISIBLE);
        linkTitle.setVisibility(View.INVISIBLE);
        linkProgress.setVisibility(View.INVISIBLE);

        switch(type) {
            case LBT_LINK:
                linkTitle.setVisibility(View.VISIBLE);
                return;
            case LBT_CHECK:
                linkMark.setVisibility(View.VISIBLE);
                return;
            case LBT_LOADER:
                linkProgress.setVisibility(View.VISIBLE);
                return;
        }
    }

    private void resignAllFirstResponders() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        if(imm != null) {
            View focus = getCurrentFocus();

            if(focus != null) {
                IBinder windowToken = focus.getWindowToken();

                if(windowToken != null) {
                    imm.hideSoftInputFromWindow(windowToken, 0);
                }
            }
        }

//        cardNumberEditText.clearFocus();
//        expiryEditText.clearFocus();
    }

    private void blockInput() {
        inputBlocker.setAlpha(0.5f);

        inputBlocker.setClickable(true);

        inputBlocker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resignAllFirstResponders();
            }
        });
    }

    private void unblockInput() {
        inputBlocker.setAlpha(0.0f);

        inputBlocker.setOnClickListener(null);
        inputBlocker.setClickable(false);
    }

    private void disableTOSCheckbox() {
        setBtnTOSCheckBoxSelected(false);
    }

    private void linkCardWithCurrentFormData() {
        String rawCard = cardNumberEditText.getText().toString();
        rawCard = CreditCardUtil.clean(rawCard);

        String country = countryTextView.getText().toString();
        String code = CountriesUtil.countryCodeForCountry(country);

        String expiryStr = expiryEditText.getText().toString();

        String monthStr = ExpiryDateUtil.monthStringFromExpiryString(expiryStr);
        int month = ExpiryDateUtil.monthFromString(monthStr);

        String yearStr = ExpiryDateUtil.yearStringFromExpiryString(expiryStr);
        int year = ExpiryDateUtil.yearFromString(yearStr);
        year = ExpiryDateUtil.yearInFourDigitFormat(year);

        WeakReference<EnterCardDetailsActivity> r = new WeakReference<>(this);

        Fidel.linkCard(new FidelServiceAuthorization(), rawCard, month, year, code, r);
    }

    public void onCardLinked(final String cardId) {
        Toast.makeText(this, "Card linked", Toast.LENGTH_SHORT).show();

        setLinkButtonType(LinkButtonType.LBT_CHECK);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.putExtra(Fidel.FIDEL_LINK_CARD_RESULT_CARD_ID, cardId);

                setResult(Fidel.FIDEL_LINK_CARD_REQUEST_CODE, intent);

                finish();
            }
        }, 1000);
    }

    public void onFailedToLinkCard(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setBtnTOSCheckBoxSelected(false);

                unblockInput();

                setLinkButtonType(LinkButtonType.LBT_LINK);
            }
        }, 1000);
    }
}
