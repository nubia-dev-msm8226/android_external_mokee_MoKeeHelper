
package com.mokee.helper.receiver;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.mokee.helper.R;
import com.mokee.helper.misc.Constants;
import com.mokee.helper.misc.ItemInfo;

public class DownloadNotifier {

    private DownloadNotifier() {
        // Don't instantiate me bro
    }

    public static void notifyDownloadComplete(Context context,
            Intent updateIntent, File updateFile, int flag) {
        String updateUiName = ItemInfo.extractUiName(updateFile.getName());

        // Set Notification Info
        int mContentTitleID, mTickerID, mActionTitleID, mTextID;
        if (flag == Constants.INTENT_FLAG_GET_UPDATE) {
            mContentTitleID = R.string.not_download_success;
            mTickerID = R.string.not_download_success;
            mActionTitleID = R.string.not_action_install_update;
            mTextID = R.string.not_download_install_notice;
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
        installIntent.putExtra("flag", flag);
        installIntent.putExtra(DownloadReceiver.EXTRA_FILENAME, updateFile.getName());

        return PendingIntent.getBroadcast(context, 0,
                installIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
