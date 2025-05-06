<template>
  <div class="ai-image-editor">
    <a-modal
      class="ai-image-editor-modal"
      v-model:visible="visible"
      title="AI图片编辑"
      :footer="false"
      @cancel="closeModal"
      width="1000px"
    >
      <!-- Add a full screen loading spinner overlay -->
      <div class="full-screen-loading" v-if="processing">
        <div class="spinner-container">
          <a-spin size="large" />
          <div class="loading-text">AI正在处理中...</div>
        </div>
      </div>

      <div class="editor-container">
        <!-- 左侧图片展示区域 -->
        <div class="image-preview-section">
          <div class="image-container">
            <vue-cropper ref="cropperRef" :img="imageUrl" output-type="png" />
          </div>
          <div class="image-controls">
            <a-tooltip title="重置图片">
              <a-button type="text" shape="circle" @click="resetImage">
                <template #icon><SyncOutlined /></template>
              </a-button>
            </a-tooltip>
            <a-tooltip title="放大">
              <a-button type="text" shape="circle" @click="zoomIn">
                <template #icon><ZoomInOutlined /></template>
              </a-button>
            </a-tooltip>
            <a-tooltip title="缩小">
              <a-button type="text" shape="circle" @click="zoomOut">
                <template #icon><ZoomOutOutlined /></template>
              </a-button>
            </a-tooltip>
          </div>
        </div>

        <!-- 右侧AI功能区域 -->
        <div class="ai-controls-section">
          <h2 class="section-title">AI 编辑功能</h2>

          <div class="ai-features">
            <a-radio-group
              @change="handleFeatureChange"
              v-model:value="selectedFeature"
              class="feature-radio-group"
            >
              <a-radio-button value="remove_watermark" class="feature-button">
                <div class="feature-content">
                  <div class="feature-icon-wrapper">
                    <svg class="icon" aria-hidden="true">
                      <use xlink:href="#icon-shipinqushuiyin"></use>
                    </svg>
                    <!--                    <DeleteOutlined class="feature-icon" />-->
                  </div>
                  <span class="feature-text">去除水印</span>
                </div>
              </a-radio-button>
              <a-radio-button value="expand" class="feature-button">
                <div class="feature-content">
                  <div class="feature-icon-wrapper">
                    <svg class="icon" aria-hidden="true">
                      <use xlink:href="#icon-AIkuotu"></use>
                    </svg>
                    <!--                    <FullscreenOutlined class="feature-icon" />-->
                  </div>
                  <span class="feature-text">AI扩图</span>
                </div>
              </a-radio-button>
              <a-radio-button value="super_resolution" class="feature-button">
                <div class="feature-content">
                  <div class="feature-icon-wrapper">
                    <svg class="icon" aria-hidden="true">
                      <use xlink:href="#icon-fenbianshuai"></use>
                    </svg>
                    <!--                    <ExpandOutlined class="feature-icon" />-->
                  </div>
                  <span class="feature-text">图像超分</span>
                </div>
              </a-radio-button>
              <a-radio-button value="doodle" class="feature-button">
                <div class="feature-content">
                  <div class="feature-icon-wrapper">
                    <svg class="icon" aria-hidden="true">
                      <use xlink:href="#icon-yishu_sumiao-cuxian"></use>
                    </svg>
                    <!--                    <HighlightOutlined class="feature-icon" />-->
                  </div>
                  <span class="feature-text">线稿生图</span>
                </div>
              </a-radio-button>
              <a-radio-button value="colorization" class="feature-button">
                <div class="feature-content">
                  <div class="feature-icon-wrapper">
                    <svg class="icon" aria-hidden="true">
                      <use xlink:href="#icon-shangse"></use>
                    </svg>
                    <!--                    <BgColorsOutlined class="feature-icon" />-->
                  </div>
                  <span class="feature-text">图像上色</span>
                </div>
              </a-radio-button>
              <a-radio-button value="stylization_local" class="feature-button">
                <div class="feature-content">
                  <div class="feature-icon-wrapper">
                    <svg class="icon" aria-hidden="true">
                      <use xlink:href="#icon-houqishejimeigong"></use>
                    </svg>
                    <!--                    <EditOutlined class="feature-icon" />-->
                  </div>
                  <span class="feature-text">风格化</span>
                </div>
              </a-radio-button>
            </a-radio-group>
          </div>

          <div class="feature-description">
            <div class="description-icon">
              <component :is="getFeatureIcon()" />
            </div>
            <div class="description-text">
              <p>{{ getFeatureDescription() }}</p>
            </div>
          </div>

          <div class="prompt-input" v-if="needsPrompt()">
            <div class="input-label-with-icon">
              <BulbOutlined class="input-icon" />
              <span>AI 提示词</span>
            </div>
            <a-textarea
              v-model:value="createOutPaintingRequest.prompt"
              placeholder="输入AI提示词，描述您想要的效果..."
              :rows="3"
              allow-clear
            />
          </div>

          <div class="processing-options">
            <a-form layout="vertical" :style="{ marginBottom: '8px' }">
              <a-form-item
                v-if="selectedFeature === 'super_resolution'"
                label="放大倍数"
                :style="{ marginBottom: '12px' }"
              >
                <div class="input-label-with-icon">
                  <ColumnHeightOutlined class="input-icon" />
                </div>
                <a-select
                  v-model:value="upscaleFactor"
                  placeholder="选择放大倍数"
                  class="custom-select"
                >
                  <a-select-option value="2">2x</a-select-option>
                  <a-select-option value="4">4x</a-select-option>
                  <a-select-option value="8">8x</a-select-option>
                </a-select>
              </a-form-item>
            </a-form>
          </div>

          <div class="action-buttons">
            <a-button @click="resetImage" class="reset-button" :disabled="processing">
              <template #icon><SyncOutlined /></template>
              重置
            </a-button>
            <a-button
              type="primary"
              :loading="processing"
              @click="applyAiFeature"
              :disabled="!canApplyFeature() || processing"
              class="apply-button"
            >
              应用AI效果
            </a-button>
            <a-button
              type="primary"
              :loading="loading"
              @click="handleConfirm"
              :disabled="processing"
              class="save-button"
            >
              保存图片
            </a-button>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'
