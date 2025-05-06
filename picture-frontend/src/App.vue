
<template>
  <div id="app">
    <BasicLayout />
  </div>
</template>
<script setup lang="ts">
import BasicLayout from '@/layout/BasicLayout.vue'
import '@/access.ts'
import { healthUsingGet } from '@/api/mainController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
// healthUsingGet().then(res => {
//   console.log(res)
// })
// const loginUserStore = useLoginUserStore()
// loginUserStore.fetchLoginUser()
</script>

<style scoped>

</style>
