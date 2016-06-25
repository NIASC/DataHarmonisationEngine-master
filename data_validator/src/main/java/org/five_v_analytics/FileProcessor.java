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
import java.util.Arrays;
import java.util.Map;


public class FileProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileProcessor.class);
    private static final String[] SPLITTERS = {";",",","\t"," "};
    private static Path success;
    private static Path failure;
    private static DataValidator validator;
    private static String[] columnNames;

    public static void process(String inputPath, String outputPath, String type) {
        validator = ValidatorFactory.getInstance(type);
        try {
            Files.walk(Paths.get(inputPath)).forEach(filePath -> {
                if(!filePath.getFileName().toString().equals(".DS_Store")){
                    if (Files.isRegularFile(filePath)) {
                        createOutputDirectories(outputPath, filePath.getFileName().toString());
                        processLine(filePath);
                    }
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
        columnNames = getColumnNames(line);
        ColumnHeaderMapper.mapHeaderToIndex(columnNames);
        failureWriter.write(line + "\n");
    }

    private static void validateLine(BufferedWriter successWriter, BufferedWriter failureWriter, String line) throws IOException {
        Map<String, Integer> headers = ColumnHeaderMapper.getColumnMap();
        try {
            successWriter.write(validator.validateAndReturnLine(headers, getColumnValues(line)) + "\n");
        }catch (DataValidationException e){
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

    private static String[] getColumnNames(String line){
        return splitLine(line, "[ ;\\t]+");
    }

    private static String[] getColumnValues(String line){
        for (String splitter : SPLITTERS){
            String[] holder = splitLine(line, splitter);
            if(holder.length == columnNames.length){
                return holder;
            }
        }
       return splitLine(line, "[;\t ]+");

    }

    private static String[] splitLine(String line, String splitter){
        return Arrays.stream(line.split(splitter)).map(word ->
                word.replaceAll("\\p{C}", "")
        ).toArray(String[]::new);
    }
}
