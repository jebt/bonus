package com.keppie;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    FileWriter writer;
    SimpleDateFormat simpleDateFormatdf;
    Date date;
    String timeStamp;
    String output;

    public Logger() throws IOException {
        simpleDateFormatdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");
        date = new Date();
        String date_start_string = simpleDateFormatdf.format(date);
        String absPath = new File("logs/" + date_start_string + ".log").getAbsolutePath();
        writer = new FileWriter(absPath);
        writer.write("Starting to log at " + date_start_string + "...");
        writer.close();
        writer = new FileWriter(absPath, true);
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(String message) throws IOException {
        date = new Date();
        timeStamp = simpleDateFormatdf.format(date);
        output = System.lineSeparator() + timeStamp + "   " + message;
        System.out.println(message);
        writer.write(output);
        writer.flush();
    }
}