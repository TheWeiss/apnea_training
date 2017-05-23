package ru.megazlo.apnea.frag;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.*;

import org.androidannotations.annotations.*;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

import ru.megazlo.apnea.R;
import ru.megazlo.apnea.component.ArcProgress;
import ru.megazlo.apnea.component.Utils;
import ru.megazlo.apnea.receivers.OxiReceiver;
import ru.megazlo.apnea.receivers.SquareFragmentReceiver;
import ru.megazlo.apnea.service.ApneaPrefs_;
import ru.megazlo.apnea.syservice.SquareForeService;
import ru.megazlo.apnea.syservice.SquareForeService_;

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

	@ViewById(R.id.prg_square)
	ProgressBar progressBar;

	@ColorRes(R.color.colorPrimaryUltraLight)
	int defColor;

	FloatingActionButton fab;

	@AfterViews
	void afterView() {
		holdFullTx.setText(pref.squareHoldFull().get().toString());
		holdEmptyTx.setText(pref.squareHoldEmpty().get().toString());
		exhalationTx.setText(pref.squareExhalation().get().toString());
		breatheTx.setText(pref.squareBreathe().get().toString());
		progress.setMax(Utils.getTotalSeconds(pref.squareTotal().get()));
		progress.setProgress(0);
	}

	@Override
	public boolean backPressed() {
		return true;
	}

	@Override
	public void clickByContext(View view) {
		if (SquareForeService_.STATE == SquareForeService_.STOP) {
			SquareForeService_.intent(getActivity().getBaseContext()).start();
			Toast.makeText(getActivity(), "Run", Toast.LENGTH_SHORT).show();
		} else {
			SquareForeService_.intent(getActivity().getBaseContext()).stop();
			Toast.makeText(getActivity(), "Stop", Toast.LENGTH_SHORT).show();
		}
		setViewPlayPause(SquareForeService_.STATE != SquareForeService_.STOP);
	}

	@Override
	public void modifyToContext(View view) {
		view.setVisibility(View.VISIBLE);
		fab = (FloatingActionButton) view;
		setViewPlayPause(SquareForeService_.STATE == SquareForeService_.STOP);
	}

	private void setViewPlayPause(boolean isPlayClick) {
		fab.setImageResource(isPlayClick ? R.drawable.ic_play : R.drawable.ic_stop);
	}

	@Receiver(actions = SquareFragmentReceiver.ACTION_UPDATER)
	void detailReceiver(Intent i) {
		final int totalMax = i.getIntExtra(SquareFragmentReceiver.KEY_TOTAL_MAX, 0);
		final int totalProgress = i.getIntExtra(SquareFragmentReceiver.KEY_TOTAL_PROGRESS, 0);
		final int currentMax = i.getIntExtra(SquareFragmentReceiver.KEY_CURRENT_MAX, 0);
		final int currentProgress = i.getIntExtra(SquareFragmentReceiver.KEY_CURRENT_PROGRESS, 0);
		final boolean ended = i.getBooleanExtra(SquareFragmentReceiver.KEY_ENDED, false);
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
		switch (stt) {
			case HOLD_FULL:
				holdFullTx.setBackgroundResource(R.drawable.textlines);
				break;
			case HOLD_EMPTY:
				holdEmptyTx.setBackgroundResource(R.drawable.textlines);
				break;
			case EXHALATION:
				exhalationTx.setBackgroundResource(R.drawable.textlines);
				break;
			case BREATHE:
				breatheTx.setBackgroundResource(R.drawable.textlines);
				break;
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	@Receiver(actions = OxiReceiver.ACTION)
	void getDataOximeter(Intent intent) {
		final String pulse = intent.getStringExtra(OxiReceiver.PULSE_VAL);
		final String spo = intent.getStringExtra(OxiReceiver.SPO_VAL);
		/*if (spo != null && tvHeartSpo != null) {
			tvHeartSpo.setText(spo);
		}
		if (pulse != null && tvHeartRate != null) {
			tvHeartRate.setText(pulse);
		}*/
	}
}
