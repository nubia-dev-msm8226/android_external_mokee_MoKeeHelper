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

import com.mokee.helper.R;
import com.mokee.helper.adapters.TabsAdapter;
import com.mokee.helper.fragments.MoKeeSupportFragment;
import com.mokee.helper.fragments.MoKeeUpdaterFragment;
import com.mokee.helper.fragments.MoKeeExtrasFragment;
import com.mokee.helper.misc.Constants;
import com.mokee.helper.service.UpdateCheckService;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

public class MoKeeCenter extends FragmentActivity {

    public static final String ACTION_MOKEE_CENTER = "com.mokee.mkupdater.action.MOKEE_CENTER";
    public static final String KEY_MOKEE_SERVICE = "key_mokee_service";
    public static final String KEY_MOKEE_UPDATER = "key_mokee_updater";
    public static final String BR_ONNewIntent = "onNewIntent";
    private ViewPager mViewPager;
    private TabsAdapter mTabsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.viewPager);
        setContentView(mViewPager);

        final ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setTitle(R.string.mokee_center_title);

        mTabsAdapter = new TabsAdapter(this, mViewPager);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.mokee_updater_title),
                MoKeeUpdaterFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.mokee_extras_title),
                MoKeeExtrasFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.mokee_support_title),
                MoKeeSupportFragment.class, null);
        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }

        // Turn on the Options Menu
        invalidateOptionsMenu();
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
        send.putExtra("flag", intent.getIntExtra("flag", Constants.INTENT_FLAG_GET_UPDATE));
        sendBroadcast(send);
    }
}
