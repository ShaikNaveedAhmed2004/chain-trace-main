import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

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
  email: string;
  role: string;
  userId: number;
}

export interface Product {
  id: number;
  name: string;
  description: string;
  category: string;
  sku: string;
  createdBy: number;
  createdByEmail: string;
  createdAt: string;
}

export interface Batch {
  id: number;
  productId: number;
  productName: string;
  batchNumber: string;
  quantity: number;
  currentOwner: number;
  currentOwnerEmail: string;
  currentLocation: string;
  status: 'CREATED' | 'IN_TRANSIT' | 'DELIVERED' | 'SOLD';
  createdAt: string;
}

export interface SupplyChainEvent {
  id: number;
  batchId: number;
  productId: number;
  productName: string;
  fromParty: number;
  fromPartyEmail: string;
  toParty: number;
  toPartyEmail: string;
  location: string;
  status: string;
  timestamp: string;
  txHash: string;
  blockNumber: number;
  verified: boolean;
}

export interface VerificationResponse {
  batch: Batch;
  product: Product;
  events: SupplyChainEvent[];
  allEventsVerified: boolean;
  paymentStatus: 'PENDING' | 'RELEASED' | 'COMPLETED' | 'FAILED';
  paymentAmount: number | null;
  paymentTxHash: string | null;
}

// Auth APIs
export const authAPI = {
  register: async (email: string, password: string, role: string) => {
    const response = await api.post<AuthResponse>('/auth/register', { email, password, role });
    return response.data;
  },
  login: async (email: string, password: string) => {
    const response = await api.post<AuthResponse>('/auth/login', { email, password });
    return response.data;
  },
};

// Product APIs
export const productAPI = {
  create: async (product: Partial<Product>) => {
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
};

// Batch APIs
export const batchAPI = {
  create: async (batch: Partial<Batch>) => {
    const response = await api.post<Batch>('/batches', batch);
    return response.data;
  },
  getAll: async () => {
    const response = await api.get<Batch[]>('/batches');
    return response.data;
  },
  getMyBatches: async () => {
    const response = await api.get<Batch[]>('/batches/my');
    return response.data;
  },
  getById: async (id: number) => {
    const response = await api.get<Batch>(`/batches/${id}`);
    return response.data;
  },
  updateStatus: async (id: number, status: string, location: string, newOwner: number) => {
    const response = await api.put<Batch>(`/batches/${id}/status`, { status, location, newOwner });
    return response.data;
  },
  getHistory: async (id: number) => {
    const response = await api.get<SupplyChainEvent[]>(`/batches/${id}/history`);
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
    const response = await api.put<User>(`/users/${id}/status?status=${status}`);
    return response.data;
  },
  updateRole: async (id: number, role: string) => {
    const response = await api.put<User>(`/users/${id}/role?role=${role}`);
    return response.data;
  },
  getProfile: async () => {
    const response = await api.get<User>('/users/profile');
    return response.data;
  },
  updateEmail: async (newEmail: string, currentPassword: string) => {
    const response = await api.put<User>('/users/profile/email', { newEmail, currentPassword });
    return response.data;
  },
  updatePassword: async (currentPassword: string, newPassword: string) => {
    await api.put('/users/profile/password', { currentPassword, newPassword });
  },
  getActivity: async () => {
    const response = await api.get<any[]>('/users/activity');
    return response.data;
  },
};

// Batch Export/Import APIs
export const batchExportAPI = {
  exportCsv: async () => {
    const response = await api.get('/batches/export/csv', {
      responseType: 'blob',
    });
    return response.data;
  },
  exportJson: async () => {
    const response = await api.get('/batches/export/json', {
      responseType: 'blob',
    });
    return response.data;
  },
  importCsv: async (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post<Batch[]>('/batches/import/csv', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },
  importJson: async (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post<Batch[]>('/batches/import/json', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },
};

export default api;
