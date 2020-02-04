#!/usr/local/bin/bash
#run in the terminal sh evaluate.sh set -e
#for now, working only for the first model

#initiate an array with all possible grobidCommandName

# treat "train_dictionary_segmentation" later
declare -a arrCommand=( "train_dictionary_segmentation" "train_dictionary_body_segmentation" "train_lexicalEntries"
"train_form" "train_gramGrp" "train_sense" "train_sub_sense")
#
#declare -a arrFeature=("train_dictionary_segmentation-segmentation" "train_dictionary_body_segmentation" "train_lexicalEntries"
#"train_form" "train_gramGrp" "train_sense" "train_sub_sense")




#read dictionary name
read -r dictName<"resources/dataset/dictName.txt"

# for each model
for i in "${arrCommand[@]}"
do
   echo "processing model $i"
   #initiate model name
   case "$i" in
   "train_dictionary_segmentation") modelName="dictionary-segmentation" modelExtension="dictionarySegmentation" ;;
   "train_dictionary_body_segmentation") modelName="dictionary-body-segmentation" modelExtension="dictionaryBodySegmentation" ;;
   "train_lexicalEntries") modelName="lexical-entry" modelExtension="lexicalEntry" ;;
   "train_form") modelName="form" modelExtension="form" ;;
   "train_gramGrp") modelName="gramGrp" modelExtension="gramGrp" ;;
   "train_sense") modelName="sense" modelExtension="sense" ;;
   "train_sub_sense") modelName="sub-sense" modelExtension="subSense" ;;
   *) modelName "$status" ;;
   esac

   srcdir="resources/dataset/${modelName}/corpus/batches"
   dstdir="resources/dataset/${modelName}/corpus/tei"

    evalTeidir="resources/dataset/${modelName}/evaluation/tei"
    evalRawdir="resources/dataset/${modelName}/evaluation/raw"
    dstRawdir="resources/dataset/${modelName}/corpus/raw"

    # make sure all the raw files are included in training and evaluation datasets
     cp -R ${evalRawdir}/*.${modelExtension} ${dstRawdir} && echo "evaluation sense1 raw files copied " || echo "couldn't copy evaluation raw"
     cp -R ${dstRawdir}/*.${modelExtension} ${evalRawdir} && echo "evaluation sense2 raw files copied " || echo "couldn't copy evaluation raw"

       #first, clean the tei repo
       rm ${dstdir}/*
   ##Feature aspects experiment

   #copy all batches for training
   for batch in {1..4}
   do

    #copy files from the current batch
       cp -R ${srcdir}/${batch}/*.tei.xml ${dstdir} && echo "files copied from batch $batch" || echo "couldn't copy batches"

   done

   #copy unigram template file and rename it the same as the models
   if [[ "${modelName}" == "dictionary-segmentation" ]] ; then
            cp resources/expe/basicUnigram/dictionary-segmentation.template resources/dataset/${modelName}/crfpp-templates/${modelName}.template
       else
            cp resources/expe/basicUnigram/lexical-entry.template resources/dataset/${modelName}/crfpp-templates/${modelName}.template
   fi

   #run the training
   mvn generate-resources -P ${i} -e -Dexec.args="$dictName Unigram  5"


   #copy bigram template file and rename it the same as the models
   if [[ "${modelName}" == "dictionary-segmentation" ]] ; then
       cp resources/expe/basicUnigram/dictionary-segmentation.template resources/dataset/${modelName}/crfpp-templates/${modelName}.template
       else
   cp resources/expe/basicBigram/lexical-entry.template resources/dataset/${modelName}/crfpp-templates/${modelName}.template
   fi

   #run the training
   mvn generate-resources -P ${i} -e -Dexec.args="$dictName Bigram 5"

   #copy engineered template file and rename it the same as the models
   if [[ "${modelName}" == "dictionary-segmentation" ]] ; then
       cp resources/expe/basicUnigram/dictionary-segmentation.template resources/dataset/${modelName}/crfpp-templates/${modelName}.template
       else
   cp resources/expe/bigramEngineered/lexical-entry.template resources/dataset/${modelName}/crfpp-templates/${modelName}.template
   fi

   #run the training
    mvn generate-resources -P ${i} -e -Dexec.args="$dictName Engineered  5"

# To activate this when the automation includes generation of training data or parsing but not for traning
#   #copy all files in the evaluation to the training dataset
#
#    cp -R ${evalTeidir}/*.tei.xml ${dstdir} && echo "evaluation tei files copied " || echo "couldn't copy evaluation tei"
#    cp -R ${evalRawdir}/*.${modelExtension} ${dstRawdir} && echo "evaluation raw files copied " || echo "couldn't copy evaluation raw"
#
#   #run again the training to strengthen the model before passing to the following model (without evaluation reporting)
#   mvn generate-resources -P ${i} -e

   ##Learning curve experiment (based on the bigram feature engineered)
    #first, clean the tei repo
       rm ${dstdir}/*
for batch in {1..4}
   do
#check if the batch has tei files
    FILE=${srcdir}/${batch}/*.xml


count=`ls -1 $FILE 2>/dev/null | wc -l`
if [ $count != 0 ]
then

  #copy files from the current batch
       cp -R ${srcdir}/${batch}/*.tei.xml ${dstdir} && echo "files copied from batch $batch" || echo "couldn't copy batches"

  #copy unigram template file and rename it the same as the models
   if [[ "${modelName}" == "dictionary-segmentation" ]] ; then
            cp resources/expe/basicUnigram/dictionary-segmentation.template resources/dataset/${modelName}/crfpp-templates/${modelName}.template
       else
            cp resources/expe/basicUnigram/lexical-entry.template resources/dataset/${modelName}/crfpp-templates/${modelName}.template
   fi

   #run the training
   mvn generate-resources -P ${i} -e -Dexec.args="$dictName Unigram  $batch"

   #copy bigram template file and rename it the same as the models
   if [[ "${modelName}" == "dictionary-segmentation" ]] ; then
       cp resources/expe/basicBigram/dictionary-segmentation.template resources/dataset/${modelName}/crfpp-templates/${modelName}.template
       else
   cp resources/expe/basicBigram/lexical-entry.template resources/dataset/${modelName}/crfpp-templates/${modelName}.template
   fi

   #run the training
   mvn generate-resources -P ${i} -e -Dexec.args="$dictName Bigram $batch"

   #copy engineered template file and rename it the same as the models
   if [[ "${modelName}" == "dictionary-segmentation" ]] ; then
       cp resources/expe/bigramEngineered/dictionary-segmentation.template resources/dataset/${modelName}/crfpp-templates/${modelName}.template
       else
   cp resources/expe/bigramEngineered/lexical-entry.template resources/dataset/${modelName}/crfpp-templates/${modelName}.template
   fi

   #run the training
    mvn generate-resources -P ${i} -e -Dexec.args="$dictName Engineered $batch"



fi


   done
  #copy all files in the evaluation to the training dataset

    cp -R ${evalTeidir}/*.tei.xml ${dstdir} && echo "evaluation tei files copied " || echo "couldn't copy evaluation tei"
    cp -R ${evalRawdir}/*.${modelExtension} ${dstRawdir} && echo "evaluation raw files copied " || echo "couldn't copy evaluation raw"



 #run again the training to strengthen the model before passing to the following model (without evaluation reporting)
   mvn generate-resources -P ${i} -e

done





