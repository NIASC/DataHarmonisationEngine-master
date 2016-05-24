LABCODE – Is validated against table county2lab.txt

PNR – Validated using Luhn algorithm (10-modul) and PNR regular expressions (In the log file also generate the % of invalid PNRs in the file)

SAMPLE_YEAR – Numeric and between 1960 and current year

REFERRAL_NR (remissnummer) – Not validated, lab specific.

SCR_TYPE - (Screening type) A few labs report this, expected values are ‘1’ or ‘2’. For those who don’t report it, we have a code exit (Xit2_raw_pad_cell.sas) where the scr_type is determined (lab specific rules).

SAMPLEDATE – Normal validation. If invalid, take the REG_DATE if ok, otherwise assume 01 JULY

REGDATE – Normal date validation

COUNTY – Validated against county2lab.txt

MUNICIP or RESIDK – Kommun, Currently not validated

TOPO/TOPOCODE – Only relevant for PAD data (TODO: Generate frequency tables of topocodes for inspection)

SNOMED – Never rejected. Instead:
      ***Frequency tables of proportion of unknown codes should be an output, if too many unknowns -rejection.
      ***Secondary variable: snomed translated can be generated using the NKCx translatorn Table.

SAMPLETYPE – No validation

REMCLINIC/ANSCLINIC/DEBCLINIC – No validation

REFSITE – No validation

DOCTOR – No validation

SAMPLENR – No validation

DIAGNR – No validation

OBLITERATED – No validation

RESPONSEDATE – Normal date validation
 
Regarding the Cytology Snomed codes try to harmonize based on the table Nkc_snomed_cyt.csv. To accomplish this translation table (Nkc_translation_cell_diag.csv) is used. It is also used to build the Translation matrix, which resembles all the combinations of Year/lab/reported_diagnoses(snomed) per sample  – it defines the UCODES and SNOMED_WORST for this specific sample. To assist in interpreting the unknown snomed codes additional table (Nkc_klartext.csv) is provided. 
