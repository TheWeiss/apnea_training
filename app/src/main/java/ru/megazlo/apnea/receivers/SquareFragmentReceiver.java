package ru.megazlo.apnea.receivers;

import android.content.BroadcastReceiver;

/** Created by iGurkin on 23.05.2017. */

public abstract class SquareFragmentReceiver extends BroadcastReceiver {
	public final static String ACTION_UPDATER = "ru.megazlo.apnea.APNEA_SQUARE_UPDATE";

	public final static String KEY_TOTAL_MAX = "KEY_TOTAL_MAX";
	public final static String KEY_TOTAL_PROGRESS = "KEY_TOTAL_PROGRESS";
	public final static String KEY_CURRENT_MAX = "KEY_CURRENT_MAX";
	public final static String KEY_CURRENT_PROGRESS = "KEY_CURRENT_PROGRESS";
	public final static String KEY_ENDED = "KEY_ENDED";
	public final static String KEY_STATE = "KEY_STATE";
}
