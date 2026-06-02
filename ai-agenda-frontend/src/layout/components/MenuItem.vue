<template>
  <!-- 情况一：有子菜单 -->
  <el-sub-menu v-if="item.children && item.children.length > 0" :index="resolvePath(item.path)">
    <template #title>
      <el-icon><component :is="item.icon || 'Menu'" /></el-icon>
      <span>{{ item.menuName }}</span>
    </template>
    <!-- 递归调用时，把当前路径也传下去 -->
    <menu-item
      v-for="child in item.children"
      :key="child.id"
      :item="child"
      :base-path="resolvePath(item.path)"
    />
  </el-sub-menu>
  
  <!-- 情况二：没有子菜单 -->
  <el-menu-item v-else :index="resolvePath(item.path)">
    <el-icon><component :is="item.icon || 'Menu'" /></el-icon>
    <span>{{ item.menuName }}</span>
  </el-menu-item>
</template>

<script setup>
import { defineProps } from 'vue'
import path from 'path-browserify' // 引入路径处理库

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  basePath: {
    type: String,
    default: ''
  }
})

// 路径拼接函数
const resolvePath = (routePath) => {
  if (!routePath || routePath === '/') {
    return props.basePath || '/'
  }
  try {
    return path.resolve(props.basePath, routePath)
  } catch (error) {
    console.error('路径解析错误:', error)
    return props.basePath || '/'
  }
}
</script>