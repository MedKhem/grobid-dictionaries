package org.grobid.trainer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.grobid.core.GrobidModel;
import org.grobid.core.GrobidDictionaryModels;
import org.grobid.core.engines.DictionaryTaggerFactory;
import org.grobid.core.engines.tagging.GenericTagger;
import org.grobid.core.engines.tagging.GrobidCRFEngine;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.utilities.GrobidDictionaryProperties;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.trainer.evaluation.EvaluationUtilities;
import org.grobid.trainer.evaluation.LabelResult;
import org.grobid.trainer.evaluation.ModelStats;
import org.grobid.trainer.evaluation.Stats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Med on 05.12.19.
 */
public abstract class AbstractDictionaryTrainer implements Trainer {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractDictionaryTrainer.class);
    public static final String OLD_MODEL_EXT = ".old";
    public static final String NEW_MODEL_EXT = ".new";

    // default training parameters (only exploited by Wapiti)
    protected double epsilon = 0.0; // size of the interval for stopping criterion
    protected int window = 0; // similar to CRF++
    protected int nbMaxIterations = 0; // maximum number of iterations in training

    protected GrobidModel model;
    private File trainDataPath;
    private File evalDataPath;
    private GenericTagger tagger;

    public AbstractDictionaryTrainer(final GrobidModel model) {
        GrobidFactory.getInstance().createEngine();
        this.model = model;
        if (model.equals(GrobidDictionaryModels.DUMMY)) {

            // In case of dummy model we do not initialise (and create) temporary files
            return;
        }
        this.trainDataPath = getTempTrainingDataPath();
        this.evalDataPath = getTempEvaluationDataPath();
    }

    public void setParams(double epsilon, int window, int nbMaxIterations) {
        this.epsilon = epsilon;
        this.window = window;
        this.nbMaxIterations = nbMaxIterations;
    }

    @Override
    public int createCRFPPData(final File corpusDir, final File trainingOutputPath) {
        return createCRFPPData(corpusDir, trainingOutputPath, null, 1.0);
    }

    @Override
    public void train() {
        final File dataPath = trainDataPath;
        createCRFPPData(getCorpusPath(), dataPath);
        GenericTrainer trainer = DictionaryTrainerFactory.getTrainer(model);

        if (epsilon != 0.0)
            trainer.setEpsilon(epsilon);
        if (window != 0)
            trainer.setWindow(window);
        if (nbMaxIterations != 0)
            trainer.setNbMaxIterations(nbMaxIterations);

        File dirModelPath = new File(GrobidDictionaryProperties.getModelPath(model).getAbsolutePath()).getParentFile();
        if (!dirModelPath.exists()) {
            LOGGER.warn("Cannot find the destination directory " + dirModelPath.getAbsolutePath() + " for the model " + model.getModelName() + ". Creating it.");
            dirModelPath.mkdir();
            //throw new GrobidException("Cannot find the destination directory " + dirModelPath.getAbsolutePath() + " for the model " + model.toString());
        }
        final File tempModelPath = new File(GrobidDictionaryProperties.getModelPath(model).getAbsolutePath() + NEW_MODEL_EXT);
        final File oldModelPath = GrobidDictionaryProperties.getModelPath(model);
        trainer.train(getTemplatePath(), dataPath, tempModelPath, GrobidDictionaryProperties.getNBThreads(), model);
        // if we are here, that means that training succeeded
        // rename model for CRF sequence labellers (not with DeLFT deep learning models)
        if (((GrobidDictionaryProperties.getGrobidCRFEngine() == GrobidCRFEngine.DELFT) && (model.getModelName().equals("dictionary-segmentation")))
                || (GrobidDictionaryProperties.getGrobidCRFEngine() == GrobidCRFEngine.WAPITI))
            renameModels(oldModelPath, tempModelPath);


    }

    protected void renameModels(final File oldModelPath, final File tempModelPath) {
        if (oldModelPath.exists()) {
            if (!oldModelPath.renameTo(new File(oldModelPath.getAbsolutePath() + OLD_MODEL_EXT))) {
                LOGGER.warn("Unable to rename old model file: " + oldModelPath.getAbsolutePath());
                return;
            }
        }

        if (!tempModelPath.renameTo(oldModelPath)) {
            LOGGER.warn("Unable to rename new model file: " + tempModelPath);
        }
    }

    @Override
    public String evaluate() {
        return evaluate(false);
    }

    @Override
    public String evaluate(boolean includeRawResults) {
        createCRFPPData(getEvalCorpusPath(), evalDataPath);
        return EvaluationUtilities.evaluateStandard(evalDataPath.getAbsolutePath(), getTagger()).toString(includeRawResults);
    }

    @Override
    public String evaluate(GenericTagger tagger, boolean includeRawResults) {
        createCRFPPData(getEvalCorpusPath(), evalDataPath);
        return EvaluationUtilities.evaluateStandard(evalDataPath.getAbsolutePath(), tagger).toString(includeRawResults);
    }

    @Override
    public String splitTrainEvaluate(Double split) {
        final File dataPath = trainDataPath;
        createCRFPPData(getCorpusPath(), dataPath, evalDataPath, split);
        GenericTrainer trainer = DictionaryTrainerFactory.getTrainer(model);

        if (epsilon != 0.0)
            trainer.setEpsilon(epsilon);
        if (window != 0)
            trainer.setWindow(window);
        if (nbMaxIterations != 0)
            trainer.setNbMaxIterations(nbMaxIterations);

        File dirModelPath = new File(GrobidDictionaryProperties.getModelPath(model).getAbsolutePath()).getParentFile();
        if (!dirModelPath.exists()) {
            LOGGER.warn("Cannot find the destination directory " + dirModelPath.getAbsolutePath() + " for the model " + model.getModelName() + ". Creating it.");
            dirModelPath.mkdir();
            //throw new GrobidException("Cannot find the destination directory " + dirModelPath.getAbsolutePath() + " for the model " + model.toString());
        }

        final File tempModelPath = new File(GrobidDictionaryProperties.getModelPath(model).getAbsolutePath() + NEW_MODEL_EXT);
        final File oldModelPath = GrobidDictionaryProperties.getModelPath(model);

        trainer.train(getTemplatePath(), dataPath, tempModelPath, GrobidDictionaryProperties.getNBThreads(), model);

        // if we are here, that means that training succeeded
        renameModels(oldModelPath, tempModelPath);

        return EvaluationUtilities.evaluateStandard(evalDataPath.getAbsolutePath(), getTagger()).toString();
    }

    @Override
    public String nFoldEvaluate(int numFolds) {
        return nFoldEvaluate(numFolds, false);
    }

    @Override
    public String nFoldEvaluate(int numFolds, boolean includeRawResults) {
        final File dataPath = trainDataPath;
        final File evalPath = evalDataPath;

        createCRFPPData(getCorpusPath(), dataPath);
        GenericTrainer trainer = DictionaryTrainerFactory.getTrainer(model);

        // Load in memory and Shuffle
        Path dataPath2 = Paths.get(dataPath.getAbsolutePath());
        Path dataPath3 = Paths.get(dataPath.getAbsolutePath());

        List<String> trainingData = loadAndShuffle(dataPath2, dataPath3);


        // Split into folds
        List<ImmutablePair<String, String>> foldMap = splitNFold(trainingData, numFolds);

        // Train and evaluation
        if (epsilon != 0.0)
            trainer.setEpsilon(epsilon);
        if (window != 0)
            trainer.setWindow(window);
        if (nbMaxIterations != 0)
            trainer.setNbMaxIterations(nbMaxIterations);

        //We dump the model in the tmp directory
        File tmpDirectory = new File(GrobidDictionaryProperties.getTempPath().getAbsolutePath());
        if (!tmpDirectory.exists()) {
            LOGGER.warn("Cannot find the destination directory " + tmpDirectory);
        }

        // Output
        StringBuilder sb = new StringBuilder();
        sb.append("Recap results for each fold:").append("\n\n");

        AtomicInteger counter = new AtomicInteger(0);
        List<ModelStats> evaluationResults = foldMap.stream().map(fold -> {
            sb.append("\n");
            sb.append("====================== Fold " + counter.get() + " ====================== ").append("\n");
            System.out.println("====================== Fold " + counter.get() + " ====================== ");

            final File tempModelPath = new File(tmpDirectory + File.separator + getModel().getModelName()
                    + "_nfold_" + counter.getAndIncrement() + ".wapiti");
            sb.append("Saving model in " + tempModelPath).append("\n");

            sb.append("Training input data: " + fold.getLeft()).append("\n");
            trainer.train(getTemplatePath(), new File(fold.getLeft()), tempModelPath, GrobidDictionaryProperties.getNBThreads(), model);
            sb.append("Evaluation input data: " + fold.getRight()).append("\n");

            //TODO: find a better solution!!
            GrobidModel tmpModel = new GrobidModel() {
                @Override
                public String getFolderName() {
                    return tmpDirectory.getAbsolutePath();
                }

                @Override
                public String getModelPath() {
                    return tempModelPath.getAbsolutePath();
                }

                @Override
                public String getModelName() {
                    return model.getModelName();
                }

                @Override
                public String getTemplateName() {
                    return model.getTemplateName();
                }
            };

            ModelStats modelStats = EvaluationUtilities.evaluateStandard(fold.getRight(), DictionaryTaggerFactory.getTagger(tmpModel));

            sb.append(modelStats.toString(includeRawResults));
            sb.append("\n");
            sb.append("\n");

            return modelStats;
        }).collect(Collectors.toList());


        sb.append("\n").append("Summary results: ").append("\n");

        Comparator<ModelStats> f1ScoreComparator = (o1, o2) -> {
            Stats fieldStatsO1 = o1.getFieldStats();
            Stats fieldStatsO2 = o2.getFieldStats();

            if (fieldStatsO1.getMacroAverageF1() > fieldStatsO2.getMacroAverageF1()) {
                return 1;
            } else if (fieldStatsO1.getMacroAverageF1() < fieldStatsO2.getMacroAverageF1()) {
                return -1;
            } else {
                return 0;
            }
        };

        Optional<ModelStats> worstModel = evaluationResults.stream().min(f1ScoreComparator);
        sb.append("Worst fold").append("\n");
        ModelStats worstModelStats = worstModel.orElseGet(() -> {
            throw new GrobidException("Something wrong when computing evaluations " +
                    "- worst model metrics not found. ");
        });
        sb.append(worstModelStats.toString()).append("\n");

        sb.append("Best fold:").append("\n");
        Optional<ModelStats> bestModel = evaluationResults.stream().max(f1ScoreComparator);
        ModelStats bestModelStats = bestModel.orElseGet(() -> {
            throw new GrobidException("Something wrong when computing evaluations " +
                    "- best model metrics not found. ");
        });
        sb.append(bestModelStats.toString()).append("\n").append("\n");

        // Averages
        sb.append("Average over " + numFolds + " folds: ").append("\n");

        TreeMap<String, LabelResult> averagesLabelStats = new TreeMap<>();
        int totalInstances = 0;
        int correctInstances = 0;
        for (ModelStats ms : evaluationResults) {
            totalInstances += ms.getTotalInstances();
            correctInstances += ms.getCorrectInstance();
            for (Map.Entry<String, LabelResult> entry : ms.getFieldStats().getLabelsResults().entrySet()) {
                String key = entry.getKey();
                if (averagesLabelStats.containsKey(key)) {
                    averagesLabelStats.get(key).setAccuracy(averagesLabelStats.get(key).getAccuracy() + entry.getValue().getAccuracy());
                    averagesLabelStats.get(key).setF1Score(averagesLabelStats.get(key).getF1Score() + entry.getValue().getF1Score());
                    averagesLabelStats.get(key).setRecall(averagesLabelStats.get(key).getRecall() + entry.getValue().getRecall());
                    averagesLabelStats.get(key).setPrecision(averagesLabelStats.get(key).getPrecision() + entry.getValue().getPrecision());
                    averagesLabelStats.get(key).setSupport(averagesLabelStats.get(key).getSupport() + entry.getValue().getSupport());
                } else {
                    averagesLabelStats.put(key, new LabelResult(key));
                    averagesLabelStats.get(key).setAccuracy(entry.getValue().getAccuracy());
                    averagesLabelStats.get(key).setF1Score(entry.getValue().getF1Score());
                    averagesLabelStats.get(key).setRecall(entry.getValue().getRecall());
                    averagesLabelStats.get(key).setPrecision(entry.getValue().getPrecision());
                    averagesLabelStats.get(key).setSupport(entry.getValue().getSupport());
                }
            }
        }

        sb.append(String.format("\n%-20s %-12s %-12s %-12s %-12s %-7s\n\n",
                "label",
                "accuracy",
                "precision",
                "recall",
                "f1",
                "support"));

        for (String label : averagesLabelStats.keySet()) {
            LabelResult labelResult = averagesLabelStats.get(label);

            double avgAccuracy = labelResult.getAccuracy() / evaluationResults.size();
            averagesLabelStats.get(label).setAccuracy(avgAccuracy);

            double avgF1Score = labelResult.getF1Score() / evaluationResults.size();
            averagesLabelStats.get(label).setF1Score(avgF1Score);

            double avgPrecision = labelResult.getPrecision() / evaluationResults.size();
            averagesLabelStats.get(label).setPrecision(avgPrecision);

            double avgRecall = labelResult.getRecall() / evaluationResults.size();
            averagesLabelStats.get(label).setRecall(avgRecall);

            sb.append(labelResult.toString());
        }

        OptionalDouble averageF1 = evaluationResults.stream().mapToDouble(e -> e.getFieldStats().getMacroAverageF1()).average();
        OptionalDouble averagePrecision = evaluationResults.stream().mapToDouble(e -> e.getFieldStats().getMacroAveragePrecision()).average();
        OptionalDouble averageRecall = evaluationResults.stream().mapToDouble(e -> e.getFieldStats().getMacroAverageRecall()).average();
        OptionalDouble averageAccuracy = evaluationResults.stream().mapToDouble(e -> e.getFieldStats().getMacroAverageAccuracy()).average();

        double avgAccuracy = averageAccuracy.orElseGet(() -> {
            throw new GrobidException("Missing average accuracy. Something went wrong. Please check. ");
        });

        double avgF1 = averageF1.orElseGet(() -> {
            throw new GrobidException("Missing average F1. Something went wrong. Please check. ");
        });

        double avgPrecision = averagePrecision.orElseGet(() -> {
            throw new GrobidException("Missing average precision. Something went wrong. Please check. ");
        });

        double avgRecall = averageRecall.orElseGet(() -> {
            throw new GrobidException("Missing average recall. Something went wrong. Please check. ");
        });

        sb.append("\n");

        sb.append(String.format("%-20s %-12s %-12s %-12s %-7s\n",
                "all (macro avg.)",
                TextUtilities.formatTwoDecimals(avgAccuracy * 100),
                TextUtilities.formatTwoDecimals(avgPrecision * 100),
                TextUtilities.formatTwoDecimals(avgRecall * 100),
                TextUtilities.formatTwoDecimals(avgF1 * 100))
//            String.valueOf(supportSum))
        );

        sb.append("\n===== Instance-level results =====\n\n");

        double averageTotalInstances = (double) totalInstances / numFolds;
        double averageCorrectInstances = (double) correctInstances / numFolds;
        sb.append(String.format("%-27s %s\n", "Total expected instances:", TextUtilities.formatTwoDecimals(averageTotalInstances)));
        sb.append(String.format("%-27s %s\n", "Correct instances:", TextUtilities.formatTwoDecimals(averageCorrectInstances)));
        sb.append(String.format("%-27s %s\n",
                "Instance-level recall:",
                TextUtilities.formatTwoDecimals(averageCorrectInstances / averageTotalInstances * 100)));


        return sb.toString();
    }

    /**
     * Partition the corpus in n folds, dump them in n files and return the pairs of (trainingPath, evaluationPath)
     */
    protected List<ImmutablePair<String, String>> splitNFold(List<String> trainingData, int numberFolds) {
        int trainingSize = CollectionUtils.size(trainingData);
        int foldSize = Math.floorDiv(trainingSize, numberFolds);
        if (foldSize == 0) {
            throw new IllegalArgumentException("There aren't enough training data for n-fold evaluation with fold of size " + numberFolds);
        }

        return IntStream.range(0, numberFolds).mapToObj(foldIndex -> {
            int foldStart = foldSize * foldIndex;
            int foldEnd = foldStart + foldSize;

            if (foldIndex == numberFolds - 1) {
                foldEnd = trainingSize;
            }

            List<String> foldEvaluation = trainingData.subList(foldStart, foldEnd);
            List<String> foldTraining0 = trainingData.subList(0, foldStart);
            List<String> foldTraining1 = trainingData.subList(foldEnd, trainingSize);
            List<String> foldTraining = new ArrayList<>();
            foldTraining.addAll(foldTraining0);
            foldTraining.addAll(foldTraining1);

            //Dump Evaluation
            String tempEvaluationDataPath = getTempEvaluationDataPath().getAbsolutePath();
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(tempEvaluationDataPath))) {
                writer.write(String.join("\n\n", foldEvaluation));
                writer.write("\n");
            } catch (IOException e) {
                throw new GrobidException("Error when dumping n-fold evaluation data into files. ", e);
            }

            //Dump Training
            String tempTrainingDataPath = getTempTrainingDataPath().getAbsolutePath();
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(tempTrainingDataPath))) {
                writer.write(String.join("\n\n", foldTraining));
                writer.write("\n");
            } catch (IOException e) {
                throw new GrobidException("Error when dumping n-fold training data into files. ", e);
            }

            return new ImmutablePair<>(tempTrainingDataPath, tempEvaluationDataPath);
        }).collect(Collectors.toList());
    }

    /**
     * Load the dataset in memory and shuffle it.
     */
    protected List<String> loadAndShuffle(Path dataPath, Path dataPath3) {
        List<String> trainingData = load(dataPath);
        trainingData.addAll(load(dataPath3));

        Collections.shuffle(trainingData, new Random(839374947498L));

        return trainingData;
    }

    /**
     * Read the Wapiti training files in list of String.
     * Assuming that each empty line is a delimiter between instances.
     * Each list element corresponds to one instance.
     * Empty line are filtered out from the output.
     */
    public List<String> load(Path dataPath) {
        List<String> trainingData = new ArrayList<>();
        try (Stream<String> stream = Files.lines(dataPath)) {
            List<String> instance = new ArrayList<>();
            ListIterator<String> iterator = stream.collect(Collectors.toList()).listIterator();
            while (iterator.hasNext()) {
                String current = iterator.next();

                if (StringUtils.isBlank(current)) {
                    if (CollectionUtils.isNotEmpty(instance)) {
                        trainingData.add(String.join("\n", instance));
                    }
                    instance = new ArrayList<>();
                } else {
                    instance.add(current);
                }
            }
            if (CollectionUtils.isNotEmpty(instance)) {
                trainingData.add(String.join("\n", instance));
            }

        } catch (IOException e) {
            throw new GrobidException("Error in n-fold, when loading training data. Failing. ", e);
        }

        return trainingData;
    }

    protected final File getTempTrainingDataPath() {
        try {
            return File.createTempFile(model.getModelName(), ".train", GrobidDictionaryProperties.getTempPath());
        } catch (IOException e) {
            throw new RuntimeException("Unable to create a temporary training file for model: " + model);
        }
    }

    protected final File getTempEvaluationDataPath() {
        try {
            return File.createTempFile(model.getModelName(), ".test", GrobidDictionaryProperties.getTempPath());
        } catch (IOException e) {
            throw new RuntimeException("Unable to create a temporary evaluation file for model: " + model);
        }
    }

    protected GenericTagger getTagger() {
        if (tagger == null) {
            tagger = DictionaryTaggerFactory.getTagger(model);
        }

        return tagger;
    }

    protected static File getFilePath2Resources() {
        File theFile = new File(GrobidDictionaryProperties.get_GROBID_HOME_PATH().getAbsoluteFile() + File.separator + ".." + File.separator
                + "grobid-trainer" + File.separator + "resources");
        if (!theFile.exists()) {
            theFile = new File("resources");
        }
        return theFile;
    }

    protected File getCorpusPath() {
        return GrobidDictionaryProperties.getCorpusPath(getFilePath2Resources(), model);
    }

    protected File getTemplatePath() {
        return getTemplatePath(model);
    }

    protected File getTemplatePath(final GrobidModel model) {
        return GrobidDictionaryProperties.getTemplatePath(getFilePath2Resources(), model);
    }

    protected File getEvalCorpusPath() {
        return GrobidDictionaryProperties.getEvalCorpusPath(getFilePath2Resources(), model);
    }

    public static File getEvalCorpusBasePath() {
        final String path2Evelutation = getFilePath2Resources().getAbsolutePath() + File.separator + "dataset" + File.separator + "patent"
                + File.separator + "evaluation";
        return new File(path2Evelutation);
    }

    @Override
    public GrobidModel getModel() {
        return model;
    }

    public static void runTraining(final AbstractDictionaryTrainer trainer) {
        long start = System.currentTimeMillis();
        trainer.train();
        long end = System.currentTimeMillis();
        LOGGER.warn("runntraining said the model is " + trainer.getModel().getModelPath());

        System.out.println("Model for " + trainer.getModel() + " created in " + (end - start) + " ms");
    }

    public File getEvalDataPath() {
        return evalDataPath;
    }

    public static String runEvaluation(final AbstractDictionaryTrainer trainer, boolean includeRawResults) {
        long start = System.currentTimeMillis();
        String report = "";
        try {
            report = trainer.evaluate(includeRawResults);
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while evaluating Grobid.", e);
        }
        long end = System.currentTimeMillis();
        report += "\n\nEvaluation for " + trainer.getModel() + " model is realized in " + (end - start) + " ms";


        return report;
    }

    public static String runEvaluation(final AbstractDictionaryTrainer trainer, boolean includeRawResults, String[] variables) throws IOException {
        long start = System.currentTimeMillis();
        String report = "";
        StringBuffer trainingParameters = new StringBuffer();
        try {
            report = trainer.evaluate(includeRawResults);
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while evaluating Grobid.", e);
        }
        long end = System.currentTimeMillis();
        report += "\n\nEvaluation for " + trainer.getModel() + " model is realized in " + (end - start) + " ms";

        if (variables.length > 0) {
            String modelType = GrobidDictionaryProperties.getGrobidCRFEngine().toString();
            String outPathRawtext;

            if (variables.length == 1) {
                if (variables[0].toString().equals("true")) {

                    outPathRawtext = "resources" + "/" + "eval" + modelType + "/" + trainer.getModel() + "/evaluationOnly.txt";
                    FileUtils.writeStringToFile(new File(outPathRawtext), report, "UTF-8");
                    System.out.print("File stored at" + outPathRawtext);
                }

            } else if (variables.length > 1) {

                try {

                    trainingParameters.append("Dict+");
                    trainingParameters.append(variables[1] + "+");
                    trainingParameters.append("Model+");
                    trainingParameters.append(trainer.getModel() + "+");


                    if (variables.length > 1) {
                        trainingParameters.append("Feature+");
                        trainingParameters.append(variables[2] + "+");
                    }
                    if (variables.length > 2) {
                        trainingParameters.append("DataLevel+");
                        trainingParameters.append(variables[3]);
                    }


                    String dictName = variables[1];


                    outPathRawtext = "resources" + "/" + "eval" + modelType + "/" + dictName + "/" + trainer.getModel();
                    File file = new File(outPathRawtext);
                    if (!file.exists()) {
                        if (file.mkdir()) {
                            System.out.println("Directory is created!");
                        } else {
                            System.out.println("Failed to create directory!");
                        }
                    }
                    report = trainingParameters.toString() + report;
                    FileUtils.writeStringToFile(new File(outPathRawtext + "/" + "Feature" + variables[2] + "DataLevel" + variables[3] + ".txt"), report, "UTF-8");

                } catch (final Exception exp) {
                    throw new GrobidException("An exception occurred while rendering evaluation.", exp);
                }

            }
        }

        return report;
    }


    public static String runEvaluation(final AbstractDictionaryTrainer trainer) {
        return trainer.evaluate(false);
    }

    public static String runSplitTrainingEvaluation(final AbstractDictionaryTrainer trainer, Double split) {
        long start = System.currentTimeMillis();
        String report = "";
        try {
            report = trainer.splitTrainEvaluate(split);

        } catch (Exception e) {
            throw new GrobidException("An exception occurred while evaluating Grobid.", e);
        }
        long end = System.currentTimeMillis();
        report += "\n\nSplit, training and evaluation for " + trainer.getModel() + " model is realized in " + (end - start) + " ms";

        return report;
    }

    public static void runNFoldEvaluation(final AbstractDictionaryTrainer trainer, int numFolds, Path outputFile) {
        runNFoldEvaluation(trainer, numFolds, outputFile, false);
    }

    public static void runNFoldEvaluation(final AbstractDictionaryTrainer trainer, int numFolds, Path outputFile, boolean includeRawResults) {

        String report = runNFoldEvaluation(trainer, numFolds, includeRawResults);

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            writer.write(report);
            writer.write("\n");
        } catch (IOException e) {
            throw new GrobidException("Error when dumping n-fold training data into files. ", e);
        }

    }

    public static String runNFoldEvaluation(final AbstractDictionaryTrainer trainer, int numFolds) {
        return runNFoldEvaluation(trainer, numFolds, false);
    }

    public static String runNFoldEvaluation(final AbstractDictionaryTrainer trainer, int numFolds, boolean includeRawResults) {
        long start = System.currentTimeMillis();
        String report = "";
        try {
            report = trainer.nFoldEvaluate(numFolds, includeRawResults);

        } catch (Exception e) {
            throw new GrobidException("An exception occurred while evaluating Grobid.", e);
        }
        long end = System.currentTimeMillis();
        report += "\n\nN-Fold evaluation for " + trainer.getModel() + " model is realized in " + (end - start) + " ms";

        return report;
    }

    /**
     * Dispatch the example to the training or test data, based on the split ration and the drawing of
     * a random number
     */
    public Writer dispatchExample(Writer writerTraining, Writer writerEvaluation, double splitRatio) {
        Writer writer = null;
        if ((writerTraining == null) && (writerEvaluation != null)) {
            writer = writerEvaluation;
        } else if ((writerTraining != null) && (writerEvaluation == null)) {
            writer = writerTraining;
        } else {
            if (Math.random() <= splitRatio)
                writer = writerTraining;
            else
                writer = writerEvaluation;
        }
        return writer;
    }


}