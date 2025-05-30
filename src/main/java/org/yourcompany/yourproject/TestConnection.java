package org.yourcompany.yourproject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.yourcompany.yourproject.UniversityDAO.GradeEntry;

import io.github.cdimascio.dotenv.Dotenv;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;

public class TestConnection {
    private static final Dotenv env = Dotenv.load();
    private static final String DB_URL      = env.get("DB_URL");
    private static final String DB_USER     = env.get("DB_USER");
    private static final String DB_PASSWORD = env.get("DB_PASSWORD");

    public static void main(String[] args) {
        Properties info = new Properties();
        info.put(OracleConnection.CONNECTION_PROPERTY_USER_NAME, DB_USER);
        info.put(OracleConnection.CONNECTION_PROPERTY_PASSWORD, DB_PASSWORD);
        info.put(OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20");

        try {
            OracleDataSource ods = new OracleDataSource();
            ods.setURL(DB_URL);
            ods.setConnectionProperties(info);

            try (OracleConnection conn = (OracleConnection) ods.getConnection()) {
                System.out.println("Connected!");
                DatabaseMetaData md = conn.getMetaData();
                System.out.println(md.getDriverName() + " v" + md.getDriverVersion());
                System.out.println();

                // --- wire up your DAO ---
                UniversityDAO dao = new UniversityDAO(conn);

                System.out.println("-- Clearing existing data --");
                dao.clearAllData();

                System.out.println("-- Loading static setup --");
                // 1) Departments & Majors
                dao.addDepartment("CS");
                dao.addDepartment("ECE");
                dao.addMajor("CS",  "CS", 5);
                dao.addMajor("ECE", "ECE", 5);

                // 2) Master Course List
                String[] courses = {"CS174","CS170","CS160","CS026","EC154","EC140","EC015", "CS154","CS130","CS010","EC152","EC010"};
                for (String c : courses) {
                    dao.addCourse(c, c);
                }

                // 3) Terms
                dao.addTerm(4, 2024); // Fall 2024
                dao.addTerm(1, 2025); // Winter 2025
                dao.addTerm(2, 2025); // Spring 2025

                // 4) Classroom Settings
                dao.addSetting("Psycho","1132","TR",1000,1200);
                dao.addSetting("English","1124","MWF",1000,1100);
                dao.addSetting("Engr","1132","MWF",1400,1500);
                dao.addSetting("Bio","2222","MWF",1400,1500);
                dao.addSetting("Maths","3333","T",1500,1700);
                dao.addSetting("Chem","1234","TR",1300,1500);
                dao.addSetting("Engr","2116","MW",1100,1300);
                dao.addSetting("Engr",   "2116", "MF",  800, 1000);  // CS154 (MF@8–10)
                dao.addSetting("Engr",   "2116", "MWF",  800, 1000);  // CS154 in Fall ’24 (MWF@8–10)
                dao.addSetting("Engr",   "3163", "MW",  1100, 1300);  // EC152
                dao.addSetting("Engr",   "1124", "TR",  1400, 1600);  // EC015
                dao.addSetting("Chem",   "1111", "TR",  1400, 1600);  // CS130 in Winter ’25
                dao.addSetting("Chem",   "3333", "MWF", 1500, 1700);  // CS010 in Fall ’24
                dao.addSetting("Physics","4004", "MWF",  800, 1000);  // EC010 in Fall ’24

                Object[][] students = {
                    {"12345","Alfred","Hitchcock","12345","6667 El Colegio #40","CS","CS"},
                    {"14682","Billy","Clinton","14682","5777 Hollister","ECE","ECE"},
                    {"37642","Cindy","Laugher","37642","7000 Hollister","CS","CS"},
                    {"85821","David","Copperfill","85821","1357 State St","CS","CS"},
                    {"38567","Elizabeth","Sailor","38567","4321 State St","ECE","ECE"},
                    {"81934","Fatal","Castro","81934","3756 La Cumbre Plaza","CS","CS"},
                    {"98246","George","Brush","98246","5346 Foothill Av","CS","CS"},
                    {"35328","Hurryson","Ford","35328","678 State St","ECE","ECE"},
                    {"84713","Ivan","Lendme","84713","1235 Johnson Dr","ECE","ECE"},
                    {"36912","Joe","Pepsi","36912","3210 State St","CS","CS"},
                    {"46590","Kelvin","Coster","46590","Santa Cruz #3579","CS","CS"},
                    {"91734","Li","Kung","91734","2 People's Rd Beijing","ECE","ECE"},
                    {"73521","Magic","Jordon","73521","3852 Court Rd","CS","CS"},
                    {"53540","Nam-hoi","Chung","53540","1997 People's St HK","CS","CS"},
                    {"82452","Olive","Stoner","82452","6689 El Colegio #151","ECE","ECE"},
                    {"18221","Pit","Wilson","18221","911 State St","ECE","ECE"}
                };

                for (Object[] s : students) {
                    dao.addStudent(
                        (String) s[0], // perm
                        (String) s[1], // firstName
                        (String) s[2], // lastName
                        (String) s[3], // rawPin
                        (String) s[4], // address
                        (String) s[5], // dname
                        (String) s[6]  // mname
                    );
                }

                System.out.println("-- Adding historical offerings --");

                System.out.println("-- Adding Spring 2025 offerings --");
                dao.addCourseOffering(12345, "CS174", "Psycho", "1132", "TR", 1000, 2, 2025, 8,  "Venus", "");  // CS130, CS026 prereqs added below
                dao.addCourseOffering(54321, "CS170", "English","1124","MWF",1000, 2, 2025, 8,  "Jupiter","");
                dao.addCourseOffering(41725, "CS160", "Engr",  "1132","MWF",1400, 2, 2025, 8,  "Mercury","");
                dao.addCourseOffering(76543, "CS026", "Bio",   "2222","MWF",1400, 2, 2025, 8,  "Mars",   "");
                dao.addCourseOffering(93156, "EC154", "Maths", "3333","T",  1500, 2, 2025, 7,  "Saturn","");
                dao.addCourseOffering(19023, "EC140", "Chem",  "1234","TR", 1300, 2, 2025, 10, "Gold",   "");
                dao.addCourseOffering(71631, "EC015", "Engr",  "2116","MW", 1100, 2, 2025, 8,  "Silver","");

                System.out.println("-- Adding Winter 2025 offerings --");
                dao.addCourseOffering(54321, "CS170", "English","1124","MWF",1000, 1, 2025, 18, "Copper","");
                dao.addCourseOffering(41725, "CS160", "Engr",  "1132","MWF",1400, 1, 2025, 15, "Iron",  "");
                dao.addCourseOffering(32165, "CS154", "Engr",  "2116","MF",  800, 1, 2025, 10, "Tin",   "");
                dao.addCourseOffering(56789, "CS130", "Chem",  "1111","TR", 1400, 1, 2025, 15, "Star",  "");
                dao.addCourseOffering(76543, "CS026", "Bio",   "2222","MWF",1400, 1, 2025, 15, "Tin",   "");
                dao.addCourseOffering(93156, "EC154", "Maths", "3333","T",  1500, 1, 2025, 18, "Saturn","");
                dao.addCourseOffering(91823, "EC152", "Engr",  "3163","MW", 1100, 1, 2025, 10, "Gold",  "");

                System.out.println("-- Adding Fall 2024 offerings --");
                dao.addCourseOffering(54321, "CS170", "English","1124","MWF",1000, 4, 2024, 15, "Copper","");
                dao.addCourseOffering(41725, "CS160", "Engr",  "1132","MWF",1400, 4, 2024, 10, "Mercury","");
                dao.addCourseOffering(32165, "CS154", "Engr",  "2116","MWF", 800, 4, 2024, 10, "Mars",   "");
                dao.addCourseOffering(56789, "CS130", "Chem",  "1111","TR", 1400, 4, 2024, 15, "Jupiter","");
                dao.addCourseOffering(76543, "CS026", "Bio",   "2222","MWF",1400, 4, 2024, 15, "Tin",    "");
                dao.addCourseOffering(81623, "CS010", "Chem",  "3333","MWF",1500, 4, 2024, 10, "Gold",   "");
                dao.addCourseOffering(93156, "EC154", "Maths", "3333","T",  1500, 4, 2024, 10, "Silver","");
                dao.addCourseOffering(91823, "EC152", "Engr",  "3163","MW", 1100, 4, 2024, 10, "Sun",    "");
                dao.addCourseOffering(71631, "EC015", "Engr",  "1124","TR", 1400, 4, 2024, 15, "Moon",   "");
                dao.addCourseOffering(82612, "EC010", "Physics","4004","MWF", 800, 4, 2024, 15, "Earth","");

                                // --- Prerequisites (Spring ’25) ---
                System.out.println("-- Adding prerequisites --");
                dao.addPrerequisite("CS174", "CS130");
                dao.addPrerequisite("CS174", "CS026");
                dao.addPrerequisite("CS170", "CS130");
                dao.addPrerequisite("CS170", "CS154");
                dao.addPrerequisite("CS160", "CS026");
                dao.addPrerequisite("EC154", "CS026");
                dao.addPrerequisite("EC154", "EC152");

                // --- Major requirements (Part 3) ---
                System.out.println("-- Adding major requirements --");
                String[] required = {"CS026","CS130","CS154","CS160","CS170"};
                String[] electives = {"CS010","EC010","EC015","EC140","EC152","EC154","CS174"};
                for (String req : required) {
                    dao.addMandatoryCourse("CS",  req);
                    dao.addMandatoryCourse("ECE", req);
                }
                for (String ele : electives) {
                    dao.addMajorElective("CS",  ele);
                    dao.addMajorElective("ECE", ele);
                }

                // --- Seeding historical “Taken” enrollments & grades ---
                // (Fall 2024: quarter=4, year=2024; Winter 2025: quarter=1, year=2025)

                System.out.println("-- Loading Fall 2024 completed courses --");
                // Alfred Hitchcock (perm=12345): CS026 A, CS010 A :contentReference[oaicite:0]{index=0}
                dao.enrollStudentInCourse("12345", 76543, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("12345", 76543, 4, 2024, "A")
                ));
                dao.enrollStudentInCourse("12345", 81623, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("12345", 81623, 4, 2024, "A")
                ));
                dao.enrollStudentInCourse("12345", 91823, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("12345", 91823, 4, 2024, "A")
                ));

                // Billy Clinton (perm=14682): CS026 B, CS010 A :contentReference[oaicite:1]{index=1}
                dao.enrollStudentInCourse("14682", 76543, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("14682", 76543, 4, 2024, "B")
                ));
                dao.enrollStudentInCourse("14682", 81623, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("14682", 81623, 4, 2024, "A")
                ));

                // Cindy Laugher (perm=37642): EC015 B, EC010 A :contentReference[oaicite:2]{index=2}
                dao.enrollStudentInCourse("37642", 71631, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("37642", 71631, 4, 2024, "B")
                ));
                dao.enrollStudentInCourse("37642", 82612, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("37642", 82612, 4, 2024, "A")
                ));

                // David Copperfill (perm=85821): CS010 A, EC015 B :contentReference[oaicite:3]{index=3}
                dao.enrollStudentInCourse("85821", 81623, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("85821", 81623, 4, 2024, "A")
                ));
                dao.enrollStudentInCourse("85821", 71631, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("85821", 71631, 4, 2024, "B")
                ));

                // Elizabeth Sailor (perm=38567): EC152 B, CS154 B :contentReference[oaicite:4]{index=4}
                dao.enrollStudentInCourse("38567", 91823, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("38567", 91823, 4, 2024, "B")
                ));
                dao.enrollStudentInCourse("38567", 32165, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("38567", 32165, 4, 2024, "B")
                ));

                // Fatal Castro (perm=81934): CS026 A, EC152 B :contentReference[oaicite:5]{index=5}
                dao.enrollStudentInCourse("81934", 76543, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("81934", 76543, 4, 2024, "A")
                ));
                dao.enrollStudentInCourse("81934", 91823, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("81934", 91823, 4, 2024, "B")
                ));

                // George Brush (perm=98246): CS154 A, CS130 B, CS026 A :contentReference[oaicite:6]{index=6}
                dao.enrollStudentInCourse("98246", 32165, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("98246", 32165, 4, 2024, "A")
                ));
                dao.enrollStudentInCourse("98246", 56789, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("98246", 56789, 4, 2024, "B")
                ));
                dao.enrollStudentInCourse("98246", 76543, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("98246", 76543, 4, 2024, "A")
                ));

                // Hurryson Ford (perm=35328): CS130 B, CS026 A :contentReference[oaicite:7]{index=7}
                dao.enrollStudentInCourse("35328", 56789, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("35328", 56789, 4, 2024, "B")
                ));
                dao.enrollStudentInCourse("35328", 76543, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("35328", 76543, 4, 2024, "A")
                ));

                // Ivan Lendme (perm=84713): EC015 F, CS010 C :contentReference[oaicite:8]{index=8}
                dao.enrollStudentInCourse("84713", 71631, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("84713", 71631, 4, 2024, "F")
                ));
                dao.enrollStudentInCourse("84713", 81623, 4, 2024);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("84713", 81623, 4, 2024, "C")
                ));


                // --- Now Winter 2025 completed courses ---
                System.out.println("-- Loading Winter 2025 completed courses --");
                // Alfred Hitchcock (perm=12345): CS154 A, CS130 B, EC154 C :contentReference[oaicite:9]{index=9}
                dao.enrollStudentInCourse("12345", 32165, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("12345", 32165, 1, 2025, "A")
                ));
                dao.enrollStudentInCourse("12345", 56789, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("12345", 56789, 1, 2025, "B")
                ));
                System.out.println("EC 154");
                dao.enrollStudentInCourse("12345", 93156, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("12345", 93156, 1, 2025, "C")
                ));

                // Billy Clinton (perm=14682): CS160 B, CS130 B :contentReference[oaicite:10]{index=10}
                System.out.println("Billy Clinton");
                dao.enrollStudentInCourse("14682", 41725, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("14682", 41725, 1, 2025, "B")
                ));
                dao.enrollStudentInCourse("14682", 56789, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("14682", 56789, 1, 2025, "B")
                ));

                // Cindy Laugher (perm=37642): EC152 C, CS130 B :contentReference[oaicite:11]{index=11}
                System.out.println("Cindy Laugher");
                dao.enrollStudentInCourse("37642", 91823, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("37642", 91823, 1, 2025, "C")
                ));
                dao.enrollStudentInCourse("37642", 56789, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("37642", 56789, 1, 2025, "B")
                ));

                // David Copperfill (perm=85821): CS130 C, CS026 A :contentReference[oaicite:12]{index=12}
                System.out.println("David Copperfill");
                dao.enrollStudentInCourse("85821", 56789, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("85821", 56789, 1, 2025, "C")
                ));
                dao.enrollStudentInCourse("85821", 76543, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("85821", 76543, 1, 2025, "A")
                ));

                // Elizabeth Sailor (perm=38567): EC154 C, CS130 A :contentReference[oaicite:13]{index=13}
                System.out.println("Elizabeth Sailor");
                // dao.enrollStudentInCourse("38567", 93156, 1, 2025);
                // dao.enterGrades(Arrays.asList(
                //     new GradeEntry("38567", 93156, 1, 2025, "C")
                // ));
                dao.enrollStudentInCourse("38567", 56789, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("38567", 56789, 1, 2025, "A")
                ));

                // Fatal Castro (perm=81934): CS154 C, CS130 A :contentReference[oaicite:14]{index=14}
                System.out.println("Fatal Castro");
                dao.enrollStudentInCourse("81934", 32165, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("81934", 32165, 1, 2025, "C")
                ));
                dao.enrollStudentInCourse("81934", 56789, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("81934", 56789, 1, 2025, "A")
                ));

                // George Brush (perm=98246): EC152 B :contentReference[oaicite:15]{index=15}
                System.out.println("George Brush");
                dao.enrollStudentInCourse("98246", 91823, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("98246", 91823, 1, 2025, "B")
                ));

                // Hurryson Ford (perm=35328): (none in 25W) :contentReference[oaicite:16]{index=16}

                // Ivan Lendme (perm=84713): CS026 D :contentReference[oaicite:17]{index=17}
                System.out.println("Ivan Lendme");
                dao.enrollStudentInCourse("84713", 76543, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("84713", 76543, 1, 2025, "D")
                ));

                // Kelvin Coster (perm=46590): CS026 A :contentReference[oaicite:18]{index=18}
                System.out.println("Kelvin Coster");
                dao.enrollStudentInCourse("46590", 76543, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("46590", 76543, 1, 2025, "A")
                ));

                // Li Kung (perm=91734): CS026 A :contentReference[oaicite:19]{index=19}
                dao.enrollStudentInCourse("91734", 76543, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("91734", 76543, 1, 2025, "A")
                ));

                // Magic Jordon (perm=73521): CS026 B :contentReference[oaicite:20]{index=20}
                dao.enrollStudentInCourse("73521", 76543, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("73521", 76543, 1, 2025, "B")
                ));

                // Nam-hoi Chung (perm=53540): CS154 C, CS130 C :contentReference[oaicite:21]{index=21}
                dao.enrollStudentInCourse("53540", 32165, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("53540", 32165, 1, 2025, "C")
                ));
                dao.enrollStudentInCourse("53540", 56789, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("53540", 56789, 1, 2025, "C")
                ));

                // Olive Stoner (perm=82452): EC152 C, CS026 C :contentReference[oaicite:22]{index=22}
                dao.enrollStudentInCourse("82452", 91823, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("82452", 91823, 1, 2025, "C")
                ));
                dao.enrollStudentInCourse("82452", 76543, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("82452", 76543, 1, 2025, "C")
                ));

                // Pit Wilson (perm=18221): CS130 B, CS026 B :contentReference[oaicite:23]{index=23}
                dao.enrollStudentInCourse("18221", 56789, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("18221", 56789, 1, 2025, "B")
                ));
                dao.enrollStudentInCourse("18221", 76543, 1, 2025);
                dao.enterGrades(Arrays.asList(
                    new GradeEntry("18221", 76543, 1, 2025, "B")
                ));

                System.out.println("✅ Sample data (including history) loaded!");
            }
        } catch (SQLException e) {
            System.err.println("ERROR during test run:");
            e.printStackTrace();
        }
    }
}


