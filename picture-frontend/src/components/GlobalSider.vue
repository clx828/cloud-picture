<template>
  <div id="globalSider">
    <a-layout-sider
      :collapsed="true"
      :trigger="null"
      width="200"
      :collapsedWidth="80"
      breakpoint="lg"
    >
      <a-menu
        v-model:selectedKeys="current"
        mode="inline"
        :items="items"
        @click="doMenuClick"
      >
      </a-menu>
    </a-layout-sider>
  </div>
</template>

<script lang="ts" setup>
import { computed, h, ref } from 'vue'
import {
  UserOutlined,
  PictureOutlined
} from '@ant-design/icons-vue'
import { MenuProps, message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { logoutUsingPost } from '@/api/userController.ts'

const loginUserStore = useLoginUserStore()
const originItems = ref<MenuProps['items']>([
  {
    key: '/',
    icon: () => h(PictureOutlined),
    label: '公共图库',
    title: '公共图库',
  },
  {
    key: '/my_space',
    icon: () => h(UserOutlined),
    label: '我的空间',
    title: '我的空间',
  },
])
//过滤菜单项
const filterMenus = (menus= [] as  MenuProps['items'])=>{
  return menus.filter((menu)=>{
    if(menu?.key?.startsWith('/admin')){
      const loginUser = loginUserStore.loginUser
      if(!loginUser ||loginUser.userRole!=='admin'){
        return false
      }
    }
    return true
  })
}
//过滤后的菜单
const items = computed(() => {
  return filterMenus(originItems.value)
})
const router = useRouter()
const doMenuClick = ({ key }) => {
  router.push({
    path: key,
  })
}
const current = ref<string[]>([])

// 修改后的路由钩子，添加对 /space/ 路径的特殊处理
router.afterEach((to, from, next) => {
  // 检查是否是 /space/ 开头的路径
  if (to.path.startsWith('/space/')) {
    current.value = ['/my_space'] // 如果是 /space/ 开头，则高亮 "我的空间" 菜单项
  } else {
    // 否则正常匹配路径
    current.value = [to.path]
  }
})

const doLogout = async () => {
  try {
    const res = await logoutUsingPost()
    if (res.data.code === 200) {
      message.success('退出成功')
      loginUserStore.clearLoginUser()
      await router.push({
        path: '/user/login',
        replace: true,
      })
    }
  } catch (e) {
    console.log(e)
    message.error('退出失败,' + e)
  }
}
</script>

<style scoped>
#globalHeader .title-bar {
  display: flex;
  align-content: center;
}
.title {
  color: black;
  font-size: 18px;
  margin-left: 16px;
}
.logo {
  width: 48px;
  height: 40px;
  margin-top: 10px;
}
</style>
