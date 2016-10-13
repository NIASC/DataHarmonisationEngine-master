package org.five_v_analytics;


import com.ibm.icu.text.CharsetDetector;
import org.five_v_analytics.exceptions.DataValidationException;
import org.five_v_analytics.validators.DataValidator;
import org.five_v_analytics.validators.DataValidatorImpl;
//TODO: import org.five_v_analytics.MultipleCharsetDetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.Arrays;
import java.util.Map;


public class FileProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileProcessor.class);
	private static final String[] SPLITTERS = { ";", ",", "\t", " ", "[\t]+", "[ ]+" };
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
				if (!filePath.getFileName().toString().equals(".DS_Store")) {
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
				System.out.println("Reading Raw CSV data line number: " + lineCount);
				LOGGER.info("Reading CSV data: " + lineCount);
				if (lineCount == 0) {
					createColumnHeadersMap(failureWriter, successWriter, line);
					//get column names to define length of it
					columnNames = getColumnNames(line);
					LOGGER.info("columnNames.length: " + columnNames.length);
				} else {
					LOGGER.info("Line number = {}", lineCount);
					if (ColumnHeaderMapper.getColumnMap().isEmpty()) {
						validateColumn(successWriter, failureWriter, line);
					} else {
						validateLine(successWriter, failureWriter, line, columnNames.length);
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
		
		//TODO: here it will be much better to guess based on value, eg 1st is labcode, 2nd is pnr etc //Integer.parseInt(columnNames[0].trim()) == 88
		if((columnNames[0].trim().equals("88")) && ColumnHeaderMapper.getColumnMap().isEmpty()) {
			String lab088_header = "labcode,pnr,sampleyear,referralnr,referral_type,sample_date,reg_date,rem_clinic,ans_clinic,deb_clinic,doctor,sample_nr,topo,diag_nr,snomed,sample_type,obliterated,county,municip"; 
			ColumnHeaderMapper.mapHeaderToIndex(splitLine(lab088_header, ","));
			LOGGER.info("File doesn't have the header line, but it seems files comes from lab 088 and its columnNames are: " + lab088_header);
		}else if ((columnNames[0].trim().equals("127")) && ColumnHeaderMapper.getColumnMap().isEmpty()) {
			String lab127_header = "labcode,pnr,sampleyear,referralnr,referraltype,sampledate,remclinic,ansclinic,debclinic,doctor,samplenr,snomed,sampletype,obliterated,county,municip,responsedate"; 
			ColumnHeaderMapper.mapHeaderToIndex(splitLine(lab127_header, ","));
			LOGGER.info("File doesn't have the header line, but it seems files comes from lab 088 and its columnNames are: " + lab127_header);
		}

		if (ColumnHeaderMapper.getColumnMap().isEmpty()) {
			LOGGER.info("File doesn't have the header line");
			validateColumn(successWriter, failureWriter, line);
			return;
		}
		LOGGER.info("Writing column headers in success and failure files");
		// \\//:
		// successWriter.write(line + "\n");
		successWriter.write("labCode,countyCode,pnr,sampleYear,regDate,sampleDate,snomed,topoCode,refNR,remClinic,ansClinic,sampleNR,diagNR,responseDate,diagDate,scrType,Flag_correct,Comment"+ "\n");
		LOGGER.info("Writing column headers in failure file");
		failureWriter.write(line + "\n");
	}

	private static void validateColumn(BufferedWriter successWriter, BufferedWriter failureWriter, String line) {
		for (String splitter : SPLITTERS) {
			String[] splicedLine = splitLine(line, splitter);
			String result = "";
			if (splicedLine.length > 1) {
				for (String value : splicedLine) {
					String validationResult = validator.validateColumnValue(value);
					if (validationResult == null) {
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

	private static void validateLine(BufferedWriter successWriter, BufferedWriter failureWriter, String line, int columnNames_length)
			throws IOException {
		// LOGGER.info("Validating line [{}]", line);
		Map<String, Integer> headers = ColumnHeaderMapper.getColumnMap();
		
		//Check if length of line is equal to length of 
		if ( getColumnValues(line).length == columnNames_length) {
			try {
				// \\//:
				// successWriter.write(validator.validateAndReturnLine(headers,
				// getColumnValues(line)) + "\n");
				successWriter.write(validator.validateAndReturnLine(headers, getColumnValues(line)).toString().replaceAll(" ", "").replaceAll("\\[", "").replaceAll("\\]", "") + "\n");
				LOGGER.info("validation finished successfully for line [{}]"); // line
			} catch (DataValidationException e) {
				LOGGER.info("Validation failed for line [{}], writing to failure directory"); // line
				failureWriter.write(line + "\n");
			}
		} else {
			LOGGER.info("Validation failed for line [{}], it has diffeternt length than its headers. writing to failure directory"); // line
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
    
	private static String getEncoding(Path filePath) {
		LOGGER.info("Trying to get file encoding for {}", filePath.getFileName());
		
		CharsetDetector detector = new CharsetDetector();
		 		
		try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(filePath.toFile()))) {
			detector.setText(reader);
		} catch (IOException e) {
			LOGGER.info("Exception. " + e.getMessage());
			e.printStackTrace();
		}
		LOGGER.info("File encoding for {} is {}", filePath.getFileName(), detector.detect().getName());
		return detector.detect().getName();
	}

	private static String[] getColumnNames(String line) {
		LOGGER.info("Trying to get file delimiter");
		String[] initial = splitLine(line, "[ ;\\t]+");
		for (String splitter : SPLITTERS) {
			String[] holder = splitLine(line, splitter);
			if (holder.length == initial.length) {
				delimiter = splitter;
				LOGGER.info("File delimiter is {}", delimiter);
				return holder;
			}
		}
		return initial;
	}

	private static String[] getColumnValues(String line) {
		// LOGGER.info("Splitting line {}", line);
		return splitLine(line, delimiter);
	}

	private static String[] splitLine(String line, String splitter) {
		return Arrays.stream(line.split(splitter)).map(word -> word.replaceAll("\\p{C}", "")).toArray(String[]::new);
	}
}
