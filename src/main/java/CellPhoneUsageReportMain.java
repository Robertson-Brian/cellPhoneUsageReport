import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.text.SimpleDateFormat;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

public class CellPhoneUsageReportMain {

    public static void main(String[] args) {
        ArrayList<List<String>> cellPhone = new ArrayList<List<String>>();
        ArrayList<List<String>> cellPhoneUsage = new ArrayList<List<String>>();

        readInFile(cellPhone, "src/main/resources/CellPhone.csv");
        readInFile(cellPhoneUsage, "src/main/resources/CellPhoneUsageByMonth.csv");
        generateReport(cellPhone, cellPhoneUsage);
    }

    static void readInFile(ArrayList<List<String>> table, String file){
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                table.add(Arrays.asList(values));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void generateReport(ArrayList<List<String>> cellPhone, ArrayList<List<String>> cellPhoneUsage){
        ArrayList<List<String>> reportCSV = new ArrayList<List<String>>();
        ArrayList<Date> dates = new ArrayList<Date>();
        int totalMonths = 1;
        int totalMinutes = 0;
        int totalData = 0;

        //add first half of report header row
        reportCSV.add(new ArrayList<String>());
        reportCSV.get(0).add("employee ID");
        reportCSV.get(0).add("employee name");
        reportCSV.get(0).add("employee model");
        reportCSV.get(0).add("purchase date");

        //pull dates out of CSV file
        for(int i = 1; i < cellPhoneUsage.size(); i++){
            try {
                dates.add(new SimpleDateFormat("MM/dd/yyyy").parse(cellPhoneUsage.get(i).get(1)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            totalMinutes+=parseInt(cellPhoneUsage.get(i).get(2));
            totalData+=parseFloat(cellPhoneUsage.get(i).get(3));
        }

        //sort dates
        Collections.sort(dates);

        //Add a column to the report for each phone
        for(int i = 1; i < cellPhone.size(); i++) {
            reportCSV.add(new ArrayList<String>());
            reportCSV.get(i).add(cellPhone.get(i).get(0));//employeeId
            reportCSV.get(i).add(cellPhone.get(i).get(1));//employeeName
            reportCSV.get(i).add(cellPhone.get(i).get(3));//model
            reportCSV.get(i).add(cellPhone.get(i).get(2));//purchaseDate
        }

        //add a minutes/data column to the header row for each month the report will show
        reportCSV.get(0).add("minutes " + (dates.get(0).getMonth()+1) + "/" + Integer.toString(dates.get(0).getYear()).substring(1));
        reportCSV.get(0).add("data " + (dates.get(0).getMonth()+1) + "/" + Integer.toString(dates.get(0).getYear()).substring(1));

        for (int i = 1; i < dates.size(); i++) {
            if(dates.get(i).getMonth() != dates.get(i-1).getMonth() || dates.get(i).getYear() != dates.get(i-1).getYear()){
                totalMonths++;
                reportCSV.get(0).add("minutes " + (dates.get(i).getMonth()+1) + "/" + Integer.toString(dates.get(i).getYear()).substring(1));

                reportCSV.get(0).add("data " + (dates.get(i).getMonth()+1) + "/" + Integer.toString(dates.get(i).getYear()).substring(1));
            }
        }

        //fill all minutes/data non header rows with zeros
        for(int i = 1; i < reportCSV.size(); i++){
            for (int p = 0; p < totalMonths*2; p++) {
                reportCSV.get(i).add("0");
            }
        }

        //traverse data and populate the report with the minutes/data values
        for(int i = 1; i < cellPhoneUsage.size(); i++) {
            for(int k = 1; k < reportCSV.size(); k++) {
                if(cellPhoneUsage.get(i).get(0).equals(reportCSV.get(k).get(0))){
                    //records match
                    //check date and check against header row to add minutes and data to correct column
                    for(int p = 4; p < reportCSV.get(0).size(); p++) {
                        String[] tmp = cellPhoneUsage.get(i).get(1).split("/");
                        String[] tmp2 = reportCSV.get(0).get(p).split("/");
                        String[] tmp3 = tmp2[0].split(" ");

                        if(tmp3[1].equals(tmp[0]) && tmp2[1].equals(tmp[2].substring(2))){
                            //dates match

                            String s = String.valueOf(cellPhoneUsage.get(i).get(2));
                            reportCSV.get(k).set(p, s);

                            String s2 = String.valueOf(cellPhoneUsage.get(i).get(3));
                            reportCSV.get(k).set(p+1, s2);

                            break;
                        }
                    }
                }
            }
        }

        //write report to file
        String reportFileName = "src/main/out/CellPhoneUsageReport" + new Date() + ".csv";

        try (PrintWriter writer = new PrintWriter(reportFileName)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Date Report Run : " + new Date() + "\n");
            sb.append("Number Of Phones : " + (cellPhone.size() - 1) + "\n");
            sb.append("Total Minutes : " + totalMinutes + "\n");
            sb.append("Total Data : " + totalData + "\n");
            sb.append("Average Minutes (monthly): " + totalMinutes/totalMonths + "\n");
            sb.append("Average Data (monthly): " + totalData/totalMonths + "\n");
            sb.append('\n');

            for(int i = 0; i < reportCSV.size(); i++) {
                for (int k = 0; k < reportCSV.get(i).size(); k++) {
                    sb.append(reportCSV.get(i).get(k));
                    if(i < reportCSV.size()){
                        sb.append(',');
                    }
                }
                sb.append('\n');
            }
            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}