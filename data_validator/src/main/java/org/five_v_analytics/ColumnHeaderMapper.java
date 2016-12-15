package org.five_v_analytics;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ColumnHeaderMapper {
    private static Map<String, String> columnNames;
    private static Map<String, Integer> columnMap;
    private static final Logger LOGGER = LoggerFactory.getLogger(ColumnHeaderMapper.class);

    static {
        columnNames = new HashMap<>();
//      Lab Code
        columnNames.put("labcode", "labCode");
        columnNames.put("labkod","labCode");
//      Personal Number
        columnNames.put("pnr", "pnr");
//      Sample Year
        columnNames.put("padyear", "sampleYear");
        columnNames.put("sampleyear", "sampleYear");
        columnNames.put("smearyear", "sampleYear");
        columnNames.put("sample_year", "sampleYear");
//      Screening Type
        columnNames.put("scrtype", "ScreeningType");
//      Sample Date
        columnNames.put("smeardate", "sampleDate");
        columnNames.put("sampledate", "sampleDate");
        columnNames.put("sample_date", "sampleDate");
        columnNames.put("paddate", "sampleDate");
        columnNames.put("pad_date", "sampleDate");
//      Registration Date
        columnNames.put("regdate", "regDate");
//      Response Date
        columnNames.put("responsedate", "responseDate");
//      Diagnosis date	
        columnNames.put("diagdate", "diagDate");
//      County
        columnNames.put("residc", "countyCode");
        columnNames.put("county", "countyCode");
        columnNames.put("countycode", "countyCode");
//      SNOMED
        columnNames.put("snomed", "snomed");
//      Topology code	
        columnNames.put("topocode", "topoCode");
//      Referral number		
        columnNames.put("referralnr", "refNR");
//      Referral type		
        columnNames.put("referral_type", "refType");        
        columnNames.put("referraltype", "refType");                
//      Rem clinic name	
        columnNames.put("remclinic", "remClinic");
        columnNames.put("rem_clinic", "remClinic");
//      Ans clinic name	
        columnNames.put("ansclinic", "ansClinic");
        columnNames.put("ans_clinic", "ansClinic");
//      Sample number	
        columnNames.put("samplenr", "sampleNR");
        columnNames.put("sample_nr", "sampleNR");
//      Diagnosis number
        columnNames.put("diagnr", "diagNR");
//      Screening type
        columnNames.put("scr_type", "scrType");                
        columnNames.put("scrtype", "scrType");                

//      Doctor
        columnNames.put("Doctor", "Doctor");                
        columnNames.put("doctor", "Doctor");                

    }

    public static void mapHeaderToIndex(String[] headers) {
        columnMap = new HashMap<>();
        String value;
        for (int i = 0; i < headers.length; i++) {
            value = columnNames.get(headers[i].toLowerCase().trim());
            if (value != null) {
                LOGGER.info("Indexing header [{}] ", headers[i]);
                columnMap.put(value, i);
            } else {
                System.out.println("File contains unsupported Header, please contact the support");
//              LOGGER.info("[{}] is unsupported Header, please contact the support", headers[i]);
//              System.exit(-1);
            }
        }
    }

    public static Map<String, Integer> getColumnMap() {
        return columnMap;
    }

}
