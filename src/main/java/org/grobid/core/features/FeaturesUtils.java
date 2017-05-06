package org.grobid.core.features;

import org.grobid.core.features.enums.CapitalisationType;
import org.grobid.core.features.enums.FontStatus;
import org.grobid.core.features.enums.LineStatus;
import org.grobid.core.features.enums.PonctuationType;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by med on 20.10.16.
 * <p>
 * Class contains elementary steps for producing features from a content of a document (tokens, text..)
 */


public class FeaturesUtils {
    static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder(); // or "ISO-8859-1" for ISO Latin 1

    public FeaturesUtils() {

    }

    public static String[] checkFontStatus(String currentFont, String previousFont) {
        String fontStatus;

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

    public static String checkPunctuationType(String token) {
        //Punctuation: Only the categorization of different value of a ponctuation type matters to be captured for the features
        String punctuationType;

        Pattern punctuationPattern = Pattern.compile("\\p{Punct}");
        Matcher mP = punctuationPattern.matcher(token);
        boolean isPonctuationCharacter = mP.matches();


        if (isPonctuationCharacter) {
            Pattern openBracketPattern = Pattern.compile("[\\[\\(\\{]");
            Matcher mOB = openBracketPattern.matcher(token);
            boolean isOpenBracket = mOB.matches();
            Pattern closeBracketPattern = Pattern.compile("[\\]\\)\\}]");
            Matcher mCB = closeBracketPattern.matcher(token);
            boolean isCloseBracket = mCB.matches();
            if (isOpenBracket) {
                punctuationType = PonctuationType.OPENBRACKET.toString();
            } else if (isCloseBracket) {
                punctuationType = PonctuationType.ENDBRACKET.toString();
            } else {
                punctuationType = PonctuationType.PUNCT.toString();
            }
        } else {
            punctuationType = PonctuationType.NOPUNCT.toString();
        }

        return punctuationType;
    }

    public static CapitalisationType computeCapitalisation(String word) {
        FeatureFactory featureFactory = FeatureFactory.getInstance();
        if (featureFactory.test_all_capital(word)) {
            return CapitalisationType.ALLCAPS;
        } else if (featureFactory.test_first_capital(word)) {
            return CapitalisationType.INITCAP;
        } else {
            return CapitalisationType.NOCAPS;
        }
    }

    public static boolean containsSpecialCharacter(String s) {
        return (s == null) ? false : s.matches("[^A-Za-z0-9 ]");
    }

    public static boolean isPureAscii(String v) {
        return asciiEncoder.canEncode(v);
    }


}
