package ru.idesade.gpstracker;

import android.content.Intent;
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
}

