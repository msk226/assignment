export type DashboardStats = {
  date: string
  totalBudget: number
  usedBudget: number
  remainingBudget: number
  participants: number
  totalPoints: number
}

export type Budget = {
  date: string
  totalBudget: number
  usedBudget: number
}

export type RouletteParticipation = {
  id: number
  userId: number
  nickname?: string
  date: string
  points: number
  createdAt: string
}

export type Product = {
  id: number
  name: string
  price: number
  stock: number
  imageUrl?: string | null
}

export type Order = {
  id: number
  userId: number
  nickname?: string
  productId: number
  productName?: string
  pointsUsed: number
  status: 'COMPLETED' | 'CANCELLED'
  createdAt: string
}
