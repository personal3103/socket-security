package vn.edu.ptit.network.securesocket;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * Lớp tiện ích cấu hình Bảo mật Socket (SSL/TLS Socket - TCP)
 * Sinh viên: Trần Đức Anh - PTIT
 */
public class SSLHelper {
    public static final String KEYSTORE_FILE = "server.keystore";
    public static final String KEYSTORE_PASS = "password123";
    public static final String KEY_ALIAS = "securesocket";

    /**
     * Tìm kiếm và nạp InputStream cho file Keystore từ nhiều nguồn khác nhau
     */
    private static InputStream findKeystoreStream() throws IOException {
        // 1. Thử từ thư mục hiện tại
        File f1 = new File(KEYSTORE_FILE);
        if (f1.exists()) return new FileInputStream(f1);

        // 2. Thử trong thư mục src
        File f2 = new File("src", KEYSTORE_FILE);
        if (f2.exists()) return new FileInputStream(f2);

        // 3. Thử trong package
        File f3 = new File("src/vn/edu/ptit/network/securesocket", KEYSTORE_FILE);
        if (f3.exists()) return new FileInputStream(f3);

        // 4. Thử tìm từ classpath
        InputStream is = SSLHelper.class.getResourceAsStream("/" + KEYSTORE_FILE);
        if (is != null) return is;

        is = SSLHelper.class.getResourceAsStream(KEYSTORE_FILE);
        if (is != null) return is;

        // Nếu không thấy, tự động gọi keytool sinh mới
        ensureKeyStoreExists();
        if (f1.exists()) return new FileInputStream(f1);

        throw new FileNotFoundException("Không tìm thấy file keystore: " + KEYSTORE_FILE);
    }

    /**
     * Tự động khởi tạo keystore nếu chưa có
     */
    public static void ensureKeyStoreExists() {
        File ksFile = new File(KEYSTORE_FILE);
        if (!ksFile.exists()) {
            try {
                System.out.println("[SSL] Đang tự động tạo keystore: " + KEYSTORE_FILE);
                ProcessBuilder pb = new ProcessBuilder(
                        "keytool", "-genkeypair",
                        "-alias", KEY_ALIAS,
                        "-keyalg", "RSA",
                        "-keysize", "2048",
                        "-storetype", "PKCS12",
                        "-keystore", KEYSTORE_FILE,
                        "-validity", "3650",
                        "-storepass", KEYSTORE_PASS,
                        "-keypass", KEYSTORE_PASS,
                        "-dname", "CN=localhost, OU=PTIT, O=Network, L=Hanoi, ST=Hanoi, C=VN"
                );
                Process process = pb.start();
                process.waitFor();
            } catch (Exception e) {
                System.err.println("[SSL] Lỗi tạo keystore: " + e.getMessage());
            }
        }
    }

    /**
     * Tạo SSLServerSocket cho Server
     */
    public static SSLServerSocket createSSLServerSocket(int port) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (InputStream is = findKeystoreStream()) {
            ks.load(is, KEYSTORE_PASS.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, KEYSTORE_PASS.toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());

        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(port);
        serverSocket.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});
        return serverSocket;
    }

    /**
     * Tạo SSLSocket cho Client kết nối an toàn tới Server
     */
    public static SSLSocket createSSLSocket(String host, int port) throws Exception {
        // TrustManager chấp nhận chứng chỉ self-signed của Server cho mục đích thực hành/lab
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());

        SSLSocketFactory sf = sslContext.getSocketFactory();
        SSLSocket socket = (SSLSocket) sf.createSocket(host, port);
        socket.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});
        socket.startHandshake();
        return socket;
    }

    /**
     * Lấy thông tin mã hóa bảo mật của kết nối Socket
     */
    public static String getSecurityInfo(Socket socket) {
        if (socket instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket) socket;
            SSLSession session = sslSocket.getSession();
            if (session != null && session.isValid()) {
                return session.getProtocol() + " (" + session.getCipherSuite() + ")";
            }
            return "SSL/TLS (Handshake thành công)";
        }
        return "TCP Tiêu chuẩn (Không mã hóa)";
    }
}
