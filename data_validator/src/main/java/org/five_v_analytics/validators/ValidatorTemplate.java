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
import java.util.HashMap;
import java.util.Map;

public abstract class ValidatorTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidatorTemplate.class);
    private final int COUNTY_NUMBER = 25;
    private static final Map<String, String> countyLabCodes;
    public static  Map<String,String> snomedCodes = new HashMap<>();

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
        return county.trim().matches("^\\d{2}") && Integer.parseInt(county) >= COUNTY_NUMBER;
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
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("./snomed_codes/nkc_translation_cell_diag.csv"), Charset.forName("Cp1252"))) {
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
        generateSnomedList();
        String regSnomed = snomedCodes.get(snomed.trim());
        if (regSnomed != null) {
            return regSnomed;
        } else {
            LOGGER.error("Snomed: {} didn't match any Reg_snomed", snomed);
            return snomed;
        }

    }
}
