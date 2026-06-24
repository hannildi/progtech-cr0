package org.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Store {
    private final Map<Product, Double> prices = new HashMap<>();
    private final Map<Product, List<Discount>> discounts = new HashMap<>();
    private final List<Period> periods = new ArrayList<>();

    public Store() {
    }

    public Store(Product product, double unitPrice) {
        prices.put(product, unitPrice);
    }

    public Store(List<ProductPrice> productPrices) {
        for (ProductPrice productPrice : productPrices) {
            prices.put(productPrice.getProduct(), productPrice.getUnitPrice());
        }
    }

    public void setDiscount(Product product, double threshold, double discountRate) {
        discounts
                .computeIfAbsent(product, key -> new ArrayList<>())
                .add(new Discount(threshold, discountRate));
    }

    public void addPeriod(Period period) {
        periods.add(period);
    }

    public double getCartPrice(Cart cart) {
        Map<Product, Double> quantities = sumQuantitiesByProduct(cart);
        return calculateTotal(quantities);
    }

    public double getCartPrice(Cart cart, Period period) {
        Map<Product, Double> quantities = sumQuantitiesByProduct(cart);
        return calculateTotal(quantities, period);
    }

    private double calculateTotal(Map<Product, Double> quantities) {
        double total = 0.0;

        for (Map.Entry<Product, Double> entry : quantities.entrySet()) {
            Product product = entry.getKey();
            double quantity = entry.getValue();
            double unitPrice = prices.get(product);
            double productTotal = unitPrice * quantity;
            double discountRate = findBestDiscount(product, quantity);

            total += productTotal * (1.0 - discountRate);
        }

        return roundToNearestFive(total);
    }

    private double calculateTotal(Map<Product, Double> quantities, Period period) {
        double total = 0.0;

        for (Map.Entry<Product, Double> entry : quantities.entrySet()) {
            Product product = entry.getKey();
            double quantity = entry.getValue();
            double unitPrice = period.getUnitPrice(product);
            double productTotal = unitPrice * quantity;
            double discountRate = period.getBestDiscount(product, quantity);

            total += productTotal * (1.0 - discountRate);
        }

        return roundToNearestFive(total);
    }

    private Map<Product, Double> sumQuantitiesByProduct(Cart cart) {
        Map<Product, Double> quantities = new HashMap<>();

        for (Item item : cart.getItems()) {
            double oldQuantity = quantities.getOrDefault(item.getProduct(), 0.0);
            quantities.put(item.getProduct(), oldQuantity + item.getQuantity());
        }

        return quantities;
    }

    private double findBestDiscount(Product product, double quantity) {
        double bestDiscount = 0.0;

        for (Discount discount : discounts.getOrDefault(product, List.of())) {
            if (quantity >= discount.getThreshold() && discount.getDiscountRate() > bestDiscount) {
                bestDiscount = discount.getDiscountRate();
            }
        }

        return bestDiscount;
    }

    private double roundToNearestFive(double amount) {
        double remainder = amount % 10.0;

        if (remainder < 2.5) {
            return amount - remainder;
        } else if (remainder < 5.0) {
            return amount - remainder + 5.0;
        } else if (remainder < 7.5) {
            return amount - remainder + 5.0;
        } else {
            return amount - remainder + 10.0;
        }
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
