package vn.edu.ptit.network.securesocket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Giao diện Server Java Swing - TCP Bảo mật Socket (SSL/TLS) Đa luồng
 * Sinh viên: Trần Đức Anh - PTIT
 */
public class ServerGUI extends JFrame {
    private JTextField txtPort;
    private JCheckBox chkUseSSL;
    private JButton btnStart;
    private JButton btnStop;
    private JLabel lblStatusBadge;
    private JLabel lblStats;
    private JTextArea txtLog;
    private JTable tblClients;
    private DefaultTableModel clientTableModel;

    private ServerSocket serverSocket;
    private ExecutorService clientThreadPool;
    private final List<ClientHandler> activeClients = Collections.synchronizedList(new ArrayList<>());
    
    private volatile boolean isRunning = false;
    private int totalProcessedRequests = 0;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ServerGUI() {
        initUI();
    }

    private void initUI() {
        setTitle("SERVER TCP BẢO MẬT (SSL/TLS - MULTITHREAD) - PTIT");
        setSize(920, 650);
        setMinimumSize(new Dimension(800, 520));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(245, 247, 250));

        // 1. TOP PANEL
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 2. CENTER PANEL
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.42);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(6);

        splitPane.setTopComponent(createClientListPanel());
        splitPane.setBottomComponent(createLogPanel());

        mainPanel.add(splitPane, BorderLayout.CENTER);

        // 3. BOTTOM PANEL
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
        log("Hệ thống Server sẵn sàng. Nhấn 'Khởi động Server' để lắng nghe kết nối.");
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("HỆ THỐNG MÁY CHỦ TCP - MULTITHREAD SOCKET");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(new Color(21, 67, 96));
        titlePanel.add(lblTitle);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controls.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 220), 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        controls.setBackground(Color.WHITE);

        controls.add(new JLabel("Cổng (Port):"));
        txtPort = new JTextField("8888", 6);
        txtPort.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        controls.add(txtPort);

        chkUseSSL = new JCheckBox("Bảo mật SSL/TLS", true);
        chkUseSSL.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chkUseSSL.setForeground(new Color(21, 101, 192));
        controls.add(chkUseSSL);

        btnStart = new JButton("▶ Khởi động Server");
        btnStart.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnStart.setBackground(new Color(46, 125, 50));
        btnStart.setForeground(Color.WHITE);
        btnStart.setFocusPainted(false);
        btnStart.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnStart.addActionListener(e -> startServer());
        controls.add(btnStart);

        btnStop = new JButton("⏹ Dừng Server");
        btnStop.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnStop.setBackground(new Color(198, 40, 40));
        btnStop.setForeground(Color.WHITE);
        btnStop.setFocusPainted(false);
        btnStop.setEnabled(false);
        btnStop.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnStop.addActionListener(e -> stopServer());
        controls.add(btnStop);

        lblStatusBadge = new JLabel("● ĐÃ DỪNG");
        lblStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatusBadge.setForeground(new Color(180, 40, 40));
        lblStatusBadge.setBorder(new EmptyBorder(0, 15, 0, 5));
        controls.add(lblStatusBadge);

        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(controls, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createClientListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 200), 1),
                " Danh sách Client đang kết nối (Đa luồng - Multithread) ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13), new Color(40, 55, 71)
        ));

        String[] columns = {"STT", "Địa chỉ Client (IP:Port)", "Thời gian kết nối", "Số tin xử lý", "Giao thức / Mã hóa"};
        clientTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tblClients = new JTable(clientTableModel);
        tblClients.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblClients.setRowHeight(24);
        tblClients.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(JLabel.CENTER);
        tblClients.getColumnModel().getColumn(0).setPreferredWidth(45);
        tblClients.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        tblClients.getColumnModel().getColumn(1).setPreferredWidth(170);
        tblClients.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblClients.getColumnModel().getColumn(3).setPreferredWidth(90);
        tblClients.getColumnModel().getColumn(3).setCellRenderer(centerRender);
        tblClients.getColumnModel().getColumn(4).setPreferredWidth(260);

        JScrollPane scrollPane = new JScrollPane(tblClients);
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 200), 1),
                " Nhật ký xử lý Server (Activity Log) ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13), new Color(40, 55, 71)
        ));

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtLog.setBackground(new Color(30, 30, 30));
        txtLog.setForeground(new Color(230, 230, 230));
        txtLog.setMargin(new Insets(6, 6, 6, 6));

        JScrollPane scrollPane = new JScrollPane(txtLog);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel logActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        logActions.setOpaque(false);
        JButton btnClearLog = new JButton("Xóa nhật ký");
        btnClearLog.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClearLog.addActionListener(e -> txtLog.setText(""));
        logActions.add(btnClearLog);
        panel.add(logActions, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(4, 5, 0, 5));

        lblStats = new JLabel("Client online: 0 | Tổng số chuỗi đã xử lý: 0 | Chế độ: Chờ khởi động");
        lblStats.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStats.setForeground(new Color(80, 90, 100));
        panel.add(lblStats, BorderLayout.WEST);

        JLabel lblCopyright = new JLabel("Lập trình Mạng PTIT - Socket TCP Bảo mật (SSL/TLS)");
        lblCopyright.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblCopyright.setForeground(new Color(120, 130, 140));
        panel.add(lblCopyright, BorderLayout.EAST);

        return panel;
    }

    private synchronized void startServer() {
        int port;
        try {
            port = Integer.parseInt(txtPort.getText().trim());
            if (port < 1024 || port > 65535) {
                JOptionPane.showMessageDialog(this, "Số cổng phải từ 1024 đến 65535!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cổng phải là số nguyên dương!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean useSSL = chkUseSSL.isSelected();
        btnStart.setEnabled(false);
        txtPort.setEnabled(false);
        chkUseSSL.setEnabled(false);

        new Thread(() -> {
            try {
                if (useSSL) {
                    log("[BẢO MẬT] Đang tạo SSLServerSocket với chứng chỉ TLS...");
                    serverSocket = SSLHelper.createSSLServerSocket(port);
                    log("[BẢO MẬT] SSLServerSocket kích hoạt mã hóa TLSv1.3 & TLSv1.2.");
                } else {
                    serverSocket = new ServerSocket(port);
                    log("[CẢNH BÁO] Chạy chế độ TCP thường (Không mã hóa SSL).");
                }

                isRunning = true;
                clientThreadPool = Executors.newCachedThreadPool();

                SwingUtilities.invokeLater(() -> {
                    btnStop.setEnabled(true);
                    lblStatusBadge.setText("● ĐANG CHẠY (PORT " + port + " - " + (useSSL ? "SSL/TLS" : "PLAIN") + ")");
                    lblStatusBadge.setForeground(new Color(46, 125, 50));
                    updateStats();
                });

                log("==> Server TCP đã lắng nghe thành công trên cổng: " + port);

                while (isRunning && !serverSocket.isClosed()) {
                    try {
                        Socket socket = serverSocket.accept();
                        ClientHandler handler = new ClientHandler(socket, this);
                        activeClients.add(handler);
                        clientThreadPool.submit(handler);
                    } catch (IOException e) {
                        if (!isRunning) break;
                    }
                }
            } catch (Exception ex) {
                log("[LỖI KHỞI ĐỘNG] " + ex.getMessage());
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ServerGUI.this, 
                            "Không thể khởi động Server: " + ex.getMessage(), 
                            "Lỗi Server", JOptionPane.ERROR_MESSAGE);
                    resetUIOnStop();
                });
            }
        }).start();
    }

    private synchronized void stopServer() {
        isRunning = false;
        log("Đang dừng Server và ngắt tất cả kết nối Client...");

        synchronized (activeClients) {
            for (ClientHandler handler : activeClients) {
                handler.close();
            }
            activeClients.clear();
        }

        if (serverSocket != null && !serverSocket.isClosed()) {
            try { serverSocket.close(); } catch (IOException ignored) {}
        }

        if (clientThreadPool != null && !clientThreadPool.isShutdown()) {
            clientThreadPool.shutdownNow();
        }

        resetUIOnStop();
        log("Server đã dừng hoàn toàn.");
    }

    private void resetUIOnStop() {
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        txtPort.setEnabled(true);
        chkUseSSL.setEnabled(true);
        lblStatusBadge.setText("● ĐÃ DỪNG");
        lblStatusBadge.setForeground(new Color(180, 40, 40));
        refreshClientTable();
        updateStats();
    }

    public void onClientConnected(ClientHandler handler) {
        SwingUtilities.invokeLater(() -> {
            log("[KẾT NỐI] Client [" + handler.getClientAddress() + "] đã kết nối.");
            log("          Bảo mật: " + handler.getSecurityInfo());
            refreshClientTable();
            updateStats();
        });
    }

    public void onMessageProcessed(ClientHandler handler, String original, String uppercase) {
        SwingUtilities.invokeLater(() -> {
            totalProcessedRequests++;
            log("[XỬ LÝ - " + handler.getClientAddress() + "]");
            log("  ├─ Nhận: \"" + original + "\"");
            log("  └─ Chuyển in hoa & gửi trả: \"" + uppercase + "\"");
            refreshClientTable();
            updateStats();
        });
    }

    public void onClientDisconnected(ClientHandler handler) {
        activeClients.remove(handler);
        SwingUtilities.invokeLater(() -> {
            log("[NGẮT KẾT NỐI] Client [" + handler.getClientAddress() + "] đã ngắt kết nối.");
            refreshClientTable();
            updateStats();
        });
    }

    private void refreshClientTable() {
        clientTableModel.setRowCount(0);
        synchronized (activeClients) {
            int stt = 1;
            for (ClientHandler h : activeClients) {
                clientTableModel.addRow(new Object[]{
                        stt++,
                        h.getClientAddress(),
                        h.getConnectTime(),
                        h.getMessageCount(),
                        h.getSecurityInfo()
                });
            }
        }
    }

    private void updateStats() {
        String mode = isRunning ? (chkUseSSL.isSelected() ? "SSL/TLS (Bảo mật)" : "TCP Tiêu chuẩn") : "Đang dừng";
        lblStats.setText(String.format("Client online: %d | Tổng số chuỗi đã xử lý: %d | Chế độ: %s",
                activeClients.size(), totalProcessedRequests, mode));
    }

    public void log(String message) {
        String timestamp = LocalTime.now().format(TIME_FMT);
        SwingUtilities.invokeLater(() -> {
            txtLog.append("[" + timestamp + "] " + message + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            ServerGUI serverGUI = new ServerGUI();
            serverGUI.setVisible(true);
        });
    }
}
