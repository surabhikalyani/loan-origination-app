package com.example.loanorigination.util;

public class DataMaskingUtil {

    private DataMaskingUtil() {
    }

    public static String maskSsn(String ssn) {
        if (ssn == null) return "***-**-****";
        String d = ssn.replaceAll("[^0-9]", "");
        if (d.length() != 9) return "***-**-****";
        return "***-**-" + d.substring(5); // ***-**-6789
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "****";
        String[] parts = email.split("@");
        String namePart = parts[0];
        return namePart.charAt(0) + "****@" + parts[1];
    }
}
