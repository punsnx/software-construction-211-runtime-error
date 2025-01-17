package ku.cs.models.request;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class RegisterRequestForm extends Request{
    // ประสงค์ลงทะเบียนล่าช้า
    private boolean lateRegister;

    // ประสงค์เพิ่มถอน
    private boolean addDrop;

    // ประสงค์ลงทะเบียนมากกว่า 22 หน่วยกิต
    private boolean registerMoreThan22;
    private String semester;
    private int semesterYear;
    private int oldCredit;
    private int newCredit;

    // ประสงค์ลงทะเบียนต่ำกว่า 9 หน่วยกิต
    private boolean registerLessThan9;

    // ประสงค์ผ่อนผันค่าธรรมเนียมการศึกษา
    private boolean latePayment;
    private String latePaymentSemester;
    private int latePaymentYear;

    // ประสงค์ย้ายคณะหรือสาขาวิชา
    private boolean transferFaculty;
    private String oldFaculty;
    private String newFaculty;

    // เหตุผล
    private String since;

    public RegisterRequestForm(UUID ownerUUID, String name, String nisitId, String requestType) {
        super(ownerUUID, name, nisitId, requestType);
    }

    public RegisterRequestForm(String[] data) {
        super(data[1], data[2], data[3], data[4], data[5], data[6], data[0], data[7], data[8], data[24]);
        this.lateRegister = Boolean.parseBoolean(data[9]);
        this.addDrop = Boolean.parseBoolean(data[10]);
        this.registerMoreThan22 = Boolean.parseBoolean(data[11]);
        this.semester = data[12];
        this.semesterYear = Integer.parseInt(data[13]);
        this.oldCredit = Integer.parseInt(data[14]);
        this.newCredit = Integer.parseInt(data[15]);
        this.registerLessThan9 = Boolean.parseBoolean(data[16]);
        this.latePayment = Boolean.parseBoolean(data[17]);
        this.latePaymentSemester = data[18];
        this.latePaymentYear = Integer.parseInt(data[19]);
        this.transferFaculty = Boolean.parseBoolean(data[20]);
        this.oldFaculty = data[21];
        this.newFaculty = data[22];
        this.since = data[23];
    }

    public void setLateRegister(boolean lateRegister) {
        this.lateRegister = lateRegister;
    }

    public void setAddDrop(boolean addDrop) {
        this.addDrop = addDrop;
    }

    public void setRegisterMoreThan22(boolean registerMoreThan22) {
        this.registerMoreThan22 = registerMoreThan22;
    }

    public void setSemester(String semester) {
        semester = semester.trim();
        if (semester.isEmpty()) {
            throw new IllegalArgumentException("กรุณาบอกภาคการศึกษา");
        }
        this.semester = semester;
    }

    public void setSemesterYear(String semesterYear) {
        semesterYear = semesterYear.trim();
        LocalDate currentDate = LocalDate.now();
        int semesterYearInt;
        try {
            semesterYearInt = Integer.parseInt(semesterYear);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("กรุณากรอกปีการศึกษา และต้องเป็นตัวเลขเท่านั้น");
        }
        if (semesterYearInt <= 0 || semesterYearInt > currentDate.getYear() + 543) {
            throw new IllegalArgumentException("ปีการศึกษาจะต้องมากกว่า 0 และไม่มากกว่าปีปัจจุบัน");
        }
        this.semesterYear = semesterYearInt;
    }

    public void setOldCredit(String oldCredit) {
        oldCredit = oldCredit.trim();
        int oldCreditInt;
        try {
            oldCreditInt = Integer.parseInt(oldCredit);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("กรุณากรอกหน่วยกิตเก่า และต้องเป็นตัวเลข");
        }
        if (oldCreditInt <= 0) {
            throw new IllegalArgumentException("หน่วยกิตเก่าจะต้องมากกว่า 0");
        }
        this.oldCredit = oldCreditInt;
    }

    public void setNewCredit(String newCredit) {
        newCredit = newCredit.trim();
        int newCreditInt;
        try {
            newCreditInt = Integer.parseInt(newCredit);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("กรุณากรอกหน่วยกิตใหม่ และต้องเป็นตัวเลข");
        }
        if (newCreditInt <= 0 ) {
            throw new IllegalArgumentException("หน่วยกิตใหม่จะต้องมากกว่า 0");
        }
        this.newCredit = newCreditInt;
    }

    public void setRegisterLessThan9(boolean registerLessThan9) {
        this.registerLessThan9 = registerLessThan9;
    }

    public void setLatePaymentSemester(String latePaymentSemester) {
        latePaymentSemester = latePaymentSemester.trim();
        if (latePaymentSemester.isEmpty()) {
            throw new IllegalArgumentException("กรุณาบอกภาคการศึกษา");
        }
        this.latePaymentSemester = latePaymentSemester;
    }

    public void setLatePaymentYear(String latePaymentYear) {
        latePaymentYear = latePaymentYear.trim();
        int latePaymentYearInt;
        try {
            latePaymentYearInt = Integer.parseInt(latePaymentYear);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("กรุณากรอกปีการศึกษาในการผ่อนผันค่าธรรมเนียม และต้องเป็นตัวเลข");
        }
        LocalDate currentDate = LocalDate.now();
        if (latePaymentYearInt <= 0 || latePaymentYearInt > currentDate.getYear() + 543) {
            throw new IllegalArgumentException("ปีการศึกษาในการผ่อนผันค่าธรรมเนียมต้องมีค่ามากกว่า 0 และไม่เกินปีการศึกษาปัจจุบัน");
        }
        this.latePaymentYear = latePaymentYearInt;
    }

    public void setTransferFaculty(boolean transferFaculty) {
        this.transferFaculty = transferFaculty;
    }

    public void setOldFaculty(String oldFaculty) {
        oldFaculty = oldFaculty.trim();
        if (oldFaculty.isEmpty()) {
            throw new IllegalArgumentException("กรุณากรอกคณะหรือสาขาวิชาเก่า");
        }
        this.oldFaculty = oldFaculty;
    }

    public void setNewFaculty(String newFaculty) {
        newFaculty = newFaculty.trim();
        if (newFaculty.isEmpty()) {
            throw new IllegalArgumentException("กรุณากรอกคณะหรือสาขาวิชาใหม่");
        }
        this.newFaculty = newFaculty;
    }

    public void setSince(String since) {
        since = since.trim();
        if (since.isEmpty()) {
            throw new IllegalArgumentException("กรุณากรอกเหตุผล");
        }
        this.since = since;
    }

    public boolean isLateRegister() {
        return lateRegister;
    }

    public boolean isAddDrop() {
        return addDrop;
    }

    public boolean isRegisterMoreThan22() {
        return registerMoreThan22;
    }

    public String getSemester() {
        return semester;
    }

    public int getSemesterYear() {
        return semesterYear;
    }

    public int getOldCredit() {
        return oldCredit;
    }

    public int getNewCredit() {
        return newCredit;
    }

    public boolean isRegisterLessThan9() {
        return registerLessThan9;
    }

    public boolean isLatePayment() {
        return latePayment;
    }

    public String getLatePaymentSemester() {
        return latePaymentSemester;
    }

    public int getLatePaymentYear() {
        return latePaymentYear;
    }

    public boolean isTransferFaculty() {
        return transferFaculty;
    }

    public String getOldFaculty() {
        return oldFaculty;
    }

    public String getNewFaculty() {
        return newFaculty;
    }

    public String getSince() {
        return since;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss");
        String timestamp = super.getTimeStamp().format(formatter);
        String date = super.getDate().format(formatter);
        return  super.getRequestType() + "," +
                super.getUuid().toString() + "," +
                super.getOwnerUUID().toString() + "," +
                super.getName() + "," +
                super.getNisitId() + "," +
                timestamp + "," +
                date + "," +
                super.getStatusNow() + "," +
                super.getStatusNext() + "," +
                lateRegister + "," +
                addDrop + "," +
                registerMoreThan22 + "," +
                semester + "," +
                semesterYear + "," +
                oldCredit + "," +
                newCredit + "," +
                registerLessThan9 + "," +
                latePayment + "," +
                latePaymentSemester + "," +
                latePaymentYear + "," +
                transferFaculty + "," +
                oldFaculty + "," +
                newFaculty + "," +
                since + "," +
                super.getReasonForNotApprove();

    }
}
