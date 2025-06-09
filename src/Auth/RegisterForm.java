package Auth;

import Connection.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;

public class RegisterForm extends JFrame {

    private JTextField txtUsername = new JTextField();
    private JPasswordField txtPassword = new JPasswordField();
    private JComboBox<String> cmbRole = new JComboBox<>(new String[]{"admin", "petugas", "warga"});
    private JTextField txtIdWarga = new JTextField();
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

        String[] labels = {"Username:", "Password:", "Role:", "ID Warga (warga only):"};
        Component[] comps = {txtUsername, txtPassword, cmbRole, txtIdWarga};

        for (int i = 0; i < labels.length; i++) {
            c.gridx = 0;
            c.gridy = i;
            p.add(new JLabel(labels[i]), c);
            c.gridx = 1;
            p.add(comps[i], c);
        }
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        p.add(btnRegister, c);

        add(p);
        updateIdWargaField();
        cmbRole.addActionListener(e -> updateIdWargaField());
        btnRegister.addActionListener(e -> registerUser());
        setVisible(true);
    }

    private void updateIdWargaField() {
        txtIdWarga.setEnabled("warga".equals(cmbRole.getSelectedItem()));
        if (!txtIdWarga.isEnabled()) {
            txtIdWarga.setText("");
        }
    }

    private void registerUser() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String role = (String) cmbRole.getSelectedItem();
        String idWargaText = txtIdWarga.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showErr("Username dan Password harus diisi!");
            return;
        }
        Integer idWarga = null;
        if ("warga".equals(role)) {
            if (idWargaText.isEmpty()) {
                showErr("ID Warga wajib diisi untuk warga!");
                return;
            }
            try {
                idWarga = Integer.parseInt(idWargaText);
            } catch (NumberFormatException e) {
                showErr("ID Warga harus angka!");
                return;
            }
        }

        try {
            if (isUsernameExist(username)) {
                showErr("Username sudah digunakan!");
                return;
            }
            String hashed = hashPassword(password);
            String sql = "INSERT INTO user_app (username, password, role, id_warga) VALUES (?, ?, ?, ?)";
            try (Connection con = DBConnection.getConnection();
                    PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, username);
                pst.setString(2, hashed);
                pst.setString(3, role);
                if (idWarga != null) {
                    pst.setInt(4, idWarga);
                } else {
                    pst.setNull(4, Types.INTEGER);
                }
                if (pst.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Registrasi berhasil!");
                    clearForm();
                } else {
                    showErr("Registrasi gagal!");
                }
            }
        } catch (Exception e) {
            showErr("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isUsernameExist(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_app WHERE username = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, username);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private String hashPassword(String pass) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] h = md.digest(pass.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : h) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void clearForm() {
        txtUsername.setText("");
        txtPassword.setText("");
        txtIdWarga.setText("");
        cmbRole.setSelectedIndex(0);
        updateIdWargaField();
    }

    private void showErr(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegisterForm::new);
    }
}
