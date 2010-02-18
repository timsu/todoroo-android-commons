/**
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.androidcommons.widget;

import java.text.Format;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.todoroo.astrid.R;

/**
 * Picks a Date Time by way of a single button. This widget can return
 * null if the user chooses to delete the set value.
 * <p>
 * There are extra settings on this date button that can be accessed with
 * setters, such as whether the user can "un-pick" a value, whether
 * the button also includes time, etc.
 *
 * @author Tim Su <tim@todoroo.com>
 *
 */
public class DateButton implements OnTimeSetListener,
        OnDateSetListener, View.OnClickListener {

    private final Format dateFormatter;
    private final Format timeFormatter;

    protected final Context context;
    protected Button button;
    protected Date date, intermediateDate;

    /** whether minutes default to 0 */
    protected boolean clearMinutes = false;

    /** whether user can "unset" the date */
    protected boolean canBeNull = true;

    /** whether this component has a time */
    protected boolean hasTime = true;

    /**
     * Public Constructor
     *
     * @param context
     * @param button
     * @param initialValue
     */
    public DateButton(Context context, Button button, Date initialValue) {
        this(context);

        this.button = button;

        if(button != null)
            button.setOnClickListener(this);

        setDate(initialValue);
    }

    // --- getters and setters

    public Date getDate() {
        return date;
    }

    /** Initialize the components for the given date field */
    public void setDate(Date newDate) {
        if(newDate == null) {
            date = null;
        } else {
            this.date = new Date(newDate.getTime());
        }

        updateButton();
    }

    /**
     * Sets the clear minutes setting - whether minutes should always be set to
     * 00 in the dialog.
     *
     * @param clearMinutes
     */
    public void setClearMinutes(boolean clearMinutes) {
        this.clearMinutes = clearMinutes;
    }

    /**
     * Sets the can be null setting - whether this object can remain unset
     * @param canBeNull
     */
    public void setCanBeNull(boolean canBeNull) {
        this.canBeNull = canBeNull;
    }

    /**
     * Sets whether this button allows users to pick times
     * @param hasTime
     */
    public void setHasTime(boolean hasTime) {
        this.hasTime = hasTime;
    }

    public Button getButton() {
        return button;
    }

    // --- internal state

    protected DateButton(Context context) {
        this.context = context;

        dateFormatter = DateFormat.getMediumDateFormat(context);
        timeFormatter = DateFormat.getTimeFormat(context);
    }

    public void updateButton() {
        if(button != null) {
            if(date == null) {
                button.setText(R.string.WID_dateButtonUnset);
            } else if(hasTime) {
                String label = context.getString(R.string.WID_dateButtonLabel).
                replace("$D", dateFormatter.format(date)). //$NON-NLS-1$
                replace("$T", timeFormatter.format(date)); //$NON-NLS-1$
                button.setText(label);
            } else {
                button.setText(dateFormatter.format(date));
            }
        }
    }

    /**
     * @return true if system is in 24 hour mode, false otherwise
     */
    protected boolean isSystem24Hour() {
        String timeFormatS = android.provider.Settings.System.getString(
                context.getContentResolver(), Settings.System.TIME_12_24);
        return !(timeFormatS == null || timeFormatS.equals("12")); //$NON-NLS-1$
    }

    // --- event handlers

    public void onDateSet(DatePicker view, int year, int month, int monthDay) {
        intermediateDate.setYear(year - 1900);
        intermediateDate.setMonth(month);
        intermediateDate.setDate(monthDay);

        if(clearMinutes)
            intermediateDate.setMinutes(0);

        if(!hasTime) {
            date = new Date(intermediateDate.getTime());
            updateButton();
            return;
        }

        new TimePickerDialog(context, this, intermediateDate.getHours(),
                intermediateDate.getMinutes(), isSystem24Hour()).show();
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        intermediateDate.setHours(hourOfDay);
        intermediateDate.setMinutes(minute);
        date = new Date(intermediateDate.getTime());
        updateButton();
    }

    public void onClick(View v) {
        if(date == null)
            intermediateDate = new Date();
        else
            intermediateDate = new Date(date.getTime());

        intermediateDate.setSeconds(0);
        DatePickerDialog datePicker = new DatePickerDialog(context, this, 1900 +
                intermediateDate.getYear(), intermediateDate.getMonth(), intermediateDate.getDate());
        if(canBeNull) {
            datePicker.setButton(Dialog.BUTTON_NEUTRAL, context.getString(R.string.WID_disableButton),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            date = null;
                            updateButton();
                        }
                    });
        }
        datePicker.show();
    }
}