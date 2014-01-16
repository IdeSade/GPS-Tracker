package ru.idesade.gpstracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GPSTrackerFragment extends Fragment implements View.OnClickListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_gps_tracker, container, false);
		assert rootView != null;

		rootView.findViewById(R.id.button_gps_tracker_start).setOnClickListener(this);
		rootView.findViewById(R.id.button_gps_tracker_stop).setOnClickListener(this);

		return rootView;
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
			case R.id.button_gps_tracker_start: {
				if (!CheckPlayServiceAvailable.isGooglePlayServicesAvailable(getActivity())) {
					return;
				}
				break;
			}
			case R.id.button_gps_tracker_stop: {
				break;
			}
		}
	}
}

