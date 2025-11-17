import axios, { AxiosResponse } from 'axios';
import { ServiceHealth, UserData, ItemData, OrderData, PaymentData, ApiResponse } from './types';

export class ShoppingServiceAPI {
  private baseUrl: string;
  private token: string = '';

  constructor(baseUrl: string = 'http://localhost:8080') {
    this.baseUrl = baseUrl;
  }

  private getHeaders() {
    const headers: any = {
      'Content-Type': 'application/json'
    };
    
    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }
    
    return headers;
  }

  async checkServiceHealth(): Promise<ServiceHealth[]> {
    const services = [
      { name: 'Auth', port: 9000 },
      { name: 'Account', port: 9001 },
      { name: 'Item', port: 9002 },
      { name: 'Inventory', port: 9003 },
      { name: 'Order', port: 9004 },
      { name: 'Payment', port: 9005 },
      { name: 'Gateway', port: 8080 }
    ];

    const healthChecks = await Promise.allSettled(
      services.map(async (service): Promise<ServiceHealth> => {
        try {
          await axios.get(`http://localhost:${service.port}/actuator/health`, {
            timeout: 3000
          });
          return { ...service, status: 'OK' };
        } catch (error) {
          return { ...service, status: 'FAILED' };
        }
      })
    );

    return healthChecks.map((result, index) => 
      result.status === 'fulfilled' ? result.value : { ...services[index], status: 'FAILED' }
    );
  }

  async registerUser(): Promise<ApiResponse<UserData>> {
    try {
      const userData = {
        email: `demo${Math.floor(Math.random() * 9999)}@example.com`,
        username: `demo_user_${Math.floor(Math.random() * 9999)}`,
        password: 'SecurePass123',
        shippingAddress: '123 Main St',
        billingAddress: '123 Main St',
        paymentMethod: 'Credit Card'
      };

      const response = await axios.post(`${this.baseUrl}/api/accounts`, userData);
      return { success: true, data: { ...response.data, email: userData.email } };
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  }

  async login(email: string, password: string): Promise<ApiResponse<string>> {
    try {
      const response = await axios.post(`${this.baseUrl}/api/auth/login`, {
        email,
        password
      });
      
      this.token = response.data.token;
      return { success: true, data: this.token };
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  }

  async createItem(): Promise<ApiResponse<ItemData>> {
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

      const response = await axios.post(`${this.baseUrl}/api/items`, itemData, {
        headers: this.getHeaders()
      });

      return { success: true, data: response.data };
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  }

  async initializeInventory(sku: string, quantity: number = 50): Promise<ApiResponse<any>> {
    try {
      await axios.post(`${this.baseUrl}/api/inventory/${sku}/init?quantity=${quantity}`, {}, {
        headers: this.getHeaders()
      });
      return { success: true };
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  }

  async createOrder(userId: string, sku: string): Promise<ApiResponse<OrderData>> {
    try {
      const orderData = {
        userId,
        items: [{
          sku,
          quantity: 2
        }],
        shippingAddress: '123 Main St'
      };

      const response = await axios.post(`${this.baseUrl}/api/orders`, orderData, {
        headers: this.getHeaders()
      });

      return { success: true, data: response.data };
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  }

  async processPayment(orderId: string, amount: number): Promise<ApiResponse<PaymentData>> {
    try {
      const paymentData = {
        orderId,
        amount,
        paymentMethod: 'CREDIT_CARD'
      };

      const response = await axios.post(`${this.baseUrl}/api/payments`, paymentData, {
        headers: this.getHeaders()
      });

      return { success: true, data: response.data };
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  }

  async getOrderStatus(orderId: string): Promise<ApiResponse<OrderData>> {
    try {
      const response = await axios.get(`${this.baseUrl}/api/orders/${orderId}`, {
        headers: this.getHeaders()
      });

      return { success: true, data: response.data };
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  }

  extractUserIdFromToken(): string | null {
    if (!this.token) return null;
    
    try {
      const parts = this.token.split('.');
      const payload = JSON.parse(Buffer.from(parts[1], 'base64').toString());
      return payload.sub;
    } catch (error) {
      return null;
    }
  }
}