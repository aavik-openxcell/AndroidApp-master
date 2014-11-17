package com.icanvass.views;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TimePicker;

import com.icanvass.R;

import java.util.Calendar;

public class DateTimeDialog extends NMDialog {

    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private Button btOk;

    private DateTimeDialogCallback callback;

    private int dayOfMonth;
    private int monthOfYear;
    private int year;
    private int hours;
    private int minuteInHour;

    public interface DateTimeDialogCallback {
        void onPicked(DateTimeDialog view, int year, int monthOfYear,
                      int dayOfMonth, int hourOfDay, int minute);
    }

    public DateTimeDialog(Context context,Calendar calendar, DateTimeDialogCallback callback) {
        super(context);
        this.callback = callback;
        dayOfMonth =calendar.get(Calendar.DAY_OF_MONTH);
        year = calendar.get(Calendar.YEAR);
        monthOfYear = calendar.get(Calendar.MONTH);
        hours = calendar.get(Calendar.HOUR_OF_DAY);
        minuteInHour = calendar.get(Calendar.MINUTE);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initRootView() {
        setContentView(R.layout.dialog_date_time_picker);
    }

    @Override
    protected void initUIComponents(View rootView) {
        mDatePicker = (DatePicker) rootView.findViewById(R.id.date_picker);
        mTimePicker = (TimePicker) rootView.findViewById(R.id.timePicker);
        btOk = (Button) rootView.findViewById(R.id.bt_ok);

        mDatePicker.init(year, monthOfYear, dayOfMonth,
                new OnDateChangedListener() {

                    @Override
                    public void onDateChanged(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                        DateTimeDialog.this.dayOfMonth = dayOfMonth;
                        DateTimeDialog.this.monthOfYear = monthOfYear;
                        DateTimeDialog.this.year = year;
                    }
                }
        );


        mTimePicker.setCurrentHour(hours);
        mTimePicker.setCurrentMinute(minuteInHour);
    }

    @Override
    protected void setListeners() {

        btOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int hour = mTimePicker.getCurrentHour();
                int minute = mTimePicker.getCurrentMinute();
                if (callback != null) {
                    callback.onPicked(DateTimeDialog.this, year, monthOfYear,
                            dayOfMonth, hour, minute);
                }
            }
        });
    }

    @Override
    protected void loadData() {

    }

}
