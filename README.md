# GROBID-Dictionaries

[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
<!-- [![Build Status](https://travis-ci.org/kermitt2/grobid-quantities.svg?branch=master)](https://travis-ci.org/kermitt2/grobid-quantities) -->
<!-- [![Coverage Status](https://coveralls.io/repos/kermitt2/grobid-quantities/badge.svg)](https://coveralls.io/r/kermitt2/grobid-quantities) -->
<!-- [![Documentation Status](https://readthedocs.org/projects/grobid-quantities/badge/?version=latest)](https://readthedocs.org/projects/grobid-quantities/?badge=latest) -->

## Purpose

GROBID-Dictionaries is a GROBID sub-module, implementing a java machine learning library, for structuring digitized lexical resources. It allows the parsing, extraction and structuring of text information in such resources. 

## To Cite

Mohamed Khemakhem, Luca Foppiano, Laurent Romary. Automatic Extraction of TEI Structures in Digitized Lexical Resources using Conditional Random Fields. electronic lexicography, eLex 2017, Sep 2017, Leiden, Netherlands. <hal-01508868v2>
```@inproceedings{khemakhem:hal-01508868,
     TITLE = {{Automatic Extraction of TEI Structures in Digitized Lexical Resources using Conditional Random Fields}},
     AUTHOR = {Khemakhem, Mohamed and Foppiano, Luca and Romary, Laurent},
     URL = {https://hal.archives-ouvertes.fr/hal-01508868},
     BOOKTITLE = {{electronic lexicography, eLex 2017}},
     ADDRESS = {Leiden, Netherlands},
     YEAR = {2017},
     MONTH = Sep,
     KEYWORDS = { digitized dictionaries  ;  automatic structuring ;  CRF ;  TEI ; machine learning},
     PDF = {https://hal.archives-ouvertes.fr/hal-01508868/file/eLex-2017-Template.pdf},
     HAL_ID = {hal-01508868},
     HAL_VERSION = {v2},
   }
```

## Approach

GROBID-Dictionaries is based on cascading CRF models. The diagram below presents the architecture enabling the processing and the transfer of the text information through the models.

![GROBID Dictionaries Structure](doc/img/modelsGrobid.png)

Each box represents a model and not a text structure. Every model allows the processing and the structuring of the information provided by its predecessor. 
The name of each model reflects the information to be processed per level. Consequently, the same appellation is used for naming the implemented java classes and their functionalities.
The boxes in red color represent the models which are already implemented, where the rest of the diagram corresponds to the pending components. 

__Dictionary Segmentation__
This is the first model and has as goal the segmentation of each dictionary page into 3 main blocks: Header, Body and Footer. Another block, "Other" could be generated for text information that do not belong to the principal blocks

__Dictionary Body Segmentation__
The second model gets the Body, recognized by the first model, and processes it to recognize the boundaries of each lexical entry.

__Lexical Entry__
The third model parses each lexical entry, recognized by the second model, to segment it into 4 main blocks: Form, Etymology, Senses, Related Entries. An "Other" block is there as well for unrecognised information. 


__The rest of the models__
The same logic applies respectively for the recognised blocks in the lexical entry by having a specific model for each one of them


*N.B*: The current architecture could change at any milestone of the project, as soon as new ideas or technical constraints emerge. 

## Input/Output

GROBID-Dictionaries takes as input lexical resources digitized in PDF format. Each model of the aforementioned components generates a TEI P5-encoded hierarchy of the different recognized text structures at that specific cascading level.


## Install, build, run

Building GROBID-Dictionaries requires gradle, maven and JDK 1.8.  

Clone/download the latest and most stable version of GROBID-parent under [this fork](https://github.com/MedKhem/grobid).

Copy the  dictionaries as sibling sub-project to grobid-core, grobid-trainer, etc.:
> cp -r grobid-dictionaries grobid/

Build GROBID-parent:
> cd PATH-TO-GROBID/grobid/

> ./gradlew clean install

Build Grobid-Dictionaries:
> cd PATH-TO-GROBID/grobid/grobid-dictionaries

> mvn clean install



**The models have to be trained before running the tests!**

## Training

Training a GROBID model is actually about creating new model trained on the available training data. And generating training data from new PDF documents using GROBID-dictionaries, to be corrected afterwards, needs the existence of a trained model (even slightly trained). We provide [toy data](https://github.com/MedKhem/grobid-dictionaries/tree/master/toyData/) to create first models for each segmentation level. 

For creating, training or re-training the models, the followings commands should be executed.

First rename the training data *skeletonwithToyData* directory to *resources*. 

Make sure that the current directory is grobid-dictionaries: 

> cd PATH-TO-GROBID/grobid/grobid-dictionaries

For **Dictionary Segmentation** model:

> mvn generate-resources -P train_dictionary_segmentation -e

For **Dictionary Body Segmentation** model:

> mvn generate-resources -P train_dictionary_body_segmentation -e

For **Lexical Entry** model:

> mvn generate-resources -P train_lexicalEntries -e

For **Form** model:

> mvn generate-resources -P train_form -e

For **Sense** model:

> mvn generate-resources -P train_sense -e

For the first stage model of processing etymology information (**EtymQuote** model):

> mvn generate-resources -P train_etymQuote -e

For the second stage model of processing etymology information (**Etym** model):

> mvn generate-resources -P train_etym -e


For the moment, the default training stop criteria are used. So, the training can be stopped manually after 1000 iterations, simply do a "control-C" to stop the training and save the model produced in the latest iteration. 1000 iterations are largely enough. Otherwise, the training will continue beyond several thousand iterations before stopping. 
The models will be saved respectively under ```grobid/grobid-home/models/MODEL_NAME```

## Generation of training data

Make sure that the current directory is grobid-dictionaries: 

> cd PATH-TO-GROBID/grobid/grobid-dictionaries

For **Dictionary Segmentation** model:

> java -jar PATH-TO-GROBID/grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -gH ../grobid-home/ -gP ../grobid-home/config/grobid.properties -dIn PATH_TO_THE_INPUT_PDF_FILE_OR_DIRECTORY  -dOut PATH-TO-OUTPUT-DIRECTORY -exe createTrainingDictionarySegmentation

For **Dictionary Body Segmentation** model:

> java -jar PATH-TO-GROBID/grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -gH ../grobid-home/ -gP ../grobid-home/config/grobid.properties -dIn PATH_TO_THE_INPUT_PDF_FILE_OR_DIRECTORY  -dOut PATH-TO-OUTPUT-DIRECTORY -exe createTrainingDictionaryBodySegmentation

For **Lexical Entry** model:

> java -jar PATH-TO-GROBIDt/grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -gH ../grobid-home/ -gP ../grobid-home/config/grobid.properties -dIn PATH_TO_THE_INPUT_PDF_FILE_OR_DIRECTORY  -dOut PATH-TO-OUTPUT-DIRECTORY -exe createTrainingLexicalEntry

For **Form** model:

> java -jar PATH-TO-GROBID/grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -gH ../grobid-home/ -gP ../grobid-home/config/grobid.properties -dIn PATH_TO_THE_INPUT_PDF_FILE_OR_DIRECTORY  -dOut PATH-TO-OUTPUT-DIRECTORY -exe createTrainingForm

For **Sense** model:

> java -jar PATH-TO-GROBID/grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -gH ../grobid-home/ -gP ../grobid-home/config/grobid.properties -dIn PATH_TO_THE_INPUT_PDF_FILE_OR_DIRECTORY  -dOut PATH-TO-OUTPUT-DIRECTORY -exe createTrainingSense

For **EtymQuote** model:

> java -jar PATH-TO-GROBIDt/grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -gH ../grobid-home/ -gP ../grobid-home/config/grobid.properties -dIn PATH_TO_THE_INPUT_PDF_FILE_OR_DIRECTORY  -dOut PATH-TO-OUTPUT-DIRECTORY -exe createTrainingEtymQuote

For **Etym** model:

> java -jar PATH-TO-GROBIDt/grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -gH ../grobid-home/ -gP ../grobid-home/config/grobid.properties -dIn PATH_TO_THE_INPUT_PDF_FILE_OR_DIRECTORY  -dOut PATH-TO-OUTPUT-DIRECTORY -exe createTrainingEtym

The above commands create training data to be annotated from scratch (files ending with *tei.xml*). 
It is possible also to generate pre-annotations using the current model, to be corrected afterwards (this mode is recommended when the model to be trained is becoming more precise). To do so, the latest token of the above commands should include *Annotated*. 
For example:  *createTrainingDictionarySegmentation* -> *createAnnotatedTrainingDictionarySegmentation*

The execution of any of the previous commands result in the generation of 5 files:
* inputFile **.rawtxt**: contains the raw text extracted from a the input file (not used for training)
* inputFile **.tei.xml**: contains gold standard segmentation of the input file (crucial for the training)
* inputFile **.modelname**: contains features corresponding to each line/token in the input file (crucial for the training). The beginning of each line in the feature matrix should be synchronised with each line/token in the **tei.xml**
* modelname **.css**: a stylesheet for a better rendering of *.tei.xml* elements in [Oxygen](https://www.oxygenxml.com)'s author mode (useful for annotation)
* modelname **.rng**: an xml syntax descriptor for a element suggestion applied to *.tei.xml* elements in [Oxygen](https://www.oxygenxml.com)'s author mode (useful for annotation)

The generated files should be included in the training dataset while the architecture of directories and files in the toy data directory is respected.

## Training data

GROBID-Dictionaries's the training data is encoded following the [TEI P5](http://www.tei-c.org/Guidelines/P5). See the [annotation guidelines page](https://github.com/MedKhem/grobid-dictionaries/wiki) for detailed explanations and examples concerning the best practices fro annotating the data.  

## Service mode (GUI)

After making sure that a model exists for each segmentation level you could start using the GUI to segment your PDF dictionaries

After running jetty with the following command:
> mvn -Dmaven.test.skip=true jetty:run-war

the web service would be accessible directly at the navigator (if you run it locally):
> http://localhost:8080/

For the time being, `Process Full Dictionary` corresponds to the cascading execution of all existing models to segment the input file. The rest of the labels corresponds to the execution of the first  segmentation models.   

## License

GROBID and GROBID-Dictionaries are distributed under [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0). 

Contact: Mohamed Khemakhem (<mohamed.khemakhem@inria.fr>), Patrice Lopez (<patrice.lopez@science-miner.com>), Luca Foppiano (<luca.foppiano@inria.fr>)
