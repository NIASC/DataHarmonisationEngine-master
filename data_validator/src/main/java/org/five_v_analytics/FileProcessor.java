package org.five_v_analytics;

import com.ibm.icu.text.CharsetDetector;
import org.five_v_analytics.exceptions.DataValidationException;
import org.five_v_analytics.factories.ValidatorFactory;
import org.five_v_analytics.validators.DataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;


public class FileProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileProcessor.class);
    private static Path success;
    private static Path failure;
    private static DataValidator validator;

    public static void process(String inputPath, String outputPath, String type) {
        validator = ValidatorFactory.getInstance(type);
        try {
            Files.walk(Paths.get(inputPath)).forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    createOutputDirectories(outputPath, filePath.getFileName().toString());
                    processLine(filePath);
                }
            });
        } catch (IOException e) {
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
                if (lineCount == 0) {
                    createColumnHeadersMap(failureWriter, line);
                } else {
                    LOGGER.info("Line number = {}", lineCount);
                    validateLine(successWriter, failureWriter, line);
                }
                lineCount++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createColumnHeadersMap(BufferedWriter failureWriter, String line) throws IOException {
        ColumnHeaderMapper.mapHeaderToIndex(splitLine(line));
        failureWriter.write(line + "\n");
    }

    private static void validateLine(BufferedWriter successWriter, BufferedWriter failureWriter, String line) throws IOException {
        Map<String, Integer> headers = ColumnHeaderMapper.getColumnMap();
        try {
            String temp = validator.validateAndReturnLine(headers, splitLine(line));
            successWriter.write(validator.validateAndReturnLine(headers, splitLine(line)) + "\n");
        }catch (DataValidationException e){
            failureWriter.write(line);
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getEncoding(Path filePath)  {
        CharsetDetector detector = new CharsetDetector();
        try(BufferedInputStream reader  = new BufferedInputStream(new FileInputStream(filePath.toFile()))) {
            detector.setText(reader);
        } catch (IOException e ) {
            e.printStackTrace();
        }
        return detector.detect().getName();
    }

    private static String[] splitLine(String line){
        return Arrays.stream(line.split("[ ;\\t]+")).map(word ->
                word.replaceAll("\\p{C}", "")
        ).toArray(String[]::new);
    }
}
