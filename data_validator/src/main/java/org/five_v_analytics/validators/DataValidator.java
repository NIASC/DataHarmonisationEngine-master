package org.five_v_analytics.validators;

import java.util.Map;

public interface DataValidator {
    boolean validate(Map<String, Integer> columns, String[] data);
}
