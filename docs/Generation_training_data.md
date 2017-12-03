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
