# Shopping Service Demo Frontend

A TypeScript-based web dashboard for demonstrating the Shopping Service microservices architecture.

## Features

- **Real-time Demo Execution**: Watch each microservice step execute in real-time
- **Service Health Monitoring**: Visual indicators for all service statuses
- **Step-by-Step Visualization**: Clear progress tracking through the entire demo flow
- **Error Highlighting**: Specifically designed to showcase the payment service 500 error
- **Interactive Dashboard**: Start, stop, and reset demos with a modern web interface

## Demo Flow

1. **Health Check** - Verify all 7 microservices are running
2. **User Registration** - Create a new user account
3. **Authentication** - Login and receive JWT token
4. **Product Creation** - Add a new item to the catalog
5. **Inventory Management** - Initialize stock for the item
6. **Order Processing** - Create a new order
7. **Payment Processing** - Attempt payment (expected to fail with 500 error)
8. **Order Status Verification** - Check final order status

## Quick Start

```bash
# Install dependencies
npm install

# Run in development mode
npm run dev

# Or build and run in production
npm run build
npm start
```

Access the dashboard at: http://localhost:3000

## Architecture

- **Backend**: Express.js with TypeScript
- **Frontend**: Vanilla HTML/CSS/JavaScript
- **API Integration**: Axios for HTTP requests
- **Real-time Updates**: Polling-based status updates

## API Endpoints

- `GET /` - Demo dashboard
- `POST /api/demo/start` - Start demo execution
- `GET /api/demo/status` - Get current demo status
- `POST /api/demo/reset` - Reset demo state

## Purpose

This frontend is designed to visually demonstrate the Shopping Service microservices and specifically highlight the payment service 500 error that occurs during the demo. It provides a user-friendly way to observe the system behavior and debug the payment processing issue.