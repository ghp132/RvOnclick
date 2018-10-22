package com.example.RvOnclick;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TableLayout;

public class CustomerTransactionActivity extends AppCompatActivity {

    public int orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_transaction);
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.customerTransactionActivityToolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        final String custCode = intent.getStringExtra("custCode");


        Bundle bundle = new Bundle();
        bundle.putString("custCode", custCode);
        //bundle.putString("orderId", String.valueOf(orderId));
        CustomerTransactionProductFragment productFragment = new CustomerTransactionProductFragment();
        productFragment.setArguments(bundle);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout_product);
        tabLayout.addTab(tabLayout.newTab().setText("Items"));
        tabLayout.addTab(tabLayout.newTab().setText("Orders"));
        tabLayout.addTab(tabLayout.newTab().setText("History"));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = findViewById(R.id.productPager);
        final ProductPagerAdapter adapter = new ProductPagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount(),bundle);
        viewPager.setAdapter(adapter);




        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }



}
