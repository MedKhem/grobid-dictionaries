## Install, build, run

Building GROBID-Dictionaries requires gradle, maven and JDK 1.8.  

Clone/download the latest and most stable version of GROBID-parent under [this fork](https://github.com/MedKhem/grobid).

Copy the  dictionaries as sibling sub-project to grobid-core, grobid-trainer, etc.:
```bash
> cp -r grobid-dictionaries grobid/
```

Build GROBID-parent:
```bash
> cd PATH-TO-GROBID/grobid/

> ./gradlew clean install
```

Build Grobid-Dictionaries:
```bash
> cd PATH-TO-GROBID/grobid/grobid-dictionaries

> mvn -Dmaven.test.skip=true clean install
```