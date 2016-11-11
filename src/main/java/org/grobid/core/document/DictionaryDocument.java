package org.grobid.core.document;

import org.grobid.core.engines.enums.DictionarySegmentationLabel;

import java.util.SortedSet;

/**
 * Created by med on 08.11.16.
 */
public class DictionaryDocument extends Document {

    public DictionaryDocument(DocumentSource document){
        super (document);

    }

    public SortedSet<DocumentPiece> getDocumentDictionaryPart(DictionarySegmentationLabel segmentationLabel) {
        if(this.labeledBlocks == null) {
            LOGGER.debug("labeledBlocks is null");
            return null;
        } else {
            if(segmentationLabel.getLabel() == null) {
                System.out.println("DictionarySegmentationLabel.getLabel()  is null");
            }

            return this.labeledBlocks.get(segmentationLabel.getLabel());
        }
    }
}
