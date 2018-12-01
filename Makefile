ASSETS_DIR=app/src/main/assets
SAMPLEDATA_DIR=app/sampledata

IMAGE_LIST=image_list.txt
MODELS=models.imgdb


.PHONY: build-db

${ASSETS_DIR}/${MODELS}: ${SAMPLEDATA_DIR}/${IMAGE_LIST}
	arcoreimg build-db --input_image_list_path=$^ --output_db_path=$@

build-db: ${ASSETS_DIR}/${MODELS}

eval-db: ${ASSETS_DIR}/${MODELS}
	arcoreimg eval-db --input_db_path=$< --input_image_list_path=$<-imglist.txt

