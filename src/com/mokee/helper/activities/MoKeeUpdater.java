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
import com.mokee.helper.misc.ExtraInfo;
import com.mokee.helper.utils.UpdateFilter;
import com.mokee.helper.utils.Utils;
import com.mokee.helper.widget.ExtraPreference;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class MoKeeUpdater extends PreferenceFragment implements ExtraPreference.OnReadyListener,
        ExtraPreference.OnActionListener {
    private static String TAG = "MoKeeUpdater";

    private static final String KEY_MOKEE_VERSION = "mokee_version";
    private static final String KEY_MOKEE_VERSION_TYPE = "mokee_version_type";
    private static final String KEY_MOKEE_EXTRAS = "mokee_extras_title";

    private static final String GOOGLE_MOBILE_SERVICE_PACKAGE_NAME = "com.google.android.gms";

    private PreferenceCategory mExtrasList;
    private ExtraPreference mExtraPreference;

    private DownloadManager mDownloadManager;
    private boolean mDownloading = false;
    private long mDownloadId;
    private String mFileName;

    // Building Demo
    public static PreferenceScreen mTmpEntry;
    private static final String KEY_TMP_ENTRY = "tmp_entry";

    private Context mContext;

    private File mExtraFolder;
    
    private boolean mStartUpdateVisible = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.mokee_updater);
        mContext = getActivity();
        setValueSummary(KEY_MOKEE_VERSION, "ro.mk.version");
        setStringSummary(KEY_MOKEE_VERSION_TYPE, getMoKeeVersionType());

        mExtrasList = (PreferenceCategory) findPreference(KEY_MOKEE_EXTRAS);
        mTmpEntry = (PreferenceScreen) findPreference(KEY_TMP_ENTRY);
        updateExtrasLayout();
    }

    private void updateExtrasLayout() {
        // Check Google Mobile Service
        if (!Utils.isApkInstalled(GOOGLE_MOBILE_SERVICE_PACKAGE_NAME, mContext)) {
            LinkedList<String> existingFiles = new LinkedList<String>();

            mExtraFolder = Utils.makeExtraFolder();
            File[] files = mExtraFolder.listFiles(new UpdateFilter(".zip"));
            if (mExtraFolder.exists() && mExtraFolder.isDirectory() && files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        existingFiles.add(file.getName());
                    }
                }
            }
            final LinkedList<ExtraInfo> updates = new LinkedList<ExtraInfo>();

            for (String fileName : existingFiles) {
                updates.add(new ExtraInfo(fileName));
            }
            // Clear the notification if one exists
            Utils.cancelNotification(mContext);
            refreshExtrasPreferences(updates);

            // Prune obsolete change log files
            new Thread() {
                @Override
                public void run() {
                    File[] files = mContext.getCacheDir().listFiles(new UpdateFilter(".changelog"));
                    if (files == null) {
                        return;
                    }

                    for (File file : files) {
                        boolean updateExists = false;
                        for (ExtraInfo info : updates) {
                            if (file.getName().startsWith(info.getmFileName())) {
                                updateExists = true;
                                break;
                            }
                        }
                        if (!updateExists) {
                            file.delete();
                        }
                    }
                }
            }.start();
        }

    }

    private void refreshExtrasPreferences(LinkedList<ExtraInfo> updates) {
        if (mExtrasList == null) {
            return;
        }

        // Clear the list
        mExtrasList.removeAll();

        // Add the updates
        for (ExtraInfo ei : updates) {
            // Determine the preference style and create the preference
            boolean isDownloading = ei.getmFileName().equals(mFileName);
            int style;

            if (isDownloading) {
                // In progress download
                style = ExtraPreference.STYLE_DOWNLOADING;
            } else if (ei.getmDownloadUrl() != null) {
                style = ExtraPreference.STYLE_NEW;
            } else {
                style = ExtraPreference.STYLE_DOWNLOADED;
            }

            ExtraPreference up = new ExtraPreference(mContext, ei, style);
            up.setOnActionListener(this);
            up.setKey(ei.getmFileName());

            // If we have an in progress download, link the preference
            if (isDownloading) {
                mExtraPreference = up;
                up.setOnReadyListener(this);
                mDownloading = true;
            }

            // Add to the list
            mExtrasList.addPreference(up);
        }
    }

    private String getMoKeeVersionType() {
        String MoKeeVersion = SystemProperties.get("ro.mk.version",
                getString(R.string.mokee_info_default));
        String MoKeeVersionType;
        if (MoKeeVersion.equals(getString(R.string.mokee_info_default))) {
            return MoKeeVersion;
        } else {
            MoKeeVersionType = Utils.getMoKeeVersionTypeString(MoKeeVersion, mContext);
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

    @Override
    public void onStartDownload(ExtraPreference pref) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopDownload(ExtraPreference pref) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStartUpdate(ExtraPreference pref) {
        final ExtraInfo updateInfo = pref.getExtraInfo();

        // Prevent the dialog from being triggered more than once
        if (mStartUpdateVisible) {
            return;
        }

        mStartUpdateVisible = true;

        // Get the message body right
        String dialogBody = getString(R.string.apply_install_dialog_text, updateInfo.getmFileName());

        // Display the dialog
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.apply_install_dialog_title)
                .setMessage(dialogBody)
                .setPositiveButton(R.string.dialog_install, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Utils.triggerUpdate(mContext, updateInfo.getmFileName(), false);
                        } catch (IOException e) {
                            Log.e(TAG, "Unable to reboot into recovery mode", e);
                            Toast.makeText(mContext, R.string.apply_unable_to_reboot_toast,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mStartUpdateVisible = false;
                    }
                })
                .show();

    }

    @Override
    public void onDeleteUpdate(ExtraPreference pref) {
        final String fileName = pref.getKey();

        if (mExtraFolder.exists() && mExtraFolder.isDirectory()) {
            File zipFileToDelete = new File(mExtraFolder, fileName);

            if (zipFileToDelete.exists()) {
                zipFileToDelete.delete();
            } else {
                Log.d(TAG, "Update to delete not found");
                return;
            }

            String message = getString(R.string.delete_single_update_success_message, fileName);
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        } else if (!mExtraFolder.exists()) {
            Toast.makeText(mContext, R.string.delete_updates_noFolder_message, Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(mContext, R.string.delete_updates_failure_message, Toast.LENGTH_SHORT)
                    .show();
        }

        // Update the extras list
        updateExtrasLayout();

    }

    @Override
    public void onReady(ExtraPreference pref) {
        // TODO Auto-generated method stub

    }
}
