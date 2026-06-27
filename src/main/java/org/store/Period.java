package org.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Period {
    private final String name;
    private final Map<Product, Double> unitPrices = new HashMap<>();
    private final Map<Product, List<Discount>> discounts = new HashMap<>();

    public Period(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setUnitPrice(Product product, double unitPrice) {
        unitPrices.put(product, unitPrice);
    }

    public double getUnitPrice(Product product) {
        return unitPrices.get(product);
    }

    public void setDiscount(Product product, double threshold, double discountRate) {
        discounts
                .computeIfAbsent(product, key -> new ArrayList<>())
                .add(new Discount(threshold, discountRate));
    }

    public double getBestDiscount(Product product, double quantity) {
        double bestDiscount = 0.0;

        for (Discount discount : discounts.getOrDefault(product, List.of())) {
            if (quantity >= discount.getThreshold() && discount.getDiscountRate() > bestDiscount) {
                bestDiscount = discount.getDiscountRate();
            }
        }

        return bestDiscount;
    }

    private static class Discount {
        private final double threshold;
        private final double discountRate;

        private Discount(double threshold, double discountRate) {
            this.threshold = threshold;
            this.discountRate = discountRate;
        }

        private double getThreshold() {
            return threshold;
        }

        private double getDiscountRate() {
            return discountRate;
        }
    }
}
