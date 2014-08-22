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
import java.util.Map;

import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

public class UpdatesJsonObjectRequest extends JsonObjectRequest {
    private String mUserAgent;
    private HashMap<String, String> mHeaders = new HashMap<String, String>();

    public UpdatesJsonObjectRequest(String url, String userAgent, JSONObject jsonRequest,
            Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(url, jsonRequest, listener, errorListener);
        mUserAgent = userAgent;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if (mUserAgent != null) {
            mHeaders.put("User-Agent", mUserAgent);
        }
        mHeaders.put("Cache-Control", "no-cache");
        return mHeaders;
    }

    public void addHeader(String key, String what) {
        mHeaders.put(key, what);
    }
}
