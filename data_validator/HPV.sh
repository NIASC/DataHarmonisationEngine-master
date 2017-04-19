#!/bin/sh

export in_dir=$1
export out_dir=$2

if [ -d $out_dir ]; then
   rm -r $out_dir
fi
if [ ! -d $out_dir ]; then
   mkdir $out_dir
fi

find $in_dir -name "*xls*" | while read FILE
do
        #echo "$FILE"
        #grep '>' $FILE | awk '{ gsub(">","",$1); print $1 }' |  sort -k1,1 -T ./ | awk '!x[$1]++' >> $list_file
        #xlsx2csv $FILE > /media/mountpoint/databases/KNCx_HPV_2013-2015/Stockholm/2013/HPV2013.csv
        filename_extention=$(basename "$FILE")
        #result_string="${filename_extention/ /_}"
        result_string="$( echo "$filename_extention" | sed 's/[[:space:]]/_/g')"
        #echo $FILE
        echo $result_string
        xlsx_Regex='.*\.(xlsx$)'
        if [[ $result_string =~ $xlsx_Regex ]]; then
            xlsx2csv "$FILE" > $out_dir/$result_string.csv
        fi
        xls_Regex='.*\.(xls$)'
        if [[ $result_string =~ $xls_Regex ]]; then
           /media/StorageOne/HTS/viralmeta_bioifo/public_programs/xls2csv-1.07/script/xls2csv -x "$FILE" -c "$out_dir/$result_string.csv" -a UTF-8
        fi
done
