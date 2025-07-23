package View;

import Auth.LoginForm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import Connection.DBConnection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * MainForm adalah tampilan utama dashboard berdasarkan role pengguna: Admin,
 * Petugas, atau Warga.
 */
public class MainForm extends JFrame {

    private final String role;
    private final String username;
    private final int userId;

    private JLabel lblUserInfo;
    private JPanel mainPanel;

    private AdminPanel1 adminPanel;
    private PetugasPanel1 petugasPanel;
    private WargaPanel1 wargaPanel;

    public MainForm(String role, int userId, String username) {
        this.role = role;
        this.userId = userId;
        this.username = username;

        initUI();
    }

    private void initUI() {
        setTitle("Dashboard - " + capitalize(role));
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header (Top Panel)
        JPanel topPanel = createTopPanel();

        // Panel Konten Utama
        mainPanel = createMainPanelByRole();

        // Menu Bar (opsional)
        JMenuBar menuBar = createMenuBar();

        // Setup Frame
        setJMenuBar(menuBar);
        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    /**
     * Membuat panel bagian atas (header), termasuk label user, tombol refresh,
     * dan logout.
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        topPanel.setBackground(new Color(240, 240, 240));

        String nama = "";
        String sql = "SELECT nama FROM " + role + " WHERE id_user = ?";

        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId); // asumsi userId adalah integer
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    nama = rs.getString("nama");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        lblUserInfo = new JLabel("Logged in as: " + capitalize(role)
                + " | User ID: " + userId
                + " | Name: " + nama);
        lblUserInfo.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Panel kanan atas untuk tombol Refresh & Logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(new RefreshAction());

        JButton btnLogout = new JButton("Logout");
        btnLogout.setForeground(Color.RED);
        btnLogout.addActionListener(new LogoutAction());

        rightPanel.add(btnRefresh);
        rightPanel.add(btnLogout);

        topPanel.add(lblUserInfo, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.EAST);

        return topPanel;
    }

    /**
     * Membuat panel berdasarkan peran pengguna.
     */
    private JPanel createMainPanelByRole() {
        switch (role.toLowerCase()) {
            case "admin":
                adminPanel = new AdminPanel1(this, userId);
                return adminPanel;
            case "petugas":
                petugasPanel = new PetugasPanel1(userId);
                return petugasPanel;
            default:
                wargaPanel = new WargaPanel1(userId);
                return wargaPanel;
        }
    }

    /**
     * Membuat menu bar dasar. Bisa dikembangkan lebih lanjut.
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("File");
        JMenuItem itemLogout = new JMenuItem("Logout");
        itemLogout.addActionListener(new LogoutAction());
        JMenuItem itemExit = new JMenuItem("Keluar");
        itemExit.addActionListener(e -> System.exit(0));

        menuFile.add(itemLogout);
        menuFile.addSeparator();
        menuFile.add(itemExit);

        JMenu menuHelp = new JMenu("Bantuan");
        JMenuItem itemAbout = new JMenuItem("Tentang Aplikasi");
        itemAbout.addActionListener(e
                -> JOptionPane.showMessageDialog(this,
                        "Aplikasi Monitoring Sampah v1.0\nDikembangkan oleh Tim Informatika",
                        "Tentang",
                        JOptionPane.INFORMATION_MESSAGE));
        menuHelp.add(itemAbout);

        menuBar.add(menuFile);
        menuBar.add(menuHelp);

        return menuBar;
    }

    /**
     * Utility untuk kapitalisasi huruf pertama.
     */
    private String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();

    }

    /**
     * Inner class untuk menangani aksi logout.
     */
    private class LogoutAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int confirm = JOptionPane.showConfirmDialog(
                    MainForm.this,
                    "Yakin ingin logout?",
                    "Konfirmasi Logout",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                dispose(); // tutup dashboard
                new LoginForm(); // kembali ke form login
            }
        }
    }

    /**
     * Inner class untuk aksi tombol Refresh.
     */
    private class RefreshAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (role.toLowerCase()) {
                case "admin":
                    if (adminPanel != null) {
                        adminPanel.refreshData();
                    }
                    break;
                case "petugas":
                    if (petugasPanel != null) {
                        petugasPanel.refreshData();
                    }
                    break;
                default:
                    if (wargaPanel != null) {
                        wargaPanel.refreshData();
                    }
                    break;
            }
        }
    }

}
