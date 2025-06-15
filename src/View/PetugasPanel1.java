package View;

import Connection.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class PetugasPanel1 extends JPanel {

    private DefaultTableModel jadwalModel;
    private DefaultTableModel laporanModel;
    private int userId;

    private JTable jadwalTable;
    private JTable laporanTable;

    // Filter komponen untuk laporan
    private JTextField txtTanggalMulai, txtTanggalAkhir;
    private JComboBox<String> cbRT, cbRW;

    public PetugasPanel1(int userId) {
        this.userId = userId;

        setLayout(new BorderLayout());

        // Tabbed Pane
        JTabbedPane tabs = new JTabbedPane();

        // Tab 1: Jadwal Pengangkutan
        JPanel jadwalPanel = new JPanel(new BorderLayout());
        jadwalModel = new DefaultTableModel(new String[]{"ID Jadwal", "Tanggal", "Jam", "RT", "RW", "Status"}, 0);
        jadwalTable = new JTable(jadwalModel);
        refreshJadwalTable();
        JButton btnUpdate = new JButton("Update Status");
        btnUpdate.addActionListener(e -> updateStatus(jadwalTable));
        jadwalPanel.add(new JScrollPane(jadwalTable), BorderLayout.CENTER);
        jadwalPanel.add(btnUpdate, BorderLayout.SOUTH);
        tabs.add("Jadwal Pengangkutan", jadwalPanel);

        // Tab 2: Laporan Sampah Warga
        JPanel laporanPanel = new JPanel(new BorderLayout());

        // Filter Panel atas laporan
        JPanel filterPanel = new JPanel();
        filterPanel.add(new JLabel("Tanggal Mulai (YYYY-MM-DD):"));
        txtTanggalMulai = new JTextField(10);
        filterPanel.add(txtTanggalMulai);

        filterPanel.add(new JLabel("Tanggal Akhir (YYYY-MM-DD):"));
        txtTanggalAkhir = new JTextField(10);
        filterPanel.add(txtTanggalAkhir);

        filterPanel.add(new JLabel("RT:"));
        cbRT = new JComboBox<>();
        cbRT.addItem("Semua");
        loadRTRW(cbRT, "rt"); // load data RT dari DB
        filterPanel.add(cbRT);

        filterPanel.add(new JLabel("RW:"));
        cbRW = new JComboBox<>();
        cbRW.addItem("Semua");
        loadRTRW(cbRW, "rw"); // load data RW dari DB
        filterPanel.add(cbRW);

        JButton btnFilter = new JButton("Filter");
        btnFilter.addActionListener(e -> refreshLaporanTable());
        filterPanel.add(btnFilter);

        laporanPanel.add(filterPanel, BorderLayout.NORTH);

        // Tabel laporan
        laporanModel = new DefaultTableModel(new String[]{"Nama Warga", "Tanggal", "Organik (kg)", "Anorganik (kg)", "Catatan"}, 0);
        laporanTable = new JTable(laporanModel);
        laporanPanel.add(new JScrollPane(laporanTable), BorderLayout.CENTER);

        tabs.add("Laporan Sampah Warga", laporanPanel);

        add(tabs, BorderLayout.CENTER);
    }

    private void refreshJadwalTable() {
        jadwalModel.setRowCount(0);
        String sql = "SELECT j.id_jadwal, j.tanggal, j.jam, j.id_rt, j.id_rw, "
                + "(SELECT status FROM status_pengangkutan s WHERE s.id_jadwal=j.id_jadwal AND s.id_user_petugas=?) AS sts "
                + "FROM jadwal_pengangkutan j";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                jadwalModel.addRow(new Object[]{
                    rs.getInt("id_jadwal"), rs.getDate("tanggal"),
                    rs.getString("jam"), rs.getInt("id_rt"),
                    rs.getInt("id_rw"), rs.getString("sts")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateStatus(JTable tbl) {
        int row = tbl.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih jadwal.");
            return;
        }
        int idJ = (int) jadwalModel.getValueAt(row, 0);
        String[] opts = {"belum", "sudah"};
        String s = (String) JOptionPane.showInputDialog(this, "Status:", "Update",
                JOptionPane.PLAIN_MESSAGE, null, opts, jadwalModel.getValueAt(row, 5));
        if (s != null) {
            try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO status_pengangkutan (id_jadwal, id_user_petugas, status, catatan, tanggal_input) "
                    + "VALUES (?, ?, ?, NULL, NOW()) "
                    + "ON DUPLICATE KEY UPDATE status = VALUES(status), tanggal_input = NOW()")) {
                ps.setInt(1, idJ);
                ps.setInt(2, userId);
                ps.setString(3, s);
                ps.executeUpdate();
                refreshJadwalTable();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void refreshLaporanTable() {
        laporanModel.setRowCount(0);
        String tanggalMulai = txtTanggalMulai.getText().trim();
        String tanggalAkhir = txtTanggalAkhir.getText().trim();

        String selectedRT = (String) cbRT.getSelectedItem();
        String selectedRW = (String) cbRW.getSelectedItem();

        // Query contoh asumsi tabel laporan_sampah ada kolom nama_warga, tanggal, organik, anorganik, catatan, id_rt, id_rw
        StringBuilder sql = new StringBuilder("SELECT l.nama_warga, l.tanggal, l.organik, l.anorganik, l.catatan "
                + "FROM laporan_sampah l WHERE 1=1 ");

        if (!tanggalMulai.isEmpty()) {
            sql.append(" AND l.tanggal >= TO_DATE(?, 'YYYY-MM-DD') ");
        }
        if (!tanggalAkhir.isEmpty()) {
            sql.append(" AND l.tanggal <= TO_DATE(?, 'YYYY-MM-DD') ");
        }
        if (selectedRT != null && !selectedRT.equals("Semua")) {
            sql.append(" AND l.id_rt = ? ");
        }
        if (selectedRW != null && !selectedRW.equals("Semua")) {
            sql.append(" AND l.id_rw = ? ");
        }

        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {

            int idx = 1;
            if (!tanggalMulai.isEmpty()) {
                ps.setString(idx++, tanggalMulai);
            }
            if (!tanggalAkhir.isEmpty()) {
                ps.setString(idx++, tanggalAkhir);
            }
            if (selectedRT != null && !selectedRT.equals("Semua")) {
                ps.setInt(idx++, Integer.parseInt(selectedRT));
            }
            if (selectedRW != null && !selectedRW.equals("Semua")) {
                ps.setInt(idx++, Integer.parseInt(selectedRW));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                laporanModel.addRow(new Object[]{
                    rs.getString("nama"),
                    rs.getDate("tanggal"),
                    rs.getDouble("organik"),
                    rs.getDouble("anorganik"),
                    rs.getString("catatan")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Method untuk load RT atau RW ke JComboBox dari DB
    private void loadRTRW(JComboBox<String> comboBox, String jenis) {
        String sql = "";
        if ("rt".equalsIgnoreCase(jenis)) {
            sql = "SELECT DISTINCT id_rt FROM rt ORDER BY id_rt";
        } else if ("rw".equalsIgnoreCase(jenis)) {
            sql = "SELECT DISTINCT id_rw FROM rw ORDER BY id_rw";
        }
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                comboBox.addItem(String.valueOf(rs.getInt(1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
