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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class Utils {
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

}
