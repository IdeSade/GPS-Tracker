package ru.idesade.gpstracker;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.Date;

import ru.idesade.gpstracker.Service.GPSTrackChangeListener;
import ru.idesade.gpstracker.Service.GPSTrackerService;
import ru.idesade.gpstracker.Service.GPSTrackerServiceBinder;

public class GPSTrackerFragment extends Fragment implements
		View.OnClickListener,
		LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		GPSTrackChangeListener {

	private final String TAG = "GPSTrackerFragment";

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setPriority(LocationRequest.PRIORITY_NO_POWER);
	private LocationClient mLocationClient;

	private GoogleMap mMap;

	private int showedTrackIndex;

	private Button mStartTracking;
	private Button mStopTracking;
	private TextView mTrackInfo;

	private Intent serviceIntent;
	private ServiceConnection serviceConnection;
	private GPSTrackerService mGpsTrackerService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");

		super.onCreate(savedInstanceState);

		serviceIntent = new Intent(getActivity(), GPSTrackerService.class);
		serviceConnection = new GPSTrackServiceConnection();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView()");

		View rootView = inflater.inflate(R.layout.fragment_gps_tracker, container, false);
		assert rootView != null;

		mStartTracking = (Button) rootView.findViewById(R.id.button_gps_tracker_start);
		mStartTracking.setOnClickListener(this);
		mStartTracking.setEnabled(true);
		mStopTracking = (Button) rootView.findViewById(R.id.button_gps_tracker_stop);
		mStopTracking.setOnClickListener(this);
		mStopTracking.setEnabled(false);

		mTrackInfo = (TextView) rootView.findViewById(R.id.text_gps_tracker_track_info);
		mTrackInfo.setVisibility(View.GONE);

		rootView.findViewById(R.id.button_gps_tracker_load_track).setOnClickListener(this);

		return rootView;
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart()");
		super.onStart();

		getActivity().bindService(serviceIntent, serviceConnection, 0);
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();

		setUpMapIfNeeded();
		setUpLocationClientIfNeeded();

		mLocationClient.connect();
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause()");
		super.onPause();

		if (mLocationClient != null) {
			mLocationClient.disconnect();
		}
	}

	@Override
	public void onStop() {
		Log.d(TAG, "onStop()");
		super.onStop();

		getActivity().unbindService(serviceConnection);

		clearGpsTrackerService();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		super.onDestroy();
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
			case R.id.button_gps_tracker_start: {
				if (!CheckPlayServiceAvailable.isGooglePlayServicesAvailable(getActivity())) {
					return;
				}
				getActivity().startService(serviceIntent);
				getActivity().bindService(serviceIntent, serviceConnection, 0);
				break;
			}
			case R.id.button_gps_tracker_stop: {
				getActivity().stopService(serviceIntent);
				mTrackInfo.setVisibility(View.GONE);
				break;
			}
			case R.id.button_gps_tracker_load_track: {
				selectStoreTrack();
				break;
			}
		}
	}

	// LocationListener

	@Override
	public void onLocationChanged(final Location location) {
		Log.d(TAG, "onLocationChanged(): " + location);
		if (mMap != null) {
			CameraPosition position = new CameraPosition.Builder()
					.target(new LatLng(location.getLatitude(), location.getLongitude()))
					.zoom(15.5f)
					.build();
			mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
		}
	}

	// GooglePlayServicesClient.ConnectionCallbacks

	@Override
	public void onConnected(final Bundle bundle) {
		Log.d(TAG, "onConnected()");
		mLocationClient.requestLocationUpdates(REQUEST, this);
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "onDisconnected()");
	}

	// GooglePlayServicesClient.OnConnectionFailedListener

	@Override
	public void onConnectionFailed(final ConnectionResult connectionResult) {
		Log.d(TAG, "onConnectionFailed()");
	}

	// Service connection

	private class GPSTrackServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected(): " + name);

			mStartTracking.setEnabled(false);
			mStopTracking.setEnabled(true);

			mGpsTrackerService = ((GPSTrackerServiceBinder) service).getService();
			mGpsTrackerService.setGpsTrackChangeListener(GPSTrackerFragment.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "onServiceDisconnected(): " + name);

			mStartTracking.setEnabled(true);
			mStopTracking.setEnabled(false);

			clearGpsTrackerService();
		}
	}

	// GPSTrackChangeListener

	@Override
	public void onTrackChange(GPSTrack track) {
		if (track != null) {
			mTrackInfo.setVisibility(View.VISIBLE);
			mTrackInfo.setText(track.toString());
			showTrack(track);
		}
	}

	// Helper methods

	private void setUpMapIfNeeded() {
		if (mMap == null) {
			Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
			mMap = ((SupportMapFragment) fragment).getMap();
			if (mMap != null) {
				mMap.setMyLocationEnabled(true);
			}
		}
	}

	private void setUpLocationClientIfNeeded() {
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(
					getActivity().getApplicationContext(),
					this,  // ConnectionCallbacks
					this); // OnConnectionFailedListener
		}
	}

	private void selectStoreTrack() {
		final String[] files = GPSTrackerUtils.getGPSTrackerDir(getActivity()).list();
		final String[] tracks = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			int idx = files[i].lastIndexOf(".");
			Date date = new Date(Long.parseLong(files[i].substring(0, idx)));
			tracks[i] = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(date);
		}

		new AlertDialog.Builder(getActivity())
				.setTitle("Select store track")
				.setSingleChoiceItems(tracks, showedTrackIndex, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						GPSTrack loadTrack = loadTrackFromFile(files[which]);
						showTrack(loadTrack);

						showedTrackIndex = which;

						dialog.dismiss();
					}
				})
				.create()
				.show();
	}

	private GPSTrack loadTrackFromFile(final String fileName) {
		GPSTrack track = new GPSTrack();
		track.loadFromFile(getActivity(), fileName);
		return track;
	}

	private void showTrack(final GPSTrack track) {
		if (mMap != null && track != null) {
			mMap.clear();
			mMap.addPolyline(new PolylineOptions()
					.addAll(track.getLatLng())
					.width(5)
					.color(Color.RED));
		}
	}

	private void clearGpsTrackerService() {
		if (mGpsTrackerService != null) {
			mGpsTrackerService.clearGpsTrackChangeListener();
			mGpsTrackerService = null;
		}
	}
}

