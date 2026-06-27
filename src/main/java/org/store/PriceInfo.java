package org.store;

import java.util.List;

public class PriceInfo {
    private final double price;
    private final List<String> unusedCoupons;

    public PriceInfo(double price, List<String> unusedCoupons) {
        this.price = price;
        this.unusedCoupons = unusedCoupons;
    }

    public double getPrice() {
        return price;
    }

    public List<String> getUnusedCoupons() {
        return unusedCoupons;
    }
}
