/*
 * Copyright (C) 2013 The MoKee OpenSource Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mokee.helper.activities;

import com.mokee.helper.R;
import com.mokee.helper.utils.Utils;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

public class MoKeeUpdater extends PreferenceFragment {

    private static final String KEY_MOKEE_VERSION = "mokee_version";
    private static final String KEY_MOKEE_VERSION_TYPE = "mokee_version_type";
    private static final String KEY_MOKEE_EXTENSIONS_COMPONENTS = "mokee_extensions_components";
    private static final String KEY_GOOGLE_MOBILE_SERVICE = "google_mobile_service";

    private static final String GOOGLE_MOBILE_SERVICE_PACKAGE_NAME = "com.google.android.gms";

    private PreferenceCategory mExtensionsList;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.mokee_updater);
        mContext = getActivity().getBaseContext();
        setValueSummary(KEY_MOKEE_VERSION, "ro.mk.version");
        setStringSummary(KEY_MOKEE_VERSION_TYPE, getMoKeeVersionType());

        mExtensionsList = (PreferenceCategory) findPreference(KEY_MOKEE_EXTENSIONS_COMPONENTS);
        refreshExtensionsList();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void refreshExtensionsList() {
        // Check Google Mobile Service
        if (!Utils.isApkInstalled(GOOGLE_MOBILE_SERVICE_PACKAGE_NAME, mContext)) {
            Preference gmsDefaultPreference = (Preference) mExtensionsList
                    .findPreference(KEY_GOOGLE_MOBILE_SERVICE);
            mExtensionsList.removePreference(gmsDefaultPreference);
        }

    }

    private String getMoKeeVersionType() {
        String MoKeeVersion = SystemProperties.get("ro.mk.version",
                getString(R.string.mokee_info_default));
        String MoKeeVersionType;
        if (MoKeeVersion.equals(getString(R.string.mokee_info_default))) {
            return MoKeeVersion;
        } else {
            MoKeeVersionType = MoKeeVersion.substring(MoKeeVersion.lastIndexOf("-") + 1,
                    MoKeeVersion.length()).toLowerCase();
            if (MoKeeVersionType.equals("release"))
                return getString(R.string.mokee_version_type_release);
            else if (MoKeeVersionType.equals("experimental"))
                return getString(R.string.mokee_version_type_experimental);
            else if (MoKeeVersionType.equals("nightly"))
                return getString(R.string.mokee_version_type_nightly);
            else if (MoKeeVersionType.equals("unofficial"))
                return getString(R.string.mokee_version_type_unofficial);
        }
        return MoKeeVersionType;
    }

    private void setStringSummary(String preference, String value) {
        try {
            findPreference(preference).setSummary(value);
        } catch (RuntimeException e) {
            findPreference(preference).setSummary(
                    getString(R.string.mokee_info_default));
        }
    }

    private void setValueSummary(String preference, String property) {
        try {
            findPreference(preference).setSummary(
                    SystemProperties.get(property,
                            getString(R.string.mokee_info_default)));
        } catch (RuntimeException e) {
            // No recovery
        }
    }
}
