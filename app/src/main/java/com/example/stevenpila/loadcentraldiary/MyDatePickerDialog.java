package com.example.stevenpila.loadcentraldiary;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by stevenjefferson.pila on 1/13/2016.
 */
public class MyDatePickerDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener{

    private EditText mEditText;
    private int mYear, mMonth, mDay;

//    static public class MyTimePickerDialog extends DialogFragment implements TimePickerDialog.OnTimeSetListener { // TODO - include time along date or not?
//        private int mHour, mMin;
//        private EditText mEditText2;
//
//        public void setEditText(View view, String newDateTimeStr) {
//            mEditText2 = (EditText) view;
//        }
//
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            Calendar calendar = MyUtility.getDateFromString(mEditText2.getText().toString());
//
//            mHour = MyUtility.getCalendar().get(Calendar.HOUR_OF_DAY);
//            mMin = MyUtility.getCalendar().get(Calendar.MINUTE);
//
//            return new TimePickerDialog(getActivity(), this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(getActivity()));
//        }
//
//        public void onTimeSet(TimePicker view, int hour, int minute) {
//            mEditText2.setText(mEditText2.getText().toString() + " " + hour + ":" + minute);
//        }
//    }

    public void setEditText(View view) {
        mEditText = (EditText) view;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = MyUtility.getDateFromString(mEditText.getText().toString().trim());

        mYear = MyUtility.getCalendar().get(Calendar.YEAR);
        mMonth = MyUtility.getCalendar().get(Calendar.MONTH);
        mDay = MyUtility.getCalendar().get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        return datePickerDialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        if (year > mYear || (month > mMonth && year == mYear) || (day > mDay && year == mYear && month == mMonth))
            return;

        mEditText.setText(MyUtility.getCurrentDate(year, month, day));
    }
}
