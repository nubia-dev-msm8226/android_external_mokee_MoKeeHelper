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

package com.mokee.helper.activities;

import org.json.JSONException;
import org.mokee.util.MoKeeUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.settings.mkstats.Utilities;
import com.mokee.helper.R;
import com.mokee.helper.adapters.TabsAdapter;
import com.mokee.helper.fragments.MoKeeExtrasFragment;
import com.mokee.helper.fragments.MoKeeSupportFragment;
import com.mokee.helper.fragments.MoKeeUpdaterFragment;
import com.mokee.helper.misc.Constants;
import com.mokee.helper.service.DownLoadService;
import com.mokee.helper.service.UpdateCheckService;
import com.mokee.helper.utils.PayPal;
import com.mokee.helper.utils.Utils;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.wanpu.pay.PayConnect;
import com.wanpu.pay.PayResultListener;

public class MoKeeCenter extends FragmentActivity {

    public static final String ACTION_MOKEE_CENTER = "com.mokee.mkupdater.action.MOKEE_CENTER";
    public static final String KEY_MOKEE_SERVICE = "key_mokee_service";
    public static final String KEY_MOKEE_UPDATER = "key_mokee_updater";
    public static final String BR_ONNewIntent = "onNewIntent";
    private ActionBar bar;
    private ViewPager mViewPager;
    private TabsAdapter mTabsAdapter;
    private static Context mContext;
    private static EditText mEditText;
    private static boolean initialized = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.viewPager);
        setContentView(mViewPager);

        mContext = getApplicationContext();

        bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE);
        bar.setTitle(R.string.mokee_center_title);

        mTabsAdapter = new TabsAdapter(this, mViewPager);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.mokee_extras_title), MoKeeExtrasFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.mokee_updater_title), MoKeeUpdaterFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.mokee_support_title), MoKeeSupportFragment.class, null);
        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 1));
        }
        bar.setSelectedNavigationItem(1);
        // Turn on the Options Menu
        invalidateOptionsMenu();

        // Start service when create
        if (getResources().getBoolean(R.bool.use_paypal) && !MoKeeUtils.isChineseLanguage() || getResources().getBoolean(R.bool.use_paypal) && MoKeeUtils.isTWLanguage()) {
            Intent intent = new Intent(this, PayPalService.class);
            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, PayPal.config);
            startServiceAsUser(intent, UserHandle.CURRENT);
        } else {
            initialized = false;
        }
        PayConnect.getInstance("179a03b58d0dc099e7770f1f5e1f8887", Utils.getMoKeeVersionTypeString(this), this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null) {
            int flag = intent.getIntExtra(DownLoadService.DOWNLOAD_FLAG, Constants.INTENT_FLAG_GET_UPDATE);
            if (flag == Constants.INTENT_FLAG_GET_EXTRAS) {
                bar.setSelectedNavigationItem(0);
            } else if (flag == Constants.INTENT_FLAG_GET_UPDATE) {
                bar.setSelectedNavigationItem(1);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }

    @Override
    protected void onNewIntent(Intent intent) {

        // super.onNewIntent(intent);
        Intent send = new Intent(BR_ONNewIntent);
        send.putExtra(UpdateCheckService.EXTRA_UPDATE_LIST_UPDATED,
                intent.getBooleanExtra(UpdateCheckService.EXTRA_UPDATE_LIST_UPDATED, false));
        send.putExtra(UpdateCheckService.EXTRA_FINISHED_DOWNLOAD_ID,
                intent.getLongExtra(UpdateCheckService.EXTRA_FINISHED_DOWNLOAD_ID, -1));
        send.putExtra(UpdateCheckService.EXTRA_FINISHED_DOWNLOAD_PATH,
                intent.getStringExtra(UpdateCheckService.EXTRA_FINISHED_DOWNLOAD_PATH));
        send.putExtra(DownLoadService.DOWNLOAD_FLAG, intent.getIntExtra(DownLoadService.DOWNLOAD_FLAG, Constants.INTENT_FLAG_GET_UPDATE));
        sendBroadcastAsUser(send, UserHandle.CURRENT);
    }

    public static void donateButton(final Activity mContext) {

        DialogInterface.OnClickListener mDialogButton = new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String priceStr = mEditText.getText().toString().trim();
                String orderId = System.currentTimeMillis() + "";
                String userId = Utilities.getUniqueID(mContext);
                if (TextUtils.isEmpty(priceStr)) {
                    Toast.makeText(mContext, R.string.donate_money_toast_error, Toast.LENGTH_SHORT).show();
                } else {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            PayConnect.getInstance(mContext).aliPay(mContext, orderId, userId, Float.valueOf(priceStr),
                                    mContext.getString(R.string.donate_money_name), mContext.getString(R.string.donate_money_description), "",
                                    mPayResultListener);
                            break;
                        case DialogInterface.BUTTON_NEUTRAL:
                            PayConnect.getInstance(mContext).tclBankPay(mContext, orderId, userId, Float.valueOf(priceStr),
                                    mContext.getString(R.string.donate_money_name), mContext.getString(R.string.donate_money_description), "",
                                    mPayResultListener);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            if (MoKeeUtils.isChineseLanguage() && !MoKeeUtils.isTWLanguage()) {
                                PayConnect.getInstance(mContext).tenPay(mContext, orderId, userId, Float.valueOf(priceStr),
                                        mContext.getString(R.string.donate_money_name), mContext.getString(R.string.donate_money_description), "",
                                        mPayResultListener);
                            } else {
                                if (initialized) {
                                    PayPal.onPayPalDonatePressed(mContext, priceStr, mContext.getString(R.string.donate_money_description));
                                } else {
                                    MoKeeSupportFragment.goToURL(mContext, MoKeeSupportFragment.URL_MOKEE_DONATE);
                                }
                            }
                            break;
                    }
                }
            }
        };
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View donateView = inflater.inflate(R.layout.donate, null);
        mEditText = (EditText) donateView.findViewById(R.id.money_price);
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.donate_dialog_title)
                .setMessage(R.string.donate_dialog_message)
                .setView(donateView)
                .setPositiveButton(R.string.donate_dialog_via_alipay, mDialogButton)
                .setNeutralButton(R.string.donate_dialog_via_unionpay, mDialogButton)
                .setNegativeButton(MoKeeUtils.isChineseLanguage() && !MoKeeUtils.isTWLanguage() ? R.string.donate_dialog_via_tenpay : R.string.donate_dialog_via_paypal, mDialogButton).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayPal.REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i(PayPal.TAG, confirm.toJSONObject().toString(4));
                        Log.i(PayPal.TAG, confirm.getPayment().toJSONObject().toString(4));
                        Toast.makeText(mContext, R.string.donate_money_toast_success, Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        Log.e(PayPal.TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(PayPal.TAG, "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(PayPal.TAG, "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }

    @Override
    public void onDestroy() {
        // Stop service when done
        if (initialized) {
            stopServiceAsUser(new Intent(this, PayPalService.class), UserHandle.CURRENT);
        }
        PayConnect.getInstance(this).close();
        super.onDestroy();
    }

    private static PayResultListener mPayResultListener = new PayResultListener() {

        @Override
        public void onPayFinish(Context payViewContext, String orderId,
                int resultCode, String resultString, int payType, float amount,
                String goodsName) {
            if (resultCode == 0) {
                Toast.makeText(mContext, R.string.donate_money_toast_success, Toast.LENGTH_LONG).show();
                PayConnect.getInstance(mContext).closePayView(payViewContext);
                PayConnect.getInstance(mContext).confirm(orderId,payType);
            }
        }
    };
}
