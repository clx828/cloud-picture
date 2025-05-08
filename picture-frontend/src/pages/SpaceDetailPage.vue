<template>
  <div id="spaceDetailPage" class="space-detail-container">
    <!-- 顶部信息栏 -->
    <div class="header-section">
      <a-flex justify="space-between" align="center">
        <div class="space-title">
          <h1>{{ space.spaceName }} <span class="space-type-badge">{{ SPACE_TYPE_MAP[space.spaceType] }}</span></h1>
          <p v-if="space.description" class="space-description">{{ space.description }}</p>
        </div>
        <a-space size="middle" class="action-buttons">
          <a-button
            type="primary"
            :href="`/add_picture?spaceId=${id}`"
            target="_blank"
            class="primary-action-btn"
          >
            <PlusOutlined /> 创建图片
          </a-button>
          <a-button
            type="primary"
            ghost
            :icon="h(TeamOutlined)"
            :href="`/spaceUserManage/${id}`"
            target="_blank"
            class="secondary-action-btn"
          >
            成员管理
          </a-button>
          <a-button

            type="primary"
            ghost
            :icon="h(BarChartOutlined)"
            :href="`/space_analyze?spaceId=${id}`"
            target="_blank"
            class="secondary-action-btn"
          >
            空间分析
          </a-button>
          <a-button
            v-if="canEditPicture"
            :icon="h(EditOutlined)"
            @click="doBatchEdit"
            class="action-btn"
          >
            批量编辑
          </a-button>
          <a-tooltip
            :title="`占用空间 ${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`"
            placement="bottom"
          >
            <div class="storage-usage">
              <a-progress
                type="circle"
                :size="48"
                :percent="((space.totalSize * 100) / space.maxSize).toFixed(1)"
                :strokeColor="getProgressColor()"
                :strokeWidth="8"
              />
            </div>
          </a-tooltip>
        </a-space>
      </a-flex>
    </div>

    <!-- 工具栏：显示统计和过滤选项 -->
    <div class="toolbar-section">
      <a-row justify="space-between" align="middle">
        <a-col :xs="24" :sm="8">
          <div class="stats-display">
            <span class="stat-item">
              <PictureOutlined /> {{ total }} 张图片
            </span>
            <span class="stat-item" v-if="space.createTime">
              <ClockCircleOutlined /> 创建于 {{ formatDate(space.createTime) }}
            </span>
          </div>
        </a-col>
        <a-col :xs="24" :sm="16">
          <div class="filter-controls">
            <a-input-search
              v-model:value="searchKeyword"
              placeholder="搜索图片..."
              enter-button
              @search="onKeywordSearch"
              class="search-input"
              allow-clear
            />
            <a-select
              v-model:value="searchParams.sortField"
              style="width: 140px"
              @change="onSortChange"
              class="sort-select"
            >
              <a-select-option value="create_time">按时间排序</a-select-option>
              <a-select-option value="name">按名称排序</a-select-option>
              <a-select-option value="pic_size">按大小排序</a-select-option>
            </a-select>
            <a-select
              v-model:value="searchParams.sortOrder"
              style="width: 100px"
              @change="onSortOrderChange"
              class="order-select"
            >
              <a-select-option value="ascend">升序</a-select-option>
              <a-select-option value="descend">降序</a-select-option>
            </a-select>
            <a-radio-group v-model:value="viewMode" button-style="solid" class="view-mode-toggle">
              <a-radio-button value="grid"><AppstoreOutlined /></a-radio-button>
              <a-radio-button value="list"><UnorderedListOutlined /></a-radio-button>
            </a-radio-group>
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 图片列表 -->
    <div class="gallery-section" :class="{ 'list-view': viewMode === 'list' }">
      <a-spin :spinning="loading" tip="加载中..." class="loading-spinner">
        <PictureList
          :dataList="dataList"
          :loading="loading"
          :showOp="true"
          :canEdit="canEditPicture"
          :canDelete="canDeletePicture"
          :onReload="fetchData"
          :viewMode="viewMode"
        />

        <!-- 空状态展示 -->
        <a-empty
          v-if="!loading && dataList.length === 0"
          description="暂无图片"
          class="empty-state"
        >
          <template #image>
<!--            <img src="/assets/empty-gallery.svg" alt="暂无图片" class="empty-image" />-->
          </template>
          <a-button
            type="primary"
            :href="`/add_picture?spaceId=${id}`"
            target="_blank"
          >
            上传第一张图片
          </a-button>
        </a-empty>
      </a-spin>
    </div>

    <!-- 分页 -->
    <div class="pagination-section">
      <a-pagination
        v-model:current="searchParams.current"
        v-model:pageSize="searchParams.pageSize"
        :total="total"
        :showSizeChanger="true"
        :pageSizeOptions="['12', '24', '36', '48']"
        :showTotal="total => `共 ${total} 张图片`"
        @change="onPageChange"
        @showSizeChange="onSizeChange"
      />
    </div>

  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref, watch } from 'vue'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import {
  getPictureByPageUsingPost,
} from '@/api/pictureController.ts'
import { formatSize } from '@/utils'
import PictureList from '@/components/PictureList.vue'
import ShareModal from '@/components/PictureList.vue'

