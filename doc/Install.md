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