package org.yourcompany.yourproject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
                System.out.println("2. Registrar (for staff)");
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
        int CURRENT_QUARTER = 2;
        int CURRENT_YEAR = 2025;

        int perm = -1;
        while (true) {
            System.out.print("Enter your 5-digit PERM number: ");
            String input = scanner.nextLine().trim();
        
            if (input.matches("\\d{5}")) {
                perm = Integer.parseInt(input);
                try {
                    if (dao.getStudent(String.valueOf(perm)) != null) {
                        System.out.println("Logged in with PERM: " + perm + "\n");
                        break;
                    } else {
                        System.out.println("PERM not found in the database.\n");
                    }
                } catch (SQLException e) {
                    System.out.println("Database error while validating PERM: " + e.getMessage());
                }
            } else {
                System.out.println("Invalid PERM format. Please enter exactly 5 digits.\n");
            }
        }

        while (true) {
            System.out.println("\n===== ⚜️  GOLD Menu⚜️ =====");
            System.out.println("1. Add a course");
            System.out.println("2. Drop a course");
            System.out.println("3. List currently enrolled courses");
            System.out.println("4. View grades for previous quarters");
            System.out.println("5. Requirements check");
            System.out.println("6. Make a study plan");
            System.out.println("7. Change PIN");
            System.out.println("0. Exit GOLD");
            System.out.print("Choose an option: ");
    
            String choice = scanner.nextLine().trim();
    
            switch (choice) {
                case "1":
                    System.out.print("To add a course, enter the 5-digit enrollment code: ");
                    String addInput = scanner.nextLine().trim();
                    
                    if (addInput.matches("\\d{5}")) {
                        int code = Integer.parseInt(addInput);

                        System.out.print("Please enter your PIN: ");
                        String pinInput = scanner.nextLine().trim();

                        try {
                            if (!dao.verifyPin(String.valueOf(perm), pinInput)) {
                                System.out.println("❌ Incorrect PIN. Enrollment aborted.");
                                break;
                            }

                            System.out.println("📚 Adding course with code " + code + "...");
                            boolean success = dao.enrollStudentInCourse(String.valueOf(perm), code, CURRENT_QUARTER, CURRENT_YEAR);
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
                            System.out.print("Please enter your PIN to confirm: ");
                            String pinInput = scanner.nextLine().trim();
                            try {
                                // Verify the PIN before dropping
                                if (!dao.verifyPin(String.valueOf(perm), pinInput)) {
                                    System.out.println("❌ PIN verification failed. Cannot drop course.");
                                    break;
                                }

                                int code = Integer.parseInt(dropInput);
                                System.out.println("🗑️ Dropping course with code " + code + "...");
                                boolean success = dao.dropStudentFromCourse(String.valueOf(perm), code, CURRENT_QUARTER, CURRENT_YEAR);
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
                        List<UniversityDAO.CourseOffering> result = dao.listCurrentCourses(String.valueOf(perm), CURRENT_QUARTER, CURRENT_YEAR);
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
                    try {
                        List<UniversityDAO.CompletedCourse> completed = dao.getPreviousQuarterGrades(String.valueOf(perm), CURRENT_QUARTER, CURRENT_YEAR);
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
                    System.out.println("Making a graduation plan:");
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

    public void registrar_ui(UniversityDAO dao) {
        Scanner scanner = new Scanner(System.in);
        int CURRENT_QUARTER = 2;
        int CURRENT_YEAR = 2025;

        System.out.println("📋 Logged in as Registrar Staff.\n");

        while (true) {
            System.out.println("\n===== 🏛️  Registrar Menu 🏛️ =====");
            System.out.println("1. Add student to course");
            System.out.println("2. Drop student from course");
            System.out.println("3. List courses taken by a student");
            System.out.println("4. View previous quarter grades of a student");
            System.out.println("5. Generate class list for a course");
            System.out.println("6. Enter grades for a course from file");
            System.out.println("7. Request a transcript for a student");
            System.out.println("8. Generate grade mailer for all students");
            System.out.println("0. Exit Registrar System");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        System.out.print("Enter student's PERM: ");
                        String permAdd = scanner.nextLine().trim();
                        System.out.print("Enter 5-digit enrollment code: ");
                        String enrollCodeAdd = scanner.nextLine().trim();

                        if (permAdd.matches("\\d{5}") && enrollCodeAdd.matches("\\d{5}")) {
                            boolean success = dao.enrollStudentInCourse(permAdd, Integer.parseInt(enrollCodeAdd), CURRENT_QUARTER, CURRENT_YEAR);
                            if (success) {
                                System.out.println("✅ Student enrolled successfully.");
                            } else {
                                System.out.println("❌ Enrollment failed.");
                            }
                        } else {
                            System.out.println("❌ Invalid PERM or enrollment code.");
                        }
                        break;

                    case "2":
                        System.out.print("Enter student's PERM: ");
                        String permDrop = scanner.nextLine().trim();
                        System.out.print("Enter 5-digit enrollment code: ");
                        String enrollCodeDrop = scanner.nextLine().trim();

                        if (permDrop.matches("\\d{5}") && enrollCodeDrop.matches("\\d{5}")) {
                            boolean success = dao.dropStudentFromCourse(permDrop, Integer.parseInt(enrollCodeDrop), CURRENT_QUARTER, CURRENT_YEAR);
                            if (success) {
                                System.out.println("✅ Student dropped successfully.");
                            } else {
                                System.out.println("❌ Drop failed.");
                            }
                        } else {
                            System.out.println("❌ Invalid PERM or enrollment code.");
                        }
                        break;

                    case "3":
                        System.out.print("Enter student's PERM: ");
                        String permCurr = scanner.nextLine().trim();
                        try {
                            List<UniversityDAO.CourseOffering> result = dao.listCurrentCourses(String.valueOf(permCurr), CURRENT_QUARTER, CURRENT_YEAR);
                            if (result.isEmpty()) {
                                System.out.println("Perm #" + permCurr + " is not currently enrolled in any courses.");
                            } else {
                                System.out.println();
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
                        System.out.print("Enter student's PERM: ");
                        String permHist = scanner.nextLine().trim();
                        try {
                            List<UniversityDAO.CompletedCourse> completed = dao.getPreviousQuarterGrades(String.valueOf(permHist), CURRENT_QUARTER, CURRENT_YEAR);
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
                        System.out.print("Enter 5-digit enrollment code: ");
                        String classEnrollCode = scanner.nextLine().trim();
                        if (classEnrollCode.matches("\\d{5}")) {
                            List<UniversityDAO.Student> classList = dao.listStudentsInCourse(Integer.parseInt(classEnrollCode), CURRENT_QUARTER, CURRENT_YEAR);
                            if (classList.isEmpty()) {
                                System.out.println("No students enrolled.");
                            } else {
                                System.out.printf("%-10s | %-20s | %-20s | %-20s%n", "PERM", "First Name", "Last Name", "Major");
                                System.out.println("----------------------------------------------------------------------");
                                for (UniversityDAO.Student s : classList) {
                                    System.out.printf("%-10s | %-20s | %-20s | %-20s%n", s.perm, s.firstName, s.lastName, s.mname);
                                }
                            }
                        } else {
                            System.out.println("Invalid enrollment code.");
                        }
                        break;

                    case "6":
                        System.out.print("Enter filename with grades (e.g., grades.txt): ");
                        String filename = scanner.nextLine().trim();
                    
                        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                            String header = br.readLine();
                            if (header == null) {
                                System.out.println("❌ File is empty.");
                                break;
                            }
                    
                            String[] meta = header.split(",");
                            if (meta.length != 3) {
                                System.out.println("❌ Header must include: enrollment_code, quarter, year");
                                break;
                            }
                    
                            int enrollmentCode = Integer.parseInt(meta[0].trim());
                            int quarter = Integer.parseInt(meta[1].trim());
                            int year = Integer.parseInt(meta[2].trim());
                    
                            String line;
                            int successCount = 0;
                            while ((line = br.readLine()) != null) {
                                String[] parts = line.split(",");
                                if (parts.length != 2) {
                                    System.out.println("⚠️ Skipping malformed line: " + line);
                                    continue;
                                }
                                String perm = parts[0].trim();
                                String grade = parts[1].trim();
                    
                                try {
                                    boolean succ = dao.enterGrades(perm, enrollmentCode, quarter, year, grade);
                                    successCount += succ ? 1 : 0;
                                } catch (SQLException e) {
                                    System.out.println("⚠️ Failed to upload grade for " + perm + ": " + e.getMessage());
                                }
                            }
                    
                            System.out.println("✅ Grades uploaded successfully for " + successCount + " students.");
                        } catch (IOException | NumberFormatException e) {
                            System.out.println("❌ Grade upload failed: " + e.getMessage());
                        }
                        break;

                    case "7":
                        System.out.print("Enter student's PERM: ");
                        String permTranscript = scanner.nextLine().trim();
                    
                        try {
                            // Get current courses
                            List<UniversityDAO.CourseOffering> currentCourses = dao.listCurrentCourses(permTranscript, CURRENT_QUARTER, CURRENT_YEAR);
                            
                            // Get completed (previous quarter) courses
                            List<UniversityDAO.CompletedCourse> completedCourses = dao.getPreviousQuarterGrades(permTranscript, CURRENT_QUARTER, CURRENT_YEAR);
                    
                            // Display current courses
                            System.out.println("\nCurrently Enrolled Courses:");
                            if (currentCourses.isEmpty()) {
                                System.out.println("Not currently enrolled in any courses.");
                            } else {
                                System.out.printf("%-8s | %-8s | %-30s | %-5s | %-5s | %-5s | %-8s | %-10s | %-4s | %-4s | %-15s%n",
                                    "Enroll#", "Course", "Title", "Bldg", "Room", "Days", "Time", "Quarter", "Year", "Cap", "Instructor");
                                System.out.println("-------------------------------------------------------------------------------------------------------------");
                                for (UniversityDAO.CourseOffering c : currentCourses) {
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
                    
                            // Display completed courses
                            System.out.println("\nCompleted Courses (Previous Quarter):");
                            if (completedCourses.isEmpty()) {
                                System.out.println("No grades found for the previous quarter.");
                            } else {
                                System.out.printf("%-8s | %-30s | %-8s | %-6s | %-5s%n", 
                                    "Course", "Title", "Quarter", "Year", "Grade");
                                System.out.println("--------------------------------------------------------------------------");
                                for (UniversityDAO.CompletedCourse course : completedCourses) {
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
                            System.out.println("❌ Failed to retrieve transcript: " + e.getMessage());
                        }
                    
                        break;

                    case "8":
                        System.out.print("Enter a quarter (1=Winter, 2=Spring, 3=Summer, 4=Fall): ");
                        String quarterStr = scanner.nextLine().trim();
                        System.out.print("Enter a year: ");
                        String yearStr = scanner.nextLine().trim();
                    
                        try {
                            int quarter = Integer.parseInt(quarterStr);
                            int year = Integer.parseInt(yearStr);
                    
                            List<UniversityDAO.GradeMailer> mailers = dao.generateGradeMailer(quarter, year, CURRENT_QUARTER, CURRENT_YEAR);
                            if (mailers.isEmpty()) {
                                System.out.println("No grade mailers found for that quarter and year.");
                            } else {
                                String lastPerm = "";
                                for (UniversityDAO.GradeMailer gm : mailers) {
                                    if (!gm.perm.equals(lastPerm)) {
                                        // New student
                                        if (!lastPerm.equals("")) {
                                            System.out.println(); // line break between students
                                        }
                                        System.out.println("---------------------------------------------------------------");
                                        System.out.printf("Student: %s %s %nPERM: %s%n", gm.firstName, gm.lastName, gm.perm);
                                        System.out.printf("Address: %s%n", gm.address);
                                        System.out.println("Courses:");
                                        System.out.printf("  %-10s | %-30s | %-12s%n", "Course #", "Course Title", "Grade");
                                        System.out.println("---------------------------------------------------------------");
                                        lastPerm = gm.perm;
                                    }
                    
                                    if (gm.courseNo != null && gm.title != null) {
                                        System.out.printf("  %-10s | %-30s | %-12s%n",
                                                gm.courseNo, gm.title, gm.grade != null ? gm.grade : "N/A");
                                    } else {
                                        System.out.println("  (No courses enrolled in this term)");
                                        // Do not print this line repeatedly if multiple nulls (optional logic)
                                    }
                                }
                                System.out.println("--------------------------------------------------------");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("❌ Invalid input. Please enter numeric values for quarter and year.");
                        } catch (SQLException e) {
                            System.out.println("❌ Failed to generate grade mailers: " + e.getMessage());
                        }
                        break;

                    case "0":
                        System.out.println("Exiting Registrar System...");
                        return;

                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (SQLException e) {
                System.out.println("❌ Database error: " + e.getMessage());
            }
        }
    }
}
