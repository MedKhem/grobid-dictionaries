package org.grobid.core.features;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.features.enums.CapitalisationType;
import org.grobid.core.features.enums.FontStatus;
import org.grobid.core.features.enums.LineStatus;
import org.grobid.core.features.enums.PonctuationType;
import org.grobid.core.layout.LayoutToken;

/**
 * Created by med on 20.10.16.
 *
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


    public static String[] checkLineStatus(LayoutToken token, String previousTokenLineStatus, String lineStatus) {

        if (previousTokenLineStatus == null) {
            lineStatus = LineStatus.LINE_START.toString();

        } else if (token.isNewLineAfter()) {
            // The second case, when the token is he last one, is handled outside this method
            lineStatus = LineStatus.LINE_END.toString();

        } else if (StringUtils.equals(previousTokenLineStatus, LineStatus.LINE_END.toString())) {
            lineStatus = LineStatus.LINE_START.toString();

        } else if (StringUtils.equals(previousTokenLineStatus, LineStatus.LINE_START.toString()) || StringUtils.equals(previousTokenLineStatus, LineStatus.LINE_IN.toString())) {
            lineStatus = LineStatus.LINE_IN.toString();

        }
        previousTokenLineStatus = lineStatus;
        return new String[]{previousTokenLineStatus, lineStatus};
    }


}
