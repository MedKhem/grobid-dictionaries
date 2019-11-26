FROM openjdk:8

MAINTAINER Mohamed Khemakhem <mohamed.khemakhem@inria.fr>

ARG GROBID_VERSION=0.5.6-SNAPSHOT

RUN \
  export DEBIAN_FRONTEND=noninteractive && \
  sed -i 's/# \(.*multiverse$\)/\1/g' /etc/apt/sources.list && \
  apt-get update && \
  apt-get -y upgrade

RUN \
   wget -q https://services.gradle.org/distributions/gradle-3.3-bin.zip \
    && unzip gradle-3.3-bin.zip -d /opt \
    && rm gradle-3.3-bin.zip


RUN \
  apt-get install -y --no-install-recommends software-properties-common && \
  apt-get install -y vim wget curl git maven

#To clone from fork
RUN \
 # Use original grobid repo for now
  git clone https://github.com/MedKhem/grobid

# To copy from a local directory
#COPY grobid-master.zip grobid-master.zip
#COPY ../grobid /grobid
#RUN unzip grobid-master.zip && mv grobid-master grobid && rm grobid-master.zip && cd #grobid && ./gradlew clean install

RUN rm -r /grobid/grobid-service/ && rm -r /grobid/grobid-trainer/resources/ && rm -r /grobid/grobid-home/models/*  && rm -r /grobid/grobid-core/*

#To clone from fork
RUN \
  cd /grobid && git clone -b basNum --single-branch https://github.com/MedKhem/grobid-dictionaries

## To copy from a local directory
##COPY grobid-dictionaries /grobid/grobid-dictionaries


RUN \
  cd /grobid/grobid-dictionaries && \
  mv toyData resources && \
mvn install:install-file -Dfile=grobidDependencies/grobid-core-$GROBID_VERSION.jar -DgroupId=org.grobid -DartifactId=grobid-core -Dversion=$GROBID_VERSION -Dpackaging=jar && \
mvn install:install-file -Dfile=grobidDependencies/grobid-trainer-$GROBID_VERSION.jar -DgroupId=org.grobid -DartifactId=grobid-trainer -Dversion=$GROBID_VERSION -Dpackaging=jar && \
 mvn -Dmaven.test.skip=true clean install && \
 mvn generate-resources -P train_dictionary_segmentation -e && \
 mvn generate-resources -P train_dictionary_body_segmentation -e && \
 mvn generate-resources -P train_lexicalEntries -e && \
 mvn generate-resources -P train_form -e && \
 mvn generate-resources -P train_sense -e && \
 mvn generate-resources -P train_etymQuote -e && \
 mvn generate-resources -P train_etym -e && \
 mvn generate-resources -P train_sub_sense -e && \
  mvn generate-resources -P train_gramGrp -e && \
 mvn generate-resources -P train_crossRef -e

# && \
# mvn -Dmaven.test.skip=true jetty:run-war && \
# kill -INT 888


WORKDIR /grobid/grobid-dictionaries
EXPOSE 8080


############Useful commands

##See images
# docker images -a

##See containers
# docker ps -a

##To Stop all containers
#docker stop $(docker ps -qa)

##To remove alla containers
#docker rm $(docker ps -qa)

##To remove all images
#docker rmi $(docker images -qa)

##To build an image inside forbid
#docker build -f FirstGrobidDictionaries -t docker-grobid-training .

##To run a container based on the created image inside forbid
#docker run docker-grobid-training:latest