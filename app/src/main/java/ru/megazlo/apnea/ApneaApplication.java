package ru.megazlo.apnea;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.IntentFilter;
import android.os.Build;

import org.androidannotations.annotations.EApplication;

import ru.megazlo.apnea.receivers.BluetoothReceiver;
import ru.megazlo.apnea.syservice.BluetoothLeService_;

/** Created by iGurkin on 23.12.2016. */
@EApplication
public class ApneaApplication extends Application {

	private BluetoothReceiver receiver = new BluetoothReceiver();

	public void onCreate() {
		super.onCreate();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
			registerReceiver(receiver, filter);
			BluetoothLeService_.intent(getBaseContext()).start();
		}
	}
}
