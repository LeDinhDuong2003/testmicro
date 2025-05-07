## Use Case: Đặt vé xem phim

### Thành viên
- Lê Đình Dương - B21DCCN281
- Dương Hoàng Quân - B21DCCN102
- Nguyễn Huy Tú - B21DCCN750

---

### Mô tả

Hệ thống đặt vé xem phim trực tuyến cho phép người dùng tìm kiếm, lựa chọn phim, xem lịch chiếu, đặt vé và thanh toán trực tuyến. 

---

### Mô tả dịch vụ

## 1. Movie Service
- **Mô tả**: Movie Service quản lý thông tin về phim và lịch chiếu phim. Dịch vụ này cung cấp API để tìm kiếm phim, xem chi tiết phim và quản lý lịch chiếu. Đây là một microservice được xây dựng bằng Spring Boot và Spring Cloud.
- **Cơ sở dữ liệu**: MySQL
- **Endpoints chính**:
#### Movie
  - Base URL: `http://localhost:8081/api/movies`
  - `GET /api/movies`: Lấy danh sách tất cả phim
  - `GET /api/movies/{id}`: Lấy thông tin chi tiết của một phim theo ID
  - `POST /api/movies`: Tạo phim mới
  - `PUT /api/movies/{id}`: Cập nhật thông tin phim
  - `DELETE /api/movies/{id}`: Xóa phim
#### Showtime
  - Base URL: `http://localhost:8081/api/showtimes`
  - `GET /api/showtimes`: Lấy danh sách tất cả lịch chiếu
  - `GET /api/showtimes/{id}`: Lấy thông tin chi tiết của một lịch chiếu theo ID
  - `GET /api/showtimes/movie/{movieId}`: Lấy danh sách lịch chiếu của một phim
  - `GET /api/showtimes/time-range?start={startTime}&end={endTime}`: Lấy danh sách lịch chiếu trong khoảng thời gian
  - `POST /api/showtimes`: Tạo lịch chiếu mới
  - `PUT /api/showtimes/{id}`: Cập nhật thông tin lịch chiếu
  - `DELETE /api/showtimes/{id}`: Xóa lịch chiếu

## 2. Seat Service

- **Mô tả**: Seat Service quản lý thông tin về ghế ngồi và trạng thái đặt chỗ trong rạp chiếu phim. Dịch vụ này cung cấp API để kiểm tra ghế trống, đặt ghế và giải phóng ghế. Đây là một microservice được xây dựng bằng Spring Boot và Spring Cloud.

- **Cơ sở dữ liệu**: MySQL
- **Xử lý đồng thời**
  - Seat Service sử dụng Redis để xử lý trường hợp nhiều người cùng đặt một ghế:
  - Khi người dùng bắt đầu đặt ghế, ghế được khóa tạm thời trong Redis với TTL 10 phút
  - Nếu thanh toán thành công, ghế được đánh dấu là đã đặt trong cơ sở dữ liệu
  - Nếu thanh toán thất bại hoặc hết thời gian, khóa Redis sẽ hết hạn và ghế được giải phóng
  - RedisExpirationListener theo dõi sự kiện hết hạn khóa để tự động giải phóng ghế
- **Endpoints chính**:
   - Base URL: `http://localhost:8082/api/seats`
  - `GET /api/seats/{showtimeId}/{seatNumber}`: Kiểm tra trạng thái của một ghế
  - `GET /api/seats/{showtimeId}/available`: Lấy danh sách ghế còn trống cho một lịch chiếu
  - `POST /api/seats/reserve/{showtimeId}/{seatNumber}`: Đặt ghế
  - `POST /api/seats/release`: Giải phóng ghế đã đặt

## 3. Booking Service

- **Mô tả**: Booking Service xử lý quá trình đặt vé xem phim, thanh toán và quản lý đơn đặt vé. Dịch vụ này tương tác với Movie Service để lấy thông tin phim và lịch chiếu, với Seat Service để kiểm tra và đặt ghế. Đây là một microservice trung tâm được xây dựng bằng Spring Boot và Spring Cloud.
- **Cơ sở dữ liệu**: MySQL
- **Xử lý lỗi và khả năng chịu lỗi**:
  - Circuit Breaker: Sử dụng Resilience4j để xử lý khi service khác không khả dụng
  - Rate Limiting: Bảo vệ API khỏi quá tải
  - Scheduled Tasks: Tự động hủy các đơn đặt vé chưa thanh toán sau 10 phút
- **Giao tiếp với các service khác**:
  - Sử dụng Feign Client để gọi API của Movie Service và Seat Service
  - Sử dụng RabbitMQ để gửi thông báo
- **Endpoints chính**:
   - Base URL: `http://localhost:8083/api/bookings`
  - `POST /api/bookings`: Tạo đơn đặt vé mới
  - `GET /api/bookings/{id}`: Lấy thông tin chi tiết của một đơn đặt vé
  - `GET /api/bookings/email/{email}`: Lấy danh sách đơn đặt vé theo email
  - `DELETE /api/bookings/{id}`: Hủy đơn đặt vé
- **Thanh toán**:
  - Base URL: `http://localhost:8083/api/payment`
  - `POST /api/payment/{bookingId}`: Xử lý thanh toán cho đơn đặt vé

---

### Hướng dẫn chạy ứng dụng

1. **Clone this repository**

   ```bash
   git clone https://github.com/jnp2018/mid-project-281102750.git
   cd mid-project-281102750
   ```

2. **Copy environment file**

   ```bash
   cp .env.example .env
   ```

3. **Run with Docker Compose**

   ```bash
   docker-compose up --build
   ```
---

### Cách sử dụng API






