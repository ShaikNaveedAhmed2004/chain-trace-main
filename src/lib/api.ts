import axios from 'axios';

const API_BASE_URL = 'https://chain-trace-main.onrender.com/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    console.log('API Request:', config.method?.toUpperCase(), config.url, {
      headers: config.headers,
      data: config.data
    });
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export interface User {
  id: number;
  email: string;
  role: 'ADMIN' | 'SUPPLIER' | 'MANUFACTURER' | 'DISTRIBUTOR' | 'RETAILER' | 'CONSUMER';
  status: 'ACTIVE' | 'INACTIVE' | 'PENDING';
  createdAt: string;
}

export interface AuthResponse {
  token: string;
}

export interface Product {
  id: number;
  name: string;
  description: string;
  category: string;
  sku: string;
}

export interface Batch {
  id: number;
  productId: number;
  batchNumber: string;
  quantity: number;
  currentLocation: string;
  status: string;
}

export interface SupplyChainEvent {
  id: number;
  batchId: number;
  location: string;
  status: string;
  timestamp: string;
}

export interface VerificationResponse {
  productName: string;
  batchNumber: string;
  currentLocation: string;
  status: string;
  isAuthentic: boolean;
}

// Auth APIs
export const authAPI = {
  register: async (name: string, email: string, password: string, role: string) => {
    const response = await api.post<AuthResponse>('/auth/register', { name, email, password, role });
    return response.data;
  },
  login: async (email: string, password: string) => {
    const response = await api.post<AuthResponse>('/auth/login', { email, password });
    return response.data;
  },
};

// Product APIs
export const productAPI = {
  create: async (product: { name: string; description: string; category: string; sku: string }) => {
    const response = await api.post<Product>('/products', product);
    return response.data;
  },
  getAll: async () => {
    const response = await api.get<Product[]>('/products');
    return response.data;
  },
  getById: async (id: number) => {
    const response = await api.get<Product>(`/products/${id}`);
    return response.data;
  },
  update: async (id: number, product: { name: string; description: string; category: string; sku: string }) => {
    const response = await api.put<Product>(`/products/${id}`, product);
    return response.data;
  },
  delete: async (id: number) => {
    await api.delete(`/products/${id}`);
  },
};

// Batch APIs
export const batchAPI = {
  create: async (batch: {
    productId: number;
    batchNumber: string;
    quantity: number;
    currentLocation: string;
    status: string;
  }) => {
    const response = await api.post<Batch>('/batches', batch);
    return response.data;
  },
  getAll: async () => {
    const response = await api.get<Batch[]>('/batches');
    return response.data;
  },
  getById: async (id: number) => {
    const response = await api.get<Batch>(`/batches/${id}`);
    return response.data;
  },
  updateStatus: async (id: number, currentLocation: string, status: string) => {
    const response = await api.put<Batch>(`/batches/${id}/status`, { currentLocation, status });
    return response.data;
  },
  delete: async (id: number) => {
    await api.delete(`/batches/${id}`);
  },
  getHistory: async (id: number) => {
    const response = await api.get<SupplyChainEvent[]>(`/batches/${id}/history`);
    return response.data;
  },
  getMyBatches: async () => {
    const response = await api.get<Batch[]>('/batches/my');
    return response.data;
  },
};

// Consumer APIs
export const consumerAPI = {
  verify: async (batchId: number) => {
    const response = await api.get<VerificationResponse>(`/consumer/verify?batchId=${batchId}`);
    return response.data;
  },
};

// User APIs (Admin)
export const userAPI = {
  getAll: async () => {
    const response = await api.get<User[]>('/users');
    return response.data;
  },
  updateStatus: async (id: number, status: string) => {
    const response = await api.put<User>(`/users/${id}/status`, { status });
    return response.data;
  },
  updateRole: async (id: number, role: string) => {
    const response = await api.put<User>(`/users/${id}/role`, { role });
    return response.data;
  },
  getProfile: async () => {
    const response = await api.get<User>('/users/profile');
    return response.data;
  },
  updateEmail: async (newEmail: string) => {
    const response = await api.put<User>('/users/profile/email', { newEmail });
    return response.data;
  },
  updatePassword: async (oldPassword: string, newPassword: string) => {
    await api.put('/users/profile/password', { oldPassword, newPassword });
  },
  getActivity: async () => {
    const response = await api.get<any[]>('/users/activity');
    return response.data;
  },
};

// Batch Export/Import APIs
export const batchExportAPI = {
  export: async (format: 'csv' | 'json') => {
    const response = await api.get(`/batches/export/${format}`, {
      responseType: 'blob',
    });
    return response.data;
  },
  import: async (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    const format = file.name.endsWith('.json') ? 'json' : 'csv';
    const response = await api.post<Batch[]>(`/batches/import/${format}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },
};

export default api;

