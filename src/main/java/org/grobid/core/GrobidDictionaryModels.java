package org.grobid.core;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.utilities.GrobidDictionaryProperties;
import org.grobid.core.utilities.GrobidProperties;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.grobid.core.engines.EngineParsers.LOGGER;

/**
 * Created by Med on 07.12.19.
 */
public enum GrobidDictionaryModels implements GrobidModel {

    DICTIONARY_SEGMENTATION("dictionary-segmentation"),
    DICTIONARY_BODY_SEGMENTATION("dictionary-body-segmentation"),
    LEXICAL_ENTRY("lexical-entry"),
    FORM("form"),
    SENSE("sense"),
    SUB_SENSE("sub-sense"),
    GRAMMATICAL_GROUP ("gramGrp"),
    ETYM_QUOTE("etymQuote"),
    ETYM ("etym"),
    CROSS_REF("crossRef"),
    DIVISION("division"),
    SUB_ENTRY("sub-entry"),
    DUMMY("none");

    //I cannot declare it before
    public static final String DUMMY_FOLDER_LABEL = "none";

    /**
     * Absolute path to the model.
     */
    private String modelPath;

    private String folderName;

    private static final ConcurrentMap<String, GrobidModel> models = new ConcurrentHashMap<>();

    GrobidDictionaryModels(String folderName) {
        if(StringUtils.equals(DUMMY_FOLDER_LABEL, folderName)) {
            modelPath = DUMMY_FOLDER_LABEL;
            this.folderName = DUMMY_FOLDER_LABEL;
            return;
        }

        this.folderName = folderName;
        File path = GrobidDictionaryProperties.getModelPath(this);
        if (!path.exists()) {
            // to be reviewed
            /*System.err.println("Warning: The file path to the "
                    + this.name() + " CRF model is invalid: "
					+ path.getAbsolutePath());*/
        }
        modelPath = path.getAbsolutePath();
    }

    public String getFolderName() {
        return folderName;
    }

    public String getModelPath() {
        return modelPath;
    }

    public String getModelName() {
        return folderName.replaceAll("/", "-");
    }

    public String getTemplateName() {
        return StringUtils.substringBefore(folderName, "/") + ".template";
    }

    @Override
    public String toString() {
        return folderName;
    }

    public static GrobidModel modelFor(final String name) {
        if (models.isEmpty()) {
            for (GrobidModel model : values())
                models.putIfAbsent(model.getFolderName(), model);
        }

        models.putIfAbsent(name.toString(/* null-check */), new GrobidModel() {
            @Override
            public String getFolderName() {
                return name;
            }

            @Override
            public String getModelPath() {
                File path = GrobidDictionaryProperties.getModelPath(this);
                if (!path.exists()) {
                    LOGGER.warn("The file path to the "
                            + name + " model is invalid: "
                            + path.getAbsolutePath());
                }
                return path.getAbsolutePath();
            }

            @Override
            public String getModelName() {
                return getFolderName().replaceAll("/", "-");
            }

            @Override
            public String getTemplateName() {
                return StringUtils.substringBefore(getFolderName(), "/") + ".template";
            }
        });
        return models.get(name);
    }

    public String getName() {
        return name();
    }
}
