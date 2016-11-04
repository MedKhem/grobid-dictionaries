package org.grobid.core.features;

import org.grobid.core.features.enums.FontStatus;
import org.grobid.core.features.enums.LineStatus;

/**
 * Created by med on 20.10.16.
 * <p>
 * Class contains elementary steps for producing features from a content of a document (tokens, text..)
 */


public class FeaturesUtils {

    public FeaturesUtils() {

    }

    public static String[] checkFontStatus(String currentFont, String previousFont, String fontStatus) {

        if (previousFont == null) {
            previousFont = currentFont;
            fontStatus = FontStatus.NEWFONT.toString();
        } else if (!previousFont.equals(currentFont)) {
            previousFont = currentFont;
            fontStatus = FontStatus.NEWFONT.toString();
        } else {
            fontStatus = FontStatus.SAMEFONT.toString();
        }
        return new String[]{previousFont, fontStatus};
    }


    public static String checkLineStatus(String text, Boolean previousTokenIsNewLineAfter, String previousTokenText,
                                         Boolean nextTokenIsNewLineAfter, String nextTokenText,
                                         Boolean afterNextTokenIsNewLineAfter) {

        String lineStatus;
        if (previousTokenIsNewLineAfter || (previousTokenText.equals(" ") && previousTokenIsNewLineAfter)) {
            lineStatus = LineStatus.LINE_START.toString();
        } else if (nextTokenIsNewLineAfter || (nextTokenText.equals(" ") && afterNextTokenIsNewLineAfter)
                || (text.equals("-") && nextTokenIsNewLineAfter)
                || (text.equals("-") && nextTokenText.equals(" ") && afterNextTokenIsNewLineAfter)) {
            lineStatus = LineStatus.LINE_END.toString();
        } else {
            lineStatus = LineStatus.LINE_IN.toString();
        }


        return lineStatus;

    }

}
