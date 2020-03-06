// "BonusPercentage"
// author: roelantvanderhilst@gmail.com
// don't worry: https://www.youtube.com/watch?v=SETnK2ny1R0

package com.keppie;
// Git: master
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

//todo for new week {CURRENT_WEEK}:
/*
// - 103 change the constant CURRENT_WEEK to the new number
// - 103 create table in ***REMOVED*** db
// - 103 uncomment code
*/

//todo list:
/*
 todo: change field names in database (no capitals!)
 TODO: reflect these field name changes in the code!!

 todo: automatically build a categories tree: lvl0: product, lvl1: afdelingen, lvl2: ... CARE: a product can be in multiple categories!
 todo: make category inherit tree(superclasses/subclasses), assign categories/subcategories so we can do some fancy sorting and collapse/expand action in a future frontend
 todo: add all level of categories as fields to the products

 todo: https://www.vojtechruzicka.com/idea-best-plugins/

 todo: schedule the entire weekly process to as soon as the new bonus info is available (monday at 00:15?).
 todo: see what we can do with next week bonus (becomes available somewhere halfway in/end of the week? determine!)

 todo: for bonus_type="stapelen" show how many you must buy to get the discount

 todo: set all fieldnames of the table as String constants and use those trough out the code?

 todo: use hashmap of stringlist solution for the extraction fields?
 todo: use threads for the parsing (1 per extraction field) to increase processing speeds?
 todo: use hibernate?

 todo: replace all paths with constants
 todo: make it so that all the folders and files get created after checks if they exist. If they do exist already give error and abort
 todo: each week should get it's own superfolder with all the files and subfolders for that week within it
 todo: clean up all the deprecated and unused methods

 todo: catch and handle file already exists bug exception during the mass headless html download. delete folder(and file) of the particular html and try again. for continuity

 todo: t/m dinsdag aanbieidingen handlen
*/

public class Main {

    public static final boolean ONLY_DO_FIRST_THREE = true;
    public static final int CURRENT_WEEK = 103;
    public static final String BASE_URL = "https://www.ah.nl/producten/product/";
    enum dbType{UNKNOWN, VARCHAR16, VARCHAR64, TEXT, INT, DOUBLE};

    static ArrayList<String> wi_ids;
    static Product[] products;
    static Pattern WAS_NOW_PATTERN;
    static Pattern WAS_NOW_PATTERN_GALL;
    static Matcher matcher;

