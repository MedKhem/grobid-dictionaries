package org.grobid.core.features;

import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.document.DocumentUtils;
import org.grobid.core.engines.EngineParsers;
import org.grobid.core.engines.SegmentationLabel;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.features.enums.CapitalisationType;
import org.grobid.core.features.enums.LineStatus;
import org.grobid.core.features.enums.PonctuationType;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.utilities.TextUtilities;

import java.io.File;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public FeatureVectorLexicalEntry() {
    }

    public static FeatureVectorLexicalEntry addFeaturesLexicalEntries(LayoutToken layoutToken,
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

        featuresVector.punctType = checkPonctuationType(word);


        //Get line and font status as parameters from the line level (upper level class)
        featuresVector.lineStatus = lineStatus;
        featuresVector.fontStatus = fontStatus;

        return featuresVector;
    }


    // This is a key method. It is required by the dictionary parser (by the process() method)
    public static StringBuilder createFeaturesFromLayoutTokens(LayoutTokenization tokens) {

        StringBuilder stringBuilder = new StringBuilder();

        String previousFont = null;
        String fontStatus = null;

        String lineStatus = null;
        int nbToken = tokens.getTokenization().size();
        int counter = 0;


        for (LayoutToken layoutToken : tokens.getTokenization()) {
            // Feature Vector won't contain the space between tokens neither the different line breaks, although they are considered as a separate layoutToken
            String text = layoutToken.getText();
            text = text.replace(" ", "");

            if (TextUtilities.filterLine(text) || (text == null) || (text.length() == 0)) {
                counter++;
                continue;
            }
            if (text.equals("\n") || text.equals("\r") || (text.equals("\n\r"))) {
                counter++;
                continue;
            }

            // First token
            if (counter - 1 < 0) {
                lineStatus = LineStatus.LINE_START.toString();
            } else if (counter + 1 == nbToken) {
                // Last token
                lineStatus = LineStatus.LINE_END.toString();

            } else {
                String previousTokenText;
                Boolean previousTokenIsNewLineAfter;
                String nextTokenText;
                Boolean nextTokenIsNewLineAfter;
                Boolean afterNextTokenIsNewLineAfter = false;

                //The existence of the previousToken and nextToken is already check.
                previousTokenText = tokens.getTokenization().get(counter - 1).getText();
                previousTokenIsNewLineAfter = tokens.getTokenization().get(counter - 1).isNewLineAfter();
                nextTokenText = tokens.getTokenization().get(counter + 1).getText();
                nextTokenIsNewLineAfter = tokens.getTokenization().get(counter + 1).isNewLineAfter();

                // Check the existence of the afterNextToken
                if (tokens.getTokenization().get(counter + 2) != null) {
                    afterNextTokenIsNewLineAfter = tokens.getTokenization().get(counter + 2).isNewLineAfter();
                }

                lineStatus = FeaturesUtils.checkLineStatus(text, previousTokenIsNewLineAfter, previousTokenText, nextTokenIsNewLineAfter, nextTokenText, afterNextTokenIsNewLineAfter);


            }
            counter++;

            String[] returnedFont = FeaturesUtils.checkFontStatus(layoutToken.getFont(), previousFont, fontStatus);
            previousFont = returnedFont[0];
            fontStatus = returnedFont[1];
            FeatureVectorLexicalEntry vector = FeatureVectorLexicalEntry.addFeaturesLexicalEntries(layoutToken, "", lineStatus, fontStatus);
            String featureVector = vector.printVector();
            stringBuilder.append(featureVector + "\n");

        }

        return stringBuilder;
    }

    // This is a key method. It is required by the dictionary trainer (to generate the training data from a PDF file)
    // similar to createFeaturesFromLayoutTokens() but more independent from the parser
    public static StringBuilder createFeaturesFromPDF(File inputFile) {

        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        DocumentSource documentSource = DocumentSource.fromPdf(inputFile, config.getStartPage(), config.getEndPage(), config.getPdfAssetPath() != null);
        Document doc = new EngineParsers().getSegmentationParser().processing(documentSource, config);

        SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentPart(SegmentationLabel.BODY);

        LayoutTokenization tokens = DocumentUtils.getLayoutTokenizations(doc, documentBodyParts);

        StringBuilder stringBuilder = createFeaturesFromLayoutTokens(tokens);

        return stringBuilder;
    }

    public static String checkPonctuationType(String token) {
        //Ponctuation: Only the categorization of different value of a ponctuation type matters to be captured for the features
        String ponctuationType;

        Pattern ponctuationPattern = Pattern.compile("\\p{Punct}");
        Matcher mP = ponctuationPattern.matcher(token);
        boolean isPonctuationCharacter = mP.matches();


        if (isPonctuationCharacter) {
            Pattern openBracketPattern = Pattern.compile("[\\[\\(\\{]");
            Matcher mOB = openBracketPattern.matcher(token);
            boolean isOpenBracket = mOB.matches();
            Pattern closeBracketPattern = Pattern.compile("[\\]\\)\\}]");
            Matcher mCB = closeBracketPattern.matcher(token);
            boolean isCloseBracket = mCB.matches();
            if (isOpenBracket) {
                ponctuationType = PonctuationType.OPENBRACKET.toString();
            } else if (isCloseBracket) {
                ponctuationType = PonctuationType.ENDBRACKET.toString();
            } else {
                ponctuationType = PonctuationType.PUNCT.toString();
            }
        } else {
            ponctuationType = PonctuationType.NOPUNCT.toString();
        }

        return ponctuationType;
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
