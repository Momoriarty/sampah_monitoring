package View;

import javax.swing.*;
import java.awt.*;

public class MainForm extends JFrame {

    private String role;
    private int userId;

    public MainForm(String role, int id) {
        this.role = role;
        this.userId = id;

        setTitle("Dashboard - " + role);
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        if ("rt_rw".equals(role)) {
            add(new AdminPanel());
        } else if ("petugas".equals(role)) {
            add(new PetugasPanel());
        } else {
            add(new WargaPanel(userId));
        }

        setVisible(true);
    }
}
