This version of the tool has a new set of models and labels customised for parsing onomasiological content. In the following we detail each model and its corresponding labels to reach the [target encoding](img/targetEncoding.jpg). 

<h3>Models</h3>
**Dictionary Segmentation** This is the first model and has as goal the segmentation of each dictionary page into 3 main blocks: Headnote, Body and Footnote. Another block, "dictScarp" could be generated for text information that do not belong to the principal blocks

**Dictionary Body Segmentation** The second model gets the _Body_, recognized by the first model, and processes it to recognize the boundaries of each _concept_.

**Sub-Entry** The third model analyses each concept block, recognized by the second model, to extract _sub-entries_ or we can call them _lexical entries_. 

**Lexical Entry** The fourth model parses each _lexical entry_, recognized by the third model.

**Form** The fifth model parses each _Form_, which has been recognized in the previsous model as an antonym or a synonym.


<h3>Labels per Model</h3>
All models have two extra labels, in addition tio the labels listed below: **\<pc>** for punctuation/separation characters (basically any character that was not annotated manually) and **\<dictScrap>** for for text information that do not belong to the principal blocks.

For _Dictionary Segmentation:_ **\<headnote>**, **\<body>** and **\<footnote>**.

For _Dictionary Body Segmentation:_ **\<subEntry>** and **\<num>**

For _Sub-Entry:_ **\<entry>**, **\<xr>** and **\<num>** 

For _Lexical Entry:_ **\<synonym>**, **\<antonym>**, **\<num>** and **\<gramGrp>**

For _Form:_ **\<lemma>**, **\<prefix>**, **\<suffix>**, **\<def>**, **\<gloss>** and **\<xr>**


Note that a label used for annotation (e.g. \<synonym>) is going to be transformed in the final TEI output as \<form type="syn">