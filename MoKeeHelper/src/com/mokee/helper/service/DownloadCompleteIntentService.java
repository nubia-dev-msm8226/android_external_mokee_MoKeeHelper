
package com.mokee.helper.service;

import java.io.File;

import android.app.IntentService;
import android.content.Intent;
import android.os.UserHandle;

import com.mokee.helper.MoKeeApplication;
import com.mokee.helper.R;
import com.mokee.helper.db.DownLoadDao;
import com.mokee.helper.db.ThreadDownLoadDao;
import com.mokee.helper.misc.DownLoadInfo;
import com.mokee.helper.receiver.DownloadNotifier;
import com.mokee.helper.receiver.DownloadReceiver;
import com.mokee.helper.utils.DownLoader;
import com.mokee.helper.utils.MD5;

public class DownloadCompleteIntentService extends IntentService {

    public DownloadCompleteIntentService() {
        super(DownloadCompleteIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long id;
        int flag;
        String downloadedMD5;
        if (intent.hasExtra(DownLoadService.DOWNLOAD_FLAG) && intent.hasExtra(DownLoadService.DOWNLOAD_ID) &&
                intent.hasExtra(DownLoadService.DOWNLOAD_MD5)) {
            id = intent.getLongExtra(DownLoadService.DOWNLOAD_ID, -1);
            flag = intent.getIntExtra(DownLoadService.DOWNLOAD_FLAG, 1024);
            downloadedMD5 = intent.getStringExtra(DownLoadService.DOWNLOAD_MD5);
        } else if (intent.hasExtra(DownLoadService.DOWNLOAD_FLAG) && intent.hasExtra(DownLoadService.DOWNLOAD_EXTRAS_ID) &&
                intent.hasExtra(DownLoadService.DOWNLOAD_EXTRAS_MD5)) {
            id = intent.getLongExtra(DownLoadService.DOWNLOAD_EXTRAS_ID, -1);
            flag = intent.getIntExtra(DownLoadService.DOWNLOAD_FLAG, 1024);
            downloadedMD5 = intent.getStringExtra(DownLoadService.DOWNLOAD_EXTRAS_MD5);
        } else {
            return;
        }

        DownLoadInfo dli = DownLoadDao.getInstance().getDownLoadInfo(String.valueOf(id));
        if (dli == null) {
            return;
        }
        int status = dli.getState();
        Intent updateIntent = new Intent(DownloadReceiver.ACTION_NOTIFICATION_CLICKED);
        updateIntent.putExtra(DownLoadService.DOWNLOAD_FLAG, flag);

        if (status == DownLoader.STATUS_COMPLETE) {
            // Get the full path name of the downloaded file and the MD5

            // Strip off the .partial at the end to get the completed
            // file
            String partialFileFullPath = dli.getLocalFile();
            String completedFileFullPath = partialFileFullPath.replace(".partial", "");

            File partialFile = new File(partialFileFullPath);
            File updateFile = new File(completedFileFullPath);
            partialFile.renameTo(updateFile);

            // Start the MD5 check of the downloaded file
            if (MD5.checkMD5(downloadedMD5, updateFile)) {
             // We passed. Bring the main app to the foreground and trigger download completed
                updateIntent.putExtra(UpdateCheckService.EXTRA_FINISHED_DOWNLOAD_ID, id);
                updateIntent.putExtra(UpdateCheckService.EXTRA_FINISHED_DOWNLOAD_PATH, completedFileFullPath);
                displaySuccessResult(updateIntent, updateFile, flag);
            } else {
                // We failed. Clear the file and reset everything
                DownLoadDao.getInstance().delete(String.valueOf(id));
                if (updateFile.exists()) {
                    updateFile.delete();
                }
                displayErrorResult(updateIntent, R.string.md5_verification_failed);
            }
            //delete thread info
            ThreadDownLoadDao.getInstance().delete(dli.getUrl());
        } else if (status == DownLoader.STATUS_ERROR) {
            // The download failed, reset
            displayErrorResult(updateIntent, R.string.unable_to_download_file);
        }
    }

    private void displayErrorResult(Intent updateIntent, int failureMessageResId) {
        DownloadNotifier.notifyDownloadError(this, updateIntent, failureMessageResId);
    }

    private void displaySuccessResult(Intent updateIntent, File updateFile, int flag) {
        final MoKeeApplication app = (MoKeeApplication) getApplicationContext();
        if (app.isMainActivityActive()) {
            sendBroadcastAsUser(updateIntent, UserHandle.CURRENT_OR_SELF);
        } else {
            DownloadNotifier.notifyDownloadComplete(this, updateIntent, updateFile, flag);
        }
    }
}
