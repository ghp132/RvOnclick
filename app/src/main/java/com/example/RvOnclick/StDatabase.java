package com.example.RvOnclick;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Product.class,Customer.class,Order.class,
        OrderProduct.class,Invoice.class,Payment.class,
        PriceList.class,Account.class,Price.class,TblSettings.class,Company.class,
        UserConfig.class, User.class, Territory.class, VolleyErrorRecord.class,
        Brand.class, Warehouse.class, ProductsList.class, ProductsListItem.class, StockEntry.class}, version = 27)
public abstract class StDatabase extends RoomDatabase {

    public abstract StDao stDao();



}