    static void downloadToFolder(String toPath, ArrayList<String> wi_ids) throws IOException {
        for (int i = 0; i < wi_ids.size(); i++) {
            if (ONLY_DO_FIRST_THREE) {
                if (i > 2) break;
            }
            System.out.println(i+1 + "/" + wi_ids.size() + " Downloading " + wi_ids.get(i) + " HTML-File from " +
                    BASE_URL + wi_ids.get(i) + " ...");
            AhLib.dlHtml(BASE_URL, wi_ids.get(i), toPath);
        }
    }
    static void headlessDownloadToFolder(String toPath, ArrayList<String> wi_ids) throws IOException, InterruptedException {
        for (int i = 0; i < wi_ids.size(); i++) {
            if (ONLY_DO_FIRST_THREE) {
                if (i > 2) break;
            }
            System.out.println(i+1 + "/" + wi_ids.size() + " Downloading (headless) " + wi_ids.get(i) + " HTML-File from " +
                    BASE_URL + wi_ids.get(i) + " ...");
            AhLib.dlHtmlHeadless(BASE_URL, wi_ids.get(i), toPath);
        }
    }
    static ArrayList<ExtractionField> generateExtractionFields() {
        ArrayList<ExtractionField> extractionFields = new ArrayList<>();
        extractionFields.add(new ExtractionField(
                "bonus_type",
                "Het soort actie dat deze week geld voor het product",
                "<div class=\"promo-sticker_content__31jjs\">\n" +
                        "                                <p class=\"promo-sticker-text_text__1qwWM\">",
                "</p>",
                dbType.VARCHAR16
        ));
        extractionFields.add(new ExtractionField(
                "description",
                "Een beschrijving van het product",
                "<main title=\"",
                "\"",
                dbType.TEXT
        ));
        extractionFields.add(new ExtractionField(
                "stapelen_tot",
                "In het geval het een stapelen actie betreft, de hoeveelheid die het hoogste kortingspercentage geeft",
                "stapelen                                </p>\n" +
                        "                                <p class=\"promo-sticker-text_text__1qwWM\">\n" +
                        "tot                                </p>\n" +
                        "                                <p class=\"promo-sticker-text_text__1qwWM promo-sticker-text_xl__3MMau\">\n",
                "</p>",
                dbType.VARCHAR16
        ));
        extractionFields.add(new ExtractionField(
                "price_whole_euros",
                "De normale prijs van het product (wanneer het niet in de bonus is). Voor de komma.",
                "<span class=\"price-amount_integer__OuZOn\">\n",
                "</span>",
                dbType.VARCHAR16
        ));
        extractionFields.add(new ExtractionField(
                "price_modulo_cents",
                "De normale prijs van het product (wanneer het niet in de bonus is). Na de komma.",
                "<span class=\"price-amount_fractional__2DgNL\">\n",
                "</span>",
                dbType.VARCHAR16
        ));
        extractionFields.add(new ExtractionField(
                "price_total",
                "Het aantal producten dat gekocht moet worden om het kortingspercentage te krijgen vermenigvuldigd met het gereduceerde tarief",
                "voor                                </p>\n" +
                        "                                <p class=\"promo-sticker-text_text__1qwWM promo-sticker-text_xl__3MMau\">\n",
                "</p>",
                dbType.VARCHAR16
        ));
        extractionFields.add(new ExtractionField(
                "unit_size",
                "De hoeveelheid per eenheid, normaal gesproken uitgedrukt in gram of liter",
                "<div class=\"product-card-header_unitInfo__2mgzj\">\n",
                "<",
                dbType.VARCHAR16
        ));
        extractionFields.add(new ExtractionField( // todo: NEEDS AFTER AND BEFORE
                "bonus_price",
                "Het gereduceerde tarief per eenheid",
                "",
                "",
                dbType.VARCHAR16
        ));
        extractionFields.add(new ExtractionField(
                "was_whole_euros",
                "Originele prijs voor 1 unit, hoeveelheid hele euros",
                "<div class=\"price-amount_root__kIi9Q price-amount_was__19DLG product-card-hero-price_was__EisfA\" data-testhook=\"price-amount\">\n" +
                        "                                <span class=\"price-amount_integer__OuZOn\">\n",
                "</span>",
                dbType.VARCHAR16
        ));
        extractionFields.add(new ExtractionField(
                "was_modulo_cents",
                "Originele prijs voor 1 unit,  aantal centen na de komma",
                "",//TODO: regex oplossing
                "",//TODO: regex oplossing
                dbType.VARCHAR16
        ));
        extractionFields.add(new ExtractionField(
                "now_whole_euros",
                "Gereduceerde tarief voor 1 unit, hoeveelheid hele euros",
                "",//TODO: regex oplossing
                "",//TODO: regex oplossing
                dbType.VARCHAR16
        ));
        extractionFields.add(new ExtractionField(
                "now_modulo_cents",
                "Gereduceerde prijs voor 1 unit, aantal centen na de komma",
                "",//TODO: regex oplossing
                "",//TODO: regex oplossing
                dbType.VARCHAR16
        ));
        extractionFields.add(new ExtractionField(
                "x_voor",
                "Aantal te koop aangeboden producten, de x in een 'x voor y' aanbieding",
                "<p class=\"promo-sticker-text_text__1qwWM promo-sticker-text_xxl__1EmCX\">\n",
                "</p>\n" +
                        "                                <p class=\"promo-sticker-text_text__1qwWM\">\n" +
                        "voor",
                dbType.VARCHAR16
        ));
        extractionFields.add(new ExtractionField(
                "voor_y",
                "Bedrag te betalen in een, de y in een 'x voor y' aanbieding",
                "voor                                </p>\n" +
                        "                                <p class=\"promo-sticker-text_text__1qwWM promo-sticker-text_xl__3MMau\">\n",
                "</p>",
                dbType.VARCHAR16
        ));
        extractionFields.add(new ExtractionField(
                "normale_prijs_per_kg",
                "",
                "",
                "",
                dbType.UNKNOWN
        ));


        // todo: unit_price // normale prijs per unit_price_unit // eerst column aanmaken in db
        // todo: unit_price_unit // per KG / ... // eerst column aanmaken in db
        // todo: catlvl0 stnd: "Producten"
        // todo: catlvl1 e.g.: "Vlees, kip, vis, vega"
        // todo: catlvl2 e.g.: "Kip"
        // todo: catlvl3 e.g.: "Kip- burger, schnitzel, cordon bleu"
        // todo: catlvl4 e.g.: "Schnitzel (kip)"
        // todo: bonuscat to add some fancy collapse/expand action in the frontend (flutter)
        // todo: streepjescode or EAM/EAN-code or something
        // todo: aantal_porties // just because
//        extractionFields.add(new ExtractionField(
//                "",
//                "",
//                "",
//                "",
//                dbType.UNKNOWN
//        ));
        return extractionFields;
    }
    static Product[] generateProducts(ArrayList<String> wi_ids) throws IOException {
        Product[] products = new Product[wi_ids.size()];
        for (int i = 0; i < wi_ids.size(); i++) {
            if (ONLY_DO_FIRST_THREE) {
                if (i > 2) break;
            }
            String wi_id = wi_ids.get(i);
            products[i] = new Product(wi_id);
            Product product = products[i];
            product.setInitial_index(i);
            product.setUrl(BASE_URL + wi_id);
            String absPath = new File("week_" + CURRENT_WEEK + "\\HeadlessHTML_week_" + CURRENT_WEEK + "\\" + wi_id).getAbsolutePath() + ".html";
            product.setFile_path(absPath);
            product.setExtractionFields(generateExtractionFields());
            if (Files.exists(Paths.get(absPath))) {
                product.setHtml_text(Files.readString(Paths.get(absPath), StandardCharsets.UTF_8));
            }
        }
        return products;
    }
    static ArrayList<String> generateAfdelingenList() {
        ArrayList<String> afdelingen = new ArrayList<>();
        afdelingen.add("aardappel-groente-fruit");
        afdelingen.add("verse-kant-en-klaar-maaltijden-salades");
        afdelingen.add("vlees-kip-vis-vega");
        afdelingen.add("kaas-vleeswaren-delicatessen");
        afdelingen.add("zuivel-eieren");
        afdelingen.add("bakkerij");
        afdelingen.add("ontbijtgranen-broodbeleg-tussendoor");
        afdelingen.add("frisdrank-sappen-koffie-thee");
        afdelingen.add("wijn");
        afdelingen.add("bier-sterke-drank-aperitieven");
        afdelingen.add("pasta-rijst-internationale-keuken");
        afdelingen.add("soepen-conserven-sauzen-smaakmakers");
        afdelingen.add("snoep-koek-chips");
        afdelingen.add("diepvries");
        afdelingen.add("drogisterij-baby");
        afdelingen.add("bewuste-voeding");
        afdelingen.add("huishouden-huisdier");
        afdelingen.add("koken-tafelen-non-food");
        return afdelingen;
    }
    static void compileRegexPatterns() {
        WAS_NOW_PATTERN = Pattern.compile("(was__EisfA\" data-testhook=\"price-amount\">\n" +
                "                                <span class=\"price-amount_integer__OuZOn\">\n" +
                ")(\\d*)(\\s*)(</span>\n" +
                "                                <span class=\"price-amount_dot__M_dXY\">\n" +
                ".                                </span>\n" +
                "                                <span class=\"price-amount_fractional__2DgNL\">\n" +
                ")(\\d*)(\\s*)(</span>\n" +
                "                              </div>\n" +
                "                              <div class=\"price-amount_root__kIi9Q price-amount_bonus__3a5qc product-card-hero-price_now__90TBU\" data-testhook=\"price-amount\">\n" +
                "                                <span class=\"price-amount_integer__OuZOn\">\n" +
                ")(\\d*)(\\s*)(</span>\n" +
                "                                <span class=\"price-amount_dot__M_dXY\">\n" +
                ".                                </span>\n" +
                "                                <span class=\"price-amount_fractional__2DgNL\">\n" +
                ")(\\d*)");

        WAS_NOW_PATTERN_GALL = Pattern.compile("(was__EisfA\" data-testhook=\"price-amount\">\n" +
                "                                <span class=\"price-amount_integer__OuZOn\">\n" +
                ")(\\d*)(\\s*)(</span>\n" +
                "                                <span class=\"price-amount_dot__M_dXY\">\n" +
                ".                                </span>\n" +
                "                                <span class=\"price-amount_fractional__2DgNL\">\n" +
                ")(\\d*)(\\s*)(</span>\n" +
                "                              </div>\n" +
                "                              <div class=\"price-amount_root__kIi9Q price-amount_gall__90sIY product-card-hero-price_now__90TBU\" data-testhook=\"price-amount\">\n" +
                "                                <span class=\"price-amount_integer__OuZOn\">\n)(\\d*)(\\s*)(</span>\n" +
                "                                <span class=\"price-amount_dot__M_dXY\">\n" +
                ".                                </span>\n" +
                "                                <span class=\"price-amount_fractional__2DgNL\">\n" +
                ")(\\d*)");
    }
    static int wasNow(Product product) throws IOException {
        int bonus_price = -1;
        String content = product.getHtml_text();
        matcher = WAS_NOW_PATTERN.matcher(content);
        while (matcher.find()) {
            bonus_price = Integer.parseInt(matcher.group(8))*100 + Integer.parseInt(matcher.group(11));
            return bonus_price;
        }
        matcher = WAS_NOW_PATTERN_GALL.matcher(content);
        while (matcher.find()) {
            bonus_price = Integer.parseInt(matcher.group(8))*100 + Integer.parseInt(matcher.group(11));
            return bonus_price;
        }
        return bonus_price;
    }
    static void checkCreateWeekDir(){
        String dirPath = "week_" + CURRENT_WEEK + "\\";
        if (Files.exists(Paths.get(dirPath))) {
            System.out.println(dirPath + " directory already exists! Terminating.");
            System.exit(871);
        }
        File weekDir = new File(dirPath);
        weekDir.mkdir();

        // Creating sub folders
        String subPath;
        File subDir;

        subPath = dirPath + "HTML_week_" + CURRENT_WEEK + "\\";
        subDir = new File(subPath);
        subDir.mkdir();

        subPath = dirPath + "HeadlessHTML_week_" + CURRENT_WEEK + "\\";
        subDir = new File(subPath);
        subDir.mkdir();

        subPath = dirPath + "BonusPerAfdelingHeadlessHTML_week_" + CURRENT_WEEK + "\\";
        subDir = new File(subPath);
        subDir.mkdir();

        System.out.println("This week's folders created.");
    }

