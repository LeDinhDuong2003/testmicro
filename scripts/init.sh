#!/bin/bash
# Triển khai hệ thống đặt vé xem phim lên Kubernetes
# Script này sẽ tạo và áp dụng các file cấu hình Kubernetes

set -e

# Màu sắc cho output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Thư mục hiện tại
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
K8S_DIR="$ROOT_DIR/kubernetes"

# Tạo thư mục Kubernetes nếu chưa tồn tại
mkdir -p $K8S_DIR

# Tạo Namespace mới
echo -e "${BLUE}Tạo Namespace mới cho ứng dụng...${NC}"
cat > $K8S_DIR/namespace.yaml << EOF
apiVersion: v1
kind: Namespace
metadata:
  name: movie-booking-system
EOF

kubectl apply -f $K8S_DIR/namespace.yaml
echo -e "${GREEN}Đã tạo namespace movie-booking-system${NC}"

# Tạo ConfigMap cho các cấu hình chung
echo -e "${BLUE}Tạo ConfigMap cho các cấu hình chung...${NC}"
cat > $K8S_DIR/configmap.yaml << EOF
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: movie-booking-system
data:
  SPRING_PROFILES_ACTIVE: "prod"
  EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: "http://eureka-server:8761/eureka/"
  SPRING_DATASOURCE_URL: "jdbc:mysql://mysql:3306/booking_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true"
  SPRING_RABBITMQ_HOST: "rabbitmq"
  SPRING_DATA_REDIS_HOST: "redis"
EOF

kubectl apply -f $K8S_DIR/configmap.yaml
echo -e "${GREEN}Đã tạo ConfigMap app-config${NC}"

# Tạo Secret
echo -e "${BLUE}Tạo Secret cho thông tin xác thực...${NC}"
cat > $K8S_DIR/secret.yaml << EOF
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
  namespace: movie-booking-system
type: Opaque
data:
  SPRING_DATASOURCE_USERNAME: $(echo -n "root" | base64)
  SPRING_DATASOURCE_PASSWORD: $(echo -n "root" | base64)
  JWT_SECRET: $(echo -n "your-secret-key-for-jwt-signing" | base64)
  RABBITMQ_DEFAULT_USER: $(echo -n "guest" | base64)
  RABBITMQ_DEFAULT_PASS: $(echo -n "guest" | base64)
EOF

kubectl apply -f $K8S_DIR/secret.yaml
echo -e "${GREEN}Đã tạo Secret app-secrets${NC}"

# Triển khai MySQL
echo -e "${BLUE}Triển khai MySQL...${NC}"
cat > $K8S_DIR/mysql.yaml << EOF
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pvc
  namespace: movie-booking-system
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
  namespace: movie-booking-system
spec:
  selector:
    matchLabels:
      app: mysql
  strategy:
    type: Recreate
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - image: mysql:8.0
        name: mysql
        env:
        - name: MYSQL_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: SPRING_DATASOURCE_PASSWORD
        - name: MYSQL_DATABASE
          value: booking_db
        ports:
        - containerPort: 3306
          name: mysql
        volumeMounts:
        - name: mysql-data
          mountPath: /var/lib/mysql
      volumes:
      - name: mysql-data
        persistentVolumeClaim:
          claimName: mysql-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: mysql
  namespace: movie-booking-system
spec:
  ports:
  - port: 3306
  selector:
    app: mysql
  clusterIP: None
EOF

kubectl apply -f $K8S_DIR/mysql.yaml
echo -e "${GREEN}Đã triển khai MySQL${NC}"

# Triển khai Redis
echo -e "${BLUE}Triển khai Redis...${NC}"
cat > $K8S_DIR/redis.yaml << EOF
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: redis-pvc
  namespace: movie-booking-system
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
  namespace: movie-booking-system
spec:
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - name: redis
        image: redis:alpine
        ports:
        - containerPort: 6379
        volumeMounts:
        - name: redis-data
          mountPath: /data
        args: ["--appendonly", "yes"]
      volumes:
      - name: redis-data
        persistentVolumeClaim:
          claimName: redis-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: redis
  namespace: movie-booking-system
spec:
  ports:
  - port: 6379
    targetPort: 6379
  selector:
    app: redis
EOF

kubectl apply -f $K8S_DIR/redis.yaml
echo -e "${GREEN}Đã triển khai Redis${NC}"

# Triển khai RabbitMQ
echo -e "${BLUE}Triển khai RabbitMQ...${NC}"
cat > $K8S_DIR/rabbitmq.yaml << EOF
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: rabbitmq-pvc
  namespace: movie-booking-system
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: rabbitmq
  namespace: movie-booking-system
spec:
  selector:
    matchLabels:
      app: rabbitmq
  template:
    metadata:
      labels:
        app: rabbitmq
    spec:
      containers:
      - name: rabbitmq
        image: rabbitmq:3-management
        ports:
        - containerPort: 5672
          name: amqp
        - containerPort: 15672
          name: management
        env:
        - name: RABBITMQ_DEFAULT_USER
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: RABBITMQ_DEFAULT_USER
        - name: RABBITMQ_DEFAULT_PASS
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: RABBITMQ_DEFAULT_PASS
        volumeMounts:
        - name: rabbitmq-data
          mountPath: /var/lib/rabbitmq
      volumes:
      - name: rabbitmq-data
        persistentVolumeClaim:
          claimName: rabbitmq-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: rabbitmq
  namespace: movie-booking-system
