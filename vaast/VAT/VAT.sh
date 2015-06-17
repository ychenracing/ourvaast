#!/bin/bash

cd ../case
concentrations=$(ls)
for concentration in $concentrations
do
	cd $concentration
	casegvfFiles=$(ls *.gvf)
	suffix=".vat.gvf"
	for i in $casegvfFiles
	do
	  VAT -f ../../refGene_hg19.gff3 -a ../../hg19.fasta $i > ${i%.*}${suffix}
	done
	cd ..
done

cd ../control
controlgvfFiles=$(ls *.gvf)
suffix=".vat.gvf"
for i in $controlgvfFiles
do
  VAT -f ../refGene_hg19.gff3 -a ../hg19.fasta $i > ${i%.*}${suffix}
done
cd ..

