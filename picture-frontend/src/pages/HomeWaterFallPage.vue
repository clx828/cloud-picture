<template>
  <div id="homePage">
    <div>
      <div class="search-bar">
        <a-input-search
          v-model:value="searchText"
          placeholder="从海量图片中搜索"
          enter-button="搜索"
          size="large"
          @search="doSearch"
        />
      </div>
      <!--    标签分类-->
      <div>
        <a-tabs v-model:active-key="selectCategoryList" @change="doSearch()">
          <a-tab-pane key="all" tab="全部" />
          <a-tab-pane v-for="category in categoryList"  :tab="category" :key="category" @change="doSearch" />
        </a-tabs>
      </div>
      <div class="tag-bar">
        <span style="margin-right: 8px">标签:</span>
        <a-space :size="[0, 8]" wrap>
          <a-checkable-tag
            v-for="(tag, index) in tagList"
            :key="tag"
            v-model:checked="selectTagList[index]"
            @change="doSearch"
          >
            {{ tag }}
          </a-checkable-tag>
        </a-space>
      </div>
    </div>
    <div >
      <FsWaterFall
        :gap="20"
        :page-size="15"
        :request="fetchData"
      >
        <template #item="{item}">
          <img :src="item.url" alt="item.picName" class="image">
        </template>
      </FsWaterFall>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import {
  getPictureTagCategoryUsingGet,
  getPictureVoByPageUsingPost,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import FsWaterFall from '@/components/fs-waterFall/FsWaterFall.vue'

const searchParans = reactive<API.getPictureVOByIdUsingPOSTParams>({
  current: 1,
  pageSize: 12,
  searchText: '',
  category: '',
  tags: [],
  sortField: 'create_time',
  sortOrder: 'descend',
})
const dataList = ref<API.PictureVO[]>([])
const searchText = ref<string>('')
const tagList = ref<string[]>([])
const categoryList = ref<string[]>([])
const selectTagList = ref<string[]>([])
const selectCategoryList = ref<string>('all')
const loading = ref(true)
const pagination = computed(() => {
  return {
    current: searchParans.current,
    pageSize: searchParans.pageSize,
    total: 0,
    showSizeChanger: false,
    onChange: (page: number, pageSize: number) => {
      searchParans.current = page
      searchParans.pageSize = pageSize
      fetchData()
    },
  }
})
const fetchData = async ( page: number,pageSize: number,) => {
  try {
   const params = {
     ...searchParans,
     current: page,
     pageSize: pageSize,
   }
    const res = await getPictureVoByPageUsingPost(params)
    if (res.data.code === 200) {
      dataList.value = res.data.data.records ?? []
      // 为 total 赋值
      pagination.value.total = res.data.data.total ?? 0
      return dataList.value
    } else {
      message.error('获取图片列表失败,' + res.data.message)
      console.log(res.data.message)
      return
    }
  } catch (e) {
    message.error('获取图片列表失败')
    console.log(e)
  } finally {
    loading.value = false
  }
}
const doSearch = () => {
  searchParans.searchText = searchText.value
  if (selectCategoryList.value !== 'all'){
    searchParans.category = selectCategoryList.value
  }
  searchParans.tags = []
  selectTagList.value.forEach((useTag,index) => {
    if (useTag){
      searchParans.tags.push(tagList.value[index])
    }
  })
  searchParans.current = 1
  fetchData()
}
const getCategoryandTags = async () => {
  try {
    const res = await getPictureTagCategoryUsingGet()
    if (res.data.code === 200 && res.data.data) {
      const data = res.data.data
      categoryList.value = data.categoryList ?? []
      tagList.value = data.tagList ?? []
    } else {
      message.error('获取标签分类列表失败,' + res.data.message)
    }
  } catch (e) {
    message.error('获取标签分类列表失败,' + e)
    console.log(e)
  }
}

onMounted(() => {
  getCategoryandTags()
})

// 组件卸载时移除监听器，防止内存泄漏
onUnmounted(() => {
  window.removeEventListener('resize', debouncedUpdateColumnNum)
})
</script>

<style scoped>
#homePage {
  margin-bottom: 20px;
  .search-bar {
    max-width: 480px;
    margin: 0 auto 16px;
  }
  .tag-bar {
    margin-bottom: 16px;
  }
  .image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}
</style>
