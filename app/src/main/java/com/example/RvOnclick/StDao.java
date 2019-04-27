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
    public long addProductToOrder(OrderProduct orderProduct);

    @Query("select * from OrderProduct where orderProductId=:id")
    OrderProduct getOrderProdutByOrderProductId(long id);

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

    @Query("select * from Company where isDefault=1")
    Company getDefaultCompany();

    @Update
    void updateCompany(Company...companies);

    @Query("select * from Company")
    List<Company> getAllCompanies();

    @Query("select * from Company where companyName = :companyName")
    Company getCompanyByCompanyName(String companyName);

    @Query("select abbr from Company where companyName = :companyName")
    String getAbbrByCompanyName(String companyName);

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

    @Query("select orderStatus from `Order` where orderId = :id")
    int getOrderStatusByOrderId(long id);

    @Query("select * from Product where productDisabled = 0")
    public List<Product> getProduct();

    @Query("select * from Customer where customer_disabled = 0")
    public List<Customer> getCustomer();

    @Query("select mobileNo from Customer where customer_id=:custCode")
    String getMobileNoByCustomer(String custCode);

    @Query("update OrderProduct set qty = :qty, rate = :rate where orderProductId = :id")
    public int updateOrderProductById(double qty, double rate, int id);

    @Query("select * from Customer where customer_id = :custCode")
    Customer getCustomerbyCustomerCode(String custCode);

    @Query("select * from Invoice where customer = :custCode")
    public List<Invoice> getInvoicesByCustomerCode(String custCode);

    @Query("select * from Invoice where customer = :custCode AND outstanding!=0")
    List<Invoice> getOutstandingInvoicesByCustomerCode(String custCode);

    @Query("select * from Invoice where invoiceNumber = :invoiceNo")
    Invoice getInvoiceByInvoiceNo(String invoiceNo);

    @Query("update 'Order' set appOrderId = :appOrderId where orderId = :id")
    public void updateAppOrderId(String appOrderId, Long id);


    @Query("select * from `Order` where orderStatus=-1")
    public List<Order> getUnsyncedOrders();

    @Query("delete from Company")
    void deleteAllCompanies();

    @Query("delete from Account")
    void deleteAllAccounts();

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

    @Query("delete from Payment where paymentId = :id")
    void deletePaymentByPaymentId(Long id);

    @Delete
    void deleteOrderProduct(OrderProduct...orderProduct);

    @Delete
    void deletePayment(Payment...payment);


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

    @Query("select * from Payment where paymentId = :id")
    Payment getPaymentByPaymentId(Long id);

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

    @Query("update Product set productRate = 0 where 1")
    void resetProductRate();

    @Query("update Product set currentOrderQty = 0 AND currentOrderFreeQty=0")
    void resetCurrentOrderQty();

    @Query("select count(*) from PriceList")
    int countPriceList();

    @Query("select count(*) from Price")
    int countPrice();

    @Query("select count(*) from Product")
    int countProduct();

    @Query("select count(*) from Account")
    int countAccounts();

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

    @Query("select * from Account where name = :name")
    Account getAccountByName(String name);

    @Query("select * from Product where 1")
    List<Product> getAllProducts();

    @Insert
    void addUserConfig(UserConfig userConfig);

    @Update
    void updateUserConfig(UserConfig...userConfigs);

    @Query("select * from UserConfig")
    List<UserConfig> getAllUserConfig();

    @Query("select count(*) from UserConfig")
    int countUserConfig();

    @Query("Delete from UserConfig")
    void deleteAllUserConfig();

    @Query("select sendSms from UserConfig where userId=:userId")
    int getSendSmsConfig(String userId);

    @Insert
    long addUser(User user);

    @Query("select count(*) from User")
    int countUsers();

    @Query("delete from User")
    void deleteAllUsers();

    @Query("select * from User where emailId = :email")
    User getUserByEmailId(String email);


}
