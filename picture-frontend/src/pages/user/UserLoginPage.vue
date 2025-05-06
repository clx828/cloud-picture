<template>
<div id="userLoginPage">
  <h2 class="title">云图库 - 用户登录</h2>
  <div class="desc">企业级免费智能云图库</div>
    <a-form
      :model="formState"
      name="basic"
      autocomplete="on"
      @finish="handleSubmit"
      @finishFailed="onFinishFailed"
    >
      <a-form-item
        name="userAccount"
        :rules="[{ required: true, message: '请输入账号' }]"
      >
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号"/>
      </a-form-item>

      <a-form-item
        name="userPassword"
        :rules="[{ required: true, message: '请输入密码!' },{min:8,message:'密码长度不能小于8位'}]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>
      <div class="tips">
        没有账号？
        <RouterLink to="/user/register">去注册</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">登录</a-button>
      </a-form-item>
    </a-form>
</div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue';
import { logoutUsingPost, userLoginUsingPost } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'
import router from '@/router'

//用于表单数据绑定
const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
});
const loginUserStore = useLoginUserStore();
/**
 * 表单提交
 * @param values
 */
const handleSubmit = async (values: any) => {
  try {
    const res = await userLoginUsingPost(formState);
    if (res.data.code === 200 && res.data.data) {
      await loginUserStore.fetchLoginUser();
      message.success("登录成功");
      router.push({
        path: '/',
        replace: true,
      })
    }else {
      message.error("登录失败"+res.data.message);
    }
  }catch (e){
    console.log(e)
    message.error("登录失败"+e);
  }

};

const onFinishFailed = (errorInfo: any) => {
  console.log('Failed:', errorInfo);
};
</script>

<style scoped>
#userLoginPage{
  max-width: 360px;
  margin: 0 auto;
  padding-top: 32px;
}
.title{
  text-align: center;
  margin-bottom: 16px;
}
.desc{
text-align: center;
  margin-bottom: 16px;
  color: #bbb;
}
.tips{
  text-align: right;
  font-size: 13px;
  margin-bottom: 16px;
  color: #bbb;
}
</style>
