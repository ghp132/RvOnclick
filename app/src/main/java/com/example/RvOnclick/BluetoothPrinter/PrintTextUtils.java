package com.example.RvOnclick.BluetoothPrinter;

import java.util.Calendar;

public class PrintTextUtils {
    public static final int TWO_INCH = 32;
    public static final int THREE_INCH = 48;

    //Format 1 Column lengths for 3 inch 48 character printer
    public static final int f1Col1 = 3;
    public static final int f1Col2 = 18;
    public static final int f1Col3 = 9;
    public static final int f1Col4 = 9;
    public static final int f1Col5 = 9;


    public String[] getDateTime() {
        final Calendar c = Calendar.getInstance();
        String dateTime[] = new String[2];
        dateTime[0] = c.get(Calendar.DAY_OF_MONTH) + "/" + c.get(Calendar.MONTH) + "/" + c.get(Calendar.YEAR);
        dateTime[1] = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
        return dateTime;
    }

    public String lineFormatter(String textToPrint, int lineLen) {
        int spaceCount = 0;
        int textLen = textToPrint.length();
        int extraCharacCount = textLen % lineLen;
        spaceCount = lineLen - extraCharacCount;
        if (extraCharacCount > 0) {
            textToPrint = textToPrint + textRepeater(" ", spaceCount);
        }
        return textToPrint;
    }

    public String rLineFomatter(String textToPrint, int lineLen) {
        int spaceCount = 0;
        int textLen = textToPrint.length();
        int extraCharacCount = textLen % lineLen;
        spaceCount = lineLen - extraCharacCount;
        if (extraCharacCount > 0) {
            textToPrint = textRepeater(" ", spaceCount) + textToPrint;
        }

        return textToPrint;
    }

    public String textRepeater(String character, int count) {
        String textToPrint = "";
        for (int i = 1; i <= count; i++) {
            textToPrint = textToPrint + character;
        }
        return textToPrint;
    }

    public int getSpaceCount(String text, int lineLen) {
        int extraSpace = 0;
        int textCount = text.length();
        extraSpace = lineLen - textCount;
        return extraSpace;

    }

    public int len(String toCountString) {
        int count = 0;

        //Counts each character except space
        for (int i = 0; i < toCountString.length(); i++) {
            if (toCountString.charAt(i) != ' ')
                count++;
        }
        return count;
    }
}
