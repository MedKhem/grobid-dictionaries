package org.grobid.core.main.batch;

import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.engines.*;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * The entrance point for starting grobid-dictionaries from command line and perform batch initiateProcessing
 * <p>
 * Created by med on 27.07.16.
 */
public class DictionaryMain {

    private static final String CREATE_TRAINING_DICTIONARY_SEGMENTATION = "createTrainingDictionarySegmentation";
    private static final String CREATE_ANNOTATED_TRAINING_DICTIONARY_SEGMENTATION = "createAnnotatedTrainingDictionarySegmentation";
    private static final String CREATE_TRAINING_DICTIONARY_BODY_SEGMENTATION = "createTrainingDictionaryBodySegmentation";
    private static final String CREATE_ANNOTATED_TRAINING_DICTIONARY_BODY_SEGMENTATION = "createAnnotatedTrainingDictionaryBodySegmentation";
    private static final String CREATE_TRAINING_LEXICAL_ENTRY = "createTrainingLexicalEntry";
    private static final String CREATE_ANNOTATED_TRAINING_LEXICAL_ENTRY = "createAnnotatedTrainingLexicalEntry";
    private static final String CREATE_TRAINING_FORM = "createTrainingForm";
    private static final String CREATE_ANNOTATED_TRAINING_FORM = "createAnnotatedTrainingForm";
    private static final String CREATE_TRAINING_SENSE = "createTrainingSense";
    private static final String CREATE_ANNOTATED_TRAINING_SENSE = "createAnnotatedTrainingSense";
    private static final String CREATE_TRAINING_SUB_SENSE = "createTrainingSubSense";
    private static final String CREATE_ANNOTATED_TRAINING_SUB_SENSE = "createAnnotatedTrainingSubSense";
    private static final String CREATE_TRAINING_ETYMQUOTE = "createTrainingEtymQuote";
    private static final String CREATE_ANNOTATED_TRAINING_ETYMQUOTE = "createAnnotatedTrainingEtymQuote";
    private static final String CREATE_TRAINING_ETYM = "createTrainingEtym";
    private static final String CREATE_ANNOTATED_TRAINING_ETYM = "createAnnotatedTrainingEtym";
    private static final String CREATE_TRAINING_CROSSREF = "createTrainingMorphoGram";
    private static final String CREATE_ANNOTATED_TRAINING_CROSSREF = "createAnnotatedTrainingCrossRef";
    private static final String CREATE_TRAINING_SENSE_CROSSREF = "createTrainingSenseCrossRef";
    private static final String CREATE_ANNOTATED_TRAINING_SENSE_CROSSREF = "createAnnotatedTrainingSenseCrossRef";
    private static final String CREATE_TRAINING_FORM_GRAMGRP = "createTrainingFormGramGrp";
    private static final String CREATE_ANNOTATED_TRAINING_FORM_GRAMGRP = "createAnnotatedTrainingFormGramGrp";
    private static final String CREATE_TRAINING_LEXICAL_ENTRY_GRAMGRP = "createTrainingGramGrp";
    private static final String CREATE_ANNOTATED_TRAINING_LEXICAL_ENTRY_GRAMGRP = "createAnnotatedTrainingGramGrp";
    private static final String CREATE_TRAINING_LEXICAL_SENSE_GRAMGRP = "createTrainingSenseGramGrp";
    private static final String CREATE_ANNOTATED_TRAINING_SENSE_GRAMGRP = "createAnnotatedTrainingSenseGramGrp";
    private static final String CREATE_TRAINING_SUB_SENSE_GRAMGRP = "createAnnotatedTrainingSubSenseGramGrp";
    private static final String CREATE_ANNOTATED__TRAINING_SUB_SENSE_GRAMGRP = "createAnnotatedTrainingSubSenseGramGrp";



