package com.bytatech.ayoos.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import org.springframework.data.elasticsearch.annotations.Document;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A MedicalCase.
 */
@Entity
@Table(name = "medical_case")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "medicalcase")
public class MedicalCase implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consulted_date")
    private LocalDate consultedDate;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Column(name = "diagnosed")
    private String diagnosed;

    @Column(name = "note")
    private String note;

    @ManyToOne
    @JsonIgnoreProperties("medicalCases")
    private Patient patient;

    @OneToMany(mappedBy = "medicalCase")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<DMSRecord> dmsRecords = new HashSet<>();
    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getConsultedDate() {
        return consultedDate;
    }

    public MedicalCase consultedDate(LocalDate consultedDate) {
        this.consultedDate = consultedDate;
        return this;
    }

    public void setConsultedDate(LocalDate consultedDate) {
        this.consultedDate = consultedDate;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public MedicalCase createdDate(LocalDate createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public String getDiagnosed() {
        return diagnosed;
    }

    public MedicalCase diagnosed(String diagnosed) {
        this.diagnosed = diagnosed;
        return this;
    }

    public void setDiagnosed(String diagnosed) {
        this.diagnosed = diagnosed;
    }

    public String getNote() {
        return note;
    }

    public MedicalCase note(String note) {
        this.note = note;
        return this;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Patient getPatient() {
        return patient;
    }

    public MedicalCase patient(Patient patient) {
        this.patient = patient;
        return this;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Set<DMSRecord> getDmsRecords() {
        return dmsRecords;
    }

    public MedicalCase dmsRecords(Set<DMSRecord> dMSRecords) {
        this.dmsRecords = dMSRecords;
        return this;
    }

    public MedicalCase addDmsRecords(DMSRecord dMSRecord) {
        this.dmsRecords.add(dMSRecord);
        dMSRecord.setMedicalCase(this);
        return this;
    }

    public MedicalCase removeDmsRecords(DMSRecord dMSRecord) {
        this.dmsRecords.remove(dMSRecord);
        dMSRecord.setMedicalCase(null);
        return this;
    }

    public void setDmsRecords(Set<DMSRecord> dMSRecords) {
        this.dmsRecords = dMSRecords;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MedicalCase medicalCase = (MedicalCase) o;
        if (medicalCase.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), medicalCase.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "MedicalCase{" +
            "id=" + getId() +
            ", consultedDate='" + getConsultedDate() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", diagnosed='" + getDiagnosed() + "'" +
            ", note='" + getNote() + "'" +
            "}";
    }
}
