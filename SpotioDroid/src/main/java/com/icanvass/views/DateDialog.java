package com.icanvass.views;

/**
 * Created by dev2 on 04.09.14.
 */

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.icanvass.R;

import java.util.Calendar;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TimePicker;

import com.icanvass.R;

import java.util.Calendar;

public class DateDialog extends NMDialog {

    private DatePicker mDatePicker;
    private Button btOk;

    private DateDialogCallback callback;

    private int dayOfMonth;
    private int monthOfYear;
    private int year;

    public interface DateDialogCallback {
        void onPicked(DateDialog view, int year, int monthOfYear, int dayOfMonth);
    }

    public DateDialog(Context context,Calendar calendar, DateDialogCallback callback) {
        super(context);
        this.callback = callback;
        dayOfMonth =calendar.get(Calendar.DAY_OF_MONTH);
        year = calendar.get(Calendar.YEAR);
        monthOfYear = calendar.get(Calendar.MONTH);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initRootView() {
        setContentView(R.layout.dialog_date_picker);
    }

    @Override
    protected void initUIComponents(View rootView) {
        mDatePicker = (DatePicker) rootView.findViewById(R.id.date_picker);
        btOk = (Button) rootView.findViewById(R.id.bt_ok);

        mDatePicker.init(year, monthOfYear, dayOfMonth,
                new DatePicker.OnDateChangedListener() {

                    @Override
                    public void onDateChanged(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                        DateDialog.this.dayOfMonth = dayOfMonth;
                        DateDialog.this.monthOfYear = monthOfYear;
                        DateDialog.this.year = year;
                    }
                }
        );

    }

    @Override
    protected void setListeners() {

        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onPicked(DateDialog.this, year, monthOfYear, dayOfMonth);
                }
            }
        });
    }

    @Override
    protected void loadData() {

    }

}
