package org.store;

public class ProductPrice {
    private final Product product;
    private final double unitPrice;

    public ProductPrice(Product product, double unitPrice) {
        this.product = product;
        this.unitPrice = unitPrice;
    }

    public Product getProduct() {
        return product;
    }

    public double getUnitPrice() {
        return unitPrice;
    }
}
