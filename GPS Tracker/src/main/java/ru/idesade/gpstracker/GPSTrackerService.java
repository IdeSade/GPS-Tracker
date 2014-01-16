package ru.idesade.gpstracker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class GPSTrackerService extends Service {
	private final String TAG = "GPSTrackerService";

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate()");
		super.onCreate();
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		Log.d(TAG, "onStartCommand()");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(final Intent intent) {
		Log.d(TAG, "onBind()");
		return null;
	}
}
