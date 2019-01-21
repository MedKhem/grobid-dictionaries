# GROBID-Dictionaries

[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Version](https://img.shields.io/badge/version-0.4.3--SNAPSHOT-pink.svg)](https://github.com/MedKhem/grobid-dictionaries/blob/master/README.md)
[![Documentation Status](https://readthedocs.org/projects/grobid-dictionaries/badge/?version=latest)](https://readthedocs.org/projects/grobid-dictionaries/?badge=latest) 
[![Docker build](https://dockerbuildbadges.quelltext.eu/status.svg?organization=medkhem&repository=grobid-dictionaries)](https://hub.docker.com/r/medkhem/grobid-dictionaries/builds/)
## Purpose

GROBID-Dictionaries is a GROBID sub-module, implementing a java machine learning library, for structuring digitized lexical resources and entry-based documents with encyclopedic or bibliographic content. It allows the parsing, extraction and structuring of text information in such resources. 

## Approach

GROBID-Dictionaries is based on cascading CRF models. The diagram below presents the architecture enabling the processing and the transfer of the text information through the models.

![GROBID Dictionaries Structure](docs/img/modelsIn.png)

__Dictionary Segmentation__
This is the first model and has as goal the segmentation of each dictionary page into 3 main blocks: Headnote, Body and Footnote. Another block, "dictScarp" could be generated for text information that do not belong to the principal blocks

__Dictionary Body Segmentation__
The second model gets the Body, recognized by the first model, and processes it to recognize the boundaries of each lexical entry.

__Lexical Entry__
The third model parses each lexical entry, recognized by the second model, to segment it into 4 main blocks: Form, Etymology, Senses, Related Entries. A "dictScrap" block is there as well for unrecognised information. 


__The rest of the models__
The same logic applies respectively for the recognised blocks in a lexical entry by having a specific model for each one of them


*N.B*: The current architecture could change at any milestone of the project, as soon as new ideas or technical constraints emerge. 

## Input/Output

GROBID-Dictionaries takes as input lexical resources digitized in PDF format. Each model of the aforementioned components generates a TEI P5-encoded hierarchy of the different recognized text structures at that specific cascading level.

## Docker Use
To shortcut the installation of the tool, the [Docker manual](https://github.com/MedKhem/grobid-dictionaries/wiki/Docker_Instructions) could be followed to use the latest image of the tool

## To Cite

Mohamed Khemakhem, Luca Foppiano, Laurent Romary. Automatic Extraction of TEI Structures in Digitized Lexical Resources using Conditional Random Fields. electronic lexicography, eLex 2017, Sep 2017, Leiden, Netherlands. [hal-01508868v2](https://hal.archives-ouvertes.fr/hal-01508868v2)

Mohamed Khemakhem, Axel Herold, Laurent Romary. Enhancing Usability for Automatically Structuring Digitised Dictionaries. GLOBALEX workshop at LREC 2018, May 2018, Miyazaki, Japan. 2018.  [hal-01708137v2](https://hal.archives-ouvertes.fr/hal-01708137v2)

## More Reading
Hervé Bohbot, Francesca Frontini, Giancarlo Luxardo, Mohamed Khemakhem, Laurent Romary. Presenting the Nénufar Project: a Diachronic Digital Edition of the Petit Larousse Illustré. GLOBALEX 2018 - Globalex workshop at LREC2018, May 2018, Miyazaki, Japan. [hal-01728328](https://hal.archives-ouvertes.fr/hal-01728328v1)

Mohamed Khemakhem, Carmen Brando, Laurent Romary, Frédérique Mélanie-Becquet, Jean-Luc Pinol. Fueling Time Machine: Information Extraction from Retro-Digitised Address Directories. JADH2018 "Leveraging Open Data", Sep 2018, Tokyo, Japan.  [hal-01814189](https://hal.archives-ouvertes.fr/hal-01814189v1)

Mohamed Khemakhem, Laurent Romary, Simon Gabay, Hervé Bohbot, Francesca Frontini, et al.. Automatically Encoding Encyclopedic-like Resources in TEI. The annual TEI Conference and Members Meeting, Sep 2018, Tokyo, Japan.[hal-01819505](https://hal.archives-ouvertes.fr/hal-01819505v1)

David Lindemann, Mohamed Khemakhem, Laurent Romary. Retro-digitizing and Automatically Structuring a Large Bibliography Collection. European Association for Digital Humanities (EADH) Conference, Dec 2018, Galway, Ireland. [hal-01941534](https://hal.archives-ouvertes.fr/hal-01941534v1)

## Documentation
For more expert and development uses, the documentation of the tool is detailed [here](http://grobid-dictionaries.readthedocs.io/en/latest/)



## Contact
Mohamed Khemakhem (<mohamed.khemakhem@inria.fr>), Laurent Romary (<laurent.romary@inria.fr>)
