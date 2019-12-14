package org.grobid.trainer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.grobid.core.data.SimpleLabeled;
import org.grobid.core.engines.DictionaryModels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.utilities.GrobidDictionaryProperties;
import org.grobid.trainer.sax.TEICrossRefSaxParser;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.List;

/**
 * Created by Med on 26.04.19.
 */
public class CrossRefTrainer extends AbstractTrainer {

    public CrossRefTrainer() {
        super(DictionaryModels.CROSS_REF);
    }

    public static void main(String[] args) throws Exception {
        GrobidDictionaryProperties.getInstance();
        Trainer trainer = new CrossRefTrainer();
        AbstractTrainer.runTraining( trainer);

        System.out.println( AbstractTrainer.runEvaluation( trainer, false));
    }

    @Override
    public int createCRFPPData(File corpusPath, File outputFile) {
        return addFeaturesSense(corpusPath.getAbsolutePath() + "/tei", corpusPath + "/raw", outputFile);
    }

    @Override
    public int createCRFPPData(final File corpusDir,
                               final File trainingOutputPath,
                               final File evalOutputPath,
                               double splitRatio) {
        return 0;
    }

    public int addFeaturesSense(String sourcePathLabels,
                                String sourceFeatures,
                                File outputPath) {
        int totalExamples = 0;
        OutputStream os2 = null;
        Writer writer2 = null;

        try {
            System.out.println("source labels: " + sourcePathLabels);
            System.out.println("source features: " + sourceFeatures);
            System.out.println("outputPath: " + outputPath);

            // we need first to generate the labeled files from the TEI annotated files
            File input = new File(sourcePathLabels);
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

            SAXParserFactory spf = SAXParserFactory.newInstance();

            for (File tf : refFiles) {
                String name = tf.getName();
                System.out.println("Processing: " + name);

                TEICrossRefSaxParser parser2 = new TEICrossRefSaxParser();

                //get a new instance of parser
                SAXParser p = spf.newSAXParser();
                p.parse(tf, parser2);

                List<SimpleLabeled> labeled = parser2.getLabeledResult();

                // we open the featured file
                BufferedReader featuresFile = new BufferedReader(
                        new InputStreamReader(new FileInputStream(sourceFeatures + File.separator +
                                name.replace(".tei.xml", "")), "UTF8"));
                //Index to iterate through a list of LabeledForm
                int indexLabeledList = 0;
                //Index to iterate through the LabeledFormLabels
                int indexLabeledCrossRef = 0;
                StringBuilder trainingDataLineBuilder = new StringBuilder();

                String line;
                while ((line = featuresFile.readLine()) != null) {

                    String tokenFromFeatures = extractToken(line);

                    // we get the label in the labelled data file for the same token
                    outer:
                    for (int pp = indexLabeledList; pp < labeled.size(); pp++) {
                        SimpleLabeled simpleLabeled = labeled.get(pp);

                        for (int qq = indexLabeledCrossRef; qq < simpleLabeled.getLabels().size(); qq++) {

                            final String labelToken = simpleLabeled.getLabels().get(qq).getRight();
                            final String tokenFromLabels = simpleLabeled.getLabels().get(qq).getLeft();
                            if (StringUtils.equals(tokenFromLabels, tokenFromFeatures)) {
                                trainingDataLineBuilder.append(line).append(" ").append(labelToken).append("\n");
                                indexLabeledCrossRef = qq + 1;
                                if (simpleLabeled.getLabels().size() == indexLabeledCrossRef) {
                                    pp++;
                                    indexLabeledCrossRef = 0;
                                    trainingDataLineBuilder.append("\n");
                                }
                                indexLabeledList = pp;

                                break outer;
                            }
                        }
                    }
                }
                IOUtils.closeQuietly(featuresFile);
                writer2.write(trainingDataLineBuilder.toString() + "");
            }

        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        } finally {
            IOUtils.closeQuietly(writer2, os2);
        }
        return totalExamples;
    }

    private String extractToken(String line) {
        int ii = line.indexOf(' ');
        String token = null;
        if (ii != -1)
            token = line.substring(0, ii);
        return token;
    }


}
