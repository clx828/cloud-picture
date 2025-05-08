// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** getSpaceCategoryAnalyze POST /api/space/analyze/category */
export async function getSpaceCategoryAnalyzeUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSpaceCategoryAnalyzeUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListSpaceCategoryAnalyzeResponse_>('/api/space/analyze/category', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** getSpaceRankAnalyze POST /api/space/analyze/rank */
export async function getSpaceRankAnalyzeUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSpaceRankAnalyzeUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListSpace_>('/api/space/analyze/rank', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** getSpaceSizeAnalyze POST /api/space/analyze/size */
export async function getSpaceSizeAnalyzeUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSpaceSizeAnalyzeUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListSpaceSizeAnalyzeResponse_>('/api/space/analyze/size', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** getSpaceTagAnalyze POST /api/space/analyze/tag */
export async function getSpaceTagAnalyzeUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSpaceTagAnalyzeUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListSpaceTagAnalyzeAResponse_>('/api/space/analyze/tag', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** getSpaceUsageAnalyze POST /api/space/analyze/usage */
export async function getSpaceUsageAnalyzeUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSpaceUsageAnalyzeUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseSpaceUsageAnalyzeResponse_>('/api/space/analyze/usage', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** getSpaceUserAnalyze POST /api/space/analyze/user */
export async function getSpaceUserAnalyzeUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSpaceUserAnalyzeUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListSpaceUserAnalyzeResponse_>('/api/space/analyze/user', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
