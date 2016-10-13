package org.five_v_analytics.validators;

import org.five_v_analytics.exceptions.DataValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.*;

public class DataValidatorImpl implements DataValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataValidatorImpl.class);
    private static final String CELL_SNOMED_PATH = "./snomed_codes/nkc_translation_cell_diag.csv";
    private static final String PAD_SNOMED_PATH = "./snomed_codes/nkc_translation_pad_diag.csv";
    private static final int COUNTY_NUMBER = 25;
    private static final int RESEARCH_START_YEAR = 1960;
    private static final Map<String, String> countyLabCodes;
    private static final Map<String, String> referalOrganaisedTypes;
    public static  Map<String,String> snomedCodes = new HashMap<>();
    private String snomedPath;

    public DataValidatorImpl(String type) {
        this.snomedPath = getSnomedPath(type);
        generateSnomedList();
    }

    private String getSnomedPath(String type) {
        switch (type){
            case "i":
            case "c":
                return  CELL_SNOMED_PATH;
            case "p":
                return PAD_SNOMED_PATH;

        }

        return null;
    }

    static {
        countyLabCodes = new HashMap<>();
        countyLabCodes.put("00", "051");
        countyLabCodes.put("01", "088");
        countyLabCodes.put("03", "121");
        countyLabCodes.put("04", "131");
        countyLabCodes.put("05", "211");
        countyLabCodes.put("06", "231");
        countyLabCodes.put("07", "241");
        countyLabCodes.put("08", "251");
        countyLabCodes.put("09", "261");
        countyLabCodes.put("10", "271");
        countyLabCodes.put("12", "417");
        countyLabCodes.put("13", "421");
        countyLabCodes.put("14", "501");
        countyLabCodes.put("17", "541");
        countyLabCodes.put("18", "551");
        countyLabCodes.put("19", "561");
        countyLabCodes.put("20", "571");
        countyLabCodes.put("21", "611");
        countyLabCodes.put("22", "621");
        countyLabCodes.put("23", "088");
        countyLabCodes.put("24", "631");
        countyLabCodes.put("25", "651");
    }
    
    static {
    	referalOrganaisedTypes = new HashMap<>();
    	referalOrganaisedTypes.put("VK", "1");
    	referalOrganaisedTypes.put("H", "1");
    	referalOrganaisedTypes.put("HL", "1");
    	referalOrganaisedTypes.put("G", "1");
    	referalOrganaisedTypes.put("ZH", "1");
    	referalOrganaisedTypes.put("S", "1");
    	referalOrganaisedTypes.put("GH", "1");
    	referalOrganaisedTypes.put("HK", "1");
    	referalOrganaisedTypes.put("R", "1");
    	referalOrganaisedTypes.put("YD", "1");
    	referalOrganaisedTypes.put("Y", "1");
    	referalOrganaisedTypes.put("YS", "1");
    	referalOrganaisedTypes.put("VD", "1");
    	referalOrganaisedTypes.put("1", "1");
    	referalOrganaisedTypes.put("5", "1");
    	
    	referalOrganaisedTypes.put("V", "2");
    	referalOrganaisedTypes.put("ZV", "2");
    	referalOrganaisedTypes.put("IL", "2");
    	referalOrganaisedTypes.put("GYN", "2");
    	referalOrganaisedTypes.put("CD", "2");
    	referalOrganaisedTypes.put("C", "2");
    	referalOrganaisedTypes.put("CS", "2");
    	referalOrganaisedTypes.put("2", "2");
    	referalOrganaisedTypes.put("4", "2");
    	
    	referalOrganaisedTypes.put("", "3");
    }


    protected String validateLabCode(String labCode, String county) throws DataValidationException {
        LOGGER.info("Validating LabCode {} and County {}", labCode , county);
        if (!validateCounty(county) || !countyLabCodes.get(county).equals(labCode)){
            LOGGER.error("Validation Error, County code {} and labCode {} doesn't match, Check it", county, labCode);
            //\\//:
            //throw new DataValidationException();
        }
        return  labCode;
    }

    protected boolean validateLabCode(String value) {
        return countyLabCodes.containsValue(value);
    }


    protected boolean validateCounty(String county) {
        return county.trim().matches("^\\d{2}") && Integer.parseInt(county) <= COUNTY_NUMBER;
    }

    protected boolean validateSwedishPersonalNumber(String value)  {
        LOGGER.info("Validating Swedish personal number");
        String personalNumberHash;
        // Check for nulls and false lengths
        if (value == null ||  value.length() < 10) {
            LOGGER.error("Validation Error, personal number is incorrect", value);
            return false;            
        }

        try {
            // Remove dash and plus
            value = value.trim().replace("-", "").replace("+", "");

            String century = "";

            // Remove century and check number
            if (value.length() == 12) {
                century = value.substring(0, 2);
                value = value.substring(2, 12);
            } else if (value.length() == 10) {
                value = value.substring(0, 10);
            } else {
                LOGGER.error("Validation Error, personal number is incorrect", value); 
                return false;
            }
            // Remove check number
            int check = Integer.parseInt(value.substring(9, 10));
            String sValue = value.substring(0, 9);

            int result = 0;

            // Calculate check number
            for (int i = 0, len = sValue.length(); i < len; i++) {
                int tmp = Integer.parseInt(sValue.substring(i, i+1));

                if ((i % 2) == 0) {
                    tmp = (tmp * 2);
                }

                if (tmp > 9) {
                    result += (1 + (tmp % 10));
                } else {
                    result += tmp;
                }
            }

            boolean isValid = (((check + result) % 10) == 0);
            boolean isSSN = Integer.parseInt(value.substring(2, 4), 10) < 13 && Integer.parseInt(value.substring(4, 6), 10) < 32;
            boolean isCoOrdinationNumber = Integer.parseInt(value.substring(4, 6), 10) > 60;
            boolean isMale = !((Integer.parseInt(value.substring(8, 9)) % 2) == 0);
            boolean isCompany = Integer.parseInt(value.substring(2, 4), 10) >= 20;
            return isValid;
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
        LOGGER.error("Validation Error, personal number is incorrect", value);
        return false;
    }

    protected String encryptPersonalNumber(String pnr) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] hash = digest.digest(pnr.getBytes());
        return String.format("%064x", new java.math.BigInteger(1, hash));
    }

    private void generateSnomedList() {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(snomedPath), Charset.forName("Cp1252"))) {
            String line;
            String[] lines;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                if (lineCount > 0) {
                    lines = line.split(";");
                    snomedCodes.put(lines[0].trim(), lines[1].trim());
                }
                lineCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String validateSnomed(String snomed)  { 
        LOGGER.error("Validating snomed", snomed);
        //TODO: Check in the outputs if white spaces and hyphen removal is necessary: .replace(" ", "").replace("-", "")
        String regSnomed = snomedCodes.get(snomed.trim());
        if (regSnomed != null) {
            return regSnomed;
        } else {
            LOGGER.error("Snomed: {} didn't match any Reg_snomed", snomed);
            //\\//:
            //return snomed;
            return "NO_Reg_snomed";
        }
    }

	//\\//:
    public String validateAndReturnLine(Map<String, Integer> columns, String[] data) throws DataValidationException {
    	
    	int labCode_pos;
    	int countyCode_pos;
    	int pnr_pos;
    	int sampleYear_pos;
    	int regDate_pos;
    	int sampleDate_pos;
    	int snomed_pos;
    	int topoCode_pos;
    	int refNR_pos;
    	int remClinic_pos;
    	int ansClinic_pos;
    	int sampleNR_pos;
    	int diagNR_pos;
    	int responseDate_pos;
    	int diagDate_pos;
    	int scrType_pos;

        //TODO:
    	String flagCorrect = "1";
    	String snomedComment = "";
    	String pnrComment = "";
    	String remClinic = "";
    	String labCode = "";

        List<String> sortedData = new ArrayList<String>();
        
        if(columns.containsKey("labCode") && columns.containsKey("countyCode") ){
        	labCode_pos = columns.get("labCode");
        	countyCode_pos = columns.get("countyCode");
        	labCode = data[labCode_pos].trim();
            validateLabCode(labCode,data[countyCode_pos]);
            sortedData.add(labCode);
            sortedData.add(data[countyCode_pos]);
        }else{
            LOGGER.info("[labCode], [countyCode] Columns doesn't exist");
            throw new DataValidationException();
        }
        
        if(columns.containsKey("pnr")) {
        	pnr_pos = columns.get("pnr");
            if (validateSwedishPersonalNumber(data[pnr_pos])){
                data[pnr_pos] = encryptPersonalNumber(data[pnr_pos]);
                sortedData.add(data[pnr_pos]);
            }else{
            	//change flag to 0, which means line is incorrect
            	flagCorrect = "0";
            	pnrComment = "pnr_is_not_valid!";
                data[pnr_pos] = encryptPersonalNumber(data[pnr_pos]);
                sortedData.add(data[pnr_pos]);
                LOGGER.info("Cannot validate [pnr]");
                //throw new DataValidationException();
            }
        }
        else {
            LOGGER.info("[pnr] Column doesn't exist");
        }
        
        if(columns.containsKey("sampleYear")) {
        	sampleYear_pos = columns.get("sampleYear");
            validateSampleYear(data[sampleYear_pos]);
            sortedData.add(data[sampleYear_pos]);
        }
        else {
            LOGGER.info("[sampleYear] Column doesn't exist");
        }
        if(columns.containsKey("regDate")){
        	regDate_pos = columns.get("regDate");
        	sampleDate_pos = columns.get("sampleDate");
            validateRegistrationDate(data[regDate_pos]);
            data[sampleDate_pos] = validateSampleDate(data[sampleDate_pos], data[regDate_pos]);
            sortedData.add(data[regDate_pos]);
            sortedData.add(data[sampleDate_pos]);
        }else{
            if(columns.containsKey("sampleDate")){            	
            	sampleDate_pos = columns.get("sampleDate");
                data[sampleDate_pos] = validateSampleDate(data[sampleDate_pos]);
                sortedData.add("NULL");
                sortedData.add(data[sampleDate_pos]);
            }else{
                LOGGER.info("Cannot validate [registration Date]");
            }    
        }
        if(columns.containsKey("snomed")){
        	snomed_pos = columns.get("snomed");
        	String snomed = validateSnomed(data[snomed_pos]);
        	if (snomed != "NO_Reg_snomed"){
                data[snomed_pos] = validateSnomed(data[snomed_pos]);            
                sortedData.add(data[snomed_pos]);
        	}else{
            	//change flag to 0, which means line is incorrect
            	flagCorrect = "0";
            	snomedComment = "snomed_is_not_valid!";
                sortedData.add(data[snomed_pos]);
                LOGGER.info("Cannot validate [snomed]");
        	}
        }
        if(columns.containsKey("topoCode")){
        	topoCode_pos = columns.get("topoCode");
        	if(!data[topoCode_pos].trim().isEmpty()){
                sortedData.add(data[topoCode_pos]);
        	}else{
                sortedData.add("NULL");        	
            }
        }else{
            sortedData.add("NULL");        	
        }
        if(columns.containsKey("refNR")){
        	refNR_pos = columns.get("refNR");
        	if(!data[refNR_pos].trim().isEmpty()){
                sortedData.add(data[refNR_pos]);
        	}else{
                sortedData.add("NULL");        	
            }
        }else{
            sortedData.add("NULL");        	
        }
        if(columns.containsKey("remClinic")){
        	remClinic_pos = columns.get("remClinic");
        	if(!data[remClinic_pos].trim().isEmpty()){
        		remClinic = data[remClinic_pos];
                sortedData.add(remClinic);
        	}else{
                sortedData.add("NULL");        	
            }
        }else{
            sortedData.add("NULL");        	
        }
        if(columns.containsKey("ansClinic")){
        	ansClinic_pos = columns.get("ansClinic");
        	if(!data[ansClinic_pos].trim().isEmpty()){
                sortedData.add(data[ansClinic_pos]);
        	}else{
                sortedData.add("NULL");        	
            }
        }else{
            sortedData.add("NULL");        	
        }
        if(columns.containsKey("sampleNR")){
        	sampleNR_pos = columns.get("sampleNR");
        	if(!data[sampleNR_pos].trim().isEmpty()){
                sortedData.add(data[sampleNR_pos]);
        	}else{
                sortedData.add("NULL");        	
            }
        }else{
            sortedData.add("NULL");        	
        }
        if(columns.containsKey("diagNR")){
        	diagNR_pos = columns.get("diagNR");
        	if(!data[diagNR_pos].trim().isEmpty()){
                sortedData.add(data[diagNR_pos]);
        	}else{
                sortedData.add("NULL");        	
            }
        }else{
            sortedData.add("NULL");        	
        }
        if(columns.containsKey("responseDate")){
        	responseDate_pos = columns.get("responseDate");
        	if(!data[responseDate_pos].trim().isEmpty()){
                sortedData.add(data[responseDate_pos]);
        	}else{
                sortedData.add("NULL");        	
            }
        }else{
            sortedData.add("NULL");        	
        }
        if(columns.containsKey("diagDate")){
        	diagDate_pos = columns.get("diagDate");
        	if(!data[diagDate_pos].trim().isEmpty()){
                sortedData.add(data[diagDate_pos]);
        	}else{
                sortedData.add("NULL");        	
            }
        }else{
            sortedData.add("NULL");        	
        }
        if(columns.containsKey("scrType")){
        	scrType_pos = columns.get("scrType");        	
        	if(!data[scrType_pos].trim().isEmpty()){
        		if(referalOrganaisedTypes.containsKey(data[scrType_pos].trim())){
        			sortedData.add(referalOrganaisedTypes.get(data[scrType_pos].trim()));
        		}else{
                    sortedData.add("NULL");        	
                }
                //sortedData.add(data[scrType_pos]);
        	}else{
                sortedData.add("NULL");        	
            }
        } else if ( labCode.equals("541") || labCode.equals("571") || labCode.equals("611") || labCode.equals("241") || labCode.equals("247")) {
        	//From these labs referal types are extracted remClinic 
        	if( !remClinic.equals("") ){
        		LOGGER.info("remClinic_referalOrganaisedTypes " + remClinic.substring(0, 2));
        		if(referalOrganaisedTypes.containsKey(remClinic.substring(0, 2))) {
                   sortedData.add( referalOrganaisedTypes.get(remClinic.substring(0, 2)) );
        		}else{ //and if the key is not present in referalOrganaisedTypes, means 2 (only for these labs)
        		   sortedData.add("2");
        		}
        	}else{
                sortedData.add("NULL");        	
            }
        } else{
            sortedData.add("NULL");        	
        }

        //TODO:
    	if (flagCorrect == "1"){
            sortedData.add(flagCorrect);    		
            sortedData.add("Correct");    		
    	}else if(flagCorrect == "0"){
            sortedData.add(flagCorrect);
            sortedData.add(pnrComment + snomedComment);
    	}
        
        String[] sortedArray = new String[ sortedData.size() ];
        sortedData.toArray( sortedArray );        
        //return Arrays.toString(data);
        return Arrays.toString(sortedArray);
    }
    
    public String validateColumnValue(String value) {
        if (validateSwedishPersonalNumber(value)){
            return encryptPersonalNumber(value);
        }
        return (validateSampleYear(value) ||
                validateFullDate(value) ||
                validateCounty(value) ||
                validateLabCode(value)) ? value : null;
    }

    private String validateSampleDate(String sampleDate) {
        LOGGER.info("Validating sampleDate");
        return validateFullDate(sampleDate.trim()) ? sampleDate : (Year.now().getValue() - 1) + "0601";
    }

    private boolean validateSampleYear(String sampleYear) {
        LOGGER.info("Validating Sample Year");
        if (!sampleYear.trim().matches("^\\d{4}")  || Integer.parseInt(sampleYear) < RESEARCH_START_YEAR){
            LOGGER.error("Sample year parsing error, {}", sampleYear);
            return false;
        }
        return true;
    }

    private String validateSampleDate(String sampleDate, String regDate) {
        LOGGER.info("Validating Sample Date");
        if (validateFullDate(sampleDate.trim())){
            return sampleDate;
        } else {
            return validateFullDate(regDate.trim())? regDate: (Year.now().getValue() - 1) + "0601";
        }
    }

    private boolean validateRegistrationDate(String regDate) {
        LOGGER.info("Validating Registration Date");
        return validateFullDate(regDate.trim());
    }

    private boolean validateFullDate(String fullDate) {
        LOGGER.info("Validating Full Date");
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyyMMdd")
                .parseStrict()
                .toFormatter();
        try {
            LocalDate.parse(fullDate, formatter);
            return true;
        } catch (DateTimeParseException e) {
            LOGGER.error("Date parsing error, Data malformatted {}", fullDate);
            return false;
        }
    }
}
