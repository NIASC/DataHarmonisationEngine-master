package org.five_v_analytics.validators;

import com.sun.media.sound.InvalidDataException;
import org.five_v_analytics.exceptions.DataValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by iliaaptsiauri on 28/05/16.
 */
public abstract class ValidatorTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidatorTemplate.class);
    private final int COUNTY_NUMBER = 25;
    private static final Map<String, String> countyLabCodes;

    static {
        countyLabCodes = new HashMap<>();
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
        if (!countyLabCodes.get(county).equals(labCode)){
            throw new DataValidationException();
        }
        return  labCode;
    }

    protected String validateCounty(String county) throws DataValidationException{
        if (!county.trim().matches("^\\d{2}") || Integer.parseInt(county) >= COUNTY_NUMBER){
            throw new DataValidationException();
        }

        return county;
    }


    protected String validateSwedishPersonalNumber(String value) throws DataValidationException{
        String personalNumberHash;
        // Check for nulls and false lengths
        if (value == null ||  value.length() < 10) {
            throw new DataValidationException();
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
                throw new DataValidationException();
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
            if (!isValid){
                LOGGER.error("Personal number not valid {}", value);
                throw new DataValidationException();
            }
            return encryptPersonalNumber(value);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
        throw new DataValidationException();
    }

    private String encryptPersonalNumber(String pnr) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] hash = digest.digest(pnr.getBytes());
        return String.format("%064x", new java.math.BigInteger(1, hash));
    }
}
