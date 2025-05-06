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
          <a-tab-pane v-for="category in categoryList" :tab="category" :key="category" @change="doSearch" />
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
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5 }"
      :data-source="dataList"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <a-card hoverable @click="() => $router.push(`/picture_detail/${picture.id}`)">
            <template #cover>
              <img
                :alt="picture.name"
                :data-src="picture.thumbnailUrl ?? picture.url"
                :src="placeholderImage"
                class="lazy-image"
                style="height: 180px; object-fit: cover;"
                ref="lazyImages"
              />
            </template>
          </a-card>
        </a-list-item>
      </template>
      <template #loadMore>
        <div v-if="loading" class="loading-container">
          <a-spin size="large" />
          <span class="loading-text">加载中...</span>
        </div>
        <div v-else-if="isEnd" class="end-container">
          <a-divider>已经到底啦</a-divider>
        </div>
      </template>
    </a-list>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, onUnmounted, nextTick } from 'vue'
import {
  getPictureTagCategoryUsingGet,
  getPictureVoByPageUsingPost,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

// 1x1像素的透明占位图
const placeholderImage = 'https://bizihu.com/fliee/loading.jpg'

const searchParans = reactive<API.getPictureVOByIdUsingPOSTParams>({
  current: 1,
  pageSize: 15,
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
const selectTagList = ref<boolean[]>([])
const selectCategoryList = ref<string>('all')
const loading = ref(false)
const isEnd = ref(false)
const scrollListenerAttached = ref(false)
const lazyImages = ref<HTMLImageElement[]>([])
const observer = ref<IntersectionObserver | null>(null)

// 初始化IntersectionObserver
const initObserver = () => {
  observer.value = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const img = entry.target as HTMLImageElement
        const src = img.dataset.src
        if (src) {
          img.src = src
          img.classList.remove('lazy-image')
          observer.value?.unobserve(img)
        }
      }
    })
  }, {
    rootMargin: '0px 0px 500px 0px' // 提前500px加载
  })
}

// 观察所有懒加载图片
const observeImages = () => {
  nextTick(() => {
    const imgs = document.querySelectorAll('.lazy-image')
    imgs.forEach(img => {
      observer.value?.observe(img as HTMLImageElement)
    })
  })
}

const fetchData = async () => {
  if (loading.value || isEnd.value) return

  loading.value = true
  try {
    const res = await getPictureVoByPageUsingPost(searchParans)
    if (res.data.code === 200) {
      const newData = res.data.data.records ?? []
      if (newData.length === 0) {
        isEnd.value = true
        return
      }

      if (searchParans.current === 1) {
        dataList.value = newData
      } else {
        dataList.value = [...dataList.value, ...newData]
      }

      if (newData.length > 0) {
        searchParans.current += 1
      }

      // 数据加载完成后观察新图片
      observeImages()
    } else {
      message.error('获取图片列表失败,' + res.data.message)
      console.log(res.data.message)
    }
  } catch (e) {
    message.error('获取图片列表失败')
    console.log(e)
  } finally {
    loading.value = false
  }
}

const handleScroll = () => {
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const windowHeight = window.innerHeight
  const fullHeight = document.documentElement.scrollHeight

  if (fullHeight - (scrollTop + windowHeight) < 100 && !loading.value && !isEnd.value) {
    fetchData()
  }
}

const attachScrollListener = () => {
  if (!scrollListenerAttached.value) {
    window.addEventListener('scroll', handleScroll)
    scrollListenerAttached.value = true
  }
}

const detachScrollListener = () => {
  if (scrollListenerAttached.value) {
    window.removeEventListener('scroll', handleScroll)
    scrollListenerAttached.value = false
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
  isEnd.value = false
  fetchData()
}

const getCategoryandTags = async () => {
  try {
    const res = await getPictureTagCategoryUsingGet()
    if (res.data.code === 200 && res.data.data) {
      const data = res.data.data
      categoryList.value = data.categoryList ?? []
      tagList.value = data.tagList ?? []

      // 初始化 selectTagList 与 tagList 长度相同，全部为 false
      selectTagList.value = new Array(tagList.value.length).fill(false)
    } else {
      message.error('获取标签分类列表失败,' + res.data.message)
    }
  } catch (e) {
    message.error('获取标签分类列表失败,' + e)
    console.log(e)
  }
}

onMounted(() => {
  initObserver()
  fetchData()
  getCategoryandTags()
  attachScrollListener()
})

onUnmounted(() => {
  detachScrollListener()
  observer.value?.disconnect()
})
</script>

<style scoped>
#homePage {
  margin-bottom: 20px;
}

#homePage .search-bar {
  max-width: 480px;
  margin: 0 auto 16px;
}

#homePage .tag-bar {
  margin-bottom: 16px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
  gap: 8px;
}

.loading-text {
  color: rgba(0, 0, 0, 0.45);
}

.end-container {
  padding: 20px 0;
  text-align: center;
}

:deep(.ant-divider-inner-text) {
  color: rgba(0, 0, 0, 0.45);
  font-size: 14px;
}

.lazy-image {
  background-color: #f0f0f0; /* 添加背景色作为加载前的占位 */
  transition: opacity 0.3s ease-in-out;
}

.lazy-image:not([src]) {
  opacity: 0;
}
</style>
