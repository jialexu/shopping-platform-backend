# Account Service

账户管理服务 - Chuwa 在线购物平台的用户账户管理微服务

## 功能概述

Account Service 是 Chuwa Final Project 的核心服务之一，负责用户账户的完整生命周期管理：

- ✅ 用户注册
- ✅ 用户信息更新
- ✅ 用户信息查询
- ✅ 为 Auth Service 提供内部认证接口

## 技术栈

- **Spring Boot 3.x** - 应用框架
- **Spring Data JPA** - 数据持久化
- **Spring Security** - 安全认证
- **PostgreSQL** - 关系型数据库
- **Spring Cloud OpenFeign** - 服务间通信
- **Spring Kafka** - 消息队列
- **Swagger/OpenAPI** - API 文档
- **JUnit 5 & Mockito** - 单元测试
- **Jacoco** - 代码覆盖率
- **Docker** - 容器化部署

## 架构设计

```
account-service/
├── src/main/java/com/icc/account/
│   ├── AccountApplication.java          # 应用入口
│   ├── config/
│   │   ├── SecurityConfig.java          # Spring Security 配置
│   │   └── OpenApiConfig.java           # Swagger 配置
│   ├── domain/
│   │   ├── User.java                    # 用户实体
│   │   └── UserRepository.java          # 数据访问层
│   ├── dto/
│   │   ├── RegisterRequest.java         # 注册请求
│   │   ├── UpdateRequest.java           # 更新请求
│   │   ├── AccountResponse.java         # 账户响应
│   │   ├── AuthAccountRequest.java      # 认证请求
│   │   └── AuthAccountDto.java          # 认证响应
│   ├── exception/
│   │   ├── UserNotFoundException.java
│   │   ├── UserAlreadyExistsException.java
│   │   └── GlobalExceptionHandler.java  # 全局异常处理
│   ├── service/
│   │   └── AccountService.java          # 业务逻辑层
│   └── web/
│       └── AccountController.java       # REST API 控制器
└── src/test/java/                       # 单元测试
```

## API 接口

### 1. 注册用户
```http
POST /api/accounts
Content-Type: application/json

{
  "email": "user@example.com",
  "username": "john_doe",
  "password": "SecurePass123!",
  "shippingAddress": "123 Main St, City, State 12345",
  "billingAddress": "123 Main St, City, State 12345",
  "paymentMethod": "Credit Card"
}
```

**响应: 201 Created**
```json
{
  "id": 1,
  "email": "user@example.com",
  "username": "john_doe",
  "shippingAddress": "123 Main St, City, State 12345",
  "billingAddress": "123 Main St, City, State 12345",
  "paymentMethod": "Credit Card"
}
```

### 2. 更新用户信息
```http
PUT /api/accounts/{id}
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}

{
  "username": "john_doe_updated",
  "shippingAddress": "456 New St, City, State 67890",
  "billingAddress": "456 New St, City, State 67890",
  "paymentMethod": "Debit Card"
}
```

**响应: 200 OK**

### 3. 查询用户信息
```http
GET /api/accounts/{id}
Authorization: Bearer {JWT_TOKEN}
```

**响应: 200 OK**

### 4. 内部认证接口（供 Auth Service 使用）
```http
POST /api/accounts/_internal/auth
Content-Type: application/json

{
  "email": "user@example.com"
}
```

**响应: 200 OK**
```json
{
  "id": 1,
  "email": "user@example.com",
  "passwordHash": "$2a$10$..."
}
```

## 数据库设计

### Users 表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| email | VARCHAR(255) | 邮箱，唯一索引 |
| username | VARCHAR(50) | 用户名 |
| password_hash | VARCHAR(255) | 密码哈希（BCrypt） |
| shipping_address | TEXT | 配送地址 |
| billing_address | TEXT | 账单地址 |
| payment_method | VARCHAR(100) | 支付方式 |

## 环境变量配置

