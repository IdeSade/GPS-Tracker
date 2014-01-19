package ru.idesade.gpstracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class GPSTrackerFragment extends Fragment implements
		View.OnClickListener,
		LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private final String TAG = "GPSTrackerFragment";

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setPriority(LocationRequest.PRIORITY_NO_POWER);
	private LocationClient mLocationClient;

	private GoogleMap mMap;

	private int showedTrackIndex;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_gps_tracker, container, false);
		assert rootView != null;

		rootView.findViewById(R.id.button_gps_tracker_start).setOnClickListener(this);
		rootView.findViewById(R.id.button_gps_tracker_stop).setOnClickListener(this);
		rootView.findViewById(R.id.button_gps_tracker_load_track).setOnClickListener(this);

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		setUpMapIfNeeded();
		setUpLocationClientIfNeeded();
		mLocationClient.connect();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mLocationClient != null) {
			mLocationClient.disconnect();
		}
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
			case R.id.button_gps_tracker_start: {
				if (!CheckPlayServiceAvailable.isGooglePlayServicesAvailable(getActivity())) {
					return;
				}
				getActivity().startService(new Intent(getActivity(), GPSTrackerService.class));
				break;
			}
			case R.id.button_gps_tracker_stop: {
				getActivity().stopService(new Intent(getActivity(), GPSTrackerService.class));
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
}

