# BÀI THỰC HÀNH: LẬP TRÌNH SOCKET TCP BẢO MẬT (SSL/TLS) - PTIT

- **Học phần**: Lập trình mạng
- **Sinh viên**: Trần Đức Anh - MSSV: `n24dtcn003` - PTIT
- **Công nghệ**: Java Swing, TCP Socket, JSSE (SSL/TLS), Multithreading (Đa luồng).

---

## 📌 Tóm tắt chức năng

1. **Giao thức**: TCP bảo mật bằng **SSL/TLS (TLSv1.3 & TLSv1.2)** với mã hóa đối xứng nâng cao (`TLS_AES_256_GCM_SHA384`).
2. **Xử lý nghiệp vụ**:
   - Client gửi 1 chuỗi ký tự bất kỳ lên Server (hỗ trợ hoàn hảo tiếng Việt UTF-8 có dấu).
   - Server nhận chuỗi, chuyển đổi thành toàn bộ **chữ in hoa** (`toUpperCase()`), sau đó gửi trả kết quả cho Client.
   - Client nhận kết quả và xuất ra màn hình giao diện Swing (hiển thị trực quan và lưu lại vào bảng lịch sử).
3. **Đa luồng (Multithreading)**:
   - Server tiếp nhận nhiều Client đồng thời qua `ExecutorService` (Cached ThreadPool) và `ClientHandler`.
   - Các luồng hoạt động độc lập, không làm đơ giao diện Swing (`SwingUtilities.invokeLater()`).

---

## 📁 Cấu trúc thư mục chuẩn PTIT

```
btvn/
├── src/
│   └── vn/edu/ptit/network/securesocket/
│       ├── SSLHelper.java          # Cấu hình SSLServerSocket, SSLSocket, nạp Keystore/Truststore
│       ├── ClientHandler.java      # Luồng xử lý độc lập cho từng Client (Multithread)
│       ├── ServerGUI.java          # Giao diện Swing Server Dashboard & Quản trị Client
│       ├── ClientGUI.java          # Giao diện Swing Client gửi/nhận chuỗi
│       ├── Main.java               # Launcher tự động xếp 3 cửa sổ song song chụp ảnh báo cáo
│       └── TestSecureSocket.java   # Script kiểm thử tự động đa luồng & mã hóa SSL
├── .vscode/
│   ├── launch.json                 # Cấu hình F5 chạy trực tiếp trong VS Code
│   └── settings.json               # Cấu hình sourcePaths "src" và UTF-8
├── pom.xml                         # Cấu hình Maven chuẩn cho IntelliJ IDEA / Eclipse / NetBeans
├── server.keystore                 # Chứng chỉ bảo mật Server RSA 2048-bit (PKCS12)
├── client.truststore               # Truststore xác thực Server
├── server.crt                      # Certificate công khai
├── Chay_HeThong.bat                # MENU điều khiển hệ thống tương tác (0-6)
├── run_server.bat                  # Khởi chạy nhanh Server GUI
├── run_client.bat                  # Khởi chạy nhanh Client GUI
├── Run_N_Clients.bat               # Mở đồng thời N Client để test đa luồng
├── compile.bat                     # Biên dịch lại toàn bộ mã nguồn Java
└── README.md                       # Tài liệu hướng dẫn
```

---

## 🚀 Hướng dẫn chạy chương trình

### Cách 1: Sử dụng Menu tương tác `Chay_HeThong.bat` (Khuyên dùng)
Nhấp đúp chuột vào file [**`Chay_HeThong.bat`**](file:///c:/Users/trand/Downloads/btvn/Chay_HeThong.bat):
- **Phím `[1]`**: Mở Server GUI.
- **Phím `[2]`**: Mở 1 Client mới (cho phép đặt tên client tùy ý).
- **Phím `[3]`**: Mở cùng lúc N Client để thử nghiệm Đa luồng.
- **Phím `[4]`**: Mở kịch bản chuẩn (Server bên trái, 2 Client bên phải) để chụp ảnh báo cáo nộp bài.
- **Phím `[5]`**: Chạy kiểm thử tự động (`TestSecureSocket`).
- **Phím `[6]`**: Biên dịch lại mã nguồn Java.

---

### Cách 2: Chạy trực tiếp trong VS Code / IntelliJ IDEA / NetBeans

- **Trong VS Code**:
  - Mở thư mục `btvn` trong VS Code.
  - Chuyển sang tab **Run & Debug** (phím tắt `Ctrl + Shift + D`).
  - Chọn **"▶ Chạy Server Dashboard"** hoặc **"▶ Kịch bản mẫu (Server + 2 Client)"** rồi nhấn F5.
- **Trong IntelliJ IDEA**:
  - Mở thư mục `btvn`. IntelliJ sẽ tự động nhận diện file `pom.xml` làm Maven Project chuẩn.
  - Chuột phải vào `ServerGUI.java` $\rightarrow$ chọn **Run 'ServerGUI.main()'**.
  - Chuột phải vào `ClientGUI.java` $\rightarrow$ chọn **Run 'ClientGUI.main()'**.

---

### Cách 3: Chạy từ Terminal / Command Prompt

```powershell
cd c:\Users\trand\Downloads\btvn

# 1. Biên dịch
javac -encoding UTF-8 src\vn\edu\ptit\network\securesocket\*.java

# 2. Khởi chạy Server
java -cp src vn.edu.ptit.network.securesocket.ServerGUI

# 3. Khởi chạy Client (mở thêm terminal khác)
java -cp src vn.edu.ptit.network.securesocket.ClientGUI "Client 1"
```
