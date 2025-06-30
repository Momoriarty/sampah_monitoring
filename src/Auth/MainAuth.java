package Auth;

import javax.swing.*;
import java.awt.*;

public class MainAuth extends JFrame {

    public MainAuth() {
        setTitle("Menu Utama Aplikasi Sampah");
        setSize(350, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel lblWelcome = new JLabel("Selamat Datang!", JLabel.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblWelcome);

        JButton btnLogin = new JButton("Login");
        JButton btnRegister = new JButton("Register");
        JButton btnExit = new JButton("Keluar");

        panel.add(btnLogin);
        panel.add(btnRegister);
        panel.add(btnExit);

        btnLogin.addActionListener(e -> {
            dispose();
            new LoginForm();
        });

        btnRegister.addActionListener(e -> {
            dispose();
            new RegisterForm();
        });

        btnExit.addActionListener(e -> System.exit(0));

        add(panel);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainAuth::new);
    }
}
