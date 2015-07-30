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

package com.mokee.helper.receiver;

import com.mokee.helper.R;
import com.mokee.helper.fragments.MoKeeUpdaterFragment;
import com.mokee.helper.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PaymentReceiver extends BroadcastReceiver {

    private static final String ACTION_PAYMENT_SUCCESS = "com.mokee.pay.action.PAYMENT_SUCCESS";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ACTION_PAYMENT_SUCCESS)) {
            String packagename = intent.getStringExtra("packagename");
            if (packagename.equals(Utils.getPackageName(context))) {
                Toast.makeText(context, R.string.donate_money_toast_success, Toast.LENGTH_LONG).show();
                MoKeeUpdaterFragment.refreshOption();
            }
        }
    }
}
