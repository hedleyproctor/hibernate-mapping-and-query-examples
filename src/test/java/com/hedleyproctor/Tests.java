package com.hedleyproctor;

import com.hedleyproctor.domain.*;
import com.hedleyproctor.domain.Order;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.*;

public class Tests {
    private SessionFactory sessionFactory;

    private static final DateFormat DAY_MONTH_YEAR_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

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

        Status s1 = new Status();
        s1.setName("Created");
        s1.setCreatedDate(DAY_MONTH_YEAR_FORMAT.parse("01-01-2014"));
        s1.setUser(user);

        Status s2 = new Status();
        s2.setName("Updated");
        s2.setCreatedDate(DAY_MONTH_YEAR_FORMAT.parse("02-01-2014"));
        s1.setUser(user);

        List<Status> statuses = new ArrayList<Status>();
        statuses.add(s1);
        statuses.add(s2);
        user.setStatuses(statuses);

        Query query = session.createQuery("from User where statuses[0].name = 'Created'");
        List results = query.list();
        tx.rollback();
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

        Query query = session.createQuery("from Customer c inner join c.userAccount u");
        List results = query.list();

        tx.rollback();
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
        tx.rollback();
        session.close();

        assertEquals(rowsUpdated,50);
    }

    // CURRENT_DATE(), CURRENT_TIME(),
    // CURRENT_TIMESTAMP()
