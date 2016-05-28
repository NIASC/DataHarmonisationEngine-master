package org.company_name;

import org.apache.commons.cli.*;

import java.io.IOException;


public class Cli {
    private String[] args = null;
    private Options options = new Options();

    public Cli(String[] args) {
        this.args = args;

        options.addOption("h", "help", false, "show help.");
        options.addOption("i", "input", true, "input file location.");
        options.addOption("o", "output", true, "output file location.");
        options.addOption("t", "type", true, "file type (c - Cell | p - Pad | i - Inv).");
        options.addOption("s", "separator", true, "data separator");
    }

    public void parse() {
        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                help();
            }

            if (cmd.hasOption("i") &&
                    cmd.hasOption("o") &&
                    cmd.hasOption("t")) {
                    FileProcessor.process(cmd.getOptionValue("i"),
                            cmd.getOptionValue("o"),
                            cmd.getOptionValue("t"));

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

