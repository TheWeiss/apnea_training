package ru.megazlo.apnea.frag;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.androidannotations.annotations.*;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.Timer;
import java.util.TimerTask;

import ru.megazlo.apnea.R;
import ru.megazlo.apnea.component.ArcProgress;
import ru.megazlo.apnea.component.Utils;
import ru.megazlo.apnea.receivers.OxiReceiver;
import ru.megazlo.apnea.service.ApneaPrefs_;

@EFragment(R.layout.record_view)
public class RecordFragment extends Fragment implements FabClickListener {

	private Timer timer;

	@ColorRes
	int colorAccent;

	@Pref
	ApneaPrefs_ pref;

	@ViewById(R.id.arc_record)
	ArcProgress progress;
	@ViewById(R.id.start_record)
	Button button;
	@ViewById(R.id.tv_rec_spo)
	TextView tvHeartSpo;
	@ViewById(R.id.tv_rec_heart)
	TextView tvHeartRate;

	@AfterViews
	void afterView() {
		initProgress();
	}

	@Click(R.id.start_record)
	void startButtonClick() {
		if (timer == null) {
			initProgress();
			timer = new Timer();
			timer.scheduleAtFixedRate(new RecordTask(), 0, 1000);
			setButtonState(R.drawable.ic_stop_white);
		} else {
			stopTimer();
			final int oldBest = Utils.getTotalSeconds(pref.bestRecord().get());
			if (progress.getMax() > oldBest) {
				new AlertDialog.Builder(getActivity()).setMessage(R.string.dlg_new_rec).setCancelable(false).setNegativeButton(R.string.cancel, null)
						.setPositiveButton(R.string.ok, (dialog, which) -> pref.edit().bestRecord().put(Utils.formatMS(progress.getMax())).apply()).show();

			}
		}
	}

	private void stopTimer() {
		stopTimer(true);
	}

	private void stopTimer(boolean changeState) {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (changeState) {
			setButtonState(R.drawable.ic_play_white);
		}
	}

	private void setButtonState(int resId) {
		Drawable img = getActivity().getResources().getDrawable(resId);
		button.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
		if (resId == R.drawable.ic_play_white) {
			button.setText(R.string.bt_text_start);
		} else {
			button.setText(R.string.bt_text_stop);
		}
	}

	private void initProgress() {
		progress.setProgress(0);
		final int totalSeconds = Utils.getTotalSeconds(pref.bestRecord().get());
		progress.setMax(totalSeconds);
	}

	@Override
	public void clickByContext(View view) {
	}

	@Override
	public void onDestroy() {
		if (timer != null) {
			stopTimer(false);
		}
		super.onDestroy();
	}

	@Override
	public void modifyToContext(View view) {
		view.setVisibility(View.GONE);
	}

	@Override
	public boolean backPressed() {
		if (timer != null) {
			stopTimer();
			return false;
		}
		return true;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	@Receiver(actions = OxiReceiver.ACTION)
	void getDataOximeter(Intent intent) {
		final String pulse = intent.getStringExtra(OxiReceiver.PULSE_VAL);
		final String spo = intent.getStringExtra(OxiReceiver.SPO_VAL);
		if (spo != null && tvHeartSpo != null) {
			tvHeartSpo.setText(spo);
		}
		if (pulse != null && tvHeartRate != null) {
			tvHeartRate.setText(pulse);
		}
	}

	class RecordTask extends TimerTask {
		@Override
		public void run() {
			getActivity().runOnUiThread(() -> {
				final int totalSeconds = Utils.getTotalSeconds(pref.bestRecord().get());
				if (progress != null) {
					if (progress.getProgress() < totalSeconds) {
						progress.setProgress(progress.getProgress() + 1);
					} else if (progress.getProgress() == totalSeconds) {
						if (progress.getUnfinishedStrokeColor() != colorAccent) {
							progress.setUnfinishedStrokeColor(colorAccent);
						}
						progress.setMax(progress.getMax() + 1);
					}
				}
			});
		}
	}
}
