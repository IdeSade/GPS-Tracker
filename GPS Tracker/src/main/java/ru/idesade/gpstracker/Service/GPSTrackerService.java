package ru.idesade.gpstracker.Service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.io.File;

import ru.idesade.gpstracker.GPSTrack;
import ru.idesade.gpstracker.GPSTrackerUtils;

public class GPSTrackerService extends Service implements
		LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private final String TAG = "GPSTrackerService";

	private final LocationRequest REQUEST = LocationRequest.create()
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
			.setInterval(GPSTrackerUtils.UPDATE_INTERVAL_IN_MILLISECONDS)
			.setSmallestDisplacement(GPSTrackerUtils.SMALLEST_DISPLACEMENT_METER);

	private LocationClient mLocationClient;

	private GPSTrack mTrack;

	private GPSTrackerServiceBinder mBinder = new GPSTrackerServiceBinder(this);

	private GPSTrackChangeListener mGpsTrackChangeListener;

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate()");
		mLocationClient = new LocationClient(this, this, this);
		super.onCreate();
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		Log.d(TAG, "onStartCommand()");
		if (mTrack == null) {
			mTrack = new GPSTrack();
			mTrack.setStartTime();
			mLocationClient.connect();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		mLocationClient.disconnect();
		if (mTrack != null) {
			mTrack.setFinishTime();
			File file = mTrack.storeToFile(this);
			if (file != null) {
				Log.d(TAG, "Track store to file " + file.getAbsolutePath());
			} else {
				Log.d(TAG, "Track not store");
			}
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(final Intent intent) {
		Log.d(TAG, "onBind()");
		return mBinder;
	}

	public void setGpsTrackChangeListener(GPSTrackChangeListener listener) {
		mGpsTrackChangeListener = listener;
		doChangeGpsTrack();
	}

	public void clearGpsTrackChangeListener() {
		mGpsTrackChangeListener = null;
	}

	public void doChangeGpsTrack() {
		if (mGpsTrackChangeListener != null && mTrack != null) {
			mGpsTrackChangeListener.onTrackChange(mTrack);
		}
	}

	// LocationListener

	@Override
	public void onLocationChanged(final Location location) {
		if (mTrack != null) {
			mTrack.addLocation(location);
			doChangeGpsTrack();
		}
	}

	// GooglePlayServicesClient.ConnectionCallbacks

	@Override
	public void onConnected(final Bundle bundle) {
		Log.d(TAG, "onConnected()");
		startPeriodicUpdates();
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "onDisconnected()");
		stopPeriodicUpdates();
	}

	// GooglePlayServicesClient.OnConnectionFailedListener

	@Override
	public void onConnectionFailed(final ConnectionResult connectionResult) {
		Log.d(TAG, "onConnectionFailed()");
	}

	// Helper methods

	private void startPeriodicUpdates() {
		Log.d(TAG, "Start periodic updates...");
		mLocationClient.requestLocationUpdates(REQUEST, GPSTrackerService.this);
		doChangeGpsTrack();
	}

	private void stopPeriodicUpdates() {
		Log.d(TAG, "Stop periodic updates...");
		mLocationClient.removeLocationUpdates(this);
	}
}
