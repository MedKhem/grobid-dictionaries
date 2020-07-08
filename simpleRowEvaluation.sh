#!/usr/local/bin/bash

mvn generate-resources -P train_dictionary_segmentation -e && \
cp ~/grobid/grobid-home/models/dictionary-segmentation/model.wapiti ~/grobid/grobid-dictionaries/resources/dataset/dictionary-segmentation/  && \
mvn generate-resources -P train_dictionary_segmentation -e -Dexec.args="true" && \


mvn generate-resources -P train_dictionary_body_segmentation -e && \
cp ~/grobid/grobid-home/models/dictionary-body-segmentation/model.wapiti ~/grobid/grobid-dictionaries/resources/dataset/dictionary-body-segmentation/ && \
mvn generate-resources -P train_dictionary_body_segmentation -e -Dexec.args="true" && \


mvn generate-resources -P train_lexicalEntries -e && \
cp ~/grobid/grobid-home/models/lexical-entry/model.wapiti ~/grobid/grobid-dictionaries/resources/dataset/lexical-entry/  && \
mvn generate-resources -P train_lexicalEntries -e -Dexec.args="true" && \


mvn generate-resources -P train_form -e && \
cp ~/grobid/grobid-home/models/form/model.wapiti ~/grobid/grobid-dictionaries/resources/dataset/form/  && \
mvn generate-resources -P train_form -e -Dexec.args="true" && \


mvn generate-resources -P train_sense -e && \
cp ~/grobid/grobid-home/models/sense/model.wapiti ~/grobid/grobid-dictionaries/resources/dataset/sense/  && \
mvn generate-resources -P train_sense -e -Dexec.args="true" && \


mvn generate-resources -P train_sub_sense -e && \
cp ~/grobid/grobid-home/models/sub-sense/model.wapiti ~/grobid/grobid-dictionaries/resources/dataset/sub-sense/  && \
mvn generate-resources -P train_sub_sense -e -Dexec.args="true" && \


mvn generate-resources -P train_gramGrp -e  && \
cp ~/grobid/grobid-home/models/gramGrp/model.wapiti ~/grobid/grobid-dictionaries/resources/dataset/gramGrp/ && \
mvn generate-resources -P train_gramGrp -e -Dexec.args="true"