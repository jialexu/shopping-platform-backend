"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.ShoppingServiceAPI = void 0;
const axios_1 = __importDefault(require("axios"));
class ShoppingServiceAPI {
    constructor(baseUrl = 'http://localhost:8080') {
        this.token = '';
        this.baseUrl = baseUrl;
    }
    getHeaders() {
        const headers = {
            'Content-Type': 'application/json'
        };
        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }
        return headers;
    }
    async checkServiceHealth() {
        const services = [
            { name: 'Auth', port: 9000 },
            { name: 'Account', port: 9001 },
            { name: 'Item', port: 9002 },
            { name: 'Inventory', port: 9003 },
            { name: 'Order', port: 9004 },
            { name: 'Payment', port: 9005 },
            { name: 'Gateway', port: 8080 }
        ];
        const healthChecks = await Promise.allSettled(services.map(async (service) => {
            try {
                await axios_1.default.get(`http://localhost:${service.port}/actuator/health`, {
                    timeout: 3000
                });
                return { ...service, status: 'OK' };
            }
            catch (error) {
                return { ...service, status: 'FAILED' };
            }
        }));
        return healthChecks.map((result, index) => result.status === 'fulfilled' ? result.value : { ...services[index], status: 'FAILED' });
    }
    async registerUser() {
        try {
            const userData = {
                email: `demo${Math.floor(Math.random() * 9999)}@example.com`,
                username: `demo_user_${Math.floor(Math.random() * 9999)}`,
                password: 'SecurePass123',
                shippingAddress: '123 Main St',
                billingAddress: '123 Main St',
                paymentMethod: 'Credit Card'
            };
            const response = await axios_1.default.post(`${this.baseUrl}/api/accounts`, userData);
            return { success: true, data: { ...response.data, email: userData.email } };
        }
        catch (error) {
            return { success: false, error: error.message };
        }
    }
    async login(email, password) {
        try {
            const response = await axios_1.default.post(`${this.baseUrl}/api/auth/login`, {
                email,
                password
            });
            this.token = response.data.token;
            return { success: true, data: this.token };
        }
        catch (error) {
            return { success: false, error: error.message };
        }
    }
    async createItem() {
        try {
            const upc = Math.floor(Math.random() * (999999999999 - 100000000000) + 100000000000).toString();
            const itemData = {
                name: 'Laptop Pro 15 - Demo',
                description: 'High-performance laptop for professionals',
                price: 1299.99,
                upc,
                stockQuantity: 50,
                category: 'Electronics'
            };
            const response = await axios_1.default.post(`${this.baseUrl}/api/items`, itemData, {
                headers: this.getHeaders()
            });
            return { success: true, data: response.data };
        }
        catch (error) {
            return { success: false, error: error.message };
        }
    }
    async initializeInventory(sku, quantity = 50) {
        try {
            await axios_1.default.post(`${this.baseUrl}/api/inventory/${sku}/init?quantity=${quantity}`, {}, {
                headers: this.getHeaders()
            });
            return { success: true };
        }
        catch (error) {
            return { success: false, error: error.message };
        }
    }
    async createOrder(userId, sku) {
        try {
            const orderData = {
                userId,
                items: [{
                        sku,
                        quantity: 2
                    }],
                shippingAddress: '123 Main St'
            };
            const response = await axios_1.default.post(`${this.baseUrl}/api/orders`, orderData, {
                headers: this.getHeaders()
            });
            return { success: true, data: response.data };
        }
        catch (error) {
            return { success: false, error: error.message };
        }
    }
    async processPayment(orderId, amount) {
        try {
            const paymentData = {
                orderId,
                amount,
                paymentMethod: 'CREDIT_CARD'
            };
            const response = await axios_1.default.post(`${this.baseUrl}/api/payments`, paymentData, {
                headers: this.getHeaders()
            });
            return { success: true, data: response.data };
        }
        catch (error) {
            return { success: false, error: error.message };
        }
    }
    async getOrderStatus(orderId) {
        try {
            const response = await axios_1.default.get(`${this.baseUrl}/api/orders/${orderId}`, {
                headers: this.getHeaders()
            });
            return { success: true, data: response.data };
        }
        catch (error) {
            return { success: false, error: error.message };
        }
    }
    extractUserIdFromToken() {
        if (!this.token)
            return null;
        try {
            const parts = this.token.split('.');
            const payload = JSON.parse(Buffer.from(parts[1], 'base64').toString());
            return payload.sub;
        }
        catch (error) {
            return null;
        }
    }
}
exports.ShoppingServiceAPI = ShoppingServiceAPI;
