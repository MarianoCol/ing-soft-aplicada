export interface Product {
  id: number;
  name: string;
  description?: string;
  price: number;
  stock: number;
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
