package com.example.stevenpila.loadcentraldiary;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * Created by Steven on 11/29/2015.
 */
public class MyAutoCompleteTextView extends AutoCompleteTextView {
    private final Drawable m_errorIcon;
    private final Drawable m_normalState;
    private final Drawable m_errorState;

    public MyAutoCompleteTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        m_errorIcon = context.getResources().getDrawable(R.drawable.my_info_error);
        assert m_errorIcon != null;
        m_errorIcon.setBounds(0, 0, (int) (m_errorIcon.getIntrinsicWidth() * 0.7), (int) (m_errorIcon.getIntrinsicHeight() * 0.7));
        m_normalState = context.getResources().getDrawable(R.drawable.my_edit_text_normal);
        m_errorState = context.getResources().getDrawable(R.drawable.my_edit_text_error);

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty())
                    setError(false);
            }
        });

        setNormalState();
    }

    @Override
    public void setError(CharSequence error, Drawable icon) {
        setCompoundDrawables(null, null, icon, null);

        if(icon != null)
            setErrorState();
        else
            setNormalState();
    }
    public void setError(boolean isTrue) {
        if(isTrue)
            setError("", m_errorIcon);
        else
            setError("", null);
    }

    private void setErrorState() {
        // TODO - set error state implementation here..
        setBackground(m_errorState);
    }
    private void setNormalState() {
        // TODO - set normal state implementation here..
        setBackground(m_normalState);
    }

//    private void setCursor(Object drawable) {
//        try {
//            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
//            f.setAccessible(true);
//            f.set(this, drawable);
//        }
//        catch (Exception e) {
//            // TODO - set exception error here...
//        }
//    }
}
