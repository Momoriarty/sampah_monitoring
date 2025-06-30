package View;

import Connection.DBConnection;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;

public class WargaPanel extends JPanel {

    private int userId;
    private DefaultTableModel model;
    private DefaultTableModel pembayaranModel;
    private CardLayout cardLayout;
    private JPanel mainPanel, choicePanel, laporanPanel, pembayaranPanel;

    public WargaPanel(int userId) {
        this.userId = userId;
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Panel Pilihan
        choicePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JButton btnProfil = new JButton("Perbarui Profil");
        JButton btnLaporan = new JButton("Tambah Laporan");
        JButton btnPembayaran = new JButton("Riwayat Pembayaran");

        btnProfil.addActionListener(e -> showUpdateProfileDialog());
        btnLaporan.addActionListener(e -> {
            if (isProfileComplete()) {
                cardLayout.show(mainPanel, "laporan");
                refreshTable();
            }
        });

        btnPembayaran.addActionListener(e -> {
            cardLayout.show(mainPanel, "pembayaran");
            refreshPaymentTable();
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        choicePanel.add(new JLabel("Silakan pilih aksi:"), gbc);
        gbc.gridy++;
        choicePanel.add(btnProfil, gbc);
        gbc.gridy++;
        choicePanel.add(btnLaporan, gbc);
        gbc.gridy++;
        choicePanel.add(btnPembayaran, gbc);

        // Panel Laporan
        laporanPanel = new JPanel(new BorderLayout());
        model = new DefaultTableModel(new String[]{"No", "Tanggal", "Organik", "Anorganik", "Catatan", "Status"}, 0);
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

        // Panel Pembayaran
        pembayaranPanel = new JPanel(new BorderLayout());
        pembayaranModel = new DefaultTableModel(new String[]{"ID", "Jumlah", "Metode", "Tanggal", "Bukti","Tanggal"}, 0);
        JTable pembayaranTable = new JTable(pembayaranModel);
        pembayaranPanel.add(new JScrollPane(pembayaranTable), BorderLayout.CENTER);

        JPanel pembayaranBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnBayar = new JButton("Lakukan Pembayaran");
        JButton btnBack2 = new JButton("Kembali ke Menu Utama");

        btnBayar.addActionListener(e -> showPaymentDialog());
        btnBack2.addActionListener(e -> cardLayout.show(mainPanel, "awal"));

        pembayaranBottom.add(btnBack2);
        pembayaranBottom.add(btnBayar);
        pembayaranPanel.add(pembayaranBottom, BorderLayout.SOUTH);

        mainPanel.add(choicePanel, "awal");
        mainPanel.add(laporanPanel, "laporan");
        mainPanel.add(pembayaranPanel, "pembayaran");

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        cardLayout.show(mainPanel, "awal");
    }

    private void refreshPaymentTable() {
        pembayaranModel.setRowCount(0);
        try (Connection c = DBConnection.getConnection()) {
            String sql = "SELECT * FROM riwayat_pembayaran WHERE id_warga = (SELECT id_warga FROM user_app WHERE id_user=?) ORDER BY tanggal_pembayaran DESC";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    pembayaranModel.addRow(new Object[]{
                        rs.getInt("id_pembayaran"),
                        rs.getDouble("jumlah_pembayaran"),
                        rs.getString("metode_pembayaran"),
                        rs.getDate("tanggal_pembayaran"),
                        rs.getString("gambar_path"),
                        rs.getString("created_at")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data pembayaran.");
        }
    }

    private void showPaymentDialog() {
        JPanel p = new JPanel(new GridLayout(5, 2));
        JTextField tfJumlah = new JTextField();
        tfJumlah.setEditable(false); // agar warga tidak bisa ubah
        double jumlahTetap = 50000.0;
        tfJumlah.setText(String.valueOf(jumlahTetap));

        String[] metodeList = {"e-wallet", "transfer bank"};
        JComboBox<String> cbMetode = new JComboBox<>(metodeList);

        JComboBox<String> cbJenis = new JComboBox<>();
        JLabel lblTujuan = new JLabel("Nomor: -");

        cbMetode.addActionListener(e -> {
            cbJenis.removeAllItems();
            if (cbMetode.getSelectedItem().equals("e-wallet")) {
                cbJenis.addItem("GoPay");
                cbJenis.addItem("OVO");
                cbJenis.addItem("DANA");
            } else {
                cbJenis.addItem("BCA");
                cbJenis.addItem("BNI");
                cbJenis.addItem("BRI");
            }
        });

        cbJenis.addActionListener(e -> {
            String selected = (String) cbJenis.getSelectedItem();
            if (selected == null) {
                return;
            }
            switch (selected) {
                case "GoPay":
                    lblTujuan.setText("Nomor: 0812-3456-7890");
                    break;
                case "OVO":
                    lblTujuan.setText("Nomor: 0856-1122-3344");
                    break;
                case "DANA":
                    lblTujuan.setText("Nomor: 0888-2233-4455");
                    break;
                case "BCA":
                    lblTujuan.setText("Rekening: 1234567890");
                    break;
                case "BNI":
                    lblTujuan.setText("Rekening: 0987654321");
                    break;
                case "BRI":
                    lblTujuan.setText("Rekening: 5678901234");
                    break;
            }

        });

        JButton btnBrowse = new JButton("Pilih Bukti Pembayaran");
        JLabel lblPath = new JLabel("Belum dipilih");
        final String[] selectedFilePath = new String[1];

        btnBrowse.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Gambar", "jpg", "png", "jpeg"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fc.getSelectedFile();
                selectedFilePath[0] = selectedFile.getAbsolutePath();
                lblPath.setText(selectedFile.getName());
            }
        });

        p.add(new JLabel("Jumlah Pembayaran:"));
        p.add(tfJumlah);
        p.add(new JLabel("Metode Pembayaran:"));
        p.add(cbMetode);
        p.add(new JLabel("Jenis:"));
        p.add(cbJenis);
        p.add(new JLabel("Tujuan Transfer:"));
        p.add(lblTujuan);
        p.add(btnBrowse);
        p.add(lblPath);

        cbMetode.setSelectedIndex(0); // trigger jenis & tujuan default

        int ok = JOptionPane.showConfirmDialog(this, p, "Lakukan Pembayaran", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            if (selectedFilePath[0] == null || cbJenis.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Lengkapi semua data dan unggah bukti.");
                return;
            }

            try {
                // 1. Buat folder images jika belum ada
                String imagesDir = "images";
                File folder = new File(imagesDir);
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                // 2. Salin file bukti pembayaran
                File sourceFile = new File(selectedFilePath[0]);
                String newFileName = System.currentTimeMillis() + "_" + sourceFile.getName();
                File destFile = new File(folder, newFileName);

                java.nio.file.Files.copy(
                        sourceFile.toPath(),
                        destFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );

                String pathRelatif = imagesDir + "/" + newFileName;

                // 3. Gabungkan metode + jenis (misal: "E-Wallet - GoPay")
                String metode = cbMetode.getSelectedItem().toString() + " - " + cbJenis.getSelectedItem().toString();

                try (Connection c = DBConnection.getConnection()) {

                    // 4. Simpan data pembayaran
                    String sql = "INSERT INTO riwayat_pembayaran (id_warga, jumlah_pembayaran, metode_pembayaran, tanggal_pembayaran, gambar_path) "
                            + "VALUES ((SELECT id_warga FROM user_app WHERE id_user=?), ?, ?, CURRENT_DATE, ?)";
                    try (PreparedStatement ps = c.prepareStatement(sql)) {
                        ps.setInt(1, userId);
                        ps.setDouble(2, jumlahTetap); // jumlah tetap dari variabel
                        ps.setString(3, metode);
                        ps.setString(4, pathRelatif);
                        ps.executeUpdate();
                    }

                    // 5. Update status pembayaran di tabel warga
                    String updateSql = "UPDATE warga SET status_pembayaran = 'Sudah' WHERE id_warga = (SELECT id_warga FROM user_app WHERE id_user = ?)";
                    try (PreparedStatement updatePs = c.prepareStatement(updateSql)) {
                        updatePs.setInt(1, userId);
                        updatePs.executeUpdate();
                    }

                    // 6. Refresh tabel dan tampilkan notifikasi
                    refreshPaymentTable();
                    JOptionPane.showMessageDialog(this, "Pembayaran berhasil disimpan dan status diperbarui.");

                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal menyimpan pembayaran.");
            }

        }
    }

    private void refreshTable() {
        model.setRowCount(0);
        try (Connection c = DBConnection.getConnection()) {
            String sql = "SELECT l.*, w.id_rt, w.id_rw "
                    + "FROM laporan_sampah l "
                    + "JOIN user_app u ON l.id_warga=u.id_warga "
                    + "JOIN warga w ON w.id_warga = u.id_warga "
                    + "WHERE u.id_user=?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                int no = 1;
                while (rs.next()) {
                    int idLaporan = no++;
                    Date tanggal = rs.getDate("tanggal_lapor");
                    float organik = rs.getFloat("berat_organik");
                    float anorganik = rs.getFloat("berat_anorganik");
                    String catatan = rs.getString("catatan");
                    String status = rs.getString("status");
                    int idRt = rs.getInt("id_rt");
                    int idRw = rs.getInt("id_rw");

                    String jadwalSql = "SELECT * FROM jadwal_pengangkutan WHERE tanggal=? AND id_rt=? AND id_rw=?";
                    try (PreparedStatement psJadwal = c.prepareStatement(jadwalSql)) {
                        psJadwal.setDate(1, tanggal);
                        psJadwal.setInt(2, idRt);
                        psJadwal.setInt(3, idRw);
                        ResultSet rsJadwal = psJadwal.executeQuery();
                        boolean adaJadwal = rsJadwal.next();

                        if (adaJadwal && (status == null || !status.equalsIgnoreCase("Selesai"))) {
                            String updateStatus = "UPDATE laporan_sampah SET status='Selesai' WHERE id_laporan=?";
                            try (PreparedStatement psUpdate = c.prepareStatement(updateStatus)) {
                                psUpdate.setInt(1, idLaporan);
                                psUpdate.executeUpdate();
                            }
                            status = "Selesai";
                        }
                    }

                    model.addRow(new Object[]{idLaporan, tanggal, organik, anorganik, catatan, status});
                }
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Belum ada laporan yang tersedia.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data laporan.");
        }
    }

    private boolean isProfileComplete() {
        String sql = "SELECT w.nama, w.alamat, w.no_hp, w.no_rumah, w.catatan_rumah, w.status_pembayaran "
                + "FROM warga w JOIN user_app u ON w.id_warga = u.id_warga WHERE u.id_user=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                boolean isProfileFilled = rs.getString("nama") != null && !rs.getString("nama").trim().isEmpty()
                        && rs.getString("alamat") != null && !rs.getString("alamat").trim().isEmpty()
                        && rs.getString("no_hp") != null && !rs.getString("no_hp").trim().isEmpty()
                        && rs.getString("no_rumah") != null && !rs.getString("no_rumah").trim().isEmpty()
                        && rs.getString("catatan_rumah") != null && !rs.getString("catatan_rumah").trim().isEmpty();

                boolean isPaid = "Sudah".equalsIgnoreCase(rs.getString("status_pembayaran"));

                if (!isProfileFilled) {
                    JOptionPane.showMessageDialog(this, "Lengkapi profil terlebih dahulu.");
                    return false;
                }

                if (!isPaid) {
                    JOptionPane.showMessageDialog(this, "Silakan lakukan pembayaran terlebih dahulu.");
                    return false;
                }

                return true;
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

                if (o < 0 || a < 0) {
                    JOptionPane.showMessageDialog(this, "Berat tidak boleh negatif.");
                    return;
                }

                String ins = "INSERT INTO laporan_sampah (id_warga, tanggal_lapor, berat_organik, berat_anorganik, catatan, status) "
                        + "VALUES ((SELECT id_warga FROM user_app WHERE id_user=?), CURDATE(), ?, ?, ?, 'Menunggu')";
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
                JOptionPane.showMessageDialog(this, "Gagal menambahkan laporan.");
            }
        }
    }

