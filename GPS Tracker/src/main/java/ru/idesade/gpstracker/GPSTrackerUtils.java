package ru.idesade.gpstracker;

import android.content.Context;
import android.location.Location;
import android.os.Environment;

import java.io.File;

public final class GPSTrackerUtils {

	public static final int MILLISECONDS_PER_SECOND = 1000;
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
	public static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

	public static final int SMALLEST_DISPLACEMENT_METER = 10;

	public static final String EMPTY_STRING = new String();

	public static String getLatLng(Context context, Location currentLocation) {
		if (currentLocation != null) {
			return context.getString(
					R.string.latitude_longitude,
					currentLocation.getLatitude(),
					currentLocation.getLongitude());
		} else {
			return EMPTY_STRING;
		}
	}

	public static File prepareGPSTrackerDir(Context context) {
//		File dir = context.getFilesDir();
		File dir = new File(Environment.getExternalStorageDirectory() + "/GPSTracker");

		Boolean dirExists = dir.exists();
		if (!dirExists) {
			dirExists = dir.mkdirs();
		}

		if (!dirExists) {
			return null;
		} else {
			return dir;
		}
	}
}
