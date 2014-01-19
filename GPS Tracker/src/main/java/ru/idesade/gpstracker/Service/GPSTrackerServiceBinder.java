package ru.idesade.gpstracker.Service;

import android.os.Binder;

public class GPSTrackerServiceBinder extends Binder {

	private GPSTrackerService mGpsTrackerService;

	public GPSTrackerServiceBinder(GPSTrackerService gpsTrackerService) {
		this.mGpsTrackerService = gpsTrackerService;
	}

	public GPSTrackerService getService() {
		return mGpsTrackerService;
	}
}
