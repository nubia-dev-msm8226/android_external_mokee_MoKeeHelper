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

package com.mokee.helper.utils;

import com.mokee.helper.R;
import com.mokee.helper.misc.Constants;

import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.PowerManager;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class Utils {

    public static File makeUpdateFolder() {
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                Constants.UPDATES_FOLDER);
    }

    public static File makeExtraFolder() {
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                Constants.EXTRAS_FOLDER);
    }

    public static void cancelNotification(Context context) {
        final NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(R.string.not_new_updates_found_title);
        nm.cancel(R.string.not_download_success);
    }

    public static boolean isApkInstalled(String packagename, Context context) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(
                    packagename, 0);

        } catch (NameNotFoundException e) {
            packageInfo = null;
        }

        if (packageInfo == null) {
            return false;
        } else {
            return true;
        }
    }

    public static String getMoKeeVersionTypeString(String version, Context context) {
        String MoKeeVersionType = version.substring(version.lastIndexOf("-") + 1,
                version.length()).toLowerCase();
        if (MoKeeVersionType.equals("release"))
            return context.getString(R.string.mokee_version_type_release);
        else if (MoKeeVersionType.equals("experimental"))
            return context.getString(R.string.mokee_version_type_experimental);
        else if (MoKeeVersionType.equals("nightly"))
            return context.getString(R.string.mokee_version_type_nightly);
        else
            return context.getString(R.string.mokee_version_type_unofficial);
    }

    public static void triggerUpdate(Context context, String updateFileName, boolean isUpdate) throws IOException {
        /*
         * Should perform the following steps. 1.- mkdir -p /cache/recovery 2.-
         * echo 'boot-recovery' > /cache/recovery/command 3.- if(mBackup) echo
         * '--nandroid' >> /cache/recovery/command 4.- echo
         * '--update_package=SDCARD:update.zip' >> /cache/recovery/command 5.-
         * reboot recovery
         */

        // Set the 'boot recovery' command
        Process p = Runtime.getRuntime().exec("sh");
        OutputStream os = p.getOutputStream();
        os.write("mkdir -p /cache/recovery/\n".getBytes());
        os.write("echo 'boot-recovery' >/cache/recovery/command\n".getBytes());

        // See if backups are enabled and add the nandroid flag
        /*
         * TODO: add this back once we have a way of doing backups that is not
         * recovery specific if (mPrefs.getBoolean(Constants.BACKUP_PREF, true))
         * {
         * os.write("echo '--nandroid'  >> /cache/recovery/command\n".getBytes(
         * )); }
         */

        // Add the update folder/file name
        // Emulated external storage moved to user-specific paths in 4.2
        String userPath = Environment.isExternalStorageEmulated() ? ("/" + UserHandle.myUserId())
                : "";

        String cmd = "echo '--update_package=" + getStorageMountpoint(context) + userPath
                + "/" + (isUpdate ? Constants.UPDATES_FOLDER :Constants.EXTRAS_FOLDER) + "/" + updateFileName
                + "' >> /cache/recovery/command\n";
        os.write(cmd.getBytes());
        os.flush();

        // Trigger the reboot
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        powerManager.reboot("recovery");
    }

    private static String getStorageMountpoint(Context context) {
        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] volumes = sm.getVolumeList();
        String primaryStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        boolean alternateIsInternal = context.getResources().getBoolean(R.bool.alternateIsInternal);

        if (volumes.length <= 1) {
            // single storage, assume only /sdcard exists
            return "/sdcard";
        }

        for (int i = 0; i < volumes.length; i++) {
            StorageVolume v = volumes[i];
            if (v.getPath().equals(primaryStoragePath)) {
                if (!v.isRemovable() && alternateIsInternal) {
                    return "/emmc";
                }
            }
            ;
        }
        // Not found, assume non-alternate
        return "/sdcard";
    }
}
