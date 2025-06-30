package Auth;

import Connection.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class RegisterForm extends JFrame {

    private JTextField txtUsername = new JTextField();
    private JPasswordField txtPassword = new JPasswordField();
    private JComboBox<String> cmbRole = new JComboBox<>(new String[]{"admin", "petugas", "warga"});
    private JButton btnRegister = new JButton("Register");

    public RegisterForm() {
        setTitle("Register User");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"Username:", "Password:", "Role:"};
        Component[] comps = {txtUsername, txtPassword, cmbRole};

        for (int i = 0; i < labels.length; i++) {
            c.gridx = 0;
            c.gridy = i;
            p.add(new JLabel(labels[i]), c);
            c.gridx = 1;
            p.add(comps[i], c);
        }

        // Tombol Register
        c.gridx = 0;
        c.gridy = labels.length;
        c.gridwidth = 2;
        p.add(btnRegister, c);

        // Tombol Kembali
        JButton btnBack = new JButton("Kembali");
        btnBack.setBackground(Color.GRAY);
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        c.gridy++;
        p.add(btnBack, c);

        add(p);

        btnRegister.addActionListener(e -> registerUser());
        btnBack.addActionListener(e -> {
            dispose();
            new MainAuth();
        });

        setVisible(true);
    }

    private void registerUser() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String role = (String) cmbRole.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username dan Password harus diisi!");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            if (isUsernameExist(con, username)) {
                showError("Username sudah digunakan.");
                return;
            }

            con.setAutoCommit(false);
            Integer idWarga = null;

            if ("warga".equals(role)) {
                String insertWarga = "INSERT INTO warga (nama) VALUES (?)";
                try (PreparedStatement pst = con.prepareStatement(insertWarga, Statement.RETURN_GENERATED_KEYS)) {
                    pst.setString(1, username);
                    pst.executeUpdate();

                    try (ResultSet rs = pst.getGeneratedKeys()) {
                        if (rs.next()) {
                            idWarga = rs.getInt(1);
                        } else {
                            con.rollback();
                            showError("Gagal menyimpan data warga.");
                            return;
                        }
                    }
                }
            }

            String insertUser = "INSERT INTO user_app (username, password, role, id_warga) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pst = con.prepareStatement(insertUser)) {
                pst.setString(1, username);
                pst.setString(2, hashPassword(password));
                pst.setString(3, role);
                if (idWarga != null) {
                    pst.setInt(4, idWarga);
                } else {
                    pst.setNull(4, Types.INTEGER);
                }

                int rows = pst.executeUpdate();
                if (rows > 0) {
                    con.commit();
                    JOptionPane.showMessageDialog(this, "Registrasi berhasil!");
                    clearForm();
                } else {
                    con.rollback();
                    showError("Registrasi gagal.");
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Database error: " + ex.getMessage());
        }
    }

    private boolean isUsernameExist(Connection con, String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_app WHERE username = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, username);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing gagal: algoritma tidak ditemukan", e);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void clearForm() {
        txtUsername.setText("");
        txtPassword.setText("");
        cmbRole.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegisterForm::new);
    }
}
