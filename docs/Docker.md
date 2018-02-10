
1. Get docker 
 * For Windows 7 and 8 Users download from [this link](https://docs.docker.com/toolbox/toolbox_install_windows/)
 * For Windows 10 and later Users download (from Stable Channel) from [this link](https://docs.docker.com/docker-for-windows/install/)
 * For macOS Users download (from Stable Channel) from [this link](https://docs.docker.com/docker-for-mac/install/)
 * For Linux (Ubuntu) Users follow the instructions in [this link](https://docs.docker.com/engine/installation/linux/docker-ce/ubuntu/)
 
2. Run in your terminal (For Windows 7 and 8, run in Quickstart terminal / For Windows 10, run in Command Prompt) 
```bash
docker pull medkhem/grobid-dictionaries
```
3. You need the 'toyData' directory to create dummy models. You could get it from the [github repository](https://github.com/MedKhem/grobid-dictionaries)
 

4. We could now run our image and having the 'toyData' and 'resources' as shared volumes between your machine and the container. Whatever you do in on of these directories, it's applied to both of them

* For macOS users:
```bash
docker run -v PATH_TO_YOUR_TOYDATA/toyData:/grobid/grobid-dictionaries/resources -p 8080:8080 -it medkhem/grobid-dictionaries bash
```

* For Windows users: 
```bash
docker run -v //c/Users/YOUR_USERNAME/Desktop/toyData:/grobid/grobid-dictionaries/resources -p 8080:8080 -it medkhem/grobid-dictionaries bash
```
5. Create/train the first models with the toyData by running these commands

For **Dictionary Segmentation** model, run:
```bash
> mvn generate-resources -P train_dictionary_segmentation -e
```
For **Dictionary Body Segmentation** model, run:
```bash
> mvn generate-resources -P train_dictionary_body_segmentation -e
```
For **Lexical Entry** model, run:
```bash
> mvn generate-resources -P train_lexicalEntries -e
```
For **Form** model, run:
```bash
> mvn generate-resources -P train_form -e
```
For **Sense** model, run:
```bash
> mvn generate-resources -P train_sense -e
```
For the first stage model of processing etymology information (**EtymQuote** model), run:
```bash
> mvn generate-resources -P train_etymQuote -e
```
For the second stage model of processing etymology information (**Etym** model), run:
```bash
> mvn generate-resources -P train_etym -e
```


6. Run the web service to see the output of the dummy models 

```bash
> mvn -Dmaven.test.skip=true jetty:run-war
   ```
You can see the running application in your web browser: 

* For Windows, your 8080 port should be free to see the web application on the address:
```http://192.168.99.100:8080```

* For MacOs, the web application is running on the address:   
```http://localhost:8080```

To shutdown the server, you need to press 
```ctrl + c```
 
7. To generate training data from your dictionary, copy the [pdf directory](https://drive.google.com/drive/folders/1I83_WJeDBwP_076U3OHv_LryiiRV1w9F?usp=sharing) corresponding to the target model and paste it under the corresponding model location under your toyData.   
   
For **Dictionary Segmentation** model:
```bash
> java -jar /grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -dIn resources/DIRECTORY_OF_YOUR_PDF  -dOut resources -exe createTrainingDictionarySegmentation
```
For **Dictionary Body Segmentation** model:
```bash
> java -jar /grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -dIn resources/DIRECTORY_OF_YOUR_PDF  -dOut resources -exe createTrainingDictionaryBodySegmentation
```
For **Lexical Entry** model:
```bash
> java -jar /grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -dIn resources/DIRECTORY_OF_YOUR_PDF  -dOut resources -exe createTrainingLexicalEntry
```
For **Form** model:
```bash
> java -jar /grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -dIn resources/DIRECTORY_OF_YOUR_PDF  -dOut resources -exe createTrainingForm
```
For **Sense** model:
```bash
> java -jar /grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -dIn resources/DIRECTORY_OF_YOUR_PDF  -dOut resources -exe createTrainingSense
```
For **EtymQuote** model:
```bash
> java -jar /grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -dIn resources/DIRECTORY_OF_YOUR_PDF  -dOut resources -exe createTrainingEtymQuote
```
For **Etym** model:
```bash
> java -jar /grobid/grobid-dictionaries/target/grobid-dictionaries-0.4.3-SNAPSHOT.one-jar.jar -dIn resources/DIRECTORY_OF_YOUR_PDF  -dOut resources -exe createTrainingEtym
```

* If you are using macOS, you might need to remove './DS_Store' file, which blocks the jar to run (thniking that it's a pdf)

* Note also the choice of the pages is also imported: it should be varied

* The above commands create training data to be annotated from scratch (files ending with *tei.xml*). 
It is possible also to generate pre-annotations using the current model, to be corrected afterwards (this mode is recommended when the model to be trained is becoming more precise). To do so, the latest token of the above commands should include *Annotated*. 
For example:  *createTrainingDictionarySegmentation* -> *createAnnotatedTrainingDictionarySegmentation*

8. Annotate your files 

9. Move your **tei.xml** files under your _toyData/MODEL_NAME/corpus/tei_ directory and the rest (except rng and css files) under your _toyData/MODEL_NAME/corpus/raw_ directory  

10. Train the model (step 5)

11. Don't forget to put the same files under evaluation. **tei.xml** files under your _toyData/MODEL_NAME/evaluation/tei_ directory and the rest (except rng and css files) under your _toyData/MODEL_NAME/evaluation/raw_ directory. If you have carried out your annotation correctly, you must see 100% in your the evaluation table displayed at the end of the model training  

12. Run the web app to see the result 