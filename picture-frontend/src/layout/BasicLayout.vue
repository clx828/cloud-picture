<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalSider from '@/components/GlobalSider.vue'
</script>

<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header"><GlobalHeader/></a-layout-header>
      <a-layout>
        <a-layout-sider
          class="sider"
          :collapsed="true"
          :trigger="null"
          width="200"
          collapsedWidth="80"
          :style="{ height: '100vh', position: 'fixed', left: 0 }"
        >
          <GlobalSider />
        </a-layout-sider>
        <a-layout :style="{ marginLeft: '80px' }">
          <a-layout-content class="content">
            <router-view />
          </a-layout-content>
        </a-layout>
      </a-layout>
      <a-layout-footer class="footer">
        <a href="https://clx828.github.io/" target="_blank">
          云图库 by caden
        </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<style scoped>
#basicLayout .header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  padding-inline: 20px;
  background: #fff;
  height: 64px;
  line-height: 64px;
}
#basicLayout .sider {
  padding-top: 64px;
  background: #fff;
  border-right: 0.5px solid #eee;
  z-index: 998;
  overflow-x: hidden; /* 防止横向滚动条 */
}
#basicLayout :deep(.ant-menu-root){
  border-bottom: none !important;
  border-inline-end: none !important;
}

#basicLayout .content {
  padding: 84px 20px 20px; /* 顶部留出header高度 */
  background: linear-gradient(to right, #fff, #f9f9f9);
  margin-bottom: 28px;
  margin-top: 28px;
  min-height: calc(100vh - 112px); /* 视情况调整 */
}

#basicLayout .footer {
  background: #efefef;
  text-align: center;
  padding: 16px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
}
</style>
