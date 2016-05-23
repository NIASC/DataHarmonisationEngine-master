package org.company_name.factories;

import org.company_name.validators.CellDataValidator;
import org.company_name.validators.DataValidator;
import org.company_name.validators.InvDataValidator;
import org.company_name.validators.PadDataValidator;

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