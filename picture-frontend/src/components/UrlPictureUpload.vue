<template>
  <div class="picture-display">
    <div class="upload-container">
      <a-input-group compact>

        <a-input v-model:value="uploadUrl" style="width: calc(100% - 120px)" placeholder="请输入图片地址(ps:需包含http:// 或 https://)"   />
        <a-button type="primary" style="width: 120px" @click="doUpload" :loading="loading">提交</a-button>
      </a-input-group>
    </div>
    <div class="image-container">

      <img class="display-img" v-if="picture?.url" :src="picture.url" alt="图片预览" />
      <div v-else class="empty-container">
        <picture-outlined />
        <div class="empty-text">暂无图片</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { PictureOutlined } from '@ant-design/icons-vue';
import { ref } from 'vue'
import { uploadPictureByUrlUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

const loading = ref(false);
const uploadUrl = ref<string>()
interface Props {
  picture?:API.PictureVO;
  onSuccess?: (picture: API.PictureVO) => void;
  spaceId?: number;
}
const props = defineProps<Props>();
const validateUrl = (url: string): boolean => {
  // 检查URL是否为空
  if (!url || url.trim() === '') {
    message.error('URL不能为空');
    return false;
  }

  // 使用正则表达式检查URL是否包含http或https协议
  const urlPattern = /^(https?:\/\/)/i;

  if (!urlPattern.test(url)) {
    message.error('URL必须包含http或https协议');
    return false;
  }

  return true;
};
const doUpload = async () => {
  loading.value = true;
  if (!validateUrl(uploadUrl.value)) {
    loading.value = false;
    return;
  }
  try {
    const res = await uploadPictureByUrlUsingPost({
      fileUrl: uploadUrl.value,
      spaceId: props.spaceId,
    })
    if (res.data.code === 200 && res.data.data) {
      message.success('上传成功');
      props.onSuccess?.(res.data.data);
    } else {
      message.error(res.data.message);
    }
  }catch (e){
    message.error('上传失败,' + e);
  }finally {
  loading.value = false;
  }

};
</script>

<style scoped>
.picture-display {
  width: 100%;
}
.upload-container {
  width: 100%;
  margin-bottom: 16px;
}

.image-container {
  width: 100%;
  height: 100%;
  min-width: 152px;
  min-height: 152px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
  border: 1px dashed #d9d9d9;
  border-radius: 10px;
  overflow: hidden;
  transition: border-color 0.3s;
}

.display-img {
  max-width: 100%;
  max-height: 480px;
  object-fit: contain;
}

.empty-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #999;
}

.empty-container > span {
  font-size: 32px;
  margin-bottom: 8px;
}

.empty-text {
  font-size: 14px;
  color: #666;
}
</style>
