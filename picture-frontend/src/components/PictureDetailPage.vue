<template>
  <div id="pictureDetailPage">
    <a-row :gutter="[16,16]" >
<!--      图片预览-->
      <a-col :sm="24" :md="16" :xl="18">
        <a-card title="图片预览">
          <a-image :alt="picture.name" :src="picture.url" style="max-height: 600px;object-fit: contain" />
        </a-card>
      </a-col>
<!--      图片编辑-->
      <a-col :span="24" :md="8" :xl="6" >
      <a-card>
        <a-descriptions title="图片信息" :column="1">
          <a-descriptions-item label="图片名称">{{ picture.name ?? '未命名' }}</a-descriptions-item>
          <a-descriptions-item label="图片描述">{{ picture.introduction ?? '暂无描述' }}</a-descriptions-item>
          <a-descriptions-item label="图片作者">
            <a-flex align="center" gap="small">
              <a-avatar
                :alt="picture.user?.userName"
                :src="picture.user?.userAvatar"
                :size="40"
                style="border: 2px solid #f0f0f0; box-shadow: 0 2px 8px rgba(0,0,0,0.09);" />
              <div style="font-weight: 500; font-size: 16px; color: #262626; margin-left: -4px;">
                {{ picture.user?.userName ?? '未知' }}
              </div>
            </a-flex>
          </a-descriptions-item>
          <a-descriptions-item label="图片分类">
            <a-tag color="orange">{{picture.category ?? '未分类' }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="图片标签">
            <a-tag color="green" v-for="tag in picture.tags" :key="tag">{{ tag }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="图片格式">{{ picture.picFormat }}</a-descriptions-item>
          <a-descriptions-item label="图片宽高">{{ picture.picWidth }} x {{ picture.picHeight }}</a-descriptions-item>
          <a-descriptions-item label="图片大小">{{ formatSize(picture.picSize) }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ dayjs(picture.createTime).format('YYYY-MM-DD HH:mm:ss') }}</a-descriptions-item>
          <a-descriptions-item >
            <a-button style="margin-right: 5px" type="primary" :icon="h(DownloadOutlined)" @click="doDownload">
              免费下载
            </a-button>
            <a-button v-if="hasAuth" style="margin-right: 5px" :icon="h(EditOutlined)" type="primary" @click="doEdit">
              编辑
            </a-button>
            <a-button v-if="hasAuth" :icon="h(DeleteOutlined)" danger type="dashed"  @click="doDelete">
              删除
            </a-button>
          </a-descriptions-item>
        </a-descriptions>
      </a-card>
      </a-col>
    </a-row>

  </div>
</template>

<script setup lang="ts">
import { onMounted, ref ,h } from 'vue'
import { message } from 'ant-design-vue'
import { deletePictureUsingPost, getPictureVoByIdUsingPost } from '@/api/pictureController.ts'
import dayjs from 'dayjs'
import { formatSize, hasEditAuth, useDownloadImage } from '@/utils'
import {DownloadOutlined,EditOutlined,DeleteOutlined} from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'

interface Props {
  id:string | number
}
const props = defineProps<Props>()
const picture = ref<API.PictureVO>({})
const hasAuth = ref<boolean>()
const { downloadImage } = useDownloadImage()
const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingPost({
      id: props.id
    })
    if (res.data.code === 200 && res.data.data) {
      picture.value = res.data.data
      hasAuth.value = hasEditAuth(picture.value.userId)
    } else {
      message.error('获取图片信息失败,' + res.data.message)
      console.log(res.data.message)
    }
  }catch (e){
    console.log(e)
    message.error('图片信息获取失败,' + e)
  }
}
const router = useRouter()
const doDownload = async () => {
  try {
    await downloadImage(picture.value.url, picture.value.name)
  } catch (e) {
    console.log(e)
    message.error('下载失败,' + e)
  }
}
const doEdit = async () => {
  if (!hasAuth) {
    message.error('您没有权限编辑该图片')
    return
  }
  await router.push({
    path: '/add_picture',
    query: { id: props.id }
  })
}
const doDelete = async () => {
  if (!hasAuth) {
    message.error('您没有权限编辑该图片')
    return
  }
  try {
    const res = await deletePictureUsingPost({ id: props.id })
    if (res.data.code === 200) {
      message.success('删除成功')
      await router.push('/')
    } else {
      message.error('删除失败,' + res.data.message)
    }
  } catch (e) {
    message.error('删除失败,' + e)
    console.log(e)
  }
}

onMounted(()=>{
  fetchPictureDetail()
})
</script>

<style scoped>
#pictureDetailPage {
  margin-bottom: 20px;
  .search-bar {
    max-width: 480px;
    margin: 0 auto 16px;
  }
  .tag-bar {
    margin-bottom: 16px;
  }
}
</style>
