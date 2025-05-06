<template>
  <div id="spaceManagePage">
    <a-flex justify="space-between">
      <h2>空间管理</h2>
      <a-space>
        <a-button type="primary" href="/add_space" target="_blank"
          ><PlusOutlined />创建空间</a-button
        >
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px" />
    <a-form layout="inline" :model="searchParans" v-bind="formItemLayout">
      <a-form-item label="空间名称">
        <a-input v-model:value="searchParans.spaceName" placeholder="输入空间名称" allow-clear />
      </a-form-item>
      <a-form-item label="用户ID">
        <a-input v-model:value="searchParans.spaceName" placeholder="输入用户ID" allow-clear />
      </a-form-item>
      <a-form-item label="级别">
        <a-select
          style="width: 182px"
          :options="SPACE_LEVEL_OPTIONS"
          v-model:value="searchParans.spaceLevel"
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
        <template v-else-if="column.dataIndex === 'spaceName'">
              {{ record[column.dataIndex] }}
        </template>
        <template v-else-if="column.dataIndex === 'spaceMaxUse'">
          <div>
            <div>空间大小:{{ formatSize(record.maxSize) }}</div>
            <div>图片数量:{{ record.maxCount }}张</div>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'spaceUsed'">
          <a-progress
            type="circle"
            :stroke-color="{
              '0%': '#108ee9',
              '100%': '#87d068',
            }"
            :percent="computeSize(record.maxSize, record.totalSize)"
            :size="40"
            :status="computeSize(record.maxSize, record.totalSize) === 100 ? 'normal' : 'active'"
          />
        </template>
        <template v-else-if="column.dataIndex === 'spaceLevel'">
          <div>
            <a-tag
              :color="
                record.spaceLevel === 0 ? 'grey' : record.spaceLevel === 1 ? 'green' : 'orange'
              "
            >
              {{ SPACE_LEVEL_MAP[record.spaceLevel] }}
            </a-tag>
          </div>
        </template>

        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.dataIndex === 'userVO'">
          <div>
            <a-flex align="center" gap="small">
              <a-avatar
                :alt="record.userVO?.userName"
                :src="record.userVO?.userAvatar"
                :size="40"
                style="border: 2px solid #f0f0f0; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09)"
              />
              <div style="font-weight: 500; font-size: 16px; color: #262626; margin-left: -4px">
                {{ record.userVO?.userName ?? '未知' }}
              </div>
            </a-flex>
          </div>
        </template>
        <template v-else-if="column.key === 'action'">
          <div >
            <a-space wrap>
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
        <template v-else-if="['spaceName', 'introduction'].includes(column.dataIndex)">
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
        <a-modal
          ref="modalRef"
          v-model:open="isOpen"
          :wrap-style="{ overflow: 'hidden' }"
          @ok="doSave"
          @cancel="doCancel"
        >
          <a-form :model="editableData" :label-col="labelCol" :wrapper-col="wrapperCol">
            <a-form-item label="空间名称">
              <a-input
                v-model:value="editableData.spaceName"
                placeholder="请输入空间名称"
                allow-clear
              />
            </a-form-item>

            <!-- 空间级别使用 radio -->
            <a-form-item label="空间级别">
              <a-radio-group v-model:value="editableData.spaceLevel" @change="changeLevel">
                <a-radio v-for="item in SPACE_LEVEL_OPTIONS" :key="item.value" :value="item.value">
                  {{ item.label }}
                </a-radio>
              </a-radio-group>
            </a-form-item>

            <!-- 空间大小（使用 flex 布局让输入框和单位同行） -->
            <a-form-item label="空间大小">
              <div style="display: flex; align-items: center; gap: 8px">
                <a-input
                  disabled
                  v-model:value="editableData.maxSize"
                  :min="0"
                  :step="1"
                  placeholder="请输入空间大小"
                  allow-clear
                />
                <span>MB</span>
              </div>
            </a-form-item>

            <!-- 图片数量（同样使用 flex 布局） -->
            <a-form-item label="图片数量">
              <div style="display: flex; align-items: center; gap: 8px">
                <a-input
                  disabled
                  v-model:value="editableData.maxCount"
                  :min="0"
                  :step="1"
                  placeholder="请输入空间数量"
                  allow-clear
                />
                <span>张</span>
              </div>
            </a-form-item>
          </a-form>
          <template #title>
            <div ref="modalTitleRef" style="width: 100%; cursor: move">编辑空间</div>
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
import { PlusOutlined, CloudUploadOutlined } from '@ant-design/icons-vue'

import { cloneDeep } from 'lodash-es'
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  deleteSpaceUsingPost,
  editSpaceUsingPost,
  getSpaceVoPageUsingPost,
  updateSpaceUsingPost
} from '@/api/spaceController.ts'
import { SPACE_LEVEL_ENUM, SPACE_LEVEL_MAP, SPACE_LEVEL_OPTIONS } from '@/constants/space.ts'
import { computeSize, formatSize } from '../../utils'
import { updateUserUsingPost } from '@/api/userController'

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
    title: '名称',
    dataIndex: 'spaceName',
  },
  {
    title: '空间级别',
    dataIndex: 'spaceLevel',
  },
  {
    title: '空间所属',
    dataIndex: 'userVO',
  },
  {
    title: '空间额度',
    dataIndex: 'spaceMaxUse',
  },
  {
    title: '剩余容量',
    dataIndex: 'spaceUsed',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]
const dataList = ref<API.SpaceVO[]>([])
const total = ref(0)
const editableData = ref<API.SpaceUpdateRequest>({})
const searchParans = reactive<API.SpaceQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'create_time',
  sortOrder: 'descend',
  spaceLevel: null,
  spaceName: '',
  spaceType: null,
  userId: null,
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
const fetchData = async () => {
  try {
    const res = await getSpaceVoPageUsingPost(searchParans)
    if (res.data.code === 200) {
      dataList.value = res.data.data.records ?? []
      total.value = res.data.data.total ?? 0
    } else {
      message.error('获取空间列表失败,' + res.data.message)
      console.log(res.data.message)
    }
  } catch (e) {
    message.error('获取空间列表失败')
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
    const res = await deleteSpaceUsingPost({id})
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
  isOpen.value = true
  editableData.value = cloneDeep(dataList.value.filter((item) => key === item.id)[0])
  editableData.value.maxSize = (editableData.value.maxSize)/(1024*1024)
  console.log(editableData.value)
}
const doSave = async (key: string) => {
  try {
    const res = await updateSpaceUsingPost(editableData.value)
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
  }finally {
    isOpen.value = false
    editableData.value = {}
  }
}
const doCancel = () => {
  editableData.value = {}
}
const changeLevel = ()=>{
  switch (editableData.value.spaceLevel){
    case 0:
      editableData.value.maxSize = 100
      editableData.value.maxCount = 100
      break
    case 1:
      editableData.value.maxSize = 1000
      editableData.value.maxCount = 1000
      break
    case 2:
      editableData.value.maxSize = 10000
      editableData.value.maxCount = 10000
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#userManagePage {
  .a-image {
    max-height: 60px;
  }
}
</style>
