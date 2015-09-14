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

package com.mokee.helper.service;

import java.io.File;

import android.app.IntentService;
import android.content.Intent;
import android.os.UserHandle;

import com.mokee.helper.MoKeeApplication;
import com.mokee.helper.R;
import com.mokee.helper.db.DownLoadDao;
import com.mokee.helper.db.ThreadDownLoadDao;
import com.mokee.helper.misc.Constants;
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
            flag = intent.getIntExtra(DownLoadService.DOWNLOAD_FLAG, Constants.INTENT_FLAG_GET_UPDATE);
            downloadedMD5 = intent.getStringExtra(DownLoadService.DOWNLOAD_MD5);
        } else if (intent.hasExtra(DownLoadService.DOWNLOAD_FLAG) && intent.hasExtra(DownLoadService.DOWNLOAD_EXTRAS_ID) &&
                intent.hasExtra(DownLoadService.DOWNLOAD_EXTRAS_MD5)) {
            id = intent.getLongExtra(DownLoadService.DOWNLOAD_EXTRAS_ID, -1);
            flag = intent.getIntExtra(DownLoadService.DOWNLOAD_FLAG, Constants.INTENT_FLAG_GET_EXTRAS);
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

            // Strip off the .partial at the end to get the completed file
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
                if (updateFile.exists()) {
                    updateFile.delete();
                }
                displayErrorResult(updateIntent, R.string.md5_verification_failed);
            }

            //delete info
            DownLoadDao.getInstance().delete(dli.getUrl());
            ThreadDownLoadDao.getInstance().delete(dli.getUrl());
        } else if (status == DownLoader.STATUS_ERROR || status == DownLoader.STATUS_DELETE) {
            // The download failed, reset
            displayErrorResult(updateIntent, R.string.unable_to_download_file);
            DownLoadDao.getInstance().updataState(dli.getUrl(), DownLoader.STATUS_PAUSED);
        }
    }

    private void displayErrorResult(Intent updateIntent, int failureMessageResId) {
        DownloadNotifier.notifyDownloadError(this, updateIntent, failureMessageResId);
    }

    private void displaySuccessResult(Intent updateIntent, File updateFile, int flag) {
        final MoKeeApplication app = (MoKeeApplication) getApplicationContext();
        if (app.isMainActivityActive()) {
            sendBroadcastAsUser(updateIntent, UserHandle.CURRENT);
        } else {
            DownloadNotifier.notifyDownloadComplete(this, updateIntent, updateFile, flag);
        }
    }
}
