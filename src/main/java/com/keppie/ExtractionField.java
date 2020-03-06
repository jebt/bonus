package com.keppie;

import java.io.IOException;

// todo: remove this class and use a Hashmap of ArrayList<String> instead. maybe. probably.
public class ExtractionField {

    private String name;
    private String info;
    private String after;
    private String before;
    Main.dbType dbType;

    // to be filled by execute()
    private String valueString;

    // to optionally be filled by execute()
    private int valueInt = -1;           // optional
    private double valueDouble = -1.00;  // optional

    public String getName() {
        return name;
    }

    public String getValueString() {
        return valueString;
    }

    public int getValueInt() {
        return valueInt;
    }

    public double getValueDouble() {
        return valueDouble;
    }

    public ExtractionField(String name, String info, String after, String before, Main.dbType dbType) {
        this.name = name;
        this.info = info;
        this.after = after;
        this.before = before;
        this.dbType = dbType;

        this.valueString = "VALUESTRING_PLACEHOLDER";  // default

        this.valueInt = -1;        // default
        this.valueDouble = -1.00;  // default
    }

    // parse .html file, extract and fill valueString.
    public void execute(Product product) throws IOException {
        valueString = AhLib.extract(product.getHtml_text(), after, before);
        if (name.equals("price_whole_euros")) {
            product.setPrice_whole_euros(valueString);
        } else if (name.equals("price_modulo_cents")) {
            product.setPrice_modulo_cents(valueString);
        } else if (name.equals("bonus_type")) {
            product.setBonus_type(valueString);
        } else if (name.equals("description")) {
            product.setDescription(valueString);
        } else if (name.equals("stapelen_tot")) {
            product.setStapelen_tot(valueString);
        } else if (name.equals("price_total")) {
            product.setPrice_total(valueString);
        } else if (name.equals("unit_size")) {
            product.setUnit_size(valueString);
        } else if (name.equals("bonus_price")) {
            product.setBonus_price(valueString);
        } else if (name.equals("x_voor")) {
            product.setX_voor(valueString);
        } else if (name.equals("voor_y")) {
            product.setVoor_y(valueString);
        }
    }
}