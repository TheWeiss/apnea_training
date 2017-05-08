package ru.megazlo.apnea.frag;

import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.*;
import org.androidannotations.annotations.sharedpreferences.Pref;

import ru.megazlo.apnea.R;
import ru.megazlo.apnea.component.ArcProgress;
import ru.megazlo.apnea.component.NumberPickerDialog;
import ru.megazlo.apnea.service.ApneaPrefs_;

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

	@AfterViews
	void afterView() {

	}

	@Override
	public boolean backPressed() {
		return true;
	}

	@Override
	public void clickByContext(View view) {

	}

	@Override
	public void modifyToContext(View view) {
		view.setVisibility(View.VISIBLE);
		FloatingActionButton fab = (FloatingActionButton) view;
		fab.setImageResource(R.drawable.ic_play);
	}

	@Click({R.id.hold_full_tx, R.id.hold_empty_tx, R.id.exhal_tx, R.id.breathe_tx})
	public void editValueEdge(View view) {
		final Object tag = view.getTag();
		NumberPickerDialog dlg = new NumberPickerDialog(getActivity(), 5/*(int) tag*/, (picker, value) -> {
			Toast.makeText(getActivity(), "time: " + value, Toast.LENGTH_SHORT).show();
		});
		dlg.show();
		/*NumberPicker dlg = new NumberPicker(getActivity(), null, 5);
		dlg.show*/
	}
}
