# Account Service API 测试指南

## 测试环境准备

### 1. 启动服务
```bash
# 使用 Docker Compose
docker-compose up -d

# 或使用启动脚本（Windows）
start.bat
```

### 2. 验证服务状态
```bash
# 健康检查
curl http://localhost:9001/actuator/health

# 预期响应
{
  "status": "UP"
}
```

## API 测试用例

### 测试 1: 用户注册

#### 请求
```bash
curl -X POST http://localhost:9001/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "username": "john_doe",
    "password": "SecurePass123!",
    "shippingAddress": "123 Main Street, New York, NY 10001",
    "billingAddress": "123 Main Street, New York, NY 10001",
    "paymentMethod": "Credit Card - Visa ending in 1234"
  }'
```

#### 预期响应 (201 Created)
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "username": "john_doe",
  "shippingAddress": "123 Main Street, New York, NY 10001",
  "billingAddress": "123 Main Street, New York, NY 10001",
  "paymentMethod": "Credit Card - Visa ending in 1234"
}
```

#### PowerShell 版本
```powershell
$body = @{
    email = "john.doe@example.com"
    username = "john_doe"
    password = "SecurePass123!"
    shippingAddress = "123 Main Street, New York, NY 10001"
    billingAddress = "123 Main Street, New York, NY 10001"
    paymentMethod = "Credit Card - Visa ending in 1234"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:9001/api/accounts" `
  -Method POST `
  -ContentType "application/json" `
  -Body $body
```

### 测试 2: 重复注册（预期失败）

#### 请求
```bash
curl -X POST http://localhost:9001/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "username": "another_user",
    "password": "Password123!",
    "shippingAddress": "456 Another St",
    "billingAddress": "456 Another St",
    "paymentMethod": "Debit Card"
  }'
```

#### 预期响应 (409 Conflict)
```json
{
  "status": 409,
  "message": "User already exists with email: john.doe@example.com",
  "timestamp": "2025-11-13T10:30:00"
}
```

### 测试 3: 数据验证失败

#### 无效邮箱
```bash
curl -X POST http://localhost:9001/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "email": "invalid-email",
    "username": "testuser",
    "password": "Password123!",
    "shippingAddress": "123 Main St",
    "billingAddress": "123 Main St",
    "paymentMethod": "Cash"
  }'
```

#### 预期响应 (400 Bad Request)
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "email": "Email must be valid"
  },
  "timestamp": "2025-11-13T10:31:00"
}
```

#### 密码太短
```bash
curl -X POST http://localhost:9001/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "username": "testuser",
    "password": "short",
    "shippingAddress": "123 Main St",
    "billingAddress": "123 Main St",
    "paymentMethod": "Cash"
  }'
```

#### 预期响应 (400 Bad Request)
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "password": "Password must be at least 8 characters"
  },
  "timestamp": "2025-11-13T10:32:00"
}
```

### 测试 4: 查询用户信息

⚠️ **注意**: 此端点需要 JWT 认证（待 Auth Service 完成后启用）

#### 请求（当前无需 JWT）
```bash
curl -X GET http://localhost:9001/api/accounts/1
```

#### 使用 JWT 的请求（将来）
```bash
curl -X GET http://localhost:9001/api/accounts/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 预期响应 (200 OK)
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "username": "john_doe",
  "shippingAddress": "123 Main Street, New York, NY 10001",
  "billingAddress": "123 Main Street, New York, NY 10001",
  "paymentMethod": "Credit Card - Visa ending in 1234"
}
```

### 测试 5: 查询不存在的用户

#### 请求
```bash
curl -X GET http://localhost:9001/api/accounts/999
```

#### 预期响应 (404 Not Found)
```json
{
  "status": 404,
  "message": "User not found with id: 999",
  "timestamp": "2025-11-13T10:35:00"
}
```

### 测试 6: 更新用户信息

⚠️ **注意**: 此端点需要 JWT 认证（待 Auth Service 完成后启用）

#### 请求
```bash
curl -X PUT http://localhost:9001/api/accounts/1 \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe_updated",
    "shippingAddress": "789 New Avenue, Los Angeles, CA 90001",
    "billingAddress": "789 New Avenue, Los Angeles, CA 90001",
    "paymentMethod": "Credit Card - Mastercard ending in 5678"
  }'
