package View;

import javax.swing.*;
import java.awt.*;

public class MainForm extends JFrame {

    private String role;
    private int userId;

    public MainForm(String role, int userId) {
        this.role = role;
        this.userId = userId;

        setTitle("Dashboard - " + role);
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        if ("admin".equalsIgnoreCase(role)) {
            add(new AdminPanel1(), BorderLayout.CENTER);
        } else if ("petugas".equalsIgnoreCase(role)) {
            add(new PetugasPanel1(userId), BorderLayout.CENTER);
        } else {
            add(new WargaPanel1(userId), BorderLayout.CENTER);
        }

        setVisible(true);
    }
}
