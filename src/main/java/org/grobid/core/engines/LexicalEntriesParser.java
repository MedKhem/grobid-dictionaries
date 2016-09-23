package org.grobid.core.engines;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.util.IOUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeatureVectorLexicalEntry;
import org.grobid.core.features.enums.FontStatus;
import org.grobid.core.features.enums.LineStatus;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.layout.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

/**
 * Created by med on 02.08.16.
 */
public class LexicalEntriesParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(LexicalEntriesParser.class);
    private static volatile LexicalEntriesParser instance;
    private String lexEntries;

    //Might be needed to have several LEXICALENTRIES_XYZ models, based on the function,
    // depending how many sub models will be created.
    public LexicalEntriesParser() {
        super(GrobidModels.DICTIONARIES_LEXICAL_ENTRIES);
    }

    public static LexicalEntriesParser getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    /**
     * Create a new instance.
     */
    private static synchronized void getNewInstance() {
        instance = new LexicalEntriesParser();
    }

    public String process(File originFile) {
        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().generateTeiIds(true).build();


        // Segment the document to identify the document's block
        DocumentSource documentSource = DocumentSource.fromPdf(originFile, config.getStartPage(), config.getEndPage(), config.getPdfAssetPath() != null);
        Document doc = new EngineParsers().getSegmentationParser().processing(documentSource, config);

        //only body please :)
        SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentPart(SegmentationLabel.BODY);

        LayoutTokenization tokens = getLayoutTokenizations(doc, documentBodyParts);

        String text = tokens.getTokenization().stream().map(LayoutToken::getText).collect(Collectors.joining());


        return text;
    }

    LayoutTokenization getLayoutTokenizations(Document doc, SortedSet<DocumentPiece> documentBodyParts) {


        List<LayoutToken> layoutTokens = new ArrayList<>();

        for (Page page : doc.getPages()) {
            //get the blocks
            if ((page.getBlocks() == null) || (page.getBlocks().size() == 0))
                continue;

            for (int blockIndex = 0; blockIndex < page.getBlocks().size(); blockIndex++) {
                Block block = page.getBlocks().get(blockIndex);

                String localText = block.getText();
                List<LayoutToken> tokens = block.getTokens();

                if (localText != null) {
                    if (tokens != null) {

                        String[] lines = localText.split("[\\n\\r]");

//                        //For each line of the block
//                        for (int li = 0; li < lines.length; li++) {
//                            String line = lines[li].trim();
//

                        // For each token of the block
                        for (int k = 0; k < tokens.size(); k++) {
                            //catch the line status here: now it's based on the position of the token in the block. Should be modified for the position in the line
                            LayoutToken token = null;

                            token = tokens.get(k);
                            if ((token.getText() == null) ||
                                    (token.getText().trim().length() == 0) ||
                                    (token.getText().trim().equals("\n")) ||
                                    (token.getText().trim().equals("\r")) ||
                                    (token.getText().trim().equals("\n\r")))
                                continue;
                            else if (token.isNewLineAfter() || (block.getEndToken() == k)) {
                                token.setNewLineAfter(true);
                            }

                            layoutTokens.add(token);


                        }

                    }


                }
            }
        }

        return new LayoutTokenization(layoutTokens);
    }


    public StringBuilder createTrainingFeatureData(File inputFile) {


        DocumentSource documentSource = null;
        StringBuilder stringBuilder = new StringBuilder();

        documentSource = DocumentSource.fromPdf(inputFile);
        Document doc;
        try {
            doc = new Document(documentSource);
            doc.addTokenizedDocument(GrobidAnalysisConfig.defaultInstance());

        } finally {
            if (GrobidAnalysisConfig.defaultInstance().getPdfAssetPath() == null) {
                DocumentSource.close(documentSource, false);
            } else {
                DocumentSource.close(documentSource, true);
            }
        }
        SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentPart(SegmentationLabel.BODY);
        LayoutTokenization tokens = getLayoutTokenizations(doc, documentBodyParts);

        String previousFont = null;
        String fontStatus = null;
        String previousTokenLineStatus = null;
        String lineStatus = null;
        int nbToken = tokens.getTokenization().size();
        int counter = 1;


        for (LayoutToken layoutToken : tokens.getTokenization()) {

            if (counter != nbToken) {

                String[] returnedStatus = checkLineStatus(layoutToken, previousTokenLineStatus, lineStatus);
                previousTokenLineStatus = returnedStatus[0];
                lineStatus = returnedStatus[1];

                counter++;
            } else {
                // The last token

                lineStatus = LineStatus.LINE_END.toString();
            }

            String[] returnedFont = checkFontStatus(layoutToken.getFont(), previousFont, fontStatus);
            previousFont = returnedFont[0];
            fontStatus = returnedFont[1];
            FeatureVectorLexicalEntry vector = FeatureVectorLexicalEntry.addFeaturesLexicalEntries(layoutToken, "", lineStatus, fontStatus);
            stringBuilder.append(vector.printVector()).append("\n");

        }

        return stringBuilder;
    }

    public String[] checkFontStatus(String currentFont, String previousFont, String fontStatus) {

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


    public String[] checkLineStatus(LayoutToken token, String previousTokenLineStatus, String lineStatus) {

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

    @SuppressWarnings({"UnusedParameters"})
    public int createTrainingBatch(String inputDirectory, String outputDirectory) throws IOException {
        try {
            File path = new File(inputDirectory);
            if (!path.exists()) {
                throw new GrobidException("Cannot create training data because input directory can not be accessed: " + inputDirectory);
            }

            File pathOut = new File(outputDirectory);
            if (!pathOut.exists()) {
                throw new GrobidException("Cannot create training data because ouput directory can not be accessed: " + outputDirectory);
            }

            // we process all pdf files in the directory

            int n = 0;

            if (path.isDirectory()) {
                for (File fileEntry : path.listFiles()) {
                    String featuresFile = outputDirectory + "/" + fileEntry.getName().substring(0, fileEntry.getName().length() - 4) + ".training.lexicalEntries";
                    Writer writer = new OutputStreamWriter(new FileOutputStream(new File(featuresFile), false), "UTF-8");
                    writer.write(createTrainingFeatureData(fileEntry) + "\n");
                    IOUtils.closeWhileHandlingException(writer);
                    n++;
                }

            } else {
                String featuresFile = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.lexicalEntries";
                Writer writer = new OutputStreamWriter(new FileOutputStream(new File(featuresFile), false), "UTF-8");
                writer.write(createTrainingFeatureData(path).toString());
                IOUtils.closeWhileHandlingException(writer);
                n++;
            }

            System.out.println(n + " files to be processed.");

            return n;
        } catch (final Exception exp) {
            throw new GrobidException("An exception occurred while running Grobid batch.", exp);
        }
    }


}
