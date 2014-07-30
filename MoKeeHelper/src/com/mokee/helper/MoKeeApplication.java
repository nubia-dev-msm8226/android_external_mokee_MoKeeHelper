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
import android.os.Bundle;
import cn.jpush.android.api.JPushInterface;

import com.android.settings.mkstats.Utilities;
import com.mokee.helper.activities.MoKeeCenter;
import com.mokee.helper.utils.Utils;

public class MoKeeApplication extends Application implements
        Application.ActivityLifecycleCallbacks {
    private static Context context;
    private boolean mMainActivityActive;

    @Override
    public void onCreate() {
        super.onCreate();
        mMainActivityActive = false;
        registerActivityLifecycleCallbacks(this);
        context = getApplicationContext();

        // MoKeePush Interface
        JPushInterface.setDebugMode(false);
        JPushInterface.init(this);
        Set<String> tags = new HashSet<String>();
        tags.add(Utils.getDeviceType());
        tags.add(Utilities.getMoKeeVersion());
        JPushInterface.setAliasAndTags(this, Utilities.getUniqueID(this), tags);
    }

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
}
