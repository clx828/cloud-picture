<template>
  <div class="image-cropper">
    <a-modal
      class="image-cropper"
      v-model:visible="visible"
      title="编辑图片"
      :footer="false"
      @cancel="closeModal"
    >
    <vue-cropper
      ref="cropperRef"
      :img="imageUrl"
      output-type="png"
      :info="true"
      :can-move-box="true"
      :fixed-box="false"
      :auto-crop="true"
      :center-box="true"
    />
    <div style="margin-bottom: 16px" />
    <!-- 图片操作 -->
    <div class="image-cropper-actions">
      <a-space>
        <a-button @click="rotateLeft" >向左旋转</a-button>
        <a-button @click="rotateRight">向右旋转</a-button>
        <a-button @click="changeScale(1)" >放大</a-button>
        <a-button @click="changeScale(-1)" >缩小</a-button>
        <a-button type="primary" :loading="loading"  @click="handleConfirm"
        >确认
        </a-button>
      </a-space>
    </div>
    </a-modal>
  </div>

</template>

<script lang="ts" setup>
import {ref, } from 'vue'
import { PICTURE_EDIT_ACTION_ENUM,  } from '@/constants/picture.ts'
import 'vue-cropper/dist/index.css'
import { VueCropper }  from "vue-cropper";
import { message } from 'ant-design-vue'
import { uploadPictureUsingPost } from '@/api/pictureController'

interface Props {
  imageUrl?: string,
  picture?: API.PictureVO,
  spaceId?: number,
  onSuccess?: (picture: API.PictureVO) => void
}

const props = defineProps<Props>()

// 获取图片裁切器的引用
const cropperRef = ref()
const visible = ref(false)

const openModal = () => {
  visible.value = true
}
const closeModal = () => {
  visible.value = false
}

// 缩放比例
const changeScale = (num) => {
  cropperRef.value?.changeScale(num)
}

// 向左旋转
const rotateLeft = () => {
  cropperRef.value.rotateLeft()
}

// 向右旋转
const rotateRight = () => {
  cropperRef.value.rotateRight()
}

// 确认裁切
const handleConfirm = () => {
  cropperRef.value.getCropBlob((blob: Blob) => {
    // blob 为已经裁切好的文件
    const fileName = (props.picture?.name || 'image') + '.png'
    const file = new File([blob], fileName, { type: blob.type })
    // 上传图片
    handleUpload({ file })
  })
}

/**
 * 上传图片
 * @param file
 */
const handleUpload = async ({ file }: any) => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = props.picture ? { id: props.picture.id } : {}
    params.spaceId = props.spaceId
    const res = await uploadPictureUsingPost(params, {}, file)
    if (res.data.code === 200 && res.data.data) {
      message.success('图片上传成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
      closeModal()
    } else {
      message.error('图片上传失败，' + res.data.message)
    }
  } catch (error) {
    console.error('图片上传失败', error)
    message.error('图片上传失败，' + error.message)
  }
  loading.value = false
}
const loading = ref(false)
defineExpose({
  openModal,
})
</script>

<style>
.image-cropper {
  text-align: center;
}

.image-cropper .vue-cropper {
  height: 400px !important;
}
</style>
