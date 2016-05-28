package org.five_v_analytics.validators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Map;


public class CellDataValidator implements DataValidator {
    public static final Logger LOGGER = LoggerFactory.getLogger(CellDataValidator.class);

    public static final DataValidator INSTANCE = new CellDataValidator();

    private CellDataValidator() { }

    public boolean validate(Map<String, Integer> columns, String[] data) {
        return validateSwedishId(data[columns.get("pnr")]) &&
                validateSampleYear(data[columns.get("sampleYear")]);
    }

    private final int COUNTY_NUMBER = 25;

    private boolean validateSwedishId (String value) {

        // Check for nulls and false lengths
        if (value == null ||  value.length() < 10) {
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
            if (!isValid){
                LOGGER.error("Personal number not valid {}", value);
            }
            return isValid;
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    private boolean validateSampleYear(String sampleYear) {
        return sampleYear.trim().matches("^\\d{4}");
    }

    private boolean validateSampleDate(String sampleDate) {
        return validaFullDate(sampleDate.trim());
    }

    private boolean validateRegistrationDate(String regDate) {
        return validaFullDate(regDate.trim());
    }

    private boolean validateDiagnoseDate(String regDate) {
        return validaFullDate(regDate.trim());
    }

    private boolean validateCountyCode(String countyCode) {
        return Integer.parseInt(countyCode) <= COUNTY_NUMBER;
    }

    private boolean validaFullDate(String fullDate) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyyMMdd")
                .parseStrict()
                .toFormatter();
        try {
            LocalDate.parse(fullDate, formatter);
            return true;
        } catch (DateTimeParseException e) {
            //Todo Logger should be implemented
            System.out.println("Date parsing error");
            return false;
        }
    }
}
