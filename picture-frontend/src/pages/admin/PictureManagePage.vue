<template>
  <div id="pictureManagePage">
    <a-flex justify="space-between">
      <h2>图片管理</h2>
      <a-space>
        <a-button type="primary" href="/add_picture" target="_blank"><PlusOutlined />创建图片</a-button>
        <a-button type="primary" href="/add_picture/batch" target="_blank" ghost><CloudUploadOutlined />批量抓取图片</a-button>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px" />
    <a-form layout="inline" :model="searchParans" v-bind="formItemLayout">
      <a-form-item label="关键词">
        <a-input v-model:value="searchParans.searchText" placeholder="输入账号" allow-clear />
      </a-form-item>
      <a-form-item label="分类">
        <a-select
          style="width: 182px"
          :options="categoryOptions"
          v-model:value="searchParans.category"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="标签">
        <a-select
          style="width: 182px"
          :options="tagsOptions"
          mode="tags"
          v-model:value="searchParans.tags"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="状态">
        <a-select
          style="width: 182px"
          :options="PIC_REVIEW_STATUS_OPTIONS"
          v-model:value="searchParans.reviewStatus"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="doSearch">搜索</a-button>
      </a-form-item>
    </a-form>
    <div style="padding-bottom: 16px" />
    <!--    表格-->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'url'">
          <a-image :height="60" class="a-image" :src="record.url" />
        </template>
        <template v-else-if="column.dataIndex === 'category'">
          <div v-if="editableData[record.id]">
            <a-select :options="categoryOptions" v-model:value="editableData[record.id].category" />
          </div>
          <div v-else>
            <a-tag color="red"> {{ record.category }}</a-tag>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'tags'">
          <div v-if="editableData[record.id]">
            <a-select
              :options="tagsOptions"
              mode="tags"
              v-model:value="editableData[record.id].tags"
            />
          </div>
          <div v-else v-for="tag in record.tags">
            <a-tag color="green"> {{ tag }}</a-tag>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'picInfo'">
          <div>
            <div>格式:{{ record.picFormat }}</div>
            <div>宽度:{{ record.picWidth }}</div>
            <div>高度:{{ record.picHeight }}</div>
            <div>宽高比:{{ record.picScale }}</div>
            <div>大小:{{ (record.picSize / 1024).toFixed(2) }}KB</div>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'reviewStatus'">
          <div>
            <a-tag
              :color="
                record.reviewStatus === 0 ? 'orange' : record.reviewStatus === 1 ? 'green' : 'red'
              "
            >
              {{ PIC_REVIEW_STATUS_MAP[record.reviewStatus] }}
            </a-tag>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <div v-if="editableData[record.id]">
            <a-space wrap>
              <a-button style="margin-left: 5px" type="link" @click="doSave(record.id)">
                保存
              </a-button>
              <!--            //灰色按钮-->
              <a-button style="margin-left: 5px" type="dashed" @click="doCancel(record.id)">
                取消
              </a-button>
            </a-space>
          </div>
          <div v-else>
            <a-space wrap>
              <a-button
                type="link"
                v-if="record.reviewStatus === 0 || record.reviewStatus === 2"
                @click="doPictureReview(record.id, PIC_REVIEW_STATUS_ENUM.PASS)"
              >
                通过
              </a-button>
              <a-button
                type="link"
                v-if="record.reviewStatus === 0 || record.reviewStatus === 1"
                danger
                @click="handleReject(record.id)"
              >
                不通过
              </a-button>
              <a-button type="link" @click="doEdit(record.id)"> 编辑</a-button>

              <a-popconfirm
                title="您确定要删除这条数据吗？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="doDelete(record.id)"
              >
                <a-button style="margin-left: 5px" type="dashed" danger> 删除 </a-button>
              </a-popconfirm>
            </a-space>
          </div>
        </template>
        <template v-else-if="['name', 'introduction'].includes(column.dataIndex)">
          <div>
            <a-input
              v-if="editableData[record.id]"
              v-model:value="editableData[record.id][column.dataIndex]"
              style="margin: -5px 0"
            />
            <template v-else style="min-width: 180px">
              {{ record[column.dataIndex] }}
            </template>
          </div>
        </template>
      </template>
    </a-table>
    <!-- 在表格外部放置模态框 -->
    <a-modal
      ref="modalRef"
      v-model:open="isOpen"
      :wrap-style="{ overflow: 'hidden' }"
      @ok="handleRejectConfirm"
    >
      <a-textarea v-model:value="reviewReason" placeholder="请输入审核失败原因" />
      <template #title>
        <div ref="modalTitleRef" style="width: 100%; cursor: move">审核意见</div>
      </template>
      <template #modalRender="{ originVNode }">
        <div :style="transformStyle">
          <component :is="originVNode" />
        </div>
      </template>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import {PlusOutlined,CloudUploadOutlined} from '@ant-design/icons-vue';

