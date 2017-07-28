package org.grobid.core.features;

import org.grobid.core.document.DictionaryDocument;
import org.grobid.core.engines.DictionarySegmentationParser;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.features.enums.CapitalisationType;
import org.grobid.core.features.enums.LineStatus;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.utilities.TextUtilities;

import java.io.File;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.grobid.core.engines.label.DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL;

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
    public String parentTag = null; //element on which this lexical entry is contained (entry, related entry, etc)

    public FeatureVectorLexicalEntry() {
    }

    public static FeatureVectorLexicalEntry addFeaturesLexicalEntries(LayoutToken layoutToken,
                                                                      String label, String lineStatus,
                                                                      String fontStatus) {
        return addFeaturesLexicalEntries(layoutToken, label, lineStatus, fontStatus, DICTIONARY_ENTRY_LABEL);
    }

    public static FeatureVectorLexicalEntry addFeaturesLexicalEntries(LayoutToken layoutToken,
                                                                      String label, String lineStatus,
                                                                      String fontStatus, String parentTag) {

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

        featuresVector.punctType = FeaturesUtils.checkPunctuationType(word);

        //Get line and font status as parameters from the line level (upper level class)
        featuresVector.lineStatus = lineStatus;
        featuresVector.fontStatus = fontStatus;
        featuresVector.parentTag = parentTag;

        return featuresVector;
    }


    // This is a key method. It is required by the dictionary parser (by the process() method)
    public static StringBuilder createFeaturesFromLayoutTokens(List<LayoutToken> tokens) {
        return createFeaturesFromLayoutTokens(tokens, DICTIONARY_ENTRY_LABEL);
    }

    public static StringBuilder createFeaturesFromLayoutTokens(List<LayoutToken> tokens, String parentTag) {

        StringBuilder stringBuilder = new StringBuilder();

        String previousFont = null;
        String fontStatus = null;

        String lineStatus = null;
        int nbToken = tokens.size();
        int counter = 0;

        for (LayoutToken layoutToken : tokens) {
            // Feature Vector won't contain the space between tokens neither the different line breaks, although they are considered as a separate layoutToken
            String text = layoutToken.getText();
            text = text.replace(" ", "");

            if (TextUtilities.filterLine(text) || isBlank(text)) {
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
                previousTokenText = tokens.get(counter - 1).getText();
                previousTokenIsNewLineAfter = tokens.get(counter - 1).isNewLineAfter();
                nextTokenText = tokens.get(counter + 1).getText();
                nextTokenIsNewLineAfter = tokens.get(counter + 1).isNewLineAfter();

                // Check the existence of the afterNextToken
                if ((nbToken > counter + 2) && (tokens.get(counter + 2) != null)) {
                    afterNextTokenIsNewLineAfter = tokens.get(counter + 2).isNewLineAfter();
                }

                lineStatus = FeaturesUtils.checkLineStatus(text, previousTokenIsNewLineAfter, previousTokenText, nextTokenIsNewLineAfter, nextTokenText, afterNextTokenIsNewLineAfter);

            }
            counter++;

            String[] returnedFont = FeaturesUtils.checkFontStatus(layoutToken.getFont(), previousFont);
            previousFont = returnedFont[0];
            fontStatus = returnedFont[1];
            FeatureVectorLexicalEntry vector = FeatureVectorLexicalEntry.addFeaturesLexicalEntries(layoutToken, "", lineStatus, fontStatus, parentTag);
            String featureVector = vector.printVector();
            stringBuilder.append(featureVector + "\n");

        }

        return stringBuilder;
    }

    // This is a key method. It is required by the dictionary trainer (to generate the training data from a PDF file)
    // similar to createFeaturesFromLayoutTokens() but more independent from the parser
    public static StringBuilder createFeaturesFromPDF(File inputFile) {

        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        DictionarySegmentationParser parser = new DictionarySegmentationParser();
        DictionaryDocument doc = parser.initiateProcessing(inputFile, config);

        LayoutTokenization tokens = new LayoutTokenization(doc.getTokenizations());
        StringBuilder stringBuilder = createFeaturesFromLayoutTokens(tokens.getTokenization());

        return stringBuilder;
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
        res.append(" ").append(label);
        // parentTag is not yet used

        return res.toString();
    }
}
