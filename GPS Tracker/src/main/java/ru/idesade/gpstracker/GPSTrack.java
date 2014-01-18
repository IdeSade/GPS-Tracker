package ru.idesade.gpstracker;

import android.content.Context;
import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class GPSTrack {

	public long StartTime;
	public long FinishTime;

	public final ArrayList<Location> LocationList = new ArrayList<>();

	public synchronized boolean addLocation(final Location location) {
		if (StartTime == 0) {
			StartTime = location.getTime();
		}
		FinishTime = location.getTime();
		LocationList.add(location);
		return true;
	}

	public synchronized JSONObject toJSONObject() {
		JSONObject jsonTrack = new JSONObject();
		try {
			// Track header
			jsonTrack.put("StartTime", StartTime);
			jsonTrack.put("FinishTime", FinishTime);

			// Track locations
			JSONArray jsonLocations = new JSONArray();
			for (Location location : LocationList) {
				JSONObject jsonLocation = new JSONObject();
				jsonLocation.put("Time", location.getTime());
				jsonLocation.put("Lat", location.getLatitude());
				jsonLocation.put("Lng", location.getLongitude());
				if (location.hasAltitude()) {
					jsonLocation.put("Alt", location.getAltitude());
				}
				if (location.hasAccuracy()) {
					jsonLocation.put("Acc", location.getAccuracy());
				}
				jsonLocations.put(jsonLocation);
			}
			jsonTrack.put("LocationList", jsonLocations);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return jsonTrack;
	}

	public synchronized File storeToExternalStorage(Context context) {
		JSONObject json = toJSONObject();

		File file = prepareFile(context);
		if (file == null) {
			return null;
		}

		try {
			FileWriter writer = new FileWriter(file);
			writer.write(json.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return file;
	}

	private File prepareFile(Context context) {
		File dir = GPSTrackerUtils.prepareGPSTrackerDir(context);
		return new File(dir, StartTime + ".trc");
	}
}
