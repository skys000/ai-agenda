import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 120000
})

service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage({
        message: res.msg || '请求处理失败',
        type: 'error',
        duration: 5000
      })

      if ([401, 403, 50008, 50012, 50014].includes(res.code)) {
        localStorage.removeItem('token')
        router.push('/login')
      }
      return Promise.reject(new Error(res.msg || '请求处理失败'))
    }
    return res
  },
  error => {
    ElMessage({
      message: error.response?.data?.msg || error.message || '网络请求失败',
      type: 'error',
      duration: 5000
    })
    return Promise.reject(error)
  }
)

export default service
