package View;

import Connection.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PetugasPanel1 extends JPanel {

    private JTable jadwalTable, laporanTable, keluhanTable;
    private DefaultTableModel jadwalModel, laporanModel, keluhanModel;
    private int userId;

    private CardLayout cardLayout;
    private JPanel contentPanel;

    public PetugasPanel1(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());

        // Panel tombol navigasi
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnJadwal = new JButton("Lihat Jadwal");
        JButton btnLaporan = new JButton("Lihat Laporan");
        JButton btnKeluhan = new JButton("Lihat Keluhan");

        buttonPanel.add(btnJadwal);
        buttonPanel.add(btnLaporan);
        buttonPanel.add(btnKeluhan);

        add(buttonPanel, BorderLayout.NORTH);

        // CardLayout untuk konten utama
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        contentPanel.add(JadwalPanel(), "jadwal");
        contentPanel.add(LaporanPanel(), "laporan");
        contentPanel.add(KeluhanPanel(), "keluhan");

        add(contentPanel, BorderLayout.CENTER);

        // Default tampilan
        cardLayout.show(contentPanel, "jadwal");

        // Aksi tombol
        btnJadwal.addActionListener(e -> cardLayout.show(contentPanel, "jadwal"));
        btnLaporan.addActionListener(e -> cardLayout.show(contentPanel, "laporan"));
        btnKeluhan.addActionListener(e -> cardLayout.show(contentPanel, "keluhan"));

        // Refresh data awal
        refreshJadwal();
        refreshLaporan();
        refreshKeluhan();
    }

    public void refreshData() {
        for (Component comp : contentPanel.getComponents()) {
            if (comp.isVisible()) {
                if (comp == contentPanel.getComponent(0)) {
                    refreshJadwal();
                } else if (comp == contentPanel.getComponent(1)) {
                    refreshLaporan();
                } else if (comp == contentPanel.getComponent(2)) {
                    refreshKeluhan();
                }
                break;
            }
        }
    }

    private JPanel JadwalPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Jadwal Pengangkutan"));

        jadwalModel = new DefaultTableModel(new String[]{"ID Jadwal", "Tanggal", "Jam", "RT", "RW", "Status"}, 0);
        jadwalTable = new JTable(jadwalModel);
        panel.add(new JScrollPane(jadwalTable), BorderLayout.CENTER);

        JButton btnUpdate = new JButton("Update Status");
        btnUpdate.addActionListener(e -> {
            int row = jadwalTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih jadwal terlebih dahulu.");
                return;
            }

            int idJadwal = (int) jadwalModel.getValueAt(row, 0);
            String statusLama = (String) jadwalModel.getValueAt(row, 5);
            String statusBaru = (String) JOptionPane.showInputDialog(this, "Pilih status:",
                    "Update Status", JOptionPane.PLAIN_MESSAGE, null, new String[]{"belum", "sudah"}, statusLama);

            if (statusBaru == null || statusBaru.equals(statusLama)) {
                return;
            }

            int konfirmasi = JOptionPane.showConfirmDialog(this,
                    "Yakin ingin mengubah status menjadi '" + statusBaru + "'?",
                    "Konfirmasi", JOptionPane.YES_NO_OPTION);

            if (konfirmasi != JOptionPane.YES_OPTION) {
                return;
            }

            try (Connection c = DBConnection.getConnection()) {
                String sql = "INSERT INTO status_pengangkutan (id_jadwal, id_petugas, status, catatan, tanggal_input) "
                        + "VALUES (?, ?, ?, NULL, NOW()) "
                        + "ON DUPLICATE KEY UPDATE status=VALUES(status), tanggal_input=NOW()";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setInt(1, idJadwal);
                    ps.setInt(2, userId);
                    ps.setString(3, statusBaru);
                    ps.executeUpdate();
                }

                if ("sudah".equals(statusBaru)) {
                    String detailSql = "SELECT id_rt, id_rw, tanggal FROM jadwal_pengangkutan WHERE id_jadwal=?";
                    try (PreparedStatement ps = c.prepareStatement(detailSql)) {
                        ps.setInt(1, idJadwal);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            int idRt = rs.getInt("id_rt"), idRw = rs.getInt("id_rw");
                            Date tanggal = rs.getDate("tanggal");
                            String updateLaporan = "UPDATE laporan_sampah SET status='Selesai' "
                                    + "WHERE tanggal_lapor <= ? AND id_warga IN "
                                    + "(SELECT id_warga FROM warga WHERE id_rt=? AND id_rw=?)";

                            try (PreparedStatement psU = c.prepareStatement(updateLaporan)) {
                                psU.setDate(1, tanggal);
                                psU.setInt(2, idRt);
                                psU.setInt(3, idRw);
                                psU.executeUpdate();
                            }
                        }
                    }
                }

                refreshJadwal();
                refreshLaporan();
                JOptionPane.showMessageDialog(this, "Status diperbarui menjadi '" + statusBaru + "'.");

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal mengupdate status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(btnUpdate, BorderLayout.SOUTH);
        return panel;
    }

    public void refreshJadwal() {
        jadwalModel.setRowCount(0);
        String sql = "SELECT j.id_jadwal, j.tanggal, j.jam, j.id_rt, j.id_rw, "
                + "(SELECT status FROM status_pengangkutan s WHERE s.id_jadwal=j.id_jadwal AND s.id_petugas=?) AS sts "
                + "FROM jadwal_pengangkutan j";

        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String status = rs.getString(6);
                jadwalModel.addRow(new Object[]{
                    rs.getInt(1), rs.getDate(2), rs.getString(3),
                    rs.getInt(4), rs.getInt(5), (status != null ? status : "belum")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private JPanel LaporanPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Laporan Sampah Warga"));

        laporanModel = new DefaultTableModel(
                new String[]{"Nama Warga", "Tanggal", "Organik (kg)", "Anorganik (kg)", "Catatan"}, 0);
        laporanTable = new JTable(laporanModel);

        panel.add(new JScrollPane(laporanTable), BorderLayout.CENTER);
        return panel;
    }

    public void refreshLaporan() {
        laporanModel.setRowCount(0);
        String sql = "SELECT w.nama, l.tanggal_lapor, l.berat_organik, l.berat_anorganik, l.catatan "
                + "FROM laporan_sampah l "
                + "JOIN warga w ON l.id_warga = w.id_warga";

        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                laporanModel.addRow(new Object[]{
                    rs.getString(1), rs.getDate(2),
                    rs.getDouble(3), rs.getDouble(4),
                    rs.getString(5)
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mengambil data laporan.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel KeluhanPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Keluhan Warga"));

        keluhanModel = new DefaultTableModel(new String[]{
            "No", "ID", "ID Warga", "Isi Keluhan", "Gambar", "Tanggapan", "Ditanggapi Oleh", "Tanggal Tanggapan", "Petugas PJ", "Status"
        }, 0);
        keluhanTable = new JTable(keluhanModel);
        panel.add(new JScrollPane(keluhanTable), BorderLayout.CENTER);

        JButton btnUbahStatus = new JButton("Ubah Status");
        btnUbahStatus.addActionListener(e -> {
            int row = keluhanTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih keluhan terlebih dahulu.");
                return;
            }

            int idKeluhan = (int) keluhanModel.getValueAt(row, 1); // Kolom ID keluhan

            String[] statusEnum = {"Menunggu", "Diproses", "Selesai", "Ditolak"};
            String statusBaru = (String) JOptionPane.showInputDialog(
                    this,
                    "Pilih status baru untuk keluhan:",
                    "Ubah Status Keluhan",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    statusEnum,
                    statusEnum[0]
            );

            if (statusBaru == null) {
                return; // Batal
            }
            try (Connection c = DBConnection.getConnection()) {
                String sql = "UPDATE keluhan SET status_keluhan = ?, tanggal_ditanggapi = NOW() WHERE id_keluhan = ?";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, statusBaru);
                    ps.setInt(2, idKeluhan);
                    ps.executeUpdate();
                }
                refreshKeluhan();
                JOptionPane.showMessageDialog(this, "Status keluhan berhasil diperbarui menjadi '" + statusBaru + "'.");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal memperbarui status keluhan.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btnUbahStatus, BorderLayout.SOUTH);

        return panel;
    }

    public void refreshKeluhan() {
        keluhanModel.setRowCount(0);

        int idPetugas = -1;

        // 1. Ambil id_petugas berdasarkan id_user
        String sqlGetPetugas = "SELECT id_petugas FROM petugas WHERE id_user = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps1 = c.prepareStatement(sqlGetPetugas)) {

            ps1.setInt(1, userId); // Asumsikan userId sudah tersedia di kelas
            try (ResultSet rs1 = ps1.executeQuery()) {
                if (rs1.next()) {
                    idPetugas = rs1.getInt("id_petugas");
                } else {
                    JOptionPane.showMessageDialog(this, "Petugas tidak ditemukan.");
                    return;
                }
            }

            // 2. Ambil semua keluhan yang ditangani oleh id_petugas tersebut
            String sqlKeluhan = "SELECT k.*, p.nama AS nama_petugas FROM keluhan k "
                    + "LEFT JOIN petugas p ON k.id_petugas = p.id_petugas "
                    + "WHERE k.id_petugas = ?";

            try (PreparedStatement ps2 = c.prepareStatement(sqlKeluhan)) {
                ps2.setInt(1, idPetugas);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    int no = 1;
                    while (rs2.next()) {
                        keluhanModel.addRow(new Object[]{
                            no++,
                            rs2.getInt("id_keluhan"),
                            rs2.getInt("id_warga"),
                            rs2.getString("isi_keluhan"),
                            rs2.getString("gambar_path"),
                            rs2.getString("tanggapan"),
                            rs2.getString("ditanggapi_oleh"),
                            rs2.getTimestamp("tanggal_ditanggapi"),
                            rs2.getString("nama_petugas"),
                            rs2.getString("status_keluhan")
                        });
                    }
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mengambil data keluhan.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
