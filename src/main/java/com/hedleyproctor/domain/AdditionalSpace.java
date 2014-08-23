package com.hedleyproctor.domain;

import javax.persistence.*;

@Entity
public class AdditionalSpace {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Apartment apartment;

    private long area;

    public Apartment getApartment() {
        return apartment;
    }

    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }

    public long getArea() {
        return area;
    }

    public void setArea(long area) {
        this.area = area;
    }
}
