package org.grobid.core.document;

import org.grobid.core.data.LabeledLexicalEntry;
import org.grobid.core.data.LabeledLexicalInformation;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.LayoutToken;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import static org.grobid.core.engines.label.DictionarySegmentationLabels.DICTIONARY_DICTSCRAP_LABEL;
import static org.grobid.core.engines.label.DictionarySegmentationLabels.DICTIONARY_FOOTNOTE_LABEL;
import static org.grobid.core.engines.label.DictionarySegmentationLabels.DICTIONARY_HEADNOTE_LABEL;

/**
 * Created by med on 08.11.16.
 */
public class DictionaryDocument extends Document {
    private LabeledLexicalInformation bodyComponents = new LabeledLexicalInformation();
    private LabeledLexicalInformation headnotesOptimised = new LabeledLexicalInformation();
    private LabeledLexicalInformation footnotesOptimised = new LabeledLexicalInformation();
    private LabeledLexicalInformation dictScrapsOptimised = new LabeledLexicalInformation();


    public DictionaryDocument(Document document) {
        super(document.documentSource);
        bodyComponents = null;

    }
    public DictionaryDocument() {
        super();
        bodyComponents = null;

    }
    public DictionaryDocument(File file){

        bodyComponents = null;
        setPathXML(file);
    }


    public void setPathXML(File pathXML) {
        this.pathXML = pathXML.getAbsolutePath();
    }

    public void setTokenizations(List<LayoutToken> tokenizations) {
        this.tokenizations = tokenizations;
    }

    public static DictionaryDocument createFromText(String text) {
        DictionaryDocument doc = new DictionaryDocument();
        doc.fromText(text);
        return doc;
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
            return getDocumentPieceText(pieces);
        }
    }
    public void setBodyComponents(LabeledLexicalInformation listOfTaggedLayoutTokens){
        this.bodyComponents = listOfTaggedLayoutTokens;
    }
    public void setDictionaryPagePartOptimised(LabeledLexicalInformation listOfTaggedLayoutTokens, String label){
        if(label == DICTIONARY_HEADNOTE_LABEL){
            this.headnotesOptimised = listOfTaggedLayoutTokens;
        }
        if(label == DICTIONARY_FOOTNOTE_LABEL){
            this.footnotesOptimised = listOfTaggedLayoutTokens;

        }
        if(label == DICTIONARY_DICTSCRAP_LABEL){
            this.dictScrapsOptimised = listOfTaggedLayoutTokens;
        }

    }

    public LabeledLexicalInformation getBodyComponents(){
        if (this.bodyComponents == null) {
            LOGGER.debug("bodyComponents block is null");
            return null;
        } else {

            return this.bodyComponents;
        }
    }
    public LabeledLexicalInformation getHeadnotesOptimised(){
        if (this.headnotesOptimised == null) {
            LOGGER.debug("bodyComponents block is null");
            return null;
        } else {

            return this.headnotesOptimised;
        }
    }
    public LabeledLexicalInformation getFootnotesOptimised(){
        if (this.footnotesOptimised == null) {
            LOGGER.debug("bodyComponents block is null");
            return null;
        } else {

            return this.footnotesOptimised;
        }
    }
    public DictionaryDocument  removePagesAndTheirBlocks(DictionaryDocument doc, int pageNumber, int blockNumber){

        doc.getBlocks().remove(blockNumber);
        doc.getPages().remove(pageNumber);


        return doc;
    }
    public LabeledLexicalInformation gettDictScrapsOptimised(){
        if (this.dictScrapsOptimised == null) {
            LOGGER.debug("bodyComponents block is null");
            return null;
        } else {

            return this.dictScrapsOptimised;
        }
    }


}
