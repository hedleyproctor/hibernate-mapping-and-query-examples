package com.hedleyproctor.domain.preference;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
public class DateETL {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(mappedBy = "dateETL", cascade = CascadeType.ALL)
    private Set<PreferenceDateETL> preferenceDateETLs = new HashSet<PreferenceDateETL>();

    private Timestamp localDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<PreferenceDateETL> getPreferenceDateETLs() {
        return preferenceDateETLs;
    }

    public void setPreferenceDateETLs(Set<PreferenceDateETL> preferenceDateETLs) {
        this.preferenceDateETLs = preferenceDateETLs;
    }

    public Timestamp getLocalDate() {
        return localDate;
    }

    public void setLocalDate(Timestamp localDate) {
        this.localDate = localDate;
    }
}
