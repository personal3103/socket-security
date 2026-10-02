package vn.edu.ptit.network.securesocket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Giao diện Client Java Swing - Gửi chuỗi lên Server và nhận kết quả in hoa
 * Sinh viên: Trần Đức Anh - PTIT
 */
public class ClientGUI extends JFrame {
    private final String clientTitle;
    private JTextField txtHost;
    private JTextField txtPort;
    private JCheckBox chkUseSSL;
    private JButton btnConnect;
    private JButton btnDisconnect;
    private JLabel lblStatus;

    private JTextField txtInput;
    private JButton btnSend;
    private JTextField txtResult;
    private JButton btnCopyResult;

    private DefaultTableModel historyTableModel;
    private JTable tblHistory;
    private JTextArea txtLog;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private volatile boolean isConnected = false;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ClientGUI() {
        this("Client");
    }

    public ClientGUI(String clientTitle) {
        this.clientTitle = (clientTitle != null && !clientTitle.trim().isEmpty()) ? clientTitle : "Client";
        initUI();
    }

    private void initUI() {
        setTitle("CLIENT TCP BẢO MẬT (" + clientTitle + ") - PTIT");
        setSize(820, 650);
        setMinimumSize(new Dimension(720, 520));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(245, 247, 250));

        // 1. TOP: Cấu hình kết nối
        mainPanel.add(createConnectionPanel(), BorderLayout.NORTH);

        // 2. CENTER: Phần nhập chuỗi & kết quả nhận được
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(createActionPanel(), BorderLayout.NORTH);
        centerPanel.add(createHistoryPanel(), BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. BOTTOM: Thanh trạng thái
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(4, 5, 0, 5));
        JLabel lblHint = new JLabel("💡 Mẹo: Nhập chuỗi rồi nhấn phím Enter để gửi nhanh.");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(new Color(100, 110, 120));
        bottomBar.add(lblHint, BorderLayout.WEST);

