package View;

// AdminPanel.java
import Connection.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AdminPanel extends JPanel {

    private DefaultTableModel modelRW, modelRT, modelWarga;

    public AdminPanel() {
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("RW", panelRW());
        tabs.addTab("RT", panelRT());
        tabs.addTab("Warga", panelWarga());

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
            try (Connection c = DBConnection.getConnection();
                    PreparedStatement ps = c.prepareStatement("INSERT INTO rw (id_rw, nama_rw) VALUES (seq_rw.nextval, ?)")) {
                ps.setString(1, nama);
                ps.executeUpdate();
                refreshRW();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        p.add(new JScrollPane(tbl), BorderLayout.CENTER);
        p.add(btnAdd, BorderLayout.SOUTH);
        return p;
    }

    private void refreshRW() {
        modelRW.setRowCount(0);
        try (Connection c = DBConnection.getConnection();
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT * FROM rw")) {
            while (rs.next()) {
                modelRW.addRow(new Object[]{rs.getInt("id_rw"), rs.getString("nama_rw")});
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
        btnAdd.addActionListener(e -> {
            String rw = JOptionPane.showInputDialog(this, "ID RW:");
            String nama = JOptionPane.showInputDialog(this, "Nama RT:");
            try (Connection c = DBConnection.getConnection();
                    PreparedStatement ps = c.prepareStatement("INSERT INTO rt (id_rt,id_rw,nama_rt) VALUES (seq_rt.nextval, ?, ?)")) {
                ps.setInt(1, Integer.parseInt(rw));
                ps.setString(2, nama);
                ps.executeUpdate();
                refreshRT();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        p.add(new JScrollPane(tbl), BorderLayout.CENTER);
        p.add(btnAdd, BorderLayout.SOUTH);
        return p;
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
                            "INSERT INTO warga (id_warga,nama,alamat,id_rt,id_rw,no_hp) VALUES (seq_warga.nextval,?,?,?,?,?)")) {
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
}
