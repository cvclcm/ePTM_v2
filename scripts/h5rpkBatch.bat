set FILEDIR=PATH_TO_FOLDER_WITH_HYDRO_HDF5_FILE
set FILENAME=HYDRO_HDF5_FILE
cd %FILEDIR%
h5repack -l CONTI %FILENAME%.h5 %FILENAME%_unpack.h5
