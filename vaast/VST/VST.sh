#!/bin/bash

cd ../case
concentrations=$(ls)
for concentration in $concentrations
do
	cd $concentration
	number=$(ls|wc -l)
        prefix="VST -o 'U(0..$number)' -b hg19 "
	vatGvfNames=$(ls *.vat.gvf)
	space=" "
	for nameItem in $vatGvfNames
	do
	  prefix="${prefix}""${nameItem}""${space}"
	done
	suffix="> case_"${concentration}"_output.cdr"
	prefix="${prefix}""${suffix}"
	echo "#!/bin/sh" > VST_command_case_${concentration}.sh
	echo $prefix >> VST_command_case_${concentration}.sh
	chmod 777 VST_command_case_${concentration}.sh
	sh VST_command_case_${concentration}.sh 
	cd ..
done

cd ../control
controlNumber=$(ls|sc -l)
prefix="VST -o 'U(0..$controlNumber)' -b hg19 "
vatGvfNames=$(ls *.vat.gvf)
space=" "
for nameItem in $vatGvfNames
do
  prefix="${prefix}""${nameItem}""${space}"
done
suffix="> control_output.cdr"
prefix="${prefix}""${suffix}"
echo "#!/bin/sh" > VST_command_control.sh
echo $prefix >> VST_command_control.sh
chmod 777 VST_command_control.sh
sh VST_command_control.sh 
cd ..

