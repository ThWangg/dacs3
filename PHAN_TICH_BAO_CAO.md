# 📊 Phân tích Báo cáo `BaoCaoDACS_3.docx` vs Ứng dụng thực tế

> **Mục đích**: So sánh nội dung báo cáo đồ án với tình trạng triển khai thực tế trong code, chỉ ra các điểm còn thiếu ở cả hai phía.

---

## 1. Tổng quan báo cáo

Báo cáo mô tả việc xây dựng **ứng dụng bán hàng trực tuyến TAUT Shop** với các đặc điểm:

- **Công nghệ**: Kotlin + Jetpack Compose + Firebase (Firestore, Auth)
- **Kiến trúc**: MVVM
- **Bộ màu chủ đạo**: `#787FF6`, `#1CA7EC`, `#1F2F98`
- **3 phân hệ chính**: Người mua (BUYER) · Người bán (SELLER) · Quản trị viên (ADMIN)
- **Tính năng đặc biệt**: Hệ thống gợi ý sản phẩm bằng thuật toán **Cosine Similarity** dựa trên nhãn (tags) sản phẩm

---

## 2. ✅ Chức năng có trong Báo cáo VÀ đã triển khai trong App

| # | Chức năng | Phân hệ |
|---|-----------|---------|
| 1 | Đăng ký / Đăng nhập / Đăng xuất | Chung |
| 2 | Phân quyền 3 vai trò (BUYER / SELLER / ADMIN) | Chung |
| 3 | Quản lý thông tin cá nhân (profile, avatar) | Chung |
| 4 | Trang chủ người mua, xem danh mục sản phẩm | Buyer |
| 5 | Tìm kiếm sản phẩm theo từ khóa | Buyer |
| 6 | Hệ thống gợi ý sản phẩm (Cosine Similarity + tags) | Buyer |
| 7 | Thêm vào giỏ hàng & Đặt hàng (thanh toán giả lập) | Buyer |
| 8 | Theo dõi lịch sử & trạng thái đơn hàng | Buyer |
| 9 | Nhắn tin real-time Buyer ↔ Seller | Buyer / Seller |
| 10 | Quản lý sản phẩm gian hàng (thêm / sửa / xóa) | Seller |
| 11 | Xử lý đơn hàng (cập nhật trạng thái: đang chuẩn bị → đang giao → hoàn tất) | Seller |
| 12 | Dashboard thống kê doanh thu, đơn hàng | Seller |
| 13 | Gửi hồ sơ đăng ký gian hàng lên Admin duyệt | Seller |
| 14 | Admin: xem & khóa/mở tài khoản người dùng | Admin |
| 15 | Admin: kiểm duyệt yêu cầu lên Seller (duyệt / từ chối) | Admin |
| 16 | Admin: Dashboard thống kê tổng quan hệ thống | Admin |

---

## 3. 🔴 Chức năng có trong Báo cáo NHƯNG CHƯA triển khai trong App

### 3.1 🏦 Hệ thống Ví điện tử

> **Mức độ**: ⚠️ **Nghiêm trọng** — Báo cáo có cả **Use Case** và **Activity Diagram** cho chức năng này

**Báo cáo đề cập**:
- Model `User` có trường **"số dư ví cá nhân"**
- Chức năng **"Nạp tiền vào ví tài khoản"** (Hình 3.7: Biểu đồ hoạt động)
- Người dùng truy cập "Ví của tôi" → chọn "Nạp tiền"

**Thực tế trong code**:
```kotlin
// User.kt — KHÔNG có field balance/walletBalance
data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val role: String = "BUYER",
    val isDeactivated: Boolean = false,
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
    // ❌ Không có: val balance: Double = 0.0
)
```

- ❌ Không có màn hình **"Ví của tôi"**
- ❌ Không có màn hình **"Nạp tiền"**
- ❌ Không có logic trừ tiền từ ví khi đặt hàng

---

### 3.2 ⭐ Hệ thống Đánh giá sản phẩm (Review)

> **Mức độ**: ⚠️ **Quan trọng** — Ảnh hưởng trực tiếp đến tính chính xác của thuật toán gợi ý

**Báo cáo đề cập**:
- Model `Review` (STT 10 trong danh sách lớp): "Lưu trữ thông tin đánh giá sản phẩm"
- Thuật toán gợi ý sử dụng `ratingAverage` và `soldCount` để tính điểm cuối cùng

**Thực tế trong code**:
- ❌ **Không có file `Review.kt`** trong thư mục `data/model/`
- `ratingAverage` trong `Product.kt` chỉ là **dữ liệu tĩnh** từ seed data, không được tạo ra từ đánh giá thực của người dùng
- ❌ Không có giao diện để người mua gửi đánh giá sau khi đơn hàng hoàn tất
- ❌ Không có màn hình xem danh sách đánh giá sản phẩm

---

### 3.3 📢 Thông báo đẩy (Push Notification)

**Báo cáo đề cập**:
- Khi Admin duyệt/từ chối Seller: "Hệ thống tự động gửi **thông báo đẩy** đến thiết bị của người dùng"
- Gửi kết quả và lý do từ chối qua push notification

**Thực tế trong code**:
- Chỉ có `SellerNotificationScreen.kt` hiển thị thông báo **trong app** (in-app notification)
- ❌ **Không tích hợp Firebase Cloud Messaging (FCM)** để gửi push notification thực đến thiết bị

---

### 3.4 📊 Thống kê Doanh thu toàn sàn (Admin Dashboard)

**Báo cáo đề cập**: "theo dõi doanh thu toàn sàn, số lượng người dùng đang hoạt động"

