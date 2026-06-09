package com.example.graphql.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "service_offering")
public class ServiceOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "type-cd", nullable = false)
    private String typeCd;

    protected ServiceOffering() {
    }

    public ServiceOffering(String description, String typeCd) {
        this.description = description;
        this.typeCd = typeCd;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTypeCd() {
        return typeCd;
    }

    public void setTypeCd(String typeCd) {
        this.typeCd = typeCd;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
