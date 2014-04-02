package com.hedleyproctor.domain;

import java.util.List;

import javax.persistence.*;

@Entity
public class User {
	private long id;
	private String name;
	private List<Status> statuses;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @OrderColumn(name = "status_index")
	public List<Status> getStatuses() {
		return statuses;
	}
	public void setStatuses(List<Status> statuses) {
		this.statuses = statuses;
	}
	
	
}