import { message, Spin } from 'ant-design-vue'
import {
  BgColorsOutlined,
  BulbOutlined,
  ColumnHeightOutlined,
  DeleteOutlined,
  EditOutlined,
  ExpandOutlined,
  FullscreenOutlined,
  HighlightOutlined,
  SyncOutlined,
  ZoomInOutlined,
  ZoomOutOutlined,
} from '@ant-design/icons-vue'
import { AI_PICTURE_EDIT_PROMPT } from '@/constants/picture'
import {
  createPictureOutPaintingTaskUsingPost,
  getPictureOutPaintingTaskUsingGet, uploadPictureUsingPost
} from '@/api/pictureController'

interface Props {
  imageUrl?: string
  picture?: API.PictureVO
  spaceId?: number
  onSuccess?: (picture: API.PictureVO) => void
}

const props = defineProps<Props>()
//存放提交任务的taskId
const taskId = ref('')
// 组件引用和状态
const cropperRef = ref()
const visible = ref(false)
const loading = ref(false)
const processing = ref(false)

// AI 功能相关状态
const selectedFeature = ref('remove_watermark')
const promptText = ref('')
const upscaleFactor = ref('2')
const drawingTool = ref('brush')
const createOutPaintingRequest = ref<API.CreatePictureOutPaintingTaskRequest>({
  prompt: AI_PICTURE_EDIT_PROMPT[selectedFeature.value],
  function: selectedFeature.value,
  pictureId: props.picture?.id,
  baseImageUrl: '',
  parameters: {},
})

// 打开/关闭模态框
const openModal = () => {
  handleFeatureChange()
  visible.value = true
}

const closeModal = () => {
  visible.value = false
}

// 图片操作函数
const resetImage = () => {
  cropperRef.value?.refresh()
  createOutPaintingRequest.value.prompt = ''
}

const zoomIn = () => {
  cropperRef.value?.zoomIn()
}

const zoomOut = () => {
  cropperRef.value?.zoomOut()
}

// 判断当前功能是否需要提示词
const needsPrompt = () => {
  return ['expand', 'doodle', 'stylization_local'].includes(selectedFeature.value)
}

// 判断是否可以应用AI功能
const canApplyFeature = () => {
  if (needsPrompt() && !createOutPaintingRequest.value.prompt) {
    return false
  }
  return true
}

// 获取功能图标
const getFeatureIcon = () => {
  const icons = {
    remove_watermark: DeleteOutlined,
    expand: FullscreenOutlined,
    super_resolution: ExpandOutlined,
    doodle: HighlightOutlined,
    colorization: BgColorsOutlined,
    stylization_local: EditOutlined,
  }

  return icons[selectedFeature.value] || DeleteOutlined
}

// 获取功能描述
const getFeatureDescription = () => {
  const descriptions = {
    remove_watermark: '自动识别并移除图片中的水印，保持原始图像的质量。',
    expand: '智能扩展图像边界，生成符合原图风格的新内容。',
    super_resolution: '提高图像分辨率，使图像更加清晰。',
    doodle: '将线稿转换为彩色图像，根据提示词生成具体效果。',
    colorization: '为黑白或灰度图像添加自然的色彩。',
    stylization_local: '转换成法国绘本风格。',
  }
  return descriptions[selectedFeature.value] || ''
}

