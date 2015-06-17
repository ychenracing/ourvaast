#!/bin/bash

cd ./case
concentrations=$(ls)
for concentration in $concentrations
do
	cd $concentration
	caseVcfFiles=$(ls)
	for j in $caseVcfFiles
	do
	  vaast_converter --build hg19 --path $(pwd)/ $j
	done
	rm Gene_RefSeq.gvf
	rm SampleID.gvf
	cd ..
done

cd ../control
controlVcfFiles=$(ls)
for j in $controlVcfFiles
do
  vaast_converter --build hg19 --path $(pwd)/ $j
done
rm Gene_RefSeq.gvf
rm SampleID.gvf
cd ..
