ASSETS_DIR=app/src/main/assets
SAMPLEDATA_DIR=app/sampledata


.PHONY: fruits-db

fruits-db: ${ASSETS_DIR}/fruits.imgdb

${ASSETS_DIR}/fruits.imgdb: ${SAMPLEDATA_DIR}/fruits_image_list.txt
	arcoreimg build-db --input_image_list_path=${SAMPLEDATA_DIR}/fruits_image_list.txt --output_db_path=${ASSETS_DIR}/fruits.imgdb
