package com.hedleyproctor;

import com.hedleyproctor.domain.preference.*;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PreferenceTest {

    private static final DateFormat DAY_MONTH_YEAR_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

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
    public void hqlWithDistinctAndOrderBy() throws ParseException {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        Corporation corporation = new Corporation();
        corporation.setName("Test corporation");
        session.save(corporation);

        Employee employee = new Employee();
        employee.setForename("Hedley");
        employee.setSurname("Proctor");
        employee.setDeleted(false);
        session.save(employee);

        Preference preference = new Preference();
        preference.setEmployee(employee);
        preference.setDeleted(false);
        preference.setApprovalStatus("APPROVED");
        preference.setDateCreated(new Timestamp(DAY_MONTH_YEAR_FORMAT.parse("10-01-2014").getTime()));
        session.save(preference);
        Preference preference1 = new Preference();
        preference1.setEmployee(employee);
        preference1.setDeleted(false);
        preference1.setApprovalStatus("APPROVED");
        preference.setDateCreated(new Timestamp(DAY_MONTH_YEAR_FORMAT.parse("07-01-2014").getTime()));
        session.save(preference1);
        Preference preference2 = new Preference();
        preference2.setEmployee(employee);
        preference2.setDeleted(false);
        preference2.setApprovalStatus("APPROVED");
        preference.setDateCreated(new Timestamp(DAY_MONTH_YEAR_FORMAT.parse("05-01-2014").getTime()));
        session.save(preference2);

        Date middleOfYearDate = DAY_MONTH_YEAR_FORMAT.parse("01-08-2014");
        Timestamp timestamp = new Timestamp(middleOfYearDate.getTime());
        DateETL dateETL = new DateETL();
        dateETL.setLocalDate(timestamp);
        session.save(dateETL);
        DateETL dateETL1 = new DateETL();
        dateETL1.setLocalDate(timestamp);
        session.save(dateETL1);
        DateETL dateETL2 = new DateETL();
        dateETL2.setLocalDate(timestamp);
        session.save(dateETL2);

        // first pref links to first two dates
        PreferenceDateETL preferenceDateETL = new PreferenceDateETL(preference,dateETL);
        preferenceDateETL.setCorporation(corporation);
        preferenceDateETL.setDeleted(false);
        session.save(preferenceDateETL);
        PreferenceDateETL preferenceDateETL1 = new PreferenceDateETL(preference,dateETL1);
        preferenceDateETL1.setCorporation(corporation);
        preferenceDateETL1.setDeleted(false);
        session.save(preferenceDateETL1);
        PreferenceDateETL preferenceDateETL2 = new PreferenceDateETL(preference1,dateETL1);
        preferenceDateETL2.setCorporation(corporation);
        preferenceDateETL2.setDeleted(false);
        session.save(preferenceDateETL2);
        PreferenceDateETL preferenceDateETL3 = new PreferenceDateETL(preference2,dateETL2);
        preferenceDateETL3.setCorporation(corporation);
        preferenceDateETL3.setDeleted(false);
        session.save(preferenceDateETL3);

        tx.commit();
        tx = session.beginTransaction();

        System.out.println("running SQL query to show contents of PreferenceDateETL table");
        Query query = session.createSQLQuery("select * from PreferenceDateETL");
        List results = query.list();
        TestHelper.printResults(results);

        System.out.println("running original HQL query without distinct to show that 4 records returned");
        query = session.createQuery("select pd.preference from PreferenceDateETL pd"
                                    + " where pd.corporation.id=:corporationId"
                                    + " and pd.preference.employee.deleted=false"
                                    + " and pd.deleted=false and pd.preference.deleted=false"
                                    + " and pd.dateETL.localDate>=:startDM"
                                    + " and pd.dateETL.localDate<=:endDM"
                                    + " and pd.preference.approvalStatus != :approvalStatus"
                                    + " order by pd.preference.dateCreated"
        );
        query.setLong("corporationId",corporation.getId());
        query.setParameter("startDM", new Timestamp(DAY_MONTH_YEAR_FORMAT.parse("01-01-2014").getTime()));
        query.setParameter("endDM", new Timestamp(DAY_MONTH_YEAR_FORMAT.parse("12-31-2014").getTime()));
        query.setString("approvalStatus","DENIED");
        results = query.list();
        System.out.println("Number of results found from original query without distinct: " + results.size());
        TestHelper.printResults(results);

        System.out.println("running new HQL query that uses sub-query to avoid the distinct and order by");
        query = session.createQuery("from Preference p where p in "
                                        + "(select pd.preference from PreferenceDateETL pd"
                                        + " where pd.corporation.id=:corporationId"
                                        + " and pd.deleted=false"
                                        + " and pd.dateETL.localDate>=:startDM"
                                        + " and pd.dateETL.localDate<=:endDM)"
                                        + " and p.employee.deleted=false"
                                        + " and p.deleted=false"
                                        + " and p.approvalStatus != :approvalStatus"
                                        + " order by p.dateCreated");
        query.setLong("corporationId",corporation.getId());
        query.setParameter("startDM", new Timestamp(DAY_MONTH_YEAR_FORMAT.parse("01-01-2014").getTime()));
        query.setParameter("endDM", new Timestamp(DAY_MONTH_YEAR_FORMAT.parse("12-31-2014").getTime()));
        query.setString("approvalStatus","DENIED");
        results = query.list();
        System.out.println("Number of results from new HQL: " + results.size());
        TestHelper.printResults(results);

        tx.commit();
        session.close();
    }
}
