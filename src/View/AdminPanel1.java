package View;

import Connection.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.table.DefaultTableCellRenderer;

public class AdminPanel1 extends JPanel {

    private final JFrame parentFrame;
    private final int userId;

    private JTabbedPane tabs; // Buat global agar bisa diakses di refreshData()

    private DefaultTableModel modelRW, modelRT, modelWarga, modelJadwal, modelKeluhan, modelUsers, modelPembayaran;

    public AdminPanel1(JFrame parentFrame, int userId) {
        this.parentFrame = parentFrame;
        this.userId = userId;
        setLayout(new BorderLayout());

        // Inisialisasi tab utama
        tabs = new JTabbedPane();
        tabs.addTab("RW", panelRW());
        tabs.addTab("RT", panelRT());
        tabs.addTab("Warga", panelWarga());
        tabs.addTab("Jadwal Pengangkutan", panelJadwal());
        tabs.addTab("Keluhan", panelKeluhan());
        tabs.addTab("Users", panelUsers());
        tabs.addTab("Riwayat Pembayaran", panelPembayaran());

        add(tabs, BorderLayout.CENTER);
    }

    public void refreshData() {
        int selectedTab = tabs.getSelectedIndex();
        String tabTitle = tabs.getTitleAt(selectedTab);

        switch (tabTitle) {
            case "RW":
                refreshRW();
                break;
            case "RT":
                refreshRT();
                break;
            case "Warga":
                refreshWarga();
                break;
            case "Jadwal Pengangkutan":
                refreshJadwal();
                break;
            case "Keluhan":
                refreshKeluhan();
                break;
            case "Users":
                refreshUsers();
                break;
            case "Riwayat Pembayaran":
                refreshPembayaran();
                break;
            default:
                break;
        }
    }

