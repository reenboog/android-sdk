package com.fidel.fidel;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * @author Alex Gievsky
 */

public class ExpiryDateTextWatcher implements TextWatcher {

    public static final int EXPIRY_TEXT_MAX_LENGTH = 7;

    private EditText textEdit;

    private String lastValidString = "";

    public ExpiryDateTextWatcher(EditText editText) {
        textEdit = editText;
    }

    private void syncLastValidString() {
        lastValidString = textEdit.getText().toString();
    }

    private void revertEditTextValueToALastValidOne() {
        String previousValidString = lastValidString;

        textEdit.setText(lastValidString);

        lastValidString = previousValidString;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        textEdit.setError(null);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        syncLastValidString();
    }

    @Override
    public void afterTextChanged(Editable s) {
        int lastStringLength = lastValidString.length();
        int currentStringLength = textEdit.getText().length();

        boolean deletingChars = currentStringLength < lastStringLength;

        if(deletingChars) {
            //
        } else {
            if(currentStringLength == 1) {
                char ch = s.charAt(0);

                if(Character.isDigit(ch)) {
                    int digit = Character.getNumericValue(ch);

                    if(digit > 1) {
                        textEdit.setText(ExpiryDateUtil.monthNumericStringForMonth(digit) + ExpiryDateUtil.DATE_SPACE);
                        syncLastValidString();
                    }
                } else {
                    revertEditTextValueToALastValidOne();
                }
            } else {
                if(ExpiryDateUtil.isStringInValidFormat(s.toString())) {

                    if(currentStringLength == 2) {
                        textEdit.setText(textEdit.getText() + ExpiryDateUtil.DATE_SPACE);
                        syncLastValidString();
                    }


                } else {
                    // apply an old value
                    revertEditTextValueToALastValidOne();
                }
            }

            textEdit.setSelection(textEdit.getText().length());
        }
    }
}