/**
 * Enhanced DAO layer with complete functionality for the university system
 */
class UniversityDAO {
    private final Connection conn;

    public UniversityDAO(Connection conn) {
        this.conn = conn;
    }

    // ============ FOUNDATIONAL DATA METHODS ============
    
    /**
     * Add a department.
     */
    public void addDepartment(String dname) throws SQLException {
        String sql = "INSERT INTO Department (dname) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dname);
            ps.executeUpdate();
        }
    }

    /**
     * Add a major linked to a department.
     */
    public void addMajor(String mname, String deptName, int numberElectives) throws SQLException {
        String sql = "INSERT INTO Major (mname, dname, number_electives) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mname);
            ps.setString(2, deptName);
            ps.setInt(3, numberElectives);
            ps.executeUpdate();
        }
    }

    /**
     * Add a course.
     */
    public void addCourse(String courseNo, String title) throws SQLException {
        String sql = "INSERT INTO Courses (course_no, title) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseNo);
            ps.setString(2, title);
            ps.executeUpdate();
        }
    }

    /**
     * Add a term (quarter/year combination).
     */
    public void addTerm(int quarter, int year) throws SQLException {
        String sql = "INSERT INTO Terms (quarter, year) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quarter);
            ps.setInt(2, year);
            ps.executeUpdate();
        }
    }

    /**
     * Add a setting (classroom/time slot).
     */
    public void addSetting(String buildingCode, String room, String days, 
                          int timeStart, int timeEnd) throws SQLException {
        String sql = "INSERT INTO Settings (building_code, room, days, time_start, time_end) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, buildingCode);
            ps.setString(2, room);
            ps.setString(3, days);
            ps.setInt(4, timeStart);
            ps.setInt(5, timeEnd);
            ps.executeUpdate();
        }
    }

    /**
     * Add a prerequisite relationship between courses.
     */
    public void addPrerequisite(String courseNo, String prereqCourseNo) throws SQLException {
        String sql = "INSERT INTO Is_Prerequisite (course_no, prereq_course_no) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseNo);
            ps.setString(2, prereqCourseNo);
            ps.executeUpdate();
        }
    }

    /**
     * Add a mandatory course for a major.
     */
    public void addMandatoryCourse(String mname, String courseNo) throws SQLException {
        String sql = "INSERT INTO Mandatory_Major_Courses (mname, course_no) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mname);
            ps.setString(2, courseNo);
            ps.executeUpdate();
        }
    }

    /**
     * Add an elective course for a major.
     */
    public void addMajorElective(String mname, String courseNo) throws SQLException {
        String sql = "INSERT INTO Major_Electives (mname, course_no) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mname);
            ps.setString(2, courseNo);
            ps.executeUpdate();
        }
    }

    /**
     * Verify student PIN (for authentication).
     */
    private String hashPin(String rawPin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(rawPin.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // ============ STUDENT MANAGEMENT ============
    
    /**
     * Add a new student (pin defaults to '00000').
     */
    public void addStudent(String perm, String firstName, String lastName, String rawPin, String address, String dname, String mname) throws SQLException {
        // hash the PIN
        String pinHash = hashPin(rawPin);

        String sql = "INSERT INTO Students "
                + "(perm, sname_first, sname_last, pin, address, dname, mname) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setString(4, pinHash);
            ps.setString(5, address);
            ps.setString(6, dname);
            ps.setString(7, mname);
            ps.executeUpdate();
        }
    }

    /** Verify that the provided rawPin (5 digits) matches the stored hash */
    public boolean verifyPin(String perm, String rawPin) throws SQLException {
        String sql = "SELECT pin FROM Students WHERE perm = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;               // no such student
                String storedHash = rs.getString("pin");
                String candidateHash = hashPin(rawPin);
                return storedHash.equals(candidateHash);
            }
        }
    }

    /** Change student PIN: only store the hash of the new PIN */
    public boolean setPin(String perm, String oldRawPin, String newRawPin) throws SQLException {
        // first verify old PIN
        if (!verifyPin(perm, oldRawPin)) {
            return false;
        }
        String newHash = hashPin(newRawPin);
        String sql = "UPDATE Students SET pin = ? WHERE perm = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setString(2, perm);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Get student information by perm number.
     */
    public Student getStudent(String perm) throws SQLException {
        String sql = 
        "SELECT * " +
        "FROM Students " +
        "WHERE TRIM(perm) = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(rs);
                }
                return null;
            }
        }
    }


    // ============ COURSE OFFERING MANAGEMENT ============

    /**
     * Add a new course offering.
     */
    public void addCourseOffering(int enrollCode, String courseNo,
                                  String buildingCode, String room, String days,
                                  int timeStart, int quarter, int year,
                                  int maxEnrollment, String profFname, String profLname) throws SQLException {
        String sql = "INSERT INTO Course_Offerings " +
                     "(enrollment_code, course_no, building_code, room, days, time_start, quarter, year, max_enrollment, prof_fname, prof_lname) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollCode);
            ps.setString(2, courseNo);
            ps.setString(3, buildingCode);
            ps.setString(4, room);
            ps.setString(5, days);
            ps.setInt(6, timeStart);
            ps.setInt(7, quarter);
            ps.setInt(8, year);
            ps.setInt(9, maxEnrollment);
            ps.setString(10, profFname);
            ps.setString(11, profLname);
            ps.executeUpdate();
        }
    }

    /**
     * Get course offering details by enrollment code.
     */
    public CourseOffering getCourseOffering(int enrollmentCode, int quarter, int year ) throws SQLException {
        String sql =
            "SELECT o.*, c.title " +
            "FROM Course_Offerings o " +
            "JOIN Courses c ON o.course_no = c.course_no " +
            "WHERE o.enrollment_code = ? " +
            "  AND o.quarter         = ? " +
            "  AND o.year            = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentCode);
            ps.setInt(2, quarter);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new CourseOffering(rs);
                }
                return null;
            }
        }
    }


    /**
     * Check if course has available spots for enrollment.
     */
    public boolean hasAvailableSpots(int enrollmentCode, int quarter, int year) throws SQLException {
        String sql =
            "SELECT o.max_enrollment, COUNT(e.perm) AS current_enrollment " +
            "FROM Course_Offerings o " +
            "LEFT JOIN Currently_Enrolled e " +
            "  ON o.enrollment_code = e.enrollment_code " +
            "  AND o.quarter         = e.quarter " +
            "  AND o.year            = e.year " +
            "  AND e.dropped = 'N' " +
            "WHERE o.enrollment_code = ? " +
            "  AND o.quarter         = ? " +
            "  AND o.year            = ? " +
            "GROUP BY o.max_enrollment";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentCode);
            ps.setInt(2, quarter);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int maxEnrollment     = rs.getInt("max_enrollment");
                    int currentEnrollment = rs.getInt("current_enrollment");
                    return currentEnrollment < maxEnrollment;
                }
                return false;
            }
        }
    }

    // ============ ENROLLMENT MANAGEMENT ============

        /**
         * Check prerequisites for a course before enrollment.
         */
        /**
     * Returns true if the student has completed *all* prerequisites
     * (with grade A–C) for the given course code, across any past term.
     */
    public boolean hasCompletedPrerequisites(String perm, String courseNo) throws SQLException {
            // 1) Fetch the prereq list *trimmed* from the DB
            String sqlFetch =
            "SELECT TRIM(prereq_course_no) AS prereq_code " +
            "  FROM Is_Prerequisite " +
            " WHERE TRIM(course_no) = ?";
            List<String> prereqs = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sqlFetch)) {
                ps.setString(1, courseNo.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        prereqs.add(rs.getString("prereq_code"));
                    }
                }
            }
            System.out.println("Trimmed prereqs for " + courseNo + " -> " + prereqs);

            // 2) Check each prereq against Completed_Course → Course_Offerings join
            String sqlCheck =
                "SELECT 1\n" +
                "FROM Completed_Course cc\n" +
                "JOIN Course_Offerings co\n" +
                "  ON cc.enrollment_code = co.enrollment_code\n" +
                " AND cc.quarter         = co.quarter\n" +
                " AND cc.year            = co.year\n" +
                "WHERE TRIM(cc.perm)      = ?\n" +
                "  AND TRIM(co.course_no) = ?\n" +
                "  AND TRIM(cc.grade)    IN ('A+','A','A-','B+','B','B-','C+','C')";

                try (PreparedStatement ps2 = conn.prepareStatement(sqlCheck)) {
                    for (String prereq : prereqs) {
                        System.out.println("Checking prereq `" + prereq + "` for student " + perm);
                        ps2.setString(1, perm.trim());
                        ps2.setString(2, prereq);  // already trimmed when loaded
                        try (ResultSet rs2 = ps2.executeQuery()) {
                            if (!rs2.next()) {
                                System.err.println("  → FAILED to find " + prereq + " (grade mismatch?)");
                                return false;
                            }
                            System.out.println("  → FOUND " + prereq);
                        }
                    }
                }
            return true;
        }

    /**
     * Get current enrollment count for a student.
     */
    /**
 * Returns the number of active (not dropped) enrollments
 * for a given student in a specific term.
 */
    public int getCurrentEnrollmentCount(
            String perm,
            int quarter,
            int year
    ) throws SQLException {
        String sql =
            "SELECT COUNT(*) AS cnt " +
            "FROM Currently_Enrolled " +
            "WHERE perm    = ? " +
            "  AND quarter = ? " +
            "  AND year    = ? " +
            "  AND dropped = 'N'";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            ps.setInt(2, quarter);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cnt") : 0;
            }
        }
    }


    /**
     * Enroll a student in a course with validation.
     */
    public boolean enrollStudentInCourse(String perm, int enrollCode, int quarter, int year) throws SQLException {
        // Check if student exists
        if (getStudent(perm) == null) {
            throw new SQLException("Student not found");
        }

        // Check enrollment limit (max 5 courses)
        if (getCurrentEnrollmentCount(perm, quarter, year) >= 5) {
            throw new SQLException("Student already enrolled in maximum courses (5)");
        }

        // Check if course has available spots
        if (!hasAvailableSpots(enrollCode, quarter, year)) {
            throw new SQLException("Course is full");
        }

        // Check prerequisites
        CourseOffering offering = getCourseOffering(enrollCode, quarter, year);
        if (offering != null && !hasCompletedPrerequisites(perm, offering.courseNo)) {
            throw new SQLException("Prerequisites not met");
        }

        // Check if already enrolled
        if (isCurrentlyEnrolled(perm, enrollCode, quarter, year)) {
            throw new SQLException("Student already enrolled in this course");
        }

        String sql =
        "INSERT INTO Currently_Enrolled "
        + "(perm, enrollment_code, quarter, year, dropped) "
        + "VALUES (?, ?, ?, ?, 'N')";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            ps.setInt(2, enrollCode);
            ps.setInt(3, quarter);
            ps.setInt(4, year);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Check if student is currently enrolled in a course.
     */
    /**
 * Checks if a student is currently enrolled in a specific offering
 * during a given term (and not dropped).
 */
    public boolean isCurrentlyEnrolled(String perm, int enrollmentCode, int quarter, int year) throws SQLException {
        String sql =
            "SELECT 1 " +
            "FROM Currently_Enrolled " +
            "WHERE perm            = ? " +
            "  AND enrollment_code = ? " +
            "  AND quarter         = ? " +
            "  AND year            = ? " +
            "  AND dropped         = 'N'";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            ps.setInt(2, enrollmentCode);
            ps.setInt(3, quarter);
            ps.setInt(4, year);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }


    /**
     * Drop a student from a course with validation.
     */
    public boolean dropStudentFromCourse(String perm, int enrollmentCode, int quarter, int year) throws SQLException {
        String sql =
                "UPDATE Currently_Enrolled " +
                "SET dropped = 'Y' " +
                "WHERE perm            = ? " +
                "  AND enrollment_code = ? " +
                "  AND quarter         = ? " +
                "  AND year            = ? " +
                "  AND dropped         = 'N'";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, perm);
                ps.setInt(2, enrollmentCode);
                ps.setInt(3, quarter);
                ps.setInt(4, year);
                return ps.executeUpdate() == 1;
            }
    }

    /**
     * List courses in which a student is currently enrolled.
     */
    public List<CourseOffering> listCurrentCourses(String perm, int quarter, int year) throws SQLException {
        String sql =
            "SELECT o.*, c.title " +
            "FROM Currently_Enrolled e " +
            "JOIN Course_Offerings o " +
            "  ON e.enrollment_code = o.enrollment_code " +
            "  AND e.quarter         = o.quarter " +
            "  AND e.year            = o.year " +
            "JOIN Courses c " +
            "  ON o.course_no = c.course_no " +
            "WHERE e.perm    = ? " +
            "  AND e.quarter = ? " +
            "  AND e.year    = ? " +
            "  AND e.dropped = 'N'";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            ps.setInt(2, quarter);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                List<CourseOffering> offerings = new ArrayList<>();
                while (rs.next()) {
                    offerings.add(new CourseOffering(rs));
                }
                return offerings;
            }
        }
    }

    /**
     * List students enrolled in a course.
     */
    public List<Student> listStudentsInCourse(int enrollmentCode, int quarter, int year) throws SQLException {
        String sql =
            "SELECT s.* " +
            "FROM Currently_Enrolled e " +
            "JOIN Students s " +
            "  ON e.perm = s.perm " +
            "WHERE e.enrollment_code = ? " +
            "  AND e.quarter         = ? " +
            "  AND e.year            = ? " +
            "  AND e.dropped         = 'N'";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentCode);
            ps.setInt(2, quarter);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                List<Student> students = new ArrayList<>();
                while (rs.next()) {
                    students.add(new Student(rs));
                }
                return students;
            }
        }
    }

    // ============ GRADE MANAGEMENT ============

    /**
     * Enter grades for a course (batch operation).
     */
    public void enterGrades(List<GradeEntry> gradeEntries) throws SQLException {
        String sql =
            "INSERT INTO Completed_Course " +
            "  (perm, enrollment_code, quarter, year, grade) " +
            "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (GradeEntry ge : gradeEntries) {
                ps.setString(1, ge.getPerm());
                ps.setInt(2, ge.getEnrollmentCode());
                ps.setInt(3, ge.getQuarter());
                ps.setInt(4, ge.getYear());
                ps.setString(5, ge.getGrade());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Get grades for previous quarter for a student.
     */
    public List<CompletedCourse> getPreviousQuarterGrades(String perm, int currentQuarter, int currentYear) throws SQLException {
        // Calculate previous quarter
        int prevQuarter = currentQuarter == 1 ? 3 : currentQuarter - 1;
        int prevYear = currentQuarter == 1 ? currentYear - 1 : currentYear;

        String sql = "SELECT c.*, o.course_no, o.quarter, o.year, co.title " +
                     "FROM Completed_Course c " +
                     "JOIN Course_Offerings o ON c.enrollment_code = o.enrollment_code " +
                     "JOIN Courses co ON o.course_no = co.course_no " +
                     "WHERE c.perm = ? AND o.quarter = ? AND o.year = ?";
        
        List<CompletedCourse> grades = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            ps.setInt(2, prevQuarter);
            ps.setInt(3, prevYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    grades.add(new CompletedCourse(rs));
                }
            }
        }
        return grades;
    }

    /**
     * Generate complete transcript for a student.
     */
    public List<CompletedCourse> generateTranscript(String perm) throws SQLException {
        String sql = "SELECT c.*, o.course_no, o.quarter, o.year, co.title " +
                     "FROM Completed_Course c " +
                     "JOIN Course_Offerings o ON c.enrollment_code = o.enrollment_code " +
                     "JOIN Courses co ON o.course_no = co.course_no " +
                     "WHERE c.perm = ? " +
                     "ORDER BY o.year, o.quarter, co.course_no";
        
        List<CompletedCourse> transcript = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transcript.add(new CompletedCourse(rs));
                }
            }
        }
        return transcript;
    }

    // ============ REQUIREMENTS AND PLANNING ============

    /**
     * Check if student meets graduation requirements for their major.
     */
    public RequirementsCheck checkRequirements(String perm) throws SQLException {
        Student student = getStudent(perm);
        if (student == null) {
            throw new SQLException("Student not found");
        }

        // Get major requirements
        String majorSql = "SELECT number_electives FROM Major WHERE mname = ?";
        int requiredElectives = 0;
        try (PreparedStatement ps = conn.prepareStatement(majorSql)) {
            ps.setString(1, student.mname);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    requiredElectives = rs.getInt("number_electives");
                }
            }
        }

        // Get completed mandatory courses
        String mandatorySql = "SELECT m.course_no FROM Mandatory_Major_Courses m " +
                             "WHERE m.mname = ? AND m.course_no NOT IN (" +
                             "    SELECT o.course_no FROM Completed_Course c " +
                             "    JOIN Course_Offerings o ON c.enrollment_code = o.enrollment_code " +
                             "    WHERE c.perm = ? AND c.grade IN ('A+','A','A-','B+','B','B-','C+','C')" +
                             ")";
        
        List<String> missingMandatory = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(mandatorySql)) {
            ps.setString(1, student.mname);
            ps.setString(2, perm);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    missingMandatory.add(rs.getString("course_no"));
                }
            }
        }

        // Count completed electives
        String electivesSql = "SELECT COUNT(DISTINCT o.course_no) as completed_electives " +
                             "FROM Completed_Course c " +
                             "JOIN Course_Offerings o ON c.enrollment_code = o.enrollment_code " +
                             "JOIN Major_Electives e ON o.course_no = e.course_no " +
                             "WHERE c.perm = ? AND e.mname = ? AND c.grade IN ('A+','A','A-','B+','B','B-','C+','C')";
        
        int completedElectives = 0;
        try (PreparedStatement ps = conn.prepareStatement(electivesSql)) {
            ps.setString(1, perm);
            ps.setString(2, student.mname);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    completedElectives = rs.getInt("completed_electives");
                }
            }
        }

        int missingElectives = Math.max(0, requiredElectives - completedElectives);
        boolean canGraduate = missingMandatory.isEmpty() && missingElectives == 0;

        return new RequirementsCheck(canGraduate, missingMandatory, missingElectives);
    }

    // ============ UTILITY METHODS ============

    /**
     * Generate grade mailer data for all students.
     */
    public List<GradeMailer> generateGradeMailer(int quarter, int year) throws SQLException {
        String sql = "SELECT s.perm, s.sname_first, s.sname_last, s.address, " +
                     "c.grade, o.course_no, co.title " +
                     "FROM Students s " +
                     "JOIN Completed_Course c ON s.perm = c.perm " +
                     "JOIN Course_Offerings o ON c.enrollment_code = o.enrollment_code " +
                     "JOIN Courses co ON o.course_no = co.course_no " +
                     "WHERE o.quarter = ? AND o.year = ? " +
                     "ORDER BY s.perm, o.course_no";
        
        List<GradeMailer> mailers = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quarter);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mailers.add(new GradeMailer(rs));
                }
            }
        }
        return mailers;
    }

    // ============ DOMAIN OBJECTS ============

    public static class Student {
        public String perm;
        public String firstName;
        public String lastName;
        public String address;
        public String dname;
        public String mname;

        public Student(ResultSet rs) throws SQLException {
            this.perm = rs.getString("perm");
            this.firstName = rs.getString("sname_first");
            this.lastName = rs.getString("sname_last");
            this.address = rs.getString("address");
            this.dname = rs.getString("dname");
            this.mname = rs.getString("mname");
        }

        @Override
        public String toString() {
            return String.format("Student[%s: %s %s, Major: %s]", perm, firstName, lastName, mname);
        }
    }

    public static class CourseOffering {
        public int enrollmentCode;
        public String courseNo;
        public String title;
        public String buildingCode;
        public String room;
        public String days;
        public int timeStart;
        public int quarter;
        public int year;
        public int maxEnrollment;
        public String profFirstName;
        public String profLastName;

        public CourseOffering(ResultSet rs) throws SQLException {
            this.enrollmentCode = rs.getInt("enrollment_code");
            this.courseNo = rs.getString("course_no");
            this.title = rs.getString("title");
            this.buildingCode = rs.getString("building_code");
            this.room = rs.getString("room");
            this.days = rs.getString("days");
            this.timeStart = rs.getInt("time_start");
            this.quarter = rs.getInt("quarter");
            this.year = rs.getInt("year");
            this.maxEnrollment = rs.getInt("max_enrollment");
            this.profFirstName = rs.getString("prof_fname");
            this.profLastName = rs.getString("prof_lname");
        }

        @Override
        public String toString() {
            return String.format("CourseOffering[%d: %s - %s, %s %s, %s %s, %s %d]", 
                enrollmentCode, courseNo, title, profFirstName, profLastName, 
                buildingCode, room, days, timeStart);
        }
    }

    public static class CompletedCourse {
        public String perm;
        public int enrollmentCode;
        public String grade;
        public String courseNo;
        public String title;
        public int quarter;
        public int year;

        public CompletedCourse(ResultSet rs) throws SQLException {
            this.perm = rs.getString("perm");
            this.enrollmentCode = rs.getInt("enrollment_code");
            this.grade = rs.getString("grade");
            this.courseNo = rs.getString("course_no");
            this.title = rs.getString("title");
            this.quarter = rs.getInt("quarter");
            this.year = rs.getInt("year");
        }

        @Override
        public String toString() {
            return String.format("CompletedCourse[%s: %s - %s, Grade: %s, Q%d %d]", 
                perm, courseNo, title, grade, quarter, year);
        }
    }

    public static class GradeEntry {
        private final String perm;
        private final int enrollmentCode;
        private final int quarter;
        private final int year;
        private final String grade;

        public GradeEntry(
            String perm,
            int enrollmentCode,
            int quarter,
            int year,
            String grade
        ) {
            this.perm = perm;
            this.enrollmentCode = enrollmentCode;
            this.quarter = quarter;
            this.year = year;
            this.grade = grade;
        }

        public String getPerm() {
            return perm;
        }

        public int getEnrollmentCode() {
            return enrollmentCode;
        }

        public int getQuarter() {
            return quarter;
        }

        public int getYear() {
            return year;
        }

        public String getGrade() {
            return grade;
        }
    }


    public static class RequirementsCheck {
        public boolean canGraduate;
        public List<String> missingMandatoryCourses;
        public int missingElectives;

        public RequirementsCheck(boolean canGraduate, List<String> missingMandatoryCourses, int missingElectives) {
            this.canGraduate = canGraduate;
            this.missingMandatoryCourses = missingMandatoryCourses;
            this.missingElectives = missingElectives;
        }

        @Override
        public String toString() {
            if (canGraduate) {
                return "Requirements Check: PASSED - Student can graduate";
            } else {
                return String.format("Requirements Check: MISSING - %d mandatory courses: %s, %d electives", 
                    missingMandatoryCourses.size(), missingMandatoryCourses, missingElectives);
            }
        }
    }

    public static class GradeMailer {
        public String perm;
        public String firstName;
        public String lastName;
        public String address;
        public String grade;
        public String courseNo;
        public String title;

        public GradeMailer(ResultSet rs) throws SQLException {
            this.perm = rs.getString("perm");
            this.firstName = rs.getString("sname_first");
            this.lastName = rs.getString("sname_last");
            this.address = rs.getString("address");
            this.grade = rs.getString("grade");
            this.courseNo = rs.getString("course_no");
            this.title = rs.getString("title");
        }

        @Override
        public String toString() {
            return String.format("GradeMailer[%s: %s %s, %s - %s, Grade: %s]", 
                perm, firstName, lastName, courseNo, title, grade);
        }
    }

    // ============ ADDITIONAL UTILITY METHODS ============

    /**
     * Get all available course offerings for a given quarter/year.
     */
    public List<CourseOffering> getAvailableCourseOfferings(int quarter, int year) throws SQLException {
        String sql = "SELECT o.*, c.title FROM Course_Offerings o " +
                     "JOIN Courses c ON o.course_no = c.course_no " +
                     "WHERE o.quarter = ? AND o.year = ? " +
                     "ORDER BY o.course_no";
        
        List<CourseOffering> offerings = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quarter);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    offerings.add(new CourseOffering(rs));
                }
            }
        }
        return offerings;
    }
    /**
     * Calculate GPA for a student.
     */
    public double calculateGPA(String perm) throws SQLException {
        String sql = "SELECT c.grade FROM Completed_Course c WHERE c.perm = ?";
        List<String> grades = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    grades.add(rs.getString("grade"));
                }
            }
        }

        if (grades.isEmpty()) return 0.0;

        double totalPoints = 0.0;
        int totalUnits = 0;

        for (String grade : grades) {
            double points = getGradePoints(grade);
            totalPoints += points * 4; // All courses are 4 units
            totalUnits += 4;
        }

        return totalUnits > 0 ? totalPoints / totalUnits : 0.0;
    }

    /**
     * Convert letter grade to grade points.
     */
    private double getGradePoints(String grade) {
        switch (grade.toUpperCase()) {
            case "A+": return 4.3;
            case "A": return 4.0;
            case "A-": return 3.7;
            case "B+": return 3.3;
            case "B": return 3.0;
            case "B-": return 2.7;
            case "C+": return 2.3;
            case "C": return 2.0;
            case "C-": return 1.7;
            case "D+": return 1.3;
            case "D": return 1.0;
            case "D-": return 0.7;
            case "F": case "F-": return 0.0;
            default: return 0.0;
        }
    }

    /**
     * Generate a basic study plan for a student to complete their major requirements.
     */
    public StudyPlan generateStudyPlan(String perm) throws SQLException {
        RequirementsCheck requirements = checkRequirements(perm);
        if (requirements.canGraduate) {
            return new StudyPlan(true, new ArrayList<>(), "Student can already graduate!");
        }

        List<String> plannedCourses = new ArrayList<>();
        
        // Add missing mandatory courses
        plannedCourses.addAll(requirements.missingMandatoryCourses);
        
        // Add suggested electives if needed
        if (requirements.missingElectives > 0) {
            List<String> availableElectives = getAvailableElectives(getStudent(perm).mname, perm);
            for (int i = 0; i < Math.min(requirements.missingElectives, availableElectives.size()); i++) {
                plannedCourses.add(availableElectives.get(i));
            }
        }

        String message = String.format("Need to complete %d mandatory courses and %d electives", 
            requirements.missingMandatoryCourses.size(), requirements.missingElectives);
        
        return new StudyPlan(false, plannedCourses, message);
    }

    /**
     * Get available electives for a major that the student hasn't completed.
     */
    private List<String> getAvailableElectives(String mname, String perm) throws SQLException {
        String sql = "SELECT e.course_no FROM Major_Electives e " +
                     "WHERE e.mname = ? AND e.course_no NOT IN (" +
                     "    SELECT o.course_no FROM Completed_Course c " +
                     "    JOIN Course_Offerings o ON c.enrollment_code = o.enrollment_code " +
                     "    WHERE c.perm = ?" +
                     ")";
        
        List<String> electives = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mname);
            ps.setString(2, perm);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    electives.add(rs.getString("course_no"));
                }
            }
        }
        return electives;
    }

    public static class StudyPlan {
        public boolean canGraduate;
        public List<String> plannedCourses;
        public String message;

        public StudyPlan(boolean canGraduate, List<String> plannedCourses, String message) {
            this.canGraduate = canGraduate;
            this.plannedCourses = plannedCourses;
            this.message = message;
        }

        @Override
        public String toString() {
            if (canGraduate) {
                return "Study Plan: " + message;
            } else {
                return String.format("Study Plan: %s - Suggested courses: %s", 
                    message, plannedCourses);
            }
        }
    }

    public void clearAllData() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // delete from the most dependent tables first:
            stmt.executeUpdate("DELETE FROM Currently_Enrolled");
            stmt.executeUpdate("DELETE FROM Completed_Course");
            stmt.executeUpdate("DELETE FROM Course_Offerings");
            stmt.executeUpdate("DELETE FROM Is_Prerequisite");

            stmt.executeUpdate("DELETE FROM Mandatory_Major_Courses");
            stmt.executeUpdate("DELETE FROM Major_Electives");
            stmt.executeUpdate("DELETE FROM Students");
            stmt.executeUpdate("DELETE FROM Major");

            // then the “leaf” master tables:
            stmt.executeUpdate("DELETE FROM Courses");
            stmt.executeUpdate("DELETE FROM Settings");
            stmt.executeUpdate("DELETE FROM Terms");
            stmt.executeUpdate("DELETE FROM Department");
        }
        System.out.println("🗑️  All tables cleared.");
    }
}