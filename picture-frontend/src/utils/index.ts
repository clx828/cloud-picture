import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { saveAs } from 'file-saver'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'

export const formatSize = (size: number) => {
  if (!size) {
    return '未知'
  }
  if (size < 1024) {
    return size + 'B'
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + 'KB'
  } else if (size < 1024 * 1024 * 1024) {
    return (size / (1024 * 1024)).toFixed(2) + 'MB'
  } else {
    return (size / (1024 * 1024 * 1024)).toFixed(2) + 'GB'
  }
}

export const hasEditAuth = (userId: number | string) => {
  console.log('hasEditAuth', userId)
  const loginUserStore = useLoginUserStore()
  return loginUserStore.loginUser.id === userId || loginUserStore.loginUser.userRole === 'admin'
}
export const useDownloadImage = () => {
  const router = useRouter()
  const loginUserStore = useLoginUserStore()

  const downloadImage = async (url: string, fileName?: string) => {
    if (!loginUserStore.loginUser.id) {
      message.error('请先登录')
      router.push('/user/login')
      return
    }
    if (!url) {
      return
    }
    saveAs(url, fileName)
  }

  return { downloadImage }
}

export const computeSize = (maxSize: number, totalSize: number) => {
  if (maxSize <= 0) {
    return 0
  }
  console.log('maxSize', maxSize, 'totalSize', totalSize)
  return parseFloat((maxSize - totalSize) / maxSize).toFixed(2)*100
}
