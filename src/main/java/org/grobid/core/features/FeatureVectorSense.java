package org.grobid.core.features;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.TextUtilities;

/**
 * Created by med on 19.07.16.
 */
public class FeatureVectorSense {

    public String string = null; // first lexical feature
    public String label = null; // label if known
    public boolean containsSpecialCharacters = false; //contains special characters
    public boolean isPureAscii = false;

    public String lineStatus = null; // one of LINESTART, LINEIN, LINEEND
    public String fontStatus = null; // one of NEWFONT, SAMEFONT
    public String fontSize = null; // one of HIGHERFONT, SAMEFONTSIZE, LOWERFONT
    public boolean bold = false;
    public boolean italic = false;
    public String capitalisation = null; // one of INITCAP, ALLCAPS, NOCAPS
    public String punctType = null;

    public FeatureVectorSense() {
    }

    public static FeatureVectorSense addFeaturesSense(LayoutToken layoutToken, String label,
                                                      String lineStatus, String fontStatus) {

        String word = layoutToken.getText();

        FeatureVectorSense featuresVector = new FeatureVectorSense();
        featuresVector.string = layoutToken.getText();
        featuresVector.label = StringUtils.isBlank(label) ? "" : label;

        //1. get features from layout tokens
        featuresVector.fontSize = String.valueOf(layoutToken.getFontSize());
        featuresVector.bold = layoutToken.getBold();
        featuresVector.italic = layoutToken.getItalic();

        //2. calculate features that are not in the layoutToken
        featuresVector.capitalisation = FeaturesUtils.computeCapitalisation(word).toString();
        featuresVector.punctType = FeaturesUtils.checkPunctuationType(word);

        //Get line and font status as parameters from the line level (upper level class)
        featuresVector.lineStatus = lineStatus;
        featuresVector.fontStatus = fontStatus;

        featuresVector.containsSpecialCharacters = FeaturesUtils.containsSpecialCharacter(word);
        featuresVector.isPureAscii = FeaturesUtils.isPureAscii(word);

        return featuresVector;
    }


    public String printVector() {
        if (string == null) return null;
        if (string.length() == 0) return null;
        StringBuffer res = new StringBuffer();

        // token string (1)
        res.append(string);

        // lowercase string
        res.append(" " + string.toLowerCase());

        // prefix (4)
        res.append(" ").append(TextUtilities.prefix(string, 1));
        res.append(" ").append(TextUtilities.prefix(string, 2));
        res.append(" ").append(TextUtilities.prefix(string, 3));
        res.append(" ").append(TextUtilities.prefix(string, 4));

        // suffix (4)
        res.append(" ").append(TextUtilities.suffix(string, 1));
        res.append(" ").append(TextUtilities.suffix(string, 2));
        res.append(" ").append(TextUtilities.suffix(string, 3));
        res.append(" ").append(TextUtilities.suffix(string, 4));

        res.append(" ").append(fontSize);
        res.append(" ").append(bold);
        res.append(" ").append(italic);
        res.append(" ").append(capitalisation);
        res.append(" ").append(punctType);
        res.append(" ").append(lineStatus);
        res.append(" ").append(fontStatus);
        //res.append(" ").append(containsSpecialCharacters);
//        res.append(" ").append(isPureAscii);

        /*if(containsSpecialCharacters)
            res.append(" 1");
        else
            res.append(" 0");*/

        res.append(" ").append(label);

        return res.toString();
    }
}
