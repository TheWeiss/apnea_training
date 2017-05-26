package ru.megazlo.apnea.frag;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.*;

import org.androidannotations.annotations.*;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

import ru.megazlo.apnea.R;
import ru.megazlo.apnea.component.ArcProgress;
import ru.megazlo.apnea.component.Utils;
import ru.megazlo.apnea.receivers.*;
import ru.megazlo.apnea.service.ApneaPrefs_;
import ru.megazlo.apnea.syservice.*;

/** Created by iGurkin on 26.04.2017. */
@EFragment(R.layout.square_view)
public class SquareFragment extends Fragment implements FabClickListener {

	@Pref
	ApneaPrefs_ pref;

	@ViewById(R.id.arc_square_total)
	ArcProgress progress;

	@ViewById(R.id.hold_full_tx)
	TextView holdFullTx;
	@ViewById(R.id.hold_empty_tx)
	TextView holdEmptyTx;
	@ViewById(R.id.exhal_tx)
	TextView exhalationTx;
	@ViewById(R.id.breathe_tx)
	TextView breatheTx;
	@ViewById(R.id.tv_sq_step_name)
	TextView stepName;

	@ViewById(R.id.tv_heart_spo)
	TextView tvHeartSpo;
	@ViewById(R.id.tv_heart_rate)
	TextView tvHeartRate;
	@ViewById(R.id.control_pane)
	RelativeLayout buttonPane;

	@ViewById(R.id.prg_square)
	ProgressBar progressBar;

	@ColorRes(R.color.colorPrimaryUltraLight)
	int defColor;

	@StringRes(R.string.sqr_sec_format)
	String seconds;

	@AfterViews
	void afterView() {
		holdFullTx.setText(String.format(seconds, pref.squareHoldFull().get()));
		holdEmptyTx.setText(String.format(seconds, pref.squareHoldEmpty().get()));
		exhalationTx.setText(String.format(seconds, pref.squareExhalation().get()));
		breatheTx.setText(String.format(seconds, pref.squareBreathe().get()));
		progress.setMax(Utils.getTotalSeconds(pref.squareTotal().get()));
		progress.setProgress(0);
		setViewPlayPause(SquareForeService_.STATE == SquareForeService_.RUN);
	}

	@Override
	public boolean backPressed() {
		return false;
	}

	@Override
	public void clickByContext(View view) {
	}

	@Override
	public void modifyToContext(View view) {
		view.setVisibility(View.GONE);
	}

	private void setViewPlayPause(boolean isPlayClick) {
		final int visibleChild = isPlayClick ? View.VISIBLE : View.GONE;
		for (int i = 0; i < buttonPane.getChildCount(); i++) {
			buttonPane.getChildAt(i).setVisibility(visibleChild);
		}
		buttonPane.findViewById(R.id.img_play).setVisibility(isPlayClick ? View.GONE : View.VISIBLE);
	}

	@Click(R.id.img_play)
	void clickPlay() {
		if (SquareForeService_.STATE == SquareForeService_.STOP) {
			SquareForeService_.intent(getActivity().getBaseContext()).start();
			setViewPlayPause(true);
		} else if (SquareForeService_.STATE == SquareForeService_.PAUSE) {
			sendServiceCommand(SquareForeReceiver.ACTION_RESUME);
			setViewPlayPause(true);
		}
	}

	@Click(R.id.img_stop)
	void clickStop(View view) {
		Snackbar.make(view, R.string.snack_stop_session, Snackbar.LENGTH_LONG).setAction(R.string.ok, v -> {
			SquareForeService_.intent(getActivity().getBaseContext()).stop();
			setViewPlayPause(false);
		}).show();
	}

	@Click(R.id.img_pause)
	void clickPause() {
		sendServiceCommand(SquareForeReceiver.ACTION_PAUSE);
		setViewPlayPause(false);
	}

	@Click(R.id.img_add_time)
	void clickAddTime() {
		sendServiceCommand(SquareForeReceiver.ACTION_ADD_TIME);
	}

	void sendServiceCommand(String name) {
		Intent tb = new Intent(SquareForeReceiver.ACTION).putExtra(SquareForeReceiver.ACTION_TYPE, name);
		getActivity().sendBroadcast(tb);
	}

	@Receiver(actions = SquareFragmentReceiver.ACTION_UPDATER)
	void detailReceiver(Intent i) {
		final int totalMax = i.getIntExtra(SquareFragmentReceiver.KEY_TOTAL_MAX, 0);
		final int totalProgress = i.getIntExtra(SquareFragmentReceiver.KEY_TOTAL_PROGRESS, 0);
		final int currentMax = i.getIntExtra(SquareFragmentReceiver.KEY_CURRENT_MAX, 0);
		final int currentProgress = i.getIntExtra(SquareFragmentReceiver.KEY_CURRENT_PROGRESS, 0);
		final boolean ended = i.getBooleanExtra(SquareFragmentReceiver.KEY_ENDED, false);
		if (ended) {
			afterView();
			updateViewByState(SquareForeService.State.BREATHE);
			return;
		}
		final SquareForeService.State stt = (SquareForeService.State) i.getSerializableExtra(SquareFragmentReceiver.KEY_STATE);
		progress.setMax(totalMax);
		progress.setProgress(totalProgress);
		progressBar.setMax(currentMax);
		progressBar.setProgress(currentProgress);
		updateViewByState(stt);
	}

	private void updateViewByState(SquareForeService.State stt) {
		holdFullTx.setBackgroundColor(defColor);
		holdEmptyTx.setBackgroundColor(defColor);
		exhalationTx.setBackgroundColor(defColor);
		breatheTx.setBackgroundColor(defColor);
		choiceText(stt).setBackgroundResource(R.drawable.textlines);
		progress.setBottomText(stepName.getText().toString());
	}

	private TextView choiceText(SquareForeService.State stt) {
		switch (stt) {
			case HOLD_FULL:
				stepName.setText(R.string.sqr_state_hold);
				return holdFullTx;
			case HOLD_EMPTY:
				stepName.setText(R.string.sqr_state_hold);
				return holdEmptyTx;
			case EXHALATION:
				stepName.setText(R.string.sqr_state_exhale);
				return exhalationTx;
			case BREATHE:
				stepName.setText(R.string.sqr_state_breathe);
				return breatheTx;
		}
		return null;
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
}
