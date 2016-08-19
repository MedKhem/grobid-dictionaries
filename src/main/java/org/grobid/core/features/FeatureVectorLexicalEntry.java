package org.grobid.core.features;

import org.grobid.core.features.enums.CapitalisationType;
import org.grobid.core.features.enums.PonctuationType;
import org.grobid.core.layout.LayoutToken;

/**
 * Created by med on 19.07.16.
 */
public class FeatureVectorLexicalEntry {


    public String string = null; // first lexical feature
    //  public String secondString = null; // second lexical feature
    public String label = null; // label if known
    // public String blockStatus = null; // one of BLOCKSTART, BLOCKIN, BLOCKEND
    public String lineStatus = null; // one of LINESTART, LINEIN, LINEEND

    public String fontStatus = null; // one of NEWFONT, SAMEFONT
    public String fontSize = null; // one of HIGHERFONT, SAMEFONTSIZE, LOWERFONT
    //  public String pageStatus = null; // one of PAGESTART, PAGEIN, PAGEEND
    public boolean bold = false;
    public boolean italic = false;
    public String capitalisation = null; // one of INITCAP, ALLCAPS, NOCAPS


    public String punctType = null;
    //   public String digit;  // one of ALLDIGIT, CONTAINDIGIT, NODIGIT
    //   public boolean singleChar = false;


    //public boolean acronym = false;
 /*   public String punctType = null; // one of NOPUNCT, OPENBRACKET, ENDBRACKET, DOT, COMMA, HYPHEN, QUOTE, PUNCT (default)
    public int relativeDocumentPosition = -1;
    public int relativePagePosition = -1;
    public int relativePagePositionChar = -1; // not used
    public String punctuationProfile = null; // the punctuations of the current line of the token
    public boolean firstPageBlock = false;
    public boolean lastPageBlock = false;
    public int lineLength = 0;

    public boolean repetitivePattern = false; // if true, the textual pattern is repeated at the same position on other pages
    public boolean firstRepetitivePattern = false; // if true, this is a repetitive textual pattern and this is its first occurrence in the doc

    public int spacingWithPreviousBlock = 0; // discretized
    public int wordDensity = 0; // discretized */


    public FeatureVectorLexicalEntry() {
    }

    static public FeatureVectorLexicalEntry addFeaturesLexicalEntries(LayoutToken layoutToken,
                                                                      String label, String lineStatus, String fontStatus) {

        FeatureFactory featureFactory = FeatureFactory.getInstance();
        String word = layoutToken.getText();


        FeatureVectorLexicalEntry featuresVector = new FeatureVectorLexicalEntry();
        featuresVector.string = layoutToken.getText();
        featuresVector.label = label;

        //get features from layout tokens

        featuresVector.fontSize = String.valueOf(layoutToken.getFontSize());
        featuresVector.bold = layoutToken.getBold();
        featuresVector.italic = layoutToken.getItalic();

        //calculate features that are not in the layoutToken
        //Capitalisation
        if (featureFactory.test_all_capital(word)) {
            featuresVector.capitalisation = CapitalisationType.ALLCAPS.toString();
        } else if (featureFactory.test_first_capital(word)) {
            featuresVector.capitalisation = CapitalisationType.INITCAP.toString();
        } else {
            featuresVector.capitalisation = CapitalisationType.NOCAPS.toString();
        }

        //Ponctuation
        if (word.equals("(") || word.equals("[") || word.equals("{")) {
            featuresVector.punctType = PonctuationType.OPENBRACKET.toString();
        } else if (word.equals(")") || word.equals("]") || word.equals("}")) {
            featuresVector.punctType = PonctuationType.ENDBRACKET.toString();
        } else if (word.equals(".") || word.equals("·")) {
            featuresVector.punctType = PonctuationType.DOT.toString();
        } else if (word.equals(",")) {
            featuresVector.punctType = PonctuationType.COMMA.toString();
        } else if (word.equals("-")) {
            featuresVector.punctType = PonctuationType.HYPHEN.toString();
        } else if (word.equals("\"") || word.equals("\'") || word.equals("`")) {
            featuresVector.punctType = PonctuationType.QUOTE.toString();
        } else if (word.equals("/")) {
            featuresVector.punctType = PonctuationType.SLASH.toString();
        } else if (word.equals("^")) {
            featuresVector.punctType = PonctuationType.EXPONENT.toString();
        } else {
            featuresVector.punctType = PonctuationType.NOPUNCT.toString();
        }

        //Get line and font status as parameters from the line level (upper level class)
        featuresVector.lineStatus = lineStatus;
        featuresVector.fontStatus = fontStatus;

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
        res.append(" " + string.substring(0, 1));

        if (string.length() > 1)
            res.append(" " + string.substring(0, 2));
        else
            res.append(" " + string.substring(0, 1));

        if (string.length() > 2)
            res.append(" " + string.substring(0, 3));
        else if (string.length() > 1)
            res.append(" " + string.substring(0, 2));
        else
            res.append(" " + string.substring(0, 1));

        if (string.length() > 3)
            res.append(" " + string.substring(0, 4));
        else if (string.length() > 2)
            res.append(" " + string.substring(0, 3));
        else if (string.length() > 1)
            res.append(" " + string.substring(0, 2));
        else
            res.append(" " + string.substring(0, 1));

        // suffix (4)
        res.append(" " + string.charAt(string.length() - 1));

        if (string.length() > 1)
            res.append(" " + string.substring(string.length() - 2, string.length()));
        else
            res.append(" " + string.charAt(string.length() - 1));

        if (string.length() > 2)
            res.append(" " + string.substring(string.length() - 3, string.length()));
        else if (string.length() > 1)
            res.append(" " + string.substring(string.length() - 2, string.length()));
        else
            res.append(" " + string.charAt(string.length() - 1));

        if (string.length() > 3)
            res.append(" " + string.substring(string.length() - 4, string.length()));
        else if (string.length() > 2)
            res.append(" " + string.substring(string.length() - 3, string.length()));
        else if (string.length() > 1)
            res.append(" " + string.substring(string.length() - 2, string.length()));
        else
            res.append(" " + string.charAt(string.length() - 1));


        res.append(" ").append(fontSize);
        res.append(" ").append(bold);
        res.append(" ").append(italic);
        res.append(" ").append(capitalisation);
        res.append(" ").append(punctType);

        res.append(" ").append(lineStatus);
        res.append(" ").append(fontStatus);
        res.append(" ").append(label);


        return res.toString();
    }

}
