package org.grobid.core.data;

import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Med on 29.07.17.
 */
public class LabeledLexicalInformation {
    private List<Pair<List<LayoutToken>, String>> labels;
    public LabeledLexicalInformation (){
        labels = new ArrayList<>();
    }
    public boolean addLabel(Pair<List<LayoutToken>, String> label) {
        return labels.add(label);
    }
    public boolean addLabels(List<Pair<List<LayoutToken>, String>> labels) {
        return labels.addAll(labels);
    }

    public List<Pair<List<LayoutToken>, String>> getLabels() {
        return labels;
    }

    public void setLabels(List<Pair<List<LayoutToken>, String>> labels) {
        this.labels = labels;
    }
}
