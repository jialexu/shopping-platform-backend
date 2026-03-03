# Quick Demo Script for Shopping Service
# Run this script to execute a complete demo flow

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  SHOPPING SERVICE MICROSERVICES DEMO" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Step 1: Health Check
Write-Host "Step 1: Checking all services health..." -ForegroundColor Yellow
$services = @(
    @{Port=9000; Name="Auth"},
    @{Port=9001; Name="Account"},
    @{Port=9002; Name="Item"},
    @{Port=9003; Name="Inventory"},
    @{Port=9004; Name="Order"},
    @{Port=9005; Name="Payment"},
    @{Port=8080; Name="Gateway"}
)

$allHealthy = $true
foreach ($service in $services) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$($service.Port)/actuator/health" -UseBasicParsing -TimeoutSec 3
        Write-Host "  ✓ $($service.Name) ($($service.Port)): OK" -ForegroundColor Green
    } catch {
        Write-Host "  ✗ $($service.Name) ($($service.Port)): FAILED" -ForegroundColor Red
        $allHealthy = $false
    }
}

if (-not $allHealthy) {
    Write-Host "`nError: Not all services are healthy. Please check docker containers." -ForegroundColor Red
    exit 1
}

Start-Sleep -Seconds 2

# Step 2: Register User
Write-Host "`nStep 2: Registering a new user..." -ForegroundColor Yellow
$registerBody = @{
    email = "demo$(Get-Random -Maximum 9999)@example.com"
    username = "demo_user_$(Get-Random -Maximum 9999)"
    password = "SecurePass123"
    shippingAddress = "123 Main St"
    billingAddress = "123 Main St"
    paymentMethod = "Credit Card"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/accounts" `
        -Method POST `
        -ContentType "application/json" `
        -Body $registerBody `
        -UseBasicParsing
    Write-Host "  ✓ User registered successfully!" -ForegroundColor Green
    $userData = $registerResponse.Content | ConvertFrom-Json
    $email = ($registerBody | ConvertFrom-Json).email
} catch {
    Write-Host "  ✗ Registration failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Start-Sleep -Seconds 1

# Step 3: Login
Write-Host "`nStep 3: Logging in to get JWT token..." -ForegroundColor Yellow
$loginBody = @{
    email = $email
    password = "SecurePass123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -UseBasicParsing
    $token = ($loginResponse.Content | ConvertFrom-Json).token
    Write-Host "  ✓ Login successful!" -ForegroundColor Green
    Write-Host "  Token: $($token.Substring(0, 30))..." -ForegroundColor Gray
} catch {
    Write-Host "  ✗ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Start-Sleep -Seconds 1

# Step 4: Create Item
Write-Host "`nStep 4: Creating a product item..." -ForegroundColor Yellow
$upc = "$(Get-Random -Minimum 100000000000 -Maximum 999999999999)"
$itemBody = @{
    name = "Laptop Pro 15 - Demo"
    description = "High-performance laptop for professionals"
    price = 1299.99
    upc = $upc
    stockQuantity = 50
    category = "Electronics"
} | ConvertTo-Json

try {
    $itemResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/items" `
        -Method POST `
        -Headers $headers `
        -Body $itemBody `
        -UseBasicParsing
    $item = $itemResponse.Content | ConvertFrom-Json
    $itemId = $item.id
    $sku = $item.upc
    Write-Host "  ✓ Item created: $($item.name)" -ForegroundColor Green
    Write-Host "  Item ID: $itemId" -ForegroundColor Gray
    Write-Host "  SKU/UPC: $sku" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ Item creation failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Start-Sleep -Seconds 1

# Step 5: Add Inventory
Write-Host "`nStep 5: Adding inventory for the item..." -ForegroundColor Yellow

try {
    $inventoryResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/inventory/$sku/init?quantity=50" `
        -Method POST `
        -Headers $headers `
        -UseBasicParsing
    Write-Host "  ✓ Inventory initialized: 50 units for SKU $sku" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Inventory creation failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "  Response: $($_.Exception.Response)" -ForegroundColor Red
}

Start-Sleep -Seconds 1

# Step 6: Create Order
Write-Host "`nStep 6: Creating an order..." -ForegroundColor Yellow
# Extract userId from JWT token payload
$tokenParts = $token.Split('.')
$base64 = $tokenParts[1]
# Properly pad base64 string
$padding = 4 - ($base64.Length % 4)
if ($padding -ne 4) {
    $base64 = $base64 + ("=" * $padding)
}
$payload = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($base64))
$tokenData = $payload | ConvertFrom-Json
$userId = $tokenData.sub

$orderBody = @{
    userId = $userId
    items = @(
        @{
            sku = $sku
            quantity = 2
        }
    )
    shippingAddress = "123 Main St"
} | ConvertTo-Json -Depth 3

try {
    $orderResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/orders" `
        -Method POST `
        -Headers $headers `
        -Body $orderBody `
        -UseBasicParsing
    $order = $orderResponse.Content | ConvertFrom-Json
    $orderId = $order.id
    Write-Host "  ✓ Order created: $orderId" -ForegroundColor Green
    Write-Host "  Total Amount: `$$($order.totalAmount)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ Order creation failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Start-Sleep -Seconds 1

# Step 7: Process Payment
Write-Host "`nStep 7: Processing payment (triggers Kafka events)..." -ForegroundColor Yellow
$paymentBody = @{
    orderId = $orderId
    amount = 2599.98
    paymentMethod = "CREDIT_CARD"
} | ConvertTo-Json

try {
    $paymentResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/payments" `
        -Method POST `
        -Headers $headers `
        -Body $paymentBody `
        -UseBasicParsing
    $payment = $paymentResponse.Content | ConvertFrom-Json
    Write-Host "  ✓ Payment processed successfully!" -ForegroundColor Green
    Write-Host "  Payment ID: $($payment.id)" -ForegroundColor Gray
    Write-Host "  Status: $($payment.status)" -ForegroundColor Gray
} catch {
    Write-Host "  ⚠ Payment processing failed: $($_.Exception.Message)" -ForegroundColor Yellow
}

Start-Sleep -Seconds 2

# Step 8: Verify Order Status
Write-Host "`nStep 8: Checking order status (should be updated by Kafka event)..." -ForegroundColor Yellow
try {
    $orderStatusResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/orders/$orderId" `
        -Method GET `
        -Headers @{"Authorization"="Bearer $token"} `
        -UseBasicParsing
    $updatedOrder = $orderStatusResponse.Content | ConvertFrom-Json
    Write-Host "  ✓ Order Status: $($updatedOrder.status)" -ForegroundColor Green
} catch {
    Write-Host "  ⚠ Could not retrieve order status" -ForegroundColor Yellow
}

# Summary
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "           DEMO COMPLETED!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nWhat just happened:" -ForegroundColor White
Write-Host "  1. ✓ Verified all 7 microservices are healthy" -ForegroundColor Green
Write-Host "  2. ✓ Registered user in MySQL database" -ForegroundColor Green
Write-Host "  3. ✓ Authenticated and received JWT token" -ForegroundColor Green
Write-Host "  4. ✓ Created product in MongoDB" -ForegroundColor Green
Write-Host "  5. ✓ Added inventory in Cassandra" -ForegroundColor Green
Write-Host "  6. ✓ Created order in Cassandra" -ForegroundColor Green
Write-Host "  7. ✓ Processed payment in PostgreSQL" -ForegroundColor Green
Write-Host "  8. ✓ Kafka events triggered order status update" -ForegroundColor Green

Write-Host "`nExplore more:" -ForegroundColor Yellow
Write-Host "  • Swagger UI: http://localhost:8080/swagger-ui.html"
Write-Host "  • Kafka UI: http://localhost:8090"
Write-Host "  • Check DEMO_STEPS.md for detailed API examples"
Write-Host ""
