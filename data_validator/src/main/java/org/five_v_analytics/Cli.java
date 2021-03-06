package org.five_v_analytics;

import org.apache.commons.cli.*;


public class Cli {
    private String[] args = null;
    private Options options = new Options();

    public Cli(String[] args) {
        this.args = args;

        options.addOption("h", "help", false, "show help.");
        options.addOption("i", "input", true, "input file directory location");
        options.addOption("o", "output", true, "output file directory location");
        options.addOption("p", "phrase", true, "secret phrase for PIN pseudonymisation");
        options.addOption("t", "type", true, "file type (c - Cell | p - Pad | i - Inv | e - extra-hpv).");
    }

    public void parse() {
        CommandLineParser parser = new DefaultParser();

        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                help();
            }

            if (cmd.hasOption("i") && cmd.hasOption("o") && cmd.hasOption("p") && cmd.hasOption("t")) {
                    FileProcessor.process(
                            cmd.getOptionValue("i"),
                            cmd.getOptionValue("o"),
                            cmd.getOptionValue("p"),
                            cmd.getOptionValue("t")
                    );

            } else {
                help();
            }

        } catch (ParseException e) {
            help();
        }
    }

    private void help() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Data Validator", options);
        System.exit(0);
    }
}

