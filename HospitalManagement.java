import java.io.*;
import java.util.*;

class AdminManagement {

    void deleteDetails(String path, String id) throws IOException {
        File inputFile = new File(path);
        File tempFile = new File("temp.txt");
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            try (BufferedWriter historyWriter = new BufferedWriter(new FileWriter("History.txt", true))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmedLine = line.trim();
                    String[] details = trimmedLine.split(",");

                    if (details.length > 0 && details[0].equals(id)) {
                        historyWriter.write(trimmedLine);
                        historyWriter.newLine();
                        found = true;
                    } else {
                        writer.write(trimmedLine);
                        writer.newLine();
                    }
                }
            }
        }

        if (found) {
            if (inputFile.delete()) {
                tempFile.renameTo(inputFile);
                System.out.println("\n✅ Record deleted successfully.");
            } else {
                tempFile.delete();
                System.out.println("\n❌ ERROR: Failed to delete the original file. Deletion process aborted.");
            }
        } else {
            tempFile.delete();
            System.out.println("\n⚠️ Error: The specified ID was not found in the records.");
        }
    }

    boolean uniqueChecker(String id, String path) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.trim().split(",");
                if (details.length > 0 && id.equals(details[0])) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean validatePassword(String id, String password, String path) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.trim().split(",");
                if (details.length > 3 && id.equals(details[0]) && password.equals(details[3])) {
                    return true;
                }
            }
        }
        return false;
    }
}

class Admin {
    private final AdminManagement adminManager = new AdminManagement();

    void printAllAppointments() throws IOException {
        System.out.println("\n----------- All Patient Appointments -----------");
        String path = "Hospital.txt";
        boolean hasRecords = false;
        System.out.printf("| %-10s | %-20s | %-10s | %-25s | %-10s |%n", "ID", "Name", "Gender", "Ailment", "Doctor ID");
        System.out.println("----------------------------------------------------------------------------------");
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.trim().split(",");
                if (details.length >= 5) {
                    System.out.printf("| %-10s | %-20s | %-10s | %-25s | %-10s |%n",
                            details[0], details[1], details[2], details[3], details[4]);
                    hasRecords = true;
                }
            }
        }
        if (!hasRecords) {
            System.out.println("No patient appointments found.");
        }
        System.out.println("----------------------------------------------------------------------------------");
    }

    void printDoctors() throws IOException {
        System.out.println("\n--------- Physician Directory ------------");
        String path = "Doctors.txt";
        boolean hasDoctors = false;
        System.out.printf("| %-10s | %-20s | %-25s |%n", "ID", "Name", "Specialization");
        System.out.println("--------------------------------------------------------------------");
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.trim().split(",");
                if (details.length >= 3) {
                    System.out.printf("| %-10s | %-20s | %-25s |%n",
                            details[0], details[1], details[2]);
                    hasDoctors = true;
                }
            }
        }
        if (!hasDoctors) {
            System.out.println("No doctor records found.");
        }
        System.out.println("--------------------------------------------------------------------");
    }

    void addDoctors(Scanner scanner) throws IOException {
        scanner.nextLine();
        String path = "Doctors.txt";
        System.out.print("Enter Doctor ID (Numeric): ");
        String id = scanner.nextLine().trim();

        if (!adminManager.uniqueChecker(id, path)) {
            System.out.println("❌ ID already exists. Cannot add doctor.");
            return;
        }

        System.out.print("Enter Doctor Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Specialization (e.g., Cardiology, Neurology): ");
        String specialization = scanner.nextLine().trim();
        System.out.print("Set Initial Password: ");
        String password = scanner.nextLine().trim();
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, true))) {
            writer.write(id + "," + name + "," + specialization + "," + password);
            writer.newLine();
        }
        System.out.println("\n✅ Doctor added successfully. Specialization: " + specialization);
    }

    void deleteDoctor(Scanner scanner) throws IOException {
        scanner.nextLine();
        System.out.print("Enter Doctor ID to be deleted: ");
        String id = scanner.nextLine().trim();
        adminManager.deleteDetails("Doctors.txt", id);
    }

    void printDeletedDetails() throws IOException {
        System.out.println("\n--------- Deleted Records (History) ------------");
        String path = "History.txt";
        boolean hasHistory = false;
        System.out.printf("| %-10s | %-20s | %-25s |%n", "ID", "Name", "Details/Ailment");
        System.out.println("--------------------------------------------------------------------");
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.trim().split(",");
                if (details.length >= 3) { 
                    System.out.printf("| %-10s | %-20s | %-25s |%n", details[0], details[1], details[2]);
                    hasHistory = true;
                }
            }
        }
        if (!hasHistory) {
            System.out.println("No deleted records in history.");
        }
        System.out.println("--------------------------------------------------------------------");
    }
}

