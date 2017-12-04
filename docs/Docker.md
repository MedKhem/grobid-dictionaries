
1. Get docker
 * For Windows Users download from [this link](https://docs.docker.com/toolbox/toolbox_install_windows/)
 * For macOS Users download stable version from [this link](https://docs.docker.com/docker-for-mac/install/)
 * For macOS Users follow the instructions in [this link](https://docs.docker.com/engine/installation/linux/docker-ce/ubuntu/)
 

2. You need the 'toyData' directory to create dummy models. You could get it from the [github repository](https://github.com/MedKhem/grobid-dictionaries)
 

3. We could now run our image and having the 'toyData' and 'resources' as shared volumes between your machine and the container. Whatever you do in on of these directories, it's applied to both of them

* For macOS users:
```bash
docker run -v PATH_TO_YOUR_TOYDATA/toyData:/grobid/grobid-dictionaries/resources -p 8080:8080 -it medkhem/grobid-dictionaries:1 bash```
```

* For Windows users: 
```bash
docker run -v //c/Users/YOUR_USERNAME/Desktop/toyData:/grobid/grobid-dictionaries/resources -p 8080:8080 -it medkhem/grobid-dictionaries:1 bash```
```
4. Create the first three models with the toyData by running these commands

For **Dictionary Segmentation** model:
```bash
> mvn generate-resources -P train_dictionary_segmentation -e
```
For **Dictionary Body Segmentation** model:
```bash
> mvn generate-resources -P train_dictionary_body_segmentation -e
```
For **Lexical Entry** model:
```bash
> mvn generate-resources -P train_lexicalEntries -e
```
For **Form** model:
```bash
> mvn generate-resources -P train_form -e
```
For **Sense** model:
```bash
> mvn generate-resources -P train_sense -e
```
For the first stage model of processing etymology information (**EtymQuote** model):
```bash
> mvn generate-resources -P train_etymQuote -e
```
For the second stage model of processing etymology information (**Etym** model):
```bash
> mvn generate-resources -P train_etym -e
```


5. Run the web service to see the output of the dummy models 

``````bash
mvn -Dmaven.test.skip=true jetty:run-war
   ``````
You can see the running application in your web browser under:
```http://localhost:8080```

To shutdown the server, you need to press 
```ctrl + c```
 
6. To generate training data from your dictionary, copy the pdf directory corresponding to the target model and paste it under the corresponding model location under your toyData.   
   
For **Dictionary Segmentation** model:
```bash
> java -jar /grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -dIn PATH_TO_THE_INPUT_PDF_FILE_OR_DIRECTORY  -dOut PATH-TO-OUTPUT-DIRECTORY -exe createTrainingDictionarySegmentation
```
For **Dictionary Body Segmentation** model:
```bash
> java -jar grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -dIn PATH_TO_THE_INPUT_PDF_FILE_OR_DIRECTORY  -dOut PATH-TO-OUTPUT-DIRECTORY -exe createTrainingDictionaryBodySegmentation
```
For **Lexical Entry** model:
```bash
> java -jar /grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -dIn PATH_TO_THE_INPUT_PDF_FILE_OR_DIRECTORY  -dOut PATH-TO-OUTPUT-DIRECTORY -exe createTrainingLexicalEntry
```
For **Form** model:
```bash
> java -jar /grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -dIn PATH_TO_THE_INPUT_PDF_FILE_OR_DIRECTORY  -dOut PATH-TO-OUTPUT-DIRECTORY -exe createTrainingForm
```
For **Sense** model:
```bash
> java -jar /grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -dIn PATH_TO_THE_INPUT_PDF_FILE_OR_DIRECTORY  -dOut PATH-TO-OUTPUT-DIRECTORY -exe createTrainingSense
```
For **EtymQuote** model:
```bash
> java -jar /grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -dIn PATH_TO_THE_INPUT_PDF_FILE_OR_DIRECTORY  -dOut PATH-TO-OUTPUT-DIRECTORY -exe createTrainingEtymQuote
```
For **Etym** model:
```bash
> java -jar /grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -dIn PATH_TO_THE_INPUT_PDF_FILE_OR_DIRECTORY  -dOut PATH-TO-OUTPUT-DIRECTORY -exe createTrainingEtym
```

* If you are using macOS, you might need to remove './DS_Store' file, which blocks the jar to run (thniking that it's a pdf)

* Note also the choice of the pages is also imported: it should be varied

* The above commands create training data to be annotated from scratch (files ending with *tei.xml*). 
It is possible also to generate pre-annotations using the current model, to be corrected afterwards (this mode is recommended when the model to be trained is becoming more precise). To do so, the latest token of the above commands should include *Annotated*. 
For example:  *createTrainingDictionarySegmentation* -> *createAnnotatedTrainingDictionarySegmentation*

7. Annotate your files 

8. Move your **tei.xml** files under tei directory and the rest (except rng and css files) under **raw**  

9. Train the first model

10. Don't forget to put the same files under evaluation

11. Run the web app to see the result 