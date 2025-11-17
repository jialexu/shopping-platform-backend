"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.DemoRunner = void 0;
const api_1 = require("./api");
class DemoRunner {
    constructor(onStepUpdate) {
        this.steps = [];
        this.api = new api_1.ShoppingServiceAPI();
        this.onStepUpdate = onStepUpdate;
        this.initializeSteps();
    }
    initializeSteps() {
        this.steps = [
            { id: 1, name: 'Health Check', status: 'pending' },
            { id: 2, name: 'User Registration', status: 'pending' },
            { id: 3, name: 'User Login', status: 'pending' },
            { id: 4, name: 'Create Item', status: 'pending' },
            { id: 5, name: 'Initialize Inventory', status: 'pending' },
            { id: 6, name: 'Create Order', status: 'pending' },
            { id: 7, name: 'Process Payment', status: 'pending' },
            { id: 8, name: 'Check Order Status', status: 'pending' }
        ];
    }
    updateStep(stepId, update) {
        const stepIndex = this.steps.findIndex(s => s.id === stepId);
        if (stepIndex !== -1) {
            this.steps[stepIndex] = { ...this.steps[stepIndex], ...update };
            if (this.onStepUpdate) {
                this.onStepUpdate([...this.steps]);
            }
        }
    }
    async runDemo() {
        let userData = {};
        let itemData = {};
        let orderData = {};
        try {
            // Step 1: Health Check
            this.updateStep(1, { status: 'running' });
            const healthResults = await this.api.checkServiceHealth();
            const allHealthy = healthResults.every(service => service.status === 'OK');
            if (!allHealthy) {
                this.updateStep(1, {
                    status: 'error',
                    error: 'Not all services are healthy',
                    data: healthResults
                });
                return;
            }
            this.updateStep(1, {
                status: 'success',
                message: 'All services healthy',
                data: healthResults
            });
            // Step 2: User Registration
            this.updateStep(2, { status: 'running' });
            const registerResult = await this.api.registerUser();
            if (!registerResult.success) {
                this.updateStep(2, {
                    status: 'error',
                    error: registerResult.error
                });
                return;
            }
            userData = registerResult.data;
            this.updateStep(2, {
                status: 'success',
                message: `User registered: ${userData.email}`,
                data: userData
            });
            // Step 3: User Login
            this.updateStep(3, { status: 'running' });
            const loginResult = await this.api.login(userData.email, 'SecurePass123');
            if (!loginResult.success) {
                this.updateStep(3, {
                    status: 'error',
                    error: loginResult.error
                });
                return;
            }
            this.updateStep(3, {
                status: 'success',
                message: 'Login successful, JWT token received',
                data: { token: loginResult.data.substring(0, 30) + '...' }
            });
            // Step 4: Create Item
            this.updateStep(4, { status: 'running' });
            const itemResult = await this.api.createItem();
            if (!itemResult.success) {
                this.updateStep(4, {
                    status: 'error',
                    error: itemResult.error
                });
                return;
            }
            itemData = itemResult.data;
            this.updateStep(4, {
                status: 'success',
                message: `Item created: ${itemData.name}`,
                data: itemData
            });
            // Step 5: Initialize Inventory
            this.updateStep(5, { status: 'running' });
            const inventoryResult = await this.api.initializeInventory(itemData.upc);
            if (!inventoryResult.success) {
                this.updateStep(5, {
                    status: 'error',
                    error: inventoryResult.error
                });
                return;
            }
            this.updateStep(5, {
                status: 'success',
                message: `Inventory initialized: 50 units for SKU ${itemData.upc}`
            });
            // Step 6: Create Order
            this.updateStep(6, { status: 'running' });
            const userId = this.api.extractUserIdFromToken();
            if (!userId) {
                this.updateStep(6, {
                    status: 'error',
                    error: 'Could not extract user ID from token'
                });
                return;
            }
            const orderResult = await this.api.createOrder(userId, itemData.upc);
            if (!orderResult.success) {
                this.updateStep(6, {
                    status: 'error',
                    error: orderResult.error
                });
                return;
            }
            orderData = orderResult.data;
            this.updateStep(6, {
                status: 'success',
                message: `Order created: ${orderData.id}`,
                data: orderData
            });
            // Step 7: Process Payment
            this.updateStep(7, { status: 'running' });
            const paymentResult = await this.api.processPayment(orderData.id, 2599.98);
            if (!paymentResult.success) {
                this.updateStep(7, {
                    status: 'error',
                    error: paymentResult.error,
                    message: 'Payment processing failed - this is the known 500 error!'
                });
                // Continue to next step even if payment fails
            }
            else {
                this.updateStep(7, {
                    status: 'success',
                    message: 'Payment processed successfully',
                    data: paymentResult.data
                });
            }
            // Step 8: Check Order Status
            this.updateStep(8, { status: 'running' });
            await new Promise(resolve => setTimeout(resolve, 2000)); // Wait for potential Kafka processing
            const statusResult = await this.api.getOrderStatus(orderData.id);
            if (!statusResult.success) {
                this.updateStep(8, {
                    status: 'error',
                    error: statusResult.error
                });
                return;
            }
            this.updateStep(8, {
                status: 'success',
                message: `Order status: ${statusResult.data.status}`,
                data: statusResult.data
            });
        }
        catch (error) {
            console.error('Demo execution failed:', error);
        }
    }
    getSteps() {
        return [...this.steps];
    }
}
exports.DemoRunner = DemoRunner;
