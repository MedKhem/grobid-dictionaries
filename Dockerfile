FROM openjdk:8

MAINTAINER Mohamed Khemakhem <mohamed.khemakhem@inria.fr>

RUN \
  export DEBIAN_FRONTEND=noninteractive && \
  sed -i 's/# \(.*multiverse$\)/\1/g' /etc/apt/sources.list && \
  apt-get update && \
  apt-get -y upgrade

#RUN \
#   wget -q https://services.gradle.org/distributions/gradle-3.3-bin.zip \
#    && unzip gradle-3.3-bin.zip -d /opt \
#    && rm gradle-3.3-bin.zip
#
#ENV GRADLE_HOME /opt/gradle-3.3
#ENV PATH $PATH:/opt/gradle-3.3/bin

RUN \
  apt-get install -y --no-install-recommends software-properties-common && \
  apt-get install -y vim wget curl git maven

#To clone from fork
 RUN \
  git clone https://github.com/MedKhem/grobid
#  && \
#  cd grobid && ./gradlew clean install

# To copy from a local directory
#COPY grobid-master.zip grobid-master.zip
#RUN unzip grobid-master.zip && mv grobid-master grobid && rm grobid-master.zip && cd #grobid && ./gradlew clean install

RUN rm -r /grobid/grobid-service/ && rm -r /grobid/grobid-trainer/resources/

#To clone from fork
 RUN \
  cd /grobid && \
  git clone https://github.com/MedKhem/grobid-dictionaries
#
## To copy from a local directory
##COPY grobid-dictionaries /grobid/grobid-dictionaries
#
#
RUN \
  cd /grobid/grobid-dictionaries && \
  mv toyData resources && \
mvn install:install-file -Dfile=grobidDependencies/grobid-core-0.5.4-SNAPSHOT.jar -DgroupId=org.grobid -DartifactId=grobid-core -Dversion=0.5.4-SNAPSHOT -Dpackaging=jar && \
mvn install:install-file -Dfile=grobidDependencies/grobid-trainer-0.5.4-SNAPSHOT.jar -DgroupId=org.grobid -DartifactId=grobid-trainer -Dversion=0.5.4-SNAPSHOT -Dpackaging=jar && \
 mvn -Dmaven.test.skip=true clean install && \
 mvn generate-resources -P train_dictionary_segmentation -e && \
 mvn generate-resources -P train_dictionary_body_segmentation -e && \
 mvn generate-resources -P train_lexicalEntries -e && \
 mvn generate-resources -P train_form -e && \
 mvn generate-resources -P train_sense -e && \
 mvn generate-resources -P train_etymQuote -e && \
 mvn generate-resources -P train_etym -e && \
 mvn -Dmaven.test.skip=true jetty:run-war && \
 kill -INT 888


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