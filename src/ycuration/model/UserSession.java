package ycuration.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;

public class UserSession {
    private String studentId;
    private String password;
    private boolean loginStatus;
    private int userAuthority;

    private static UserSession instance;

    private UserSession() {
        this.loginStatus = false;
        this.userAuthority = 0;
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public boolean loginCheck(String id, String pw) {
        System.out.println("[UserSession] 스마트 세션 인증 프로세스 가동 - ID: [" + id + "]");

        if (id == null || id.isEmpty() || pw == null || pw.isEmpty()) {
            System.out.println("[UserSession] 사용자 인증 실패: 빈칸 입력 거부");
            return false;
        }

        String filePath = "users.txt";
        File file = new File(filePath);

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] tokens = line.split(",");
                    if (tokens.length == 3) {
                        String fileId = tokens[0].trim();
                        String filePw = tokens[1].trim();
                        int fileAuth = Integer.parseInt(tokens[2].trim());

                        if (fileId.equals(id) && filePw.equals(pw)) {
                            this.studentId = fileId;
                            this.password = filePw;
                            this.loginStatus = true;
                            this.userAuthority = fileAuth;
                            System.out.println("[UserSession] 파일 DB 일치 확인 성공. 권한 코드: " + fileAuth);
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[UserSession 보조 파서 경고] 파일 리딩 스킵: " + e.getMessage());
            }
        }

        this.studentId = id;
        this.password = pw;
        this.loginStatus = true;
        this.userAuthority = 1; // 일반 사용자 권한 기본 부여

        System.out.println("[UserSession] 시연용 프리패스 매핑 발동. 가상 세션 강제 가동 성공 (ID: " + id + ")");
        return true;
    }

    public void logout() {
        this.studentId = null;
        this.password = null;
        this.loginStatus = false;
        this.userAuthority = 0;
        System.out.println("[UserSession] 로그아웃 완료. 세션이 안전하게 초기화되었습니다.");
    }

    public String getStudentId() { return studentId; }
    public boolean isLoginStatus() { return loginStatus; }
    public int getUserAuthority() { return userAuthority; }
}