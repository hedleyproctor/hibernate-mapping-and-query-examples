package com.hedleyproctor.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class UserMessage {

    private Long seq;

    private Long id;
    private String message;
    private Date timestamp;
    private String hnumber;

    public UserMessage() {
        // for Hibernate
    }

    public UserMessage(Long id, String message, Date timestamp, String hnumber) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
        this.hnumber = hnumber;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getHnumber() {
        return hnumber;
    }

    public void setHnumber(String hnumber) {
        this.hnumber = hnumber;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("id:");
        stringBuilder.append(id);
        stringBuilder.append(" message:");
        stringBuilder.append(message);
        stringBuilder.append(" timestamp:");
        stringBuilder.append(timestamp);
        stringBuilder.append(" hnumber:");
        stringBuilder.append(hnumber);
        return stringBuilder.toString();
    }
}
