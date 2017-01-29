#!/bin/sh
echo DeltaFoods
curl -L 'https://api.cloudconvert.com/convert' \
     -F file=@'inputfile.m4a' \
     -F 'apikey=zB2He3zj5GS2lAJpPbPHtxIqPpQdciMGDiIhvHm_fNz9rloPL-7l1z-j5wCOpWJ25bFheaXSruLstTZVwf2elw' \
     -F 'inputformat=m4a' \
     -F 'outputformat=flac' \
     -F 'input=upload' \
     -F 'converteroptions[audio_channels]=1' \
     -F 'converteroptions[audio_frequency]=8000' \
     -F 'wait=true' \
     -F 'download=true' \
     > 'outputfile.flac'