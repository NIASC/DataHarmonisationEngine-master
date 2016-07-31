package org.five_v_analytics;

import com.ibm.icu.text.CharsetDetector;
import org.five_v_analytics.exceptions.DataValidationException;
import org.five_v_analytics.validators.DataValidator;
import org.five_v_analytics.validators.DataValidatorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;


public class FileProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileProcessor.class);
    private static final String[] SPLITTERS = {";",",","\t"," ","[\t]+","[ ]+"};
    private static Path success;
    private static Path failure;
    private static DataValidator validator;
    private static String[] columnNames;
    private static String delimiter = "";

    public static void process(String inputPath, String outputPath, String type) {
        validator = new DataValidatorImpl(type);
        try {
            LOGGER.info("Searching for files");
            Files.walk(Paths.get(inputPath)).forEach(filePath -> {
                if(!filePath.getFileName().toString().equals(".DS_Store")){
                    if (Files.isRegularFile(filePath)) {
                        createOutputDirectories(outputPath, filePath.getFileName().toString());
                        processLine(filePath);
                    }
                }
            });
        } catch (IOException e) {
            LOGGER.error("Error while Searching for files");
            LOGGER.info("Exception. " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processLine(Path filePath) {
        try (BufferedReader reader = Files.newBufferedReader(filePath, Charset.forName(getEncoding(filePath)));
             BufferedWriter successWriter = Files.newBufferedWriter(success);
             BufferedWriter failureWriter = Files.newBufferedWriter(failure)) {

            String line;
            int lineCount = 0;
            LOGGER.info("Processing file {}", filePath.getFileName().toString());
            while ((line = reader.readLine()) != null) {
                System.out.println("Raw CSV data: " + line);
                LOGGER.info("Reading CSV data: " + line);
                if (lineCount == 0) {
                    createColumnHeadersMap(failureWriter, successWriter, line);
                } else {
                    LOGGER.info("Line number = {}", lineCount);
                    if (ColumnHeaderMapper.getColumnMap().isEmpty()){
                        validateColumn(successWriter, failureWriter, line);
                    } else {
                        validateLine(successWriter, failureWriter, line);
                    }
                }
                lineCount++;
            }

        } catch (IOException e) {
            LOGGER.error("Error while processing file {}", filePath.getFileName().toString());
            LOGGER.info("Exception. " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createColumnHeadersMap(BufferedWriter failureWriter, BufferedWriter successWriter, String line) throws IOException {
        LOGGER.info("Mapping column headers");
        columnNames = getColumnNames(line);
        ColumnHeaderMapper.mapHeaderToIndex(columnNames);
        if(ColumnHeaderMapper.getColumnMap().isEmpty()){
            LOGGER.info("File doesn't have the header line");
            validateColumn(successWriter, failureWriter, line);
            return;
        }
        LOGGER.info("Writing column headers in success and failure files");
        failureWriter.write(line + "\n");
        successWriter.write(line + "\n");
    }

    private static void validateColumn(BufferedWriter successWriter, BufferedWriter failureWriter, String line) {
        for (String splitter: SPLITTERS) {
            String[] splicedLine = splitLine(line, splitter);
            String result = "";
            if(splicedLine.length > 1){
                for (String value: splicedLine) {
                    String validationResult = validator.validateColumnValue(value);
                    if (validationResult == null){
                        try {
                            failureWriter.write(line + "\n");
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    result += validationResult + ",";
                }
                try {
                    successWriter.write(result + "\n");
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void validateLine(BufferedWriter successWriter, BufferedWriter failureWriter, String line) throws IOException {
        LOGGER.info("Validating line [{}]", line);
        Map<String, Integer> headers = ColumnHeaderMapper.getColumnMap();
        try {
            successWriter.write(validator.validateAndReturnLine(headers, getColumnValues(line)) + "\n");
            LOGGER.info("validation finished successfully for line [{}]",line);
        }catch (DataValidationException e){
            LOGGER.info("Validation failed for line [{}], writing to failure directory", line);
            failureWriter.write(line + "\n");
        }
    }

    private static void createOutputDirectories(String outputPath, String fileName) {
        try {
            Path output = Paths.get(outputPath + "/" + fileName);
            Files.createDirectories(output);
            Path successDirectory = Paths.get(output.toString() + "/success");
            Path failureDirectory = Paths.get(output.toString() + "/failure");
            Files.createDirectories(successDirectory);
            Files.createDirectories(failureDirectory);
            success = Paths.get(successDirectory.toString() + "/" + fileName);
            failure = Paths.get(failureDirectory.toString() + "/" + fileName);

            LOGGER.info("Creating success and failure directories for a file file {}", fileName);

        } catch (IOException e) {
            LOGGER.error("Cannot create success and failure directories for a file file {}", fileName);
            LOGGER.info("Exception. " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getEncoding(Path filePath)  {
        LOGGER.info("Trying to get file encoding for {}",filePath.getFileName());
        CharsetDetector detector = new CharsetDetector();
        try(BufferedInputStream reader  = new BufferedInputStream(new FileInputStream(filePath.toFile()))) {
            detector.setText(reader);
        } catch (IOException e ) {
            LOGGER.info("Exception. " + e.getMessage());
            e.printStackTrace();
        }
        LOGGER.info("File encoding for {} is {}",filePath.getFileName(),detector.detect().getName());
        return detector.detect().getName();
    }

    private static String[] getColumnNames(String line){
        LOGGER.info("Trying to get file delimiter");
        String[] initial = splitLine(line, "[ ;\\t]+");
        for (String splitter : SPLITTERS){
            String[] holder = splitLine(line, splitter);
            if(holder.length == initial.length){
                delimiter = splitter;
                LOGGER.info("File delimiter is {}",delimiter);
                return holder;
            }
        }
        return initial;
    }

    private static String[] getColumnValues(String line){
        LOGGER.info("Splitting line {}", line);
        return splitLine(line, delimiter);
    }

    private static String[] splitLine(String line, String splitter){
        return Arrays.stream(line.split(splitter)).map(word ->
                word.replaceAll("\\p{C}", "")
        ).toArray(String[]::new);
    }
}
