package com.example.RvOnclick;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
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

    @Insert
    public Long createInvoice (Invoice invoice);

    @Insert
    public Long makePayment (Payment payment);

    @Insert
    public void addPriceList (PriceList priceList);

    @Insert
    public void addAccount(Account account);

    @Insert
    public  void addPrice(Price price);

    @Insert
    public void addConfig(TblSettings config);

    @Insert
    void addCompnany(Company company);

    @Query("select * from Company")
    List<Company> getAllCompanies();

    @Query("select * from Company where companyName = :companyName")
    Company getCompanyByCompanyName(String companyName);

    @Query("select * from TblSettings")
    public List<TblSettings> getAllSettings();

    @Query("select * from TblSettings where defn = :defn")
    public TblSettings getConfigByName(String defn);

    @Query("select * from `Order`")
    public List<Order> getAllOrder();

    @Query("select * from PriceList where selling = 1")
    public List<PriceList> getSellingPriceLists();

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

    @Query("select * from Customer where customer_id = :custCode")
    public Customer getCustomerbyCustomerCode(String custCode);

    @Query("select * from Invoice where customer = :custCode")
    public List<Invoice> getInvoicesByCustomerCode(String custCode);

    @Query("update 'Order' set appOrderId = :appOrderId where orderId = :id")
    public void updateAppOrderId(String appOrderId, Long id);


    @Query("select * from `Order` where orderStatus=-1")
    public List<Order> getUnsyncedOrders();

    @Query("delete from Company")
    void deleteAllCompanies();

    @Query("delete from PriceList where 1")
    public void deleteAllPriceLists();

    @Query("delete from 'Order' where 1")
    public void deleteAllOrders();

    @Query("delete from `Order` where orderId = :id")
    void deleteOrderByOrderId(Long id);

    @Query("delete from OrderProduct where orderId = :id")
    void deleteOrderProductByOrderId(Long id);

    @Query("delete from OrderProduct where 1")
    public void deleteAllOrderProducts();

    @Update
    void updateOrderProduct(OrderProduct... orderProduct);

    @Query("delete from Product where 1")
    public void deleteAllProduct();

    @Query("delete from Price where 1")
    public void deleteAllPrice();

    @Query("delete from Customer where 1")
    public void deleteAllCustomer();

    @Query("delete from Invoice where 1")
    public void deleteAllInvoices();

    @Delete
    void deleteOrderProduct(OrderProduct...orderProduct);


    @Query("update 'Order' set orderNumber = :orderNumber where orderId = :id")
    public void updateOrderNumber(String orderNumber, Long id);

    @Query("update 'Order' set orderStatus = :status where orderId = :id")
    public void updateOrderStatus(int status, Long id);

    @Query("update Payment set appPaymentId = :appPaymentId where paymentId = :id")
    public void updateAppPaymentId(String appPaymentId, Long id);

    @Query("select * from Payment where paymentStatus = -1")
    List<Payment> getUnsyncedPayments();

    @Query("update Payment set paymentNumber = :paymentNumber where paymentId = :id")
    void updatePaymentNumber(String paymentNumber, Long id);

    @Query("update Payment set paymentStatus = :status where paymentId = :id")
    void  updatePaymentStatus(int status, Long id);

    @Query("select * from Payment where paymentStatus=:paymentStatus")
    List<Payment> getPaymentByPaymentStatus(int paymentStatus);

    @Query("select * from Company where companyName=:companyName")
    Company getCompanyByName(String companyName);

    @Query("select * from Price where 1")
    List<Price> getAllPrices();

    @Query("select * from Payment where 1")
    List<Payment> getAllPayments();

    @Query("select * from PriceList where 1")
    List<PriceList> getAllPriceLists();

    @Query("select * from Price where priceList = :priceList")
    List<Price> getPricesByPriceList(String priceList);

    @Query("select * from Payment where paymentStatus = -1 and invoiceNo = :invoiceNo")
    Payment getUnsyncedPaymentByInvoiceNo(String invoiceNo);

    @Query("select * from `Order` where orderStatus = :orderStatus")
    List<Order> getOrderByOrderStatus(int orderStatus);

    @Query("select * from `Order` where orderId = :orderId")
    Order getOrderByOrderId(Long orderId);

    @Query("select sum(rate*qty) from OrderProduct where orderId=:orderId")
    double getOrderTotalValueByOrderId(Long orderId);

    @Query("select * from Product where productDisabled = :disabled")
    List<Product> getEnabledProducts(boolean disabled);

    @Query("select * from Customer where customer_disabled = :disabled")
    List<Customer> getEnabledCustomers(boolean disabled);

    @Update
    void updatePayment(Payment... payment);

    @Update
    void updateOrder(Order... order);

    @Update
    void updateProduct(Product... product);

    @Update
    void updateConfig(TblSettings... config);

    @Query("select count(*) from Company")
    int countCompany();

    @Query("select count(*) from PriceList")
    int countPriceList();

    @Query("select count(*) from Price")
    int countPrice();

    @Query("select count(*) from Product")
    int countProduct();

    @Query("select count(*) from Customer")
    int countCustomer();

    @Query("select count(*) from Invoice")
    int countInvoice();

    @Query("select count(*) from OrderProduct where orderId = :id")
    int countOrderProduct(Long id);

    @Query("select * from Account where parentAccount = :parentAccount")
    List<Account> getAccountByParentAccount(String parentAccount);

    @Query("select * from Account where accountName = :accountName")
    Account getAccountByAccountName(String accountName);




}
