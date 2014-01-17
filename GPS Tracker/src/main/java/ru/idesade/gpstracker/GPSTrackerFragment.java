package ru.idesade.gpstracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

public class GPSTrackerFragment extends Fragment implements View.OnClickListener {

	private GoogleMap mMap;

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

	// Helper methods

	private void setUpMapIfNeeded() {
		if (mMap == null) {
			mMap = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map))
					.getMap();
			if (mMap != null) {
				mMap.setMyLocationEnabled(true);
			}
		}
	}
}

