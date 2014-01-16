package ru.idesade.gpstracker;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class CheckPlayServiceAvailable {

	public static boolean isGooglePlayServicesAvailable(FragmentActivity activity) {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);

		if (ConnectionResult.SUCCESS == resultCode) {
			Log.d("Location Updates", "Google Play services is available.");

			return true;
		} else {
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, activity, 0);

			if (errorDialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(errorDialog);
				errorFragment.show(activity.getSupportFragmentManager(), "Location Updates");
			}

			return false;
		}
	}

	private static class ErrorDialogFragment extends DialogFragment {
		private Dialog mDialog;

		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}
}
