// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** health GET /api/health */
export async function healthUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseObject_>('/api/health', {
    method: 'GET',
    ...(options || {}),
  })
}
