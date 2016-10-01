/*
 * Copyright (C) 2016 Phillip Hsu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.philliphsu.bottomsheettimepickers.timepickers;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.philliphsu.bottomsheettimepickers.R;

/**
 * Created by Phillip Hsu on 7/12/2016.
 *
 */
public class NumpadTimePickerDialog extends BaseTimePickerDialog
        implements NumpadTimePicker.OnInputChangeListener {
    private static final String TAG = "NumpadTimePickerDialog";

    private static final String KEY_IS_24_HOUR_VIEW = "is_24_hour_view";
    private static final String KEY_DIGITS_INPUTTED = "digits_inputted";
    private static final String KEY_AMPM_STATE = "ampm_state";
    private static final String KEY_THEME_DARK = "theme_dark";
    private static final String KEY_THEME_SET_AT_RUNTIME = "theme_set_at_runtime";

    private boolean mIs24HourMode; // TODO: Why do we need this?
    /**
     * The digits stored in the numpad from the last time onSaveInstanceState() was called.
     *
     * Why not have the NumpadTimePicker class save state itself? Because it's a lot more
     * code to do so, as you have to create your own SavedState subclass. Also, we modeled
     * this dialog class on the RadialTimePickerDialog, where the RadialPickerLayout also
     * depends on the dialog to save its state.
     */
    private int[] mInputtedDigits;
    private int mAmPmState = NumpadTimePicker.UNSPECIFIED; // TOneverDO: zero initial value, b/c 0 == AM
    private boolean mThemeDark;
    private boolean mThemeSetAtRuntime;

    private TextView mInputField;
    private NumpadTimePicker mNumpad;

    // TODO: is24HourMode param
    public static NumpadTimePickerDialog newInstance(OnTimeSetListener callback) {
        NumpadTimePickerDialog ret = new NumpadTimePickerDialog();
        // TODO: Do these in initialize()
        ret.setOnTimeSetListener(callback);
        ret.mThemeDark = false;
        ret.mThemeSetAtRuntime = false;
        return ret;
    }

    /**
     * Set a dark or light theme. NOTE: this will only take effect for the next onCreateView.
     */
    public void setThemeDark(boolean dark) {
        mThemeDark = dark;
        mThemeSetAtRuntime = true;
    }

    public boolean isThemeDark() {
        return mThemeDark;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mInputtedDigits = savedInstanceState.getIntArray(KEY_DIGITS_INPUTTED);
            mIs24HourMode = savedInstanceState.getBoolean(KEY_IS_24_HOUR_VIEW);
            mAmPmState = savedInstanceState.getInt(KEY_AMPM_STATE);
            mThemeDark = savedInstanceState.getBoolean(KEY_THEME_DARK);
            mThemeSetAtRuntime = savedInstanceState.getBoolean(KEY_THEME_SET_AT_RUNTIME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mInputField = (TextView) view.findViewById(R.id.input_time);
        mNumpad = (NumpadTimePicker) view.findViewById(R.id.number_grid);

        final FloatingActionButton fab = (FloatingActionButton) mNumpad.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mNumpad.checkTimeValid())
                    return;
                onTimeSet(mNumpad, mNumpad.getHour(), mNumpad.getMinute());
            }
        });

        if (!mThemeSetAtRuntime) {
            mThemeDark = Utils.isDarkTheme(getActivity(), mThemeDark);
        }
        mNumpad.setOnInputChangeListener(this);
        mNumpad.insertDigits(mInputtedDigits); // TOneverDO: before mNumpad.setOnInputChangeListener(this);
        mNumpad.setAmPmState(mAmPmState);
        // Show the cursor immediately
//        mInputField.requestFocus();
        //updateInputText(""); // Primarily to disable 'OK'

        // Prepare colors
        int accentColor = Utils.getThemeAccentColor(getContext());
        int lightGray = ContextCompat.getColor(getContext(), R.color.light_gray);
        int darkGray = ContextCompat.getColor(getContext(), R.color.dark_gray);
        int white = ContextCompat.getColor(getContext(), android.R.color.white);

        // Set background color of entire view
        view.setBackgroundColor(mThemeDark? darkGray : white);

        TextView inputTime = (TextView) view.findViewById(R.id.input_time);
        inputTime.setBackgroundColor(mThemeDark? lightGray : accentColor);
        inputTime.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));

        mNumpad.setTheme(getContext()/*DO NOT GIVE THE APPLICATION CONTEXT, OR ELSE THE NUMPAD
        CAN'T GET THE CORRECT ACCENT COLOR*/, mThemeDark);

        return view;
    }

    @Override
    protected int contentLayout() {
        return R.layout.dialog_time_picker_numpad;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mNumpad != null) {
            outState.putIntArray(KEY_DIGITS_INPUTTED, mNumpad.getDigits());
            outState.putBoolean(KEY_IS_24_HOUR_VIEW, mIs24HourMode);
            outState.putInt(KEY_AMPM_STATE, mNumpad.getAmPmState());
            outState.putBoolean(KEY_THEME_DARK, mThemeDark);
            outState.putBoolean(KEY_THEME_SET_AT_RUNTIME, mThemeSetAtRuntime);
        }
    }

    @Override
    public void onDigitInserted(String newStr) {
        updateInputText(newStr);
    }

    @Override
    public void onDigitDeleted(String newStr) {
        updateInputText(newStr);
    }

    @Override
    public void onDigitsCleared() {
        updateInputText("");
    }

    @Override
    public void onInputDisabled() {
        // No implementation.
    }

    private void updateInputText(String inputText) {
        TimeTextUtils.setText(inputText, mInputField);
    }
}
