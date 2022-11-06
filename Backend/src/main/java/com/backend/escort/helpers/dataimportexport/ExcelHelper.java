package com.backend.escort.helpers.dataimportexport;

/*
 * The following class makes use of APACHE POI
 */

import com.backend.escort.helpers.Helper;
import com.backend.escort.model.CustomDriver;
import com.backend.escort.model.Driver;
import com.backend.escort.model.Student;
import com.backend.escort.model.Trip;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.ByteArrayInputStream;

public class ExcelHelper {
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /*
     * Driver exported file(s)
     */
    static String[] DRIVER_HEADERS = {"Id", "Title", "FirstName", "LastName", "Email", "Gender", "DateCreated", "Verified", "Org","Suspended","AdminId"};
    static String DRIVER_EXCEL_SHEET = "Drivers";

    /*
     * Trips exported file(s)
     */
    static String[] TRIP_HEADERS = {"Id", "PickUp", "Destination", "DateCreated", "DriverId", "Student", "OrgId"};
    static String TRIP_EXCEL_SHEET = "Trips";

    // Export list of drivers to an excel file
    public static ByteArrayInputStream driversToExcelSheet(
            List<Driver> drivers
    ) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
        ) {

            Sheet sheet = workbook.createSheet(DRIVER_EXCEL_SHEET);
            Row headerRow = sheet.createRow(0);
            for (int c = 0; c < DRIVER_HEADERS.length; c++) {
                Cell cell = headerRow.createCell(c);
                // Adding values from the array at each header column
                cell.setCellValue(DRIVER_HEADERS[c]);
            }

            //  {"Id", "Title", "FirstName", "LastName", "email" , "Gender", "DateCreated", "Verified" , "orgName"};
            int rowIndex = 1;
            for (Driver driver : drivers) {

                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(driver.getId());
                row.createCell(1).setCellValue(driver.getTitle());
                row.createCell(2).setCellValue(driver.getFirstName());
                row.createCell(3).setCellValue(driver.getLastName());
                row.createCell(4).setCellValue(driver.getUser().getEmail());
                row.createCell(5).setCellValue(driver.getGender());
                row.createCell(6).setCellValue(driver.getDateCreated());
                row.createCell(7).setCellValue(driver.isVerified());
                row.createCell(8).setCellValue(driver.getOrganisation().getName());
                row.createCell(9).setCellValue(driver.isSuspended());
                row.createCell(10).setCellValue(driver.getAdminId());
            }

            workbook.write(byteArrayOutputStream);
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

        } catch (Exception exception) {
            throw new RuntimeException("DATA EXPORT FAILED");
        }
    }

    // Export all trip data to an excel file
    public static ByteArrayInputStream tripsToExcelSheet(List<Trip> trips) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
        ) {

            Sheet sheet = workbook.createSheet(TRIP_EXCEL_SHEET);
            Row headerRow = sheet.createRow(0);
            for (int c = 0; c < TRIP_HEADERS.length; c++) {
                Cell cell = headerRow.createCell(c);
                // Adding values from the array at each header column
                cell.setCellValue(TRIP_HEADERS[c]);
            }

            int rowIndex = 1;
            for (Trip trip : trips) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(trip.getId());
                row.createCell(1).setCellValue(trip.getPickUp());
                row.createCell(2).setCellValue(trip.getDestination());
                row.createCell(3).setCellValue(trip.getDateCreated());
                row.createCell(4).setCellValue(trip.getDriverId());
                row.createCell(5).setCellValue(trip.getStudent());
                row.createCell(6).setCellValue(trip.getOrgId());
            }

            workbook.write(byteArrayOutputStream);
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

        } catch (Exception exception) {
            throw new RuntimeException("DATA EXPORT FAILED");
        }
    }


    // Check if the uploaded file is an  excel file
    public static boolean isExcelFile(MultipartFile multipartFile) {
        return TYPE.equals(multipartFile.getContentType());
    }

    // Import driver data from an excel sheet and store in our database
    public static List<CustomDriver> importExcelFile(InputStream inputStream) {
        try {
            Workbook workbook = new XSSFWorkbook(inputStream);

            Sheet sheet = workbook.getSheet(DRIVER_EXCEL_SHEET);
            Iterator<Row> rowIterator = sheet.iterator();
            List<CustomDriver> drivers = new ArrayList<>();
            int current = 0;

            while (rowIterator.hasNext()) {
                Row currentRow = rowIterator.next();
                if (current == 0) {
                    current++;
                    continue;
                }

                Iterator<Cell> cellIterator = currentRow.iterator();
                CustomDriver driver = new CustomDriver();
                int cellIndex = 0;
                while (cellIterator.hasNext()) {
                    Cell currentCell = cellIterator.next();
                    // {"Id", "Title", "FirstName", "LastName", "email" , "Gender", "DateCreated", "Verified" , "orgName"};
                    switch (cellIndex) {
                        case 0:
                            driver.setId((long) currentCell.getNumericCellValue());
                            break;
                        case 1:
                            driver.setTitle(currentCell.getStringCellValue());
                            break;
                        case 2:
                            driver.setFirstName(currentCell.getStringCellValue());
                            break;
                        case 3:
                            driver.setLastName(currentCell.getStringCellValue());
                            break;
                        case 4:
                            driver.setEmail(currentCell.getStringCellValue());
                            break;
                        case 5:
                            driver.setGender(currentCell.getStringCellValue());
                            break;
                        case 6:
                            driver.setDateCreated(Helper.getCurrentTimeDate());
                            break;
                        case 7:
                            driver.setVerified(currentCell.getBooleanCellValue());
                            break;
                        case 8:
                            driver.setOrganisationName(currentCell.getStringCellValue());
                            break;
                        case 9:
                            driver.setSuspended(currentCell.getBooleanCellValue());
                            break;
                        case 10:
                            driver.setAdminId((long) currentCell.getNumericCellValue());
                            break;

                        default:
                            break;
                    }
                    cellIndex++;
                }

                drivers.add(driver);
            }
            workbook.close();
            return drivers;

        } catch (IOException e) {
            throw new RuntimeException("Failed to read file " + e.getMessage());
        }
    }

}
