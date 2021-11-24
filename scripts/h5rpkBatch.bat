set FILEDIR=D:\Projects\ePTM_AWS\ePTMscenarios\DCP_2020_ProposedCond_11Aug21\DSM2
set FILENAME=DCP_SWP6000
cd %FILEDIR%
h5repack -l CONTI %FILENAME%.h5 %FILENAME%_unpack.h5