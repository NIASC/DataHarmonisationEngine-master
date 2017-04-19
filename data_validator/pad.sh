#!/bin/bash

cd $1

txt_Regex='.*\.(txt$)'
csv_Regex='.*\.(csv$)'

rm -rf pad
rm -rf pad_correct
rm -rf pad_success
rm -rf pad_failure

mkdir pad

ls  | grep pad | while read pad_FILE
do
   tmp_encoding=$(file -bi $pad_FILE)
   encoding=${tmp_encoding//text\/plain; charset=/}

   if [[ $pad_FILE =~ $txt_Regex ]]; then
      #cp $pad_FILE pad/$pad_FILE
      iconv -f $encoding -t UTF-8 $pad_FILE | awk '{if(NR==1)sub(/^\xef\xbb\xbf/,"");print}' > pad/$pad_FILE
   fi

   if [[ $pad_FILE =~ $csv_Regex ]]; then
      #cp $pad_FILE pad/$pad_FILE
      iconv -f $encoding -t UTF-8 $pad_FILE | awk '{if(NR==1)sub(/^\xef\xbb\xbf/,"");print}' > pad/$pad_FILE
   fi

done


#java -jar /home/davbzh/DataHarmonisationEngine-master/data_validator/JAR/NKCx_validator.jar -i pad -o pad_correct -t p
mkdir pad_correct
ls  $1/pad | while read pad_FILE
do
    java -jar /home/davbzh/DataHarmonisationEngine-master/data_validator//out/artifacts/data_validator_jar/data_validator.jar -i $pad_FILE -o $1/pad_correct/$pad_FILE -t p -p "replace_with_your_path_here"
done


mkdir $1/pad_success
mkdir $1/pad_failure

ls  $1/pad | while read pad_FILE
do
    cp $1/pad_correct/$pad_FILE/success/$pad_FILE $1/pad_success/.
    cp $1/pad_correct/$pad_FILE/failure/$pad_FILE $1/pad_failure/.
done

mkdir $1/aggregated_final
rm -rf $1/aggregated_final/pad.txt
ls $1/pad_success/ | while read pad_FILE
do
   if [[ $pad_FILE =~ $txt_Regex ]]; then
       tail -n +2 $1/pad_success/$pad_FILE >> $1/aggregated_final/pad.txt
   fi

   if [[ $pad_FILE =~ $csv_Regex ]]; then
      tail -n +2 $1/pad_success/$pad_FILE >> $1/aggregated_final/pad.txt
   fi
done

cat $1/pad_correct/$pad_FILE/failure/$pad_FILE $1/pad_failure/*txt | grep -v lab |  grep -v row