    private JPanel panelRW() {
        JPanel p = new JPanel(new BorderLayout());
        modelRW = new DefaultTableModel(new String[]{"ID RW", "Nama RW"}, 0);
        JTable tbl = new JTable(modelRW);
        refreshRW();

        JButton btnAdd = new JButton("Tambah RW");
        btnAdd.addActionListener(e -> {
            String nama = JOptionPane.showInputDialog(this, "Nama RW:");
            if (nama != null && !nama.trim().isEmpty()) {
                try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO rw (nama_rw) VALUES (?)")) {
                    ps.setString(1, nama.trim());
                    ps.executeUpdate();
                    refreshRW();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        JButton btnEdit = new JButton("Edit RW");
        btnEdit.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row != -1) {
                int idRW = (int) modelRW.getValueAt(row, 0);
                String currentNama = (String) modelRW.getValueAt(row, 1);
                String newNama = JOptionPane.showInputDialog(this, "Edit Nama RW:", currentNama);
                if (newNama != null && !newNama.trim().isEmpty()) {
                    try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE rw SET nama_rw=? WHERE id_rw=?")) {
                        ps.setString(1, newNama.trim());
                        ps.setInt(2, idRW);
                        ps.executeUpdate();
                        refreshRW();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Pilih RW yang ingin diedit.");
            }
        });

        JButton btnDelete = new JButton("Hapus RW");
        btnDelete.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row != -1) {
                int idRW = (int) modelRW.getValueAt(row, 0);
                if (JOptionPane.showConfirmDialog(this, "Yakin hapus RW ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM rw WHERE id_rw=?")) {
                        ps.setInt(1, idRW);
                        ps.executeUpdate();
                        refreshRW();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Pilih RW yang ingin dihapus.");
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);

        p.add(new JScrollPane(tbl), BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.SOUTH);
        return p;
    }

    private void refreshRW() {
        modelRW.setRowCount(0);
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM rw")) {
            while (rs.next()) {
                modelRW.addRow(new Object[]{rs.getInt("id_rw"), rs.getString("nama_rw")});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private JPanel panelRT() {
        JPanel p = new JPanel(new BorderLayout());
        modelRT = new DefaultTableModel(new String[]{"ID RT", "Nama RW", "Nama RT"}, 0);
        JTable tbl = new JTable(modelRT);
        refreshRT();

        JButton btnAdd = new JButton("Tambah RT");
        btnAdd.addActionListener(e -> showAddRT());

        JButton btnEdit = new JButton("Edit RT");
        btnEdit.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row != -1) {
                int idRT = (int) modelRT.getValueAt(row, 0);
                String namaRT = modelRT.getValueAt(row, 2).toString(); // Ambil dari kolom ke-2 (index 2)

                JTextField tfNama = new JTextField(namaRT);

                JPanel editPanel = new JPanel(new GridLayout(1, 2));
                editPanel.add(new JLabel("Nama RT:"));
                editPanel.add(tfNama);

                if (JOptionPane.showConfirmDialog(this, editPanel, "Edit RT", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE rt SET nama_rt=? WHERE id_rt=?")) {
                        ps.setString(1, tfNama.getText().trim());
                        ps.setInt(2, idRT);
                        ps.executeUpdate();
                        refreshRT();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Gagal mengupdate data RT.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Pilih RT yang ingin diedit.");
            }
        });

        JButton btnDelete = new JButton("Hapus RT");
        btnDelete.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row != -1) {
                int idRT = (int) modelRT.getValueAt(row, 0);
                if (JOptionPane.showConfirmDialog(this, "Yakin hapus RT ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM rt WHERE id_rt=?")) {
                        ps.setInt(1, idRT);
                        ps.executeUpdate();
                        refreshRT();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Pilih RT yang ingin dihapus.");
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);

        p.add(new JScrollPane(tbl), BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.SOUTH);
        return p;
    }

    private void showAddRT() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        JComboBox<String> cbRW = new JComboBox<>();

        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM rw")) {
            while (rs.next()) {
                cbRW.addItem(rs.getString("nama_rw"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JTextField tfNamaRT = new JTextField();

        panel.add(new JLabel("Pilih RW:"));
        panel.add(cbRW);
        panel.add(new JLabel("Nama RT:"));
        panel.add(tfNamaRT);

        if (JOptionPane.showConfirmDialog(this, panel, "Tambah RT", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                String selected = (String) cbRW.getSelectedItem();
                int idRW = Integer.parseInt(selected.split(" - ")[0]);
                String nama = tfNamaRT.getText().trim();

                if (!nama.isEmpty()) {
                    try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO rt (id_rw, nama_rt) VALUES (?, ?)")) {
                        ps.setInt(1, idRW);
                        ps.setString(2, nama);
                        ps.executeUpdate();
                        refreshRT();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Input tidak valid.");
            }
        }
    }

    private void refreshRT() {
        modelRT.setRowCount(0);
        String query = "SELECT rt.id_rt, rw.nama_rw, rt.nama_rt "
                + "FROM rt JOIN rw ON rt.id_rw = rw.id_rw";
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(query)) {
            while (rs.next()) {
                modelRT.addRow(new Object[]{
                    rs.getInt("id_rt"),
                    rs.getString("nama_rw"),
                    rs.getString("nama_rt")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private JPanel panelWarga() {
        JPanel panel = new JPanel(new BorderLayout());

        modelWarga = new DefaultTableModel(new String[]{
            "No", "ID Warga", "Nama", "Alamat", "No Rumah", "No HP", "Catatan", "RW", "RT", "ID User", "Status Pembayaran"
        }, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tbl = new JTable(modelWarga);
        tbl.getColumnModel().getColumn(0).setMaxWidth(40);
        JScrollPane scroll = new JScrollPane(tbl);

        JButton btnRefresh = new JButton("Refresh");
        JButton btnEdit = new JButton("Edit");

        JPanel panelButton = new JPanel();
        panelButton.add(btnRefresh);
        panelButton.add(btnEdit);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(panelButton, BorderLayout.SOUTH);

        refreshWarga();

        btnRefresh.addActionListener(e -> refreshWarga());

        btnEdit.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(null, "Pilih warga yang ingin diedit.");
                return;
            }

            int idWarga = (int) modelWarga.getValueAt(row, 1);
            int idUser = (int) modelWarga.getValueAt(row, 9);

            try (Connection c = DBConnection.getConnection()) {
                PreparedStatement ps = c.prepareStatement("SELECT * FROM warga WHERE id_warga = ?");
                ps.setInt(1, idWarga);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    JTextField tfNama = new JTextField(rs.getString("nama"));
                    JTextField tfAlamat = new JTextField(rs.getString("alamat"));
                    JTextField tfNoRumah = new JTextField(rs.getString("no_rumah"));
                    JTextField tfCatatan = new JTextField(rs.getString("catatan_rumah"));
                    JTextField tfHp = new JTextField(rs.getString("no_hp"));

                    JComboBox<String> cbRW = new JComboBox<>();
                    JComboBox<String> cbRT = new JComboBox<>();
                    int selectedRW = rs.getInt("id_rw");
                    int selectedRT = rs.getInt("id_rt");

                    // Load RW
                    PreparedStatement psRW = c.prepareStatement("SELECT * FROM rw");
                    ResultSet rsRW = psRW.executeQuery();
                    while (rsRW.next()) {
                        int id = rsRW.getInt("id_rw");
                        String label = rsRW.getString("nama_rw");
                        cbRW.addItem(label);
                        if (id == selectedRW) {
                            cbRW.setSelectedItem(label);
                        }
                    }

                    // Action RW to load RT
                    cbRW.addActionListener(ev -> {
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
                                String label = rsRT.getString("nama_rt");
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
                        cbRW.getActionListeners()[0].actionPerformed(null); // trigger isi RT
                    }

                    JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Sudah", "Belum"});
                    Object statusVal = modelWarga.getValueAt(row, 10);
                    cbStatus.setSelectedItem(statusVal != null ? statusVal.toString() : "Sudah");

                    JPanel form = new JPanel(new GridLayout(9, 2));
                    form.add(new JLabel("Nama:"));
                    form.add(tfNama);
                    form.add(new JLabel("Alamat:"));
                    form.add(tfAlamat);
                    form.add(new JLabel("No Rumah:"));
                    form.add(tfNoRumah);
                    form.add(new JLabel("Catatan Rumah:"));
                    form.add(tfCatatan);
                    form.add(new JLabel("No HP:"));
                    form.add(tfHp);
                    form.add(new JLabel("RW:"));
                    form.add(cbRW);
                    form.add(new JLabel("RT:"));
                    form.add(cbRT);
                    form.add(new JLabel("Status Akun:"));
                    form.add(cbStatus);

                    if (JOptionPane.showConfirmDialog(null, form, "Edit Warga", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        // Validasi
                        if (tfNama.getText().trim().isEmpty() || tfAlamat.getText().trim().isEmpty()
                                || tfNoRumah.getText().trim().isEmpty() || tfHp.getText().trim().isEmpty()) {
                            JOptionPane.showMessageDialog(null, "Isi semua data wajib (Nama, Alamat, No Rumah, No HP).");
                            return;
                        }

                        if (!tfHp.getText().matches("\\d+")) {
                            JOptionPane.showMessageDialog(null, "Nomor HP hanya boleh angka.");
                            return;
                        }

                        Object rwItem = cbRW.getSelectedItem();
                        Object rtItem = cbRT.getSelectedItem();
                        if (rwItem == null || rtItem == null) {
                            JOptionPane.showMessageDialog(null, "Pilih RW dan RT terlebih dahulu.");
                            return;
                        }

                        int rwId = Integer.parseInt(rwItem.toString().split(" ")[1]);

                        // Cari ID RT dari nama_rt
                        int rtId = -1;
                        PreparedStatement psGetRT = c.prepareStatement("SELECT id_rt FROM rt WHERE nama_rt=? AND id_rw=?");
                        psGetRT.setString(1, rtItem.toString());
                        psGetRT.setInt(2, rwId);
                        ResultSet rsGetRT = psGetRT.executeQuery();
                        if (rsGetRT.next()) {
                            rtId = rsGetRT.getInt("id_rt");
                        } else {
                            JOptionPane.showMessageDialog(null, "RT tidak ditemukan.");
                            return;
                        }

                        // Update data warga
                        PreparedStatement ps1 = c.prepareStatement(
                                "UPDATE warga SET nama=?, alamat=?, no_rumah=?, catatan_rumah=?, no_hp=?, id_rw=?, id_rt=?, status_pembayaran=? WHERE id_warga=?"
                        );
                        ps1.setString(1, tfNama.getText());
                        ps1.setString(2, tfAlamat.getText());
                        ps1.setString(3, tfNoRumah.getText());
                        ps1.setString(4, tfCatatan.getText());
                        ps1.setString(5, tfHp.getText());
                        ps1.setInt(6, rwId);
                        ps1.setInt(7, rtId);
                        ps1.setString(8, cbStatus.getSelectedItem().toString());
                        ps1.setInt(9, idWarga);
                        ps1.executeUpdate();

                        refreshWarga();
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Gagal memuat atau memperbarui data warga.");
            }
        });

        return panel;
    }

    private void refreshWarga() {
        modelWarga.setRowCount(0);
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT w.*, rt.nama_rt, rw.nama_rw "
                + "FROM warga w "
                + "JOIN users u ON w.id_user = u.id_user "
                + "LEFT JOIN rt ON w.id_rt = rt.id_rt "
                + "LEFT JOIN rw ON w.id_rw = rw.id_rw"
        ); ResultSet rs = ps.executeQuery()) {

            int no = 1;
            while (rs.next()) {
                modelWarga.addRow(new Object[]{
                    no++,
                    rs.getInt("id_warga"),
                    rs.getString("nama"),
                    rs.getString("alamat"),
                    rs.getString("no_rumah"),
                    rs.getString("catatan_rumah"),
                    rs.getString("nama_rt"),
                    rs.getString("nama_rw"),
                    rs.getString("no_hp"),
                    rs.getInt("id_user"),
                    rs.getString("status_pembayaran")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private JPanel panelJadwal() {
        JPanel p = new JPanel(new BorderLayout());
        modelJadwal = new DefaultTableModel(new String[]{"No", "ID Jadwal", "Tanggal", "Jam", "ID RT", "ID RW", "ID Petugas", "Status"}, 0);
        JTable tbl = new JTable(modelJadwal);
        refreshJadwal();

        JButton btnAdd = new JButton("Tambah Jadwal");
        btnAdd.addActionListener(e -> showAddJadwal());

        JButton btnDelete = new JButton("Hapus Jadwal");
        btnDelete.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih jadwal untuk dihapus.");
                return;
            }

            int id = (int) modelJadwal.getValueAt(row, 1); // Kolom ke-1 = ID Jadwal

            if (JOptionPane.showConfirmDialog(this, "Hapus jadwal dan status terkait?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try (Connection c = DBConnection.getConnection()) {
                    // 1. Hapus status terkait dulu
                    try (PreparedStatement ps1 = c.prepareStatement("DELETE FROM status_pengangkutan WHERE id_jadwal = ?")) {
                        ps1.setInt(1, id);
                        ps1.executeUpdate();
                    }

                    // 2. Hapus jadwalnya
                    try (PreparedStatement ps2 = c.prepareStatement("DELETE FROM jadwal_pengangkutan WHERE id_jadwal = ?")) {
                        ps2.setInt(1, id);
                        ps2.executeUpdate();
                    }

                    refreshJadwal();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Gagal menghapus jadwal.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnAdd);
        btnPanel.add(btnDelete);

        p.add(new JScrollPane(tbl), BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.SOUTH);
        return p;
    }

    private void refreshJadwal() {
        modelJadwal.setRowCount(0);
        String sql
                = "SELECT j.*, "
                + "rt.nama_rt, rw.nama_rw, "
                + "p.nama AS nama_petugas, "
                + "s.status "
                + "FROM jadwal_pengangkutan j "
                + "LEFT JOIN rt ON j.id_rt = rt.id_rt "
                + "LEFT JOIN rw ON j.id_rw = rw.id_rw "
                + "LEFT JOIN status_pengangkutan s ON j.id_jadwal = s.id_jadwal "
                + "LEFT JOIN petugas p ON s.id_petugas = p.id_petugas ";

        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            int no = 1;
            while (rs.next()) {
                modelJadwal.addRow(new Object[]{
                    no++,
                    rs.getInt("id_jadwal"),
                    rs.getDate("tanggal"),
                    rs.getString("jam"),
                    rs.getString("nama_rt"),
                    rs.getString("nama_rw"),
                    rs.getString("nama_petugas"),
                    rs.getString("status"),});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showAddJadwal() {
        JPanel panel = new JPanel(new GridLayout(4, 2));
        JSpinner spTanggal = new JSpinner(new SpinnerDateModel());
        spTanggal.setEditor(new JSpinner.DateEditor(spTanggal, "yyyy-MM-dd"));
        JSpinner spJam = new JSpinner(new SpinnerDateModel());
        spJam.setEditor(new JSpinner.DateEditor(spJam, "HH:mm"));

        JComboBox<String> cbRW = new JComboBox<>();
        JComboBox<String> cbRT = new JComboBox<>();

        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM rw")) {
            while (rs.next()) {
                cbRW.addItem(rs.getInt("id_rw") + " - " + rs.getString("nama_rw"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        cbRW.addActionListener(e -> {
            cbRT.removeAllItems();
            try {
                int idRW = Integer.parseInt(cbRW.getSelectedItem().toString().split(" - ")[0]);
                try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM rt WHERE id_rw=?")) {
                    ps.setInt(1, idRW);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        cbRT.addItem(rs.getString("nama_rt"));
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        panel.add(new JLabel("Tanggal:"));
        panel.add(spTanggal);
        panel.add(new JLabel("Jam:"));
        panel.add(spJam);
        panel.add(new JLabel("RW:"));
        panel.add(cbRW);
        panel.add(new JLabel("RT:"));
        panel.add(cbRT);

        if (JOptionPane.showConfirmDialog(this, panel, "Tambah Jadwal", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int idRT = Integer.parseInt(cbRT.getSelectedItem().toString().split(" - ")[0]);
                int idRW = Integer.parseInt(cbRW.getSelectedItem().toString().split(" - ")[0]);
                java.sql.Date tanggal = new java.sql.Date(((Date) spTanggal.getValue()).getTime());
                java.sql.Time jam = new java.sql.Time(((Date) spJam.getValue()).getTime());

                try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO jadwal_pengangkutan (tanggal, jam, id_rt, id_rw) VALUES (?, ?, ?, ?)")) {
                    ps.setDate(1, tanggal);
                    ps.setTime(2, jam);
                    ps.setInt(3, idRT);
                    ps.setInt(4, idRW);
                    ps.executeUpdate();
                    refreshJadwal();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Input tidak valid.");
            }
        }
    }

    private JPanel panelKeluhan() {
        JPanel p = new JPanel(new BorderLayout());
        modelKeluhan = new DefaultTableModel(new String[]{
            "ID Keluhan", "No", "ID Warga / Nama Warga", "Isi Keluhan", "Tanggal Keluhan",
            "Tanggapan", "Ditanggapi Oleh", "Tanggal Ditanggapi",
            "Melibatkan Petugas?(Tidak Wajib)", "Status Keluhan", "Aksi"}, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // semua tidak bisa diketik langsung
            }
        };

        JTable tbl = new JTable(modelKeluhan);

        // Sembunyikan kolom ID Keluhan (0)
        tbl.getColumnModel().getColumn(0).setMinWidth(0);
        tbl.getColumnModel().getColumn(0).setMaxWidth(0);

        // Tambahkan tombol detail ke kolom aksi
        tbl.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tbl.rowAtPoint(e.getPoint());
                int col = tbl.columnAtPoint(e.getPoint());
                if (col == 10) { // Kolom "Aksi"
                    showDetailDialog(tbl, row);
                }
            }
        });

        refreshKeluhan();
        p.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return p;
    }

    private void showDetailDialog(JTable tbl, int row) {
        int idKeluhan = (int) tbl.getValueAt(row, 0);
        String isi = (String) tbl.getValueAt(row, 3);
        String tanggapan = (String) tbl.getValueAt(row, 5);
        String status = (String) tbl.getValueAt(row, 9);

        JTextArea txtTanggapan = new JTextArea(tanggapan != null ? tanggapan : "", 4, 30);
        txtTanggapan.setLineWrap(true);
        txtTanggapan.setWrapStyleWord(true);

        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Menunggu", "Diproses", "Selesai", "Ditolak"});
        cmbStatus.setSelectedItem(status);

        Map<String, Integer> petugasMap = new LinkedHashMap<>();
        JComboBox<String> cmbPetugas = new JComboBox<>();
        cmbPetugas.addItem("-- Tidak Melibatkan Petugas --");
        petugasMap.put("-- Tidak Melibatkan Petugas --", null);
        try (Connection c = DBConnection.getConnection(); ResultSet rs = c.createStatement().executeQuery("SELECT id_petugas, nama FROM petugas")) {
            while (rs.next()) {
                String label = rs.getInt("id_petugas") + " - " + rs.getString("nama");
                cmbPetugas.addItem(label);
                petugasMap.put(label, rs.getInt("id_petugas"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        form.add(new JLabel("Isi Keluhan:"));
        form.add(new JScrollPane(new JTextArea(isi, 3, 30) {
            {
                setEditable(false);
                setBackground(new Color(245, 245, 245));
            }
        }));
        form.add(Box.createVerticalStrut(8));

        form.add(new JLabel("Tanggapan:"));
        form.add(new JScrollPane(txtTanggapan));
        form.add(Box.createVerticalStrut(8));

        form.add(new JLabel("Status Keluhan:"));
        form.add(cmbStatus);
        form.add(Box.createVerticalStrut(8));

        form.add(new JLabel("Melibatkan Petugas (Opsional):"));
        form.add(cmbPetugas);

        int result = JOptionPane.showConfirmDialog(null, form, "Detail Keluhan", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (Connection c = DBConnection.getConnection()) {
                String sql = "UPDATE keluhan SET tanggapan=?, status_keluhan=?, ditanggapi_oleh=?, tanggal_ditanggapi=NOW(), id_petugas=? WHERE id_keluhan=?";
                PreparedStatement ps = c.prepareStatement(sql);
                ps.setString(1, txtTanggapan.getText());
                ps.setString(2, (String) cmbStatus.getSelectedItem());
                ps.setInt(3, userId); // ← ganti dengan user login ID
                Integer idPetugas = petugasMap.get((String) cmbPetugas.getSelectedItem());
                if (idPetugas != null) {
                    ps.setInt(4, idPetugas);
                } else {
                    ps.setNull(4, Types.INTEGER);
                }
                ps.setInt(5, idKeluhan);
                ps.executeUpdate();
                refreshKeluhan();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshKeluhan() {
        modelKeluhan.setRowCount(0);
        String query = "SELECT k.*, w.nama AS nama_warga, p.nama AS nama_petugas, a.nama AS nama_admin "
                + "FROM keluhan k "
                + "LEFT JOIN warga w ON k.id_warga = w.id_warga "
                + "LEFT JOIN petugas p ON k.id_petugas = p.id_petugas "
                + "LEFT JOIN admin a ON k.ditanggapi_oleh = a.id_admin";

        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(query)) {

            int no = 1;
            while (rs.next()) {
                String petugasInfo = rs.getString("id_petugas") != null
                        ? rs.getInt("id_petugas") + " / " + rs.getString("nama_petugas")
                        : "-";

                String adminInfo = rs.getString("ditanggapi_oleh") != null
                        ? rs.getString("ditanggapi_oleh") + " / " + rs.getString("nama_admin")
                        : "-";

                String tanggapan = rs.getString("tanggapan") != null
                        ? rs.getString("tanggapan") : "-";

                Timestamp tglTanggapan = rs.getTimestamp("tanggal_ditanggapi");
                String tglTanggapanStr = (tglTanggapan != null) ? tglTanggapan.toString() : "-";

                modelKeluhan.addRow(new Object[]{
                    rs.getInt("id_keluhan"), // hidden
                    no++,
                    rs.getInt("id_warga") + " / " + rs.getString("nama_warga"),
                    rs.getString("isi_keluhan"),
                    rs.getDate("created_at"),
                    tanggapan,
                    adminInfo,
                    tglTanggapanStr,
                    petugasInfo,
                    rs.getString("status_keluhan"),
                    "Detail"
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private JPanel panelUsers() {
        JPanel p = new JPanel(new BorderLayout());

        // Tabel
        modelUsers = new DefaultTableModel(
                new String[]{"No", "ID User", "Username", "Role"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblUsers = new JTable(modelUsers);
        tblUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refreshUsers();

        // ===================== FORM TAMBAH USER =====================
        JTextField txtNama = new JTextField(10);
        JTextField txtUsername = new JTextField(10);
        JPasswordField txtPassword = new JPasswordField(10);
        JComboBox<String> cmbRole = new JComboBox<>(new String[]{"admin", "petugas", "warga"});
        JButton btnSimpan = new JButton("Simpan");

        JPanel formPanel = new JPanel();
        formPanel.setBorder(BorderFactory.createTitledBorder("Tambah User Baru"));
        formPanel.add(new JLabel("Nama:"));
        formPanel.add(txtNama);
        formPanel.add(new JLabel("Username:"));
        formPanel.add(txtUsername);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(txtPassword);
        formPanel.add(new JLabel("Role:"));
        formPanel.add(cmbRole);
        formPanel.add(btnSimpan);

        btnSimpan.addActionListener(ev -> {
            String nama = txtNama.getText().trim();
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword()).trim();
            String role = cmbRole.getSelectedItem().toString();

            if (nama.isEmpty() || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(p, "Semua field wajib diisi!");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                // Cek username
                String cekSQL = "SELECT COUNT(*) FROM users WHERE username = ?";
                try (PreparedStatement cekStmt = con.prepareStatement(cekSQL)) {
                    cekStmt.setString(1, username);
                    ResultSet rs = cekStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(p, "Username sudah digunakan.");
                        return;
                    }
                }

                con.setAutoCommit(false);

                // Insert ke tabel users
                String sqlUser = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                int idUser;
                try (PreparedStatement psUser = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                    psUser.setString(1, username);
                    psUser.setString(2, hashPassword(password));
                    psUser.setString(3, role);
                    psUser.executeUpdate();

                    ResultSet generatedKeys = psUser.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        idUser = generatedKeys.getInt(1);
                    } else {
                        con.rollback();
                        throw new SQLException("Gagal mendapatkan ID user.");
                    }
                }

                String insertDetail = null;

                switch (role) {
                    case "admin":
                        insertDetail = "INSERT INTO admin (nama, id_user) VALUES (?, ?)";
                        break;
                    case "petugas":
                        insertDetail = "INSERT INTO petugas (nama, id_user) VALUES (?, ?)";
                        break;
                    case "warga":
                        insertDetail = "INSERT INTO warga (nama, id_user) VALUES (?, ?)";
                        break;
                }

                if (insertDetail != null) {
                    try (PreparedStatement pst = con.prepareStatement(insertDetail)) {
                        pst.setString(1, nama);
                        pst.setInt(2, idUser);
                        pst.executeUpdate();
                    }
                }

                con.commit();
                refreshUsers();
                JOptionPane.showMessageDialog(p, "User berhasil ditambahkan.");

                // Reset input
                txtNama.setText("");
                txtUsername.setText("");
                txtPassword.setText("");
                cmbRole.setSelectedIndex(0);

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(p, "Gagal menambahkan user: " + ex.getMessage());
            }
        });

        // ======================= TOMBOL EDIT =========================
        JButton btnEdit = new JButton("Edit");
        btnEdit.addActionListener(e -> {
            int selectedRow = tblUsers.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(p, "Pilih user yang ingin diedit.");
                return;
            }

            int idUser = (int) modelUsers.getValueAt(selectedRow, 1);
            String currentUsername = (String) modelUsers.getValueAt(selectedRow, 2);
            String currentRole = (String) modelUsers.getValueAt(selectedRow, 3);

            JTextField txtEditUsername = new JTextField(currentUsername, 15);
            JComboBox<String> cmbEditRole = new JComboBox<>(new String[]{"admin", "petugas", "warga"});
            cmbEditRole.setSelectedItem(currentRole);

            JPasswordField txtNewPassword = new JPasswordField(15);
            JCheckBox chkGantiPassword = new JCheckBox("Ganti Password");

            JPanel editPanel = new JPanel(new GridLayout(0, 1, 5, 5));
            editPanel.add(new JLabel("Username:"));
            editPanel.add(txtEditUsername);
            editPanel.add(new JLabel("Role:"));
            editPanel.add(cmbEditRole);
            editPanel.add(chkGantiPassword);
            editPanel.add(new JLabel("Password Baru (Opsional):"));
            editPanel.add(txtNewPassword);
            txtNewPassword.setEnabled(false); // default non-aktif

            chkGantiPassword.addActionListener(evt -> txtNewPassword.setEnabled(chkGantiPassword.isSelected()));

            int result = JOptionPane.showConfirmDialog(p, editPanel, "Edit User", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            String newUsername = txtEditUsername.getText().trim();
            String newRole = cmbEditRole.getSelectedItem().toString();
            String newPassword = new String(txtNewPassword.getPassword()).trim();

            if (newUsername.isEmpty()) {
                JOptionPane.showMessageDialog(p, "Username tidak boleh kosong.");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {
                // Cek jika username sudah digunakan oleh user lain
                String cekSQL = "SELECT COUNT(*) FROM users WHERE username = ? AND id_user != ?";
                try (PreparedStatement cekStmt = con.prepareStatement(cekSQL)) {
                    cekStmt.setString(1, newUsername);
                    cekStmt.setInt(2, idUser);
                    ResultSet rs = cekStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(p, "Username sudah digunakan.");
                        return;
                    }
                }

                // Update statement
                String sql;
                PreparedStatement ps;

                if (chkGantiPassword.isSelected() && !newPassword.isEmpty()) {
                    sql = "UPDATE users SET username = ?, role = ?, password = ? WHERE id_user = ?";
                    ps = con.prepareStatement(sql);
                    ps.setString(1, newUsername);
                    ps.setString(2, newRole);
                    ps.setString(3, hashPassword(newPassword));
                    ps.setInt(4, idUser);
                } else {
                    sql = "UPDATE users SET username = ?, role = ? WHERE id_user = ?";
                    ps = con.prepareStatement(sql);
                    ps.setString(1, newUsername);
                    ps.setString(2, newRole);
                    ps.setInt(3, idUser);
                }

                ps.executeUpdate();
                refreshUsers();
                JOptionPane.showMessageDialog(p, "User berhasil diupdate.");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(p, "Gagal mengupdate user: " + ex.getMessage());
            }
        });

        // ======================= TOMBOL HAPUS ========================
        JButton btnHapus = new JButton("Hapus");
        btnHapus.addActionListener(e -> {
            int selectedRow = tblUsers.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(p, "Pilih user yang ingin dihapus.");
                return;
            }

            int idUser = (int) modelUsers.getValueAt(selectedRow, 1); // Kolom "ID User"
            String role = (String) modelUsers.getValueAt(selectedRow, 3); // Kolom "Role" — diperbaiki dari 4 ke 3

            int confirm = JOptionPane.showConfirmDialog(p, "Yakin ingin menghapus user ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try (Connection c = DBConnection.getConnection()) {
                c.setAutoCommit(false);

                String deleteDetail = null;

                switch (role) {
                    case "admin":
                        deleteDetail = "DELETE FROM admin WHERE id_user = ?";
                        break;
                    case "petugas":
                        deleteDetail = "DELETE FROM petugas WHERE id_user = ?";
                        break;
                    case "warga":
                        deleteDetail = "DELETE FROM warga WHERE id_user = ?";
                        break;
                }

                if (deleteDetail != null) {
                    try (PreparedStatement pst = c.prepareStatement(deleteDetail)) {
                        pst.setInt(1, idUser);
                        pst.executeUpdate();
                    }
                }

                try (PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id_user = ?")) {
                    ps.setInt(1, idUser);
                    ps.executeUpdate();
                }

                c.commit();
                refreshUsers();
                JOptionPane.showMessageDialog(p, "User berhasil dihapus.");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(p, "Gagal menghapus user.");
            }
        });

        // ======================= LAYOUT FINAL ========================
        JPanel panelButton = new JPanel();
        panelButton.add(btnEdit);
        panelButton.add(btnHapus);

        p.add(formPanel, BorderLayout.NORTH);
        p.add(new JScrollPane(tblUsers), BorderLayout.CENTER);
        p.add(panelButton, BorderLayout.SOUTH);

        return p;
    }

    private void refreshUsers() {
        modelUsers.setRowCount(0);
        String query = "SELECT id_user, username, role FROM users WHERE id_user != ?";

        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(query)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                int no = 1;
                while (rs.next()) {
                    modelUsers.addRow(new Object[]{
                        no++,
                        rs.getInt("id_user"),
                        rs.getString("username"),
                        rs.getString("role")
                    });
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
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

    private JPanel panelPembayaran() {
        JPanel p = new JPanel(new BorderLayout());

        modelPembayaran = new DefaultTableModel(new String[]{"ID", "ID Warga", "Jumlah", "Metode", "Tanggal", "Bukti", "Detail"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return column == 6; // hanya kolom "Detail" yang dapat diklik
            }
        };

        JTable tbl = new JTable(modelPembayaran);

        tbl.getColumn("Detail").setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                return new JButton("Detail");
            }
        });

        tbl.getColumn("Detail").setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            final JButton btn = new JButton("Detail");
            boolean clicked;
            int selectedRow;

            {
                btn.addActionListener(e -> {
                    clicked = true;
                    fireEditingStopped(); // tutup editor
                });
            }

            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                    int row, int column) {
                selectedRow = row;
                return btn;
            }

            public Object getCellEditorValue() {
                if (clicked) {
                    String id = modelPembayaran.getValueAt(selectedRow, 0).toString();
                    String idWarga = modelPembayaran.getValueAt(selectedRow, 1).toString();
                    String jumlah = modelPembayaran.getValueAt(selectedRow, 2).toString();
                    String metode = modelPembayaran.getValueAt(selectedRow, 3).toString();
                    String tanggal = modelPembayaran.getValueAt(selectedRow, 4).toString();
                    String buktiPath = (String) modelPembayaran.getValueAt(selectedRow, 5);

                    // Panel utama
                    JPanel panel = new JPanel(new BorderLayout(10, 10));

                    // Label teks detail pembayaran
                    String htmlDetail = "<html><b>ID Pembayaran:</b> " + id
                            + "<br><b>ID Warga:</b> " + idWarga
                            + "<br><b>Jumlah:</b> " + jumlah
                            + "<br><b>Metode:</b> " + metode
                            + "<br><b>Tanggal:</b> " + tanggal + "</html>";

                    panel.add(new JLabel(htmlDetail), BorderLayout.NORTH);

                    // Tambahkan gambar jika path valid
                    if (buktiPath != null && !buktiPath.isEmpty()) {
                        File imgFile = new File(buktiPath);
                        if (imgFile.exists()) {
                            ImageIcon icon = new ImageIcon(buktiPath);
                            Image img = icon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
                            JLabel imgLabel = new JLabel(new ImageIcon(img));
                            imgLabel.setBorder(BorderFactory.createTitledBorder("Bukti Pembayaran"));
                            panel.add(imgLabel, BorderLayout.CENTER);
                        } else {
                            panel.add(new JLabel("Bukti pembayaran tidak ditemukan."), BorderLayout.CENTER);
                        }
                    }

                    // Tampilkan detail di dialog
                    JOptionPane.showMessageDialog(null, panel, "Detail Pembayaran", JOptionPane.INFORMATION_MESSAGE);
                }
                clicked = false;
                return "Detail";
            }

            public boolean stopCellEditing() {
                clicked = false;
                return super.stopCellEditing();
            }
        });

        refreshPembayaran();
        p.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return p;
    }

    private void refreshPembayaran() {
        modelPembayaran.setRowCount(0);
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM riwayat_pembayaran")) {
            while (rs.next()) {
                modelPembayaran.addRow(new Object[]{
                    rs.getInt("id_pembayaran"),
                    rs.getInt("id_warga"),
                    rs.getDouble("jumlah_pembayaran"),
                    rs.getString("metode_pembayaran"),
                    rs.getDate("tanggal_pembayaran"),
                    rs.getString("gambar_path"),
                    "Detail"
                });
            }
        } catch (SQLException ex) {
        }
    }
}