import {
  BarChartOutlined,
  EditOutlined,
  TeamOutlined,
  PlusOutlined,
  PictureOutlined,
  ClockCircleOutlined,
  AppstoreOutlined,
  UnorderedListOutlined
} from '@ant-design/icons-vue'
import { SPACE_PERMISSION_ENUM, SPACE_TYPE_MAP } from '@/constants/space'

interface Props {
  id: string | number
}

const props = defineProps<Props>()
const space = ref<API.SpaceVO>({})
const viewMode = ref('grid') // 'grid' or 'list'
const searchKeyword = ref('')


// 通用权限检查函数
function createPermissionChecker(permission: string) {
  return computed(() => {
    return (space.value.permissionList ?? []).includes(permission)
  })
}

// 定义权限检查
const canManageSpaceUser = createPermissionChecker(SPACE_PERMISSION_ENUM.SPACE_USER_MANAGE)
const canUploadPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_UPLOAD)
const canEditPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDeletePicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)

// 获取进度条颜色
const getProgressColor = () => {
  const percent = ((space.value.totalSize || 0) * 100) / (space.value.maxSize || 1)
  if (percent > 90) return '#ff4d4f' // 红色 - 快满了
  if (percent > 70) return '#faad14' // 黄色 - 警告
  return '#52c41a' // 绿色 - 正常
}

// 格式化日期
const formatDate = (timestamp: string | number) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// -------- 获取空间详情 --------
const fetchSpaceDetail = async () => {
  try {
    const res = await getSpaceVoByIdUsingGet(
      {
        id: props.id,
      },
    )
    if (res.data.code === 200 && res.data.data) {
      space.value = res.data.data
    } else {
      message.error('获取空间详情失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取空间详情失败：' + e.message)
  }
}

onMounted(() => {
  fetchSpaceDetail()
})

// --------- 获取图片列表 --------

// 定义数据
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

// 搜索条件
const searchParams = ref<API.PictureQueryRequest>({
  current: 1,
  pageSize: 24,
  sortField: 'create_time',
  sortOrder: 'descend',
  searchText: '',
})

// 关键字搜索
const onKeywordSearch = (value: string) => {
  searchParams.value.searchText = value
  searchParams.value.current = 1
  fetchData()
}

// 排序改变
const onSortChange = (value: string) => {
  searchParams.value.sortField = value
  fetchData()
}

// 排序顺序改变
const onSortOrderChange = (value: string) => {
  searchParams.value.sortOrder = value
  fetchData()
}

// 页面大小改变
const onSizeChange = (current: number, size: number) => {
  searchParams.value.pageSize = size
  fetchData()
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params = {
    spaceId: props.id,
    ...searchParams.value,
  }
  const res = await getPictureByPageUsingPost(params)
  if (res.data.code === 200 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

// 页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
})

// 分页参数
const onPageChange = (page: number, pageSize: number) => {
  searchParams.value.current = page
  searchParams.value.pageSize = pageSize
  fetchData()
}

// 搜索
const onSearch = (newSearchParams: API.PictureQueryRequest) => {
  searchParams.value = {
    ...searchParams.value,
    ...newSearchParams,
    current: 1,
  }
  fetchData()
}

// 批量编辑（需要根据实际需求实现）
const doBatchEdit = () => {
  message.info('批量编辑功能正在开发中')
  // 实现批量编辑逻辑
}

// 空间 id 改变时，必须重新获取数据
watch(
  () => props.id,
  (newSpaceId) => {
    fetchSpaceDetail()
    fetchData()
  },
)
</script>

<style scoped>
.space-detail-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 24px;
  margin-bottom: 24px;
}

.header-section {
  margin-bottom: 28px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.space-title {
  display: flex;
  flex-direction: column;
}

.space-title h1 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #262626;
}

.space-type-badge {
  display: inline-block;
  font-size: 14px;
  font-weight: normal;
  background-color: #e6f7ff;
  color: #1890ff;
  padding: 2px 8px;
  border-radius: 4px;
  margin-left: 8px;
}

.space-description {
  color: #595959;
  font-size: 14px;
  margin-top: 4px;
  margin-bottom: 0;
}

.action-buttons {
  flex-wrap: wrap;
}

.primary-action-btn {
  font-weight: 500;
}

.secondary-action-btn {
  border-color: #1890ff;
}

.storage-usage {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 8px;
}

.toolbar-section {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #fafafa;
  border-radius: 6px;
}

.stats-display {
  display: flex;
  align-items: center;
}

.stat-item {
  display: inline-flex;
  align-items: center;
  margin-right: 16px;
  color: #595959;
}

.stat-item :deep(.anticon) {
  margin-right: 4px;
  color: #1890ff;
}

.filter-controls {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.search-input {
  width: 220px;
}

.view-mode-toggle {
  margin-left: 8px;
}

.gallery-section {
  min-height: 400px;
}

.gallery-section.list-view {
  /* 列表视图样式 */
}

.loading-spinner {
  width: 100%;
}

.empty-state {
  margin: 60px 0;
  padding: 24px;
}

.empty-image {
  height: 160px;
}

.pagination-section {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .filter-controls {
    justify-content: flex-start;
    margin-top: 16px;
  }

  .space-title h1 {
    font-size: 20px;
  }

  .action-buttons {
    margin-top: 16px;
  }
}
</style>
