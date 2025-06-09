package View;

import Connection.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class WargaPanel extends JPanel {

    private int userId;
    private DefaultTableModel model;

    public WargaPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{"ID", "Tanggal", "Organik", "Anorganik", "Catatan"}, 0);
        JTable table = new JTable(model);
        refreshTable();

        JButton btnAdd = new JButton("Tambah Laporan");
        btnAdd.addActionListener(e -> showAddDialog());

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnAdd, BorderLayout.SOUTH);
    }

    private void refreshTable() {
        model.setRowCount(0);
        String sql = "SELECT * FROM laporan_sampah l JOIN user_app u ON l.id_warga=u.id_warga WHERE u.id_user=?";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
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
    }

    private void showAddDialog() {
        JPanel p = new JPanel(new GridLayout(4, 2));
        JTextField tfOrg = new JTextField(), tfAnOrg = new JTextField();
        JTextField tfCat = new JTextField();

        p.add(new JLabel("Organik (kg):"));
        p.add(tfOrg);
        p.add(new JLabel("Anorganik (kg):"));
        p.add(tfAnOrg);
        p.add(new JLabel("Catatan:"));
        p.add(tfCat);

        int ok = JOptionPane.showConfirmDialog(this, p, "Tambah Laporan", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            float o = Float.parseFloat(tfOrg.getText()), a = Float.parseFloat(tfAnOrg.getText());
            String cat = tfCat.getText();

            String ins = "INSERT INTO laporan_sampah (id_warga, tanggal_lapor, berat_organik, berat_anorganik, catatan) VALUES ("
                    + "(SELECT id_warga FROM user_app WHERE id_user=?), SYSDATE, ?, ?, ?)";
            try (Connection c = DBConnection.getConnection();
                    PreparedStatement ps = c.prepareStatement(ins)) {
                ps.setInt(1, userId);
                ps.setFloat(2, o);
                ps.setFloat(3, a);
                ps.setString(4, cat);
                ps.executeUpdate();
                refreshTable();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
