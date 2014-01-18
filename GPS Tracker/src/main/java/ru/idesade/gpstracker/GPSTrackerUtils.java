package ru.idesade.gpstracker;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public final class GPSTrackerUtils {

	public static final int MILLISECONDS_PER_SECOND = 1000;
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
	public static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

	public static final int SMALLEST_DISPLACEMENT_METER = 10;

	public static File getGPSTrackerDir(Context context) {
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
