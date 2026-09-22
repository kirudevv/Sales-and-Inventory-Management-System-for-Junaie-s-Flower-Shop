package com.gplsadgroup.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_code", nullable = false)
    private String productCode;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "selling_price")
    private BigDecimal sellingPrice;

    @Column(name = "status")
    private String status;

    @Column(name = "quantity_in_stock")
    private Integer quantityInStock = 50;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    public Product() {}

    public Product(String productCode, String productName, BigDecimal sellingPrice, String status, Integer quantityInStock, Category category) {
        this.productCode = productCode;
        this.productName = productName;
        this.sellingPrice = sellingPrice;
        this.status = status;
        this.quantityInStock = quantityInStock;
        this.category = category;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(Integer quantityInStock) {
        this.quantityInStock = quantityInStock;
        if (this.quantityInStock != null && this.quantityInStock <= 0) {
            this.status = "OUT_OF_STOCK";
        } else if (this.quantityInStock != null && this.quantityInStock > 0) {
            this.status = "AVAILABLE";
        }
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public boolean isLowStock(int threshold) {
        return this.quantityInStock != null && this.quantityInStock <= threshold;
    }
}