```properties
# 数据库配置
DB_HOST=localhost
DB_PORT=5432
DB_NAME=accountdb
DB_USER=postgres
DB_PASSWORD=postgres

# JWT 配置
JWT_ISSUER_URI=http://auth-service:9000
JWT_JWK_SET_URI=http://auth-service:9000/.well-known/jwks.json

# Kafka 配置
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## 本地运行

### 前置条件
- JDK 17+
- Maven 3.8+
- PostgreSQL 14+

### 启动步骤

1. **启动 PostgreSQL 数据库**
```bash
# 使用 Docker
docker run -d --name postgres \
  -e POSTGRES_DB=accountdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:14-alpine
```

2. **编译项目**
```bash
mvn clean package -DskipTests
```

3. **运行服务**
```bash
java -jar target/account-service-1.0.0.jar
```

或使用 Maven:
```bash
mvn spring-boot:run
```

4. **访问 Swagger UI**
```
http://localhost:9001/swagger-ui.html
```

## Docker 部署

### 构建镜像
```bash
docker build -t account-service:latest .
```

### 运行容器
```bash
docker run -d --name account-service \
  -p 9001:9001 \
  -e DB_HOST=postgres \
  -e DB_PORT=5432 \
  -e DB_NAME=accountdb \
  -e DB_USER=postgres \
  -e DB_PASSWORD=postgres \
  account-service:latest
```

## 测试

### 运行单元测试
```bash
mvn test
```

### 生成代码覆盖率报告
```bash
mvn clean test jacoco:report
```

覆盖率报告位置: `target/site/jacoco/index.html`

### 测试覆盖范围
- ✅ AccountService 业务逻辑测试（9个测试用例）
- ✅ AccountController REST API 测试（9个测试用例）
- ✅ 异常场景测试
- ✅ 数据验证测试

**目标覆盖率: ≥ 30%**

## 安全配置

### 公开访问端点
- `POST /api/accounts` - 用户注册
- `POST /api/accounts/_internal/auth` - 内部认证接口
- `/swagger-ui/**` - API 文档
- `/actuator/**` - 健康检查

### 需要认证的端点
- `GET /api/accounts/{id}` - 查询用户信息（需要 JWT）
- `PUT /api/accounts/{id}` - 更新用户信息（需要 JWT）

### 密码加密
使用 **BCryptPasswordEncoder** 进行密码哈希，强度 10

## 健康检查

```bash
# 健康检查
curl http://localhost:9001/actuator/health

# 详细信息
curl http://localhost:9001/actuator/info
```

## 日志配置

日志级别配置在 `application.yml`:
```yaml
logging:
  level:
    com.icc.account: DEBUG
    org.springframework.security: DEBUG
```

## 故障排查

### 常见问题

1. **数据库连接失败**
   - 检查 PostgreSQL 是否运行
   - 验证数据库凭据
   - 确认网络连接

2. **端口已被占用**
   ```bash
   # Windows
   netstat -ano | findstr :9001
   # 终止进程
   taskkill /PID <PID> /F
   ```

3. **编译错误**
   - 确保 JDK 17+ 已安装
   - 清理 Maven 缓存: `mvn clean`

## 性能优化

- 使用连接池管理数据库连接
- 事务管理优化（@Transactional）
- 查询优化（只读事务）
- 索引优化（email 唯一索引）

## 后续增强

- [ ] 添加用户头像上传
- [ ] 实现用户搜索功能
- [ ] 添加用户角色权限管理
- [ ] 集成 Redis 缓存
- [ ] 添加审计日志
- [ ] 实现软删除功能

## 相关服务

- **Auth Service** (端口 9000) - JWT 认证中心
- **Gateway** (端口 8080) - API 网关
- **Item Service** (端口 9002) - 商品服务
- **Order Service** (端口 9004) - 订单服务

## 维护者

Chuwa Development Team

## 许可证

Copyright © 2025 Chuwa Team
