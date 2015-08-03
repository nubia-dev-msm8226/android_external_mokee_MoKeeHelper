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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.mokee.helper.R;
import com.mokee.helper.misc.Constants;
import com.mokee.helper.service.DownLoadService;

public class DownloadNotifier {

    private DownloadNotifier() {
        // Don't instantiate me bro
    }

    public static void notifyDownloadComplete(Context context,
            Intent updateIntent, File updateFile, int flag) {
        String updateUiName = updateFile.getName();

        // Set Notification Info
        int mContentTitleID, mTickerID, mActionTitleID, mTextID;
        if (flag == Constants.INTENT_FLAG_GET_UPDATE) {
            mContentTitleID = R.string.not_download_success;
            mTickerID = R.string.not_download_success;
            mActionTitleID = R.string.not_action_install_update;
            mTextID = updateUiName.startsWith("OTA") ? R.string.not_download_install_ota_notice : R.string.not_download_install_notice;
        } else {
            mContentTitleID = R.string.not_extras_download_success;
            mTickerID = R.string.not_extras_download_success;
            if (updateUiName.endsWith(".apk")) {
                mActionTitleID = R.string.not_apk_action_install_update;
            } else {
                mActionTitleID = R.string.not_action_install_update;
            }
            mTextID = R.string.not_extras_download_install_notice;
        }

        Notification.BigTextStyle style = new Notification.BigTextStyle()
                .setBigContentTitle(context.getString(mContentTitleID))
                .bigText(context.getString(mTextID, updateUiName));

        Notification.Builder builder = createBaseContentBuilder(context, updateIntent)
                .setColor(context.getResources().getColor(com.android.internal.R.color.system_notification_accent_color))
                .setSmallIcon(R.drawable.ic_mokee_updater)
                .setContentTitle(context.getString(mContentTitleID))
                .setContentText(updateUiName)
                .setTicker(context.getString(mTickerID))
                .setStyle(style)
                .addAction(R.drawable.ic_tab_install,
                        context.getString(mActionTitleID),
                        createInstallPendingIntent(context, updateFile, flag));

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(mContentTitleID, builder.build());
    }

    private static Notification.Builder createBaseContentBuilder(Context context,
            Intent updateIntent) {
        PendingIntent contentIntent = PendingIntent.getBroadcast(context, 1,
                updateIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);

        return new Notification.Builder(context)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .setAutoCancel(true);
    }

    public static void notifyDownloadError(Context context,
            Intent updateIntent, int failureMessageResId) {
        Notification.Builder builder = createBaseContentBuilder(context, updateIntent)
                .setColor(context.getResources().getColor(com.android.internal.R.color.system_notification_accent_color))
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle(context.getString(R.string.not_download_failure))
                .setContentText(context.getString(failureMessageResId))
                .setTicker(context.getString(R.string.not_download_failure));

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(R.string.not_download_success, builder.build());
    }

    private static PendingIntent createInstallPendingIntent(Context context, File updateFile,
            int flag) {
        Intent installIntent = new Intent(context, DownloadReceiver.class);
        installIntent.setAction(DownloadReceiver.ACTION_INSTALL_UPDATE);
        installIntent.putExtra(DownLoadService.DOWNLOAD_FLAG, flag);
        installIntent.putExtra(DownloadReceiver.EXTRA_FILENAME, updateFile.getName());

        return PendingIntent.getBroadcast(context, 0,
                installIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
