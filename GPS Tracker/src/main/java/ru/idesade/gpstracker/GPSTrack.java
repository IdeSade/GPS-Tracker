package ru.idesade.gpstracker;

import android.content.Context;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GPSTrack implements Parcelable {

	private static String LOCATION_PROVIDER			= "GPSTrack";

	private static String JSON_START_TIME 			= "StartTime";
	private static String JSON_FINISH_TIME 			= "FinishTime";
	private static String JSON_LOCATION_LIST 		= "LocationList";

	private static String JSON_LOCATION_TIME 		= "Time";
	private static String JSON_LOCATION_LATITUDE 	= "Lat";
	private static String JSON_LOCATION_LONGITUDE	= "Lng";
	private static String JSON_LOCATION_ALTITUDE 	= "Alt";
	private static String JSON_LOCATION_ACCURACY 	= "Acc";
	private static String JSON_LOCATION_SPEED		= "Spd";

	public long StartTime;
	public long FinishTime;

	public final ArrayList<Location> LocationList = new ArrayList<>();

	public GPSTrack() {
		setStartTime();
	}

	public void clear() {
		StartTime = 0;
		FinishTime = 0;
		LocationList.clear();
	}

	public void setStartTime() {
		StartTime = Calendar.getInstance().getTimeInMillis();
	}

	public void setFinishTime() {
		FinishTime = Calendar.getInstance().getTimeInMillis();
	}

	public boolean addLocation(final Location location) {
		LocationList.add(location);
		return true;
	}

	public List<LatLng> getLatLng() {
		ArrayList<LatLng> latLngList = new ArrayList<>(LocationList.size());

		for (Location location : LocationList) {
			latLngList.add(new LatLng(location.getLatitude(), location.getLongitude()));
		}

		return latLngList;
	}

	public JSONObject toJSONObject() {
		JSONObject jsonTrack = new JSONObject();

		try {
			// Track locations
			JSONArray jsonLocations = new JSONArray();

			for (Location location : LocationList) {
				JSONObject jsonLocation = new JSONObject();

				jsonLocation.put(JSON_LOCATION_TIME, location.getTime());
				jsonLocation.put(JSON_LOCATION_LATITUDE, location.getLatitude());
				jsonLocation.put(JSON_LOCATION_LONGITUDE, location.getLongitude());
				if (location.hasAltitude()) {
					jsonLocation.put(JSON_LOCATION_ALTITUDE, location.getAltitude());
				}
				if (location.hasAccuracy()) {
					jsonLocation.put(JSON_LOCATION_ACCURACY, location.getAccuracy());
				}
				if (location.hasSpeed()) {
					jsonLocation.put(JSON_LOCATION_SPEED, location.getSpeed());
				}

				jsonLocations.put(jsonLocation);
			}

			jsonTrack.put(JSON_LOCATION_LIST, jsonLocations);

			// Track header
			jsonTrack.put(JSON_START_TIME, StartTime);
			jsonTrack.put(JSON_FINISH_TIME, FinishTime);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return jsonTrack;
	}

	public boolean fromJSONString(final String jsonString) {
		clear();

		try {
			JSONObject jsonTrack = new JSONObject(jsonString);

			// Track locations
			JSONArray jsonLocations = jsonTrack.getJSONArray(JSON_LOCATION_LIST);

			for (int i = 0; i < jsonLocations.length(); i++) {
				JSONObject jsonLocation = jsonLocations.getJSONObject(i);

				Location location = new Location(LOCATION_PROVIDER);

				location.setTime(jsonLocation.getLong(JSON_LOCATION_TIME));
				location.setLatitude(jsonLocation.getDouble(JSON_LOCATION_LATITUDE));
				location.setLongitude(jsonLocation.getDouble(JSON_LOCATION_LONGITUDE));
				location.setAltitude(jsonLocation.optDouble(JSON_LOCATION_ALTITUDE, 0));
				location.setAccuracy((float) jsonLocation.optDouble(JSON_LOCATION_ACCURACY, 0));
				location.setSpeed((float) jsonLocation.optDouble(JSON_LOCATION_SPEED, 0));

				LocationList.add(location);
			}

			// Track header
			StartTime = jsonTrack.getLong(JSON_START_TIME);
			FinishTime = jsonTrack.getLong(JSON_FINISH_TIME);
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public File storeToFile(Context context) {
		File file = getFileByStartTime(context, StartTime);
		if (file == null) {
			return null;
		}

		JSONObject json = toJSONObject();

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(json.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return file;
	}

	public boolean loadFromFile(Context context, String fileName) {
		File file = getFile(context, fileName);
		if (file == null) {
			return false;
		}

		String loadString;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));

			StringBuilder sb = new StringBuilder();
			String receiveString;

			while ((receiveString = reader.readLine()) != null) {
				sb.append(receiveString);
			}

			reader.close();

			loadString = sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return fromJSONString(loadString);
	}

	@Override
	public String toString() {
		String extraInfo = "";

		if (LocationList.size() > 0) {
			Location lastLocation = LocationList.get(LocationList.size() - 1);

			Date date = new Date(lastLocation.getTime());
			String time = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(date);

			extraInfo = "Time: " + time + "\n"
					+ "Lat: " + lastLocation.getLatitude() + "\n"
					+ "Lng: " + lastLocation.getLongitude() + "\n"
					+ "Alt: " + lastLocation.getAltitude() + "\n"
					+ "Acc: " + lastLocation.getAccuracy() + "\n"
					+ "Spd: " + lastLocation.getSpeed() / 1000.0 * 3600.0 + "\n";
		}

		return "GPSTrack:\n" + extraInfo;
	}

	// Helper methods

	private File getFileByStartTime(Context context, long startTime) {
		File dir = GPSTrackerUtils.getGPSTrackerDir(context);
		return new File(dir, startTime + ".trc");
	}

	private File getFile(final Context context, final String fileName) {
		File dir = GPSTrackerUtils.getGPSTrackerDir(context);
		return new File(dir, fileName);
	}

	// Parcelling part

	public GPSTrack(Parcel in) {
		StartTime = in.readLong();
		FinishTime = in.readLong();
		in.readList(LocationList, Location.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(StartTime);
		dest.writeLong(FinishTime);
		dest.writeList(LocationList);
	}

	public static final Creator<GPSTrack> CREATOR = new Creator<GPSTrack>() {
		@Override
		public GPSTrack createFromParcel(Parcel source) {
			return new GPSTrack(source);
		}

		@Override
		public GPSTrack[] newArray(int size) {
			return new GPSTrack[size];
		}
	};
}
