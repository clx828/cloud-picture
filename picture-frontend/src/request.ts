import axios from 'axios'
import { message } from 'ant-design-vue'

const myAxios = axios.create({
  // baseURL: 'http://121.199.20.85:8888',
  baseURL: 'http://localhost:8888',
  timeout: 100000,
  withCredentials: true,
});

// 添加请求拦截器
myAxios.interceptors.request.use(function (config) {
  console.log(config.path)
  // 在发送请求之前做些什么
  return config;
}, function (error) {
  // 对请求错误做些什么
  return Promise.reject(error);
});

// 添加响应拦截器
myAxios.interceptors.response.use(function (response) {
  const res  = response;
if(res.data.code === 40100){
  if (
    !response.request.responseURL.includes('user/get/login') &&
    !window.location.pathname.includes('/user/login')
  ){
    message.warning('请先登录')
    window.location.href = `/user/login?redirect=${window.location.href}`
  }
}
  // 2xx 范围内的状态码都会触发该函数。
  // 对响应数据做点什么
  return response;
}, function (error) {
  // 超出 2xx 范围的状态码都会触发该函数。
  // 对响应错误做点什么
  return Promise.reject(error);
});

export default myAxios

