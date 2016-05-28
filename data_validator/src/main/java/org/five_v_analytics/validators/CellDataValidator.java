package org.five_v_analytics.validators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;


public class CellDataValidator extends ValidatorTemplate implements DataValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CellDataValidator.class);
    private final int RESEARCH_START_YEAR = 1960;

    public static final DataValidator INSTANCE = new CellDataValidator();

    private CellDataValidator() { }

    public boolean validate(Map<String, Integer> columns, String[] data) {
        return  validateLabCodeAndCounty(data[columns.get("labcode")], data[columns.get("countyCode")]) &&
                validateSwedishPersonalNumber(data[columns.get("pnr")]) &&
                validateSampleYear(data[columns.get("sampleYear")]) &&
                validateRegistrationDate(data[columns.get("regDate")]) &&
                validateSampleDate(data[columns.get("sampleDate")]);
    }

    private boolean validateSampleYear(String sampleYear) {
        return sampleYear.trim().matches("^\\d{4}") && Integer.parseInt(sampleYear) >= RESEARCH_START_YEAR;
    }

    private boolean validateSampleDate(String sampleDate) {
        return validaFullDate(sampleDate.trim());
    }

    private boolean validateRegistrationDate(String regDate) {
        return validaFullDate(regDate.trim());
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
            LOGGER.error("Date parsing error, Data malformatted {}", fullDate);
            return false;
        }
    }
}