// 应用AI功能
const applyAiFeature = async () => {
  if (!canApplyFeature()) {
    message.warning('请先完善必要信息')
    return
  }

  // 设置整体加载状态
  processing.value = true

  try {
    const res = await createPictureOutPaintingTaskUsingPost(createOutPaintingRequest.value)
    if (res.data.code === 200 && res.data.data) {
      message.success('AI处理任务提交成功')
      //保存taskId
      taskId.value = res.data.data.output.taskId
      //调用轮询获取结果的方法
      await pollForResult()
    } else {
      message.error('AI处理失败,' + res.data.message)
    }
  } catch (e) {
    message.error('AI处理失败,' + e)
  } finally {
    // 最终结果处理完毕后才关闭加载状态
    processing.value = false
  }
}

const STATUS_PENDING = 'PENDING'
const STATUS_RUNNING = 'RUNNING'
const STATUS_SUCCESS = 'SUCCEEDED'
const MAX_RETRY = 60 // 最多轮询60次（即最多轮询3分钟）

const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

const pollForResult = async () => {
  try {
    let retryCount = 0

    while (retryCount < MAX_RETRY) {
      const res = await getPictureOutPaintingTaskUsingGet({
        taskId: taskId.value,
      })

      if (res.data.code === 200) {
        const output = res.data.data.output
        const status = output.task_status

        console.log(`第 ${retryCount + 1} 次轮询，状态：${status}`)

        if (status !== STATUS_PENDING && status !== STATUS_RUNNING) {
          if (status === STATUS_SUCCESS) {
            // 处理成功结果
            props.picture.url = output.results?.[0]?.url || ''
            // 如果有成功回调，调用它
            props.onSuccess(props.picture)
            message.success('AI处理成功')
          } else {
            // 处理失败结果
            message.error('AI处理失败: ' + (output.message || '未知错误'))
          }
          break
        }
      } else {
        console.error('接口请求失败:', res.data.message || '未知错误')
        message.error('接口请求失败，请稍后重试')
        break
      }

      retryCount++
      await delay(3000)
    }

    if (retryCount >= MAX_RETRY) {
      message.warning('任务超时，请稍后重试')
    }
  } catch (error) {
    console.error('轮询异常:', error)
    message.error('轮询过程中发生异常')
  }
}

// 确认并上传
const handleConfirm = () => {
  loading.value = true
  cropperRef.value.getCropBlob((blob: Blob) => {
    // blob 为已经裁切好的文件
    const fileName = (props.picture?.name || 'image') + '.png'
    const file = new File([blob], fileName, { type: blob.type })
    // 上传图片
    handleUpload({ file })
  })
}

// 模拟上传函数，请替换为实际上传逻辑
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

//当功能改变时触发，修改createOutPaintingRequest的值
const handleFeatureChange = () => {
  //保留原有
  createOutPaintingRequest.value = {
    prompt: AI_PICTURE_EDIT_PROMPT[selectedFeature.value],
    function: selectedFeature.value,
    pictureId: props.picture?.id,
    baseImageUrl: '',
    parameters: {},
  }
  console.log(createOutPaintingRequest.value)
}

// 向外暴露方法
defineExpose({
  openModal,
})

</script>

<style scoped>
.ai-image-editor-modal {
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

/* 全屏加载覆盖层 */
.full-screen-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.spinner-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.9);
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.loading-text {
  margin-top: 16px;
  font-size: 16px;
  color: #1890ff;
  font-weight: 500;
}

.ai-image-editor-modal :deep(.ant-modal-content) {
  padding: 0;
  overflow: hidden;
  border-radius: 12px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
}

.ai-image-editor-modal :deep(.ant-modal-header) {
  padding: 16px 24px;
  background: linear-gradient(135deg, #1890ff, #52c41a);
  border-bottom: none;
}

.ai-image-editor-modal :deep(.ant-modal-title) {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.ai-image-editor-modal :deep(.ant-modal-close) {
  color: white;
}

.ai-image-editor-modal :deep(.ant-modal-body) {
  padding: 0;
}

.editor-container {
  display: flex;
  height: 550px;
  position: relative;
}

.image-preview-section {
  flex: 1.3;
  display: flex;
  flex-direction: column;
  background-color: #f7f9fc;
  padding: 16px;
}

.image-container {
  flex: 1;
  overflow: hidden;
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  background-color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.image-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 8px 0;
  gap: 12px;
}

.ai-controls-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: white;
  padding: 16px;
  border-left: 1px solid #f0f0f0;
  overflow-y: auto;
}

.section-title {
  margin-top: 0;
  margin-bottom: 16px;
  font-weight: 600;
  color: #333;
  font-size: 18px;
  position: relative;
  padding-bottom: 10px;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 40px;
  height: 3px;
  background: linear-gradient(90deg, #1890ff, #52c41a);
  border-radius: 2px;
}

.ai-features {
  margin-bottom: 20px;
}

.feature-radio-group {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 10px;
  width: 100%;
}

.feature-button {
  height: auto !important;
  padding: 0 !important;
  border-radius: 10px !important;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  position: relative;
  border: none !important;
}

.feature-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.feature-button:before {
  display: none !important;
}

.feature-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px 10px;
  width: 100%;
  height: 100%;
}

.feature-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #e6f7ff, #f6ffed);
  border-radius: 12px;
  margin-bottom: 6px;
  transition: all 0.2s ease;
}

