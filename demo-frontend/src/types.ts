export interface ServiceHealth {
  name: string;
  port: number;
  status: 'OK' | 'FAILED';
}

export interface UserData {
  id: string;
  email: string;
  username: string;
}

export interface ItemData {
  id: string;
  name: string;
  price: number;
  upc: string;
}

export interface OrderData {
  id: string;
  userId: string;
  totalAmount: number;
  status: string;
  items: OrderItem[];
}

export interface OrderItem {
  sku: string;
  quantity: number;
}

export interface PaymentData {
  id: string;
  orderId: string;
  amount: number;
  paymentMethod: string;
  status: string;
}

export interface DemoStep {
  id: number;
  name: string;
  status: 'pending' | 'running' | 'success' | 'error';
  message?: string;
  data?: any;
  error?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
}