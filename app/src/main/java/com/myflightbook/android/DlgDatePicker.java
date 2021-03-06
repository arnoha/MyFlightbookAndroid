/*
	MyFlightbook for Android - provides native access to MyFlightbook
	pilot's logbook
    Copyright (C) 2017 MyFlightbook, LLC

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.myflightbook.android;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import com.myflightbook.android.WebServices.UTCDate;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

class DlgDatePicker extends Dialog implements android.view.View.OnClickListener, OnDateChangedListener, OnTimeChangedListener {

    interface DateTimeUpdate {
        void updateDate(int id, Date dt);
    }

    enum datePickMode {LOCALDATEONLY, LOCALDATENULLABLE, UTCDATETIME}

    private Date m_Date = new Date();
    private datePickMode m_Mode;
    int m_id;    // the id of the date/time to update

    static boolean fUseLocalTime = false;

    DateTimeUpdate m_delegate = null;

    private DatePicker dpDate;
    private TimePicker tpTime;

    private void Init(datePickMode dpm, Date dt) {
        setContentView(R.layout.datepicker);
        setMode(dpm);
        setDate(dt);
    }

    DlgDatePicker(Context c, datePickMode dpm, Date dt) {
        super(c);
        Init(dpm, dt);
    }

    DlgDatePicker(Fragment f, datePickMode dpm, Date dt) {
        super(f.getActivity());
        Init(dpm, dt);
    }

    public void onCreate(Bundle savedInstanceState) {
        findViewById(R.id.btnOK).setOnClickListener(this);
        findViewById(R.id.btnDateNow).setOnClickListener(this);
        findViewById(R.id.btnDateNone).setOnClickListener(this);

        GregorianCalendar c = getCalendar();
        c.setTime(m_Date);

        dpDate = (DatePicker) findViewById(R.id.datePicker);
        tpTime = (TimePicker) findViewById(R.id.timePicker);

        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);
        int h = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);

        dpDate.init(y, m, d, this);
        tpTime.setOnTimeChangedListener(this);
        tpTime.setIs24HourView(true);
        tpTime.setCurrentHour(h);
        tpTime.setCurrentMinute(min);
    }

    private void updatePickers(Date dt) {
        GregorianCalendar c = getCalendar();
        c.setTime(dt);

        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);
        int h = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);

        if (dpDate != null)
            dpDate.updateDate(y, m, d);
        if (tpTime != null) {
            tpTime.setCurrentHour(h);
            tpTime.setCurrentMinute(min);
        }
    }

    public void setDate(Date dt) {
        updatePickers(m_Date = dt);
    }

    public Date getDate() {
        return m_Date;
    }

    private void setMode(datePickMode dp) {
        m_Mode = dp;
        findViewById(R.id.timePicker).setVisibility((dp == datePickMode.UTCDATETIME) ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnDateNone).setVisibility((dp == datePickMode.LOCALDATEONLY) ? View.GONE : View.VISIBLE);
    }

    private void NotifyDelegate() {
        if (this.m_delegate != null)
            m_delegate.updateDate(m_id, m_Date);
    }

    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnDateNone:
                m_Date = UTCDate.NullDate();
                NotifyDelegate();
                dismiss();
                break;
            case R.id.btnDateNow:
                setDate(new Date());
                NotifyDelegate();
                break;
            case R.id.btnOK:
                dismiss();
                break;
            default:
                break;
        }
    }

    private GregorianCalendar getCalendar() {
        GregorianCalendar c;
        if (fUseLocalTime || m_Mode == datePickMode.LOCALDATEONLY || m_Mode == datePickMode.LOCALDATENULLABLE)
            c = new GregorianCalendar();
        else
            c = new GregorianCalendar(UTCDate.getUTCTimeZone());

        return c;
    }

    public void onDateChanged(DatePicker vw, int year, int monthOfYear, int dayOfMonth) {
        GregorianCalendar c = getCalendar();
        c.set(year, monthOfYear, dayOfMonth, tpTime.getCurrentHour(), tpTime.getCurrentMinute());

        this.m_Date = c.getTime();

        NotifyDelegate();
    }

    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        GregorianCalendar c = getCalendar();

        c.set(dpDate.getYear(), dpDate.getMonth(), dpDate.getDayOfMonth(), hourOfDay, minute);
        this.m_Date = c.getTime();

        if (m_Mode == datePickMode.UTCDATETIME)
            NotifyDelegate();
    }
}