.feature-icon {
  font-size: 22px;
  color: #1890ff;
}

.feature-text {
  font-size: 13px;
  font-weight: 500;
  color: #333;
  margin-top: 3px;
  transition: color 0.2s ease;
}

/* 选中状态样式 */
.feature-button:deep(.ant-radio-button-wrapper-checked) {
  background: linear-gradient(to bottom right, #e6f7ff, #f6ffed) !important;
  z-index: 1;
}

.feature-button:deep(.ant-radio-button-wrapper-checked) .feature-icon-wrapper {
  background: linear-gradient(135deg, #1890ff, #52c41a);
  transform: scale(1.05);
}

.feature-button:deep(.ant-radio-button-wrapper-checked) .feature-icon {
  color: white;
}

.feature-button:deep(.ant-radio-button-wrapper-checked) .feature-text {
  color: #1890ff;
  font-weight: 600;
}

.feature-description {
  background-color: #f8f9fc;
  padding: 12px;
  border-radius: 10px;
  margin-bottom: 16px;
  min-height: 60px;
  display: flex;
  align-items: center;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f2f5;
}

.description-icon {
  font-size: 20px;
  margin-right: 12px;
  color: #1890ff;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background-color: #e6f7ff;
  border-radius: 50%;
  flex-shrink: 0;
}

.description-text {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

.input-label-with-icon {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  font-weight: 500;
  color: #333;
}

.input-icon {
  color: #1890ff;
  margin-right: 6px;
  font-size: 15px;
}

.prompt-input {
  margin-bottom: 16px;
}

.prompt-input :deep(.ant-input) {
  border-radius: 8px;
  border: 1px solid #d9d9d9;
  padding: 10px;
  font-size: 13px;
  transition: all 0.3s;
}

.prompt-input :deep(.ant-input:focus) {
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
  border-color: #1890ff;
}

.processing-options {
  margin-bottom: 10px;
}

.custom-select :deep(.ant-select-selector) {
  border-radius: 8px;
  height: 36px;
  padding: 0 12px;
}

.custom-select :deep(.ant-select-selection-item) {
  line-height: 34px;
}

.drawing-tools {
  display: flex;
  width: 100%;
}

.drawing-tools :deep(.ant-radio-button-wrapper) {
  flex: 1;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 36px;
}

.action-buttons {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  padding-top: 14px;
  border-top: 1px solid #f0f0f0;
}

.reset-button {
  color: #666;
  border-color: #d9d9d9;
}

.apply-button {
  background: linear-gradient(135deg, #1890ff, #52c41a);
  border: none;
  flex: 1;
  margin: 0 10px;
}

.save-button {
  background: #1890ff;
  border: none;
}

/* 修复 ant radio button 样式问题 */
:deep(.ant-radio-button-wrapper) {
  margin-right: 0;
  margin-bottom: 0;
  text-align: center;
  border-radius: 10px !important;
}

:deep(.ant-radio-button-wrapper::before) {
  display: none !important;
}

:deep(.ant-radio-button-wrapper-checked) {
  color: #1890ff !important;
  background: linear-gradient(to bottom right, #e6f7ff, #f6ffed) !important;
  border-color: #1890ff !important;
  box-shadow: 0 0 0 1px #1890ff !important;
  z-index: 1;
}

:deep(.vue-cropper) {
  height: 100% !important;
}

/* 响应式布局调整 */
@media (max-width: 768px) {
  .editor-container {
    flex-direction: column;
    height: auto;
  }

  .image-preview-section,
  .ai-controls-section {
    width: 100%;
  }

  .image-preview-section {
    height: 300px;
  }

  .ai-controls-section {
    border-left: none;
    border-top: 1px solid #f0f0f0;
  }

  .feature-radio-group {
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: repeat(3, 1fr);
  }
}
</style>
