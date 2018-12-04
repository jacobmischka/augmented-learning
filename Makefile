ASSETS_DIR=app/src/main/assets
SAMPLEDATA_DIR=app/sampledata

IMAGE_LIST=image_list.txt
MODELS=models.imgdb

IMAGES=app/sampledata/input-images/*/*.jpg

.PHONY: build-db

build-db: ${ASSETS_DIR}/${MODELS}

${ASSETS_DIR}/${MODELS}: ${SAMPLEDATA_DIR}/${IMAGE_LIST} ${IMAGES}
	arcoreimg build-db --input_image_list_path=$< --output_db_path=$@

eval-db: ${ASSETS_DIR}/${MODELS}
	arcoreimg eval-db --input_db_path=$< --input_image_list_path=$<-imglist.txt

