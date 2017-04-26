package ru.megazlo.apnea.component;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import java.util.List;

import ru.megazlo.apnea.R;

/** Created by iGurkin on 26.04.2017. */
public class NumberPickerDialog extends AlertDialog implements DialogInterface.OnClickListener {

	private OnValueSetListener mListener;

	private NumberPicker picker;

	private int mInitialValue;

	public NumberPickerDialog(Context context, int value, NumberPickerDialog.OnValueSetListener listener) {
		super(context, 0);
		mInitialValue = value;
		mListener = listener;
		init(context);
	}

	public NumberPickerDialog(Context context, int themeResId, int value, NumberPickerDialog.OnValueSetListener listener) {
		super(context, themeResId);
	}

	private void init(Context context) {
		setButton(BUTTON_POSITIVE, context.getText(android.R.string.ok), this);
		setButton(BUTTON_NEGATIVE, context.getText(android.R.string.cancel), (OnClickListener) null);
		setTitle(context.getText(R.string.app_name));

		LinearLayout layout = new LinearLayout(context);
		LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setGravity(Gravity.CENTER);
		layout.setLayoutParams(parms);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

		picker = new NumberPicker(new ContextThemeWrapper(getContext(), R.style.NumberPickerTextColorStyle));
		picker.setMinValue(2);
		picker.setMaxValue(30);
		picker.setValue(mInitialValue);

		layout.addView(picker, layoutParams);
		setView(layout);
	}

	@Override
	public void onClick(DialogInterface dialogInterface, int i) {
		if (mListener != null) {
			mListener.onValueSet(picker, picker.getValue());
		}
	}

	@Override
	public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, @Nullable Menu menu, int deviceId) {

	}

	public interface OnValueSetListener {
		void onValueSet(NumberPicker picker, int value);
	}
}
