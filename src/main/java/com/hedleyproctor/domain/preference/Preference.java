package com.hedleyproctor.domain.preference;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Preference {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(mappedBy = "preference", cascade = CascadeType.ALL)
    private Set<PreferenceDateETL> preferenceDateETLs = new HashSet<PreferenceDateETL>();

    @OneToOne
    private Employee employee;

    private boolean deleted;

    private String approvalStatus;

    private Timestamp dateCreated;

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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }
}