import { cloneDeep } from 'lodash-es'
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  deletePictureUsingPost,
  doPictureReviewUsingPost,
  editPictureUsingPost,
  getPictureByPageUsingPost,
  getPictureTagCategoryUsingGet,
} from '@/api/pictureController.ts'
import {
  PIC_REVIEW_STATUS_ENUM,
  PIC_REVIEW_STATUS_MAP,
  PIC_REVIEW_STATUS_OPTIONS,
} from '@/constants/picture.ts'

const isOpen = ref(false)
const currentRecordId = ref<number>()
const reviewReason = ref<string>('')
const categoryOptions = ref<string>([])
const tagsOptions = ref<string>([])
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
  },

  {
    title: '图片',
    dataIndex: 'url',
  },
  {
    title: '名称',
    dataIndex: 'name',
  },
  {
    title: '简介',
    dataIndex: 'introduction',
  },
  {
    title: '分类',
    dataIndex: 'category',
  },
  {
    title: '标签',
    dataIndex: 'tags',
  },
  {
    title: '图片信息',
    dataIndex: 'picInfo',
  },
  {
    title: '编辑时间',
    dataIndex: 'createTime',
  },
  {
    title: '状态',
    dataIndex: 'reviewStatus',
  },
  {
    title: '操作',
    key: 'action',
  },
]
const dataList = ref<API.Picture[]>([])
const total = ref(0)
const editableData = reactive<Record<string, API.UserUpdateRequest>>({})
const searchParans = reactive<API.getPictureVOByIdUsingPOSTParams>({
  current: 1,
  pageSize: 10,
  searchText: '',
  category: '',
  reviewStatus: '',
  tags: [],
  sortField: 'create_time',
  sortOrder: 'descend',
})
const pagination = computed(() => {
  return {
    current: searchParans.current,
    pageSize: searchParans.pageSize,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})
const handleRejectConfirm = () => {
  if (!reviewReason.value.trim()) {
    message.warning('请输入审核不通过原因')
    return
  }

  // 调用审核函数，传入当前记录ID、不通过状态和审核理由
  doPictureReview(currentRecordId.value, PIC_REVIEW_STATUS_ENUM.REJECT, reviewReason.value)

  // 关闭模态框
  isOpen.value = false
}
const handleReject = (id: string | number) => {
  isOpen.value = true
  currentRecordId.value = id
}
const fetchData = async () => {
  try {
    const res = await getPictureByPageUsingPost(searchParans)
    if (res.data.code === 200) {
      dataList.value = res.data.data.records ?? []
      dataList.value.forEach((item) => {
        item.tags = JSON.parse(item.tags)
      })
      total.value = res.data.data.total ?? 0
    } else {
      message.error('获取用户列表失败,' + res.data.message)
      console.log(res.data.message)
    }
  } catch (e) {
    message.error('获取用户列表失败')
    console.log(e)
  }
}
const doTableChange = (page: any) => {
  searchParans.current = page.current
  searchParans.pageSize = page.pageSize
  fetchData()
}
const doSearch = () => {
  searchParans.current = 1
  fetchData()
}
const doDelete = async (id: String) => {
  try {
    const res = await deletePictureUsingPost({ id })
    if (res.data.code === 200) {
      message.success('删除成功')
      await fetchData()
    } else {
      message.error('删除失败,' + res.data.message)
    }
  } catch (e) {
    message.error('删除失败,' + e)
    console.log(e)
  }
}
const doEdit = (key: string) => {
  console.log('点击编辑', key)
  editableData[key] = cloneDeep(dataList.value.filter((item) => key === item.id)[0])
  console.log(editableData.value)
}
const doSave = async (key: string) => {
  try {
    const res = await editPictureUsingPost(editableData[key])
    if (res.data.code === 200) {
      message.success('保存成功')
      //移除editData中id
      delete editableData[key]
      fetchData()
    } else {
      message.error('保存失败,' + res.data.message)
    }
  } catch (e) {
    message.error('保存失败,' + e)
  }
}
const doCancel = (key: string) => {
  delete editableData[key]
}
const doPictureReview = async (id: string | number, status: number, reviewMessage?: string) => {
  try {
    const params = {
      id: id,
      reviewStatus: status,
      reviewMessage: reviewMessage ?? '',
    }
    const res = await doPictureReviewUsingPost(params)
    if (res.data.code === 200 && res.data.data === true) {
      message.success('操作成功')
      fetchData()
    } else {
      message.error('操作失败,' + res.data.message)
    }
  } catch (e) {
    message.error('操作失败,' + e)
  }
}
const getCategoryandTags = async () => {
  try {
    const res = await getPictureTagCategoryUsingGet()
    if (res.data.code === 200 && res.data.data) {
      const data = res.data.data
      categoryOptions.value = (data.categoryList ?? []).map((item) => {
        return {
          label: item,
          value: item,
        }
      })
      tagsOptions.value = (data.tagList ?? []).map((item) => {
        return {
          label: item,
          value: item,
        }
      })
    }
  } catch (e) {
    message.error('获取分类失败,' + e)
    console.log(e)
  }
}
onMounted(() => {
  fetchData()
  getCategoryandTags()
})
</script>

<style scoped>
#userManagePage {
  .a-image {
    max-height: 60px;
  }
}
</style>
