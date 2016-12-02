package org.grobid.trainer;

import org.grobid.core.GrobidModels;
import org.grobid.core.engines.DictionaryModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.trainer.sax.TEILexicalEntrySaxParser;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by med on 19.08.16.
 */
public class LexicalEntryTrainer extends AbstractTrainer {

    public LexicalEntryTrainer() {
        super(DictionaryModels.LEXICAL_ENTRY);
    }

    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        MockContext.setInitialContext();
        GrobidProperties.getInstance();
        AbstractTrainer.runTraining(new LexicalEntryTrainer());
        AbstractTrainer.runEvaluation(new LexicalEntryTrainer());
        MockContext.destroyInitialContext();
    }

    @Override
    public int createCRFPPData(File corpusPath, File outputFile) {
        return addFeaturesLexicalEntries(corpusPath.getAbsolutePath() + "/tei", corpusPath + "/raw", outputFile);
    }

    /**
     * Add the selected features to a full text example set
     *
     * @param corpusDir          a path where corpus files are located
     * @param trainingOutputPath path where to store the temporary training data
     * @param evalOutputPath     path where to store the temporary evaluation data
     * @param splitRatio         ratio to consider for separating training and evaluation data, e.g. 0.8 for 80%
     * @return the total number of used corpus items
     */
    @Override
    public int createCRFPPData(final File corpusDir,
                               final File trainingOutputPath,
                               final File evalOutputPath,
                               double splitRatio) {
        return 0;
    }

    /**
     * Add the selected features to the lexical entry segmentaion model
     *
     * @param sourceTEIPathLabel               path to TEI files
     * @param sourceLexicalEntriesPathFeatures path to fulltexts
     * @param outputPath                       output train file
     * @return number of examples
     */
    public int addFeaturesLexicalEntries(String sourceTEIPathLabel,
                                         String sourceLexicalEntriesPathFeatures,
                                         File outputPath) {
        int totalExamples = 0;
        OutputStream os2 = null;
        Writer writer2 = null;

        try {
            System.out.println("sourceTEIPathLabel: " + sourceTEIPathLabel);
            System.out.println("sourceLexicalEntriesPathFeatures: " + sourceLexicalEntriesPathFeatures);
            System.out.println("outputPath: " + outputPath);

            // we need first to generate the labeled files from the TEI annotated files
            File input = new File(sourceTEIPathLabel);
            // we process all tei files in the output directory
            File[] refFiles = input.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".tei.xml");
                }
            });

            if (refFiles == null) {
                return 0;
            }

            System.out.println(refFiles.length + " tei files");

            // the file for writing the training data
            os2 = new FileOutputStream(outputPath);
            writer2 = new OutputStreamWriter(os2, "UTF8");

            // get a factory for SAX parser
            SAXParserFactory spf = SAXParserFactory.newInstance();

//            int n = 0;
            for (File tf : refFiles) {
                String name = tf.getName();
                System.out.println(name);

                TEILexicalEntrySaxParser parser2 = new TEILexicalEntrySaxParser();
                //parser2.setMode(TEILexicalEntrySaxParser.FULLTEXT);

                //get a new instance of parser
                SAXParser p = spf.newSAXParser();
                p.parse(tf, parser2);

                List<String> labeled = parser2.getLabeledResult();

                // we can now add the features
                // we open the featured file
                BufferedReader featuresFileBR = new BufferedReader(
                        new InputStreamReader(new FileInputStream(sourceLexicalEntriesPathFeatures + File.separator +
                                                                          name.replace(".tei.xml", "")), "UTF8"));

                StringBuilder trainingDataLineBuilder = new StringBuilder();

                int counterStart = 0;
                String line;
                while ((line = featuresFileBR.readLine()) != null) {
                    String token = getFirstToken(line);
                    String label = getLabelByToken(token, counterStart, labeled);
                    trainingDataLineBuilder.append(line).append(" ").append(label);
                    counterStart++;
                }
                featuresFileBR.close();
                // Add the training data with suffixed label
                writer2.write(trainingDataLineBuilder.toString() + "\n");
            }

        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        } finally {
            try {
                if (writer2 != null) {
                    writer2.close();
                }

                if (os2 != null) {
                    os2.close();
                }

            } catch (Exception ex) {
                throw new GrobidException("An exception occurred while closing file", ex);
            }

        }
        return totalExamples;
    }

    private String getFirstToken(String line) {
        int ii = line.indexOf(' ');
        String token = null;

        if (ii != -1) {
            token = line.substring(0, ii);
        }

        return token;
    }

    /**
     * Searching for the label in the labelled data file of the token in the feature file
     */
    protected String getLabelByToken(String featureFileToken, int counterStart, List<String> labeled) {

        for (int indexLabeled = counterStart; indexLabeled < labeled.size(); indexLabeled++) {
            String tokenPlusLabel = labeled.get(indexLabeled);
            StringTokenizer st = new StringTokenizer(tokenPlusLabel, " ");
            if (st.hasMoreTokens()) {
                String labelFileToken = st.nextToken();

                if(featureFileToken.equals("@BULLET")){
                    String tag = st.nextToken();
                    return tag;
                } else if (labelFileToken.equals(featureFileToken)) {
                    String tag = st.nextToken();
                    return tag;
                }
            }
            if (indexLabeled - counterStart > 5) {
                return null;
            }
        }

        return null;
    }
}
