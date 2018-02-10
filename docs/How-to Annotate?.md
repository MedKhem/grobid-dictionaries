Welcome to the annotation guidelines of grobid-dictionaries!

## Generation of Pre-Annotated Data

It is highly recommended to use the automatic generation of pre-annotated data that grobid-dictionaries provides. This is due to the fact that the annotated data used for training and the final TEI ouput are slightly different. Line breaks, for instance, are captured in the training data but do not figure in the final TEI output. Moreover, copy/paste text from PDF documents can generate or modify some text, depending on the used operating system.

The annotation is then about moving, adding, removing TEI tags in the pre-annotated text. Text, spaces or line breaks must not be added or removed from the text to be annotated, extracted from the original document by grobid-dictionaries.
 



## Field Transition and Separators
The CRF models that GROBID uses, are very sensitive for each token that belongs to the labeled sequence. The following example illustrates how the raw text of lexical entry should be annotated:

>pacte [pakt] n. m. (lat.pactum; d e pacisci, faire un pacte). Accord, convention entre Etats ou entre particuliers :
>faire, rompre un pacte.
>• Pacte fédéral, 
>Constitution de la Suisse.


For this input, the following annotation should be respected. Note the position of the separators after the end of each field: 

![Example Pacte](pictures/pacte.xml.png)

"Why every separators can be untagged? Because it is the purpose and strength of the CRF model to learn that different separator tokens are transitions between two fields. There is no need to tell the CRF that something is a separator, because it is apparent from the actual data. It is also more efficient to label all the separator with a neutral label" [*Patrice Lopez*](https://github.com/kermitt2)

In the case of grobid-dictionaries, these separators could be just not annotated and then they will be automatically considered as tagged with \<pc> element. In case, of automatically generated annotated data, these seperators are annotated with <pc> element. 
A \<pc> tag should be removed, if the seperator (dot, coma, pipes,..) do not represent a transition between two fields. 

## Cascading Annotation

Grobid does not support the sequence annotation of nested structures. This implies the need to have annotated data that do not contain nested TEI elements (the root element of the level and \<lb/> do not count).
In the follwing sections, an example of annotation of raw document and raw text could be followed for each level:

### Dictionary Segmentation
Four TEI elements could be used in this level:

* \<header>: surrounds the header block of a dictionary page. Such a block could contain, for instance, the page number, the first and the last headword on the current page,...
* \<body>: defines the limits of the main part of the dictionary that contains the lexical entries
* \<footer>: surrounds the footer block of a dictionary page. Such a block could contain, for instance, the page number, the first and the last headword on the current page,...
* \<dictScrap>: could be used to annotate any text sequence that does not belong to any of the previous blocks. 


*N:B:* The TEI body element is used for a different purpose in this level. But for the structured output, it is replaced by the convenient TEI element : \<ab>   

### Dictionary Body Segmentation
Two TEI elements could be used in this level:

* \<entry>: defines the limits of each lexical entries
* \<dictScrap>: could be used to annotate any text sequence that does not represent a lexical entry. 

### Lexical Entry
Five TEI elements could be used in this level:

* \<form>: surrounds the grammatical and morphological information related to **the headword** such as POS and pronunciation. Information related to the sense level, should not be annotated at this level
* \<etym>: surrounds the etymology information of the headword, such as etymon and origin of the word.
* \<sense>: is placed at the border of the **sense block**. Information like sense definitions, examples, synonyms are typical content of this block.  
* \<re>: is used to annotate related entries and their belonging information. Such information should not be annotated at this level, if it is included in a sense block
* \<dictScrap>: could be used to annotate any text sequence that does not belong to any of the previous blocks. 

The following examples illustrate how these annotations could be applied to the raw text of a lexical entry:
#### Example 1: 

![Example LE](pictures/aloneBasicEnglish.png)

**Source:** *Easier English Basic Dictionary-Second Edition (page 9)*

The corresponding annotation to the above excerpt should look like: 


![Example LE](pictures/aloneXML.png)

Note here the \<pc> element in the beginning of the entry, which annotates an anomaly related to the codification of headwords in bold in this sample.
According to the markup system of this dictionary, **leave alone** and **get it alone** are related entries that represent idioms. This means that they are related to the whole entry and consequently should be annotated at this level.

#### Example 2:
![argument](pictures/arrest.png)

**Source:** *Easier English Basic Dictionary-Second Edition (page 16)*


The corresponding annotation to the above excerpt should look like: 


![kh](pictures/arrestXML.png)

Note here, the related entry like **under arrest** is not annotated at this level, since it belongs to the sense of the lexical entry as a noun.   

N:B: As shown in the examples, for all the levels, \<pc> element could be used to annotate separators or punctuation outside the principal fields of the level.

## Choice of the Data to Annotate 

The data to annotate should varied as much as possible to train the model on different scenarios and exceptional cases

### Dictionary Segmentation

The pages to annotate should not be annotated separately. In other terms, a set of pages should be annotated at once. The different annotated sets should contain at least one starting pair page and one impair page. This is not related to the page number itself, rather to the position of the text on the each category of the pages. 

### Dictionary Body Segmentation

The main p

### Lexical Entry

The pages to