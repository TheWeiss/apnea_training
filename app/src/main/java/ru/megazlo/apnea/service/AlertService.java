package ru.megazlo.apnea.service;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import org.androidannotations.annotations.*;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import ru.megazlo.apnea.R;
import ru.megazlo.apnea.entity.RowState;

@EBean
public class AlertService implements TextToSpeech.OnInitListener, Closeable {

	private static final int VIBRATE_TIME = 200;

	private TextToSpeech tts;

	@SystemService
	Vibrator vibrator;

	@RootContext
	Context context;

	@Pref
	ApneaPrefs_ pref;

	@AfterInject
	void initTts() {
		tts = getTts();
	}

	private TextToSpeech getTts() {
		if (tts == null) {
			tts = new TextToSpeech(context, this);
			int result = tts.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "This Language is not supported");
			}
		}
		return tts;
	}

	public void sayImOk() {
		if (pref.notifySpeech().get()) {
			final String string = context.getResources().getString(R.string.speech_im_ok);
			notifyAlertSpeech(string);
		}
	}

	public void sayState(RowState state) {
		if (pref.notifySpeech().get()) {
			if (state == RowState.BREATHE) {
				final String string = context.getResources().getString(R.string.speech_breathe);
				notifyAlertSpeech(string);
			} else if (state == RowState.HOLD) {
				final String string = context.getResources().getString(R.string.speech_hold);
				notifyAlertSpeech(string);
			}
		}
	}

	public void checkNotifications(int sec) {
		if (pref.notifyMinute().get() && sec % 60 == 0) {
			final String string = context.getResources().getQuantityString(R.plurals.speech_minute, sec / 60, sec / 60);
			notifyAlert(string);
		} else if (pref.notify30sec().get() && sec == 30) {
			final String string = context.getResources().getString(R.string.speech_30sec);
			notifyAlert(string);
		} else if (pref.notify10sec().get() && sec == 10) {
			final String string = context.getResources().getString(R.string.speech_10sec);
			notifyAlert(string);
		} else if (pref.notify5sec().get() && sec <= 5) {
			notifyAlert(sec + "");
		}
	}

	private void notifyAlert(String textToSpeech) {
		notifyAlertVibrate();
		notifyAlertSpeech(textToSpeech);
	}

	private void notifyAlertVibrate() {
		if (pref.notifyVibrate().get()) {
			vibrator.vibrate(VIBRATE_TIME);
		}
	}

	private void notifyAlertSpeech(String textToSpeech) {
		Log.i("TTS", textToSpeech);
		if (pref.notifySpeech().get()) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				tts.speak(textToSpeech, TextToSpeech.QUEUE_FLUSH, null, null);
			}else{
				tts.speak(textToSpeech, TextToSpeech.QUEUE_FLUSH, null);
			}
		}
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = getTts().setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				pref.edit().notifySpeech().put(false).apply();
				Log.e("TTS", "язык не поддерживается");
			}
		}
	}

	@Override
	public void close() throws IOException {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
	}
}