**Thực tế**:
- Admin Dashboard hiện tại chỉ thống kê: **tổng Users / Products / Buyers / Sellers**
- ❌ Chưa hiển thị **doanh thu tổng** toàn sàn

---

### 3.5 💬 Lý do từ chối yêu cầu Seller

**Báo cáo đề cập**: Khi từ chối, "Hệ thống hiển thị hộp thoại yêu cầu QTV **nhập lý do từ chối cụ thể**"

**Thực tế**: Nút "Từ chối" có confirm dialog nhưng không có trường nhập lý do — lý do không được lưu hay gửi đến Seller.

---

## 4. 🟡 Chức năng có trong App NHƯNG chưa được đề cập trong Báo cáo

> Những tính năng này đã được triển khai nhưng báo cáo **chưa mô tả** — cần bổ sung vào chương 4

| # | Chức năng | Vị trí trong code |
|---|-----------|------------------|
| 1 | **Bong bóng chat trôi nổi** (Floating Bubble, kéo thả được, badge số tin chưa đọc) | `FloatingChatBubble.kt` |
| 2 | **Nhắn tin mặc định với Admin** (ghim đầu danh sách, tất cả user đều có) | `ChatViewModel.kt`, `ChatListScreen.kt` |
| 3 | **Admin duyệt sản phẩm mới** của Seller (tab "Duyệt Sản phẩm" trong Requests) | `AdminRequestsScreen.kt` |
| 4 | **Biểu đồ doanh thu theo tuần** trong Seller Dashboard | `SellerDashboardScreen.kt` |
| 5 | **Recent Activity** với nút "Xem thêm / Thu gọn" | `SellerDashboardScreen.kt` |
| 6 | **Thông báo icon chuông** với chấm đỏ trên Admin Dashboard header | `AdminDashboardScreen.kt` |
| 7 | **Màn hình thông báo riêng** cho Seller | `SellerNotificationScreen.kt` |
| 8 | **Seller gửi yêu cầu đăng ký** với form thông tin gian hàng | `BuyerProfileScreen.kt` |

---

## 5. 📝 Phần báo cáo còn trống / sơ sài

> [!IMPORTANT]
> Hai chương quan trọng nhất trong báo cáo hiện tại **gần như không có nội dung**

| Chương / Mục | Trạng thái hiện tại |
|-------------|---------------------|
| **Chương 4: Xây dựng chương trình** | ⚠️ Chỉ có tiêu đề, **không có nội dung** |
| **Chương 5: Kết luận & Hướng phát triển** | ⚠️ Chỉ có các tiêu đề con (Kết quả đạt được / Lợi thế / Hạn chế / Phương hướng), **không có nội dung** |
| **Tài liệu tham khảo** | ⚠️ Trống |
| **Ảnh chụp màn hình giao diện** | ❌ Chưa có trong chương 4 |

---

## 6. 📋 Danh sách việc cần làm

### Cần bổ sung/sửa trong Ứng dụng:

- [ ] **Ví điện tử**: Thêm field `balance` vào `User`, tạo màn hình nạp tiền, trừ tiền khi đặt hàng
- [ ] **Đánh giá sản phẩm**: Tạo `Review.kt`, cho phép Buyer đánh giá sau đơn DELIVERED, tự động cập nhật `ratingAverage`
- [ ] **Lý do từ chối Seller**: Thêm trường nhập lý do khi Admin từ chối yêu cầu
- [ ] **Doanh thu Admin**: Hiển thị tổng doanh thu toàn sàn trong Admin Dashboard

### Cần bổ sung/hoàn thiện trong Báo cáo:

- [ ] **Chương 4**: Viết mô tả chi tiết và đính kèm ảnh chụp màn hình cho từng chức năng đã xây dựng
- [ ] **Chương 5.1 - Kết quả đạt được**: Liệt kê đầy đủ các chức năng đã triển khai thành công
- [ ] **Chương 5.2 - Lợi thế**: Nêu điểm mạnh của ứng dụng (real-time chat, thuật toán gợi ý, v.v.)
- [ ] **Chương 5.3 - Hạn chế**: Nêu thực trạng (chưa có ví thực, chưa có push notification FCM, v.v.)
- [ ] **Chương 5.4 - Hướng phát triển**: Đề xuất tích hợp cổng thanh toán, FCM, Review thực, iOS
- [ ] **Tài liệu tham khảo**: Bổ sung nguồn (Firebase docs, Kotlin docs, Jetpack Compose, v.v.)
- [ ] **Bổ sung Use Case / Mô tả** cho các chức năng chưa đề cập: Floating Chat, Admin duyệt sản phẩm, nhắn tin Admin

---

## 7. Danh sách 10 lớp dữ liệu — Đối chiếu

| # | Lớp trong Báo cáo | Có trong code? | Ghi chú |
|---|------------------|---------------|---------|
| 1 | `User` | ✅ `User.kt` | Thiếu field `balance` |
| 2 | `Product` | ✅ `Product.kt` | Có `ratingAverage` nhưng static |
| 3 | `Category` | ✅ `Category.kt` | Đầy đủ |
| 4 | `CartItem` | ✅ `CartItem.kt` | Đầy đủ |
| 5 | `Order` | ✅ `Order.kt` | Đầy đủ |
| 6 | `OrderItem` | ✅ (trong `Order.kt`) | Đầy đủ |
| 7 | `SellerRequest` | ✅ `SellerRequest.kt` | Đầy đủ |
| 8 | `ChatRoom` | ✅ `ChatRoom.kt` | Đầy đủ |
| 9 | `Message` | ✅ `Message.kt` | Đầy đủ |
| 10 | `Review` | ❌ **Không có** | Cần tạo |

---

*Phân tích thực hiện ngày 22/05/2026*
