package com.gplsadgroup.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "component")
public class Component {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "component_id")
    private Long componentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(name = "unit_of_measure", nullable = false)
    private Integer unitOfMeasure;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;

    public Component() {}

    public Long getComponentId() { return componentId; }
    public void setComponentId(Long componentId) { this.componentId = componentId; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(Integer unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
}