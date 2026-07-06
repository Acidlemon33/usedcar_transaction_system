const JSON_HEADERS = { 'Content-Type': 'application/json' }
const BACKEND_BASE_URL = 'http://localhost:8080'

function getApiBase() {
  if (typeof window !== 'undefined' && window.location?.protocol === 'file:') {
    return BACKEND_BASE_URL
  }
  return '/api'
}

async function request(path, options = {}) {
  try {
    const baseUrl = getApiBase()
    const url = `${baseUrl}${path}`

    console.log(`[API] ${options.method || 'GET'} ${url}`, options.body || '')

    const response = await fetch(url, {
      ...options,
      headers: {
        ...JSON_HEADERS,
        ...(options.headers || {})
      }
    })

    const contentType = response.headers.get('content-type') || ''
    let data

    if (contentType.includes('application/json')) {
      data = await response.json()
    } else {
      const text = await response.text()
      console.warn(`[API] Non-JSON response (${response.status}):`, text)
      data = { code: response.status, message: text || '请求失败' }
    }

    console.log(`[API] Response ${response.status}:`, data)

    if (!response.ok) {
      return {
        code: response.status,
        message: data?.message || '请求失败'
      }
    }

    return data
  } catch (error) {
    console.error('[API] Network error:', error)
    return {
      code: 500,
      message: '无法连接到后端，请确认后端已启动。'
    }
  }
}

export const api = {
  async getCars(pageNum = 1, pageSize = 100, sellerId = null) {
    let url = `/carInfo/page?pageNum=${pageNum}&pageSize=${pageSize}`
    if (sellerId) url += `&sellerId=${sellerId}`
    return request(url)
  },

  async createCar(payload) {
    return request('/carInfo/add', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  async updateCar(payload) {
    return request('/carInfo', {
      method: 'PUT',
      body: JSON.stringify(payload)
    })
  },
  async deleteCar(carId) {
    return request(`/carInfo/${carId}`, {
      method: 'DELETE'
    })
  },

  async getSales(pageNum = 1, pageSize = 10) {
    return request(`/saleInfo/page?pageNum=${pageNum}&pageSize=${pageSize}`)
  },
  async getMySales(salerId) {
    return request(`/saleInfo/my?salerId=${salerId}`)
  },

  async createSale(payload) {
    return request('/saleInfo/publish', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },

  async getUsers(pageNum = 1, pageSize = 10) {
    return request(`/user/page?pageNum=${pageNum}&pageSize=${pageSize}`)
  },

  async login(payload, role = 'user') {
    const path = role === 'admin' ? '/admin/login' : '/user/login'
    return request(path, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },

  async register(payload, role = 'user') {
    const path = role === 'admin' ? '/admin/register' : '/user/register'
    return request(path, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },

  async getCredit(userId) {
    return request(`/user/${userId}/credit`)
  },

  async getOrders(pageNum = 1, pageSize = 10) {
    return request(`/transaction_order/page?pageNum=${pageNum}&pageSize=${pageSize}`)
  },

  async createOrder(payload) {
    return request('/transaction_order', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },

  async getNotices(pageNum = 1, pageSize = 10) {
    return request(`/notice/page?pageNum=${pageNum}&pageSize=${pageSize}`)
  },

  async predictPrice(carId, userId) {
    return request(`/ml_predict/predict_price?carId=${carId}&userId=${userId}`, {
      method: 'POST'
    })
  },

  // 收藏
  async addFavorite(userId, carId) {
    return request('/favorite', {
      method: 'POST',
      body: JSON.stringify({ userId, carId })
    })
  },
  async removeFavorite(favoriteId) {
    return request(`/favorite/${favoriteId}`, {
      method: 'DELETE'
    })
  },
  async getFavorites(userId, pageNum = 1, pageSize = 50) {
    return request(`/favorite/page?userId=${userId}&pageNum=${pageNum}&pageSize=${pageSize}`)
  },
  async checkFavorite(userId, carId) {
    return request(`/favorite/page?userId=${userId}&carId=${carId}&pageSize=1`)
  }
}
