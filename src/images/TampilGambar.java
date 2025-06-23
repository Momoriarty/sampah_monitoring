package images;

import javax.swing.*;
import java.awt.*;

public class TampilGambar extends JFrame {

    public TampilGambar() {
        setTitle("Tampilkan Gambar");
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Buat ImageIcon dari file gambar
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/images/images1.jpg"));

        // Resize agar pas ditampilkan
        Image image = imageIcon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
        imageIcon = new ImageIcon(image);

        // Tampilkan di JLabel
        JLabel label = new JLabel(imageIcon);
        add(label, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TampilGambar().setVisible(true);
        });
    }
}
