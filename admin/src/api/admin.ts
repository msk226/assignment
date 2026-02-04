import { apiClient } from './client'
import type { Budget, DashboardStats, Order, Product, RouletteParticipation } from '../types/admin'

export async function fetchDashboard() {
  const { data } = await apiClient.get<DashboardStats>('/api/admin/dashboard')
  return data
}

export async function fetchBudget() {
  const { data } = await apiClient.get<Budget>('/api/admin/budget')
  return data
}

export async function updateBudget(totalBudget: number) {
  const { data } = await apiClient.put<Budget>('/api/admin/budget', { totalBudget })
  return data
}

export async function fetchRouletteParticipations() {
  const { data } = await apiClient.get<RouletteParticipation[]>('/api/admin/roulette')
  return data
}

export async function cancelRouletteParticipation(id: number) {
  await apiClient.delete(`/api/admin/roulette/${id}`)
}

export async function fetchProducts() {
  const { data } = await apiClient.get<Product[]>('/api/admin/products')
  return data
}

export async function createProduct(payload: Omit<Product, 'id'>) {
  const { data } = await apiClient.post<Product>('/api/admin/products', payload)
  return data
}

export async function updateProduct(id: number, payload: Omit<Product, 'id'>) {
  const { data } = await apiClient.put<Product>(`/api/admin/products/${id}`, payload)
  return data
}

export async function deleteProduct(id: number) {
  await apiClient.delete(`/api/admin/products/${id}`)
}

export async function fetchOrders() {
  const { data } = await apiClient.get<Order[]>('/api/admin/orders')
  return data
}

export async function cancelOrder(id: number) {
  await apiClient.delete(`/api/admin/orders/${id}`)
}
