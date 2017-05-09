package org.grobid.core.data;

import org.grobid.core.utilities.Pair;

import java.util.ArrayList;
import java.util.List;

import static org.grobid.core.engines.label.LexicalEntryLabels.LEXICAL_ENTRY_FORM_LABEL;

/**
 * Created by lfoppiano on 05/05/2017.
 */
public class LabeledForm {
    //default parent tag is 'form'
    private String parentTag = LEXICAL_ENTRY_FORM_LABEL;

    private List<org.grobid.core.utilities.Pair<String, String>> labels = new ArrayList<>();

    public boolean addLabel(Pair<String, String> label) {
        return labels.add(label);
    }

    public List<Pair<String, String>> getLabels() {
        return labels;
    }

    public void setLabels(List<Pair<String, String>> labels) {
        this.labels = labels;
    }

    public String getParentTag() {
        return parentTag;
    }

    public void setParentTag(String parentTag) {
        this.parentTag = "<" + parentTag + ">";
    }
}