    //todo####################################################################################################

    public static void main(String[] args) throws IOException, InterruptedException, SQLException, ClassNotFoundException {

        System.out.println("Starting process for week " + CURRENT_WEEK + "...");
        final long startTime = System.nanoTime();
        System.out.println(startTime);

        Connection conn = AhLib.connect();
        //checkCreateWeekDir();

        // Download the web pages for the bonus products per afdeling
        ArrayList<String> afdelingen = generateAfdelingenList();
        String toPath = "week_" + CURRENT_WEEK + "\\BonusPerAfdelingHeadlessHTML_week_" + CURRENT_WEEK + "\\";
        String leftUrl = "https://www.ah.nl/producten/";
        String rightUrl = "?kenmerk=bonus&page=99";
        for (int i = 0; i < afdelingen.size(); i++) {
            if (ONLY_DO_FIRST_THREE) {
                if (i > 2) break;
            }
            String afdeling = afdelingen.get(i);
            System.out.println("Downloading afdeling bonus pages through headless browser: " + afdeling + "...");
            String url = leftUrl + afdeling + rightUrl;
            AhLib.dlHtmlHeadlessGeneral(url, toPath, afdeling);
        }

        // Extract the bonus wi_ids and make a list eliminating duplicates
        System.out.println("Extract the bonus wi_ids and make a list eliminating duplicates...");
        String pathForRegex;
        String content;
        final Pattern WI_PATTERN = Pattern.compile("(href=\"/producten/product/wi)(\\d*)(/)");
        Matcher m;
        ArrayList<String> wi_ids = new ArrayList<>();
        for (int i = 0; i < afdelingen.size(); i++) {
            if (ONLY_DO_FIRST_THREE) {
                if (i > 2) break;
            }
            pathForRegex = "week_" + CURRENT_WEEK + "\\BonusPerAfdelingHeadlessHTML_week_" + CURRENT_WEEK + "\\" + afdelingen.get(i) + ".html";
            content = Files.readString(Paths.get(pathForRegex), StandardCharsets.UTF_8);
            m = WI_PATTERN.matcher(content);
            while (m.find()) {
                if (ONLY_DO_FIRST_THREE) {
                    if (i > 2) break;
                }
                wi_ids.add("wi" + m.group(2));
            }
        }
        ArrayList<String> wi_ids_noDupes = (ArrayList<String>) wi_ids.stream().distinct().collect(Collectors.toList());
        System.out.println("Number of unique wi_ids: " + wi_ids_noDupes.size());

        // Write this week's bonus wi_ids to a text file
        FileWriter writer = new FileWriter("week_" + CURRENT_WEEK + "\\" + "wi_ids_week_" + CURRENT_WEEK + "_distinct.txt");
        for(String str: wi_ids_noDupes) {
            writer.write(str + System.lineSeparator());
        }
        writer.close();
        System.out.println("List saved.");

        // Read in this week's bonus wi_id list
        wi_ids = AhLib.fileToList("week_" + CURRENT_WEEK + "\\" + "wi_ids_week_" + CURRENT_WEEK + "_distinct.txt");
        System.out.println("This week's list read. wi_ids.size(): " + wi_ids.size());

        // Download web pages to the filesystem through a headless browser
        headlessDownloadToFolder("week_" + CURRENT_WEEK + "\\HeadlessHTML_week_" + CURRENT_WEEK + "\\", wi_ids);
        System.out.println("Done downloading headless HTML-files.");

        // Download plain html-files to the filesystem
        downloadToFolder("week_" + CURRENT_WEEK + "\\HTML_week_" + CURRENT_WEEK + "\\", wi_ids);
        System.out.println("Done downloading HTML-files.");

        // Create and fill an array of Product with the .wi_id, .initial_index, .url and .filePath properties assigned.
        products = generateProducts(wi_ids);
        System.out.println("Array of products created. Fresh out of the oven.");

        compileRegexPatterns();
        System.out.println("Regex patterns compiled and ready to match some text, yo!");

        // master loop:
        for (int i = 0; i < products.length; i++) {
            if (ONLY_DO_FIRST_THREE) {
                if (i > 2) break;
            }
            System.out.println("Processing product " + (i+1) + "/" + products.length + "...");
            Product product = products[i];
            if (Files.exists(Paths.get(product.getFile_path()))) {
                AhLib.extractFields(product);
                product.setBonus_price_cent(wasNow(product));
                if (!(product.getX_voor().equals("?")) && !(product.getX_voor().equals("??")) && !(product.getX_voor().equals(""))) {
                    product.setBonus_type("x_voor_y");
                } else if (AhLib.is2eHalvePrijs(product)) {
                    product.setBonus_type("2e_halve_prijs");
                } else if (AhLib.is3Is2(product)) {
                    product.setBonus_type("3_is_2");
                }
                product.setPrice_cent(AhLib.price_cent(product));
                product.setPrice_total_cent(AhLib.price_total_cent(product));
                product.setBonus_percentage(AhLib.bonus_percentage(product));
            } else {
                System.out.println("Html file was not found for this product, fields not extracted!");
                product.setBonus_type("HTML NOT FOUND");
            }
            AhLib.insertProduct(product, "week_" + CURRENT_WEEK);
        }

        final long endTime = System.nanoTime();
        final long duration = endTime - startTime;
        long durationSeconds = TimeUnit.NANOSECONDS.toSeconds(duration);
        System.out.println(endTime);
        System.out.println("Done! It took " + durationSeconds + " seconds.");
    }
}