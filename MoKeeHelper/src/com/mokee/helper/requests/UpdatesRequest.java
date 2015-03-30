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

package com.mokee.helper.requests;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.mokee.helper.MoKeeApplication;
import com.mokee.helper.fragments.MoKeeUpdaterFragment;
import com.mokee.helper.misc.Constants;
import com.mokee.helper.utils.Utils;

public class UpdatesRequest extends StringRequest {
    private String mUserAgent;

    public UpdatesRequest(int method, String url, String userAgent,
            Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        mUserAgent = userAgent;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<String, String>();
        if (mUserAgent != null) {
            headers.put("User-Agent", mUserAgent);
        }
        headers.put("Cache-Control", "no-cache");

        Locale mLocale = MoKeeApplication.getContext().getResources().getConfiguration().locale;
        String language = mLocale.getLanguage();
        String country = mLocale.getCountry();
        headers.put("Accept-Language", (language + "-" + country).toLowerCase(Locale.ENGLISH));

        return headers;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> params = new HashMap<String, String>();
        // Get the type of update we should check for
        SharedPreferences prefs = MoKeeApplication.getContext().getSharedPreferences(Constants.DOWNLOADER_PREF, 0);
        String MoKeeVersionType = Utils.getMoKeeVersionType();
        boolean isNightly = TextUtils.equals(MoKeeVersionType, "nightly");
        boolean isExperimental = TextUtils.equals(MoKeeVersionType, "experimental");
        boolean isUnofficial = TextUtils.equals(MoKeeVersionType, "unofficial");
        boolean experimentalShow = prefs.getBoolean(MoKeeUpdaterFragment.EXPERIMENTAL_SHOW,
                isExperimental);
        int updateType = prefs.getInt(Constants.UPDATE_TYPE_PREF, isUnofficial ? 3
                : isExperimental ? 2 : isNightly ? 1 : 0);// 版本类型参数
        if (updateType == 2 && !experimentalShow) {
            prefs.edit().putBoolean(MoKeeUpdaterFragment.EXPERIMENTAL_SHOW, false)
                    .putInt(Constants.UPDATE_TYPE_PREF, 0).apply();
            updateType = 0;
        }
        if (!isUnofficial && updateType == 3) {
            prefs.edit().putInt(Constants.UPDATE_TYPE_PREF, 0).apply();
            updateType = 0;
        }
        boolean isOTA = prefs.getBoolean(Constants.OTA_CHECK_PREF, true);
        params.put("device_name", Utils.getDeviceType());
        params.put("device_version", Utils.getInstalledVersion());
        params.put("build_user", Utils.getBuildUser());
        if (!isOTA) {
            params.put("device_officail", String.valueOf(updateType));
            params.put("rom_all", "0");
        }
        return params;
    }
}
