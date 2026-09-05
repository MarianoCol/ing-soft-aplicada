export interface Product {
  id: number;
  name: string;
  description?: string;
  price: number;
  stock: number;
  active?: boolean;
}

export interface Account {
  id: number;
  login: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  activated: boolean;
  authorities: string[];
}

export interface AdminProduct extends Product {
  active: boolean;
  deletable: boolean;
}

export type OrderStatus = 'PENDING' | 'COMPLETED' | 'CANCELLED';

export interface AdminOrderSummary {
  id: number;
  placedDate: string;
  status: OrderStatus;
  totalPrice: number;
  customerId: number;
  customerName: string;
  customerEmail: string;
}

export interface AdminOrderItem {
  id: number;
  quantity: number;
  totalPrice: number;
  productId: number;
  productName: string;
  unitPrice: number;
}

export interface AdminOrderDetail extends AdminOrderSummary {
  items: AdminOrderItem[];
}

export interface AdminDashboard {
  activeProducts: number;
  inactiveProducts: number;
  pendingOrders: number;
  completedOrders: number;
  cancelledOrders: number;
  activeUsers: number;
}

export interface AdminUser extends Account {
  imageUrl?: string;
  langKey?: string;
  createdBy?: string;
  createdDate?: string;
  lastModifiedBy?: string;
  lastModifiedDate?: string;
}

export interface CartItemView {
  id: number;
  quantity: number;
  totalPrice: number;
  productId: number;
  productName: string;
  unitPrice: number;
  stock: number;
}

export interface CartView {
  id: number;
  placedDate: string;
  status: 'PENDING' | 'COMPLETED' | 'CANCELLED';
  totalPrice: number;
  items: CartItemView[];
}

export type SyncState = 'pending' | 'conflict' | 'auth';

export interface PendingCartItem {
  id: string;
  username: string;
  productId: number;
  productName: string;
  unitPrice: number;
  stock: number;
  quantity: number;
  state: SyncState;
  error?: string;
}

export interface DisplayCartItem extends Omit<CartItemView, 'id'> {
  id?: number;
  syncState?: SyncState;
  error?: string;
}