//	    CAST(t as Type) Casts a given type t to a Hibernate Type
//	    INDEX(joinedCollection) Returns the index of joined collection element
//	    MINELEMENT(c), MAXELEMENT(c),
//	    MININDEX(c), MAXINDEX(c),

    // specifying join conditions in the hql - i.e. running a query across two tables

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
        tx.rollback();
        session.close();
    }


    @Test
    public void hqlWithCorrelatedSubquery() throws ParseException {

        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        Customer customer = new Customer();
        customer.setForename("Hedley");
        customer.setSurname("Proctor");
        session.save(customer);

        Order order1 = new Order();
        order1.setOrderTotal(29.99);
        order1.setCustomer(customer);
        order1.setTimestamp(DAY_MONTH_YEAR_FORMAT.parse("31-01-2014"));

        Order order2 = new Order();
        order2.setOrderTotal(8.99);
        order2.setCustomer(customer);
        order2.setTimestamp(DAY_MONTH_YEAR_FORMAT.parse("17-01-2014"));

        session.save(order1);
        session.save(order2);

        Customer customer2 = new Customer();
        customer2.setForename("Joe");
        customer2.setSurname("Bloggs");
        session.save(customer2);

        Order order3 = new Order();
        order3.setOrderTotal(35.99);
        order3.setCustomer(customer2);
        order3.setTimestamp(DAY_MONTH_YEAR_FORMAT.parse("16-06-2014"));

        Order order4 = new Order();
        order4.setOrderTotal(10.99);
        order4.setCustomer(customer2);
        order4.setTimestamp(DAY_MONTH_YEAR_FORMAT.parse("18-06-2014"));

        Order order5 = new Order();
        order5.setOrderTotal(14.99);
        order5.setCustomer(customer2);
        order5.setTimestamp(DAY_MONTH_YEAR_FORMAT.parse("20-06-2014"));

        session.save(order3);
        session.save(order4);
        session.save(order5);

        System.out.println("Showing data added to orders table:");
        Query query = session.createSQLQuery("select * from orders");
        List result = query.list();
        printResults(result);

        // Suppose you want to get a report of all customers that have two or more orders. If you were to write this in SQL using a join, you would
        // write:
        // select id from customer c join on (select customer_id,count(*) as num_orders from orders group by customer_id) co where c.id = co.customer_id and co.num_orders>2
        // To convert this to an SQL correlated subquery, the join is removed and the subquery gets moved to the where clause.
        // The conditions on the join get pushed down into the subquery. Since they refer to the columns from the outer query (on customer), this is what makes the
        // subquery a correlated subquery:
        System.out.println("Reporting all customers who have placed two or more orders, using SQL query: ");
        query = session.createSQLQuery("select id,forename,surname from customer c where (select count(*) from orders o where o.customer_id = c.id) > 2");
        result = query.list();
        printResults(result);

        // now we can write the same query in HQL
        System.out.println("Now running same query as HQL:");
        query = session.createQuery("select id,forename,surname from Customer c where (select count(*) from Order o where o.customer.id = c.id) > 2");
        result = query.list();
        printResults(result);

        // now suppose we want the most recent order for each customer
        // in the most basic variation, where you just want to get the timestamp of the most recent order for each customer id,
        // you get both columns out of your grouping query
        // select max(timestamp),customer_id from orders group by customer_id
        // In the more general case, where you want to get further details of either the customer or the order, you have to join
        // this query to the customer or orders table respectively.
        // For example, to get the order id and total as well:
        // select o.id,o.timestamp,o.order_total from orders o join (select max(timestamp) as max_ts,customer_id from orders group by customer_id) as x on o.customer_id = x.customer_id and o.timestamp = x.max_ts

        // to convert this to a correlated subquery, the subquery gets moved from the join into the where clause, and the additional
        // condition on the join becomes part of the subquery where clause.
        // Note that the group by can be removed, since the restriction on customer_id has become part of the correlated subquery where clause.
        System.out.println("Getting most recent order for each customer, using SQL:");
        query = session.createSQLQuery("select o1.id,o1.timestamp,o1.order_total from orders o1 where"
                   + " o1.timestamp in (select max(timestamp) from orders o2 where o1.customer_id = o2.customer_id)");
        result = query.list();
        printResults(result);

        // show this in HQL
        System.out.println("Getting most recent order for each customer in HQL:");
        query = session.createQuery("select o1.id,o1.timestamp,o1.orderTotal from Order o1 where " +
                                    " o1.timestamp in (select max(timestamp) from Order o2 where o1.customer.id = o2.customer.id)");
        result = query.list();
        printResults(result);

        // suppose we want to add some details of the customer as well
        // in sql we would join this to the customer table
        System.out.println("Getting most recent order for each customer, with customer details, using SQL:");
        query = session.createSQLQuery("select c.id as cust_id,c.forename,c.surname,o1.id,o1.timestamp,o1.order_total from orders o1 " +
                " join customer c on o1.customer_id = c.id " +
                " where o1.timestamp in (select max(timestamp) from orders o2 where o1.customer_id = o2.customer_id)");
        result = query.list();
        printResults(result);

        // in HQL, we simply navigate the association from order to customer to get the customer info
        System.out.println("Getting most recent order for each customer, with customer details, in HQL:");
        query = session.createQuery("select o1.customer.id,o1.customer.forename,o1.customer.surname,o1.id,o1.timestamp,o1.orderTotal from Order o1 where " +
                " o1.timestamp in (select max(timestamp) from Order o2 where o1.customer.id = o2.customer.id)");
        result = query.list();
        printResults(result);

        tx.rollback();
        session.close();
    }

    /**
     * Although subqueries are allowed in both HQL and Criteria, they can only be used in the select or where
     * clauses, not in the the from expression. However, for simple subqueries, you may be able to rewrite
     * the query as a correlated subquery.
     *
     * Each record is a user message, with a user id, message string, date and hnumber (message type).
     * We want to find the most recent message of each distinct message type for a given user, id 14.
     * The most natural way to do this is first doing a subquery that selects the max timestamp messages, grouped on id and hnumber,
     * then simply join to this subquery. However, this join would not be allowed in HQL, so you can rewrite it as a
     * correlated subquery.
     *
     * @throws ParseException
     */
    @Test
    public void hqlWithCorrelatedSubquery2() throws ParseException {
        System.out.println("Running HQL with correlated subquery");
         // create test data
        UserMessage userMessage1 = new UserMessage(14L,"xyz1",new Date(1394607463000L),"0429c3866c19981fc276855ff3cdaf100e0c9fdb");
        UserMessage userMessage2 = new UserMessage(14L,"xyz2",new Date(1394608378000L),"0429c3866c19981fc276855ff3cdaf100e0c9fdb");
        UserMessage userMessage3 = new UserMessage(14L,"xyz1",new Date(1394453678000L),"0429c3866c19981fc276855ff3cdaf100e0c9fdb");
        UserMessage userMessage4 = new UserMessage(14L,"xyz2",new Date(1394608520000L),"0429c3866c19981fc276855ff3cdaf100e0c9fdb"); // latest for 0429
        UserMessage userMessage5 = new UserMessage(14L,"xyz9",new Date(1394612791000L),"369d7cf7bd90fac78ef635b188e2a9952d77a8d1");
        UserMessage userMessage6 = new UserMessage(14L,"xyz7",new Date(1394608513793L),"0429c3866c19981fc276855ff3cdaf100e0c9fdb");
        UserMessage userMessage7 = new UserMessage(14L,"xyz6",new Date(1394608513793L),"0429c3866c19981fc276855ff3cdaf100e0c9fdb");
        UserMessage userMessage8 = new UserMessage(14L,"xyz3",new Date(1394622221000L),"369d7cf7bd90fac78ef635b188e2a9952d77a8d1"); // latest for 369
        UserMessage userMessage9 = new UserMessage(14L,"xyz4",new Date(1394608513793L),"369d7cf7bd90fac78ef635b188e2a9952d77a8d1");

        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.save(userMessage1);
        session.save(userMessage2);
        session.save(userMessage3);
        session.save(userMessage4);
        session.save(userMessage5);
        session.save(userMessage6);
        session.save(userMessage7);
        session.save(userMessage8);
        session.save(userMessage9);

        // example of what the "natural" sql query would look like, where you join to a nested subquery
//        SQLQuery sqlQuery = session.createSQLQuery("select um.id,um.hnumber,um.message from UserMessage as um " +
//                " inner join (select id,hnumber,max(timestamp) as max_tstamp from UserMessage where id = 14 group by id,hnumber) as x" +
//                " on um.id = x.id and um.hnumber = x. hnumber and um.timestamp = x.max_tstamp");
//        List list = sqlQuery.list();
//        prinResults(list);

        // for all messages
        // note how the structure of the query is different to the join version.
        // In the join version, you are joining on three columns - id, hnumber and timestamp.
        // In the correlated subquery, the outer "where" clause can only be evaluated against a single column - the timestamp,
        // but the other two columns are matched in the inner query, as part of its "where" clause.
        // Note that because those two columns become part of the correlated sub query where clause, they no longer need to be used to group the sub
        System.out.println("Printing results for query against entire table:");
        Query query = session.createQuery("select um.id,um.hnumber,um.timestamp,um.message from UserMessage as um "
                + "where um.timestamp in (select max(timestamp) from UserMessage um1 where um1.id=um.id and um1.hnumber=um.hnumber)");
        List list = query.list();
        printScalarResults(list,true);

        // for a single message
        System.out.println("Print results for query when you specify id of 14:");
        query = session.createQuery("select um.id,um.hnumber,um.timestamp,um.message from UserMessage as um "
                + "where um.timestamp in (select max(timestamp) from UserMessage um1 where um1.id=14 and um1.hnumber=um.hnumber)");
        list = query.list();
        printScalarResults(list,true);

        tx.rollback();
        session.close();
    }

    @Test
    public void criteriaWithCorrelatedSubquery() {
        System.out.println("Now running criteria query with correlated subquery...");

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

        // correlated subquery
        DetachedCriteria subquery =
                DetachedCriteria.forClass(Order.class, "o");
        subquery.add( Restrictions.eqProperty("o.customer.id", "c.id"))
                .setProjection( Property.forName("o.id").count() );
        Criteria criteria = session.createCriteria(Customer.class, "c")
                .add( Subqueries.lt(2L, subquery) );
        List resultList = criteria.list();

        // equivalent to the SQL
        // SQLQuery query = session.createSQLQuery("select id from Customer where 2 < (select count(*) from orders where customer_id = id)");
        // however, could be written using an explicit inner query, with a join to that e.g.
        // select id from customer c join on (select customer_id,count(*) as num_orders from orders group by customer_id) co where c.id = co.customer_id and co.num_orders>2

        tx.rollback();
        session.close();

    }

    /** Demonstrates that not all queries will cause the session to be flushed to the database. e.g. an SQL query won't cause a session flush,
     * you need a Hibernate query that involves the objects that are dirty
     *
     */
    @Test
    public void flushTest() {
        Order order1 = new Order();
        order1.setOrderTotal(29.99);

        Order order2 = new Order();
        order2.setOrderTotal(8.99);

        Order order3 = new Order();
        order3.setOrderTotal(15.99);

        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        // flush mode AUTO is the default, but specify it to be clear
        session.setFlushMode(FlushMode.AUTO);
        System.out.println("Flush mode: " + session.getFlushMode());

        session.save(order1);
        session.save(order2);
        session.save(order3);

        Customer customer = new Customer();
        customer.setForename("Hedley");
        customer.setSurname("Proctor");
        session.save(customer);

        order1.setCustomer(customer);
        order2.setCustomer(customer);
        order3.setCustomer(customer);

        // running an SQL query does not flush the session
        Query query = session.createSQLQuery("select id,customer_id from orders");
        List results1 = query.list();
        printResults(results1);
        assertTrue(session.isDirty(), "Session should still be dirty");
        Object order1CustomerId = ((Object[])results1.get(0))[1];
        assertNull(order1CustomerId,"Order 1 customer id will be null at this stage");

        // running a Hibernate query that involves the Order objects will cause the session to flush
        Query query2 = session.createQuery("from Order");
        query2.list();

        assertFalse(session.isDirty(), "Session should have been flushed by the HQL query");
        // rerun the original SQL query
        results1 = query.list();
        printResults(results1);

        // customer id should not be null
        order1CustomerId = ((Object[])results1.get(0))[1];
        assertNotNull(order1CustomerId,"Order 1 customer id should not be null");

        tx.rollback();
    }

    @Test
    public void criteriaWithEagerFetch() throws InterruptedException {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        Customer customer = new Customer();
        customer.setForename("Hedley");
        customer.setSurname("Proctor");
        session.save(customer);

        Order order1 = new Order();
        order1.setOrderTotal(29.99);
        order1.setCustomer(customer);

        Order order2 = new Order();
        order2.setOrderTotal(8.99);
        order2.setCustomer(customer);

        Order order3 = new Order();
        order3.setOrderTotal(15.99);
        order3.setCustomer(customer);

        session.save(order1);
        session.save(order2);
        session.save(order3);

        tx.commit();
        session.close();

        // okay, now try our query
        session = sessionFactory.openSession();
        tx = session.beginTransaction();

        Criteria criteria = session.createCriteria(Customer.class);
        criteria.add(Restrictions.eq("forename","Hedley"));
        criteria.add(Restrictions.eq("surname","Proctor"));
        criteria.setFetchMode("orders",FetchMode.JOIN);
        List results = criteria.list();

        // commit and close the session so that the Customer object in the result is detached
        tx.commit();
        session.close();
        // session is closed so customer object is now detached

        Customer customer1 = (Customer) results.get(0);
        assertEquals(customer1.getOrders().size(),3);
    }


    @Test
    public void summingValuesFromAJoinedEntityAndAddingToTheMainEntity() {
        System.out.println("Calculating area of sold apartments.");
        Apartment apartment1 = new Apartment();
        apartment1.setArea(100);
        apartment1.setSold(new BigDecimal(1));

        Apartment apartment2 = new Apartment();
        apartment2.setArea(200);

        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.save(apartment1);
        session.save(apartment2);

        AdditionalSpace additionalSpace1 = new AdditionalSpace();
        additionalSpace1.setApartment(apartment1);
        additionalSpace1.setArea(10);
        session.save(additionalSpace1);

        AdditionalSpace additionalSpace2 = new AdditionalSpace();
        additionalSpace2.setApartment(apartment1);
        additionalSpace2.setArea(10);
        session.save(additionalSpace2);

        AdditionalSpace additionalSpace3 = new AdditionalSpace();
        additionalSpace3.setApartment(apartment1);
        additionalSpace3.setArea(10);
        session.save(additionalSpace3);

        AdditionalSpace additionalSpace4 = new AdditionalSpace();
        additionalSpace4.setApartment(apartment2);
        additionalSpace4.setArea(20);
        session.save(additionalSpace4);

        AdditionalSpace additionalSpace5 = new AdditionalSpace();
        additionalSpace5.setApartment(apartment2);
        additionalSpace5.setArea(20);
        session.save(additionalSpace5);

        tx.commit();

        tx = session.beginTransaction();

        System.out.println("Running SQL version of query:");
        Query query = session.createSQLQuery("select max(a.area) + sum(ads.area) from AdditionalSpace as ads join Apartment as a on ads.apartment_id = a.id group by a.id");
        List results = query.list();
        printResults(results);

        System.out.println("Running HQL with sum");
            query = session.createQuery("select ads.apartment.id,max(a.area)+sum(ads.area) from Apartment a join a.additionalSpaces ads group by ads.apartment.id");
        results = query.list();
        printResults(results);

        System.out.println("Now running with criteria projection sum");
        Criteria criteria = session.createCriteria(Apartment.class,"a");
        criteria.createAlias("additionalSpaces", "ads");
        criteria.setProjection(Projections.projectionList()
            .add(Projections.property("area"))
            .add(Projections.groupProperty("a.id"))
            .add(Projections.sum("ads.area")));
        results = criteria.list();

        printResults(results);

        tx.commit();
    }

    void printResults(List list) {
        for (Object o : list) {
            if (o instanceof Object[]) {
                Object[] asArray = (Object[])o;
                for (Object item : asArray) {
                    System.out.println(item);
                }
            }
            else {
                System.out.println(o);
            }

        }
    }

    void printScalarResults(List list, boolean datesAsLong) {
        for (Object o : list) {
            Object[] asArray = (Object[])o;
            for (Object item : asArray) {
                if (item instanceof Date && datesAsLong) {
                    System.out.println(((Date)item).getTime());
                }
                else {
                    System.out.println(item);
                }
            }
        }
    }
}