    private void showUpdateProfileDialog() {
        String getSql = "SELECT w.id_warga, w.nama, w.alamat, w.no_rumah, w.no_hp, w.id_rw, w.id_rt, w.catatan_rumah "
                + "FROM warga w JOIN user_app u ON w.id_warga = u.id_warga WHERE u.id_user=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(getSql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int idWarga = rs.getInt("id_warga");

                JTextField tfNama = new JTextField(rs.getString("nama"));
                JTextField tfAlamat = new JTextField(rs.getString("alamat"));
                JTextField tfNoRumah = new JTextField(rs.getString("no_rumah"));
                JTextField tfHp = new JTextField(rs.getString("no_hp"));
                JTextField tfCatatanRumah = new JTextField(rs.getString("catatan_rumah"));

                JComboBox<String> cbRW = new JComboBox<>();
                JComboBox<String> cbRT = new JComboBox<>();
                int selectedRW = rs.getInt("id_rw");
                int selectedRT = rs.getInt("id_rt");

                PreparedStatement psRW = c.prepareStatement("SELECT * FROM rw");
                ResultSet rsRW = psRW.executeQuery();
                while (rsRW.next()) {
                    int id = rsRW.getInt("id_rw");
                    String label = "RW " + id + " - " + rsRW.getString("nama_rw");
                    cbRW.addItem(label);
                    if (id == selectedRW) {
                        cbRW.setSelectedItem(label);
                    }
                }

                cbRW.addActionListener(e -> {
                    cbRT.removeAllItems();
                    Object selected = cbRW.getSelectedItem();
                    if (selected == null) {
                        return;
                    }

                    int rwId = Integer.parseInt(selected.toString().split(" ")[1]);
                    try (PreparedStatement psRT = c.prepareStatement("SELECT * FROM rt WHERE id_rw=?")) {
                        psRT.setInt(1, rwId);
                        ResultSet rsRT = psRT.executeQuery();
                        while (rsRT.next()) {
                            int id = rsRT.getInt("id_rt");
                            String label = "RT " + rsRT.getInt("nomor_rt") + " - " + rsRT.getString("nama_rt");
                            cbRT.addItem(label);
                            if (id == selectedRT) {
                                cbRT.setSelectedItem(label);
                            }
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });

                if (cbRW.getItemCount() > 0) {
                    cbRW.setSelectedIndex(0);
                }

                JPanel p = new JPanel(new GridLayout(7, 2));
                p.add(new JLabel("Nama:"));
                p.add(tfNama);
                p.add(new JLabel("Alamat:"));
                p.add(tfAlamat);
                p.add(new JLabel("No Rumah:"));
                p.add(tfNoRumah);
                p.add(new JLabel("No HP:"));
                p.add(tfHp);
                p.add(new JLabel("Catatan Rumah:"));
                p.add(tfCatatanRumah);
                p.add(new JLabel("RW:"));
                p.add(cbRW);
                p.add(new JLabel("RT:"));
                p.add(cbRT);

                int ok = JOptionPane.showConfirmDialog(this, p, "Perbarui Profil", JOptionPane.OK_CANCEL_OPTION);
                if (ok == JOptionPane.OK_OPTION) {
                    if (tfNama.getText().isEmpty() || tfAlamat.getText().isEmpty()
                            || tfNoRumah.getText().isEmpty() || tfHp.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Semua field wajib diisi.");
                        return;
                    }

                    Object rwItem = cbRW.getSelectedItem();
                    Object rtItem = cbRT.getSelectedItem();
                    if (rwItem == null || rtItem == null) {
                        JOptionPane.showMessageDialog(this, "Pilih RW dan RT terlebih dahulu.");
                        return;
                    }

                    int rwId = Integer.parseInt(rwItem.toString().split(" ")[1]);
                    int rtId = Integer.parseInt(rtItem.toString().split(" ")[1]);

                    String upSql = "UPDATE warga SET nama=?, alamat=?, no_rumah=?, no_hp=?, catatan_rumah=?, id_rw=?, id_rt=? WHERE id_warga=?";
                    try (PreparedStatement up = c.prepareStatement(upSql)) {
                        up.setString(1, tfNama.getText());
                        up.setString(2, tfAlamat.getText());
                        up.setString(3, tfNoRumah.getText());
                        up.setString(4, tfHp.getText());
                        up.setString(5, tfCatatanRumah.getText());
                        up.setInt(6, rwId);
                        up.setInt(7, rtId);
                        up.setInt(8, idWarga);
                        up.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Profil berhasil diperbarui.");
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat atau memperbarui data profil.");
        }
    }
}
