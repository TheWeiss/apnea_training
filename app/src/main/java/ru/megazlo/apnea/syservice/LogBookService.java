package ru.megazlo.apnea.syservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.androidannotations.annotations.EService;

/** Created by iGurkin on 26.04.2017. */
@EService
public class LogBookService extends Service {
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
