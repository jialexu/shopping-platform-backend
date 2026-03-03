# ============================================
# Chuwa Shopping Service - Demo Test Script
# ============================================
# This script tests the complete user flow:
# 1. Register user
# 2. Login (get JWT)
# 3. Create item
# 4. Initialize inventory
# 5. Create order
# 6. Make payment
# 7. Check order status
# ============================================

$ErrorActionPreference = "Continue"
$BASE_URL = "http://localhost:8080"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Chuwa Shopping Service - Demo Test" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Function to make API calls and display results
function Invoke-APICall {
    param(
        [string]$Title,
        [string]$Method,
        [string]$Endpoint,
        [hashtable]$Headers = @{},
        [string]$Body = $null
    )
    
    Write-Host "[$Title]" -ForegroundColor Yellow
    Write-Host "  $Method $Endpoint" -ForegroundColor Gray
    
    try {
        $params = @{
            Uri = "$BASE_URL$Endpoint"
            Method = $Method
            Headers = $Headers
            ContentType = "application/json"
        }
        
        if ($Body) {
            $params.Body = $Body
            Write-Host "  Request: $Body" -ForegroundColor DarkGray
        }
        
        $response = Invoke-RestMethod @params
        $responseJson = $response | ConvertTo-Json -Depth 5
        Write-Host "  Response: " -ForegroundColor Green -NoNewline
        Write-Host $responseJson -ForegroundColor White
        Write-Host ""
        
        return $response
    }
    catch {
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errorBody = $reader.ReadToEnd()
            Write-Host "  Details: $errorBody" -ForegroundColor Red
        }
        Write-Host ""
        return $null
    }
}

# Wait for services to be ready
Write-Host "Waiting for services to be ready..." -ForegroundColor Cyan
Start-Sleep -Seconds 10

# ============================================
# Step 1: Register User
# ============================================
$registerBody = @{
    email = "demo@test.com"
    username = "demouser"
    password = "password123"
    shippingAddress = "123 Main St, City, Country"
    billingAddress = "123 Main St, City, Country"
} | ConvertTo-Json

$registerResponse = Invoke-APICall `
    -Title "1. Register User" `
    -Method "POST" `
    -Endpoint "/api/accounts" `
    -Body $registerBody

if (-not $registerResponse) {
    Write-Host "Failed to register user. Exiting..." -ForegroundColor Red
    exit 1
}

$userId = $registerResponse.id

# ============================================
# Step 2: Login to get JWT Token
# ============================================
$loginBody = @{
    email = "demo@test.com"
    password = "password123"
} | ConvertTo-Json

$loginResponse = Invoke-APICall `
    -Title "2. Login" `
    -Method "POST" `
    -Endpoint "/api/auth/login" `
    -Body $loginBody

if (-not $loginResponse -or -not $loginResponse.token) {
    Write-Host "Failed to login. Exiting..." -ForegroundColor Red
    exit 1
}

$token = $loginResponse.token
$authHeaders = @{
    "Authorization" = "Bearer $token"
}

Write-Host "✓ JWT Token obtained successfully" -ForegroundColor Green
Write-Host ""

# ============================================
# Step 3: Create Item
# ============================================
$itemBody = @{
    name = "Demo Product"
    price = 99.99
    upc = "123456789"
    pictures = @("https://example.com/pic1.jpg", "https://example.com/pic2.jpg")
    attributes = @{
        color = "blue"
        size = "large"
        brand = "Demo Brand"
    }
} | ConvertTo-Json

