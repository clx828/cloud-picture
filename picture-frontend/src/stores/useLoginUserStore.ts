import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getLoginUserUsingGet } from '@/api/userController.ts'

export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.LoginUserVO>({
    userName:'未登录',
  })
  async function fetchLoginUser(){
    const res = await getLoginUserUsingGet()
    if(res.data.code === 200&&res.data.data){
      loginUser.value = res.data.data
    }
  }
  function setLoginUser(newLoginUser:any){
    loginUser.value = newLoginUser
  }
  function clearLoginUser(){
    loginUser.value = {
      userName:'未登录',
    }
  }

  return { loginUser, fetchLoginUser, setLoginUser, clearLoginUser }
})