```

#### PowerShell 版本
```powershell
$updateBody = @{
    username = "john_doe_updated"
    shippingAddress = "789 New Avenue, Los Angeles, CA 90001"
    billingAddress = "789 New Avenue, Los Angeles, CA 90001"
    paymentMethod = "Credit Card - Mastercard ending in 5678"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:9001/api/accounts/1" `
  -Method PUT `
  -ContentType "application/json" `
  -Body $updateBody
```

#### 预期响应 (200 OK)
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "username": "john_doe_updated",
  "shippingAddress": "789 New Avenue, Los Angeles, CA 90001",
  "billingAddress": "789 New Avenue, Los Angeles, CA 90001",
  "paymentMethod": "Credit Card - Mastercard ending in 5678"
}
```

### 测试 7: 内部认证接口（供 Auth Service 使用）

#### 请求
```bash
curl -X POST http://localhost:9001/api/accounts/_internal/auth \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com"
  }'
```

#### 预期响应 (200 OK)
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "passwordHash": "$2a$10$abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJ"
}
```

#### PowerShell 版本
```powershell
$authBody = @{
    email = "john.doe@example.com"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:9001/api/accounts/_internal/auth" `
  -Method POST `
  -ContentType "application/json" `
  -Body $authBody
```

### 测试 8: 内部认证 - 用户不存在

#### 请求
```bash
curl -X POST http://localhost:9001/api/accounts/_internal/auth \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nonexistent@example.com"
  }'
```

#### 预期响应 (404 Not Found)
```json
{
  "status": 404,
  "message": "User not found with email: nonexistent@example.com",
  "timestamp": "2025-11-13T10:40:00"
}
```

## 使用 Swagger UI 测试

### 访问 Swagger
```
http://localhost:9001/swagger-ui.html
```

### Swagger 功能
1. ✅ 查看所有 API 端点
2. ✅ 查看请求/响应模型
3. ✅ 在线测试 API（Try it out）
4. ✅ 查看错误响应示例
5. ✅ 下载 OpenAPI JSON

## 使用 Postman 测试

### 导入 Collection
1. 打开 Postman
2. 点击 Import
3. 选择 `Account-Service-API.postman_collection.json`
4. 开始测试

### Collection 包含的请求
- ✅ Register User
- ✅ Get User by ID
- ✅ Update User
- ✅ Internal - Get Auth Details
- ✅ Health Check

## 批量测试脚本

### Windows PowerShell 脚本
```powershell
# test-all.ps1

Write-Host "Testing Account Service APIs..." -ForegroundColor Green

# 1. Register User
Write-Host "`n1. Testing User Registration..." -ForegroundColor Cyan
$registerBody = @{
    email = "test_$(Get-Date -Format 'HHmmss')@example.com"
    username = "testuser"
    password = "Password123!"
    shippingAddress = "123 Test St"
    billingAddress = "123 Test St"
    paymentMethod = "Credit Card"
} | ConvertTo-Json

