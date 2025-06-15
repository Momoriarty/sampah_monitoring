package View;

import Connection.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class WargaPanel1 extends JPanel {

    private int userId;
    private DefaultTableModel model;
    private CardLayout cardLayout;
    private JPanel mainPanel, choicePanel, laporanPanel;

    public WargaPanel1(int userId) {
        this.userId = userId;
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // --- Panel 1: Pilihan Awal ---
        choicePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JButton btnProfil = new JButton("Perbarui Profil");
        btnProfil.addActionListener(e -> showUpdateProfileDialog());

        JButton btnLaporan = new JButton("Tambah Laporan");
        btnLaporan.addActionListener(e -> {
            if (isProfileComplete()) {
                cardLayout.show(mainPanel, "laporan");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Lengkapi profil terlebih dahulu.");
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        choicePanel.add(new JLabel("Silakan pilih aksi:"), gbc);
        gbc.gridy++;
        choicePanel.add(btnProfil, gbc);
        gbc.gridy++;
        choicePanel.add(btnLaporan, gbc);

        // --- Panel 2: Panel Laporan ---
        laporanPanel = new JPanel(new BorderLayout());
        model = new DefaultTableModel(new String[]{"ID", "Tanggal", "Organik", "Anorganik", "Catatan"}, 0);
        JTable table = new JTable(model);
        laporanPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAddLaporan = new JButton("Tambah Laporan Baru");
        JButton btnBack = new JButton("Kembali ke Menu Utama");

        btnAddLaporan.addActionListener(e -> showAddDialog());
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "awal"));

        bottomPanel.add(btnBack);
        bottomPanel.add(btnAddLaporan);

        laporanPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Tambah panel ke card layout
        mainPanel.add(choicePanel, "awal");
        mainPanel.add(laporanPanel, "laporan");

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        cardLayout.show(mainPanel, "awal");
    }

    private void refreshTable() {
        model.setRowCount(0);
        String sql = "SELECT * FROM laporan_sampah l JOIN user_app u ON l.id_warga=u.id_warga WHERE u.id_user=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_laporan"), rs.getDate("tanggal_lapor"),
                    rs.getFloat("berat_organik"), rs.getFloat("berat_anorganik"), rs.getString("catatan")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Belum ada laporan yang tersedia.");
        }
    }

    private boolean isProfileComplete() {
        String sql = "SELECT w.nama, w.alamat, w.no_hp FROM warga w JOIN user_app u ON w.id_warga = u.id_warga WHERE u.id_user=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("nama") != null && !rs.getString("nama").isEmpty()
                        && rs.getString("alamat") != null && !rs.getString("alamat").isEmpty()
                        && rs.getString("no_hp") != null && !rs.getString("no_hp").isEmpty();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void showAddDialog() {
        JPanel p = new JPanel(new GridLayout(3, 2));
        JTextField tfOrg = new JTextField();
        JTextField tfAnOrg = new JTextField();
        JTextField tfCat = new JTextField();

        p.add(new JLabel("Organik (kg):"));
        p.add(tfOrg);
        p.add(new JLabel("Anorganik (kg):"));
        p.add(tfAnOrg);
        p.add(new JLabel("Catatan:"));
        p.add(tfCat);

        int ok = JOptionPane.showConfirmDialog(this, p, "Tambah Laporan", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            try {
                if (tfOrg.getText().isEmpty() || tfAnOrg.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Mohon isi berat sampah organik dan anorganik.");
                    return;
                }

                float o = Float.parseFloat(tfOrg.getText());
                float a = Float.parseFloat(tfAnOrg.getText());
                String cat = tfCat.getText();

                String ins = "INSERT INTO laporan_sampah (id_warga, tanggal_lapor, berat_organik, berat_anorganik, catatan) VALUES (" +
                        "(SELECT id_warga FROM user_app WHERE id_user=?), CURDATE(), ?, ?, ?)";
                try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(ins)) {
                    ps.setInt(1, userId);
                    ps.setFloat(2, o);
                    ps.setFloat(3, a);
                    ps.setString(4, cat);
                    ps.executeUpdate();
                    refreshTable();
                    JOptionPane.showMessageDialog(this, "Laporan berhasil ditambahkan.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Berat harus berupa angka.");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showUpdateProfileDialog() {
        String getSql = "SELECT w.id_warga, w.nama, w.alamat, w.no_hp, w.id_rw, w.id_rt FROM warga w JOIN user_app u ON w.id_warga = u.id_warga WHERE u.id_user=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(getSql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int idWarga = rs.getInt("id_warga");

                JTextField tfNama = new JTextField(rs.getString("nama"));
                JTextField tfAlamat = new JTextField(rs.getString("alamat"));
                JTextField tfHp = new JTextField(rs.getString("no_hp"));

                JComboBox<String> cbRW = new JComboBox<>();
                JComboBox<String> cbRT = new JComboBox<>();
                int selectedRW = rs.getInt("id_rw");
                int selectedRT = rs.getInt("id_rt");

                PreparedStatement psRW = c.prepareStatement("SELECT * FROM rw");
                ResultSet rsRW = psRW.executeQuery();
                while (rsRW.next()) {
                    int id = rsRW.getInt("id_rw");
                    String nama = rsRW.getString("nama_rw");
                    cbRW.addItem(id + " - " + nama);
                    if (id == selectedRW) {
                        cbRW.setSelectedItem(id + " - " + nama);
                    }
                }

                ActionListener rtLoader = e -> {
                    cbRT.removeAllItems();
                    if (cbRW.getSelectedItem() != null) {
                        int idRW = Integer.parseInt(cbRW.getSelectedItem().toString().split(" - ")[0]);
                        try (PreparedStatement psRT = c.prepareStatement("SELECT * FROM rt WHERE id_rw=?")) {
                            psRT.setInt(1, idRW);
                            ResultSet rsRT = psRT.executeQuery();
                            while (rsRT.next()) {
                                int id = rsRT.getInt("id_rt");
                                String nama = rsRT.getString("nama_rt");
                                cbRT.addItem(id + " - " + nama);
                                if (id == selectedRT) {
                                    cbRT.setSelectedItem(id + " - " + nama);
                                }
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                };
                cbRW.addActionListener(rtLoader);
                rtLoader.actionPerformed(null);

                JPanel p = new JPanel(new GridLayout(5, 2));
                p.add(new JLabel("Nama:"));
                p.add(tfNama);
                p.add(new JLabel("Alamat:"));
                p.add(tfAlamat);
                p.add(new JLabel("No HP:"));
                p.add(tfHp);
                p.add(new JLabel("RW:"));
                p.add(cbRW);
                p.add(new JLabel("RT:"));
                p.add(cbRT);

                int ok = JOptionPane.showConfirmDialog(this, p, "Perbarui Profil", JOptionPane.OK_CANCEL_OPTION);
                if (ok == JOptionPane.OK_OPTION) {
                    String upSql = "UPDATE warga SET nama=?, alamat=?, no_hp=?, id_rw=?, id_rt=? WHERE id_warga=?";
                    try (PreparedStatement up = c.prepareStatement(upSql)) {
                        int rwId = Integer.parseInt(cbRW.getSelectedItem().toString().split(" - ")[0]);
                        int rtId = Integer.parseInt(cbRT.getSelectedItem().toString().split(" - ")[0]);
                        up.setString(1, tfNama.getText());
                        up.setString(2, tfAlamat.getText());
                        up.setString(3, tfHp.getText());
                        up.setInt(4, rwId);
                        up.setInt(5, rtId);
                        up.setInt(6, idWarga);
                        up.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Profil berhasil diperbarui.");
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
