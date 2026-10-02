package vn.edu.ptit.network.securesocket;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Lớp ClientHandler xử lý từng Client trên luồng riêng (Multithread)
 * Sinh viên: Trần Đức Anh - PTIT
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ServerGUI serverGUI;
    private final String clientAddress;
    private final String connectTime;
    private final String securityInfo;
    private int messageCount = 0;
    private volatile boolean isRunning = true;
    private BufferedReader reader;
    private PrintWriter writer;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    public ClientHandler(Socket socket, ServerGUI serverGUI) {
        this.socket = socket;
        this.serverGUI = serverGUI;
        this.clientAddress = socket.getRemoteSocketAddress().toString().replaceFirst("^/", "");
        this.connectTime = LocalDateTime.now().format(TIME_FORMATTER);
        this.securityInfo = SSLHelper.getSecurityInfo(socket);
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            serverGUI.onClientConnected(this);

            String inputLine;
            // Đọc liên tục các chuỗi gửi từ Client
            while (isRunning && (inputLine = reader.readLine()) != null) {
                // Đề bài yêu cầu: Chuyển chuỗi thành chữ in hoa
                String uppercaseResult = inputLine.toUpperCase();
                
                // Gửi trả kết quả về cho Client
                writer.println(uppercaseResult);
                messageCount++;

                // Báo cho Server GUI hiển thị log
                serverGUI.onMessageProcessed(this, inputLine, uppercaseResult);
            }
        } catch (IOException e) {
            // Client ngắt kết nối
        } finally {
            close();
            serverGUI.onClientDisconnected(this);
        }
    }

    public void close() {
        isRunning = false;
        try {
            if (reader != null) reader.close();
        } catch (Exception ignored) {}
        try {
            if (writer != null) writer.close();
        } catch (Exception ignored) {}
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception ignored) {}
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public String getConnectTime() {
        return connectTime;
    }

    public String getSecurityInfo() {
        return securityInfo;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public Socket getSocket() {
        return socket;
    }
}
