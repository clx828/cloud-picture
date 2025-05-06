<template>
  <div class="fs-waterfall-container">
    <div class="fs-waterfall-content" ref="contentRef">
      <div class="fs-waterfall-list">
        <div
          class="fs-waterfall-item"
          v-for="(item, index) in state.imageList"
          :key="item.id"
          :style="{
          width: `${state.imageWidth}px`,
          transform: `translate3d(${imagePos[index]?.x || 0}px, ${imagePos[index]?.y || 0}px, 0)`,
          }"
        >
          <slot name="item" :item="item"></slot>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch, onUnmounted,nextTick } from 'vue'

// 内联类型定义
interface IImageItem {
  id: string | number;
  picWidth: number;
  picHeight: number;
  url?: string;
  name?: string;
  [key: string]: any;
}
interface IWaterFallProps {
  gap?: number;
  request: (page: number, pageSize: number) => Promise<IImageItem[]>;
}
defineSlots<{
  item(props: { item: IImageItem }): any;
}>()
const props = withDefaults(defineProps<IWaterFallProps>(), {
  gap: 16,
})
const column = ref(0)
const state = reactive({
  loading: false,
  isfinish: false,
  page: 1,
  pageSize: 20,
  imageWidth: 0,
  imageList: [] as IImageItem[]
})
const imagePos = ref<{ x: number; y: number }[]>([])
const contentRef = ref<HTMLDivElement>()
const columnHeight = ref<number[]>([])
const scrollParent = ref<HTMLElement | Window>()

const setColumn = () => {
  if (contentRef.value) {
    const width = contentRef.value.clientWidth
    if (width < 576) {
      column.value = 1
    } else if (width < 768) {
      column.value = 2
    } else if (width < 992) {
      column.value = 3
    } else if (width < 1200) {
      column.value = 4
    } else {
      column.value = 5
    }
  }
  imagePos.value = []
  columnHeight.value = []
}
// 查找最近的滚动父容器
const findScrollParent = (element: HTMLElement | null): HTMLElement | Window => {
  if (!element) return window
  let parent = element.parentElement
  while (parent) {
    const style = getComputedStyle(parent)
    if (['auto', 'scroll', 'overlay'].includes(style.overflowY)) {
      return parent
    }
    parent = parent.parentElement
  }
  return window
}
const handleScroll = () => {
  if (state.loading || state.isfinish) return

  let scrollTop: number, clientHeight: number, scrollHeight: number

  if (scrollParent.value instanceof Window) {
    scrollTop = document.documentElement.scrollTop || document.body.scrollTop
    clientHeight = document.documentElement.clientHeight
    scrollHeight = document.documentElement.scrollHeight || document.body.scrollHeight
  } else {
    const el = scrollParent.value as HTMLElement
    scrollTop = el.scrollTop
    clientHeight = el.clientHeight
    scrollHeight = el.scrollHeight
  }

  // 在距离底部100px时触发加载
  if (scrollHeight - (scrollTop + clientHeight) < 100) {
    getImageList(state.page, 20)
  }
}
onMounted(() => {
  init()
  // 绑定滚动容器
  if (contentRef.value) {
    const parent = findScrollParent(contentRef.value)
    scrollParent.value = parent
    parent.addEventListener('scroll', handleScroll)
  }
  // 初始加载
  if (props.request) {
    getImageList()
  }
})
onUnmounted(() => {
  // 移除监听
  if (scrollParent.value) {
    scrollParent.value.removeEventListener('scroll', handleScroll)
  }
})
const findShortestColumn = computed(() => {
  let minIndex = 0
  let minHeight = Infinity
  for (let i = 0; i < columnHeight.value.length; i++) {
    const h = columnHeight.value[i]
    if (h < minHeight) {
      minHeight = h
      minIndex = i
    }
  }
  return {
    minIndex,
    minHeight
  }
})
const getImageList = async () => {
  state.loading = true
  try {
    const list = await props.request(state.page,state.pageSize)
    if (!list.length) {
      state.isfinish = true
      return
    }
    computedImagePos(list)
    state.imageList = [...state.imageList, ...list]
    state.page++
  } catch (error) {
    console.error('获取图片列表失败', error)
  } finally {
    state.loading = false
    // 数据更新后检查是否需要继续加载
    await nextTick(() => {
      handleScroll()
    })
  }
}
const computedImagePos = (list: IImageItem[]) => {
  list.forEach((item, index) => {
    const imageHeight = Math.floor((item.picHeight * state.imageWidth) / item.picWidth)
    if (index < column.value && imagePos.value.length<6) {
      // 处理第一行
      imagePos.value.push({
        x: index % column.value !== 0
          ? index * (state.imageWidth + props.gap)
          : index * state.imageWidth,
        y: 0
      },
      )
      columnHeight.value.push(imageHeight + props.gap)
    } else {
      // 处理后续行
      const { minIndex, minHeight } = findShortestColumn.value
      imagePos.value.push({
        x: minIndex % column.value !== 0
          ? minIndex * (state.imageWidth + props.gap)
          : minIndex * state.imageWidth,
        y: minHeight
      })
      columnHeight.value[minIndex] += imageHeight + props.gap
    }
  })
}
const init = () => {
  if (contentRef.value) {
    setColumn();
    state.imageWidth = (contentRef.value.clientWidth - (column.value - 1) * props.gap) / column.value
  }
}
// 监听窗口大小变化,更新列数和图片宽度
const handleResize = () => {
  if (contentRef.value) {
    // 重新设置列数
    setColumn()
    // 重新计算图片宽度
    state.imageWidth = (contentRef.value.clientWidth - (column.value - 1) * props.gap) / column.value
    // 重新计算所有图片的位置
    computedImagePos(state.imageList)
  }
}
// 暴露方法
defineExpose({
  getImageList,
  init
})
// 在 onMounted 钩子中添加窗口大小变化监听
onMounted(() => {
  // 之前的挂载逻辑保持不变
  // 添加窗口大小变化监听
  window.addEventListener('resize', handleResize)
})

// 在 onUnmounted 钩子中移除监听
onUnmounted(() => {
  // 之前的卸载逻辑保持不变
  // 移除窗口大小变化监听
  window.removeEventListener('resize', handleResize)
})

</script>

<style scoped>
.fs-waterfall-container {
  width: 100%;
  /* 改为由内容撑开高度 */
  height: auto;
  min-height: 100vh;
}

  .fs-waterfall-content {
    width: 100%;
    height: 100%;
    position: relative;
  }

  .fs-waterfall-list {
    width: 100%;
    position: relative;
  }

  .fs-waterfall-item {
    position: absolute;
    top: 0;
    left: 0;
    box-sizing: border-box;
  }
</style>