        JLabel lblAuthor = new JLabel("Lập trình Mạng PTIT - Client Socket TCP");
        lblAuthor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAuthor.setForeground(new Color(130, 140, 150));
        bottomBar.add(lblAuthor, BorderLayout.EAST);
        mainPanel.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                disconnect();
            }
        });
    }

    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        titlePanel.setOpaque(false);
        JLabel lblHeader = new JLabel("KẾT NỐI CLIENT TCP BẢO MẬT (" + clientTitle + ")");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHeader.setForeground(new Color(21, 67, 96));
        titlePanel.add(lblHeader);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 220), 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        form.setBackground(Color.WHITE);

        form.add(new JLabel("Máy chủ (Host):"));
        txtHost = new JTextField("localhost", 10);
        txtHost.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(txtHost);

        form.add(new JLabel("Cổng (Port):"));
        txtPort = new JTextField("8888", 5);
        txtPort.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(txtPort);

        chkUseSSL = new JCheckBox("Bảo mật SSL/TLS", true);
        chkUseSSL.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chkUseSSL.setForeground(new Color(21, 101, 192));
        form.add(chkUseSSL);

        btnConnect = new JButton("🔗 Kết nối");
        btnConnect.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConnect.setBackground(new Color(21, 101, 192));
        btnConnect.setForeground(Color.WHITE);
        btnConnect.setFocusPainted(false);
        btnConnect.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConnect.addActionListener(e -> connect());
        form.add(btnConnect);

        btnDisconnect = new JButton("❌ Ngắt kết nối");
        btnDisconnect.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDisconnect.setBackground(new Color(198, 40, 40));
        btnDisconnect.setForeground(Color.WHITE);
        btnDisconnect.setFocusPainted(false);
        btnDisconnect.setEnabled(false);
        btnDisconnect.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDisconnect.addActionListener(e -> disconnect());
        form.add(btnDisconnect);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        statusPanel.setOpaque(false);
        lblStatus = new JLabel("⚪ Chưa kết nối tới Server");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(new Color(120, 130, 140));
        statusPanel.add(lblStatus);

        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(statusPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 200), 1),
                " Truyền nhận dữ liệu Client - Server ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13), new Color(40, 55, 71)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Dòng 1: Nhập chuỗi
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel lblIn = new JLabel("Chuỗi gửi lên Server:");
        lblIn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lblIn, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtInput = new JTextField();
        txtInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtInput.setMargin(new Insets(4, 6, 4, 6));
        txtInput.setEnabled(false);
        txtInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && btnSend.isEnabled()) {
                    sendData();
                }
            }
        });
        panel.add(txtInput, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        btnSend = new JButton("📤 Gửi dữ liệu");
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSend.setBackground(new Color(46, 125, 50));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        btnSend.setEnabled(false);
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSend.addActionListener(e -> sendData());
        panel.add(btnSend, gbc);

        // Dòng 2: Kết quả nhận từ server
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel lblOut = new JLabel("Kết quả nhận từ Server:");
        lblOut.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblOut.setForeground(new Color(21, 67, 96));
        panel.add(lblOut, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtResult = new JTextField();
        txtResult.setEditable(false);
        txtResult.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txtResult.setForeground(new Color(11, 83, 69));
        txtResult.setBackground(new Color(234, 250, 241));
        txtResult.setMargin(new Insets(4, 6, 4, 6));
        panel.add(txtResult, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        btnCopyResult = new JButton("📋 Sao chép");
        btnCopyResult.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCopyResult.setEnabled(false);
        btnCopyResult.addActionListener(e -> copyResultToClipboard());
        panel.add(btnCopyResult, gbc);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Tab 1: Bảng lịch sử chuỗi
        JPanel historyTab = new JPanel(new BorderLayout(5, 5));
        historyTab.setOpaque(false);

        String[] headers = {"Thời gian", "Chuỗi gửi lên Server", "Kết quả nhận về (Chữ in hoa)", "Mã hóa"};
        historyTableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tblHistory = new JTable(historyTableModel);
        tblHistory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblHistory.setRowHeight(24);
        tblHistory.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(JLabel.CENTER);
        tblHistory.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblHistory.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        tblHistory.getColumnModel().getColumn(1).setPreferredWidth(230);
        tblHistory.getColumnModel().getColumn(2).setPreferredWidth(230);
        tblHistory.getColumnModel().getColumn(3).setPreferredWidth(130);
        tblHistory.getColumnModel().getColumn(3).setCellRenderer(centerRender);

        JScrollPane scrollHistory = new JScrollPane(tblHistory);
        historyTab.add(scrollHistory, BorderLayout.CENTER);

        JPanel historyBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        historyBar.setOpaque(false);
        JButton btnClearHistory = new JButton("Xóa lịch sử");
        btnClearHistory.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClearHistory.addActionListener(e -> historyTableModel.setRowCount(0));
        historyBar.add(btnClearHistory);
        historyTab.add(historyBar, BorderLayout.SOUTH);

        tabbedPane.addTab("📊 Lịch sử gửi/nhận", historyTab);

        // Tab 2: Nhật ký Log chi tiết
        JPanel logTab = new JPanel(new BorderLayout(5, 5));
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtLog.setBackground(new Color(30, 30, 30));
        txtLog.setForeground(new Color(220, 220, 220));
        txtLog.setMargin(new Insets(6, 6, 6, 6));

        JScrollPane scrollLog = new JScrollPane(txtLog);
        logTab.add(scrollLog, BorderLayout.CENTER);
        tabbedPane.addTab("📜 Nhật ký kết nối", logTab);

        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private void connect() {
        String host = txtHost.getText().trim();
        int port;
        try {
            port = Integer.parseInt(txtPort.getText().trim());
            if (port < 1024 || port > 65535) {
                JOptionPane.showMessageDialog(this, "Số cổng phải từ 1024 - 65535!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cổng phải là số nguyên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean useSSL = chkUseSSL.isSelected();
        btnConnect.setEnabled(false);
        txtHost.setEnabled(false);
        txtPort.setEnabled(false);
        chkUseSSL.setEnabled(false);
        lblStatus.setText("🟡 Đang kết nối tới " + host + ":" + port + "...");
        lblStatus.setForeground(new Color(212, 143, 0));

        new Thread(() -> {
            try {
                if (useSSL) {
                    log("Bắt tay bảo mật SSL/TLS với " + host + ":" + port + "...");
                    socket = SSLHelper.createSSLSocket(host, port);
                } else {
                    log("Kết nối TCP thường tới " + host + ":" + port + "...");
                    socket = new Socket(host, port);
                }

                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

                isConnected = true;
                String secInfo = SSLHelper.getSecurityInfo(socket);

                SwingUtilities.invokeLater(() -> {
                    btnDisconnect.setEnabled(true);
                    txtInput.setEnabled(true);
                    btnSend.setEnabled(true);
                    txtInput.requestFocus();

                    if (useSSL) {
                        lblStatus.setText("🔒 ĐÃ KẾT NỐI BẢO MẬT: " + host + ":" + port + " [" + secInfo + "]");
                        lblStatus.setForeground(new Color(46, 125, 50));
                    } else {
                        lblStatus.setText("🔓 ĐÃ KẾT NỐI (TCP thường): " + host + ":" + port);
                        lblStatus.setForeground(new Color(230, 126, 34));
                    }
                });

                log("Kết nối thành công tới Server.");
                log("Mã hóa: " + secInfo);
            } catch (Exception ex) {
                log("Lỗi kết nối: " + ex.getMessage());
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ClientGUI.this, 
                            "Không thể kết nối tới Server: " + ex.getMessage(), 
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    resetUIOnDisconnect();
                });
            }
        }).start();
    }

    private synchronized void disconnect() {
        if (!isConnected) return;
        isConnected = false;

        try { if (reader != null) reader.close(); } catch (Exception ignored) {}
        try { if (writer != null) writer.close(); } catch (Exception ignored) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (Exception ignored) {}

        log("Đã ngắt kết nối khỏi Server.");
        resetUIOnDisconnect();
    }

    private void resetUIOnDisconnect() {
        isConnected = false;
        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);
        txtHost.setEnabled(true);
        txtPort.setEnabled(true);
        chkUseSSL.setEnabled(true);
        txtInput.setEnabled(false);
        btnSend.setEnabled(false);
        lblStatus.setText("⚪ Chưa kết nối tới Server");
        lblStatus.setForeground(new Color(120, 130, 140));
    }

    private void sendData() {
        String textToSend = txtInput.getText();
        if (textToSend == null || textToSend.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập chuỗi cần gửi!", "Nhắc nhở", JOptionPane.INFORMATION_MESSAGE);
            txtInput.requestFocus();
            return;
        }

        btnSend.setEnabled(false);
        txtInput.setEnabled(false);

        new Thread(() -> {
            try {
                log("==> Gửi chuỗi: \"" + textToSend + "\"");
                writer.println(textToSend);

                String response = reader.readLine();

                if (response != null) {
                    log("<== Kết quả nhận: \"" + response + "\"");
                    String time = LocalTime.now().format(TIME_FMT);
                    String secMode = chkUseSSL.isSelected() ? "SSL/TLS" : "TCP Plain";

                    SwingUtilities.invokeLater(() -> {
                        txtResult.setText(response);
                        btnCopyResult.setEnabled(true);

                        historyTableModel.insertRow(0, new Object[]{
                                time, textToSend, response, secMode
                        });

                        txtInput.setText("");
                        txtInput.setEnabled(true);
                        btnSend.setEnabled(true);
                        txtInput.requestFocus();
                    });
                } else {
                    log("Server đã đóng kết nối.");
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(ClientGUI.this, 
                                "Server đã ngắt kết nối!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                        disconnect();
                    });
                }
            } catch (IOException ex) {
                log("Lỗi gửi/nhận: " + ex.getMessage());
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ClientGUI.this, 
                            "Lỗi giao tiếp với Server: " + ex.getMessage(), 
                            "Lỗi mạng", JOptionPane.ERROR_MESSAGE);
                    disconnect();
                });
            }
        }).start();
    }

    private void copyResultToClipboard() {
        String result = txtResult.getText();
        if (!result.isEmpty()) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(result), null);
            JOptionPane.showMessageDialog(this, "Đã sao chép kết quả vào bộ nhớ tạm!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void log(String message) {
        String time = LocalTime.now().format(TIME_FMT);
        SwingUtilities.invokeLater(() -> {
            txtLog.append("[" + time + "] " + message + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        String title = (args != null && args.length > 0) ? args[0] : "Client";
        SwingUtilities.invokeLater(() -> {
            ClientGUI clientGUI = new ClientGUI(title);
            clientGUI.setVisible(true);
        });
    }
}
