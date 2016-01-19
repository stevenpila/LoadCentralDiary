package com.example.stevenpila.loadcentraldiary;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

/**
 * Created by stevenjefferson.pila on 1/18/2016.
 */
public class MyDateRangeSpinner extends Spinner {
    public MyDateRangeSpinner(Context context) {
        super(context);
    }

    public MyDateRangeSpinner(Context context, int mode) {
        super(context, mode);
    }

    public MyDateRangeSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyDateRangeSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setSelection(int position, boolean animate) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position, animate);
        if (sameSelected) {
            // Spinner does not call the OnItemSelectedListener if the same item is selected, so do it manually now
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }

    @Override
    public void setSelection(int position) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position);
        if (sameSelected) {
            // Spinner does not call the OnItemSelectedListener if the same item is selected, so do it manually now
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }
}
