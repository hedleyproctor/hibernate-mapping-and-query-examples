package com.hedleyproctor;

import com.hedleyproctor.domain.*;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class Tests {
    private SessionFactory sessionFactory;

    @BeforeMethod
    public void setUp() throws SQLException {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    @AfterMethod
    public void afterMethod() {
        sessionFactory.close();
    }

    @Test
    public void accessCollectionElementByIndex() throws ParseException {

        Session session = sessionFactory.openSession();
        Transaction tx  = session.beginTransaction();
        User user = new User();
        user.setName("John Smith");
        session.save(user);

        DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Status s1 = new Status();
        s1.setName("Created");
        s1.setCreatedDate(sdf.parse("01-01-2014"));
        s1.setUser(user);

        Status s2 = new Status();
        s2.setName("Updated");
        s2.setCreatedDate(sdf.parse("02-01-2014"));
        s1.setUser(user);

        List<Status> statuses = new ArrayList<Status>();
        statuses.add(s1);
        statuses.add(s2);
        user.setStatuses(statuses);

        Query query = session.createQuery("from User where statuses[0].name = 'Created'");
        List results = query.list();
        tx.commit();
        session.close();

        assertEquals(results.size(),1);
    }


    /** Demonstrates how to use a join formula. In this case we are dealing with a poorly designed legacy
     * schema that has two columns either of which could be a foreign key to an entity of type UserAccount.
     * The join formula has to perform the join on the non-null foreign key.
     * Look at the getUserAccount() method of the Customer class for the join formula.
     *
     */
    @Test
    public void joinFormula() {
        Customer john = new Customer();
        john.setForename("John");
        john.setSurname("Smith");

        UserAccount account = new UserAccount();
        account.setName("John-account");

        Customer sarah = new Customer();
        sarah.setForename("Sarah");
        sarah.setSurname("Jones");

        UserAccount partnerAccount = new UserAccount();
        partnerAccount.setName("partner-account");

        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        session.save(john);
        session.save(account);

        Long accountId = account.getId();
        System.out.println("Saved account has got id: " + accountId);

        john.setAccountId(accountId);

        session.save(sarah);
        session.save(partnerAccount);
        sarah.setPartnerId(partnerAccount.getId());

        tx.commit();

        tx = session.beginTransaction();
        Query query = session.createQuery("from Customer c inner join c.userAccount u");
        List results = query.list();

        tx.commit();
        session.close();

        for (Object result : results) {
            System.out.println("Result:");
            Object[] resultInfo = (Object[]) result;
            for (Object o : resultInfo) {
                System.out.println(o);
            }
        }

    }


    @Test
    public void hqlUpdate() {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        // set up some test data
        for (int i = 0; i<100; i++) {
            Product p = new Product();
            p.setName("Product" + i);
            p.setDescription("Description" + i);
            session.save(p);
        }

        // now do a bulk update
        Query query = session.createQuery("update Product set name='Updated' where id > 50");
        int rowsUpdated = query.executeUpdate();
        tx.commit();
        session.close();

        assertEquals(rowsUpdated,50);
    }

    // CURRENT_DATE(), CURRENT_TIME(),
    // CURRENT_TIMESTAMP()
//	    CAST(t as Type) Casts a given type t to a Hibernate Type
//	    INDEX(joinedCollection) Returns the index of joined collection element
//	    MINELEMENT(c), MAXELEMENT(c),
//	    MININDEX(c), MAXINDEX(c),

    // specifying join conditions in the hql

    // elements(), indices()

    // lazy loading / fetch

    // use of with to perform a fetch
//    rom Item i
//    left join i.bids b
//    with b.amount > 100
//    where i.description like '%Foo%'

    // criteria



    @Test
    public void examples() {
        Order order1 = new Order();
        order1.setOrderTotal(29.99);

        Order order2 = new Order();
        order2.setOrderTotal(8.99);

        Order order3 = new Order();
        order3.setOrderTotal(15.99);

        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.save(order1);
        System.out.println("Order id: " + order1.getId());
        session.save(order2);
        session.save(order3);

        Customer customer = new Customer();
        customer.setForename("Hedley");
        customer.setSurname("Proctor");
        session.save(customer);
        order1.setCustomer(customer);
        order2.setCustomer(customer);
        order3.setCustomer(customer);

        // between
        Query query = session.createQuery("from Order where orderTotal between 10 and 30");
        List list = query.list();
        assertEquals(list.size(),2);

        // where collection is not empty
        query = session.createQuery("from Customer c where c.orders is not empty");
        list = query.list();
        assertEquals(list.size(),1);

        // member of
        query = session.createQuery("from Order o, Customer c where o.orderTotal > 10 and o member of c.orders");
        list = query.list();
        assertEquals(list.size(),2);
        // each result should be an Order and Customer
        for (Object o : list) {
            Object[] results = (Object[])o;
            Order order = (Order) results[0];
            System.out.println(order);
            Customer customerFound = (Customer)results[1];
            System.out.println(customerFound);
        }

        // size of collection
        query = session.createQuery("from Customer c where size(c.orders) > 2");
        list = query.list();
        assertEquals(list.size(),1);

        // substring. note start location starts from 1, not 0
        query = session.createQuery("select substring(c.forename,1,3) from Customer c");
        list = query.list();
        String stringResult = (String) list.get(0);
        assertEquals(stringResult,"Hed");

        // length
        query = session.createQuery("select c.surname from Customer c where length(c.surname) > 5");
        list = query.list();
        stringResult = (String) list.get(0);
        assertEquals("Proctor",stringResult);

        // locate
        query = session.createQuery("select locate('e',c.forename,1) from Customer c");
        Integer intResult = (Integer) query.list().get(0);
        assertEquals(intResult.intValue(),2);

        // if the database was persistent, you should commit the transaction and close the session before
        // making the asserts, since if the assert fails it won't close the db.
        tx.commit();
        session.close();
    }
}
