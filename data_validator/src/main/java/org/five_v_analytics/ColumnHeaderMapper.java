package org.five_v_analytics;


import java.util.HashMap;
import java.util.Map;

public class ColumnHeaderMapper {
    private static Map<String, String> columnNames;
    private static Map<String, Integer> columnMap;

    static {
        columnNames = new HashMap<>();
        columnNames.put("labcode", "labCode");
        columnNames.put("pnr", "pnr");
        columnNames.put("sampleyear", "sampleYear");
        columnNames.put("smearyear", "sampleYear");
        columnNames.put("sample_year", "sampleYear");
        columnNames.put("referralnr", "referralNumber");
        columnNames.put("refsite", "referralSite");
        columnNames.put("scrtype", "scrType");
        columnNames.put("smeardate", "sampleDate");
        columnNames.put("regdate", "regDate");
        columnNames.put("topo", "topo");
        columnNames.put("snomed", "snomed");
        columnNames.put("sampletype", "sampleType");
        columnNames.put("residc", "countyCode");
        columnNames.put("residk", "residk");
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
