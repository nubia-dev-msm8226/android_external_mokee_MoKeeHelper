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

package com.mokee.helper;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.mokee.utils.MoKeeUtils;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.mokee.helper.activities.MoKeeCenter;
import com.mokee.helper.utils.Utils;
import com.mokee.os.Build;
import com.mokee.os.Build$VERSION;

public class MoKeeApplication extends Application implements
        Application.ActivityLifecycleCallbacks {

    private static Context context;
    private boolean mMainActivityActive;
    private RequestQueue mRequestQueue;
    private SharedPreferences prefs;

    private static final String TAG = "MoKeeApplication";
    private static final String MKPUSH_PREF = "mokee_push";
    private static final String MKPUSH_ALIAS = "pref_alias";
    private static final String MKPUSH_TAGS = "pref_tags";

    private static final int MSG_SET_ALIAS = 1001;
    private static final int MSG_SET_TAGS = 1002;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SET_ALIAS:
                    Log.d(TAG, "Set alias in handler.");
                    JPushInterface.setAliasAndTags(getApplicationContext(), (String) msg.obj, null,
                            mAliasCallback);
                    break;
                case MSG_SET_TAGS:
                    Log.d(TAG, "Set tags in handler.");
                    JPushInterface.setAliasAndTags(getApplicationContext(), null,
                            (Set<String>) msg.obj, mTagsCallback);
                    break;
                default:
                    Log.i(TAG, "Unhandled msg - " + msg.what);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mMainActivityActive = false;
        registerActivityLifecycleCallbacks(this);
        mRequestQueue = Volley.newRequestQueue(this);
        context = getApplicationContext();

        // MoKeePush Interface
        prefs = context.getSharedPreferences(MKPUSH_PREF, 0);
        JPushInterface.setDebugMode(false);
        JPushInterface.init(this);
        // Set Alias
        String alias = Build.getUniqueID(this);
        String prefAlias = prefs.getString(MKPUSH_ALIAS, null);
        if (!alias.equals(prefAlias))
            mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS, alias));
        // Set Tags
        Set<String> tags = new HashSet<String>();
        tags.add(Build.PRODUCT);
        tags.add(Build$VERSION.CODENAME.replace(".", ""));
        Set<String> prefTags = prefs.getStringSet(MKPUSH_TAGS, null);
        if (!tags.equals(prefTags))
            mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_TAGS, tags));
    }

    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs;
            switch (code) {
                case 0:
                    logs = "Set alias success";
                    prefs.edit().putString(MKPUSH_ALIAS, alias).apply();
                    Log.i(TAG, logs);
                    break;
                case 6002:
                    logs = "Failed to set alias due to timeout. Try again after 60s.";
                    Log.i(TAG, logs);
                    if (MoKeeUtils.isOnline(getContext())) {
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ALIAS, alias),
                                1000 * 60);
                    } else {
                        Log.i(TAG, "No network");
                    }
                    break;
                default:
                    logs = "Failed with errorCode = " + code;
                    Log.e(TAG, logs);
            }
        }
    };

    private final TagAliasCallback mTagsCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs;
            switch (code) {
                case 0:
                    logs = "Set tag success";
                    prefs.edit().putStringSet(MKPUSH_TAGS, tags).apply();
                    Log.i(TAG, logs);
                    break;
                case 6002:
                    logs = "Failed to set tags due to timeout. Try again after 60s.";
                    Log.i(TAG, logs);
                    if (MoKeeUtils.isOnline(getContext())) {
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_TAGS, tags),
                                1000 * 60);
                    } else {
                        Log.i(TAG, "No network");
                    }
                    break;
                default:
                    logs = "Failed with errorCode = " + code;
                    Log.e(TAG, logs);
            }
        }

    };

    public static Context getContext() {
        return context;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activity instanceof MoKeeCenter) {
            mMainActivityActive = true;
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity instanceof MoKeeCenter) {
            mMainActivityActive = false;
        }
    }

    public boolean isMainActivityActive() {
        return mMainActivityActive;
    }

    public RequestQueue getQueue() {
        return mRequestQueue;
    }
}
