# Booking Service

## Tổng quan
Booking Service xử lý quá trình đặt vé xem phim, thanh toán và quản lý đơn đặt vé. Dịch vụ này tương tác với Movie Service để lấy thông tin phim và lịch chiếu, với Seat Service để kiểm tra và đặt ghế. Đây là một microservice trung tâm được xây dựng bằng Spring Boot và Spring Cloud.

## Thiết lập
- Được đóng gói bằng `Dockerfile` được cung cấp.
- Mã nguồn được đặt trong thư mục `src/`.
- Sử dụng cơ sở dữ liệu MySQL để lưu trữ thông tin đặt vé.
- Sử dụng Redis để lưu trữ trạng thái đặt vé tạm thời.
- Sử dụng RabbitMQ để gửi thông báo cho Seat Service và gửi email xác nhận.
- Sử dụng Spring Mail để gửi email xác nhận đặt vé.
- Đăng ký với Eureka Server để service discovery.

## Phát triển
- Định nghĩa API trong `docs/api-specs/booking_service.yaml`.
- Chạy cục bộ thông qua `docker-compose up --build` từ thư mục gốc.
- Mô hình dữ liệu bao gồm:
    - `Booking`: Thông tin về đặt vé (id, showtime_id, seat_number, customer_name, email, paid, created_at, updated_at)

## Luồng đặt vé
1. Người dùng chọn phim và lịch chiếu từ Movie Service
2. Người dùng chọn ghế từ Seat Service
3. Booking Service tạo đơn đặt vé tạm thời và khóa ghế
4. Người dùng thanh toán
5. Sau khi thanh toán thành công:
    - Booking Service cập nhật trạng thái đặt vé
    - Gửi thông báo đến Seat Service để cập nhật trạng thái ghế
    - Gửi email xác nhận đến người dùng

## Xử lý lỗi và khả năng chịu lỗi
- Circuit Breaker: Sử dụng Resilience4j để xử lý khi service khác không khả dụng
- Rate Limiting: Bảo vệ API khỏi quá tải
- Scheduled Tasks: Tự động hủy các đơn đặt vé chưa thanh toán sau 10 phút

## Giao tiếp với các service khác
- Sử dụng Feign Client để gọi API của Movie Service và Seat Service
- Sử dụng RabbitMQ để gửi thông báo

## Endpoints
- Base URL: `http://localhost:8083/api/bookings`
- `POST /api/bookings`: Tạo đơn đặt vé mới
- `GET /api/bookings/{id}`: Lấy thông tin chi tiết của một đơn đặt vé
- `GET /api/bookings/email/{email}`: Lấy danh sách đơn đặt vé theo email
- `DELETE /api/bookings/{id}`: Hủy đơn đặt vé

### Thanh toán
- Base URL: `http://localhost:8083/api/payment`
- `POST /api/payment/{bookingId}`: Xử lý thanh toán cho đơn đặt vé

## Message Queues
Booking Service sử dụng RabbitMQ để:
- Gửi thông báo cập nhật trạng thái ghế (queue: `booking.queue`)
- Gửi yêu cầu gửi email xác nhận (queue: `booking.confirmation.emails`)

Tham khảo `docs/api-specs/booking_service.yaml` để biết chi tiết API.