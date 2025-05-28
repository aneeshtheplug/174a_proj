package org.yourcompany.yourproject;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;

public class ConsoleUI {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            ConsoleUI ui = new ConsoleUI();

            UniversityDAO dao = ui.get_connection();
            // TestConnection db = new TestConnection();
            System.out.println("✅ Connected to the database!");

            while (true) {
                System.out.println("\n\n===== IVC Console Menu =====");
                System.out.println("1. GOLD (for students)");
                System.out.println("2. Registrat (for staff)");
                System.out.println("0. Exit");
                System.out.print("Select an option: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline
                // scanner.close();

                switch (choice) {
                    case 1:         // gold interface
                        ui.gold_ui(dao);
                        break;

                    case 2:         // registrar interface
                        ui.registrar_ui(dao);
                        break;

                    case 0:         // exit
                        System.out.println("Exiting Database!");
                        return;

                    default:
                        System.out.println("Invalid option. Please try again");
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error:");
            e.printStackTrace();
        }
    }

    public UniversityDAO get_connection() {
        String DB_URL = "jdbc:oracle:thin:@aneesh174a_low?TNS_ADMIN=/Users/nilaykundu/Desktop/UCSB/3-JUNIOR/Spring/cs174a/Wallet_aneesh174a";
        String DB_USER = "ADMIN";
        String DB_PASSWORD = "oraclepassword174A";
    
        Properties info = new Properties();
        info.put(OracleConnection.CONNECTION_PROPERTY_USER_NAME, DB_USER);
        info.put(OracleConnection.CONNECTION_PROPERTY_PASSWORD, DB_PASSWORD);
        info.put(OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20");
    
        try {
            OracleDataSource ods = new OracleDataSource();
            ods.setURL(DB_URL);
            ods.setConnectionProperties(info);
    
            OracleConnection conn = (OracleConnection) ods.getConnection();
            return new UniversityDAO(conn); 
        } catch (SQLException e) {
            System.err.println("ERROR connecting to database:");
            e.printStackTrace();
            return null; 
        }
    }
    

    public void gold_ui(UniversityDAO dao){
        Scanner scanner = new Scanner(System.in);
        // TestConnection db = new TestConnection();

        int perm = -1;
        while (true) {
            System.out.print("Enter your 5-digit PERM number: ");
            String input = scanner.nextLine().trim();

            if (input.matches("\\d{5}")) {
                perm = Integer.parseInt(input);

                // if (db.valid_perm(perm)) {          // need to add a func to check       // CHANGE THISHSIHISSISISISISISISII
                if (perm > 9999) {          // need to add a func to check

                    System.out.println("Logged in with PERM: " + perm + "\n");
                    break;
                } else {
                    System.out.println("PERM not found in the database.\n");
                }
            } else {
                System.out.println("Invalid PERM format. Please enter exactly 5 digits.\n");
            }
        }

        while (true) {
            System.out.println("\n===== ⚜️ GOLD Menu ⚜️ =====");
            System.out.println("1. Add a course");
            System.out.println("2. Drop a course");
            System.out.println("3. List enrolled courses (this quarter)");
            System.out.println("4. View grades (previous quarter)");
            System.out.println("5. Requirements check");
            System.out.println("6. Make a plan");
            System.out.println("7. Change your PIN");
            System.out.println("0. Exit GOLD");
            System.out.print("Choose an option: ");
    
            String choice = scanner.nextLine().trim();
    
            switch (choice) {
                case "1":
                    System.out.print("To add a course, enter the 5-digit enrollment code: ");
                    String addInput = scanner.nextLine().trim();
                    if (addInput.matches("\\d{5}")) {
                        int code = Integer.parseInt(addInput);
                        System.out.println("📚 Adding course with code " + code + "...");
                        try {
                            boolean success = dao.enrollStudentInCourse(String.valueOf(perm), code);
                            if (success) {
                                System.out.println("✅ Successfully enrolled in course " + code + "!");
                            } else {
                                System.out.println("❌ Failed to enroll.");
                            }
                        } catch (SQLException e) {
                            System.out.println("❌ Enrollment failed: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Invalid enrollment code. Must be 5 digits.");
                    }
                    break;
            
                    case "2":
                    System.out.print("To drop a course, enter the 5-digit enrollment code: ");
                    String dropInput = scanner.nextLine().trim();
                    if (dropInput.matches("\\d{5}")) {
                        int code = Integer.parseInt(dropInput);
                        System.out.println("🗑️ Dropping course with code " + code + "...");
                        try {
                            boolean success = dao.dropStudentFromCourse(String.valueOf(perm), code);
                            if (success) {
                                System.out.println("✅ Successfully dropped course " + code + "!");
                            } else {
                                System.out.println("❌ Drop failed.");
                            }
                        } catch (SQLException e) {
                            System.out.println("❌ Drop failed: " + e.getMessage());
                        }
                    } else {
                        System.out.println("❌ Invalid enrollment code. Must be 5 digits.");
                    }
                    break;
            
                    case "3":
                    System.out.println("Listing current quarter courses...");
                    try {
                        List<UniversityDAO.CourseOffering> result = dao.listCurrentCourses(String.valueOf(perm));
                        if (result.isEmpty()) {
                            System.out.println("You are not enrolled in any courses this quarter.");
                        } else {
                            System.out.printf("%-8s | %-8s | %-30s | %-5s | %-5s | %-5s | %-8s | %-10s | %-4s | %-4s | %-15s%n",
                                "Enroll#", "Course", "Title", "Bldg", "Room", "Days", "Time", "Quarter", "Year", "Cap", "Instructor");
                            System.out.println("-------------------------------------------------------------------------------------------------------------");
                            for (UniversityDAO.CourseOffering c : result) {
                                String quarterStr = switch (c.quarter) {
                                    case 1 -> "Winter";
                                    case 2 -> "Spring";
                                    case 3 -> "Summer";
                                    case 4 -> "Fall";
                                    default -> "Unknown";
                                };
                                String timeFormatted = String.format("%02d:%02d", c.timeStart / 100, c.timeStart % 100);
                                String instructor = c.profFirstName + " " + c.profLastName;
                
                                System.out.printf("%-8d | %-8s | %-30s | %-5s | %-5s | %-5s | %-8s | %-10s | %-4d | %-4d | %-15s%n",
                                    c.enrollmentCode, c.courseNo, c.title, c.buildingCode, c.room, c.days,
                                    timeFormatted, quarterStr, c.year, c.maxEnrollment, instructor);
                            }
                        }
                    } catch (SQLException e) {
                        System.out.println("❌ Failed to list current courses: " + e.getMessage());
                    }
                    break;
                
            
                    case "4":
                    int currentQuarter = 1;
                    int currentYear = 2025;
                    try {
                        List<UniversityDAO.CompletedCourse> completed = dao.getPreviousQuarterGrades(String.valueOf(perm), currentQuarter, currentYear);
                        if (completed.isEmpty()) {
                            System.out.println("No grades found for the previous quarter.");
                        } else {
                            System.out.printf("%-8s | %-30s | %-8s | %-6s | %-5s%n", 
                                "Course", "Title", "Quarter", "Year", "Grade");
                            System.out.println("--------------------------------------------------------------------------");
                            for (UniversityDAO.CompletedCourse course : completed) {
                                String quarterStr = switch (course.quarter) {
                                    case 1 -> "Winter";
                                    case 2 -> "Spring";
                                    case 3 -> "Summer";
                                    case 4 -> "Fall";
                                    default -> "Unknown";
                                };
                                System.out.printf("%-8s | %-30s | %-8s | %-6d | %-5s%n",
                                    course.courseNo, course.title, quarterStr, course.year, course.grade);
                            }
                        }
                    } catch (SQLException e) {
                        System.out.println("❌ Failed to retrieve previous quarter grades: " + e.getMessage());
                    }
                    break;
                
            
                    case "5":
                    System.out.println("📋 Checking graduation requirements...");
                    try {
                        UniversityDAO.RequirementsCheck result = dao.checkRequirements(String.valueOf(perm));
                        
                        if (result.canGraduate) {
                            System.out.println("Major complete!");
                        } else {
                            System.out.println("You are not yet eligible to graduate. You have the remaining requirements:");
                            
                            if (!result.missingMandatoryCourses.isEmpty()) {
                                System.out.println("Missing mandatory courses:");
                                for (String course : result.missingMandatoryCourses) {
                                    System.out.println("  - " + course);
                                }
                            } else {
                                System.out.println("All mandatory courses are complete.");
                            }
                
                            if (result.missingElectives > 0) {
                                System.out.println("Number of missing electives: " + result.missingElectives);
                            } else {
                                System.out.println("All elective requirements are complete.");
                            }
                        }
                    } catch (SQLException e) {
                        System.out.println("❌ Failed to check requirements: " + e.getMessage());
                    }
                    break;
                
            
                case "6":
                    System.out.println("Making a graduation plan...");
                    try {
                        UniversityDAO.StudyPlan plan = dao.generateStudyPlan(String.valueOf(perm));
                
                        if (plan.canGraduate) {
                            System.out.println(plan.message);
                        } else {
                            System.out.println(plan.message);
                            System.out.println("Recommended courses to take:");
                            for (String course : plan.plannedCourses) {
                                System.out.println("  - " + course);
                            }
                        }
                    } catch (SQLException e) {
                        System.out.println("❌ Failed to generate study plan: " + e.getMessage());
                    }
                    break;
            
                    case "7":
                    System.out.print("Enter your current 5-digit PIN: ");
                    String oldPinInput = scanner.nextLine().trim();
                    System.out.print("Enter your new 5-digit PIN: ");
                    String newPinInput = scanner.nextLine().trim();
                
                    if (oldPinInput.matches("\\d{5}") && newPinInput.matches("\\d{5}")) {
                        if (oldPinInput.equals(newPinInput)) {
                            System.out.println("Error: New PIN cannot be the same as the current PIN.");
                        } else {
                            try {
                                boolean success = dao.setPin(String.valueOf(perm), oldPinInput, newPinInput);
                                if (success) {
                                    System.out.println("PIN successfully changed!");
                                } else {
                                    System.out.println("Incorrect current PIN. Try again.");
                                }
                            } catch (SQLException e) {
                                System.out.println("❌ Failed to change PIN: " + e.getMessage());
                            }
                        }
                    } else {
                        System.out.println("❌ PINs must be 5-digit integers.");
                    }
                    break;
                
            
                case "0":
                    System.out.println("Exiting GOLD...");
                    return;
            
                default:
                    System.out.println("Invalid option. Please try again.");
            }
            
        }

    }

    public void registrar_ui(UniversityDAO dao){
        System.out.println("Welcome to the Registrar!");
    }
}
