git checkout basnage && \

git pull && \

mvn clean install -DskipTests && \

cp resources/dataset/dictionary-segmentation/model.wapiti /grobid/grobid-home/models/dictionary-segmentation/  && \

cp resources/dataset/dictionary-body-segmentation/model.wapiti /grobid/grobid-home/models/dictionary-body-segmentation/ && \

cp resources/dataset/lexical-entry/model.wapiti /grobid/grobid-home/models/lexical-entry/ && \

cp resources/dataset/form/model.wapiti /grobid/grobid-home/models/form/ && \

cp resources/dataset/gramGrp/model.wapiti /grobid/grobid-home/models/gramGrp/ && \

cp resources/dataset/sense/model.wapiti /grobid/grobid-home/models/sense/ && \

cp resources/dataset/sub-sense/model.wapiti /grobid/grobid-home/models/sub-sense/ && \

cp resources/dataset/etym/model.wapiti /grobid/grobid-home/models/etym/ && \

cp resources/dataset/crossRef/model.wapiti /grobid/grobid-home/models/crossRef/ && \

mkdir /grobid/grobid-home/models/related-entry && \

cp resources/dataset/related-entry/model.wapiti /grobid/grobid-home/models/related-entry/