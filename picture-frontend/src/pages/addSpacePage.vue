<template>
  <div id="addSpacePage" class="space-container">
    <h2 class="page-title">{{ route.query?.id ? '修改空间' : '创建空间' }}</h2>
    <!--  空间信息表单 -->
    <a-form
      name="spaceForm"
      layout="vertical"
      :model="spaceForm"
      @finish="handleSubmit"
    >
      <a-form-item name="spaceName" label="空间名称">
        <a-input v-model:value="spaceForm.spaceName" placeholder="请输入空间名称" allow-clear />
      </a-form-item>
      <a-form-item label="空间级别">
        <div class="level-options">
          <a-radio-group v-model:value="spaceForm.spaceLevel" @change="changeLevel">
            <div v-for="item in SPACE_LEVEL_OPTIONS" :key="item.value" class="level-option">
              <a-radio :value="item.value">
                {{ item.label }}
                <span v-if="item.needVip" class="vip-badge">VIP</span>
              </a-radio>
              <div class="level-description">{{ item.description }}</div>
            </div>
          </a-radio-group>
        </div>
      </a-form-item>
      <a-form-item>
        <a-button html-type="submit" type="primary" class="submit-button">
          {{ route.query?.id ? '修改' : '创建' }}
        </a-button>
      </a-form-item>
    </a-form>

    <!-- VIP提示框 -->
    <a-alert
      v-if="showVipTip"
      type="warning"
      show-icon
      class="vip-alert"
      message="VIP功能提示"
      description="专业版和旗舰版需要VIP会员才能使用，升级会员可享受更多权益。"
    >
      <template #icon>
        <crown-outlined />
      </template>
      <template #action>
        <a-button size="small" type="primary">
          升级VIP
        </a-button>
      </template>
    </a-alert>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { CrownOutlined } from '@ant-design/icons-vue'
import { addSpaceUsingPost } from '@/api/spaceController'
import { SPACE_LEVEL_OPTIONS } from '@/constants/space'

const space = ref<API.SpaceVO>()
const spaceForm = reactive<API.SpaceAddRequest>({
  spaceLevel: 'normal' // 默认选择普通版
})
const route = useRoute()
const router = useRouter()
const showVipTip = ref(false)

const changeLevel = (e) => {
  const selectedLevel = e.target.value
  const isVipLevel = SPACE_LEVEL_OPTIONS.find(item => item.value === selectedLevel)?.needVip
  showVipTip.value = isVipLevel || false
}

const handleSubmit = async (values: any) => {
  try {
    const res = await addSpaceUsingPost(spaceForm)
    if (res.data.code === 200 && res.data.data) {
      message.success('创建成功')
      await router.push('/my_space')
    } else {
      message.error('创建失败,' + res.data.message)
    }
  } catch (error) {
    message.error('操作失败: ' + error.message)
  }
}

onMounted(async () => {

})
</script>

<style scoped>
#addSpacePage {
  max-width: 720px;
  margin: 0 auto;
  padding: 24px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.page-title {
  font-size: 20px;
  margin-bottom: 24px;
  color: #262626;
  text-align: center;
}

.level-options {
  margin-top: 8px;
}

.level-option {
  padding: 12px 16px;
  margin-bottom: 12px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  transition: all 0.3s;
}

.level-option:hover {
  border-color: #40a9ff;
  background-color: #f0f7ff;
}

.level-description {
  margin-top: 4px;
  margin-left: 24px;
  font-size: 12px;
  color: #8c8c8c;
}

.vip-badge {
  display: inline-block;
  margin-left: 8px;
  padding: 0 6px;
  font-size: 12px;
  line-height: 18px;
  color: #fff;
  background: linear-gradient(135deg, #f5a623, #f56c23);
  border-radius: 10px;
  font-weight: bold;
}

.submit-button {
  width: 100%;
  height: 40px;
  font-size: 16px;
  border-radius: 4px;
}

.vip-alert {
  margin-top: 24px;
  border-radius: 6px;
}
</style>
