## Training

Training a GROBID model is actually about creating new model trained on the available training data. And generating training data from new PDF documents using GROBID-dictionaries, to be corrected afterwards, needs the existence of a trained model (even slightly trained). We provide [toy data](https://github.com/MedKhem/grobid-dictionaries/tree/master/toyData/) to create first models for each segmentation level. 

For creating, training or re-training the models, the followings commands should be executed.

First rename the training data *toyData* directory to *resources*. 

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
