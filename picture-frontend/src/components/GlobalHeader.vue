<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <a-col flex="200px"
        ><router-link to="/">
          <div class="title-bar">
            <img class="logo" src="../../public/logo.png" alt="logo" />
            <div class="title">云图库</div>
          </div>
        </router-link></a-col
      >
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
      /></a-col>
      <a-col flex="120px">
        <div class="user-login-btn">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="goMySpace">
                    <UserOutlined />
                    我的空间
                  </a-menu-item>
                  <a-menu-item @click="doLogout">
                    <LoginOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login"> 登录 </a-button>
          </div>
        </div></a-col
      >
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, ref } from 'vue'
import {UserOutlined,  HomeOutlined, AppstoreOutlined, LoginOutlined,CloudUploadOutlined, CloudServerOutlined } from '@ant-design/icons-vue'
import { MenuProps, message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { logoutUsingPost } from '@/api/userController.ts'

const loginUserStore = useLoginUserStore()
const originItems = ref<MenuProps['items']>([
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '首页',
    title: '首页',
  },
  {
    key: '/add_picture',
    icon: () => h(CloudUploadOutlined),
    label: '创建图片',
    title: '创建图片',
  },
  {
    key: '/admin/userManage',
    icon: () => h(UserOutlined),
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/pictureManage',
    icon: () => h(AppstoreOutlined),
    label: '图片管理',
    title: '图片管理',
  },
  {
    key: '/admin/spaceManage',
    icon: () => h(CloudServerOutlined),
    label: '空间管理',
    title: '空间管理',
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
router.afterEach((to, from, next) => {
  current.value = [to.path]
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
const goMySpace = () => {
  router.push({
    path: '/my_space',
  })
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
