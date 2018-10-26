package com.example.RvOnclick;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface StDao {
    @Insert
    public void addProduct(Product product);

    @Insert
    public  void addCustomer (Customer customer);

    @Insert
    public long createOrder (Order order);

    @Insert
    public void addProductToOrder(OrderProduct orderProduct);

    @Query("select * from `Order`")
    public List<Order> getAllOrder();

    @Query("select * from Product where productCode= :productCode")
    public Product getProductByProductCode(String productCode);

    @Query("select * from OrderProduct where orderId = :id")
    public List<OrderProduct> getOrderProductsById(long id);

    @Query("select * from Product where productDisabled = 0")
    public List<Product> getProduct();

    @Query("select * from Customer where customer_disabled = 0")
    public List<Customer> getCustomer();

    @Query("update OrderProduct set qty = :qty, rate = :rate where orderProductId = :id")
    public int updateOrderProductById(double qty, double rate, int id);


}