try {
    $user = Invoke-RestMethod -Uri "http://localhost:9001/api/accounts" `
        -Method POST -ContentType "application/json" -Body $registerBody
    Write-Host "✓ User registered with ID: $($user.id)" -ForegroundColor Green
    $userId = $user.id
} catch {
    Write-Host "✗ Failed: $_" -ForegroundColor Red
    exit 1
}

# 2. Get User
Write-Host "`n2. Testing Get User..." -ForegroundColor Cyan
try {
    $retrieved = Invoke-RestMethod -Uri "http://localhost:9001/api/accounts/$userId"
    Write-Host "✓ User retrieved: $($retrieved.email)" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed: $_" -ForegroundColor Red
}

# 3. Update User
Write-Host "`n3. Testing Update User..." -ForegroundColor Cyan
$updateBody = @{
    username = "updated_user"
    shippingAddress = "456 New St"
    billingAddress = "456 New St"
    paymentMethod = "Debit Card"
} | ConvertTo-Json

try {
    $updated = Invoke-RestMethod -Uri "http://localhost:9001/api/accounts/$userId" `
        -Method PUT -ContentType "application/json" -Body $updateBody
    Write-Host "✓ User updated: $($updated.username)" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed: $_" -ForegroundColor Red
}

# 4. Internal Auth
Write-Host "`n4. Testing Internal Auth..." -ForegroundColor Cyan
$authBody = @{ email = $user.email } | ConvertTo-Json

try {
    $auth = Invoke-RestMethod -Uri "http://localhost:9001/api/accounts/_internal/auth" `
        -Method POST -ContentType "application/json" -Body $authBody
    Write-Host "✓ Auth details retrieved" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed: $_" -ForegroundColor Red
}

# 5. Health Check
Write-Host "`n5. Testing Health Check..." -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "http://localhost:9001/actuator/health"
    Write-Host "✓ Service health: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed: $_" -ForegroundColor Red
}

Write-Host "`n✅ All tests completed!" -ForegroundColor Green
```

### 运行批量测试
```powershell
# 保存上述脚本为 test-all.ps1
.\test-all.ps1
```

## 数据库验证

### 连接 PostgreSQL
```bash
# 使用 Docker
docker exec -it account-postgres psql -U postgres -d accountdb

# 或使用 psql 客户端
psql -h localhost -p 5432 -U postgres -d accountdb
```

### 查询用户数据
```sql
-- 查看所有用户
SELECT id, email, username, shipping_address, payment_method 
FROM users;

-- 查看特定用户
SELECT * FROM users WHERE email = 'john.doe@example.com';

-- 统计用户数量
SELECT COUNT(*) FROM users;

-- 查看最近注册的用户
SELECT * FROM users ORDER BY id DESC LIMIT 5;
```

## 性能测试

### 使用 Apache Bench
```bash
# 注册接口性能测试（100个请求，10个并发）
ab -n 100 -c 10 -T 'application/json' \
  -p register-payload.json \
  http://localhost:9001/api/accounts
```

### register-payload.json
```json
{
  "email": "perf_test@example.com",
  "username": "perfuser",
  "password": "Password123!",
  "shippingAddress": "123 Perf St",
  "billingAddress": "123 Perf St",
  "paymentMethod": "Credit Card"
}
```

## 故障测试场景

### 1. 数据库连接失败
```bash
# 停止数据库
docker stop account-postgres

# 尝试访问 API
curl http://localhost:9001/api/accounts/1

# 预期：500 Internal Server Error 或连接超时
```

### 2. 服务健康检查
```bash
# 服务运行中
curl http://localhost:9001/actuator/health
# {"status":"UP"}

# 数据库停止
docker stop account-postgres
curl http://localhost:9001/actuator/health
# {"status":"DOWN"}
```

## 测试检查清单

- [ ] ✅ 用户注册成功（201）
- [ ] ✅ 重复注册失败（409）
- [ ] ✅ 无效邮箱验证（400）
- [ ] ✅ 密码长度验证（400）
- [ ] ✅ 查询用户成功（200）
- [ ] ✅ 查询不存在用户（404）
- [ ] ✅ 更新用户成功（200）
- [ ] ✅ 更新不存在用户（404）
- [ ] ✅ 内部认证成功（200）
- [ ] ✅ 内部认证用户不存在（404）
- [ ] ✅ 健康检查正常
- [ ] ✅ Swagger UI 可访问
- [ ] ✅ 数据库持久化正常
- [ ] ✅ 密码正确加密

## 常见问题排查

### Q1: 服务启动失败
```bash
# 检查端口占用
netstat -ano | findstr :9001

# 检查 Docker 服务
docker ps

# 查看服务日志
docker logs account-service
```

### Q2: 数据库连接失败
```bash
# 检查 PostgreSQL 是否运行
docker ps | grep postgres

# 测试数据库连接
docker exec -it account-postgres psql -U postgres -c "SELECT 1"
```

### Q3: API 返回 500 错误
```bash
# 查看应用日志
docker logs account-service -f

# 检查数据库连接配置
cat src/main/resources/application.yml
```

## 总结

本测试指南涵盖了 Account Service 的所有 API 端点测试，包括：

- ✅ 正常场景测试
- ✅ 异常场景测试
- ✅ 数据验证测试
- ✅ 多种测试工具使用
- ✅ 批量测试脚本
- ✅ 性能测试方法
- ✅ 故障测试场景

**开始测试前请确保服务已启动！** 🚀
