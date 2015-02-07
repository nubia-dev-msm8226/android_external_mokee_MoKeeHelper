/*
 * Copyright (C) 2014 The MoKee OpenSource Project
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

package com.mokee.helper.receiver;

import java.io.File;
import java.io.IOException;

import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.mokee.helper.MoKeeApplication;
import com.mokee.helper.R;
import com.mokee.helper.db.DownLoadDao;
import com.mokee.helper.misc.Constants;
import com.mokee.helper.misc.DownLoadInfo;
import com.mokee.helper.misc.ItemInfo;
import com.mokee.helper.service.DownLoadService;
import com.mokee.helper.service.DownloadCompleteIntentService;
import com.mokee.helper.utils.DownLoader;
import com.mokee.helper.utils.Utils;

public class DownloadReceiver extends BroadcastReceiver {
    private static final String TAG = "DownloadReceiver";

    public static final String ACTION_START_DOWNLOAD = "com.mokee.mkupdater.action.START_DOWNLOAD";
    public static final String EXTRA_UPDATE_INFO = "update_info";
    public static final String ACTION_DOWNLOAD_STARTED = "com.mokee.mkupdater.action.DOWNLOAD_STARTED";
    public static final String ACTION_NOTIFICATION_CLICKED = "com.mokee.mkupdater.action.NOTIFICATION_CLICKED";

    public static final String ACTION_INSTALL_UPDATE = "com.mokee.mkupdater.action.INSTALL_UPDATE";
    public static final String EXTRA_FILENAME = "filename";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        SharedPreferences prefs = context.getSharedPreferences(Constants.DOWNLOADER_PREF, 0);
        int flag = intent.getIntExtra(DownLoadService.DOWNLOAD_FLAG, Constants.INTENT_FLAG_GET_UPDATE);
        if (ACTION_START_DOWNLOAD.equals(action)) {
            ItemInfo ui = (ItemInfo) intent.getParcelableExtra(EXTRA_UPDATE_INFO);
            handleStartDownload(context, prefs, ui, flag);
        } else if (Intent.ACTION_SHUTDOWN.equals(action)) {
            String downloadUrl = prefs.getString(DownLoadService.DOWNLOAD_URL, "");
            if (!TextUtils.isEmpty(downloadUrl)) {
                DownLoadDao.getInstance().updataState(downloadUrl, DownLoader.STATUS_PAUSED);
            }
            String downloadExtrasUrl = prefs.getString(DownLoadService.DOWNLOAD_EXTRAS_URL, "");
            if (!TextUtils.isEmpty(downloadExtrasUrl)) {
                DownLoadDao.getInstance().updataState(downloadExtrasUrl, DownLoader.STATUS_PAUSED);
            }
            prefs.edit().remove(DownLoadService.DOWNLOAD_ID).remove(DownLoadService.DOWNLOAD_MD5)
                    .remove(DownLoadService.DOWNLOAD_URL).remove(DownLoadService.DOWNLOAD_EXTRAS_ID)
                    .remove(DownLoadService.DOWNLOAD_EXTRAS_MD5).remove(DownLoadService.DOWNLOAD_EXTRAS_URL).apply();
        } else if (DownLoadService.ACTION_DOWNLOAD_COMPLETE.equals(action)) { // 接收完成通知
            long id = intent.getLongExtra(DownLoadService.DOWNLOAD_ID, -1);
            handleDownloadComplete(context, prefs, id, flag);
        } else if (ACTION_INSTALL_UPDATE.equals(action)) {
            String fileName = intent.getStringExtra(EXTRA_FILENAME);
            if (flag == Constants.INTENT_FLAG_GET_UPDATE) {
                if (fileName.endsWith(".zip")) {
                    applyTriggerUpdate(context, fileName, true);
                }
            } else if (flag == Constants.INTENT_FLAG_GET_EXTRAS) {
                if (fileName.endsWith(".zip")) {
                    applyTriggerUpdate(context, fileName, false);
                } else if (fileName.endsWith(".apk")) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setDataAndType(Uri.parse("file://" + Utils.makeExtraFolder().getAbsolutePath() + "/"
                            + fileName), "application/vnd.android.package-archive");
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MoKeeApplication.getContext().startActivity(i);
                    Utils.cancelNotification(context);
                } else {
                    Toast.makeText(MoKeeApplication.getContext(),
                            R.string.extras_unsupported_toast, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void applyTriggerUpdate(Context context, String fileName, boolean isUpdate) {
        try {
            StatusBarManager sb = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
            sb.collapsePanels();
            Utils.cancelNotification(context);
            Utils.triggerUpdate(context, fileName, isUpdate);
        } catch (IOException e) {
            Log.e(TAG, "Unable to reboot into recovery mode", e);
            Toast.makeText(context, R.string.apply_unable_to_reboot_toast,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void handleStartDownload(Context context, SharedPreferences prefs, ItemInfo ui, int flag) {

        // If directory doesn't exist, create it
        File directory;
        if (flag == Constants.INTENT_FLAG_GET_UPDATE) {
            directory = Utils.makeUpdateFolder();
        } else {
            directory = Utils.makeExtraFolder();
        }

        if (!directory.exists()) {
            directory.mkdirs();
            Log.d(TAG, "UpdateFolder created");
        }

        // Build the name of the file to download, adding .partial at the end.
        // It will get
        // stripped off when the download completes
        String fullFilePath = directory.getAbsolutePath() + "/" + ui.getFileName() + ".partial";

        DownLoadInfo dli = DownLoadDao.getInstance().getDownLoadInfoByUrl(ui.getDownloadUrl());

        long downloadId;
        if (dli != null) {
            downloadId = Long.valueOf(dli.getDownID());
        } else {
            downloadId = System.currentTimeMillis();
        }

        if (DownLoadDao.getInstance().isHasInfos(ui.getDownloadUrl())) {
            DownLoadDao.getInstance().updataState(ui.getDownloadUrl(), DownLoader.STATUS_PENDING);
        }
        Intent intentBroadcast = new Intent(ACTION_DOWNLOAD_STARTED);
        intentBroadcast.putExtra(DownLoadService.DOWNLOAD_FLAG, flag);
        intentBroadcast.putExtra(DownLoadService.DOWNLOAD_ID, downloadId);
        context.sendBroadcastAsUser(intentBroadcast, UserHandle.CURRENT);

        // Store in shared preferences
        if (flag == Constants.INTENT_FLAG_GET_UPDATE) {// 区分扩展&更新
            prefs.edit().putLong(DownLoadService.DOWNLOAD_ID, downloadId)
                    .putString(DownLoadService.DOWNLOAD_MD5, ui.getMd5Sum())
                    .putString(DownLoadService.DOWNLOAD_URL, ui.getDownloadUrl()).apply();
        } else {
            prefs.edit().putLong(DownLoadService.DOWNLOAD_EXTRAS_ID, downloadId)
                    .putString(DownLoadService.DOWNLOAD_EXTRAS_MD5, ui.getMd5Sum())
                    .putString(DownLoadService.DOWNLOAD_EXTRAS_URL, ui.getDownloadUrl()).apply();
        }
        Intent intentService = new Intent(context, DownLoadService.class);
        intentService.setAction(DownLoadService.ACTION_DOWNLOAD);
        intentService.putExtra(DownLoadService.DOWNLOAD_TYPE, DownLoadService.ADD);
        intentService.putExtra(DownLoadService.DOWNLOAD_URL, ui.getDownloadUrl());
        intentService.putExtra(DownLoadService.DOWNLOAD_FILE_PATH, fullFilePath);
        intentService.putExtra(DownLoadService.DOWNLOAD_FLAG, flag);
        intentService.putExtra(DownLoadService.DOWNLOAD_ID, downloadId);
        MoKeeApplication.getContext().startServiceAsUser(intentService, UserHandle.CURRENT);
        Utils.cancelNotification(context);
    }

    private void handleDownloadComplete(Context context, SharedPreferences prefs, long id, int flag) {
        long enqueued = 0;
        switch (flag) {
            case Constants.INTENT_FLAG_GET_UPDATE:
                enqueued = prefs.getLong(DownLoadService.DOWNLOAD_ID, -1);
                if (enqueued < 0 || id < 0 || id != enqueued) {
                    return;
                }
                String downloadedMD5 = prefs.getString(DownLoadService.DOWNLOAD_MD5, "");

                // Send off to DownloadCompleteIntentService
                Intent updateintent = new Intent(context, DownloadCompleteIntentService.class);
                updateintent.putExtra(DownLoadService.DOWNLOAD_ID, id);
                updateintent.putExtra(DownLoadService.DOWNLOAD_MD5, downloadedMD5);
                updateintent.putExtra(DownLoadService.DOWNLOAD_FLAG, flag);
                context.startService(updateintent);

                // Clear the shared prefs
                prefs.edit().remove(DownLoadService.DOWNLOAD_ID)
                        .remove(DownLoadService.DOWNLOAD_MD5)
                        .remove(DownLoadService.DOWNLOAD_URL).apply();
                break;
            case Constants.INTENT_FLAG_GET_EXTRAS:
                enqueued = prefs.getLong(DownLoadService.DOWNLOAD_EXTRAS_ID, -1);
                if (enqueued < 0 || id < 0 || id != enqueued) {
                    return;
                }
                String extrasDownloadedMD5 = prefs.getString(DownLoadService.DOWNLOAD_EXTRAS_MD5, "");

                // Send off to DownloadCompleteIntentService
                Intent extrasintent = new Intent(context, DownloadCompleteIntentService.class);
                extrasintent.putExtra(DownLoadService.DOWNLOAD_EXTRAS_ID, id);
                extrasintent.putExtra(DownLoadService.DOWNLOAD_EXTRAS_MD5, extrasDownloadedMD5);
                extrasintent.putExtra(DownLoadService.DOWNLOAD_FLAG, flag);
                context.startService(extrasintent);

                // Clear the shared prefs
                prefs.edit().remove(DownLoadService.DOWNLOAD_EXTRAS_ID)
                        .remove(DownLoadService.DOWNLOAD_EXTRAS_MD5)
                        .remove(DownLoadService.DOWNLOAD_EXTRAS_URL).apply();
                break;
            default:
                break;
        }
    }
}
