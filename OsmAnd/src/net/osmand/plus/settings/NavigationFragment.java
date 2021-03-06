package net.osmand.plus.settings;

import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;

import net.osmand.plus.R;
import net.osmand.plus.settings.preferences.SwitchPreferenceEx;

public class NavigationFragment extends BaseSettingsFragment {

	public static final String TAG = NavigationFragment.class.getSimpleName();

	@Override
	protected void setupPreferences() {
		Preference routeParameters = findPreference("route_parameters");
		SwitchPreferenceCompat showRoutingAlarms = (SwitchPreferenceCompat) findPreference(settings.SHOW_ROUTING_ALARMS.getId());
		SwitchPreferenceCompat speakRoutingAlarms = (SwitchPreferenceCompat) findPreference(settings.SPEAK_ROUTING_ALARMS.getId());
		SwitchPreferenceCompat turnScreenOn = (SwitchPreferenceCompat) findPreference(settings.TURN_SCREEN_ON_ENABLED.getId());
		SwitchPreferenceEx animateMyLocation = (SwitchPreferenceEx) findPreference(settings.ANIMATE_MY_LOCATION.getId());

		routeParameters.setIcon(getContentIcon(R.drawable.ic_action_route_distance));
		showRoutingAlarms.setIcon(getContentIcon(R.drawable.ic_action_alert));
		speakRoutingAlarms.setIcon(getContentIcon(R.drawable.ic_action_volume_up));
		turnScreenOn.setIcon(getContentIcon(R.drawable.ic_action_turn_screen_on));

		setupVehicleParametersPref();

		animateMyLocation.setDescription(getString(R.string.animate_my_location_desc));
	}

	private void setupVehicleParametersPref() {
		Preference vehicleParameters = findPreference("vehicle_parameters");
		int iconRes = getSelectedAppMode().getIconRes();
		vehicleParameters.setIcon(getContentIcon(iconRes));
	}
}