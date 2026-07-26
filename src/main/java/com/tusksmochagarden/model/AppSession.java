package com.tusksmochagarden.model;

public class AppSession {

    public static String username;
    public static String path;
    public static String date;
    public static Integer id;
    public static Integer cID;
    public static Boolean isAdmin;

    // Session info for the POS
    public static java.time.LocalTime loginTime;

    // POS settings (per session)
    public static boolean autoPrint = true;
    public static boolean showVat = false;
    public static boolean registerSounds = true;

}
