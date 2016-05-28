package org.five_v_analytics.factories;

import org.five_v_analytics.validators.CellDataValidator;
import org.five_v_analytics.validators.DataValidator;

import java.util.HashMap;
import java.util.Map;


public abstract class ValidatorFactory {
    private static Map<String, DataValidator> validators;

    static {
        validators = new HashMap<>();
        validators.put("c", CellDataValidator.INSTANCE);
//        validators.put("p", PadDataValidator.INSTANCE);
//        validators.put("i", InvDataValidator.INSTANCE);
    }

    public static DataValidator getInstance(String type) {
        return validators.get(type);
    }
}