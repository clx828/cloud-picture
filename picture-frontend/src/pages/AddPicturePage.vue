<template>
  <div id="addPicturePage">
    <ImageCropper
      ref="imageCropperRef"
      :imageUrl="picture?.url"
      :picture="picture"
      :onSuccess="onSuccess"
      :spaceId="spaceId"
    />
    <ImageAiEdit
      ref="imageAiRef"
      :imageUrl="picture?.url"
      :picture="picture"
      :onSuccess="onSuccess"
      :spaceId="spaceId"
    />
    <h2>{{ route.query?.id ? '修改图片' : '创建图片' }}</h2>
    <div v-if="spaceId" class="space-badge" :class="spaceId.value ? 'public-space' : 'private-space'">
        <span class="space-icon">
          <i class="fas" :class="spaceId.value ? 'fa-globe' : 'fa-lock'"></i>
        </span>
      <span class="space-text">{{ spaceId.value ? '公共空间' : '私有空间' }}</span>
    </div>
    <!--  图片上传组件-->
    <a-tabs v-model:activeKey="activeKey">
      <a-tab-pane key="1" tab="本地上传">
        <PictureUpload :picture="picture" :spaceId="spaceId" :onSuccess="onSuccess" />
      </a-tab-pane>
      <a-tab-pane key="2" tab="URL上传">
        <!--    通过url上传图片-->
        <UrlPictureUpload :picture="picture" :spaceId="spaceId" :onSuccess="onSuccess" />
      </a-tab-pane>
    </a-tabs>
<!--    编辑图片-->
    <div class="editbar" v-if="picture">
      <a-button style="margin-right: 16px" :icon="h(EditOutlined)" @click="doEditPicture">手动编辑</a-button>
      <a-button type="primary" :icon="h(LinkOutlined)" @click="doAiEdit">AI编辑</a-button>
    </div>

    <!--  图片信息表单-->
    <a-form
      v-if="picture"
      name="pictureForm"
      layout="vertical"
      :model="pictureForm"
      @finish="handleSubmit"
    >
      <a-form-item name="name" label="名称">
        <a-input v-model:value="pictureForm.name" placeholder="请输入图片名称" allow-clear />
      </a-form-item>
      <a-form-item name="introduction" label="简介">
        <a-textarea
          v-model:value="pictureForm.introduction"
          placeholder="请输入图片简介"
          :auto-size="{ minRows: 2, maxRows: 6 }"
          allow-clear
        />
      </a-form-item>
      <a-form-item name="category" label="分类">
        <a-auto-complete
          :options="categoryOptions"
          v-model:value="pictureForm.category"
          placeholder="请输入分类"
          allow-clear
        />
        <a-form-item name="tags" label="标签">
          <a-select
            :options="tagsOptions"
            v-model:value="pictureForm.tags"
            mode="tags"
            placeholder="请输入标签"
            allow-clear
          />
        </a-form-item>
        <a-form-item>
          <a-button html-type="submit" type="primary" style="width: 100%">
            {{ route.query?.id ? '修改' : '创建' }}
          </a-button>
        </a-form-item>
      </a-form-item>
    </a-form>

  </div>
</template>

<script setup lang="ts">
import PictureUpload from '@/components/PictureUpload.vue'
import { computed, onMounted, reactive, ref ,h} from 'vue'
import Picture = API.Picture
import { useRoute, useRouter } from 'vue-router'
import {
  editPictureUsingPost,
  getPictureTagCategoryUsingGet,
  getPictureVoByIdUsingPost,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import UrlPictureUpload from '@/components/UrlPictureUpload.vue'
import ImageCropper from '@/components/ImageCropper.vue'
import { EditOutlined, LinkOutlined } from '@ant-design/icons-vue'
import ImageAiEdit from '@/components/ImageAiEdit.vue'

const imageCropperRef = ref()
const imageAiRef = ref()
const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureEditRequest>({})
const categoryOptions = ref<string[]>([])
const tagsOptions = ref<string[]>([])
const getPictureTagCategory = async () => {
  const res = await getPictureTagCategoryUsingGet()
  if (res.data.code === 200 && res.data.data) {
    categoryOptions.value = (res.data.data.categoryList ?? []).map((item) => {
      return {
        label: item,
        value: item,
      }
    })
    tagsOptions.value = (res.data.data.tagList ?? []).map((item) => {
      return {
        label: item,
        value: item,
      }
    })
  } else {
    message.error('获取分类失败,' + res.data.message)
  }
}
const onSuccess = (newPicture: Picture) => {
  picture.value = newPicture
  pictureForm.name = newPicture.name
}
const router = useRouter()
const route = useRoute()
const spaceId = computed(()=>{
  return route.query?.spaceId
})
const handleSubmit = async (values: any) => {
  const pictureId = picture.value?.id
  if (!pictureId) {
    return
  }
  try {
    const res = await editPictureUsingPost({
      id: pictureId,
      spaceId: spaceId.value,
      ...values,
    })
    if (res.data.code === 200 && res.data.data) {
      message.success('创建成功')
      await router.push({
        path: `/picture_detail/${pictureId}`,
        replace: true,
      })
    } else {
      message.error('创建失败,' + res.data.message)
    }
  } catch (e) {
    console.log(e)
    message.error('创建失败,' + e)
  }
}
const getOldPicture = async () => {
  const pictureId = route.query?.id
  console.log('这是id', pictureId)
  if (!pictureId) {
    return
  }
  try {
    const res = await getPictureVoByIdUsingPost({
      id: pictureId,
    })
    if (res.data.code === 200 && res.data.data) {
      const oldData = res.data.data
      picture.value = oldData
      pictureForm.name = oldData.name
      pictureForm.introduction = oldData.introduction
      pictureForm.category = oldData.category
      pictureForm.tags = oldData.tags
    } else {
      message.error('获取图片失败,' + res.data.message)
    }
  } catch (e) {
    console.log(e)
    message.error('获取图片失败,' + e)
  }
}
const doEditPicture = () => {
  imageCropperRef.value.openModal()
}
const doAiEdit = () => {
  imageAiRef.value.openModal()
}

onMounted(() => {
  getPictureTagCategory()
  getOldPicture()
})
</script>

<style scoped>
#addPicturePage {
  max-width: 720px;
  margin: 0 auto;
}
.editbar{
  margin-top: 16px;
  text-align: center;
  margin-left: 16px;
}
</style>
