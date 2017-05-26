package ru.megazlo.apnea.receivers;

import android.content.BroadcastReceiver;

/** Created by iGurkin on 24.05.2017. */
public abstract class SquareForeReceiver extends BroadcastReceiver {
	public static final String ACTION = "ru.megazlo.apnea.SQUARE_FORE_ACTION";
	public static final String ACTION_TYPE = "ru.megazlo.apnea.SQUARE_ACTION_TYPE";
	public static final String ACTION_PAUSE = "ru.megazlo.apnea.SQUARE_PAUSE_CURRENT";
	public static final String ACTION_RESUME = "ru.megazlo.apnea.SQUARE_RESUME_CURRENT";
	public static final String ACTION_ADD_TIME = "ru.megazlo.apnea.SQUARE_ADD_TIME_CURRENT";
}
