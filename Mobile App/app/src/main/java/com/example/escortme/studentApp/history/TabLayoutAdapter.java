package com.example.escortme.studentApp.history;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabLayoutAdapter extends FragmentPagerAdapter {

    int tabs;
    public TabLayoutAdapter(FragmentManager fragmentManager , int totalTabs) {
        super(fragmentManager);
        tabs = totalTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new TripFragment();
            case 1:
                return new SOSFragment();
            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return tabs;
    }
}
