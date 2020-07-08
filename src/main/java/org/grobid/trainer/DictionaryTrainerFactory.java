package org.grobid.trainer;

import org.grobid.core.GrobidModel;
import org.grobid.core.utilities.GrobidDictionaryProperties;

/**
 * Created by Med on 05.12.19.
 */
public class DictionaryTrainerFactory {
    public static GenericTrainer getTrainer(GrobidModel model) {
        switch (GrobidDictionaryProperties.getGrobidCRFEngine()) {
            case CRFPP:
                return new CRFPPGenericTrainer();
            case WAPITI:
                return new WapitiTrainer();
            case DELFT:
                if (model.getModelName().equals("dictionary-segmentation") || model.getModelName().equals("dictionary-body-segmentation"))
                    return new WapitiTrainer();
                else
                    return new DeLFTTrainer();
            case DUMMY:
                return new DummyTrainer();
            default:
                throw new IllegalStateException("Unsupported Grobid sequence labelling engine: " + GrobidDictionaryProperties.getGrobidCRFEngine());
        }
    }
}
