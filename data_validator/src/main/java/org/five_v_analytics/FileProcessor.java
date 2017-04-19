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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FileProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileProcessor.class);
	private static final String[] SPLITTERS = { ";", ",", "\t", " ", "[\t]+", "[ ]+", "[; \\t ,]+"};
	private static Path success;
	private static Path failure;
	private static DataValidator validator;
	private static String[] columnNames;
	private static String delimiter = "";

	public static void process(String inputPath, String outputPath, String phrase, String type) {

		validator = new DataValidatorImpl(type, inputPath, phrase);
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
			int doctorPos;

			LOGGER.info("Processing file {}", filePath.getFileName().toString());
			while ((line = reader.readLine()) != null) {
				System.out.println("Reading Raw CSV data line number: " + lineCount);
				LOGGER.info("Reading CSV data: " + lineCount);
				//if we are reading first file create createColumnHeadersMap
				if (lineCount == 0) {
					createColumnHeadersMap(failureWriter, successWriter, line);
					//get column names to define length of it
					columnNames = getColumnNames(line);										
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

	private static void createColumnHeadersMap(BufferedWriter failureWriter, BufferedWriter successWriter, String line)
			throws IOException {
		LOGGER.info("Mapping column headers");
		columnNames = getColumnNames(line);
		ColumnHeaderMapper.mapHeaderToIndex(columnNames);

		//TODO: here it will be much better to guess based on value, eg 1st is labcode, 2nd is pnr etc
		if((columnNames[0].trim().equals("88") || columnNames[0].trim().equals("088")) &&
				ColumnHeaderMapper.getColumnMap().isEmpty()) {
			String lab088_header = "labcode,pnr,sampleyear,referralnr,scr_type,sample_date,reg_date,rem_clinic," +
					"ans_clinic,deb_clinic,doctor,sample_nr,topo,diag_nr,snomed,sample_type,obliterated,county,municip";
			ColumnHeaderMapper.mapHeaderToIndex(splitLine(lab088_header, ","));
			LOGGER.info("File doesn't have the header line, but it seems files comes from lab 088 and its columnNames " +
					"are: " + lab088_header);
		} else if ((columnNames[0].trim().equals("127")) && ColumnHeaderMapper.getColumnMap().isEmpty()) {
			String lab127_header = "labcode,pnr,sampleyear,referralnr,referraltype,sampledate,regdate,remclinic," +
					"ansclinic,debclinic,doctor,samplenr,scr_type,snomed,sampletype,obliterated,county,municip," +
					"responsedate";
			ColumnHeaderMapper.mapHeaderToIndex(splitLine(lab127_header, ","));
			LOGGER.info("File doesn't have the header line, but it seems files comes from lab 127 and its columnNames are: "
					+ lab127_header);
		} else if ((columnNames[0].trim().equals("551")) && ColumnHeaderMapper.getColumnMap().isEmpty()) {
			LOGGER.info("File doesn't have the header line, but it seems files comes from lab 551 and its " +
					"columnNames are: ");
			String lab551_header = "labcode,pnr,sampleyear,referralnr,scrtype,sampledate,regdate,remclinic,ansclinic," +
					"debclinic,doctor,notnown1,notnown2,snomed,sampletype,obliterated,county,municip";
			ColumnHeaderMapper.mapHeaderToIndex(splitLine(lab551_header, ","));
			LOGGER.info("File doesn't have the header line, but it seems files comes from lab 551 and its columnNames are: " +
					lab551_header);
		} else if ((columnNames[0].trim().equals("541")) && ColumnHeaderMapper.getColumnMap().isEmpty()) {
			LOGGER.info("File doesn't have the header line, but it seems files comes from lab 541 and its columnNames are: ");
			String lab541_header = "labcode,pnr,sampleyear,referralnr,scrtype,sampledate,regdate,remclinic,ansclinic,debclinic," +
					"doctor,notnown1,notnown2,snomed,sampletype,obliterated,county,municip";
			ColumnHeaderMapper.mapHeaderToIndex(splitLine(lab541_header, ",")); 
			LOGGER.info("File doesn't have the header line, but it seems files comes from lab 541 and its columnNames are: " +
					lab541_header);
		} else if ((columnNames[0].trim().equals("641")) && ColumnHeaderMapper.getColumnMap().isEmpty()) {
            LOGGER.info("File doesn't have the header line, but it seems files comes from lab 641 and its columnNames are: ");
            String lab641_header = "labcode,pnr,sampleyear,referralnr,referraltype,sampledate,diagdate,remclinic," +
					"ansclinic,debclinic,doctor,samplenr,topocode,diagnr,snomed,sampletype,obliterated,county,municip";
            ColumnHeaderMapper.mapHeaderToIndex(splitLine(lab641_header, ","));
            LOGGER.info("File doesn't have the header line, but it seems files comes from lab 541 and its columnNames are: " +
					lab641_header);

        } else if ((columnNames[0].trim().equals("241")) && ColumnHeaderMapper.getColumnMap().isEmpty()) {
            LOGGER.info("File doesn't have the header line, but it seems files comes from lab 641 and its columnNames are: ");
            String lab241_header = "labcode,pnr,sampleyear,referralnr,referraltype,sampledate,diagdate,remclinic,ansclinic," +
					"debclinic,doctor,samplenr,topocode,diagnr,snomed,sampletype,obliterated,county,municip";
            ColumnHeaderMapper.mapHeaderToIndex(splitLine(lab241_header, ","));
            LOGGER.info("File doesn't have the header line, but it seems files comes from lab 541 and its " +
					"columnNames are: " + lab241_header);
        } else if ((columnNames[0].trim().equals("251")) && ColumnHeaderMapper.getColumnMap().isEmpty()) {
            LOGGER.info("File doesn't have the header line, but it seems files comes from lab 641 and its columnNames are: ");
            String lab251_header = "labcode,pnr,sampleyear,referralnr,referraltype,sampledate,diagdate,remclinic," +
					"ansclinic,debclinic,doctor,samplenr,topocode,diagnr,snomed,sampletype,obliteratedcounty," +
					"municip,responsedate";
            ColumnHeaderMapper.mapHeaderToIndex(splitLine(lab251_header, ","));
            LOGGER.info("File doesn't have the header line, but it seems files comes from lab 541 and its columnNames are: "
					+ lab251_header);
        } else if ((columnNames[0].trim().equals("571")) && ColumnHeaderMapper.getColumnMap().isEmpty()) {
            LOGGER.info("File doesn't have the header line, but it seems files comes from lab 641 and its columnNames are: ");
            String lab571_header = "labcode,pnr,sampleyear,referralnr,referraltype,sampledate,diagdate,remclinic,ansclinic," +
					"debclinic,doctor,samplenr,topocode,diagnr,snomed,sampletype,obliteratedcounty,municip";
            ColumnHeaderMapper.mapHeaderToIndex(splitLine(lab571_header, ","));
            LOGGER.info("File doesn't have the header line, but it seems files comes from lab 541 and its columnNames are: "
					+ lab571_header);
        } else if ((columnNames[0].trim().equals("611")) && ColumnHeaderMapper.getColumnMap().isEmpty()) {
            LOGGER.info("File doesn't have the header line, but it seems files comes from lab 641 and its columnNames are: ");
            String lab611_header = "labcode,pnr,sampleyear,referralnr,referraltype,sampledate,diagdate,remclinic,ansclinic," +
					"debclinic,doctor,samplenr,topocode,diagnr,snomed,sampletype,obliterated,county,municip,responsedate";
            ColumnHeaderMapper.mapHeaderToIndex(splitLine(lab611_header, ","));
            LOGGER.info("File doesn't have the header line, but it seems files comes from lab 541 and its columnNames are: "
					+ lab611_header);
        } else if ((columnNames[0].trim().equals("621")) && ColumnHeaderMapper.getColumnMap().isEmpty()) {
            LOGGER.info("File doesn't have the header line, but it seems files comes from lab 641 and its columnNames are: ");
            String lab621_header = "labcode,pnr,sampleyear,referralnr,referraltype,,sampledate,diagdate,remclinic,ansclinic," +
					"debclinic,doctor,samplenr,topocode,diagnr,snomed,sampletype,obliterated,county,municip";
            ColumnHeaderMapper.mapHeaderToIndex(splitLine(lab621_header, ","));
            LOGGER.info("File doesn't have the header line, but it seems files comes from lab 541 and its columnNames are: "
					+ lab621_header);
        }  else if ((columnNames[0].trim().equals("641")) && ColumnHeaderMapper.getColumnMap().isEmpty()) {
            LOGGER.info("File doesn't have the header line, but it seems files comes from lab 641 and its columnNames are: ");
            String lab641_header = "labcode,pnr,sampleyear,referralnr,referraltype,sampledate,diagdate,remclinic,ansclinic," +
					"debclinic,doctor,samplenr,topocode,diagnr,snomed,sampletype,obliterated,county,municip,responsedate";
            ColumnHeaderMapper.mapHeaderToIndex(splitLine(lab641_header, ","));
            LOGGER.info("File doesn't have the header line, but it seems files comes from lab 541 and its " +
					"columnNames are: " + lab641_header);
        }   else if ((columnNames[0].trim().equals("631")) && ColumnHeaderMapper.getColumnMap().isEmpty()) {
			LOGGER.info("File doesn't have the header line, but it seems files comes from lab 631 and its columnNames are: ");
			String lab631_header = "labcode,pnr,sampleyear,referralnr,referraltype,sampledate,diagdate,remclinic," +
					"ansclinic,debclinic,doctor,samplenr,topocode,diagnr,snomed,sampletype,obliterated,county,municip";
			ColumnHeaderMapper.mapHeaderToIndex(splitLine(lab631_header, ","));
			LOGGER.info("File doesn't have the header line, but it seems files comes from lab 541 and its " +
					"columnNames are: " + lab631_header);
		} else if (ColumnHeaderMapper.getColumnMap().isEmpty()) {
			LOGGER.info("File doesn't have the header line");
			validateColumn(successWriter, failureWriter, line);
			return;
		}
		LOGGER.info("Writing column headers in success and failure files");
		// \\//:
		// successWriter.write(line + "\n");
		successWriter.write("labCode,countyCode,pnr,sampleYear,regDate,sampleDate,diagDate,responseDate,Age,birthDate,scrType," +
				"snomed,hpv_cell,topoCode,refNR,remClinic,ansClinic,sampleNR,diagNR,File_type,Flag_correct,Comment,"+ "\n");
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

	private static void validateLine(BufferedWriter successWriter, BufferedWriter failureWriter, String line,
									 int columnNames_length) throws IOException {
		// LOGGER.info("Validating line [{}]", line);
		Map<String, Integer> headers = ColumnHeaderMapper.getColumnMap();
	    LOGGER.info("headers: " + headers); 
		
		//TODO:-----------------------------------------------------------------------
		//check if columnNames contains doctor as it sometimes contains name surname
		//and might create problems during tokenizing, so column names doesn't correpont to column valies
		//It is writtnet on fly so needs refactoring 
		
		//create new updates headrers
		final Map<String, Integer> updated_headers_less = new HashMap<>();		
		final Map<String, Integer> updated_headers_more = new HashMap<>();		

		//populate each of them 
		for (String key : headers.keySet()) {
			updated_headers_more.put(key, headers.get(key)); 
		}

		for (String key : headers.keySet()) {
			updated_headers_less.put(key, headers.get(key)); 
		}
	
		//now determine position of doctor valieble
		int docPos = -1;		
		if ( ! (headers.get("Doctor") ==  null)){
			docPos = headers.get("Doctor"); 
		}

		LOGGER.info("The length of values is : " + getColumnValues(line).length + " and columnNames_length is " +
				columnNames_length);

		//Check if length of line is equal to length of 
		if ( getColumnValues(line).length == columnNames_length) {
			try {
				// \\//:
				// successWriter.write(validator.validateAndReturnLine(headers, getColumnValues(line)) + "\n");
				successWriter.write(validator.validateAndReturnLine(headers, getColumnValues(line)).toString()
						.replaceAll(" ", "").replaceAll("\\[", "").replaceAll("\\]", "") + "\n");
				LOGGER.info("validation finished successfully for line [{}]"); // line
			} catch (DataValidationException e) {
				LOGGER.info("Validation failed for line [{}], writing to failure directory"); // line
				failureWriter.write(line + "\n");
			}
		} else if ( getColumnValues(line).length < columnNames_length && docPos >= 0) {
			//TODO:-----------------------------------------------------------------------
			//shift columns after doctor
			for (String key : updated_headers_less.keySet()) {
				if ( updated_headers_less.get(key) > docPos){
					updated_headers_less.put(key, updated_headers_less.get(key) - (columnNames_length -
							getColumnValues(line).length));
				} 
			}
		    LOGGER.info("headers: " + headers); 
		    LOGGER.info("final_updated_headers_less: " + updated_headers_less); 

			try {
			    successWriter.write(validator.validateAndReturnLine(updated_headers_less,
						getColumnValues(line)).toString().replaceAll(" ", "").replaceAll("\\[", "")
						.replaceAll("\\]", "") + "\n");
			    LOGGER.info("validation finished successfully for line [{}]"); // line
			} catch (DataValidationException e) {
				LOGGER.info("Validation failed for line [{}], writing to failure directory"); // line
				failureWriter.write(line + "\n");
			}
		} else if ( getColumnValues(line).length > columnNames_length && docPos >= 0){
			//TODO:-----------------------------------------------------------------------
			//shift columns after doctor
			for (String key : updated_headers_more.keySet()) {
				if ( updated_headers_more.get(key) > docPos){
					updated_headers_more.put(key, updated_headers_more.get(key) + (getColumnValues(line).length -
							columnNames_length));
				}
			}
		    LOGGER.info("updated_headers_more" + updated_headers_more); 
			try {
			    successWriter.write(validator.validateAndReturnLine(updated_headers_more,
						getColumnValues(line)).toString().replaceAll(" ", "").replaceAll("\\[", "").
						replaceAll("\\]", "") + "\n");
			    LOGGER.info("validation finished successfully for line [{}]"); // line
			} catch (DataValidationException e) {
				LOGGER.info("Validation failed for line [{}], writing to failure directory"); // line
				failureWriter.write(line + "\n");
			}
		} else {
			LOGGER.info("Validation failed for line [{}], it has diffeternt length " +
					getColumnValues(line).length + " than its headers = "  + columnNames_length + " " +
					"writing to failure directory"); // line
			failureWriter.write(line + "\n");
		}
		
	}

	private static void createOutputDirectories(String outputPath, String fileName) {
		try {
			//\\//:
			Path output = Paths.get(outputPath); // + "/" + fileName
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
		String[] initial = splitLine(line, "[; \\t ,]+");
		for (String splitter : SPLITTERS) {
			String[] holder = splitLine(line, splitter);
            LOGGER.info("holder.length = " + holder.length +  " and initial.length = "
					+ initial.length + " when splitter = " + splitter);
			if (holder.length == initial.length) {
				delimiter = splitter;
				LOGGER.info("File delimiter is {}", delimiter);
				return holder;
			}
		}
		return initial;
	}

	private static String[] getColumnValues(String line) {
		//LOGGER.info("Splitting line {}", line);
		return splitLine(line, delimiter);
	}

	private static String[] splitLine(String line, String splitter) {
		return Arrays.stream(line.split(splitter)).map(word -> word.replaceAll("\\p{C}", "")).toArray(String[]::new);
	}
}
