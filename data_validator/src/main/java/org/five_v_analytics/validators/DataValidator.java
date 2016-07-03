package org.five_v_analytics.validators;

import com.sun.media.sound.InvalidDataException;
import org.five_v_analytics.exceptions.DataValidationException;

import java.util.Map;

public interface DataValidator {
    String validateAndReturnLine(Map<String, Integer> columns, String[] data) throws DataValidationException, InvalidDataException;
    String validateColumnValue(String value);
}
