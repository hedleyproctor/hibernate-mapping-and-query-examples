package com.hedleyproctor;

import com.hedleyproctor.domain.*;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.testng.annotations.BeforeClass;
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

    @BeforeClass
    public void setUp() throws SQLException {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    // is null / is not null
    // from Item i where i.bids is not empty
    // from Item i, Category c where i.id = '123' and i member of c.items
    // from User user  where concat(user.firstname, user.lastname) like 'G% K%'
    // from Item i where size(i.bids) > 3
    //SUBSTRING(s, offset, length) String values (offset starts at 1); returns a string  value
//TRIM( [[BOTH|LEADING|TRAILING] Trims spaces on BOTH sides of s if no char or
//char [FROM]] s) other specification is given; returns a string value
//LENGTH(s) String value; returns a numeric value
//LOCATE(search, s, offset)
    // CURRENT_DATE(), CURRENT_TIME(),
    // CURRENT_TIMESTAMP()
//	    CAST(t as Type) Casts a given type t to a Hibernate Type
//	    INDEX(joinedCollection) Returns the index of joined collection element
//	    MINELEMENT(c), MAXELEMENT(c),
//	    MININDEX(c), MAXINDEX(c),
    // using sql functions in the select clause
    // specifying join conditions in the hql
    // elements(), indices()
    // refer to elements of a collection by index, only in the where clause

    @Test
    public void between() {
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
        Query query = session.createQuery("from Order where orderTotal between 10 and 30");
        List list = query.list();
        tx.commit();
        session.close();

        assertEquals(2,list.size());
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
        // set up some test data

    }
}
