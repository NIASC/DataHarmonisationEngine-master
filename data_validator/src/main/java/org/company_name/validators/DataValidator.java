package org.company_name.validators;

import java.util.Map;

public interface DataValidator {
    boolean validate(Map<String, Integer> columns, String[] data);
}
