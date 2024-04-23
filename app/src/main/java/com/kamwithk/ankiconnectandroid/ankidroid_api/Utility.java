package com.kamwithk.ankiconnectandroid.ankidroid_api;

import android.text.Html;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utility {

    private static Pattern STYLE_PATTERN = Pattern.compile("(?s)<style.*?>.*?</style>");
    private static Pattern SCRIPT_PATTERN = Pattern.compile("(?s)<script.*?>.*?</script>");
    private static Pattern TAG_PATTERN = Pattern.compile("<.*?>");
    private static Pattern IMG_PATTERN = Pattern.compile("<img src=[\"']?([^\"'>]+)[\"']? ?/?>");
    private static Pattern HTML_ENTITIES_PATTERN = Pattern.compile("&#?\\w+;");
    private static final String FIELD_SEPARATOR = Character.toString('\u001f');

    private Utility() {
    }

    // taken from AnkiDroid
    public static String[] splitTags(String tags) {
        if (tags == null) {
            return null;
        }
        return tags.trim().split("\\s+");
    }

    public static String[] splitFields(String fields) {
        return fields != null? fields.split(FIELD_SEPARATOR, -1): null;
    }

    public static long getFieldChecksum(String data) {
        String strippedData = stripHTMLMedia(data);

        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] digest = md.digest(strippedData.getBytes(StandardCharsets.UTF_8));
            BigInteger bigInteger = new BigInteger(1, digest);
            String result = bigInteger.toString(16);

            if(result.length() < 40) {
                String zeroes = "0000000000000000000000000000000000000000";
                result = zeroes.substring(0, zeroes.length() - result.length()) + result;
            }
            return Long.valueOf(result.substring(0, 8), 16);
        }
        catch (Exception e) {
            throw new IllegalStateException("Error making field checksum with SHA1 algorithm and UTF-8 encoding", e);
        }
    }

    private static String stripHTMLMedia(String s) {
        Matcher imgMatcher = IMG_PATTERN.matcher(s);
        return stripHTML(imgMatcher.replaceAll(" $1 "));
    }

    private static String stripHTML(String s) {
        Matcher htmlMatcher = STYLE_PATTERN.matcher(s);
        String strRep = htmlMatcher.replaceAll("");
        htmlMatcher = SCRIPT_PATTERN.matcher(strRep);
        strRep = htmlMatcher.replaceAll("");
        htmlMatcher = TAG_PATTERN.matcher(strRep);
        strRep = htmlMatcher.replaceAll("");
        return entsToTxt(strRep);
    }

    private static String entsToTxt(String html) {
        String htmlReplaced = html.replace("&nbsp;", " ");
        Matcher htmlEntities = HTML_ENTITIES_PATTERN.matcher(htmlReplaced);
        StringBuffer sb = new StringBuffer();
        while (htmlEntities.find()) {
            htmlEntities.appendReplacement(sb, Html.fromHtml(htmlEntities.group()).toString());
        }
        htmlEntities.appendTail(sb);
        return sb.toString();
    }
}