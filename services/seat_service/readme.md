# Seat Service

## Tổng quan
Seat Service quản lý thông tin về ghế ngồi và trạng thái đặt chỗ trong rạp chiếu phim. Dịch vụ này cung cấp API để kiểm tra ghế trống, đặt ghế và giải phóng ghế. Đây là một microservice được xây dựng bằng Spring Boot và Spring Cloud.

## Thiết lập
- Được đóng gói bằng `Dockerfile` được cung cấp.
- Mã nguồn được đặt trong thư mục `src/`.
- Sử dụng cơ sở dữ liệu MySQL để lưu trữ thông tin ghế.
- Sử dụng Redis để quản lý khóa phân tán (distributed lock) và trạng thái tạm thời của ghế.
- Sử dụng RabbitMQ để nhận thông báo về kết quả đặt vé.
- Đăng ký với Eureka Server để service discovery.

## Phát triển
- Định nghĩa API trong `docs/api-specs/seat_service.yaml`.
- Chạy cục bộ thông qua `docker-compose up --build` từ thư mục gốc.
- Mô hình dữ liệu bao gồm:
    - `Seat`: Thông tin về ghế (id, seat_number, reserved, showtime_id)
    - Redis cache: "seat:{showtimeId}:{seatNumber}" để khóa ghế tạm thời

## Xử lý đồng thời
Seat Service sử dụng Redis để xử lý trường hợp nhiều người cùng đặt một ghế:
- Khi người dùng bắt đầu đặt ghế, ghế được khóa tạm thời trong Redis với TTL 10 phút
- Nếu thanh toán thành công, ghế được đánh dấu là đã đặt trong cơ sở dữ liệu
- Nếu thanh toán thất bại hoặc hết thời gian, khóa Redis sẽ hết hạn và ghế được giải phóng
- RedisExpirationListener theo dõi sự kiện hết hạn khóa để tự động giải phóng ghế

## Endpoints
- Base URL: `http://localhost:8082/api/seats`
- `GET /api/seats/{showtimeId}/{seatNumber}`: Kiểm tra trạng thái của một ghế
- `GET /api/seats/{showtimeId}/available`: Lấy danh sách ghế còn trống cho một lịch chiếu
- `POST /api/seats/reserve/{showtimeId}/{seatNumber}`: Đặt ghế
- `POST /api/seats/release`: Giải phóng ghế đã đặt

## Message Queues
Seat Service lắng nghe các tin nhắn từ RabbitMQ:
- Queue: `booking.queue`
- Message format: `{"showtimeId": 1, "seatNumber": "A1", "status": "SUCCESS|FAILED"}`

Tham khảo `docs/api-specs/seat_service.yaml` để biết chi tiết API.