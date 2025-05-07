# Movie Service

## Tổng quan
Movie Service quản lý thông tin về phim và lịch chiếu phim. Dịch vụ này cung cấp API để tìm kiếm phim, xem chi tiết phim và quản lý lịch chiếu. Đây là một microservice được xây dựng bằng Spring Boot và Spring Cloud.

## Thiết lập
- Được đóng gói bằng `Dockerfile` được cung cấp.
- Mã nguồn được đặt trong thư mục `src/`.
- Sử dụng cơ sở dữ liệu MySQL để lưu trữ thông tin phim và lịch chiếu.
- Đăng ký với Eureka Server để service discovery.

## Phát triển
- Định nghĩa API trong `docs/api-specs/movie_service.yaml`.
- Chạy cục bộ thông qua `docker-compose up --build` từ thư mục gốc.
- Mô hình dữ liệu bao gồm:
    - `Movie`: Thông tin về phim (id, title, description)
    - `Showtime`: Thông tin về lịch chiếu (id, time, theater, movie_id)

## Endpoints

### Phim
- Base URL: `http://localhost:8081/api/movies`
- `GET /api/movies`: Lấy danh sách tất cả phim
- `GET /api/movies/{id}`: Lấy thông tin chi tiết của một phim theo ID
- `POST /api/movies`: Tạo phim mới
- `PUT /api/movies/{id}`: Cập nhật thông tin phim
- `DELETE /api/movies/{id}`: Xóa phim

### Lịch chiếu
- Base URL: `http://localhost:8081/api/showtimes`
- `GET /api/showtimes`: Lấy danh sách tất cả lịch chiếu
- `GET /api/showtimes/{id}`: Lấy thông tin chi tiết của một lịch chiếu theo ID
- `GET /api/showtimes/movie/{movieId}`: Lấy danh sách lịch chiếu của một phim
- `GET /api/showtimes/time-range?start={startTime}&end={endTime}`: Lấy danh sách lịch chiếu trong khoảng thời gian
- `POST /api/showtimes`: Tạo lịch chiếu mới
- `PUT /api/showtimes/{id}`: Cập nhật thông tin lịch chiếu
- `DELETE /api/showtimes/{id}`: Xóa lịch chiếu

Tham khảo `docs/api-specs/movie_service.yaml` để biết chi tiết API.