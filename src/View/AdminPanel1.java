package View;

import Connection.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AdminPanel1 extends JPanel {

    private DefaultTableModel modelRW, modelRT, modelWarga, modelJadwal;
    private JComboBox<String> cbRW, cbRT;

    public AdminPanel1() {
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("RW", panelRW());
        tabs.addTab("RT", panelRT());
        tabs.addTab("Warga", panelWarga());
        tabs.addTab("Jadwal Pengangkutan", panelJadwal());

        add(tabs, BorderLayout.CENTER);
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
                try (Connection c = DBConnection.getConnection();
                     PreparedStatement ps = c.prepareStatement("INSERT INTO rw (nama_rw) VALUES (?)")) {
                    ps.setString(1, nama);
                    ps.executeUpdate();
                    refreshRW();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
        p.add(new JScrollPane(tbl), BorderLayout.CENTER);
        p.add(btnAdd, BorderLayout.SOUTH);
        return p;
    }

    private void refreshRW() {
        modelRW.setRowCount(0);
        if (cbRW != null) {
            cbRW.removeAllItems();
        }
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM rw")) {
            while (rs.next()) {
                modelRW.addRow(new Object[]{rs.getInt("id_rw"), rs.getString("nama_rw")});
                if (cbRW != null) {
                    cbRW.addItem(rs.getInt("id_rw") + " - " + rs.getString("nama_rw"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private JPanel panelRT() {
        JPanel p = new JPanel(new BorderLayout());
        modelRT = new DefaultTableModel(new String[]{"ID RT", "ID RW", "Nama RT"}, 0);
        JTable tbl = new JTable(modelRT);
        refreshRT();
        JButton btnAdd = new JButton("Tambah RT");
        btnAdd.addActionListener(e -> showAddRT());
        p.add(new JScrollPane(tbl), BorderLayout.CENTER);
        p.add(btnAdd, BorderLayout.SOUTH);
        return p;
    }

    private void showAddRT() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        cbRW = new JComboBox<>();
        refreshRW();
        JTextField namaRT = new JTextField();

        panel.add(new JLabel("Pilih RW:"));
        panel.add(cbRW);
        panel.add(new JLabel("Nama RT:"));
        panel.add(namaRT);

        if (JOptionPane.showConfirmDialog(this, panel, "Tambah RT", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                String selected = (String) cbRW.getSelectedItem();
                int idRW = Integer.parseInt(selected.split(" - ")[0]);
                String nama = namaRT.getText().trim();
                if (!nama.isEmpty()) {
                    try (Connection c = DBConnection.getConnection();
                         PreparedStatement ps = c.prepareStatement("INSERT INTO rt (id_rw, nama_rt) VALUES (?, ?)")) {
                        ps.setInt(1, idRW);
                        ps.setString(2, nama);
                        ps.executeUpdate();
                        refreshRT();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void refreshRT() {
        modelRT.setRowCount(0);
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM rt")) {
            while (rs.next()) {
                modelRT.addRow(new Object[]{
                        rs.getInt("id_rt"), rs.getInt("id_rw"), rs.getString("nama_rt")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private JPanel panelWarga() {
        JPanel p = new JPanel(new BorderLayout());
        modelWarga = new DefaultTableModel(new String[]{"ID", "Nama", "Alamat", "RT", "RW", "HP"}, 0);
        JTable tbl = new JTable(modelWarga);
        refreshWarga();
        JButton btnAdd = new JButton("Tambah Warga");
        btnAdd.addActionListener(e -> showAddWarga());
        p.add(new JScrollPane(tbl), BorderLayout.CENTER);
        p.add(btnAdd, BorderLayout.SOUTH);
        return p;
    }

    private void refreshWarga() {
        modelWarga.setRowCount(0);
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM warga")) {
            while (rs.next()) {
                modelWarga.addRow(new Object[]{
                        rs.getInt("id_warga"), rs.getString("nama"),
                        rs.getString("alamat"), rs.getInt("id_rt"),
                        rs.getInt("id_rw"), rs.getString("no_hp")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showAddWarga() {
        JPanel f = new JPanel(new GridLayout(5, 2));
        JTextField tn = new JTextField(), ta = new JTextField();
        JTextField trt = new JTextField(), trw = new JTextField();
        JTextField tph = new JTextField();
        f.add(new JLabel("Nama:"));
        f.add(tn);
        f.add(new JLabel("Alamat:"));
        f.add(ta);
        f.add(new JLabel("ID RT:"));
        f.add(trt);
        f.add(new JLabel("ID RW:"));
        f.add(trw);
        f.add(new JLabel("HP:"));
        f.add(tph);

        if (JOptionPane.showConfirmDialog(this, f, "Tambah Warga", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "INSERT INTO warga (nama, alamat, id_rt, id_rw, no_hp) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, tn.getText());
                ps.setString(2, ta.getText());
                ps.setInt(3, Integer.parseInt(trt.getText()));
                ps.setInt(4, Integer.parseInt(trw.getText()));
                ps.setString(5, tph.getText());
                ps.executeUpdate();
                refreshWarga();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // --- Tambahan: Panel Jadwal Pengangkutan ---

    private JPanel panelJadwal() {
        JPanel p = new JPanel(new BorderLayout());
        modelJadwal = new DefaultTableModel(new String[]{"ID Jadwal", "Tanggal", "Jam", "ID RT", "ID RW"}, 0);
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
            int idJadwal = (int) modelJadwal.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Hapus jadwal dengan ID " + idJadwal + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection c = DBConnection.getConnection();
                     PreparedStatement ps = c.prepareStatement("DELETE FROM jadwal_pengangkutan WHERE id_jadwal=?")) {
                    ps.setInt(1, idJadwal);
                    ps.executeUpdate();
                    refreshJadwal();
                } catch (SQLException ex) {
                    ex.printStackTrace();
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
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM jadwal_pengangkutan")) {
            while (rs.next()) {
                modelJadwal.addRow(new Object[]{
                        rs.getInt("id_jadwal"),
                        rs.getDate("tanggal"),
                        rs.getString("jam"),
                        rs.getInt("id_rt"),
                        rs.getInt("id_rw")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showAddJadwal() {
        JPanel panel = new JPanel(new GridLayout(4, 2));

        JTextField tfTanggal = new JTextField("YYYY-MM-DD");
        JTextField tfJam = new JTextField("HH:mm");
        cbRW = new JComboBox<>();
        cbRT = new JComboBox<>();

        refreshRW();
        panel.add(new JLabel("Tanggal (YYYY-MM-DD):"));
        panel.add(tfTanggal);
        panel.add(new JLabel("Jam (HH:mm):"));
        panel.add(tfJam);
        panel.add(new JLabel("Pilih RW:"));
        panel.add(cbRW);
        panel.add(new JLabel("Pilih RT:"));
        panel.add(cbRT);

        // update cbRT saat cbRW berubah
        cbRW.addActionListener(e -> {
            cbRT.removeAllItems();
            if (cbRW.getSelectedItem() != null) {
                try {
                    int idRW = Integer.parseInt(cbRW.getSelectedItem().toString().split(" - ")[0]);
                    try (Connection c = DBConnection.getConnection();
                         PreparedStatement ps = c.prepareStatement("SELECT id_rt, nama_rt FROM rt WHERE id_rw=?")) {
                        ps.setInt(1, idRW);
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            cbRT.addItem(rs.getInt("id_rt") + " - " + rs.getString("nama_rt"));
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        int result = JOptionPane.showConfirmDialog(this, panel, "Tambah Jadwal Pengangkutan", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String tanggal = tfTanggal.getText().trim();
                String jam = tfJam.getText().trim();
                int idRT = Integer.parseInt(cbRT.getSelectedItem().toString().split(" - ")[0]);
                int idRW = Integer.parseInt(cbRW.getSelectedItem().toString().split(" - ")[0]);

                try (Connection c = DBConnection.getConnection();
                     PreparedStatement ps = c.prepareStatement(
                             "INSERT INTO jadwal_pengangkutan (tanggal, jam, id_rt, id_rw) VALUES (?, ?, ?, ?)")) {
                    ps.setDate(1, Date.valueOf(tanggal));
                    ps.setString(2, jam);
                    ps.setInt(3, idRT);
                    ps.setInt(4, idRW);
                    ps.executeUpdate();
                    refreshJadwal();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Input data tidak valid!");
            }
        }
    }
}
