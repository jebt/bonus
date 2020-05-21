package com.keppie;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class AhLib {

    // fileToList
    public static ArrayList<String> fileToList(String filePath) throws FileNotFoundException {

        filePath = filePath.replace('/', File.separatorChar);
        String filePathAbsolute = new File(filePath).getAbsolutePath();

        ArrayList<String> list = new ArrayList<>();
        Scanner s = new Scanner(new File(filePath));

        while (s.hasNext()) {
            list.add(s.next());
        }
        s.close();

        return list;
    }

    // Download an HTML-file from a URL and save it to the filesystem
    public static void dlHtml(String urlBase, String wi_id, String toPath) throws IOException {
        if (Files.exists(Paths.get(toPath + wi_id + ".html"))) {
            System.out.println(toPath + wi_id + ".html" + " file already exists!");
            return;
        }

        java.net.URL url;
        InputStream is = null;
        BufferedReader br;
        String line;
        String urlText = urlBase + wi_id;

        //File htmlFile = new File("HTML" + File.pathSeparator + wi_id + ".html");
        String absPath = new File(toPath + wi_id + ".html").getAbsolutePath();
        File htmlFile = new File(absPath);
        FileOutputStream fos = new FileOutputStream(htmlFile);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        try {
            url = new URL(urlText);
            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine();
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ioe) {
                // nothing to see here
            }
        }
        bw.close();
    }

    // Download an HTML-file from a URL through a headless browser and save it to the filesystem
    public static void dlHtmlHeadless(final String urlBase, final String wi_id, final String toPath) throws IOException, InterruptedException {
        if (Files.exists(Paths.get(toPath + wi_id + "\\"))) {
            System.out.println(toPath + wi_id + "\\" + " folder already exists!");
            return;
        }
        if (Files.exists(Paths.get(toPath + wi_id + ".html"))) {
            System.out.println(toPath + wi_id + ".html" + " file already exists!");
            return;
        }
        HtmlPage page;
        String urlText = urlBase + wi_id;
        try (final WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED)) {
            webClient.getOptions().setJavaScriptEnabled(true); //todo false?
            webClient.getOptions().setCssEnabled(true); //todo false?
            webClient.setCssErrorHandler(new com.gargoylesoftware.htmlunit.SilentCssErrorHandler());
            webClient.getOptions().setPrintContentOnFailingStatusCode(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);

            page = webClient.getPage(urlText);
            Thread.sleep(500);
            webClient.waitForBackgroundJavaScript(500);

            String absPath = new File(toPath + wi_id + ".html").getAbsolutePath();
            //SAVE TO THE FILESYSTEM
            page.save(new File(absPath));
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
            //if (e.getCause() instanceof FileAlreadyExistsException) {
            if (e.getMessage().substring(0, 18).equals("File already exists")) {
                // delete folder and/or file and try again
                if (Files.exists(Paths.get(toPath + wi_id + "\\"))) {
                    System.out.println(toPath + wi_id + "\\" + " folder exists, deleting");
                    // delete files inside the folder and then the folder
                    File dirToDelete = new File(toPath + wi_id + "\\");
                    String[] entries = dirToDelete.list();
                    for (String s :
                            entries) {
                        File currentFile = new File(dirToDelete.getPath(), s);
                        currentFile.delete();
                    }
                    dirToDelete.delete();
                }
                if (Files.exists(Paths.get(toPath + wi_id + ".html"))) {
                    System.out.println(toPath + wi_id + ".html" + " file exists, deleting");
                    File fileToDelete = new File(toPath + wi_id + ".html");
                    fileToDelete.delete();
                }
                System.out.println("Trying again...");
                dlHtmlHeadless(urlBase, wi_id, toPath);
            } else {
                throw e;
            }
        }
    }

    // connect to postgres bonus db and return the Connection object
    public static Connection connect() throws SQLException {
        String db_url = "";
        String db_user = "";
        String db_password = "";

        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader("dbCredentials.json")) {
            Object obj = jsonParser.parse(reader);
            JSONObject dbCredentials = (JSONObject) obj;

            db_url = (String) dbCredentials.get("db_url");
            db_user = (String) dbCredentials.get("db_user");
            db_password = (String) dbCredentials.get("db_password");
            return DriverManager.getConnection(db_url, db_user, db_password);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(872);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(872);
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(872);
        }
        return DriverManager.getConnection(db_url, db_user, db_password);
    }

    // general headless html download method
    public static void dlHtmlHeadlessGeneral(String url, String toFolderPath, String pageName) throws IOException, InterruptedException {
        if (Files.exists(Paths.get(toFolderPath + pageName + "\\"))) {
            System.out.println(toFolderPath + pageName + "\\" + " folder already exists!");
            return;
        }
        if (Files.exists(Paths.get(toFolderPath + pageName + ".html"))) {
            System.out.println(toFolderPath + pageName + ".html" + " file already exists!");
            return;
        }
        HtmlPage page;
        String urlText = url;
        try (final WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED)) {
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setCssEnabled(false);
            webClient.setCssErrorHandler(new com.gargoylesoftware.htmlunit.SilentCssErrorHandler());
            webClient.getOptions().setPrintContentOnFailingStatusCode(true);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);

            page = webClient.getPage(urlText);
            Thread.sleep(500);
            webClient.waitForBackgroundJavaScript(500);

            String absPath = new File(toFolderPath + pageName + ".html").getAbsolutePath();
            //SAVE TO THE FILESYSTEM
            page.save(new File(absPath));

        } catch (IOException e) {
            //..
        } catch (InterruptedException e) {
            //..
        } catch (FailingHttpStatusCodeException e) {
            //..
        }

    }

    public static boolean createBonusTable(int CURRENT_WEEK) throws IOException, SQLException {
        String sql = Files.readString(Paths.get("src\\main\\java\\com\\keppie\\createWeekTable.sql"), StandardCharsets.UTF_8);
        sql = sql.replace("{CURRENT_WEEK}", Integer.toString(CURRENT_WEEK));

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            boolean returned = pstmt.execute();
            return returned;
        }
    }

    // insert product in db
    public static long insertProduct(Product product, String tableName) {
        String SQL = "INSERT INTO " + tableName + "(wi_id, url, initial_index, file_path, bonus_percentage, price_cent, " +
                "description, unit_size, bonus_type, stapelen_tot, price_total_cent, x_voor, voor_y, bonus_price_cent)"
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        long kp_id = -1;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)
        ) {
            pstmt.setString(1, product.getWi_id());
            pstmt.setString(2, product.getUrl());
            pstmt.setInt(3, product.getInitial_index());
            pstmt.setString(4, product.getFile_path());
            pstmt.setDouble(5, product.getBonus_percentage());
            pstmt.setInt(6, product.getPrice_cent());
            pstmt.setString(7, product.getDescription());
            pstmt.setString(8, product.getUnit_size());
            pstmt.setString(9, product.getBonus_type());
            pstmt.setString(10, product.getStapelen_tot());
            pstmt.setInt(11, product.getPrice_total_cent());
            pstmt.setString(12, product.getX_voor());
            pstmt.setString(13, product.getVoor_y());
            pstmt.setInt(14, product.getBonus_price_cent());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        kp_id = rs.getLong(1);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return kp_id;
    }

    // deduce bonus percentage
    public static double bonus_percentage(Product product) {
        double bonus_percentage = -1.00;
        String bonus_type = product.getBonus_type();
        String stapelen_tot = product.getStapelen_tot();

        switch (bonus_type) {
            case "1+1":
                return 50.00;
            case "2+1":
            case "3_is_2":
                return 33.33;
            case "3+1":
            case "2e_halve_prijs":
                return 25.00;
        }

        int price_cent = product.getPrice_cent();
        int price_total_cent = product.getPrice_total_cent();

        if (bonus_type.equals("x_voor_y")) {
            double xInt = Integer.parseInt(product.getX_voor());
            double normalTotal = price_cent * xInt;
            bonus_percentage = (((1 - (price_total_cent / normalTotal))) * 100);
            return bonus_percentage;
        }

        int bonus_price_cent = product.getBonus_price_cent();
        if (bonus_price_cent != -1) {
            double was = price_cent;
            double now = bonus_price_cent;
            bonus_percentage = (((1 - (now / was))) * 100);
        }

        String percentageSignRemoved = stapelen_tot.replace("%", "");
        if (!percentageSignRemoved.equals("?") && !percentageSignRemoved.equals("??")) {
            bonus_percentage = Double.parseDouble(percentageSignRemoved);
            if (bonus_percentage > 0) {
                return bonus_percentage;
            }
        }

        if (bonus_type.equals("2 stapelen") | bonus_type.equals("3 stapelen") | bonus_type.equals("4 stapelen")) {
            String numberStr = "" + bonus_type.charAt(0);
            int number = Integer.parseInt(numberStr);
            double normalTotal = (number * price_cent);
            bonus_percentage = (((1 - (price_total_cent / normalTotal))) * 100);
            return bonus_percentage;
        }
        return bonus_percentage;
    }

    // general extraction
    public static String extract(String text, String after, String before) throws IOException {
        String value = "";
        int sepPos = text.indexOf(after);
        if (sepPos == -1) {
            return "?";
        }
        String beginCutOff = text.substring(sepPos + after.length());
        int sepPos2 = beginCutOff.indexOf(before);
        if (sepPos2 == -1) {
            return "??";
        }
        value = beginCutOff.substring(0, sepPos2);
        value = value.trim();
        return value;
    }

    // extract fields and assign them to the input product
    public static void extractFields(Product product) throws IOException {
        ArrayList<ExtractionField> extractionFields = product.getExtractionFields();
        for (int i = 0; i < extractionFields.size(); i++) {
            ExtractionField extractionField = extractionFields.get(i);
            //if (extractionField.getName().equals("foo")) {continue;}
            //if (extractionField.getName().equals("bar")) {continue;}
            extractionField.execute(product);
        }
    }

    // calculate normal price in cents
    public static int price_cent(Product product) {
        int price_cent = 0;
        String centString = product.getPrice_modulo_cents();
        String euroString = product.getPrice_whole_euros();
        if (centString.equals("?") | centString.equals("??")) {
            return -1;
        }
        int cents1 = Integer.parseInt(centString);
        int cents2 = Integer.parseInt(euroString);
        cents2 = cents2 * 100;
        price_cent = cents1 + cents2;
        return price_cent;
    }

    public static int price_total_cent(Product product) {
        int price_total_cent = -1;
        String bonus_type = product.getBonus_type();
        if (!(bonus_type.equals("2 stapelen") | bonus_type.equals("3 stapelen") | bonus_type.equals("4 stapelen") | bonus_type.equals("x_voor_y"))) {
            return price_total_cent;
        }

        if (bonus_type.equals("x_voor_y")) {
            int yInt = priceStringToCents(product.getVoor_y());
            return yInt;
        }

        String price_total = product.getPrice_total();
        double priceDouble = -1.00;
        if (!price_total.equals("?") && !price_total.equals("??")) {
            priceDouble = Double.parseDouble(price_total);
            priceDouble = priceDouble * 100;
            price_total_cent = (int) priceDouble;
        }
        return price_total_cent;
    }

    public static int priceStringToCents(String input) {
        double priceDouble = -1.00;
        priceDouble = Double.parseDouble(input);
        priceDouble = priceDouble * 100;
        int cents = (int) priceDouble;
        return cents;
    }

    public static boolean textContains(String text, String contains) {
        boolean does = false;
        int sepPos = text.indexOf(contains);
        if (sepPos != -1) {
            does = true;
        }
        return does;
    }

    public static boolean is2eHalvePrijs(Product product) {
        boolean is = false;
        if (textContains(product.getHtml_text(), "<p class=\"promo-sticker-text_text__1qwWM promo-sticker-text_xxl__1EmCX\">\n" +
                "2e                                </p>\n" +
                "                                <p class=\"promo-sticker-text_text__1qwWM\">\n" +
                "halve prijs                                </p>")) {
            is = true;
        }
        return is;
    }

    public static boolean is3Is2(Product product) {
        boolean is = false;
        if (textContains(product.getHtml_text(), "<p class=\"promo-sticker-text_text__1qwWM promo-sticker-text_xl__3MMau\">\n" +
                "3=2                                </p>")) {
            is = true;
        }
        return is;
    }
}