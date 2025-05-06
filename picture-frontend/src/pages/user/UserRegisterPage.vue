<template>
  <div id="userLoginPage">
    <h2 class="title">云图库 - 用户注册</h2>
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
      <a-form-item
        name="checkPassword"
        :rules="[{ validator: validateConfirmPassword }]"
      >
        <a-input-password v-model:value="formState.checkPassword" placeholder="请再次输入密码" />
      </a-form-item>
      <div class="tips">
        已经有账号？
        <RouterLink to="/user/login">去登录</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue';
import { userRegisterUsingPost } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import router from '@/router'
//用于表单数据绑定
const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
});
/**
 * 表单提交
 * @param values
 */
const handleSubmit = async (values: any) => {
  try {
    const res = await userRegisterUsingPost(formState);
    if (res.data.code === 200 && res.data.data) {
      let count = 3;
      const key = 'registerSuccess';

      // 创建更新消息的函数
      const updateMessage = (currentCount) => {
        message.success({
          key,
          content: `注册成功！${currentCount}秒后跳转到登录`,
          duration: 0, // 不自动关闭
        });
      };

      // 显示初始消息
      updateMessage(count);

      // 设置倒计时
      const interval = setInterval(() => {
        count--;
        updateMessage(count);

        if (count === 0) {
          clearInterval(interval);
          message.destroy(key)
          router.push({
            path: '/user/login',
            replace: true,
          });
        }
      }, 1000);

    } else {
      message.error("注册失败!" + res.data.message);
    }
  } catch (e) {
    console.log(e);
    message.error("注册失败!" + e);
  }
};


const onFinishFailed = (errorInfo: any) => {
  console.log('Failed:', errorInfo);
};

const validateConfirmPassword = (_rule: any, value: string) => {
  if (!value) {
    return Promise.reject('请输入确认密码!');
  } else if (value.length < 8) {
    return Promise.reject('密码长度不能小于8位');
  } else if (value !== formState.userPassword) {
    return Promise.reject('两次输入的密码不一致');
  }
  return Promise.resolve();
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