$itemResponse = Invoke-APICall `
    -Title "3. Create Item" `
    -Method "POST" `
    -Endpoint "/api/items" `
    -Headers $authHeaders `
    -Body $itemBody

if (-not $itemResponse) {
    Write-Host "Failed to create item. Exiting..." -ForegroundColor Red
    exit 1
}

$itemId = $itemResponse.id

# ============================================
# Step 4: Initialize Inventory
# ============================================
$inventoryResponse = Invoke-APICall `
    -Title "4. Initialize Inventory" `
    -Method "POST" `
    -Endpoint "/api/inventory/$itemId/init?quantity=100" `
    -Headers $authHeaders

if (-not $inventoryResponse) {
    Write-Host "Warning: Failed to initialize inventory, continuing..." -ForegroundColor Yellow
}

# ============================================
# Step 5: Check Inventory
# ============================================
$checkInventoryResponse = Invoke-APICall `
    -Title "5. Check Inventory" `
    -Method "GET" `
    -Endpoint "/api/inventory/$itemId" `
    -Headers $authHeaders

# ============================================
# Step 6: Create Order
# ============================================
$orderBody = @{
    userId = $userId
    items = @(
        @{
            sku = $itemId
            quantity = 2
            unitPrice = 99.99
        }
    )
} | ConvertTo-Json -Depth 5

$orderResponse = Invoke-APICall `
    -Title "6. Create Order" `
    -Method "POST" `
    -Endpoint "/api/orders" `
    -Headers $authHeaders `
    -Body $orderBody

if (-not $orderResponse) {
    Write-Host "Failed to create order. Exiting..." -ForegroundColor Red
    exit 1
}

$orderId = $orderResponse.id

# Wait for order processing
Write-Host "Waiting for order to be processed..." -ForegroundColor Cyan
Start-Sleep -Seconds 3

# ============================================
# Step 7: Make Payment
# ============================================
$paymentBody = @{
    orderId = $orderId
    amount = 199.98
    paymentMethod = "CREDIT_CARD"
} | ConvertTo-Json

$paymentResponse = Invoke-APICall `
    -Title "7. Make Payment" `
    -Method "POST" `
    -Endpoint "/api/payments" `
    -Headers $authHeaders `
    -Body $paymentBody

if (-not $paymentResponse) {
    Write-Host "Failed to make payment. Continuing to check order..." -ForegroundColor Yellow
}

$paymentId = if ($paymentResponse) { $paymentResponse.id } else { $null }

# Wait for payment processing
Write-Host "Waiting for payment to be processed..." -ForegroundColor Cyan
Start-Sleep -Seconds 3

# ============================================
# Step 8: Check Order Status
# ============================================
$orderStatusResponse = Invoke-APICall `
    -Title "8. Check Order Status" `
    -Method "GET" `
    -Endpoint "/api/orders/$orderId" `
    -Headers $authHeaders

# ============================================
# Step 9: Check Payment Status (if payment was created)
# ============================================
if ($paymentId) {
    $paymentStatusResponse = Invoke-APICall `
        -Title "9. Check Payment Status" `
        -Method "GET" `
        -Endpoint "/api/payments/$paymentId" `
        -Headers $authHeaders
}

# ============================================
# Step 10: Get Item by UPC
# ============================================
$itemByUpcResponse = Invoke-APICall `
    -Title "10. Get Item by UPC" `
    -Method "GET" `
    -Endpoint "/api/items/by-upc/123456789" `
    -Headers $authHeaders

# ============================================
# Summary
# ============================================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Demo Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "User ID:     $userId" -ForegroundColor White
Write-Host "Item ID:     $itemId" -ForegroundColor White
Write-Host "Order ID:    $orderId" -ForegroundColor White
Write-Host "Payment ID:  $paymentId" -ForegroundColor White
Write-Host ""
Write-Host "✓ Demo completed successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps for demo:" -ForegroundColor Yellow
Write-Host "  1. Show Swagger UI: http://localhost:9001/swagger-ui.html" -ForegroundColor Gray
Write-Host "  2. Show Kafka UI: http://localhost:8082" -ForegroundColor Gray
Write-Host "  3. Show order state transitions" -ForegroundColor Gray
Write-Host "  4. Explain microservices communication" -ForegroundColor Gray
Write-Host ""
