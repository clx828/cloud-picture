# 智能协同云图库项目介绍

> 作者：橙子


## 项目介绍

基于 Vue 3 + Spring Boot + COS + WebSocket 的 **智能协同云图库平台**。

1）所有用户都可以在平台公开上传和检索图片素材，快速找到需要的图片。可用作表情包网站、设计素材网站、壁纸网站等：

![](https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/space/1911042795717275650/2025-05-06_a7b324bb-dfb3-4094-92b6-cd78d181218d.webp)

2）管理员可以上传、审核和管理图片，并对系统内的图片进行分析：

![](https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/space/1911042795717275650/2025-05-06_65036560-27e4-4666-8eb8-660957845054.webp)

3）对于个人用户，可将图片上传至私有空间进行批量管理、检索、编辑和分析，用作个人网盘、个人相册、作品集等：

![](https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/space/1911042795717275650/2025-05-06_cff667cd-5454-4cee-9cd3-35a3a091ff6e.webp)

4）对于企业，可开通团队空间并邀请成员，共享图片并 **实时协同编辑图片**，提高团队协作效率。可用于提供商业服务，如企业活动相册、企业内部素材库等：

5）对于上传的图片可以进行编辑，主要有两种编辑方式：

手动编辑：有放大、缩小、裁剪、旋转等功能
![](https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/public/1898708491314139137/2025-05-06_4f1e69e3-bfa7-44a5-83a4-f4f8bf67befc.webp)

AI编辑：有AI扩图、去水印、图像超分、线稿生成、图像上色、图片风格化六大功能
![](https://yun-picture-1253809168.cos.ap-guangzhou.myqcloud.com/public/1898708491314139137/2025-05-06_88effbca-8705-4681-bcec-69734a065d85.webp)


该项目功能丰富，涉及文件存管、内容检索、权限控制、实时协同等企业主流业务场景，并运用多种编程思想、架构设计方法和优化策略来保证项目的高速迭代和稳定运行。



项目架构设计图：

![](https://pic.yupi.icu/1/1732691889100-e562c709-cffa-477d-9329-1dc5ac1d35c8-20241204144304741-20241204145344935-20241204145354234.png)


## 技术选型

### 后端

- Java Spring Boot 框架
- MySQL 数据库 + MyBatis-Plus 框架 + MyBatis X 
- Redis 分布式缓存 + Caffeine 本地缓存
- Jsoup 数据抓取
- ⭐️ COS 对象存储
- ⭐️ ShardingSphere 分库分表
- ⭐️ Sa-Token 权限控制
- ⭐️ DDD 领域驱动设计
- ⭐️ WebSocket 双向通信
- ⭐️ Disruptor 高性能无锁队列
- ⭐️ JUC 并发和异步编程
- ⭐️ AI 绘图大模型接入
- ⭐️ 多种设计模式的运用
- ⭐️ 多角度项目优化：性能、成本、安全性等



### 前端

- Vue 3 框架
- Vite 打包工具
- Ant Design Vue 组件库
- Axios 请求库
- Pinia 全局状态管理
- 其他组件：数据可视化、图片编辑等
- ⭐️ 前端工程化：ESLint + Prettier + TypeScript
- ⭐️ OpenAPI 前端代码生成
