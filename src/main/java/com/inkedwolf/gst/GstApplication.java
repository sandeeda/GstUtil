package com.inkedwolf.gst;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class GstApplication {

    public static void main(String[] args) {
        SpringApplication.run(GstApplication.class, args);
        String sourceFilePath = "src/main/resources/source.csv";
        String targetFilePath = "src/main/resources/target2.csv";

        try (CSVReader reader = new CSVReader(new FileReader(sourceFilePath)); CSVWriter writer = new CSVWriter(new FileWriter(targetFilePath))) {

            // Reading header from source
            String[] sourceHeader = reader.readNext();
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < sourceHeader.length; i++) {
                headerIndex.put(sourceHeader[i], i);
            }

            // Define the target CSV header
            String[] targetHeader = {"Invoice_Date", "Invoice_Number", "Product_Name", "HSN", "IGST_%", "Quantity", "Amount_Before_Tax", "Amount_After_Tax", "SGST", "CGST", "IGST", "Total_Tax", "Bill_To_State"}; // add other fields as needed
            writer.writeNext(targetHeader);

            String[] sourceRow;
            while ((sourceRow = reader.readNext()) != null) {
                // Extracting and merging required fields
                String invoiceDate = sourceRow[headerIndex.get("Invoice_Date")];
                String invoiceNumber = sourceRow[headerIndex.get("Invoice_Number")];
                String productName = sourceRow[headerIndex.get("Product_Name")];
                String quantity = sourceRow[headerIndex.get(" Quantity")];
                String hsn = sourceRow[headerIndex.get("HSN")];
                String billToState = sourceRow[headerIndex.get("Bill_To_State")];

                // Merging required fields
                String igstPercent = mergeValues(sourceRow, headerIndex, "IGST_%", "Ship_IGST_%");
                String amountBeforeTax = mergeValues(sourceRow, headerIndex, "Amount_Before_Tax", "Ship_Amount_Before_Gst");
                String amountAfterTax = mergeValues(sourceRow, headerIndex, "Amount_After_Tax", "Ship_Amount_After_Gst");
                String sgst = mergeValues(sourceRow, headerIndex, "SGST", "Ship_SGST");
                String cgst = mergeValues(sourceRow, headerIndex, "CGST", "Ship_CGST");
                String igst = mergeValues(sourceRow, headerIndex, "IGST", "Ship_IGST");

                // Calculating Total_Tax as the sum of SGST, CGST, and IGST
                double totalTax = parseDouble(sgst) + parseDouble(cgst) + parseDouble(igst);

                // Mapping remaining fields one-to-one from source to target
/*        String otherField1 = sourceRow[headerIndex.get("Other_Field1")];
        String otherField2 = sourceRow[headerIndex.get("Other_Field2")];*/
                // Add other fields as necessary

                // Writing to target CSV
                String[] targetRow = {invoiceDate, invoiceNumber, productName, hsn, igstPercent, quantity, amountBeforeTax, amountAfterTax, sgst, cgst, igst, String.valueOf(totalTax), billToState/*,
            otherField1, otherField2*/ /*, ... additional fields */};
                writer.writeNext(targetRow);
            }

            System.out.println("Conversion complete. Target CSV created at " + targetFilePath);

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    // Helper method to merge two values
    private static String mergeValues(String[] row, Map<String, Integer> headerIndex, String field1, String field2) {
        double value1 = parseDouble(row[headerIndex.get(field1)]);
        double value2 = parseDouble(row[headerIndex.get(field2)]);
        return String.valueOf(value1 + value2);
    }

    // Helper method to parse a string as a double with default value 0
    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
