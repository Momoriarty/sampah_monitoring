package View;

// PetugasPanel.java
import Connection.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class PetugasPanel extends JPanel {

    private DefaultTableModel model;
    private int userId;

    public PetugasPanel() {
        setLayout(new BorderLayout());
        model = new DefaultTableModel(new String[]{"ID Jadwal", "Tanggal", "Jam", "RT", "RW", "Status"}, 0);
        JTable tbl = new JTable(model);
        refreshTable();

        JButton btnUpdate = new JButton("Update Status");
        btnUpdate.addActionListener(e -> updateStatus(tbl));

        add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(btnUpdate, BorderLayout.SOUTH);
    }

    private void refreshTable() {
        model.setRowCount(0);
        String sql = "SELECT j.id_jadwal, j.tanggal, j.jam, j.id_rt, j.id_rw, "
                + "(SELECT status FROM status_pengangkutan s WHERE s.id_jadwal=j.id_jadwal AND s.id_user_petugas=?) AS sts "
                + "FROM jadwal_pengangkutan j";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
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
        int idJ = (int) model.getValueAt(row, 0);
        String[] opts = {"belum", "sudah"};
        String s = (String) JOptionPane.showInputDialog(this, "Status:", "Update",
                JOptionPane.PLAIN_MESSAGE, null, opts, model.getValueAt(row, 5));
        if (s != null) {
            try (Connection c = DBConnection.getConnection();
                    PreparedStatement ps = c.prepareStatement(
                            "MERGE INTO status_pengangkutan s USING dual ON (s.id_jadwal=? AND s.id_user_petugas=?) "
                            + "WHEN MATCHED THEN UPDATE SET status=? "
                            + "WHEN NOT MATCHED THEN INSERT (id_status,id_jadwal,id_user_petugas,status,catatan,tanggal_input) "
                            + "VALUES (seq_status.nextval,?,?,?,NULL,SYSTIMESTAMP)")) {
                ps.setInt(1, idJ);
                ps.setInt(2, userId);
                ps.setString(3, s);
                ps.setInt(4, idJ);
                ps.setInt(5, userId);
                ps.setString(6, s);
                ps.executeUpdate();
                refreshTable();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
