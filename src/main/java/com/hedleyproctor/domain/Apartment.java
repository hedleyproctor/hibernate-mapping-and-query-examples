package com.hedleyproctor.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Set;

@Entity
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int area;

    private BigDecimal sold;

    @OneToMany(mappedBy = "apartment")
    private Set<AdditionalSpace> additionalSpaces;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getArea() {
        return area;
    }

    public BigDecimal getSold() {
        return sold;
    }

    public void setSold(BigDecimal sold) {
        this.sold = sold;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public Set<AdditionalSpace> getAdditionalSpaces() {
        return additionalSpaces;
    }

    public void setAdditionalSpaces(Set<AdditionalSpace> additionalSpaces) {
        this.additionalSpaces = additionalSpaces;
    }
}
