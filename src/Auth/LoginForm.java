package Auth;

import Connection.DBConnection;
import View.MainForm;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;

public class LoginForm extends JFrame {

    private JTextField tfUser;
    private JPasswordField pfPass;

    public LoginForm() {
        setTitle("Login Aplikasi Sampah");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1, 5, 5));

        tfUser = new JTextField();
        pfPass = new JPasswordField();
        JButton btnLogin = new JButton("Login");

        add(new JLabel("Username:"));
        add(tfUser);
        add(new JLabel("Password:"));
        add(pfPass);
        add(btnLogin);

        btnLogin.addActionListener(e -> doLogin());

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void doLogin() {
        String u = tfUser.getText();
        String p = String.valueOf(pfPass.getPassword());
        try {
            String hashed = hashPassword(p);  // hash sebelum cek database

            try (Connection c = DBConnection.getConnection();
                    PreparedStatement ps = c.prepareStatement(
                            "SELECT id_user, role FROM user_app WHERE username=? AND password=?")) {
                ps.setString(1, u);
                ps.setString(2, hashed);  // gunakan password yang sudah di-hash
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int id = rs.getInt("id_user");
                    String role = rs.getString("role");
                    JOptionPane.showMessageDialog(this, "Login sukses: " + role);
                    new MainForm(role, id);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Login gagal");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saat login: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String hashPassword(String pass) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(pass.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginForm::new);
    }
}