    private static List<String> availableCommands = Arrays.asList(
            CREATE_TRAINING_DICTIONARY_SEGMENTATION,
            CREATE_ANNOTATED_TRAINING_DICTIONARY_SEGMENTATION,
            CREATE_TRAINING_DICTIONARY_BODY_SEGMENTATION,
            CREATE_ANNOTATED_TRAINING_DICTIONARY_BODY_SEGMENTATION,
            CREATE_TRAINING_LEXICAL_ENTRY,
            CREATE_TRAINING_FORM,
            CREATE_TRAINING_SENSE,
            CREATE_TRAINING_SUB_SENSE,
            CREATE_TRAINING_ETYMQUOTE,
            CREATE_TRAINING_ETYM,
            CREATE_ANNOTATED_TRAINING_LEXICAL_ENTRY,
            CREATE_ANNOTATED_TRAINING_FORM,
            CREATE_ANNOTATED_TRAINING_SENSE,
            CREATE_ANNOTATED_TRAINING_SUB_SENSE,
            CREATE_ANNOTATED_TRAINING_ETYMQUOTE,
            CREATE_ANNOTATED_TRAINING_ETYM,
            CREATE_TRAINING_CROSSREF,
            CREATE_ANNOTATED_TRAINING_CROSSREF,
            CREATE_TRAINING_SENSE_CROSSREF,
            CREATE_ANNOTATED_TRAINING_SENSE_CROSSREF,
            CREATE_TRAINING_FORM_GRAMGRP,
            CREATE_ANNOTATED_TRAINING_FORM_GRAMGRP,
            CREATE_TRAINING_LEXICAL_ENTRY_GRAMGRP,
            CREATE_ANNOTATED_TRAINING_LEXICAL_ENTRY_GRAMGRP,
            CREATE_TRAINING_LEXICAL_SENSE_GRAMGRP,
            CREATE_ANNOTATED_TRAINING_SENSE_GRAMGRP );

    /**
     * Arguments of the batch.
     */
    private static GrobidMainArgs gbdArgs;

    /**
     * Build the path to grobid.properties from the path to grobid-home.
     *
     * @param pPath2GbdHome The path to Grobid home.
     * @return the path to grobid.properties.
     */
    protected final static String getPath2GbdProperties(final String pPath2GbdHome) {
        return pPath2GbdHome + File.separator + "config" + File.separator + "grobid.properties";
    }

    /**
     * Init process with the provided grobid-home
     *
     * @param grobidHome
     */
    protected static void initProcess(String grobidHome) {
        try {
            final GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(Arrays.asList(grobidHome));
            grobidHomeFinder.findGrobidHomeOrFail();
            GrobidProperties.getInstance(grobidHomeFinder);
            LibraryLoader.load();
        } catch (final Exception exp) {
            System.err.println("Grobid initialisation failed: " + exp);
        }
    }

    /**
     * Init process with the default value of the grobid home
     */
    protected static void initProcess() {
        try {
            LibraryLoader.load();
        } catch (final Exception exp) {
            System.err.println("Grobid initialisation failed: " + exp);
        }
        GrobidProperties.getInstance();
    }

    /**
     * @return String to display for help.
     */
    protected static String getHelp() {
        final StringBuffer help = new StringBuffer();
        help.append("HELP GROBID\n");
        help.append("-h: displays help\n");
        help.append("-gH: gives the path to grobid home directory.\n");
        help.append("-dIn: gives the path to the directory where the files to be processed are located, to be used only when the called method needs it.\n");
        help.append("-dOut: gives the path to the directory where the result files will be saved. The default output directory is the curent directory.\n");
        help.append("-s: is the parameter used for process using string as input and not file.\n");
        help.append("-r: recursive directory initiateProcessing, default initiateProcessing is not recursive.\n");
        help.append("-ignoreAssets: do not extract and save the PDF assets (bitmaps, vector graphics), by default the assets are extracted and saved.\n");
        help.append("-exe: gives the command to execute. The value should be one of these:\n");
        help.append("\t" + availableCommands + "\n");
        return help.toString();
    }

