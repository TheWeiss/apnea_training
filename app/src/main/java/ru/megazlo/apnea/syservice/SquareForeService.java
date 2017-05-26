package ru.megazlo.apnea.syservice;

import android.app.*;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import org.androidannotations.annotations.*;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.io.IOException;
import java.util.TimerTask;

import ru.megazlo.apnea.MainAct_;
import ru.megazlo.apnea.R;
import ru.megazlo.apnea.component.Utils;
import ru.megazlo.apnea.receivers.SquareForeReceiver;
import ru.megazlo.apnea.receivers.SquareFragmentReceiver;
import ru.megazlo.apnea.service.AlertService;
import ru.megazlo.apnea.service.ApneaPrefs_;

/** Created by iGurkin on 19.05.2017. */
@EService
public class SquareForeService extends AbstractForeService {

	private final static int ONGOING_NOTIFICATION_ID = 251665162;

	private int totalProgress;
	private int totalMax;
	private int currentMax;
	private int currentProgress;
	private State state = State.BREATHE;

	@Pref
	ApneaPrefs_ pref;
	@Bean
	AlertService alertService;
	@SystemService
	NotificationManager notificationManager;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("Test", "Service: onStartCommand");
		PendingIntent pi = getPendingIntent(MainAct_.class);

		builder = new Notification.Builder(getApplicationContext()).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_ico))
				.setSmallIcon(R.drawable.ic_lungs).setContentTitle(getText(R.string.app_name)).setContentIntent(pi)
				.setProgress(getTotalMax(), 0, false)//устанавливаем время
				.setOngoing(true).setAutoCancel(false).setWhen(System.currentTimeMillis());
		startForeground(ONGOING_NOTIFICATION_ID, builder.getNotification());

		startTimer();
		return START_NOT_STICKY;
	}

	private int getTotalMax() {
		return totalMax;
	}

	private void startTimer() {
		totalMax = Utils.getTotalSeconds(pref.squareTotal().get());
		currentMax = pref.squareBreathe().get();
		timer.scheduleAtFixedRate(new SquareTimerTask(), 0, 1000);
		STATE = RUN;
		registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}

	private void stopTimer() {
		updateFragmentUi(true);
		unregisterReceiver(receiver);
		STATE = STOP;
		try {
			timer.cancel();
		} catch (Exception ignored) {
		}
	}

	@Override
	protected void setExtraOnPendingIntent(Intent intent) {
	}

	@Override
	public void onDestroy() {
		stopTimer();
		super.onDestroy();
		try {
			alertService.close();
		} catch (IOException ignored) {
		}
	}

	private void updateProgress() {
		if (ApneaForeService.STATE == ApneaForeService.RUN) {
			totalProgress++;
			currentProgress++;
			updateFragmentUi(false);
			updateNotificationUi();
			if (totalProgress >= getTotalMax()) {
				Intent mainInt = new Intent(getApplicationContext(), MainAct_.class);
				mainInt.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
				mainInt.putExtra(IS_ALERT_SERIES_END, true);
				startActivity(mainInt);
				stopTimer();
			}
			if (currentProgress >= currentMax) {
				currentProgress = 0;
				switchState();
			}
		} else {// здесь видимо пауза
			updateFragmentUi(false);
		}
	}

	private void switchState() {
		if (state == State.BREATHE) {
			state = State.HOLD_FULL;
			currentMax = pref.squareHoldFull().get();
		} else if (state == State.HOLD_FULL) {
			state = State.EXHALATION;
			currentMax = pref.squareExhalation().get();
		} else if (state == State.EXHALATION) {
			state = State.HOLD_EMPTY;
			currentMax = pref.squareHoldEmpty().get();
		} else if (state == State.HOLD_EMPTY) {
			state = State.BREATHE;
			currentMax = pref.squareBreathe().get();
		} else {
			throw new RuntimeException("Square State not found");
		}
	}

	private void updateFragmentUi(boolean ended) {
		Intent tb = new Intent(SquareFragmentReceiver.ACTION_UPDATER);
		tb.putExtra(SquareFragmentReceiver.KEY_TOTAL_MAX, getTotalMax());
		tb.putExtra(SquareFragmentReceiver.KEY_TOTAL_PROGRESS, totalProgress);
		tb.putExtra(SquareFragmentReceiver.KEY_CURRENT_MAX, currentMax);
		tb.putExtra(SquareFragmentReceiver.KEY_CURRENT_PROGRESS, currentProgress);
		tb.putExtra(SquareFragmentReceiver.KEY_ENDED, ended);
		tb.putExtra(SquareFragmentReceiver.KEY_STATE, state);
		getApplication().sendBroadcast(tb);
	}

	private void updateNotificationUi() {
		final int totalMax = getTotalMax();
		final String arg = Utils.formatMS(totalMax - totalProgress);
		builder.setProgress(totalMax, totalMax - totalProgress, false)/*.setContentIntent(getPendingIntent(this.getClass()))*/;

		/*if (currentItem.getState() == RowState.BREATHE) {
			builder.setContentText(getString(R.string.notif_breathe, arg));
		} else if (currentItem.getState() == RowState.HOLD) {
			builder.setContentText(getString(R.string.notif_hold, arg));
		}*/
		notificationManager.notify(ONGOING_NOTIFICATION_ID, builder.getNotification());
	}

	@Receiver(actions = SquareForeReceiver.ACTION)
	void serviceCommandReceiver(Intent intent) {
		final String action = intent.getStringExtra(SquareForeReceiver.ACTION_TYPE);
		if (SquareForeReceiver.ACTION_PAUSE.equals(action)) {
			STATE = PAUSE;
		} else if (SquareForeReceiver.ACTION_RESUME.equals(action)) {
			STATE = RUN;
		} else if (SquareForeReceiver.ACTION_ADD_TIME.equals(action)) {
			totalMax += 30;
			Toast.makeText(getApplicationContext(), R.string.time_added, Toast.LENGTH_SHORT).show();
		}
	}

	private class SquareTimerTask extends TimerTask {
		@Override
		public void run() {
			updateProgress();
		}
	}

	public enum State {
		HOLD_FULL,
		HOLD_EMPTY,
		EXHALATION,
		BREATHE
	}
}
