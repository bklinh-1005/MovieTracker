# 🎬 Movie Tracker

### Hệ thống theo dõi và đánh giá phim

---

## I. Giới thiệu đề tài

### Lý do chọn đề tài

**Hiện trạng:**
Xem phim và series là một trong những hình thức giải trí phổ biến nhất hiện nay, với mỗi người trung bình xem hàng chục đến hàng trăm bộ phim mỗi năm.

**Hạn chế thực tế:**
Tuy nhiên, phần lớn người xem phim gặp phải những vấn đề sau:

1. Không nhớ rõ mình đã xem những phim/series nào, đánh giá và cảm nhận ra sao tại thời điểm xem
2. Mất thời gian tìm kiếm thông tin rời rạc trên nhiều nguồn khác nhau (Google, mạng xã hội, bạn bè) để quyết định nên xem phim gì tiếp theo
3. Không có công cụ nào lưu lại lịch sử xem phim và cảm nhận cá nhân một cách có hệ thống theo thời gian
4. Danh sách phim muốn xem thường bị quên lãng do không có nơi quản lý tập trung

**Giải pháp:**
Đề tài xây dựng một hệ thống giúp người dùng:

- Lưu lại lịch sử xem phim một cách có tổ chức
- Đánh giá và ghi chú cảm nhận cho từng bộ phim
- Nhận gợi ý phù hợp dựa trên sở thích đã ghi nhận, giúp tiết kiệm thời gian lựa chọn nội dung giải trí

---

## II. Yêu cầu chức năng

### Chức năng cốt lõi

| STT | Chức năng | Mô tả |
|-----|-----------|-------|
| 1 | Quản lý tài khoản | Đăng ký, đăng nhập, phân quyền người dùng/quản trị viên |
| 2 | Tìm kiếm và duyệt phim | Tìm theo tên, lọc theo thể loại, năm phát hành, xem danh sách phim thịnh hành |
| 3 | Xem chi tiết phim | Poster, mô tả, diễn viên, đạo diễn, rating, trailer |
| 4 | Đánh dấu trạng thái | Đã xem / Muốn xem (watchlist) / Đang xem (đối với series) |
| 5 | Đánh giá & nhận xét | Chấm theo thang sao, viết cảm nhận cá nhân |
| 6 | Trang cá nhân & thống kê | Danh sách phim đã xem, số liệu tổng quan theo thời gian |

### Chức năng mở rộng

- **Gợi ý cá nhân hóa** — Thuật toán tính điểm tương đồng dựa trên thể loại, đạo diễn đã đánh giá cao
- **Tính năng xã hội** — Follow bạn bè, xem hoạt động, tạo watchlist chung theo nhóm
- **Thống kê nâng cao** — Biểu đồ xu hướng xem phim theo tháng, thể loại yêu thích nhất
- **Danh sách tùy chỉnh** — Người dùng tự tạo và chia sẻ danh sách phim theo chủ đề riêng

---

## III. Yêu cầu phi chức năng

| Tiêu chí | Mô tả |
|----------|-------|
| **Hiệu năng** | Thời gian phản hồi dưới 2 giây nhờ cơ chế cache dữ liệu phim từ TMDB API vào database nội bộ |
| **Bảo mật** | Mã hóa mật khẩu, xác thực bằng JWT, ẩn API key ở phía backend (không lộ ra frontend) |
| **Khả dụng** | Hệ thống hoạt động ổn định, có xử lý lỗi khi API bên thứ ba (TMDB) bị gián đoạn hoặc chậm |
| **Khả năng mở rộng** | Kiến trúc tách lớp rõ ràng (frontend – backend – database), dễ bổ sung tính năng trong tương lai |
| **Khả năng sử dụng** | Giao diện trực quan, thao tác đơn giản, thân thiện trên nhiều kích thước màn hình |
| **Khả năng bảo trì** | Code tổ chức theo module rõ ràng, có tài liệu thiết kế và test case đi kèm |

---

## IV. Công cụ và công nghệ sử dụng

| Thành phần | Công nghệ |
|------------|-----------|
| Môi trường phát triển | IntelliJ IDEA |
| Ngôn ngữ lập trình | Java |
| Backend Framework | Spring Boot |
| Frontend | HTML/CSS + Thymeleaf |
| Build Tool | Maven (hoặc Gradle) |
| Database | MySQL (hoặc PostgreSQL) |
| Dữ liệu ngoài | TMDB API |
| Quản lý mã nguồn | Git & GitHub |
| Triển khai | Render / Railway |

---

## 👥 Thành viên nhóm

| Họ tên | Vai trò | GitHub |
|--------|---------|--------|
| ... | ... | ... |
