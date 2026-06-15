package ycuration.model;

public class User {
    // 캡슐화 원칙에 따라 모든 멤버 변수는 private으로 제한
    private String studentId;
    private String password;
    private int authorityCode; // 1: 일반 학생, 2: 관리자

    // 생성자 (Constructor)
    public User(String studentId, String password, int authorityCode) {
        this.studentId = studentId;
        this.password = password;
        this.authorityCode = authorityCode;
    }

    // 외부 레이어에서 안전하게 접근할 수 있도록 공개된 getter/setter 정의
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getAuthorityCode() { return authorityCode; }
    public void setAuthorityCode(int authorityCode) { this.authorityCode = authorityCode; }
}
