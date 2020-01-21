## Training data

GROBID-Dictionaries's training data is encoded following the [TEI P5](http://www.tei-c.org/Guidelines/P5). See the [annotation guidelines page](https://github.com/MedKhem/grobid-dictionaries/wiki) for detailed explanations and examples concerning the best practices fro annotating the data.  


## Generation of training data

To generate training data from your dictionary, copy the [pdf directory](https://drive.google.com/drive/folders/1I83_WJeDBwP_076U3OHv_LryiiRV1w9F?usp=sharing) corresponding to the target model and paste it under the corresponding model location under your toyData.   
   
For **Dictionary Segmentation** model:
```bash
> java -jar /grobid/grobid-dictionaries/target/grobid-dictionaries-0.5.4-SNAPSHOT.one-jar.jar -dIn resources/DIRECTORY_OF_YOUR_PDF  -dOut resources -exe createTrainingDictionarySegmentation
```
For **Dictionary Body Segmentation** model:
```bash
> java -jar /grobid/grobid-dictionaries/target/grobid-dictionaries-0.5.4-SNAPSHOT.one-jar.jar -dIn resources/DIRECTORY_OF_YOUR_PDF  -dOut resources -exe createTrainingDictionaryBodySegmentation
```
For **Sub-Entry** model:
```bash
> java -jar /grobid/grobid-dictionaries/target/grobid-dictionaries-0.5.4-SNAPSHOT.one-jar.jar -dIn resources/DIRECTORY_OF_YOUR_PDF  -dOut resources -exe createTrainingSubEntry
```
For **Lexical Entry** model:
```bash
> java -jar /grobid/grobid-dictionaries/target/grobid-dictionaries-0.5.4-SNAPSHOT.one-jar.jar -dIn resources/DIRECTORY_OF_YOUR_PDF  -dOut resources -exe createTrainingLexicalEntry
```
For **Form** model:
```bash
> java -jar /grobid/grobid-dictionaries/target/grobid-dictionaries-0.5.4-SNAPSHOT.one-jar.jar -dIn resources/DIRECTORY_OF_YOUR_PDF  -dOut resources -exe createTrainingForm
```

* If you are using macOS, you might need to remove './DS_Store' file, which blocks the jar to run (thniking that it's a pdf)

* Note also the choice of the pages is also imported: it should be varied

* The above commands create training data to be annotated from scratch (files ending with *tei.xml*). 
It is possible also to generate pre-annotations using the current model, to be corrected afterwards (this mode is recommended when the model to be trained is becoming more precise). To do so, the latest token of the above commands should include *Annotated*. 
For example:  *createTrainingDictionarySegmentation* -> *createAnnotatedTrainingDictionarySegmentation*
