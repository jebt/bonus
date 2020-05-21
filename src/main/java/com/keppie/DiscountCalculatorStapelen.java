package com.keppie;

public class DiscountCalculatorStapelen extends DiscountCalculatorAbstract {
    @Override
    public double getDiscountPercentage(Product product) {
        return -1.00;
    }
}
