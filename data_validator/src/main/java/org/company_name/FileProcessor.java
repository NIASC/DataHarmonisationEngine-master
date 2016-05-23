package org.company_name;

import org.company_name.factories.ValidatorFactory;
import org.company_name.validators.DataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class FileProcessor {
    public static final Logger LOGGER = LoggerFactory.getLogger(FileProcessor.class);
    private static Path success;
    private static Path failure;

    public static void process(String inputPath, String outputPath, String type) throws IOException {
        DataValidator validator = ValidatorFactory.getInstance(type);
        ColumnHeaderMapper columnHeaderMapper = new ColumnHeaderMapper();
        Files.walk(Paths.get(inputPath)).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                createOutputDirectories(outputPath, filePath.getFileName().toString());
                try (BufferedReader reader = Files.newBufferedReader(filePath);
                     BufferedWriter successWriter = Files.newBufferedWriter(success);
                     BufferedWriter failureWriter = Files.newBufferedWriter(failure)) {

                    String line;
                    int lineCount = 0;
                    LOGGER.info("Processing file {}", filePath.getFileName().toString());
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Raw CSV data: " + line);
                        if (lineCount == 0) {
                            columnHeaderMapper.mapHeaderToIndex(line.split(";"));
                            failureWriter.write(line + "\n");
                            if (columnHeaderMapper.getColumnMap().containsKey("missingName")) {
                                return;
                            }
                        } else {
                            LOGGER.info("Line number = {}", lineCount);
                            if (validator.validate(columnHeaderMapper.getColumnMap(), line.split(";"))) {
                                LOGGER.info("Line processed successfully");
                                successWriter.write(line + "\n");
                            } else {
                                failureWriter.write(line);
                            }
                        }
                        lineCount++;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void createOutputDirectories(String outputPath, String fileName) {
        Path output = Paths.get(outputPath + "/" + fileName);
        try {
            Files.createDirectories(output);
            Path successDirectory = Paths.get(output.toString() + "/success");
            Path failureDirectory = Paths.get(output.toString() + "/failure");

            Files.createDirectories(successDirectory);
            Files.createDirectories(failureDirectory);
            success = Paths.get(successDirectory.toString() + "/" + fileName);
            failure = Paths.get(failureDirectory.toString() + "/" + fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
