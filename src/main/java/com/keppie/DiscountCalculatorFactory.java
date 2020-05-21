package com.keppie;

public class DiscountCalculatorFactory {

    public static DiscountCalculatorAbstract getDiscountCalculator(Main.bonusType bonusType) {
        switch (bonusType) {
            case PERCENT:
                //return new DiscountCalculatorPercent();
            case STAPELEN:
                return new DiscountCalculatorStapelen();
            case X_PLUS_Y:
                //return new DiscountCalculatorXPlusY();
            case TWEEDE_HALVE_PRIJS:
                //return new DiscountCalculator2eHalvePrijs();
            case X_VOOR_Y:
                return new DiscountCalculatorXVoorY();
            default:
                return new DiscountCalculatorStapelen();
        }

    }

}
