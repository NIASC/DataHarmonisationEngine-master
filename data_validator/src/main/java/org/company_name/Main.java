package org.company_name;

import org.company_name.factories.ValidatorFactory;
import org.company_name.validators.DataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(Main.class);
        Cli cli = new Cli(args);
        cli.parse();
    }
}
