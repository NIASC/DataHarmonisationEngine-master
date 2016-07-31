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

public class DataValidatorImpl implements DataValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataValidatorImpl.class);
    private static final String CELL_SNOMED_PATH = "./snomed_codes/nkc_translation_cell_diag.csv";
    private static final String PAD_SNOMED_PATH = "./snomed_codes/nkc_translation_pad_diag.csv";
    private static final int COUNTY_NUMBER = 25;
    private static final int RESEARCH_START_YEAR = 1960;
    private static final Map<String, String> countyLabCodes;
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

    protected String validateLabCode(String labCode, String county) throws DataValidationException {
        LOGGER.info("Validating LabCode");
        if (!validateCounty(county) || !countyLabCodes.get(county).equals(labCode)){
            LOGGER.error("Validation Error, County code is {}, labCode is {}", county, labCode);
            throw new DataValidationException();
        }
        return  labCode;
    }

    protected boolean validateLabCode(String value) {
        return countyLabCodes.containsValue(value);
    }


    protected boolean validateCounty(String county) {
        return county.trim().matches("^\\d{2}") && Integer.parseInt(county) <= COUNTY_NUMBER;
    }


    protected boolean validateSwedishPersonalNumber(String value){
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
                    snomedCodes.put(lines[3], lines[4]);
                }
                lineCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String validateSnomed(String snomed) {
        LOGGER.error("Validating snomed", snomed);
        String regSnomed = snomedCodes.get(snomed.trim());
        if (regSnomed != null) {
            return regSnomed;
        } else {
            LOGGER.error("Snomed: {} didn't match any Reg_snomed", snomed);
            return snomed;
        }

    }

    public String validateAndReturnLine(Map<String, Integer> columns, String[] data) throws DataValidationException {
        if(columns.containsKey("labCode") && columns.containsKey("countyCode") && columns.containsKey("pnr")){
            validateLabCode(data[columns.get("labCode")],data[columns.get("countyCode")]);
            if (validateSwedishPersonalNumber(data[columns.get("pnr")])){
                data[columns.get("pnr")] = encryptPersonalNumber(data[columns.get("pnr")]);
            }
        }else{
            LOGGER.info("Cannot validate [labCode], [countyCode] and [pnr]");
            throw new DataValidationException();
        }
        if(columns.containsKey("sampleYear")) {
            validateSampleYear(data[columns.get("sampleYear")]);
        }
        else {
            LOGGER.info("[sampleYear] Column doesn't exist");
        }
        if(columns.containsKey("regDate")){
            validateRegistrationDate(data[columns.get("regDate")]);
            data[columns.get("sampleDate")] = validateSampleDate(data[columns.get("sampleDate")], data[columns.get("regDate")]);
        }else{
            if(columns.containsKey("sampleDate"))
                data[columns.get("sampleDate")] = validateSampleDate(data[columns.get("sampleDate")]);
            else
                LOGGER.info("Cannot validate [registration Date]");
        }
        if(columns.containsKey("snomed")){
            data[columns.get("snomed")] = validateSnomed(data[columns.get("snomed")]);
        }
        return Arrays.toString(data);
    }
    public String validateColumnValue(String value){
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
