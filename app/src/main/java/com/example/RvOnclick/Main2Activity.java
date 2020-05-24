package com.example.RvOnclick;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.RvOnclick.AllFragements.CustomerOutstandingList;
import com.example.RvOnclick.AllFragements.ErrorDisplayFragment;
import com.example.RvOnclick.AllFragements.ProductListFragment;
import com.example.RvOnclick.AllFragements.SettingsFragment;
import com.example.RvOnclick.AllFragements.StockTransferWarehouseFragment;
import com.example.RvOnclick.AllFragements.VolleyErrorList;
import com.example.RvOnclick.AllFragements.WarehouseSelectionFragment;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        CustomerOutstandingList.OnFragmentInteractionListener {
    private static final String TAG_CUST_OUTSTANDING_FRAGEMENT = "TAG_CUST_OUTSTANDING_FRAGEMENT";
    private static final String TAG_ERROR_DISPLAY_FRAGMENT = "TAG_ERROR_DISPLAY_FRAGMENT";
    private static final String TAG_VOLLEY_ERROR_LIST_FRAGMENT = "TAG_VOLLEY_ERROR_LIST_FRAGMENT";
    private static final String TAG_SETTINGS_FRAGEMNT = "TAG_SETTINGS_FRAGMENT";
    private static final String TAG_WAREHOUSE_SELECTION_FRAGMENT = "TAG_WAREHOUSE_SELECTION_FRAGMENT";
    private static final String TAG_PRODUCT_LIST_FRAGMENT = "TAG_PRODUCT_LIST_FRAGMENT";
    private static final String TAG_STOCK_TRANSFER_WAREHOUSE_FRAGMENT = "TAG_STOCK_TRANSFER_WAREHOUSE_FRAGMENT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Under development!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        Intent intent = getIntent();
        String fragmentName = intent.getStringExtra("fragment");

        getSupportActionBar().setTitle(R.string.app_name);


        Fragment customerOutstandingListFragment = new CustomerOutstandingList();
        Fragment errorDisplayFragment = new ErrorDisplayFragment();
        Fragment settingsFragemnt = new SettingsFragment();
        Fragment warehouseSelectionFragement = new WarehouseSelectionFragment();
        Fragment productListFragment = new ProductListFragment();
        Fragment stockTransferWarehouseFragment = new StockTransferWarehouseFragment();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();

        if (fragmentName.equals("CustomerOutstandingListFragment")) {
            fragmentTransaction.replace(R.id.fragement_container, customerOutstandingListFragment,
                    TAG_CUST_OUTSTANDING_FRAGEMENT);
            fragmentTransaction.commit();
        }

        if (fragmentName.equals(Utils.ERROR_DISPLAY_FRAGMENT)) {
            fragmentTransaction.replace(R.id.fragement_container, errorDisplayFragment,
                    TAG_ERROR_DISPLAY_FRAGMENT);
            fragmentTransaction.commit();
        }

        if (fragmentName.equals(Utils.SETTINGS_FRAGMENT)) {
            fragmentTransaction.replace(R.id.fragement_container, settingsFragemnt,
                    TAG_SETTINGS_FRAGEMNT);
            fragmentTransaction.commit();
        }

        switch (fragmentName) {
            case Utils.WAREHOUSE_SELECTION_FRAGMENT:
                fragmentTransaction.replace(R.id.fragement_container, warehouseSelectionFragement,
                        TAG_WAREHOUSE_SELECTION_FRAGMENT);
                fragmentTransaction.commit();
                break;

            case Utils.PRODUCT_LIST_FRAGMENT:
                fragmentTransaction.replace(R.id.fragement_container, productListFragment,
                        TAG_PRODUCT_LIST_FRAGMENT);
                fragmentTransaction.commit();
                break;

            case Utils.STOCK_TRANSFER_WAREHOUSE_FRAGMENT:
                getIntent().putExtra("transactionType", Utils.STOCK_MATERIAL_TRANSFER);
                getIntent().putExtra("docType", Utils.STOCK_ENTRY);
                //FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.fragement_container, stockTransferWarehouseFragment, TAG_STOCK_TRANSFER_WAREHOUSE_FRAGMENT);
                fragmentTransaction.commit();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_stockTransfer) {
            getIntent().putExtra("transactionType", Utils.STOCK_MATERIAL_TRANSFER);
            getIntent().putExtra("docType", Utils.STOCK_ENTRY);
            StockTransferWarehouseFragment stockTransferWarehouseFragment = new StockTransferWarehouseFragment();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.fragement_container, stockTransferWarehouseFragment, TAG_STOCK_TRANSFER_WAREHOUSE_FRAGMENT);
            fragmentTransaction.commit();

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_volleyError) {
            VolleyErrorList volleyErrorList = new VolleyErrorList();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.fragement_container, volleyErrorList,
                    TAG_CUST_OUTSTANDING_FRAGEMENT);
            fragmentTransaction.commit();

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
