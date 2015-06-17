#!/bin/bash

cd ../case
concentrations=$(ls)
for concentration in $concentrations
do
	cd $concentration
	VAAST --iht r --fast_gp -d 1e4 -o case_${concentration} -r 0.00035 -m lrt --conservation_file NONE -k ../../refGene_hg19.gff3 ../../control/control_output.cdr ./case_${concentration}_output.cdr
	cd ..
done
