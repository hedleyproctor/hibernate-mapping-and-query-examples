package com.hedleyproctor;

import java.util.Date;
import java.util.List;

public class TestHelper {

    public static void printResults(List list) {
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

    public static void printScalarResults(List list, boolean datesAsLong) {
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