    /**
     * Process batch given the args.
     *
     * @param pArgs The arguments given to the batch.
     */
    protected static boolean processArgs(final String[] pArgs) {
        boolean result = true;
        if (pArgs.length == 0) {
            System.out.println(getHelp());
            result = false;
        } else {
            String currArg;
            for (int i = 0; i < pArgs.length; i++) {
                currArg = pArgs[i];
                if (currArg.equals("-h")) {
                    System.out.println(getHelp());
                    result = false;
                    break;
                }
                if (currArg.equals("-gH")) {
                    gbdArgs.setPath2grobidHome(pArgs[i + 1]);
                    if (pArgs[i + 1] != null) {
                        gbdArgs.setPath2grobidProperty(getPath2GbdProperties(pArgs[i + 1]));
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-dIn")) {
                    if (pArgs[i + 1] != null) {
                        gbdArgs.setPath2Input(pArgs[i + 1]);
                        gbdArgs.setPdf(true);
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-s")) {
                    if (pArgs[i + 1] != null) {
                        gbdArgs.setInput(pArgs[i + 1]);
                        gbdArgs.setPdf(false);
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-dOut")) {
                    if (pArgs[i + 1] != null) {
                        gbdArgs.setPath2Output(pArgs[i + 1]);
                    }
                    i++;
                    continue;
                }
                if (currArg.equals("-exe")) {
                    final String command = pArgs[i + 1];
                    if (availableCommands.contains(command)) {
                        gbdArgs.setProcessMethodName(command);
                        i++;
                        continue;
                    } else {
                        System.err.println("-exe value should be one value from this list: " + availableCommands);
                        result = false;
                        break;
                    }
                }
                if (currArg.equals("-ignoreAssets")) {
                    gbdArgs.setSaveAssets(false);
                    continue;
                }
                if (currArg.equals("-r")) {
                    gbdArgs.setRecursive(true);
                    continue;
                }
            }
        }
        return result;
    }

    /**
     * Starts Grobid from command line using the following parameters:
     *
     * @param args The arguments
     */
    public static void main(final String[] args) throws Exception {
        gbdArgs = new GrobidMainArgs();

        if (processArgs(args) && (gbdArgs.getProcessMethodName() != null)) {
            if (isNotEmpty(gbdArgs.getPath2grobidHome())) {
                initProcess(gbdArgs.getPath2grobidHome());
            } else {
                initProcess();
            }

            int nb = 0;


            long time = System.currentTimeMillis();

            if (gbdArgs.getProcessMethodName().equals(CREATE_TRAINING_DICTIONARY_SEGMENTATION)) {
                DictionarySegmentationParser dictionarySegmentationParser = DictionarySegmentationParser.getInstance();
                nb = dictionarySegmentationParser.createTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output());
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_ANNOTATED_TRAINING_DICTIONARY_SEGMENTATION)) {
                DictionarySegmentationParser dictionarySegmentationParser = DictionarySegmentationParser.getInstance();
                nb = dictionarySegmentationParser.createAnnotatedTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output());
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_TRAINING_DICTIONARY_BODY_SEGMENTATION)) {
                DictionaryBodySegmentationParser dictionaryBodySegmentationParser = DictionaryBodySegmentationParser.getInstance();
                nb = dictionaryBodySegmentationParser.createTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output());
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_ANNOTATED_TRAINING_DICTIONARY_BODY_SEGMENTATION)) {
                DictionaryBodySegmentationParser dictionaryBodySegmentationParser = DictionaryBodySegmentationParser.getInstance();
                nb = dictionaryBodySegmentationParser.createAnnotatedTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output());
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_TRAINING_LEXICAL_ENTRY)) {
                LexicalEntryParser lexicalEntryParser = LexicalEntryParser.getInstance();
                nb = lexicalEntryParser.createTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output());
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_ANNOTATED_TRAINING_LEXICAL_ENTRY)) {
                LexicalEntryParser lexicalEntryParser = LexicalEntryParser.getInstance();
                nb = lexicalEntryParser.createAnnotatedTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output());
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_TRAINING_FORM)) {
                FormParser formParser = FormParser.getInstance();
                nb = formParser.createTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output());
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_ANNOTATED_TRAINING_FORM)) {
                FormParser formParser = FormParser.getInstance();
                nb = formParser.createAnnotatedTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output());
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_TRAINING_SENSE)) {
                SenseParser senseParser = SenseParser.getInstance();
                nb = senseParser.createTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output());
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_ANNOTATED_TRAINING_SENSE)) {
                SenseParser senseParser = SenseParser.getInstance();
                nb = senseParser.createAnnotatedTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output());
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_TRAINING_SUB_SENSE)) {
                SubSenseParser subSenseParser = SubSenseParser.getInstance();
                nb = subSenseParser.createTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output());
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_ANNOTATED_TRAINING_SUB_SENSE)) {
                SubSenseParser subSenseParser = SubSenseParser.getInstance();
                nb = subSenseParser.createAnnotatedTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output());
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_TRAINING_ETYMQUOTE)) {
                EtymQuoteParser etymQuoteParser = EtymQuoteParser.getInstance();
                nb = etymQuoteParser.createTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output());
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_ANNOTATED_TRAINING_ETYMQUOTE)) {
                EtymQuoteParser etymQuoteParser = EtymQuoteParser.getInstance();
                nb = etymQuoteParser.createAnnotatedTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output());
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_TRAINING_ETYM)) {
                EtymParser etymParser = EtymParser.getInstance();
                nb = etymParser.createTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output());
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_ANNOTATED_TRAINING_ETYM)) {
                EtymParser etymParser = EtymParser.getInstance();
                nb = etymParser.createAnnotatedTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output());
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_TRAINING_CROSSREF)) {
                CrossRefParser crossRefParserParser = CrossRefParser.getInstance();
                nb = crossRefParserParser.createTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output(), "lexical entry");
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_ANNOTATED_TRAINING_CROSSREF)) {
                CrossRefParser crossRefParserParser = CrossRefParser.getInstance();
                nb = crossRefParserParser.createAnnotatedTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output(), "lexical entry");
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_TRAINING_SENSE_CROSSREF)) {
                CrossRefParser crossRefParserParser = CrossRefParser.getInstance();
                nb = crossRefParserParser.createTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output(), "sense");
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_ANNOTATED_TRAINING_SENSE_CROSSREF)) {
                CrossRefParser crossRefParserParser = CrossRefParser.getInstance();
                nb = crossRefParserParser.createAnnotatedTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output(), "sense");
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }

            if (gbdArgs.getProcessMethodName().equals(CREATE_TRAINING_FORM_GRAMGRP)) {
                GramGrpParser gramGrpParser = GramGrpParser.getInstance();
                nb = gramGrpParser.createTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output(), "form");
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_ANNOTATED_TRAINING_FORM_GRAMGRP)) {
                GramGrpParser gramGrpParser = GramGrpParser.getInstance();
                nb = gramGrpParser.createAnnotatedTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output(), "form");
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_TRAINING_LEXICAL_ENTRY_GRAMGRP)) {
                GramGrpParser gramGrpParser = GramGrpParser.getInstance();
                nb = gramGrpParser.createTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output(), "lexical entry");
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_ANNOTATED_TRAINING_LEXICAL_ENTRY_GRAMGRP)) {
                GramGrpParser gramGrpParser = GramGrpParser.getInstance();
                nb = gramGrpParser.createAnnotatedTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output(), "lexical entry");
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_TRAINING_LEXICAL_SENSE_GRAMGRP)) {
                GramGrpParser gramGrpParser = GramGrpParser.getInstance();
                nb = gramGrpParser.createTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output(), "sense");
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }
            if (gbdArgs.getProcessMethodName().equals(CREATE_ANNOTATED_TRAINING_SENSE_GRAMGRP)) {
                GramGrpParser gramGrpParser = GramGrpParser.getInstance();
                nb = gramGrpParser.createAnnotatedTrainingBatch(gbdArgs.getPath2Input(), gbdArgs.getPath2Output(), "sense");
                System.out.println(nb + " files processed in " + (System.currentTimeMillis() - time) + " milliseconds");
            }



        }
    }


}
