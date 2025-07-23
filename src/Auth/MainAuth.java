package Auth;

import javax.swing.*;
import java.awt.*;

/**
 * Kelas MainAuth menampilkan menu utama aplikasi dengan pilihan Login,
 * Register, dan Keluar.
 */
public class MainAuth extends JFrame {

    // Konstruktor utama
    public MainAuth() {
        setTitle("Menu Utama Aplikasi Sampah");
        setSize(400, 250);
        setLocationRelativeTo(null); // Tengah layar
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // Panel utama dengan GridLayout
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        panel.setBackground(new Color(240, 248, 255)); // Warna latar belakang opsional

        // Label sambutan
        JLabel lblWelcome = new JLabel("Selamat Datang!", JLabel.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(lblWelcome);

        // Tombol-tombol menu
        JButton btnLogin = new JButton("Login");
        JButton btnRegister = new JButton("Register");
        JButton btnExit = new JButton("Keluar");

        // Menambahkan tombol ke panel
        panel.add(btnLogin);
        panel.add(btnRegister);
        panel.add(btnExit);

        // Aksi tombol Login
        btnLogin.addActionListener(e -> {
            dispose(); // Tutup jendela ini
            new LoginForm(); // Buka form login
        });

        // Aksi tombol Register
        btnRegister.addActionListener(e -> {
            dispose(); // Tutup jendela ini
            new RegisterForm(); // Buka form registrasi
        });

        // Aksi tombol Keluar
        btnExit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Yakin ingin keluar dari aplikasi?",
                    "Konfirmasi Keluar",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        // Tambahkan panel ke frame
        add(panel);
        setVisible(true);
    }

    // Main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainAuth::new);
    }
}
