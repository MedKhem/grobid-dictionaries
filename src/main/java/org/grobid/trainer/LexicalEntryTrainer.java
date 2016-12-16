package org.grobid.trainer;

import org.apache.commons.io.IOUtils;
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
                BufferedReader bis = new BufferedReader(
                        new InputStreamReader(new FileInputStream(sourceLexicalEntriesPathFeatures + File.separator +
                                                                          name.replace(".tei.xml", "")), "UTF8"));
                int q = 0;
                StringBuilder trainingDataLineBuilder = new StringBuilder();


                String line;
                while ((line = bis.readLine()) != null) {
                    int ii = line.indexOf(' ');
                    String token = null;
                    if (ii != -1)
                        token = line.substring(0, ii);
                    // we get the label in the labelled data file for the same token
                    for (int pp = q; pp < labeled.size(); pp++) {
                        String localLine = labeled.get(pp);
                        StringTokenizer st = new StringTokenizer(localLine, " ");
                        if (st.hasMoreTokens()) {
                            String localToken = st.nextToken();
                            if (localToken.equals(token)) {
                                String tag = st.nextToken();
                                trainingDataLineBuilder.append(line).append(" ").append(tag);
                                q = pp + 1;
                                pp = q + 10;
                            }
                        }
                        if (pp - q > 5) {
                            break;
                        }
                    }
                }
                bis.close();
                writer2.write(trainingDataLineBuilder.toString() + "");
            }

        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        } finally {
            IOUtils.closeQuietly(writer2);
            IOUtils.closeQuietly(os2);
        }
        return totalExamples;
    }


}
