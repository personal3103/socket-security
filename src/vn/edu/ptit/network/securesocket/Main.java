package vn.edu.ptit.network.securesocket;

import javax.swing.*;
import java.awt.*;

/**
 * Lớp điều phối kịch bản kiểm thử (Launcher)
 * Tự động mở Server và 2 Client song song trên màn hình để tiện chụp ảnh báo cáo
 * Sinh viên: Trần Đức Anh - PTIT
 */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int screenWidth = screenSize.width;
            int screenHeight = screenSize.height;

            // 1. Mở Server bên trái màn hình
            ServerGUI serverGUI = new ServerGUI();
            int serverWidth = (int) (screenWidth * 0.52);
            int serverHeight = (int) (screenHeight * 0.88);
            serverGUI.setBounds(20, 40, serverWidth, serverHeight);
            serverGUI.setVisible(true);

            // 2. Mở Client 1 bên phải phía trên
            ClientGUI client1 = new ClientGUI("Client 1 - Trần Đức Anh");
            int clientWidth = (int) (screenWidth * 0.44);
            int clientHeight = (int) (screenHeight * 0.43);
            int clientX = serverWidth + 40;
            client1.setBounds(clientX, 40, clientWidth, clientHeight);
            client1.setVisible(true);

            // 3. Mở Client 2 bên phải phía dưới
            ClientGUI client2 = new ClientGUI("Client 2 - Đa luồng");
            client2.setBounds(clientX, 40 + clientHeight + 15, clientWidth, clientHeight);
            client2.setVisible(true);
        });
    }
}