class Doctor {
    private final String id;
    private final AdminManagement adminManager = new AdminManagement();

    Doctor(String id) {
        this.id = id;
    }

    void printAppointments() throws IOException {
        System.out.println("\n----------- Appointments for Dr. " + id + " -----------");
        if (adminManager.uniqueChecker(id, "Doctors.txt")) {
            System.out.println("❌ Error: Doctor ID not found in system.");
            return;
        }

        boolean foundAppointments = false;
        System.out.printf("| %-10s | %-20s | %-10s | %-25s |%n", "Patient ID", "Name", "Gender", "Ailment");
        System.out.println("--------------------------------------------------------------------");
        try (BufferedReader reader = new BufferedReader(new FileReader("Hospital.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.trim().split(",");
                if (details.length > 4 && id.equals(details[4])) {
                    System.out.printf("| %-10s | %-20s | %-10s | %-25s |%n",
                            details[0], details[1], details[2], details[3]);
                    foundAppointments = true;
                }
            }
        }
        if (!foundAppointments) {
            System.out.println("No appointments scheduled for you today.");
        }
        System.out.println("--------------------------------------------------------------------");
    }

    void noOfAppointments() throws IOException {
        if (!adminManager.validatePassword(id, "dummy_password", "Doctors.txt")) {
            if (adminManager.uniqueChecker(id, "Doctors.txt")) {
                 System.out.println("❌ Error: Doctor ID not found in system.");
            }
            return;
        }

        String path = "Hospital.txt";
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.trim().split(",");
                if (details.length > 4 && id.equals(details[4])) {
                    count++;
                }
            }
        }
        System.out.println("\nNumber of Active Appointments: " + count);
    }
}

class Staff {
    private final AdminManagement adminManager = new AdminManagement();

    void viewPatientDetails(Scanner scanner) throws IOException {
        scanner.nextLine();
        System.out.print("Enter ID of Patient: ");
        String id = scanner.nextLine().trim();
        
        boolean found = false;
        System.out.println("--------------------------------------------------------------------");
        try (BufferedReader reader = new BufferedReader(new FileReader("Hospital.txt"))) {
            String line;
            while((line=reader.readLine())!=null){
                String[] details = line.trim().split(",");
                if(details.length > 3 && id.equals(details[0])){
                    found = true;
                    System.out.printf("| %-10s | %-20s | %-10s | %-25s |%n", "ID", "Name", "Gender", "Ailment");
                    System.out.println("--------------------------------------------------------------------");
                    System.out.printf("| %-10s | %-20s | %-10s | %-25s |%n",
                            details[0], details[1], details[2], details[3]);
                }
            }
            if(!found)
                System.out.println("⚠️ Patient ID not found.");
        }
        System.out.println("--------------------------------------------------------------------");
    }

    void addPatient(Scanner scanner) throws IOException {
        scanner.nextLine();
        String path = "Hospital.txt";
        System.out.print("Enter Patient ID (Numeric): ");
        String id = scanner.nextLine().trim();

        if (!adminManager.uniqueChecker(id, path)) {
            System.out.println("❌ Patient ID already exists.");
            return;
        }

        System.out.print("Enter Patient Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Patient Gender: ");
        String gender = scanner.nextLine().trim();
        System.out.print("Enter Patient Ailment/Disease: ");
        String disease = scanner.nextLine().trim();
        
        String doctorId = getDoctorId(disease);
        
        if (doctorId.isEmpty()) {
             System.out.println("❌ Error: No doctors available for assignment based on ailment. Please add a doctor first.");
             return;
        }

        String details = String.join(",", id, name, gender, disease, doctorId);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, true))) {
            writer.write(details);
            writer.newLine();
        }
        System.out.println("\n✅ Patient intake successful. Assigned to Doctor ID: " + doctorId);
    }

    String getDoctorId(String disease) throws IOException {
        String defaultId = "";
        try (BufferedReader reader = new BufferedReader(new FileReader("Doctors.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.trim().split(",");
                if (details.length >= 3) {
                    if (disease.equalsIgnoreCase(details[2].trim())) {
                        return details[0];
                    }
                    defaultId = details[0];
                }
            }
        }
        return defaultId;
    }

    void deletePatient(Scanner scanner) throws IOException {
        scanner.nextLine();
        System.out.print("Enter Patient ID to be discharged: ");
        String id = scanner.nextLine().trim();
        adminManager.deleteDetails("Hospital.txt", id);
    }
}

