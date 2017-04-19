#!/bin/bash

cd $1

txt_Regex='.*\.(txt$)'
csv_Regex='.*\.(csv$)'

rm -rf cell
rm -rf cell_correct
rm -rf cell_success
rm -rf cell_failure

mkdir cell

ls  | grep cell | while read cell_FILE
do
   tmp_encoding=$(file -bi $cell_FILE)
   encoding=${tmp_encoding//text\/plain; charset=/}

   if [[ $cell_FILE =~ $txt_Regex ]]; then
      #cp $cell_FILE cell/$cell_FILE
      iconv -f $encoding -t UTF-8 $cell_FILE | awk '{if(NR==1)sub(/^\xef\xbb\xbf/,"");print}' > cell/$cell_FILE
   fi

   if [[ $cell_FILE =~ $csv_Regex ]]; then
      #cp $cell_FILE cell/$cell_FILE
      iconv -f $encoding -t UTF-8 $cell_FILE | awk '{if(NR==1)sub(/^\xef\xbb\xbf/,"");print}' > cell/$cell_FILE
   fi

done

#java -jar /home/davbzh/DataHarmonisationEngine-master/data_validator/JAR/NKCx_validator.jar -i cell -o cell_correct -t p
mkdir cell_correct
ls  $1/cell | while read cell_FILE
do
    java -jar /home/davbzh/DataHarmonisationEngine-master/data_validator//out/artifacts/data_validator_jar/data_validator.jar -i $cell_FILE -o $1/cell_correct/$cell_FILE -t c -p "replace_with_your_path_here"
done


mkdir $1/cell_success
mkdir $1/cell_failure

ls  $1/cell | while read cell_FILE
do
    cp $1/cell_correct/$cell_FILE/success/$cell_FILE $1/cell_success/.
    cp $1/cell_correct/$cell_FILE/failure/$cell_FILE $1/cell_failure/.
done

mkdir $1/aggregated_final
rm -rf $1/aggregated_final/cell.txt
ls $1/cell_success/ | while read cell_FILE
do
   if [[ $cell_FILE =~ $txt_Regex ]]; then
       tail -n +2 $1/cell_success/$cell_FILE >> $1/aggregated_final/cell.txt
   fi

   if [[ $cell_FILE =~ $csv_Regex ]]; then
      tail -n +2 $1/cell_success/$cell_FILE >> $1/aggregated_final/cell.txt
   fi
done

cat $1/cell_correct/$cell_FILE/failure/$cell_FILE $1/cell_failure/*txt | grep -v lab |  grep -v row >> $1/cell_failure/all.fallied

echo "labCode;pnr;sampleYear;scrType;sampleDate;snomed"
awk -F '(,)|(;)|(\t)' '{ print $1";"$2";"$3";"$5";"$7";"$14";"$15 }' $1/cell_failure/all.fallied > $1/cell_failure/fallied.corrected
java -jar /home/davbzh/DataHarmonisationEngine-master/data_validator//out/artifacts/data_validator_jar/data_validator.jar -i $1/cell_failure/fallied.corrected -o $1/cell_correct/fallied.corrected -t c


echo "labCode;pnr;sampleYear;scrType;sampleDate" > $1/cell_failure/fallied.corrected
awk -F '(,)|(;)|(\t)' '{ print $1";"$2";"$3";"$5";"$7 }' $1/cell_failure/all.fallied >> $1/cell_failure/fallied.corrected
java -jar /home/davbzh/DataHarmonisationEngine-master/data_validator//out/artifacts/data_validator_jar/data_validator.jar -i $1/cell_failure/fallied.corrected -o $1/cell_correct/fallied.corrected -t c
tail -n +2 $1/cell_correct/fallied.corrected/success/fallied.corrected >> $1/aggregated_final/cell.txt

