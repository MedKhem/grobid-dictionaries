## Training

Create/train the models with the toyData by running these commands

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
> mvn generate-resources -P train_subEntries -e
```
For **Lexical Entry** model, run:
```bash
> mvn generate-resources -P train_lexicalEntries -e
```
For **Form** model, run:
```bash
> mvn generate-resources -P train_form -e
```




8. Annotate your files 

9. Move your **tei.xml** files under your _toyData/MODEL_NAME/corpus/tei_ directory and the rest (except rng and css files) under your _toyData/MODEL_NAME/corpus/raw_ directory  

10. Train the model (step 5)

11. Don't forget to put the same files under evaluation. **tei.xml** files under your _toyData/MODEL_NAME/evaluation/tei_ directory and the rest (except rng and css files) under your _toyData/MODEL_NAME/evaluation/raw_ directory. If you have carried out your annotation correctly, you must see 100% in your the evaluation table displayed at the end of the model training  

12. Run the web app to see the result 
