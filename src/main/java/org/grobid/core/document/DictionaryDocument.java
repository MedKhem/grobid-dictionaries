package org.grobid.core.document;

import org.grobid.core.data.LabeledLexicalEntry;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * Created by med on 08.11.16.
 */
public class DictionaryDocument extends Document {
    private List<Pair<List<LayoutToken>,String>> bodyComponents= new ArrayList<>();
    private List<Pair<List<LayoutToken>,String>> labeledLexicalEntries = new ArrayList<>();

    public DictionaryDocument(Document document) {
        super(document.documentSource);
        bodyComponents = null;

    }

    public SortedSet<DocumentPiece> getDocumentDictionaryPart(String segmentationLabel) {
        if (this.labeledBlocks == null) {
            LOGGER.debug("labeledBlocks is null");
            return null;
        } else {
            if (segmentationLabel == null) {
                System.out.println("DictionarySegmentationLabels.getLabel()  is null");
            }

            return this.labeledBlocks.get(segmentationLabel);
        }
    }
    public String getDictionaryDocumentPartText(String segmentationLabel) {
        SortedSet<DocumentPiece> pieces = getDocumentDictionaryPart(segmentationLabel);
        if (pieces == null) {
            return null;
        } else {
            return getDocumentPieceText(getDocumentDictionaryPart(segmentationLabel));
        }
    }
    public void setBodyComponents(List<Pair<List<LayoutToken>,String>> LTs){
        this.bodyComponents = LTs;
    }

    public List<Pair<List<LayoutToken>,String>> getBodyComponents(){
        if (this.bodyComponents == null) {
            LOGGER.debug("bodyComponents block is null");
            return null;
        } else {

            return this.bodyComponents;
        }
    }

    public List<Pair<List<LayoutToken>,String>> getLabeledLexicalEntries() {
        return labeledLexicalEntries;
    }

    public void setLabeledLexicalEntries(List<Pair<List<LayoutToken>,String>> labeledLexicalEntries) {
        this.labeledLexicalEntries = labeledLexicalEntries;
    }
}
