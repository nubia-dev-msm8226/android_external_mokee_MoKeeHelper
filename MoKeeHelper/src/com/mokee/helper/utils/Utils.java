/*
 * Copyright (C) 2014-2015 The MoKee OpenSource Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mokee.helper.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceFragment;

import com.mokee.helper.R;
import com.mokee.helper.db.DownLoadDao;
import com.mokee.helper.db.ThreadDownLoadDao;
import com.mokee.helper.misc.Constants;
import com.mokee.helper.misc.DownLoadInfo;
import com.mokee.helper.service.UpdateCheckService;
import com.mokee.os.Build;
import com.mokee.security.License;

public class Utils {

    public static File makeUpdateFolder() {
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                Constants.UPDATES_FOLDER);
    }

    public static File makeExtraFolder() {
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                Constants.EXTRAS_FOLDER);
    }

    /**
     * 检测rom是否已下载
     */
    public static boolean isLocaUpdateFile(String fileName, boolean isUpdate) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
                + (isUpdate ? Constants.UPDATES_FOLDER : Constants.EXTRAS_FOLDER), fileName);
        return file.exists();
    }

    public static void cancelNotification(Context context) {
        final NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(R.string.not_new_updates_found_title);
        nm.cancel(R.string.not_download_success);
    }

    public static String getMoKeeVersionTypeString(Context mContext) {
        String MoKeeVersionType = getMoKeeVersionType();
        if (MoKeeVersionType.equals("release"))
            return mContext.getString(R.string.mokee_version_type_release);
        else if (MoKeeVersionType.equals("experimental"))
            return mContext.getString(R.string.mokee_version_type_experimental);
        else if (MoKeeVersionType.equals("history"))
            return mContext.getString(R.string.mokee_version_type_history);
        else if (MoKeeVersionType.equals("nightly"))
            return mContext.getString(R.string.mokee_version_type_nightly);
        else if (MoKeeVersionType.equals("unofficial"))
            return mContext.getString(R.string.mokee_version_type_unofficial);
        else
            return mContext.getString(R.string.mokee_info_default);
    }

    public static String getMoKeeVersionType() {
        String MoKeeVersion = Build.MOKEE_VERSION;
        String MoKeeVersionType = MoKeeVersion.substring(MoKeeVersion.lastIndexOf("-") + 1, MoKeeVersion.length()).toLowerCase(Locale.ENGLISH);
        return MoKeeVersionType;
    }

    public static long getVersionLifeTime(String versionType) {
        if (versionType.equals("release")) {
            return 86400000 * 60; 
        } else {
            return 86400000 * 7;
        }
    }

    public static void triggerUpdate(Context context, String updateFileName, boolean isUpdate)
            throws IOException {
        /*
         * Should perform the following steps.
         * 1.- mkdir -p /cache/recovery
         * 2.- echo 'boot-recovery' > /cache/recovery/command
         * 3.- if(mBackup) echo '--nandroid'  >> /cache/recovery/command
         * 4.- echo '--update_package=SDCARD:update.zip' >> /cache/recovery/command
         * 5.- reboot recovery
         */

        // Set the 'boot recovery' command
        Process p = Runtime.getRuntime().exec("sh");
        OutputStream os = p.getOutputStream();
        os.write("mkdir -p /cache/recovery/\n".getBytes());
        os.write("echo 'boot-recovery' >/cache/recovery/command\n".getBytes());

        // See if backups are enabled and add the nandroid flag
        /* TODO: add this back once we have a way of doing backups that is not recovery specific
           if (mPrefs.getBoolean(Constants.BACKUP_PREF, true)) {
           os.write("echo '--nandroid'  >> /cache/recovery/command\n".getBytes());
           }
           */

        // Add the update folder/file name
        String primaryStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        // If data media rewrite the path to bypass the sd card fuse layer and trigger uncrypt
        String directPath = Environment.getMediaStorageDirectory().getAbsolutePath();
        String updatePath = Environment.isExternalStorageEmulated() ? directPath :
                primaryStoragePath;
        String cmd = "echo '--update_package=" + updatePath + "/" + (isUpdate ? Constants.UPDATES_FOLDER : Constants.EXTRAS_FOLDER) + "/"
                + updateFileName + "' >> /cache/recovery/command\n";
        os.write(cmd.getBytes());
        os.flush();

        // Trigger the reboot
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        powerManager.reboot("recovery");
    }

    public static void scheduleUpdateService(Context context, int updateFrequency) {
        // Load the required settings from preferences
        SharedPreferences prefs = context.getSharedPreferences(Constants.DOWNLOADER_PREF, 0);
        long lastCheck = prefs.getLong(Constants.LAST_UPDATE_CHECK_PREF, 0);

        // Get the intent ready
        Intent i = new Intent(context, UpdateCheckService.class);
        i.setAction(UpdateCheckService.ACTION_CHECK);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        // Clear any old alarms and schedule the new alarm
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);

        if (updateFrequency != Constants.UPDATE_FREQ_NONE) {
            am.setRepeating(AlarmManager.RTC_WAKEUP, lastCheck + updateFrequency, updateFrequency, pi);
        }
    }

    public static String getUserAgentString(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.packageName + "/" + pi.versionName;
        } catch (PackageManager.NameNotFoundException nnfe) {
            return null;
        }
    }

    public static String getPackageName(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.packageName;
        } catch (PackageManager.NameNotFoundException nnfe) {
            return null;
        }
    }

    /**
     * 截取日期
     */
    public static String subBuildDate(String name, boolean sameVersion) {
        String[] strs = name.split("-");
        String date;
        if (name.toLowerCase(Locale.ENGLISH).startsWith("ota")) {
            date = strs[4];
        } else {
            date = strs[2];
        }
        if (!isNum(date)) return "0";
        if (date.startsWith("20")) {
            date = date.substring(2, date.length());
        }
        if (!sameVersion) {
            if (date.length() > 6) {
                date = date.substring(0, 6);
            }
        }
        return date;
    }

    public static boolean isNum(String str) {
        return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
    }

    /**
     * 截取日期长度
     */
    public static int getBuildDateLength(String name) {
        String[] strs = name.split("-");
        String date = strs[2];
        if (date.startsWith("20")) {
            date = date.substring(2, date.length());
        }
        return date.length();
    }

    /**
     * 截取版本
     */
    public static String subMoKeeVersion(String name) {
        String[] strs = name.split("-");
        String version = strs[0];
        if (name.toLowerCase(Locale.ENGLISH).startsWith("ota")) {
            version = strs[1];
        }
        version = version.substring(2, version.length());
        return version;
    }

    /**
     * 判断版本新旧
     */
    public static boolean isNewVersion(String itemName) {
        int nowDateLength = getBuildDateLength(Build.MOKEE_VERSION);
        int itemDateLength = getBuildDateLength(itemName);
        boolean sameVersion = (nowDateLength == itemDateLength);
        int nowDate = Integer.valueOf(subBuildDate(Build.MOKEE_VERSION, sameVersion));
        int itemDate = Integer.valueOf(subBuildDate(itemName, sameVersion));
        float nowVersion = Float.valueOf(subMoKeeVersion(Build.MOKEE_VERSION));
        float itemVersion = Float.valueOf(subMoKeeVersion(itemName));
        return (itemDate > nowDate && itemVersion >= nowVersion);
    }

    public static void setSummaryFromString(PreferenceFragment prefFragment, String preference,
            String value) {
        if (prefFragment == null) {
            return;
        }
        try {
            prefFragment.findPreference(preference).setSummary(value);
        } catch (RuntimeException e) {
            prefFragment.findPreference(preference).setSummary(
                    prefFragment.getActivity().getString(R.string.mokee_info_default));
        }
    }

    public static boolean checkGmsVersion(String version) {
        String line = null;
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader("/system/etc/gprop.mokee"), 512);
            line = reader.readLine();

            return (!line.equals(version));
        } catch (IOException e) {
            return true;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                // ignored, not much we can do anyway
            }
        }
    }

    /**
     * 文件目录清理
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        } else {
            DownLoadInfo dli = null;
            if (dir.getName().endsWith(".partial")) {
                dli = DownLoadDao.getInstance().getDownLoadInfoByName(dir.getName());
            } else {
                dli = DownLoadDao.getInstance().getDownLoadInfoByName(dir.getName() + ".partial");
            }
            if (dli != null) {
                ThreadDownLoadDao.getInstance().delete(dli.getUrl());
                DownLoadDao.getInstance().delete(dli.getUrl());
            }
        }
        // The directory is now empty so delete it
        File newFile = new File(dir.getAbsolutePath() + System.currentTimeMillis());
        dir.renameTo(newFile);
        return newFile.delete();
    }

    public static Float getPaidTotal(Context mContext) {
        if (new File(Constants.LICENSE_FILE).exists()) {
            try {
                String licenseInfo[] = License.readLincense(Constants.LICENSE_FILE, Constants.PUB_KEY).split(" ");
                if (licenseInfo[0].equals(Build.getUniqueID(mContext)) && licenseInfo[1].equals(Utils.getPackageName(mContext))) {
                    return Float.valueOf(licenseInfo[licenseInfo.length - 1]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return 0f;
        }
        return 0f;
    }

    public static boolean checkLicensed(Context mContext) {
        return getPaidTotal(mContext) >= Constants.DONATION_TOTAL;
    }
}
