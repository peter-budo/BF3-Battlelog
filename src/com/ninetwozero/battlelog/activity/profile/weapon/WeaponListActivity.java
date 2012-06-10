/*
	This file is part of BF3 Battlelog

    BF3 Battlelog is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BF3 Battlelog is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 */

package com.ninetwozero.battlelog.activity.profile.weapon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.ninetwozero.battlelog.activity.CustomFragmentActivity;
import com.ninetwozero.battlelog.R;
import net.peterkuterna.android.apps.swipeytabs.SwipeyTabs;
import net.peterkuterna.android.apps.swipeytabs.SwipeyTabsPagerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.ninetwozero.battlelog.datatype.DefaultFragmentActivity;
import com.ninetwozero.battlelog.datatype.ProfileData;
import com.ninetwozero.battlelog.datatype.WeaponDataWrapper;
import com.ninetwozero.battlelog.http.ProfileClient;

public class WeaponListActivity extends CustomFragmentActivity implements DefaultFragmentActivity {

    // Attributes
    private ProfileData profileData;
    private Map<Long, List<WeaponDataWrapper>> items;
    private long selectedPersona;
    //private int selectedPosition;

    @Override
    public void onCreate(Bundle icicle) {

        // onCreate - save the instance state
        super.onCreate(icicle);

        // Get the intent
        if (!getIntent().hasExtra("profile")) {

            return;

        }

        // Get the profile
        profileData = getIntent().getParcelableExtra("profile");

        // Set the content view
        setContentView(R.layout.viewpager_default);

        // Let's setup the fragments too
        setup();

        // Last but not least - init
        init();

    }

    public void init() {

        // Are we there yet?
        // Set the selected persona
        selectedPersona = profileData.getPersona(0).getId();

    }

    @Override
    public void onResume() {

        super.onResume();

        // Reload
        reload();

    }

    public void setup() {

        // Do we need to setup the fragments?
        if (listFragments == null) {

            // Add them to the list
            listFragments = new Vector<Fragment>();
            listFragments.add(Fragment.instantiate(this, WeaponListFragment.class.getName()));

            // Iterate over the fragments
            for (int i = 0, max = listFragments.size(); i < max; i++) {

                ((WeaponListFragment) listFragments.get(i)).setViewPagerPosition(i);

            }

            // Get the ViewPager
            viewPager = (ViewPager) findViewById(R.id.viewpager);
            tabs = (SwipeyTabs) findViewById(R.id.swipeytabs);

            // Fill the PagerAdapter & set it to the viewpager
            pagerAdapter = new SwipeyTabsPagerAdapter(

                    fragmentManager,
                    new String[] {
                            "WEAPONS"
                    },
                    listFragments,
                    viewPager,
                    layoutInflater
                    );
            viewPager.setAdapter(pagerAdapter);
            tabs.setAdapter(pagerAdapter);

            // Make sure the tabs follow
            viewPager.setOnPageChangeListener(tabs);
            viewPager.setCurrentItem(0);

        }

    }

    public void reload() {

        new AsyncRefresh(this).execute();

    }

    public void doFinish() {
    }

    private class AsyncRefresh extends AsyncTask<Void, Void, Boolean> {

        // Attributes
        private Context context;

        // Construct
        public AsyncRefresh(Context c) {

            context = c;

        }

        @Override
        protected Boolean doInBackground(Void... arg) {

            try {

                items = new ProfileClient(profileData).getWeapons();
                return true;

            } catch (Exception ex) {

                ex.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (context != null) {

                if (result) {

                    ((WeaponListFragment) listFragments.get(viewPager.getCurrentItem()))
                            .showWeapons(items.get(selectedPersona));

                } else {

                    Toast.makeText(context, R.string.general_no_data, Toast.LENGTH_SHORT).show();

                }

            }
        }

    }

    public List<WeaponDataWrapper> getItemsForFragment(int p) {

        // Let's see if we got anything
        if (items == null || items.get(selectedPersona) == null) {

            return new ArrayList<WeaponDataWrapper>();

        } else {

            // Get the UnlockDataWrapper
            return items.get(selectedPersona);

            /*
             * UnlockDataunlockDataWrapper = unlocks.get(selectedPersona); // Is
             * the UnlockDataWrapper null? if (unlockDataWrapper == null) {
             * return new ArrayList<UnlockData>(); } // Switch over the position
             * switch (p) { case 0: return unlockDataWrapper.getWeapons(); case
             * 1: return unlockDataWrapper.getAttachments(); case 2: return
             * unlockDataWrapper.getKitUnlocks(); case 3: return
             * unlockDataWrapper.getVehicleUpgrades(); case 4: return
             * unlockDataWrapper.getSkills(); default: return null; }
             */

        }

    }

    public void open(WeaponDataWrapper w) {

        startActivity(new Intent(this, SingleWeaponActivity.class).putExtra("profile", profileData)
                .putExtra("weapon", w));

    }

}