public class HospitalManagement {
    
    private static boolean authenticateDoctor(Scanner scanner, String doctorId) throws IOException {
        scanner.nextLine();
        System.out.print("Enter Password for Dr. " + doctorId + ": ");
        String password = scanner.nextLine().trim();
        AdminManagement manager = new AdminManagement();
        
        if (manager.validatePassword(doctorId, password, "Doctors.txt")) {
            System.out.println("✅ Authentication successful.");
            return true;
        } else {
            System.out.println("❌ Authentication failed. Invalid ID or Password.");
            return false;
        }
    }

    public static void main(String[] args) throws IOException {
        new File("Doctors.txt").createNewFile();
        new File("Hospital.txt").createNewFile();
        new File("History.txt").createNewFile();
        
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n=================================================");
            System.out.println("      🏥 PROFESSIONAL HOSPITAL MANAGEMENT SYSTEM 🏥");
            System.out.println("=================================================");
            System.out.println("1. Physician Portal (Login Required)");
            System.out.println("2. Staff Portal");
            System.out.println("3. Administrator Portal");
            System.out.println("4. Exit Application");
            System.out.print("Enter your choice: ");

            if (!scanner.hasNextInt()) {
                System.out.println("\n❌ Invalid input. Please enter a number (1-4).");
                scanner.nextLine();
                choice = 0;
                continue;
            }
            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.println("\n---------- Physician Portal Login -------------");
                    scanner.nextLine();
                    System.out.print("Enter Doctor ID: ");
                    String doctorId = scanner.nextLine().trim();
                    
                    if (authenticateDoctor(scanner, doctorId)) {
                        Doctor doctor = new Doctor(doctorId);
                        System.out.println("\n---------- Dr. " + doctorId + "'s Dashboard ----------");
                        System.out.println("1. View Active Appointment Count");
                        System.out.println("2. View Patient Appointments List");
                        System.out.print("Enter option: ");
                        int doctorOption = scanner.nextInt();

                        try {
                            switch (doctorOption) {
                                case 1 -> doctor.noOfAppointments();
                                case 2 -> doctor.printAppointments();
                                default -> System.out.println("❌ Enter a valid option.");
                            }
                        } catch (IOException e) {
                             System.err.println("An error occurred during file operation: " + e.getMessage());
                        }
                    }
                    break;

                case 2:
                    System.out.println("\n---------- Staff Portal ----------");
                    Staff staff = new Staff();
                    System.out.println("1. View Patient Record");
                    System.out.println("2. Patient Intake (Add New)");
                    System.out.println("3. Discharge Patient (Delete Record)");
                    System.out.print("Enter option: ");
                    int staffOption = scanner.nextInt();
                    
                    try {
                         switch (staffOption) {
                            case 1 -> staff.viewPatientDetails(scanner);
                            case 2 -> staff.addPatient(scanner);
                            case 3 -> staff.deletePatient(scanner);
                            default -> System.out.println("❌ Enter valid Option (1-3).");
                        }
                    } catch (IOException e) {
                        System.err.println("An error occurred during file operation: " + e.getMessage());
                    }
                    break;

                case 3:
                    System.out.println("\n--------- Administrator Portal ----------");
                    System.out.println("1. View Physician Directory");
                    System.out.println("2. Add New Physician");
                    System.out.println("3. Delete Physician Record");
                    System.out.println("4. View All Patient Appointments");
                    System.out.println("5. View Deleted Records History");
                    System.out.print("Enter option: ");
                    int adminOption = scanner.nextInt();
                    Admin admin = new Admin();

                    try {
                         switch (adminOption) {
                            case 1 -> admin.printDoctors();
                            case 2 -> admin.addDoctors(scanner);
                            case 3 -> admin.deleteDoctor(scanner);
                            case 4 -> admin.printAllAppointments();
                            case 5 -> admin.printDeletedDetails();
                            default -> System.out.println("❌ Enter a valid option (1-5).");
                        }
                    } catch (IOException e) {
                        System.err.println("An error occurred during file operation: " + e.getMessage());
                    }
                    break;
                    
                case 4:
                    System.out.println("\nExiting Application. Thank you for using the system! 👋");
                    break;
                    
                default:
                    System.out.println("\n❌ Enter a Valid option from the main menu (1-4).");
            }
        } while (choice != 4);
        
        scanner.close();
    }
}