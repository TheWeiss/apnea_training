package ru.megazlo.apnea.syservice;

import android.app.*;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Timer;

import ru.megazlo.apnea.receivers.ScreenOffReceiver;

/** Created by iGurkin on 23.05.2017. */

public abstract class AbstractForeService extends Service {

	public static final int STOP = 0;
	public static final int RUN = 1;
	public static final int PAUSE = 2;
	public static int STATE = STOP;

	protected Notification.Builder builder;
	protected Timer timer = new Timer();
	protected ScreenOffReceiver receiver = new ScreenOffReceiver();

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	protected PendingIntent getPendingIntent(Class clazz) {
		Intent intent = new Intent(getApplicationContext(), clazz);
		intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		setExtraOnPendingIntent(intent);
		return PendingIntent.getActivity(getApplicationContext(), 651651, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	protected abstract void setExtraOnPendingIntent(Intent intent);
}
