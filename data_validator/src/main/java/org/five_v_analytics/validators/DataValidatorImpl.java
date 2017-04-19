package org.five_v_analytics.validators;

import org.five_v_analytics.exceptions.DataValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.*;

public class DataValidatorImpl implements DataValidator {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataValidatorImpl.class);
	//TODO: Snomed path should go as input option
	private static final String CELL_SNOMED_PATH = "/home/davbzh/DataHarmonisationEngine-master/data_validator/snomed_codes/nkc_translation_cell_diag.csv";
	private static final String PAD_SNOMED_PATH = "/home/davbzh/DataHarmonisationEngine-master/data_validator/snomed_codes/nkc_translation_pad_diag.csv";
	private static final String CELL_PAD_SNOMED_PATH = "/home/davbzh/DataHarmonisationEngine-master/data_validator/snomed_codes/nkc_translation_cell_pad_diag.csv";
	private static final int COUNTY_NUMBER = 25;
	private static final int RESEARCH_START_YEAR = 1960;
	private static final Map<String, String> countyLabCodes;
	private static final Map<String, String> referalOrganaisedTypes;
	private static final Map<String, String> HPVsnomedCodes;
	private static final Map<String, String> CELLsnomedCodes;
	public static Map<String, String> snomedCodes = new HashMap<>();
	private String snomedPath;
	private String inputfileName;
	private String  extraHPV;
	private String  phrase;

	public DataValidatorImpl(String type, String inputPath, String phrase) {
		//TODO: we need to create one hashmap for all snomed codes
		this.snomedPath = "";
		this.inputfileName =  "";
		this.extraHPV = "";
		this.phrase = phrase;

		if( type.equals("c")){
				this.snomedPath = CELL_SNOMED_PATH;
				generateSnomedList();
		} else if (type.equals("p")) {
				this.snomedPath = PAD_SNOMED_PATH;
				generateSnomedList();
		} else if (type.equals("cp")) {
				this.snomedPath = CELL_PAD_SNOMED_PATH;
				generateSnomedList();
		} else if (type.equals("e")) {
				this.inputfileName =  inputPath;
				this.extraHPV = "HPV";
		}
	}

	static {
		countyLabCodes = new HashMap<>();
		countyLabCodes.put("051", "01");
		countyLabCodes.put("088", "01");
		countyLabCodes.put("51", "01");
		countyLabCodes.put("88", "01");
		countyLabCodes.put("121", "03");
		countyLabCodes.put("127", "03");
		countyLabCodes.put("131", "04");
		countyLabCodes.put("211", "05");
		countyLabCodes.put("231", "06");
		countyLabCodes.put("241", "07");
		countyLabCodes.put("251", "08");
		countyLabCodes.put("261", "09");
		countyLabCodes.put("851", "09"); //medilab
		countyLabCodes.put("271", "10");
		countyLabCodes.put("431", "12");
		countyLabCodes.put("437", "12");
		countyLabCodes.put("287", "12");
		countyLabCodes.put("307", "12");
		countyLabCodes.put("417", "12");
		countyLabCodes.put("421", "13");
		countyLabCodes.put("427", "13");
		countyLabCodes.put("501", "14");
		countyLabCodes.put("507", "14");
		countyLabCodes.put("511", "14");
		countyLabCodes.put("517", "14");
		countyLabCodes.put("521", "14");
		countyLabCodes.put("527", "14");
		countyLabCodes.put("531", "14");
		countyLabCodes.put("541", "17");
		countyLabCodes.put("551", "18");
		countyLabCodes.put("561", "19");
		countyLabCodes.put("571", "20");
		countyLabCodes.put("611", "21");
		countyLabCodes.put("621", "22");
		countyLabCodes.put("631", "23");
		countyLabCodes.put("641", "24");
		countyLabCodes.put("651", "25");
		countyLabCodes.put("657", "25");
	}

	static {
		referalOrganaisedTypes = new HashMap<>();
		referalOrganaisedTypes.put("VK", "1");
		referalOrganaisedTypes.put("H", "1");
		referalOrganaisedTypes.put("HL", "1");
		referalOrganaisedTypes.put("G", "1");
		referalOrganaisedTypes.put("ZH", "1");
		referalOrganaisedTypes.put("S", "1");
		referalOrganaisedTypes.put("GH", "1");
		referalOrganaisedTypes.put("HK", "1");
		referalOrganaisedTypes.put("R", "1");
		referalOrganaisedTypes.put("YD", "1");
		referalOrganaisedTypes.put("Y", "1");
		referalOrganaisedTypes.put("YS", "1");
		referalOrganaisedTypes.put("VD", "1");
		referalOrganaisedTypes.put("1", "1");
		referalOrganaisedTypes.put("5", "1");

		referalOrganaisedTypes.put("V", "2");
		referalOrganaisedTypes.put("ZV", "2");
		referalOrganaisedTypes.put("IL", "2");
		referalOrganaisedTypes.put("GYN", "2");
		referalOrganaisedTypes.put("CD", "2");
		referalOrganaisedTypes.put("C", "2");
		referalOrganaisedTypes.put("CS", "2");
		referalOrganaisedTypes.put("2", "2");
		referalOrganaisedTypes.put("4", "2");

		referalOrganaisedTypes.put("", "3");
		referalOrganaisedTypes.put("3", "3");
	}
	
	static {
		HPVsnomedCodes = new HashMap<>();
		HPVsnomedCodes.put("F02B33", "HPV");
		HPVsnomedCodes.put("M091A6", "HPV");
		HPVsnomedCodes.put("M09024", "HPV");
		HPVsnomedCodes.put("E334990", "HPV");
		HPVsnomedCodes.put("E334999", "HPV");
		HPVsnomedCodes.put("E33406", "HPV");
		HPVsnomedCodes.put("E33411", "HPV");
		HPVsnomedCodes.put("E33416", "HPV");
		HPVsnomedCodes.put("E33418", "HPV");
		HPVsnomedCodes.put("E33426", "HPV");
		HPVsnomedCodes.put("E33431", "HPV");
		HPVsnomedCodes.put("E33433", "HPV");
		HPVsnomedCodes.put("E33435", "HPV");
		HPVsnomedCodes.put("E33439", "HPV");
		HPVsnomedCodes.put("E33440", "HPV");
		HPVsnomedCodes.put("E33442", "HPV");
		HPVsnomedCodes.put("E33443", "HPV");
		HPVsnomedCodes.put("E33444", "HPV");
		HPVsnomedCodes.put("E33445", "HPV");
		HPVsnomedCodes.put("E33450", "HPV");
		HPVsnomedCodes.put("E33451", "HPV");
		HPVsnomedCodes.put("E33452", "HPV");
		HPVsnomedCodes.put("E33453", "HPV");
		HPVsnomedCodes.put("E33454", "HPV");
		HPVsnomedCodes.put("E33456", "HPV");
		HPVsnomedCodes.put("E33458", "HPV");
		HPVsnomedCodes.put("E33459", "HPV");
		HPVsnomedCodes.put("E33461", "HPV");
		HPVsnomedCodes.put("E33462", "HPV");
		HPVsnomedCodes.put("E33466", "HPV");
		HPVsnomedCodes.put("E33468", "HPV");
		HPVsnomedCodes.put("E33470", "HPV");
		HPVsnomedCodes.put("E33471", "HPV");
		HPVsnomedCodes.put("E33472", "HPV");
		HPVsnomedCodes.put("E33473", "HPV");
		HPVsnomedCodes.put("E33481", "HPV");
		HPVsnomedCodes.put("E33482", "HPV");
		HPVsnomedCodes.put("E33483", "HPV");
		HPVsnomedCodes.put("E33484", "HPV");
		HPVsnomedCodes.put("E33485", "HPV");
		HPVsnomedCodes.put("E33489", "HPV");
	}

	static {
		CELLsnomedCodes = new HashMap<>();
		CELLsnomedCodes.put("M00110", "CELL");
		CELLsnomedCodes.put("M09005", "CELL");
		CELLsnomedCodes.put("M09010", "CELL");
		CELLsnomedCodes.put("M09019", "CELL");
		CELLsnomedCodes.put("M69700", "CELL");
		CELLsnomedCodes.put("M69710", "CELL");
		CELLsnomedCodes.put("M69719", "CELL");
		CELLsnomedCodes.put("M69720", "CELL");
		CELLsnomedCodes.put("M74006", "CELL");
		CELLsnomedCodes.put("M74007", "CELL");
		CELLsnomedCodes.put("M76700", "CELL");
		CELLsnomedCodes.put("M80009", "CELL");
		CELLsnomedCodes.put("M80702", "CELL");
		CELLsnomedCodes.put("M80703", "CELL");
		CELLsnomedCodes.put("M81403", "CELL");
	}

	protected String cleanPNR (String value) {
		// Remove dash and plus
		value = value.trim().replace("-", "").replace("+", "");
        if (value.length() == 10) {
			value = "19" + value; //.substring(0, 10);
		}
		return value;
	}

	//protected String validateLabCode(String labCode, String county) throws DataValidationException {
	protected String validateLabCode(String labCode, String county) {
			LOGGER.info("Validating LabCode {} and County {}", labCode, county);
		if ( !labCode.trim().matches("\\d+")){
			LOGGER.error("Validation Error, labCode {} contains illigeal characters", labCode);
			//throw new DataValidationException();
		}
		/*TODO: Check if this is necesaary
		if (!validateCounty(county) || !countyLabCodes.get(labCode).equals(county)) {
			LOGGER.error("Validation Error, County code {} and labCode {} doesn't match, Check it", county, labCode);
			// throw new DataValidationException();
		}
		*/
		return labCode;
	}

	protected boolean validateLabCode(String value) {
		return countyLabCodes.containsKey(value);
	}

	protected boolean validateCounty(String county) {
		return county.trim().matches("^\\d{2}") && Integer.parseInt(county) <= COUNTY_NUMBER;
	}

	protected boolean validateSwedishPersonalNumber(String value) {
		LOGGER.info("Validating Swedish personal number");
		String personalNumberHash;
		// Check for nulls and false lengths
		if (value == null || value.length() < 10) {
			LOGGER.error("Validation Error, personal number is incorrect", value);
			return false;
		}

		try {
			// Remove dash and plus
			value = cleanPNR(value);

			String century = "";
			// Remove century and check number
			if (value.length() == 12) {
				century = value.substring(0, 2);
				value = value.substring(2, 12);
			} else if (value.length() == 10) {
				value = value.substring(0, 10);
			} else {
				LOGGER.error("Validation Error, personal number is incorrect", value);
				return false;
			}

			// Remove check number
			int check = Integer.parseInt(value.substring(9, 10));
			String sValue = value.substring(0, 9);

			int result = 0;

			// Calculate check number
			for (int i = 0, len = sValue.length(); i < len; i++) {
				int tmp = Integer.parseInt(sValue.substring(i, i + 1));

				if ((i % 2) == 0) {
					tmp = (tmp * 2);
				}

				if (tmp > 9) {
					result += (1 + (tmp % 10));
				} else {
					result += tmp;
				}
			}

			boolean isValid = (((check + result) % 10) == 0);
			boolean isSSN = Integer.parseInt(value.substring(2, 4), 10) < 13 && Integer.parseInt(value.substring(4, 6), 10) < 32;
			boolean isCoOrdinationNumber = Integer.parseInt(value.substring(4, 6), 10) > 60;
			boolean isMale = !((Integer.parseInt(value.substring(8, 9)) % 2) == 0);
			boolean isCompany = Integer.parseInt(value.substring(2, 4), 10) >= 20;
			return isValid;
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
		}
		LOGGER.error("Validation Error, personal number is incorrect", value);
		return false;
	}

	protected String encryptPersonalNumber(String pnr, String phrase) {

		String pnrphrase = phrase + pnr + phrase;
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		byte[] hash = digest.digest(pnrphrase.getBytes());
		return String.format("%064x", new java.math.BigInteger(1, hash));
	}

	private void generateSnomedList() {
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(snomedPath), Charset.forName("Cp1252"))) {
			String line;
			String[] lines;
			int lineCount = 0;
			while ((line = reader.readLine()) != null) {
				if (lineCount > 0) {
					lines = line.split(";");
					snomedCodes.put(lines[0].trim(), lines[1].trim());
				}
				lineCount++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected String validateSnomed(String snomed) {
		LOGGER.error("Validating snomed", snomed);
		// TODO: Check in the outputs if white spaces and hyphen removal is
		// necessary: .replace(" ", "").replace("-", "")
		String regSnomed = snomedCodes.get(snomed.trim());
		if (regSnomed != null) {
			return regSnomed;
		} else {
			LOGGER.error("Snomed: {} didn't match any Reg_snomed", snomed);
			// \\//:
			// return snomed;
			return "NO_Reg_snomed";
		}
	}

	//\\//:
	public static long getDifferenceYears(String string_d1, String string_d2) {
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		Date startDate;
		Date endDate;
		long diff = 0; 
		try {
			startDate = df.parse(string_d1);
			endDate = df.parse(string_d2);		
		    diff = endDate.getTime() - startDate.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS))/365;
	}

	//\\//:
	public String validateAndReturnLine(Map<String, Integer> columns, String[] data) throws DataValidationException {
		int labCode_pos;
		//int countyCode_pos;
		int pnr_pos;
		int sampleYear_pos;
		int regDate_pos;
		int sampleDate_pos;
		int snomed_pos;
		int topoCode_pos;
		int refNR_pos;
		int remClinic_pos;
		int ansClinic_pos;
		int sampleNR_pos;
		int diagNR_pos;
		int responseDate_pos;
		int diagDate_pos;
		int scrType_pos;

		String flagCorrect = "1";
		String snomedComment = "";
		String pnrComment = "";
		String remClinic = "";
		String labCode = "";

		String birthDate = "";
		String regDate = "";
		String sampleDate = "";
		String diagDate  = "";
		String responseDate  = "";
		
		String fileTypeFlag  = "";
		
		List<String> sortedData = new ArrayList<String>();

		if (this.extraHPV.equals("HPV")){
			//if the file is exta HPV data
			if (columns.containsKey("labCode")) {
				try {
					labCode_pos = columns.get("labCode");
					labCode = data[labCode_pos].trim();
					sortedData.add(labCode);
				} catch (ArrayIndexOutOfBoundsException e) {
					sortedData.add("NA");
				}
			} else {
				sortedData.add("NA");
			}
			// This extra HPV files doenn't usually contain county codes so files are prepared in a way
			// that first word contians where it is comming from (e.g. "Stockholm.HPV2012.xlsx.csv").
			Path p = Paths.get(this.inputfileName);
			String file_name = p.getFileName().toString();
			LOGGER.info("file_name " + file_name);
			sortedData.add(file_name.split("_")[0]);
		} else {
			LOGGER.info("this.snomedPath " + this.snomedPath);
			LOGGER.info("this.extraHPV " + this.extraHPV);

			if (columns.containsKey("labCode")) { //&& columns.containsKey("countyCode")
				try {
					labCode_pos = columns.get("labCode");
					//countyCode_pos = columns.get("countyCode");
					labCode = data[labCode_pos].trim();
					validateLabCode(labCode, countyLabCodes.get(labCode)); //
					sortedData.add(labCode);
					sortedData.add(countyLabCodes.get(labCode));
				} catch (ArrayIndexOutOfBoundsException e) {
					//throw new DataValidationException();
					sortedData.add("NA");
					sortedData.add("NA");
					LOGGER.info("Line throws ArrayIndexOutOfBoundsException, please check...");
				}
			} else {
				//throw new DataValidationException();
				sortedData.add("NA");
				sortedData.add("NA");
				LOGGER.info("[labCode], [countyCode] Columns doesn't exist");
			}
		}

		if (columns.containsKey("pnr")) {
			pnr_pos = columns.get("pnr");
			if (validateSwedishPersonalNumber(data[pnr_pos])) {
				birthDate = cleanPNR(data[pnr_pos]).substring(0, 8);
				sortedData.add(encryptPersonalNumber(cleanPNR(data[pnr_pos]), this.phrase));
				LOGGER.info("birthDate is {}", birthDate);

			} else {
				// change flag to 0, which means line is incorrect
				flagCorrect = "0";
				pnrComment = "pnr_is_not_valid!";
				sortedData.add(encryptPersonalNumber(cleanPNR(data[pnr_pos]), this.phrase));
				LOGGER.info("Cannot validate [pnr]");
				LOGGER.info("Cannot create Birth Year");
				// throw new DataValidationException();
			}
		} else {
			sortedData.add("NA");
			LOGGER.info("[pnr] Column doesn't exist");
			//throw new DataValidationException();
		}

		if (columns.containsKey("sampleYear")) {
			sampleYear_pos = columns.get("sampleYear");
			validateSampleYear(data[sampleYear_pos]);
			sortedData.add(data[sampleYear_pos]);
		} else {
			sortedData.add("NA");
			LOGGER.info("[sampleYear] Column doesn't exist");
		}

		if (columns.containsKey("regDate")) {
			regDate_pos = columns.get("regDate");
			//sampleDate_pos = columns.get("sampleDate");
			validateRegistrationDate(data[regDate_pos]);
			//data[sampleDate_pos] = validateSampleDate(data[sampleDate_pos], data[regDate_pos]);
			regDate = data[regDate_pos];
			//sampleDate = data[sampleDate_pos].replace("-","").replace("/","");
			sortedData.add(regDate);
			//sortedData.add(sampleDate);
			LOGGER.info("regDate: {}", regDate);
		} else {
			sortedData.add("NA");
			LOGGER.info("Cannot validate [registration Date]");
		}

		if (columns.containsKey("sampleDate")) {
			sampleDate_pos = columns.get("sampleDate");
			LOGGER.info("sampleDate: {}", data[sampleDate_pos]);
			//data[sampleDate_pos] = validateSampleDate(data[sampleDate_pos]);
			//data[sampleDate_pos] = validateSampleDate(data[sampleDate_pos]);
			sampleDate = validateSampleDate(data[sampleDate_pos]); //data[sampleDate_pos].replace("-","").replace("/","");
			sortedData.add(sampleDate);
			LOGGER.info("returning sampleDate: {}", sampleDate);
		} else {
			sortedData.add("NA");
			LOGGER.info("Cannot validate [sampleDate Date]");
		}


		if (columns.containsKey("diagDate")) {
			diagDate_pos = columns.get("diagDate");
			if (!data[diagDate_pos].trim().isEmpty()) {
				diagDate = validateSampleDate(data[diagDate_pos]);
				sortedData.add(diagDate);
			} else {
				sortedData.add("NA");
			}
		} else {
			sortedData.add("NA");
		}

		if (columns.containsKey("responseDate")) {
			responseDate_pos = columns.get("responseDate");
			if (!data[responseDate_pos].trim().isEmpty()) {
				responseDate = validateSampleDate(data[responseDate_pos]);
				sortedData.add(responseDate);
			} else {
				sortedData.add("NA");
			}
		} else {
			sortedData.add("NA");
		}

		//add age variable 
		if (!birthDate.equals("") && !(sampleDate.equals("") || sampleDate.equals("NULL") || sampleDate.equals("NA") ||
				sampleDate.equals("00000000"))){
			sortedData.add(String.valueOf(getDifferenceYears(birthDate, sampleDate)));
			sortedData.add(birthDate.substring(0,6));

			LOGGER.info("sampleDate: {}", sampleDate);
			LOGGER.info("Age: {}", String.valueOf(getDifferenceYears(birthDate, sampleDate)));
		}  else if (!birthDate.equals("") && !(regDate.equals("") || regDate.equals("NULL") || regDate.equals("NA") ||
				regDate.equals("00000000"))){
			sortedData.add(String.valueOf(getDifferenceYears(birthDate, regDate)));
			sortedData.add(birthDate.substring(0,6));

			LOGGER.info("regDate: {}", regDate);
			LOGGER.info("Age: {}", String.valueOf(getDifferenceYears(birthDate, sampleDate)));
		} else if (!birthDate.equals("") && !(sampleDate.equals("") || sampleDate.equals("NULL") ||
				sampleDate.equals("NA") || regDate.equals("00000000"))){
			sortedData.add(String.valueOf(getDifferenceYears(birthDate, sampleDate)));
			sortedData.add(birthDate.substring(0,6));

			LOGGER.info("Age: {}", String.valueOf(getDifferenceYears(birthDate, sampleDate)));
		} else if (!birthDate.equals("") && !(diagDate.equals("") || diagDate.equals("NULL") ||
				diagDate.equals("NA") || diagDate.equals("00000000"))){
			sortedData.add(String.valueOf(getDifferenceYears(birthDate, diagDate)));
			sortedData.add(birthDate.substring(0,6));

			LOGGER.info("Age: {}", String.valueOf(getDifferenceYears(birthDate, sampleDate)));
		} else if (!birthDate.equals("") && !(responseDate.equals("") || responseDate.equals("NULL") ||
				responseDate.equals("NA") || responseDate.equals("00000000"))){
			sortedData.add(birthDate.substring(0,6));

			sortedData.add(String.valueOf(getDifferenceYears(birthDate, responseDate)));
			LOGGER.info("Age: {}", String.valueOf(getDifferenceYears(birthDate, sampleDate)));
     	} else {
			sortedData.add("NA");
			sortedData.add("NA");
			LOGGER.info("Cannot calculate Age...");
		}
		
		if (columns.containsKey("scrType")) {
			scrType_pos = columns.get("scrType");
			if (!data[scrType_pos].trim().isEmpty()) {
				if (referalOrganaisedTypes.containsKey(data[scrType_pos].trim())) {
					sortedData.add(referalOrganaisedTypes.get(data[scrType_pos].trim()));
				} else if ( !referalOrganaisedTypes.containsKey(data[scrType_pos].trim()) && (labCode.equals("88") ||
						labCode.equals("088"))) { //for 088 if non of refType then it is PAD
					sortedData.add("PAD");         //TODO: check if this is necessary at all
					fileTypeFlag  = "PAD";         //TODO: we need better solution for this 
				} else {
					sortedData.add("NA");
				}
			} else {
				sortedData.add("NA");
			}
		} else if (labCode.equals("541") || labCode.equals("571") || labCode.equals("611") || labCode.equals("241") ||
				labCode.equals("247")) {
			// From these labs referal types are extracted remClinic
			if (!remClinic.equals("")) {
				LOGGER.info("remClinic_referalOrganaisedTypes " + remClinic.substring(0, 2));
				if (referalOrganaisedTypes.containsKey(remClinic.substring(0, 2))) {
					sortedData.add(referalOrganaisedTypes.get(remClinic.substring(0, 2)));
				} else { // and if the key is not present in referalOrganaisedTypes, means 2 (only for these labs)
					sortedData.add("2");
				}
			} else {
				sortedData.add("NA");
			}
		} else {
			sortedData.add("NA");
		}
		
		if (columns.containsKey("snomed")) {
			snomed_pos = columns.get("snomed");
			String snomed = validateSnomed(data[snomed_pos]);
			if (snomed != "NO_Reg_snomed") {
				data[snomed_pos] = snomed;
				sortedData.add(data[snomed_pos]);
				//TODO: here add if snomed is about CELL or HPV from HPVsnomedCodes and CELLsnomedCodes
				if (HPVsnomedCodes.containsKey(snomed)){
					sortedData.add(HPVsnomedCodes.get(snomed));
				} else if (CELLsnomedCodes.containsKey(snomed)){
					sortedData.add(CELLsnomedCodes.get(snomed));					
				} else {
					sortedData.add("SNOMED_not_known");										
				}
			} else {
				// change flag to 0, which means line is incorrect
				flagCorrect = "0";
				snomedComment = "snomed_is_not_valid!";
				sortedData.add(data[snomed_pos]);
				//TODO: here add if snomed is not valid add NA what we know about snomed of CELL or HPV from
				//TODO:  HPVsnomedCodes and CELLsnomedCodes
				sortedData.add("NA");										
				LOGGER.info("Cannot validate [snomed]");
			}
		}

		if (columns.containsKey("topoCode")) {
			topoCode_pos = columns.get("topoCode");
			if (!data[topoCode_pos].trim().isEmpty()) {
				sortedData.add(data[topoCode_pos]);
			} else {
				sortedData.add("NA");
			}
		} else {
			sortedData.add("NA");
		}

		if (columns.containsKey("refNR")) {
			refNR_pos = columns.get("refNR");
			if (!data[refNR_pos].trim().isEmpty()) {
				sortedData.add(data[refNR_pos]);
			} else {
				sortedData.add("NA");
			}
		} else {
			sortedData.add("NA");
		}
		if (columns.containsKey("remClinic")) {
			remClinic_pos = columns.get("remClinic");
			if (!data[remClinic_pos].trim().isEmpty()) {
				remClinic = data[remClinic_pos];
				sortedData.add(remClinic);
			} else {
				sortedData.add("NA");
			}
		} else {
			sortedData.add("NA");
		}
		if (columns.containsKey("ansClinic")) {
			ansClinic_pos = columns.get("ansClinic");
			if (!data[ansClinic_pos].trim().isEmpty()) {
				sortedData.add(data[ansClinic_pos]);
			} else {
				sortedData.add("NA");
			}
		} else {
			sortedData.add("NA");
		}
		if (columns.containsKey("sampleNR")) {
			sampleNR_pos = columns.get("sampleNR");
			if (!data[sampleNR_pos].trim().isEmpty()) {
				sortedData.add(data[sampleNR_pos]);
			} else {
				sortedData.add("NA");
			}
		} else {
			sortedData.add("NA");
		}
		if (columns.containsKey("diagNR")) {
			diagNR_pos = columns.get("diagNR");
			if (!data[diagNR_pos].trim().isEmpty()) {
				sortedData.add(data[diagNR_pos]);
			} else {
				sortedData.add("NA");
			}
		} else {
			sortedData.add("NA");
		}

		//TODO: we need better solution for this
		if ( this.snomedPath == CELL_SNOMED_PATH ){
			sortedData.add("CELL");
		} else if ( this.snomedPath == PAD_SNOMED_PATH ){
			sortedData.add("PAD");
		} else if ( this.snomedPath == CELL_PAD_SNOMED_PATH && (labCode.equals("88") || labCode.equals("088")) &&
				fileTypeFlag.equals("PAD")) {
			sortedData.add("PAD");
		} else if ( this.snomedPath == CELL_PAD_SNOMED_PATH && (labCode.equals("88") || labCode.equals("088")) &&
				!fileTypeFlag.equals("PAD")) {
			sortedData.add("CELL");	
		} else {
		    sortedData.add("NA");
		}

		// TODO:
		if (flagCorrect == "1") {
			sortedData.add(flagCorrect);
			sortedData.add("Correct");
		} else if (flagCorrect == "0") {
			sortedData.add(flagCorrect);
			sortedData.add(pnrComment + snomedComment);
		}

		String[] sortedArray = new String[sortedData.size()];
		sortedData.toArray(sortedArray);
		// return Arrays.toString(data);
		return Arrays.toString(sortedArray);

	}

	public String validateColumnValue(String value) {
		if (validateSwedishPersonalNumber(value)) {
			return encryptPersonalNumber(value, this.phrase);
		}
		return (validateSampleYear(value) || validateFullDate(value) || validateCounty(value) || validateLabCode(value))
				? value : null;
	}

	//TODO: Should check all types of date formats
	private String validateSampleDate(String sampleDate) {
		LOGGER.info("Validating sampleDate");
		return sampleDate.trim().replace("-","").replace("/","");
		/*
		if (validateFullDate(sampleDate.trim())) {
			return sampleDate;
		} else {
			return validateFullDate(sampleDate.trim()) ? sampleDate : (Year.now().getValue() - 1) + "0601";
		}
		*/
	}

	private String validateSampleDate(String sampleDate, String regDate) {
		LOGGER.info("Validating Sample Date");
		if (validateFullDate(sampleDate.trim())) {
			return sampleDate.replace("-","").replace("/","");
		} else {
			return validateFullDate(regDate.trim()) ? regDate : (Year.now().getValue() - 1) + "0601";
		}
	}

	private boolean validateSampleYear(String sampleYear) {
		LOGGER.info("Validating Sample Year");
		if (!sampleYear.trim().matches("^\\d{4}") || Integer.parseInt(sampleYear) < RESEARCH_START_YEAR) {
			LOGGER.error("Sample year parsing error, {}", sampleYear);
			return false;
		}
		return true;
	}

	private boolean validateRegistrationDate(String regDate) {
		LOGGER.info("Validating Registration Date");
		return validateFullDate(regDate.trim());
	}

	private boolean validateFullDate(String fullDate) {
		LOGGER.info("Validating Full Date");
		DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("yyyyMMdd")
				.parseStrict().toFormatter();
		try {
			LocalDate.parse(fullDate.replace("-","").replace("/",""), formatter);
			return true;
		} catch (DateTimeParseException e) {
			LOGGER.error("Date parsing error, Data malformatted {}", fullDate);
			return false;
		}
	}
}
