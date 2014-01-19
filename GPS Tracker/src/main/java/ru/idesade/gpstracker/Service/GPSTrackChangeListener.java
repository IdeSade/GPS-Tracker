package ru.idesade.gpstracker.Service;

import ru.idesade.gpstracker.GPSTrack;

public interface GPSTrackChangeListener {
	void onTrackChange(GPSTrack track);
}
