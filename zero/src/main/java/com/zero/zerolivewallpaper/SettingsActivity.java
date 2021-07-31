package com.zero.zerolivewallpaper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.zero.zerolivewallpaper.components.SliderDialogBuilder;

import java.io.File;

import static com.zero.zerolivewallpaper.utils.StorageHelper.deleteFolder;
import static com.zero.zerolivewallpaper.utils.StorageHelper.getRootFolder;

public class SettingsActivity extends AppCompatActivity {

    private MyPreferenceFragment myPreferenceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myPreferenceFragment = new MyPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, myPreferenceFragment).commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Call fragment onActivityResult
        super.onActivityResult(requestCode, resultCode, data);
        myPreferenceFragment.onActivityResult(requestCode, resultCode, data);
    }

    public static class MyPreferenceFragment extends PreferenceFragment {

        private Context context;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            context = getActivity();

            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            // Preference
            final Preference sensor = findPreference(getString(R.string.pref_sensor_key));
            sensor.setEnabled(Utils.sensorsAvailable(context));


            // Depth
            final Preference depth = findPreference(getString(R.string.pref_depth_key));
            depth.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    setSliderPref(
                            sharedPreferences,
                            R.string.pref_depth_key,
                            R.string.pref_depth_title,
                            R.string.pref_depth_default);

                    return false;
                }
            });

            // Sensitivity
            final Preference sensitivity = findPreference(getString(R.string.pref_sensitivity_key));
            sensitivity.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    setSliderPref(
                            sharedPreferences,
                            R.string.pref_sensitivity_key,
                            R.string.pref_sensitivity_title,
                            R.string.pref_sensitivity_default);

                    return false;
                }
            });

            // Fallback
            final Preference fallback = findPreference(getString(R.string.pref_fallback_key));
            fallback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    setSliderPref(
                            sharedPreferences,
                            R.string.pref_fallback_key,
                            R.string.pref_fallback_title,
                            R.string.pref_fallback_default);

                    return false;
                }
            });

            // Zoom
            final Preference zoom = findPreference(getString(R.string.pref_zoom_key));
            zoom.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    setSliderPref(
                            sharedPreferences,
                            R.string.pref_zoom_key,
                            R.string.pref_zoom_title,
                            R.string.pref_zoom_default);

                    return false;
                }
            });

            // Scroll amount
            final Preference scroll_amount = findPreference(getString(R.string.pref_scroll_amount_key));
            scroll_amount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    setSliderPref(
                            sharedPreferences,
                            R.string.pref_scroll_amount_key,
                            R.string.pref_scroll_amount_title,
                            R.string.pref_scroll_amount_default);

                    return false;
                }
            });

            final Preference dim = findPreference(getString(R.string.pref_dim_key));
            dim.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    setSliderPref(
                            sharedPreferences,
                            R.string.pref_dim_key,
                            R.string.pref_dim_title,
                            R.string.pref_dim_default);

                    return false;
                }
            });

            // Delete
            final Preference delete = findPreference(getString(R.string.pref_delete_key));
            delete.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(context);
                    deleteDialogBuilder.setTitle(R.string.settings_dialog_delete_title);
                    deleteDialogBuilder.setMessage(R.string.settings_dialog_delete_message);
                    deleteDialogBuilder.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Delete local content
                            File root = getRootFolder(context);
                            deleteFolder(root);
                            Toast.makeText(context, R.string.main_alert_deleted, Toast.LENGTH_SHORT).show();
                        }
                    });
                    deleteDialogBuilder.setNegativeButton(R.string.common_cancel, null);
                    deleteDialogBuilder.show();

                    return false;
                }
            });

            // Version
            Preference version = findPreference(getString(R.string.pref_version_key));
            version.setSummary(BuildConfig.VERSION_NAME);
        }

        private void setSliderPref(final SharedPreferences sharedPreferences, int keyRes, int titleRes, int defaultRes) {
            // Data from resources
            final String key = getString(keyRes);
            final String title = getString(titleRes);
            final String defaultValue = getString(defaultRes);

            String stringValue = sharedPreferences.getString(key, defaultValue);
            int value = Integer.parseInt(stringValue);

            final SliderDialogBuilder dialogBuilder = new SliderDialogBuilder(context);
            dialogBuilder.setTitle(title);
            dialogBuilder.setInitialValue(value);
            dialogBuilder.setPositiveButton(R.string.common_set, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int finalValue = dialogBuilder.getValue();
                    sharedPreferences.edit().putString(key, String.valueOf(finalValue)).apply();
                }
            });

            dialogBuilder.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
        }
    }
}

