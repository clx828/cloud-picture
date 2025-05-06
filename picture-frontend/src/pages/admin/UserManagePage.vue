<template>
  <div id="userManagePage">
    <a-form layout="inline" :model="searchParans" v-bind="formItemLayout">
      <a-form-item label="账号">
        <a-input v-model:value="searchParans.userAccount" placeholder="输入账号" />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParans.userName" placeholder="输入用户名" />
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
        <template v-if="column.dataIndex === 'userAvater'">
          <a-avatar :src="record.userAvatar" />
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div>
            <a-tag color="green" v-if="record.userRole === 'admin'"> 管理员 </a-tag>
            <a-tag color="blue" v-else> 普通用户 </a-tag>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <div v-if="editableData[record.id]">
            <a-button type="primary" @click="doSave(record.id)"> 保存 </a-button>
            <a-button type="primary" @click="doCancel(record.id)"> 取消 </a-button>
            <a-button style="margin-left: 5px" type="primary" danger @click="doDelete(record.id)">
              删除
            </a-button>
          </div>
          <div v-else>
            <a-button type="primary" @click="doEdit(record.id)"> 编辑 </a-button
            >
            <a-button style="margin-left: 5px" type="primary" danger @click="doDelete(record.id)">
              删除
            </a-button>
          </div>
        </template>
        <template
          v-else-if="
            ['userRole', 'userProfile', 'userAccount', 'userName'].includes(column.dataIndex)
          "
        >
          <div>
            <a-input
              v-if="editableData[record.id]"
              v-model:value="editableData[record.id][column.dataIndex]"
              style="margin: -5px 0"
            />
            <template v-else>
              {{ record[column.dataIndex] }}
            </template>
          </div>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { cloneDeep } from 'lodash-es'
import { computed, onMounted, reactive, ref } from 'vue'
import {
  deleteByIdUsingPost,
  getUserVoByPageUsingPost,
  updateUserUsingPost
} from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
const columns = [
  {
    title: '头像',
    dataIndex: 'userAvater',
  },
  {
    title: 'ID',
    dataIndex: 'id',
  },

  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '注册时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]
const dataList = ref<API.UserVO[]>([])
const total = ref(0)
const editableData = reactive<Record<string, API.UserUpdateRequest>>({})
const searchParans = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 10,
  userAccount: '',
  userName: '',
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
const fetchData = async () => {
  try {
    const res = await getUserVoByPageUsingPost(searchParans)
    if (res.data.code === 200) {
      dataList.value = res.data.data.records ?? []
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
    const res = await deleteByIdUsingPost({ id })
    if (res.data.code === 200) {
      message.success('删除成功')
      fetchData()
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
    const res = await updateUserUsingPost(editableData[key])
    if (res.data.code === 200) {
      message.success('保存成功')
      fetchData()
    } else {
      message.error('保存失败,' + res.data.message)
    }
  }catch (e){
    message.error('保存失败,' + e)
  }
}
const doCancel = (key: string) => {
  delete editableData[key]
}
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#userManagePage {
}
</style>
