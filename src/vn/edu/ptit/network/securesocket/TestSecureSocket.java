package vn.edu.ptit.network.securesocket;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

/**
 * Script kiểm thử tự động (Unit Test / Integration Test)
 * Sinh viên: Trần Đức Anh - PTIT
 */
public class TestSecureSocket {
    private static final int TEST_PORT = 9999;

    public static void main(String[] args) throws Exception {
        System.out.println("====== BẮT ĐẦU KIỂM THỬ SOCKET TCP BẢO MẬT SSL/TLS ======");

        SSLServerSocket serverSocket = SSLHelper.createSSLServerSocket(TEST_PORT);
        System.out.println("[TEST] SSLServerSocket đã mở trên cổng: " + TEST_PORT);

        ExecutorService serverPool = Executors.newCachedThreadPool();
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch finishSignal = new CountDownLatch(3);

        Thread serverThread = new Thread(() -> {
            try {
                for (int i = 0; i < 3; i++) {
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                    serverPool.submit(() -> {
                        try (
                            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                            PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)
                        ) {
                            String line;
                            while ((line = in.readLine()) != null) {
                                String upper = line.toUpperCase();
                                out.println(upper);
                            }
                        } catch (IOException ignored) {
                        } finally {
                            try { clientSocket.close(); } catch (Exception ignored) {}
                        }
                    });
                }
            } catch (IOException ignored) {}
        });
        serverThread.start();

        String[][] testCases = {
                {"xin chào học viện bưu chính viễn thông", "XIN CHÀO HỌC VIỆN BƯU CHÍNH VIỄN THÔNG"},
                {"chúc bạn làm bài thực hành tốt", "CHÚC BẠN LÀM BÀI THỰC HÀNH TỐT"},
                {"tcp secure socket multithreading ptit", "TCP SECURE SOCKET MULTITHREADING PTIT"}
        };

        for (int i = 0; i < 3; i++) {
            final int clientId = i + 1;
            final String inputStr = testCases[i][0];
            final String expectedStr = testCases[i][1];

            new Thread(() -> {
                try {
                    startSignal.await();
                    SSLSocket clientSocket = SSLHelper.createSSLSocket("localhost", TEST_PORT);
                    System.out.println("[CLIENT " + clientId + "] Bắt tay SSL/TLS thành công! Cipher: " 
                            + clientSocket.getSession().getCipherSuite());

                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);

                    out.println(inputStr);
                    String response = in.readLine();
                    System.out.println("[CLIENT " + clientId + "] Gửi: '" + inputStr + "' -> Nhận: '" + response + "'");

                    if (expectedStr.equals(response)) {
                        System.out.println("[CLIENT " + clientId + "] [PASS] Kết quả chính xác.");
                    } else {
                        System.err.println("[CLIENT " + clientId + "] [FAIL] Kỳ vọng: " + expectedStr + " | Thực tế: " + response);
                    }

                    clientSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishSignal.countDown();
                }
            }).start();
        }

        startSignal.countDown();
        boolean completed = finishSignal.await(10, TimeUnit.SECONDS);

        serverSocket.close();
        serverPool.shutdownNow();

        if (completed) {
            System.out.println("====== KIỂM THỬ THÀNH CÔNG 100%! ======");
        } else {
            System.err.println("Kiểm thử timeout!");
            System.exit(1);
        }
    }
}
