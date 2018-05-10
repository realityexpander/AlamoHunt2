package com.realityexpander.alamohunt;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by CDA on 5/3/18.
 */

    // This allows for the custom back button behavior
    // Drops the soft keyboard in order to see the AutoCompleteTextView drop-down

public class CustomBackAutoCompleteTextView extends android.support.v7.widget.AppCompatAutoCompleteTextView  {

    public CustomBackAutoCompleteTextView(Context context) {
        super(context);
    }

    public CustomBackAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomBackAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isPopupShowing()) {
            InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

            if(inputManager.hideSoftInputFromWindow(findFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS)){
                return true;
            }
        }

        return super.onKeyPreIme(keyCode, event);
    }

}