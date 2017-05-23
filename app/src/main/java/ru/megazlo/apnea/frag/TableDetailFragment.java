package ru.megazlo.apnea.frag;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.*;

import org.androidannotations.annotations.*;
import org.androidannotations.annotations.res.StringRes;

import ru.megazlo.apnea.syservice.ApneaForeService_;
import ru.megazlo.apnea.R;
import ru.megazlo.apnea.component.ArcProgress;
import ru.megazlo.apnea.component.Utils;
import ru.megazlo.apnea.entity.*;
import ru.megazlo.apnea.extend.TableDetailAdapter;
import ru.megazlo.apnea.receivers.*;
import ru.megazlo.apnea.service.ApneaService;

@EFragment(R.layout.table_detail)
public class TableDetailFragment extends Fragment implements FabClickListener {

	private TableApnea tableApnea;

	@StringRes(R.string.total_time)
	String totalTimeStr;

	@Bean
	ApneaService apneaService;

	@ViewById(R.id.arc_progress)
	ArcProgress prg;
	@ViewById(R.id.total_time)
	TextView totalTime;
	@ViewById(R.id.list_row)
	ListView listView;
	@ViewById(R.id.control_pane)
	RelativeLayout buttonPane;
	@ViewById(R.id.tv_heart_spo)
	TextView tvHeartSpo;
	@ViewById(R.id.tv_heart_rate)
	TextView tvHeartRate;

	private void updateViews(int progress) {
		if (tableApnea != null && tableApnea.getRows() != null) {
			updateViews(false, progress);
		}
	}

	private void updateViews(boolean reset, int progress) {
		TableApneaRow cr = null;
		int total = 0;
		for (TableApneaRow r : tableApnea.getRows()) {
			if (reset) {
				r.reset();
			}
			total += r.getHold() + r.getExtHold() + r.getBreathe() + r.getExtBreathe();
			if (r.getState() != RowState.NONE) {
				cr = r;
			}
		}
		if (cr != null) {
			prg.setMax(cr.getState() == RowState.HOLD ? cr.getHold() + cr.getExtHold() : cr.getBreathe() + cr.getExtBreathe());
			prg.setBottomText(getString(cr.getState() == RowState.BREATHE ? R.string.timer_breath_lb : R.string.timer_hold_lb));
		} else if (tableApnea.getRows().size() > 0) {
			prg.setMax(tableApnea.getRows().get(0).getBreathe());
			prg.setBottomText("");
		}

		prg.setProgress(progress);
		totalTime.setText(String.format(totalTimeStr, Utils.formatMS(total)));
	}

	@AfterViews
	void init() {
		final TableDetailAdapter adapter = new TableDetailAdapter(getActivity());
		listView.setAdapter(adapter);
		if (ApneaForeService_.STATE == ApneaForeService_.STOP) {
			tableApnea.setRows(apneaService.getRowsForTable(tableApnea));
			adapter.addAll(tableApnea.getRows());
			updateViews(0);
		}
		setViewPlayPause(ApneaForeService_.STATE == ApneaForeService_.RUN);
	}

	@Click(R.id.img_discard)
	void clickDiscard() {
		sendServiceCommand(ApneaForeReceiver.ACTION_SKIP);
	}

	@Click(R.id.img_stop)
	void clickStop(View view) {
		Snackbar.make(view, R.string.snack_stop_session, Snackbar.LENGTH_LONG).setAction(R.string.ok, v -> {
			ApneaForeService_.intent(getActivity().getBaseContext()).stop();
			setViewPlayPause(false);
		}).show();
	}

	@Click(R.id.img_play)
	void clickPlay() {
		if (ApneaForeService_.STATE == ApneaForeService_.STOP) {
			ApneaForeService_.intent(getActivity().getBaseContext()).extra("table", tableApnea).start();
			setViewPlayPause(true);
		} else if (ApneaForeService_.STATE == ApneaForeService_.PAUSE) {
			sendServiceCommand(ApneaForeReceiver.ACTION_RESUME);
			setViewPlayPause(true);
		}
	}

	@Click(R.id.img_pause)
	void clickPause() {
		sendServiceCommand(ApneaForeReceiver.ACTION_PAUSE);
		setViewPlayPause(false);
	}

	@Click(R.id.img_add_time)
	void clickAddTime() {
		sendServiceCommand(ApneaForeReceiver.ACTION_ADD_TIME);
	}

	private void setViewPlayPause(boolean isPlayClick) {
		final int visibleChild = isPlayClick ? View.VISIBLE : View.GONE;
		for (int i = 0; i < buttonPane.getChildCount(); i++) {
			buttonPane.getChildAt(i).setVisibility(visibleChild);
		}
		buttonPane.findViewById(R.id.img_play).setVisibility(isPlayClick ? View.GONE : View.VISIBLE);
	}

	void sendServiceCommand(String name) {
		Intent tb = new Intent(ApneaForeReceiver.ACTION).putExtra(ApneaForeReceiver.ACTION_TYPE, name);
		getActivity().sendBroadcast(tb);
	}

	public void setTableApnea(TableApnea tableApnea) {
		this.tableApnea = tableApnea;
	}

	@Override
	public void clickByContext(View view) {
	}

	@Override
	public void modifyToContext(View view) {
		view.setVisibility(View.GONE);
	}

	@Override
	public boolean backPressed() {
		return true;
	}

	@Receiver(actions = DetailFragmentReceiver.ACTION_UPDATER)
	void detailReceiver(Intent intent) {
		boolean ended = intent.getBooleanExtra(DetailFragmentReceiver.KEY_ENDED, false);
		if (ended) {
			updateViews(true, 0);
			setViewPlayPause(false);
			return;
		}
		int progress = intent.getIntExtra(DetailFragmentReceiver.KEY_PROGRESS, -1);
		tableApnea = (TableApnea) intent.getSerializableExtra(DetailFragmentReceiver.KEY_TABLE);
		if (listView != null) {
			final TableDetailAdapter adapter = (TableDetailAdapter) listView.getAdapter();
			adapter.clear();
			adapter.addAll(tableApnea.getRows());
			adapter.notifyDataSetChanged();
			if (tableApnea.getRows() != null && tableApnea.getRows().size() > 0) {
				updateViews(progress);
			}
		}
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
