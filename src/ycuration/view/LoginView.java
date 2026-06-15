package ycuration.view;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

public class LoginView {
    private JFrame frame;
    private JTextField idField;
    private JPasswordField pwField;

    public void displayLoginWindow() {
        frame = new JFrame("University Notice Portal - Sign In");

        frame.setSize(420, 520);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.getContentPane().setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("영남대 공지 포털", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24)); // 타이틀 강조를 위해 폰트 살짝 키움
        titleLabel.setForeground(new Color(26, 54, 124));
        titleLabel.setBounds(45, 45, 325, 30);
        frame.add(titleLabel);

        JLabel subLabel = new JLabel("맞춤형 공지사항 큐레이션 서비스", SwingConstants.CENTER);
        subLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        subLabel.setForeground(new Color(127, 140, 141));
        subLabel.setBounds(45, 80, 325, 20);
        frame.add(subLabel);

        JLabel idLabel = new JLabel("학번 (Student ID)");
        idLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        idLabel.setBounds(45, 130, 200, 20);
        frame.add(idLabel);

        idField = new JTextField();
        setupStyledField(idField);
        idField.setBounds(40, 155, 325, 42);
        frame.add(idField);

        JLabel pwLabel = new JLabel("비밀번호 (Password)");
        pwLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        pwLabel.setBounds(45, 210, 200, 20);
        frame.add(pwLabel);

        pwField = new JPasswordField();
        setupStyledField(pwField);
        pwField.setBounds(40, 235, 325, 42);
        frame.add(pwField);

        JButton loginButton = new JButton("로그인");
        loginButton.setBounds(40, 310, 325, 45);
        loginButton.setBackground(new Color(26, 54, 124));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setOpaque(true);
        frame.add(loginButton);

        JLabel profGuideLabel = new JLabel(
                "<html><body style='text-align:center; color:#7f8c8d; font-family:맑은 고딕;'>"
                        + "<b>[시연 가이드]</b><br>"
                        + "별도의 가입 과정 없이, 임의의 학번과 비밀번호를<br>"
                        + "입력창에 채워 넣으시면 즉시 메인 피드로 진입합니다.<br>"
                        + "<span style='color:#e74c3c;'>(※ 단, 빈칸으로 로그인 시 진입이 거부됩니다.)</span>"
                        + "</body></html>",
                SwingConstants.CENTER
        );
        profGuideLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        profGuideLabel.setBounds(40, 375, 325, 85);
        frame.add(profGuideLabel);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String id = idField.getText().trim();
                String pw = new String(pwField.getPassword()).trim();

                ycuration.controller.NoticeDashboard controller = ycuration.controller.NoticeDashboard.getInstance();
                if (controller.handleLoginRequest(id, pw)) {
                    frame.dispose();
                    controller.renderCuratedFeed(id);
                } else {
                    showCustomErrorDialog("학번 또는 비밀번호가 일치하지 않습니다.");
                }
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void setupStyledField(JTextField field) {
        field.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        field.setBackground(new Color(245, 246, 248));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 228, 232), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void showCustomErrorDialog(String message) {
        JDialog errDialog = new JDialog(frame, "", true);
        errDialog.setUndecorated(true);
        errDialog.setSize(340, 180);
        errDialog.setLayout(null);
        errDialog.getContentPane().setBackground(Color.WHITE);

        errDialog.setShape(new RoundRectangle2D.Double(0, 0, errDialog.getWidth(), errDialog.getHeight(), 16, 16));

        JLabel iconLabel = new JLabel("!", SwingConstants.CENTER);
        iconLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        iconLabel.setForeground(new Color(192, 57, 43));
        iconLabel.setBounds(20, 25, 40, 40);
        errDialog.add(iconLabel);

        JLabel titleLabel = new JLabel("로그인 인증 실패");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        titleLabel.setForeground(new Color(192, 57, 43));
        titleLabel.setBounds(75, 22, 200, 22);
        errDialog.add(titleLabel);

        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        msgLabel.setForeground(new Color(127, 140, 141));
        msgLabel.setBounds(75, 48, 240, 20);
        errDialog.add(msgLabel);

        JButton confirmBtn = new JButton("확인");
        confirmBtn.setBounds(25, 105, 290, 40);
        confirmBtn.setBackground(new Color(26, 54, 124));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        confirmBtn.setFocusPainted(false);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setOpaque(true);
        confirmBtn.addActionListener(ev -> errDialog.dispose());
        errDialog.add(confirmBtn);

        errDialog.setLocationRelativeTo(frame);
        errDialog.setVisible(true);
    }
}