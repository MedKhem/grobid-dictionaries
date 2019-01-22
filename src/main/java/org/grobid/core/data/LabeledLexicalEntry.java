package org.grobid.core.data;

import org.grobid.core.layout.LayoutToken;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by med on 02.08.16.
 */
public class LabeledLexicalEntry {

    private List<Pair<List<LayoutToken>, String>> labels = new ArrayList<>();

    public boolean addLabel(Pair<List<LayoutToken>, String> label) {
        return labels.add(label);
    }

    public List<Pair<List<LayoutToken>, String>> getLabels() {
        return labels;
    }

    public void setLabels(List<Pair<List<LayoutToken>, String>> labels) {
        this.labels = labels;
    }

}
