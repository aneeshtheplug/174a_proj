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
                String[] courses = { "CS174","CS170","CS160","CS026","EC154","EC140","EC015" };
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
                // Fall 2024 offerings

                dao.addSetting("ENGR", "201", "MWF", 900, 950);

                // --- Simple test routine ---
                System.out.println("-- Adding test student and course offering --");
                dao.addCourseOffering(87654, "CS026", "Bio", "2222", "MWF", 1400, 4, 2024, 8, "Saturn", "");
                dao.addCourseOffering(13579, "CS160", "Engr", "1132", "MWF", 1400, 4, 2024, 8, "Mercury", "");
                dao.addCourseOffering(97531, "EC140", "Chem", "1234", "TR", 1300, 4, 2024, 10, "Gold", "");
                dao.addCourseOffering(24680, "CS170", "English", "1124", "MWF", 1000, 1, 2025, 8, "Jupiter", "");
                dao.addCourseOffering(86420, "EC015", "Engr", "2116", "MW", 1100, 1, 2025, 8, "Silver", "");

                // System.out.println("-- Populating completed courses (historical) --");
                // // Fall 2024 grades for offering 87654
                // List<GradeEntry> grades87654 = Arrays.asList(
                //     new GradeEntry("12345","A"),
                //     new GradeEntry("14682","B"),
                //     new GradeEntry("37642","A-")
                // );
                // dao.enterGrades(87654, grades87654);

                // // Winter 2025 grades for offering 24680
                // List<GradeEntry> grades24680 = Arrays.asList(
                //     new GradeEntry("12345","B+"),
                //     new GradeEntry("37642","A")
                // );
                // dao.enterGrades(24680, grades24680);

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
        String sql = "SELECT * FROM Students WHERE perm = ?";
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
    public CourseOffering getCourseOffering(int enrollCode) throws SQLException {
        String sql = "SELECT o.*, c.title FROM Course_Offerings o " +
                     "JOIN Courses c ON o.course_no = c.course_no " +
                     "WHERE o.enrollment_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollCode);
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
    public boolean hasAvailableSpots(int enrollCode) throws SQLException {
        String sql = "SELECT o.max_enrollment, COUNT(e.perm) as current_enrollment " +
                     "FROM Course_Offerings o " +
                     "LEFT JOIN Currently_Enrolled e ON o.enrollment_code = e.enrollment_code AND e.dropped = 'N' " +
                     "WHERE o.enrollment_code = ? " +
                     "GROUP BY o.max_enrollment";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int maxEnrollment = rs.getInt("max_enrollment");
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
    public boolean hasCompletedPrerequisites(String perm, String courseNo) throws SQLException {
        String sql = "SELECT COUNT(*) as missing FROM Is_Prerequisite p " +
                     "WHERE p.course_no = ? AND p.prereq_course_no NOT IN (" +
                     "    SELECT o.course_no FROM Completed_Course c " +
                     "    JOIN Course_Offerings o ON c.enrollment_code = o.enrollment_code " +
                     "    WHERE c.perm = ? AND c.grade IN ('A+','A','A-','B+','B','B-','C+','C')" +
                     ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseNo);
            ps.setString(2, perm);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("missing") == 0;
            }
        }
    }

    /**
     * Get current enrollment count for a student.
     */
    public int getCurrentEnrollmentCount(String perm) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Currently_Enrolled WHERE perm = ? AND dropped = 'N'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * Enroll a student in a course with validation.
     */
    public boolean enrollStudentInCourse(String perm, int enrollCode) throws SQLException {
        // Check if student exists
        if (getStudent(perm) == null) {
            throw new SQLException("Student not found");
        }

        // Check enrollment limit (max 5 courses)
        if (getCurrentEnrollmentCount(perm) >= 5) {
            throw new SQLException("Student already enrolled in maximum courses (5)");
        }

        // Check if course has available spots
        if (!hasAvailableSpots(enrollCode)) {
            throw new SQLException("Course is full");
        }

        // Check prerequisites
        CourseOffering offering = getCourseOffering(enrollCode);
        if (offering != null && !hasCompletedPrerequisites(perm, offering.courseNo)) {
            throw new SQLException("Prerequisites not met");
        }

        // Check if already enrolled
        if (isCurrentlyEnrolled(perm, enrollCode)) {
            throw new SQLException("Student already enrolled in this course");
        }

        String sql = "INSERT INTO Currently_Enrolled (perm, enrollment_code, dropped) VALUES (?, ?, 'N')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            ps.setInt(2, enrollCode);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Check if student is currently enrolled in a course.
     */
    public boolean isCurrentlyEnrolled(String perm, int enrollCode) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Currently_Enrolled WHERE perm = ? AND enrollment_code = ? AND dropped = 'N'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            ps.setInt(2, enrollCode);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Drop a student from a course with validation.
     */
    public boolean dropStudentFromCourse(String perm, int enrollCode) throws SQLException {
        // Check if this is the only course (can't drop if it's the only one)
        if (getCurrentEnrollmentCount(perm) <= 1) {
            throw new SQLException("Cannot drop from the only enrolled course");
        }

        // Mark as dropped instead of deleting to maintain history
        String sql = "UPDATE Currently_Enrolled SET dropped = 'Y' WHERE perm = ? AND enrollment_code = ? AND dropped = 'N'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            ps.setInt(2, enrollCode);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * List courses in which a student is currently enrolled.
     */
    public List<CourseOffering> listCurrentCourses(String perm) throws SQLException {
        String sql = "SELECT o.*, c.title FROM Course_Offerings o " +
                     "JOIN Currently_Enrolled e ON o.enrollment_code = e.enrollment_code " +
                     "JOIN Courses c ON o.course_no = c.course_no " +
                     "WHERE e.perm = ? AND e.dropped = 'N'";
        List<CourseOffering> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, perm);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CourseOffering(rs));
                }
            }
        }
        return list;
    }

    /**
     * List students enrolled in a course.
     */
    public List<Student> listStudentsInCourse(int enrollCode) throws SQLException {
        String sql = "SELECT s.* FROM Students s " +
                     "JOIN Currently_Enrolled e ON s.perm = e.perm " +
                     "WHERE e.enrollment_code = ? AND e.dropped = 'N'";
        List<Student> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Student(rs));
                }
            }
        }
        return list;
    }

    // ============ GRADE MANAGEMENT ============

    /**
     * Enter grades for a course (batch operation).
     */
    public void enterGrades(int enrollCode, List<GradeEntry> grades) throws SQLException {
        String sql = "INSERT INTO Completed_Course (perm, enrollment_code, grade) VALUES (?, ?, ?) " +
                     "ON CONFLICT (perm, enrollment_code) DO UPDATE SET grade = EXCLUDED.grade";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (GradeEntry grade : grades) {
                ps.setString(1, grade.perm);
                ps.setInt(2, enrollCode);
                ps.setString(3, grade.grade);
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
        public String perm;
        public String grade;

        public GradeEntry(String perm, String grade) {
            this.perm = perm;
            this.grade = grade;
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
     * Check if student can enroll in a specific course (all validations).
     */
    public EnrollmentValidation canEnrollInCourse(String perm, int enrollCode) throws SQLException {
        List<String> errors = new ArrayList<>();
        
        // Check if student exists
        if (getStudent(perm) == null) {
            errors.add("Student not found");
            return new EnrollmentValidation(false, errors);
        }

        // Check enrollment limit
        if (getCurrentEnrollmentCount(perm) >= 5) {
            errors.add("Already enrolled in maximum courses (5)");
        }

        // Check if course has spots
        if (!hasAvailableSpots(enrollCode)) {
            errors.add("Course is full");
        }

        // Check if already enrolled
        if (isCurrentlyEnrolled(perm, enrollCode)) {
            errors.add("Already enrolled in this course");
        }

        // Check prerequisites
        CourseOffering offering = getCourseOffering(enrollCode);
        if (offering != null && !hasCompletedPrerequisites(perm, offering.courseNo)) {
            errors.add("Prerequisites not met for " + offering.courseNo);
        }

        return new EnrollmentValidation(errors.isEmpty(), errors);
    }

    /**
     * Get course prerequisites.
     */
    public List<String> getCoursePrerequisites(String courseNo) throws SQLException {
        String sql = "SELECT prereq_course_no FROM Is_Prerequisite WHERE course_no = ?";
        List<String> prereqs = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseNo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prereqs.add(rs.getString("prereq_course_no"));
                }
            }
        }
        return prereqs;
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

    // ============ ADDITIONAL DOMAIN OBJECTS ============

    public static class EnrollmentValidation {
        public boolean canEnroll;
        public List<String> errors;

        public EnrollmentValidation(boolean canEnroll, List<String> errors) {
            this.canEnroll = canEnroll;
            this.errors = errors;
        }

        @Override
        public String toString() {
            if (canEnroll) {
                return "Enrollment: ALLOWED";
            } else {
                return "Enrollment: BLOCKED - " + String.join(", ", errors);
            }
        }
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