<template>
  <div class="picture-upload">
    <a-upload
      list-type="picture-card"
      :show-upload-list="false"
      :custom-request="handleUpload"
      :before-upload="beforeUpload"
    >
      <img class="uploadImg" v-if="picture?.url" :src="picture.url" alt="avatar" />
      <div v-else>
        <loading-outlined v-if="loading"></loading-outlined>
        <plus-outlined v-else></plus-outlined>
        <div class="ant-upload-text">点击或拖拽上传图片</div>
      </div>
    </a-upload>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import type { UploadProps } from 'ant-design-vue';
import { uploadPictureUsingPost } from '@/api/pictureController';
interface Props {
  picture?:API.PictureVO;
  onSuccess?: (picture: API.PictureVO) => void;
  spaceId: number;
}
const loading = ref(false);
const props = defineProps<Props>();
const handleUpload =async ({ file}:any) => {
  loading.value = true;
  try {
    const params = props.picture ? {id:props.picture.id}: {};
    const res = await uploadPictureUsingPost(params,{
      spaceId: props.spaceId,
    },file);
    if (res.data.code === 200 && res.data.data) {
      message.success('上传成功');
      props.onSuccess?.(res.data.data);
    } else {
      message.error(res.data.message);
    }
  }catch (e) {
    console.log(e)
    message.error('上传失败,' + e);
  }finally {
    loading.value = false;
  }
};
const beforeUpload = (file: UploadProps['fileList'][number]) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
  if (!isJpgOrPng) {
    message.error('不支持该格式的图片，推荐JPG 或 PNG格式');
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    message.error('图片必须于2MB!');
  }
  return isJpgOrPng && isLt2M;
};

</script>
<style scoped>
.picture-upload :deep(.ant-upload) {
  width: 100% !important;
  height: 100% !important;
  min-width: 152px;
  min-height: 152px;
}

.avatar-uploader > .ant-upload {
  width: 128px;
  height: 128px;
}
.ant-upload-select-picture-card i {
  font-size: 32px;
  color: #999;
}

.ant-upload-select-picture-card .ant-upload-text {
  margin-top: 8px;
  color: #666;
}
.uploadImg{
  max-width: 100%;
  max-height: 480px;
}
</style>
