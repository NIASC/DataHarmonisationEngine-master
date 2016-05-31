package org.five_v_analytics.validators;

import org.five_v_analytics.exceptions.DataValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class CellDataValidator extends ValidatorTemplate implements DataValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CellDataValidator.class);
    private final int RESEARCH_START_YEAR = 1960;

    public static final DataValidator INSTANCE = new CellDataValidator();

    private CellDataValidator() { }

    public String validateAndReturnLine(Map<String, Integer> columns, String[] data) throws DataValidationException {
        validateCounty(data[columns.get("countyCode")]);
        validateLabCode(data[columns.get("labCode")],data[columns.get("countyCode")]);
        data[columns.get("pnr")] = validateSwedishPersonalNumber(data[columns.get("pnr")]);
        validateSampleYear(data[columns.get("sampleYear")]);
        if(columns.containsKey("regDate")){
            validateRegistrationDate(data[columns.get("regDate")]);
            data[columns.get("sampleDate")] = validateSampleDate(data[columns.get("sampleDate")], data[columns.get("regDate")]);
        }else{
            data[columns.get("sampleDate")] = validateSampleDate(data[columns.get("sampleDate")]);
        }

        return Arrays.toString(data);
    }

    private String validateSampleDate(String sampleDate) {
        return validaFullDate(sampleDate.trim()) ? sampleDate : (Year.now().getValue() - 1) + "0601";
    }

    private String validateSampleYear(String sampleYear) throws DataValidationException {
        if (!sampleYear.trim().matches("^\\d{4}")  || Integer.parseInt(sampleYear) < RESEARCH_START_YEAR){
            throw new DataValidationException();
        }
        return sampleYear;
    }

    private String validateSampleDate(String sampleDate, String regDate) {
        if (validaFullDate(sampleDate.trim())){
            return sampleDate;
        } else {
            return validaFullDate(regDate.trim())? regDate: (Year.now().getValue() - 1) + "0601";
        }
    }

    private String validateRegistrationDate(String regDate) {
        return validaFullDate(regDate.trim())? regDate: (Year.now().getValue() - 1) + "0601";
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
