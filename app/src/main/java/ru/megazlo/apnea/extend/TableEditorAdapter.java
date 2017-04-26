package ru.megazlo.apnea.extend;

import android.app.TimePickerDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

import ru.megazlo.apnea.R;
import ru.megazlo.apnea.component.Utils;
import ru.megazlo.apnea.entity.TableApneaRow;

/** Created by iGurkin on 08.09.2016. */
public class TableEditorAdapter extends ArrayAdapter<TableApneaRow> implements View.OnClickListener {

	public TableEditorAdapter(Context context) {
		super(context, R.layout.table_editor_row);
	}

	@NonNull
	@Override
	public View getView(final int position, View v, @NonNull ViewGroup parent) {
		ViewHolder holder;
		if (v == null) {
			holder = new ViewHolder();
			v = LayoutInflater.from(getContext()).inflate(R.layout.table_editor_row, null);
			holder.breathe = (TextView) v.findViewById(R.id.ed_time_breathe);
			holder.hold = (TextView) v.findViewById(R.id.ed_time_hold);
			holder.imgDelete = (ImageView) v.findViewById(R.id.ed_img_delete);
			v.setTag(holder);

			holder.breathe.setOnClickListener(this);
			holder.hold.setOnClickListener(this);

			holder.imgDelete.setOnClickListener(v1 -> {
				ViewHolder vh = (ViewHolder) ((RelativeLayout) v1.getParent()).getTag();
				TableEditorAdapter.this.remove(getItem(vh.pos));
			});
		} else {
			holder = (ViewHolder) v.getTag();
		}
		holder.pos = position;

		TableApneaRow item = this.getItem(position);
		holder.breathe.setText(Utils.formatMS(item.getBreathe()));
		holder.hold.setText(Utils.formatMS(item.getHold()));
		return v;
	}

	public List<TableApneaRow> getAllItems() {
		List<TableApneaRow> rez = new ArrayList<>(getCount());
		for (int i = 0; i < getCount(); i++) {
			rez.add(getItem(i));
		}
		return rez;
	}

	@Override
	public void onClick(View view) {
		final TextView tw = (TextView) view;
		final String time = tw.getText().toString();
		TimePickerDialog tpd = new TimePickerDialog(this.getContext(), (timePicker, minutes, seconds) -> {
			tw.setText(Utils.formatMS(minutes, seconds));
			final int totalSeconds = Utils.getTotalSeconds(minutes, seconds);
			ViewHolder vh = (ViewHolder) ((ViewGroup) view.getParent().getParent()).getTag();
			if (vh.pos < getCount()) {
				if ("B".equals(tw.getTag())) {
					getItem(vh.pos).setBreathe(totalSeconds);
				} else {
					getItem(vh.pos).setHold(totalSeconds);
				}
			}
		}, Utils.getMinutes(time), Utils.getSeconds(time), true);
		tpd.show();
	}

	private static class ViewHolder {
		int pos;
		TextView breathe;
		TextView hold;
		ImageView imgDelete;
	}
}
