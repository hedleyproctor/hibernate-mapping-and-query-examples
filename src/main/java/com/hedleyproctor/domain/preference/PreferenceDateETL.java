package com.hedleyproctor.domain.preference;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class PreferenceDateETL {

    @EmbeddedId
    private Id id = new Id();
    @ManyToOne
    @JoinColumn(name = "preferenceId", insertable = false, updatable = false)
    private Preference preference;
    @ManyToOne
    @JoinColumn(name="dateETLId", insertable = false, updatable = false)
    private DateETL dateETL;

    @OneToOne
    private Corporation corporation;

    private boolean deleted;

    @Embeddable
    public static class Id implements Serializable {

        private Long preferenceId;

        private Long dateETLId;

        public Id() {}

        public Id(Long preferenceId, Long dateETLId) {
            this.preferenceId = preferenceId;
            this.dateETLId = dateETLId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof Id) {
                Id that = (Id)obj;
                return this.preferenceId.equals(that.preferenceId) && this.dateETLId.equals(that.dateETLId);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return preferenceId.hashCode() + dateETLId.hashCode();
        }

        @Override
        public String toString() {
            return "Preference id: " + preferenceId + " dateETLId: " + dateETLId;
        }
    }

    public Id getId() {
        return id;
    }

    public PreferenceDateETL(Preference preference, DateETL dateETL) {
        this.preference = preference;
        this.dateETL = dateETL;
        this.getId().preferenceId = preference.getId();
        this.getId().dateETLId = dateETL.getId();
        preference.getPreferenceDateETLs().add(this);
        dateETL.getPreferenceDateETLs().add(this);
    }

    public PreferenceDateETL() {

    }

    @Override
    public String toString() {
        return "PreferenceDateETL. Id: " + id.toString();
    }

    public Corporation getCorporation() {
        return corporation;
    }

    public void setCorporation(Corporation corporation) {
        this.corporation = corporation;
    }

    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
