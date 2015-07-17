/*
 * Copyright (C) 2015 The MoKee OpenSource Project
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

package com.mokee.helper.widget;

import android.content.Context;
import android.mokee.utils.MoKeeUtils;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;
import com.mokee.helper.MoKeeApplication;
import com.mokee.helper.R;
import com.mokee.helper.fragments.MoKeeUpdaterFragment;

public class AdmobPreference extends Preference implements AdListener {

    private static AdView adView;
    private static View admobCustomView;

    public AdmobPreference(Context context) {
        super(context);
    }

    public AdmobPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdmobPreference(Context context, AttributeSet ui, int style) {
        super(context, ui, style);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        if (admobCustomView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            admobCustomView = inflater.inflate(R.layout.preference_admob, null);
            adView = (AdView) admobCustomView.findViewById(R.id.adView);
            adView.loadAd(new AdRequest());
            adView.setAdListener(this);
        }
        return admobCustomView;
    }

    @Override
    public void onDismissScreen(Ad ad) {
    }

    @Override
    public void onFailedToReceiveAd(Ad ad, ErrorCode errorCode) {
        if (errorCode.equals(ErrorCode.INTERNAL_ERROR) || errorCode.equals(ErrorCode.NETWORK_ERROR)) {
            if (MoKeeUtils.isOnline(MoKeeApplication.getContext())) {
                MoKeeUpdaterFragment.showAdBlockedAlert();
            }
        }
    }

    @Override
    public void onLeaveApplication(Ad ad) {
    }

    @Override
    public void onPresentScreen(Ad ad) {
    }

    @Override
    public void onReceiveAd(Ad ad) {
    }

}
