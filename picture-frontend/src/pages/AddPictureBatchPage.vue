<template>
  <div id="addPicturePage">
    <h2>批量创建图片</h2>
    <!--  图片信息表单-->
    <a-form
      name="pictureForm"
      layout="vertical"
      :model="pictureBatch"
      @finish="handleSubmit"
    >
      <a-form-item name="searchText" label="关键词">
        <a-input v-model:value="pictureBatch.searchText" placeholder="请输入图片关键词" allow-clear />
      </a-form-item>
      <a-form-item name="count" label="抓取数量">
        <a-input-number
          v-model:value="pictureBatch.count"
          placeholder="请输入抓取图片数量"
          allow-clear style="min-width: 180px"
          min="1"
          max="30"
        />
      </a-form-item>
      <a-form-item name="pictureName" label="图片名称前缀">
        <a-input v-model:value="pictureBatch.pictureName" placeholder="请输入图片名称前缀" allow-clear />
      </a-form-item>
      <a-form-item>
        <a-button html-type="submit" style="width: 100%" type="primary" :loading="loading">执行任务</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { uploadPictureByBatchUsingPost } from '@/api/pictureController.ts'
import router from '@/router'

const loading = ref(false)
const pictureBatch = ref<API.PictureUploadByBatchRequest>({
  searchText: '',
  count: 1,
  pictureName: '',
});
const handleSubmit = async () => {
  loading.value = true;
  try {
    const res = await uploadPictureByBatchUsingPost(pictureBatch.value);
    if (res.data.code === 200 && res.data.data) {
      message.success(`上传成功,共上传了:${res.data.data}条数据`);
      await router.push('/');
    } else {
      message.error(res.data.message);
    }
  } catch (e) {
    message.error('上传失败,' + e);
  }finally {
    loading.value = false;
  }
};

</script>

<style scoped>
#addPicturePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
