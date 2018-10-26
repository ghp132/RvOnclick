package com.example.RvOnclick;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ProductPagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    Bundle bundle;
    public ProductPagerAdapter(FragmentManager fm, int numOfTabs, Bundle bundle){
        super(fm);
        this.mNumOfTabs = numOfTabs;
        this.bundle = bundle;

    }

    @Override
    public Fragment getItem(int position){
        switch (position){
            case 0:
                Fragment fragment = new CustomerTransactionProductFragment();
                fragment.setArguments(bundle);
                return fragment;

            case 1:
                Fragment fragment1 = new CustomerTransactionOrderFragment();

                fragment1.setArguments(bundle);
                return fragment1;
            case 2:
                return new CustomerTransactionHistoryFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount(){return mNumOfTabs;}

}
