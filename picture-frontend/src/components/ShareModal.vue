<template>
  <div class="share-modal-container">
    <a-modal
      :open="isOpen"
      :title="title"
      @ok="handleOk"
      @cancel="handleCancel"
      :footer="footer"
      :maskClosable="maskClosable"
      :width="width"
      class="share-modal"
    >
      <div class="share-content">
        <div class="qrcode-container">
          <a-qrcode
            error-level="H"
            :value="urlValue"
            :icon="qrcodeIcon"
            :size="qrcodeSize"
          />
          <p class="scan-tip">扫描二维码访问</p>
        </div>

        <div class="url-section">
          <div class="url-container">
            <a-input
              :value="urlValue"
              :disabled="true"
              class="url-input"
            />
            <a-tooltip title="复制链接">
              <a-button
                type="primary"
                @click="copyUrl"
                class="copy-btn"
              >
                <template #icon><copy-outlined /></template>
              </a-button>
            </a-tooltip>
          </div>

          <div class="share-platforms" v-if="showSocialShare">
            <p class="share-tip">分享到社交平台:</p>
            <div class="platform-icons">
              <a-tooltip title="微信">
                <a-button shape="circle" class="platform-icon wechat">
                  <template #icon><wechat-outlined /></template>
                </a-button>
              </a-tooltip>
              <a-tooltip title="微博">
                <a-button shape="circle" class="platform-icon weibo">
                  <template #icon><weibo-outlined /></template>
                </a-button>
              </a-tooltip>
              <a-tooltip title="QQ">
                <a-button shape="circle" class="platform-icon qq">
                  <template #icon><qq-outlined /></template>
                </a-button>
              </a-tooltip>
            </div>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { message } from 'ant-design-vue';
import {
  CopyOutlined,
  WechatOutlined,
  WeiboOutlined,
  QqOutlined
} from '@ant-design/icons-vue';

// 定义组件接收的属性
const props = defineProps({
  url: {
    type: String,
    required: true,
    default: ''
  },
  isOpen: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '分享链接'
  },
  qrcodeIcon: {
    type: String,
    default: ''
  },
  qrcodeSize: {
    type: Number,
    default: 160
  },
  showSocialShare: {
    type: Boolean,
    default: true
  },
  footer: {
    type: Boolean,
    default: false
  },
  maskClosable: {
    type: Boolean,
    default: true
  },
  width: {
    type: Number,
    default: 420
  }
});

// 使用计算属性或本地状态来避免直接修改props
const urlValue = computed(() => props.url);

// 定义事件
const emit = defineEmits(['update:isOpen', 'ok', 'copied']);

// 处理确认按钮点击
const handleOk = () => {
  emit('ok');
  emit('update:isOpen', false);
};

// 处理取消按钮点击
const handleCancel = () => {
  emit('update:isOpen', false);
};

// 复制链接函数
const copyUrl = () => {
  if (!urlValue.value) return;

  // 使用 Clipboard API 复制文本
  navigator.clipboard.writeText(urlValue.value)
    .then(() => {
      message.success('链接已复制到剪贴板');
      emit('copied', urlValue.value);
    })
    .catch(() => {
      message.error('复制失败，请手动复制');
    });
};
</script>

<style scoped>
.share-modal-container {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.share-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
}

.qrcode-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
}

.scan-tip {
  margin-top: 12px;
  color: #666;
  font-size: 14px;
}

.url-section {
  width: 100%;
}

.url-container {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.url-input {
  flex: 1;
  margin-right: 12px;
  border-radius: 4px;
}

.copy-btn {
  border-radius: 4px;
}

.share-platforms {
  margin-top: 16px;
  text-align: center;
}

.share-tip {
  margin-bottom: 12px;
  color: #666;
  font-size: 14px;
}

.platform-icons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.platform-icon {
  font-size: 18px;
  transition: all 0.3s;
}

.platform-icon:hover {
  transform: scale(1.1);
}

.wechat {
  color: #07c160;
  border-color: #07c160;
}

.wechat:hover {
  background-color: #07c160;
  color: white;
}

.weibo {
  color: #e6162d;
  border-color: #e6162d;
}

.weibo:hover {
  background-color: #e6162d;
  color: white;
}

.qq {
  color: #12b7f5;
  border-color: #12b7f5;
}

.qq:hover {
  background-color: #12b7f5;
  color: white;
}
</style>
