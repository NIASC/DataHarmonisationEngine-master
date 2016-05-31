package org.five_v_analytics;


import java.util.HashMap;
import java.util.Map;

public class ColumnHeaderMapper {
    private static Map<String, String> columnNames;
    private static Map<String, Integer> columnMap;

    static {
        columnNames = new HashMap<>();
//      Lab Code
        columnNames.put("labcode", "labCode");
//      Personal Number
        columnNames.put("pnr", "pnr");
//      Sample Year
        columnNames.put("sampleyear", "sampleYear");
        columnNames.put("smearyear", "sampleYear");
        columnNames.put("sample_year", "sampleYear");
//      Screening Type
        columnNames.put("scrtype", "ScreeningType");
//      Sample Date
        columnNames.put("smeardate", "sampleDate");
        columnNames.put("sampledate", "sampleDate");
        columnNames.put("sampledate", "sampleDate");

//      Registration Date
        columnNames.put("regdate", "regDate");
//      County
        columnNames.put("residc", "countyCode");
        columnNames.put("county", "countyCode");
//      SNOMED
        columnNames.put("snomed", "snomed");
//      Response Date
        columnNames.put("responsedate", "responseDate");
    }

    public static void mapHeaderToIndex(String[] headers) {
        columnMap = new HashMap<>();
        String value;
        for (int i = 0; i < headers.length; i++) {
            value = columnNames.get(headers[i]);
            if (value != null) {
                columnMap.put(columnNames.get(headers[i]), i);
            } else {
                System.out.println("File contains unsupported Headers, please contact the support");
//                System.exit(-1);
            }
        }
    }

    public static Map<String, Integer> getColumnMap() {
        return columnMap;
    }

}