spec:
  ports:
  - port: 5672
    name: amqp
    targetPort: 5672
  - port: 15672
    name: management
    targetPort: 15672
  selector:
    app: rabbitmq
EOF

kubectl apply -f $K8S_DIR/rabbitmq.yaml
echo -e "${GREEN}Đã triển khai RabbitMQ${NC}"

# Triển khai Eureka Server
echo -e "${BLUE}Triển khai Eureka Server...${NC}"
cat > $K8S_DIR/eureka-server.yaml << EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: eureka-server
  namespace: movie-booking-system
spec:
  replicas: 1
  selector:
    matchLabels:
      app: eureka-server
  template:
    metadata:
      labels:
        app: eureka-server
    spec:
      containers:
      - name: eureka-server
        image: ${ROOT_DIR##*/}-eureka-server:latest
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8761
---
apiVersion: v1
kind: Service
metadata:
  name: eureka-server
  namespace: movie-booking-system
spec:
  type: ClusterIP
  ports:
  - port: 8761
    targetPort: 8761
  selector:
    app: eureka-server
EOF

kubectl apply -f $K8S_DIR/eureka-server.yaml
echo -e "${GREEN}Đã triển khai Eureka Server${NC}"

# Triển khai Movie Service
echo -e "${BLUE}Triển khai Movie Service...${NC}"
cat > $K8S_DIR/movie-service.yaml << EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: movie-service
  namespace: movie-booking-system
spec:
  replicas: 2
  selector:
    matchLabels:
      app: movie-service
  template:
    metadata:
      labels:
        app: movie-service
    spec:
      containers:
      - name: movie-service
        image: ${ROOT_DIR##*/}-movie-service:latest
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_PROFILES_ACTIVE
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: SPRING_PROFILES_ACTIVE
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: SPRING_DATASOURCE_URL
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: SPRING_DATASOURCE_USERNAME
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: SPRING_DATASOURCE_PASSWORD
        - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
---
apiVersion: v1
kind: Service
metadata:
  name: movie-service
  namespace: movie-booking-system
spec:
  type: ClusterIP
  ports:
  - port: 8081
    targetPort: 8081
  selector:
    app: movie-service
EOF

kubectl apply -f $K8S_DIR/movie-service.yaml
echo -e "${GREEN}Đã triển khai Movie Service${NC}"

# Triển khai Seat Service
echo -e "${BLUE}Triển khai Seat Service...${NC}"
cat > $K8S_DIR/seat-service.yaml << EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: seat-service
  namespace: movie-booking-system
spec:
  replicas: 2
  selector:
    matchLabels:
      app: seat-service
  template:
    metadata:
      labels:
        app: seat-service
    spec:
      containers:
      - name: seat-service
        image: ${ROOT_DIR##*/}-seat-service:latest
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8082
        env:
        - name: SPRING_PROFILES_ACTIVE
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: SPRING_PROFILES_ACTIVE
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: SPRING_DATASOURCE_URL
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: SPRING_DATASOURCE_USERNAME
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: SPRING_DATASOURCE_PASSWORD
        - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
        - name: SPRING_RABBITMQ_HOST
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: SPRING_RABBITMQ_HOST
        - name: SPRING_DATA_REDIS_HOST
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: SPRING_DATA_REDIS_HOST
---
apiVersion: v1
kind: Service
metadata:
  name: seat-service
  namespace: movie-booking-system
spec:
  type: ClusterIP
  ports:
  - port: 8082
    targetPort: 8082
  selector:
    app: seat-service
EOF

kubectl apply -f $K8S_DIR/seat-service.yaml
echo -e "${GREEN}Đã triển khai Seat Service${NC}"

# Triển khai Booking Service
echo -e "${BLUE}Triển khai Booking Service...${NC}"
cat > $K8S_DIR/booking-service.yaml << EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: booking-service
  namespace: movie-booking-system
spec:
  replicas: 2
  selector:
    matchLabels:
      app: booking-service
  template:
    metadata:
      labels:
        app: booking-service
    spec:
      containers:
      - name: booking-service
        image: ${ROOT_DIR##*/}-booking-service:latest
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8083
        env:
        - name: SPRING_PROFILES_ACTIVE
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: SPRING_PROFILES_ACTIVE
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: SPRING_DATASOURCE_URL
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: SPRING_DATASOURCE_USERNAME
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: SPRING_DATASOURCE_PASSWORD
        - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
        - name: SPRING_RABBITMQ_HOST
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: SPRING_RABBITMQ_HOST
        - name: SPRING_DATA_REDIS_HOST
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: SPRING_DATA_REDIS_HOST
---
apiVersion: v1
kind: Service
metadata:
  name: booking-service
  namespace: movie-booking-system
spec:
  type: ClusterIP
  ports:
  - port: 8083
    targetPort: 8083
  selector:
    app: booking-service
EOF

kubectl apply -f $K8S_DIR/booking-service.yaml
echo -e "${GREEN}Đã triển khai Booking Service${NC}"

# Triển khai API Gateway
echo -e "${BLUE}Triển khai API Gateway...${NC}"
cat > $K8S_DIR/api-gateway.yaml << EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
  namespace: movie-booking-system
spec:
  replicas: 2
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
      - name: api-gateway
        image: ${ROOT_DIR##*/}-api-gateway:latest
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: SPRING_PROFILES_ACTIVE
        - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
          valueFrom:
            configMapKeyRef:
              name: app-config
              key: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: JWT_SECRET
---
apiVersion: v1
kind: Service
metadata:
  name: api-gateway
  namespace: movie-booking-system
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: api-gateway
EOF

kubectl apply -f $K8S_DIR/api-gateway.yaml
echo -e "${GREEN}Đã triển khai API Gateway${NC}"

# Kiểm tra trạng thái các pod
echo -e "${BLUE}Kiểm tra trạng thái các pod...${NC}"
kubectl get pods -n movie-booking-system

# Script để build image và push lên registry
echo -e "${BLUE}Tạo script build-images.sh...${NC}"
cat > $SCRIPT_DIR/build-images.sh << 'EOF'
#!/bin/bash
# Script để build Docker image cho các service

set -e

# Màu sắc cho output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Thư mục hiện tại
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
PROJECT_NAME=${ROOT_DIR##*/}

echo -e "${BLUE}Bắt đầu build Docker images...${NC}"

# Build Eureka Server
echo -e "${BLUE}Building Eureka Server...${NC}"
cd $ROOT_DIR/gateway/eureka_server
./mvnw clean package -DskipTests
docker build -t $PROJECT_NAME-eureka-server:latest .
echo -e "${GREEN}Đã build image $PROJECT_NAME-eureka-server:latest${NC}"

# Build API Gateway
echo -e "${BLUE}Building API Gateway...${NC}"
cd $ROOT_DIR/gateway/api_gateway
./mvnw clean package -DskipTests
docker build -t $PROJECT_NAME-api-gateway:latest .
echo -e "${GREEN}Đã build image $PROJECT_NAME-api-gateway:latest${NC}"

# Build Movie Service
echo -e "${BLUE}Building Movie Service...${NC}"
cd $ROOT_DIR/services/movie_service/movie_service
./mvnw clean package -DskipTests
docker build -t $PROJECT_NAME-movie-service:latest .
echo -e "${GREEN}Đã build image $PROJECT_NAME-movie-service:latest${NC}"

# Build Seat Service
echo -e "${BLUE}Building Seat Service...${NC}"
cd $ROOT_DIR/services/seat_service/seat_service
./mvnw clean package -DskipTests
docker build -t $PROJECT_NAME-seat-service:latest .
echo -e "${GREEN}Đã build image $PROJECT_NAME-seat-service:latest${NC}"

# Build Booking Service
echo -e "${BLUE}Building Booking Service...${NC}"
cd $ROOT_DIR/services/booking_service/booking_service
./mvnw clean package -DskipTests
docker build -t $PROJECT_NAME-booking-service:latest .
echo -e "${GREEN}Đã build image $PROJECT_NAME-booking-service:latest${NC}"

echo -e "${GREEN}Tất cả các images đã được build thành công!${NC}"
echo -e "${BLUE}Danh sách images:${NC}"
docker images | grep $PROJECT_NAME

# Nếu muốn push image lên Docker registry
# echo -e "${BLUE}Push images lên Docker registry...${NC}"
# docker push $PROJECT_NAME-eureka-server:latest
# docker push $PROJECT_NAME-api-gateway:latest
# docker push $PROJECT_NAME-movie-service:latest
# docker push $PROJECT_NAME-seat-service:latest
# docker push $PROJECT_NAME-booking-service:latest
# echo -e "${GREEN}Đã push tất cả images lên Docker registry${NC}"
EOF

chmod +x $SCRIPT_DIR/build-images.sh
echo -e "${GREEN}Đã tạo script build-images.sh${NC}"

# Tạo script để xóa các resources
echo -e "${BLUE}Tạo script cleanup.sh...${NC}"
cat > $SCRIPT_DIR/cleanup.sh << 'EOF'
#!/bin/bash
# Script để xóa tất cả các resources đã tạo

set -e

# Màu sắc cho output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}Xóa tất cả resources trong namespace movie-booking-system...${NC}"

kubectl delete namespace movie-booking-system

echo -e "${GREEN}Đã xóa tất cả resources!${NC}"
EOF

chmod +x $SCRIPT_DIR/cleanup.sh
echo -e "${GREEN}Đã tạo script cleanup.sh${NC}"

echo -e "${GREEN}Đã triển khai thành công hệ thống đặt vé xem phim lên Kubernetes!${NC}"
echo -e "${BLUE}Để truy cập hệ thống, hãy sử dụng địa chỉ IP của API Gateway:${NC}"
kubectl get service api-gateway -n movie-booking-system