package ru.megazlo.apnea.syservice;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.*;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.*;

import ru.megazlo.apnea.R;
import ru.megazlo.apnea.receivers.OxiReceiver;
import ru.megazlo.apnea.service.ApneaPrefs_;

/** Created by iGurkin on 04.10.2016. */
@EService
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothLeService extends Service {
	private final static String TAG = BluetoothLeService.class.getSimpleName();

	@Pref
	ApneaPrefs_ pref;
	@SystemService
	BluetoothManager bluetoothManager;

	@StringRes(R.string.lb_empty_val)
	String emptyVal;

	private final static UUID CONF_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	private final static String CHAR_UUID = "cdeacb81-5235-4c07-8846-93a37ee6b86d";
	private final static String SERVICE_UUID = "cdeacb80-5235-4c07-8846-93a37ee6b86d";

	private BluetoothGatt bluetoothGatt;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
		if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled() || pref.deviceAddress().get().isEmpty()) {
			stopSelf();
			return Service.START_NOT_STICKY;
		}
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(pref.deviceAddress().get());
		bluetoothGatt = device.connectGatt(getBaseContext().getApplicationContext(), true, bleGattCallback);
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (bluetoothGatt != null) {
			bluetoothGatt.disconnect();
			bluetoothGatt.close();
		}
	}

	private final BluetoothGattCallback bleGattCallback = new BluetoothGattCallback() {

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic chr) {
			if (CHAR_UUID.equals(chr.getUuid().toString())) {
				final byte[] vl = chr.getValue();
				if (vl[0] == -127) {
					final int pulse = vl[1] & 0xFF;
					//Log.d(TAG, "K " + pulse);//pulse
					final int spo = vl[2] & 0xFF;
					//Log.d(TAG, "M " + spo);//SpO2
					//Log.d(TAG, "L " + (vl[3] & 0xFF));//piB

					Intent tb = new Intent(OxiReceiver.ACTION);
					tb.putExtra(OxiReceiver.PULSE_VAL, pulse == 255 ? emptyVal : Integer.toString(pulse));
					tb.putExtra(OxiReceiver.SPO_VAL, spo == 127 ? emptyVal : Integer.toString(spo));
					getApplication().sendBroadcast(tb);// урааа нахуй, победил!!!
				}
			}
		}

		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				Log.i(TAG, "Attempting to start service discovery:" + gatt.discoverServices());
			}
			// this will get called when a device connects or disconnects
		}

		@Override
		public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
			List<BluetoothGattService> services = gatt.getServices();
			Log.d(TAG, "onServicesDiscovered");
			for (BluetoothGattService service : services) {
				if (SERVICE_UUID.equals(service.getUuid().toString())) {
					for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
						if (CHAR_UUID.equals(characteristic.getUuid().toString())) {
							gatt.setCharacteristicNotification(characteristic, true);

							final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CONF_UUID);
							descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
							gatt.writeDescriptor(descriptor);
						}
					}
				}
			}
		}
	};
}
