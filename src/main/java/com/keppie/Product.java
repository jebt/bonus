package com.keppie;

import java.util.ArrayList;

public class Product {

    private String wi_id = "";
    private String file_path = "";
    private double bonus_percentage = -1.00;
    private String url = "";
    private int price_total_cent = -1;
    private int initial_index = -1;
    private int price_cent = -1;
    private int bonus_price_cent = -1;
    private String html_text;
    private Main.bonusType bonusType;

    // extracted string fields:
    private String price_whole_euros = "";
    private String price_modulo_cents = "";
    private String bonus_type = "";
    private String description = "";
    private String stapelen_tot = "";
    private String price_total = "";
    private String unit_size = "";
    private String bonus_price = "";
    private String x_voor = "";
    private String voor_y = "";

    private ArrayList<ExtractionField> extractionFields;

    // constructor
    public Product(String wi_id) {
        this.wi_id = wi_id;
    }

    // getters and setters below:
    public Main.bonusType getBonusType() {
        return bonusType;
    }
    public void setBonusType(Main.bonusType bonusType) {
        this.bonusType = bonusType;
    }
    public String getWi_id() {
        return wi_id;
    }
    public void setWi_id(String wi_id) {
        this.wi_id = wi_id;
    } //setWi_id shouldn't be necessary because it gets it on creation
    public String getBonus_type() {
        return bonus_type;
    }
    public void setBonus_type(String bonus_type) {
        this.bonus_type = bonus_type;
    }
    public String getFile_path() {
        return file_path;
    }
    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }
    public int getInitial_index() {
        return initial_index;
    }
    public void setInitial_index(int initial_index) {
        this.initial_index = initial_index;
    }
    public String getPrice_total() {
        return price_total;
    }
    public void setPrice_total(String price_total) {
        this.price_total = price_total;
    }
    public int getPrice_total_cent() {
        return price_total_cent;
    }
    public void setPrice_total_cent(int price_total_cent) {
        this.price_total_cent = price_total_cent;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getStapelen_tot() {
        return stapelen_tot;
    }
    public void setStapelen_tot(String stapelen_tot) {
        this.stapelen_tot = stapelen_tot;
    }
    public double getBonus_percentage() {
        return bonus_percentage;
    }
    public void setBonus_percentage(double bonus_percentage) {
        this.bonus_percentage = bonus_percentage;
    }
    public ArrayList<ExtractionField> getExtractionFields() {
        return extractionFields;
    }
    public void setExtractionFields(ArrayList<ExtractionField> extractionFields) {
        this.extractionFields = extractionFields;
    }
    public int getPrice_cent() {
        return price_cent;
    }
    public void setPrice_cent(int price_cent) {
        this.price_cent = price_cent;
    }
    public String getPrice_whole_euros() {
        return price_whole_euros;
    }
    public void setPrice_whole_euros(String price_whole_euros) {
        this.price_whole_euros = price_whole_euros;
    }
    public String getPrice_modulo_cents() {
        return price_modulo_cents;
    }
    public void setPrice_modulo_cents(String price_modulo_cents) {
        this.price_modulo_cents = price_modulo_cents;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getUnit_size() {
        return unit_size;
    }
    public void setUnit_size(String unit_size) {
        this.unit_size = unit_size;
    }
    public String getBonus_price() {
        return bonus_price;
    }
    public void setBonus_price(String bonus_price) {
        this.bonus_price = bonus_price;
    }
    public String getVoor_y() {
        return voor_y;
    }
    public void setVoor_y(String voor_y) {
        this.voor_y = voor_y;
    }
    public String getX_voor() {
        return x_voor;
    }
    public void setX_voor(String x_voor) {
        this.x_voor = x_voor;
    }
    public int getBonus_price_cent() {
        return bonus_price_cent;
    }
    public void setBonus_price_cent(int bonus_price_cent) {
        this.bonus_price_cent = bonus_price_cent;
    }
    public String getHtml_text() {
        return html_text;
    }
    public void setHtml_text(String html_text) {
        this.html_text = html_text;
    }
}