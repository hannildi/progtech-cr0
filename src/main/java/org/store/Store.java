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

    public PriceInfo getCartPrice(Cart cart, Period period, List<String> coupons) {
        Map<Product, Double> quantities = sumQuantitiesByProduct(cart);
        CouponState couponState = applyCoupons(quantities, period, coupons);
        double total = calculateTotal(couponState.getQuantities(), period, couponState.getPercentageDiscounts());

        return new PriceInfo(roundToNearestFive(total), couponState.getUnusedCoupons());
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

    private double calculateTotal(
            Map<Product, Double> quantities,
            Period period,
            Map<Product, Double> percentageDiscounts) {
        double total = 0.0;

        for (Map.Entry<Product, Double> entry : quantities.entrySet()) {
            Product product = entry.getKey();
            double quantity = entry.getValue();
            double unitPrice = period.getUnitPrice(product);
            double productTotal = unitPrice * quantity;
            double periodDiscount = period.getBestDiscount(product, quantity);
            double couponDiscount = percentageDiscounts.getOrDefault(product, 0.0);
            double bestDiscount = Math.max(periodDiscount, couponDiscount);

            total += productTotal * (1.0 - bestDiscount);
        }

        return total;
    }

    private CouponState applyCoupons(
            Map<Product, Double> originalQuantities,
            Period period,
            List<String> coupons) {
        Map<Product, Double> quantities = new HashMap<>(originalQuantities);
        Map<Product, Double> percentageDiscounts = new HashMap<>();
        Map<Product, Boolean> couponUsedForProduct = new HashMap<>();
        List<String> unusedCoupons = new ArrayList<>();

        for (String coupon : coupons) {
            Product product = getCouponProduct(coupon);

            if (product == null
                    || couponUsedForProduct.getOrDefault(product, false)
                    || quantities.getOrDefault(product, 0.0) <= 0.0) {
                unusedCoupons.add(coupon);
                continue;
            }

            if (isFreeCoupon(coupon)) {
                double oldQuantity = quantities.get(product);
                quantities.put(product, Math.max(0.0, oldQuantity - 1.0));
                couponUsedForProduct.put(product, true);
                continue;
            }

            double couponDiscount = getCouponDiscount(coupon);
            double periodDiscount = period.getBestDiscount(product, quantities.get(product));

            if (couponDiscount > periodDiscount) {
                percentageDiscounts.put(product, couponDiscount);
                couponUsedForProduct.put(product, true);
            } else {
                unusedCoupons.add(coupon);
            }
        }

        return new CouponState(quantities, percentageDiscounts, unusedCoupons);
    }

    private Product getCouponProduct(String coupon) {
        if (coupon.startsWith("A")) {
            return Product.APPLE;
        }
        if (coupon.startsWith("B")) {
            return Product.BANANA;
        }
        return null;
    }

    private boolean isFreeCoupon(String coupon) {
        return coupon.equals("A-FREE1") || coupon.equals("B-FREE1");
    }

    private double getCouponDiscount(String coupon) {
        if (coupon.equals("A5") || coupon.equals("B5")) {
            return 0.05;
        }
        if (coupon.equals("A10") || coupon.equals("B10")) {
            return 0.10;
        }
        return 0.0;
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

    private static class CouponState {
        private final Map<Product, Double> quantities;
        private final Map<Product, Double> percentageDiscounts;
        private final List<String> unusedCoupons;

        private CouponState(
                Map<Product, Double> quantities,
                Map<Product, Double> percentageDiscounts,
                List<String> unusedCoupons) {
            this.quantities = quantities;
            this.percentageDiscounts = percentageDiscounts;
            this.unusedCoupons = unusedCoupons;
        }

        private Map<Product, Double> getQuantities() {
            return quantities;
        }

        private Map<Product, Double> getPercentageDiscounts() {
            return percentageDiscounts;
        }

        private List<String> getUnusedCoupons() {
            return unusedCoupons;
        }
    }
}
