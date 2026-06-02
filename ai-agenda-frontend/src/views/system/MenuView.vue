<template>
  <div class="app-container">
    <!-- 1. 顶部工具栏 -->
    <el-card class="filter-container" shadow="sm">
      <el-button type="success" icon="Plus" @click="handleCreate()">新增菜单</el-button>
    </el-card>

    <!-- 2. 树形表格 -->
    <el-card class="table-container" shadow="sm">
      <el-table
        :data="menuTreeData"
        style="width: 100%; margin-bottom: 20px"
        row-key="id"
        border
        default-expand-all
        v-loading="listLoading"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="menuName" label="菜单名称" sortable width="180" />
        <el-table-column prop="icon" label="图标" align="center" width="80">
          <template #default="scope">
            <el-icon v-if="scope.row.icon"><component :is="scope.row.icon" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="orderNum" label="排序" align="center" width="80" />
        <el-table-column prop="perms" label="权限标识" align="center" />
        <el-table-column prop="path" label="路由路径" align="center" />
        <el-table-column label="操作" align="center" width="320">
          <template #default="scope">
            <el-button size="small" type="success" icon="Plus" @click="handleCreate(scope.row)">新增下级</el-button>
            <el-button size="small" type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogFormVisible" width="600px">
      <el-form ref="dataForm" :model="temp" label-width="100px">
        <el-form-item label="上级菜单">
          <!-- 级联选择器，用于选择父菜单 -->
          <el-tree-select
            v-model="temp.parentId"
            :data="menuTreeForSelect"
            check-strictly
            :props="{ label: 'menuName', value: 'id' }"
            placeholder="不选则为根菜单"
            clearable
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="temp.menuName" />
        </el-form-item>
        <el-form-item label="路由路径" prop="path">
          <el-input v-model="temp.path" placeholder="例如: user, schedule" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component">
          <el-input v-model="temp.component" placeholder="例如: system/UserView" />
        </el-form-item>
        <el-form-item label="权限标识" prop="perms">
          <el-input v-model="temp.perms" placeholder="例如: sys:user:list" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="temp.icon" placeholder="Element Plus Icon 名称" />
        </el-form-item>
        <el-form-item label="排序" prop="orderNum">
          <el-input-number v-model="temp.orderNum" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="dialogStatus === 'create' ? createData() : updateData()">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import bus from '@/utils/eventBus.js' // 引入事件总线
import { 
  getMenuTree as apiGetMenuTree, 
  addMenu, 
  updateMenu, 
  deleteMenu 
} from '@/api/menu'

const menuTreeData = ref([])
const listLoading = ref(true)
const dialogFormVisible = ref(false)
const dialogStatus = ref('')

const initTemp = () => ({
  id: undefined,
  parentId: 0,
  menuName: '',
  path: '',
  component: '',
  perms: '',
  icon: '',
  orderNum: 0
})
const temp = ref(initTemp())

const dialogTitle = computed(() => dialogStatus.value === 'create' ? '新增菜单' : '编辑菜单')

// 用于“上级菜单”下拉选择的树形数据
const menuTreeForSelect = computed(() => {
  // 顶层增加一个“根菜单”选项
  return [{ id: 0, menuName: '根菜单', children: menuTreeData.value }]
})

onMounted(() => { getMenuTree() })

const getMenuTree = async () => {
  listLoading.value = true
  try {
    const res = await apiGetMenuTree()
    
    if (res && res.code === 200) {
      // 兼容多种数据格式
      let rawData = []
      if (Array.isArray(res.data)) {
        rawData = res.data
      } else if (res.data && Array.isArray(res.data.children)) {
        rawData = res.data.children
      } else {
        rawData = res.data ? [res.data] : []
      }
      
      
      // 强制重新赋值，确保Vue响应式更新
      menuTreeData.value = []
      // 使用nextTick确保DOM更新
      setTimeout(() => {
        menuTreeData.value = rawData
      }, 0)
      
      // 【广播】菜单已更新
      bus.emit('menu-updated')
    } else {
      console.error('获取菜单树失败:', res?.msg || '未知错误')
      ElMessage.error('获取菜单数据失败')
    }
  } catch (error) {
    console.error('获取菜单树失败:', error)
    ElMessage.error('网络错误：无法获取菜单数据')
  } finally {
    listLoading.value = false
  }
}

const handleCreate = (row) => {
  temp.value = initTemp()
  if (row && row.id) {
    temp.value.parentId = row.id // 如果是新增下级，则设置父ID
  }
  dialogStatus.value = 'create'
  dialogFormVisible.value = true
}

const handleUpdate = (row) => {
  temp.value = Object.assign({}, row)
  dialogStatus.value = 'update'
  dialogFormVisible.value = true
}

const handleDelete = (row) => {
  // 【核心】增加判断：如果 row 有 children 并且长度大于 0，则不允许删除
  if (row.children && row.children.length > 0) {
    ElMessage.error('请先删除该菜单下的所有子菜单！')
    return
  }

  ElMessageBox.confirm(`确定删除菜单 "${row.menuName}" 吗?`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteMenu(row.id)
        ElMessage.success('删除成功')
        // 等待数据完全更新后再触发事件
        await getMenuTree()
        // 延迟触发事件，确保数据已完全更新
        setTimeout(() => {
          bus.emit('menu-updated')
        }, 100)
      } catch (error) {
        console.error('删除菜单失败:', error)
        ElMessage.error('删除失败，请重试')
      }
    })
}

const createData = async () => {
  try {
    await addMenu(temp.value)
    dialogFormVisible.value = false
    ElMessage.success('创建成功')
    // 等待数据完全更新后再触发事件
    await getMenuTree()
    // 延迟触发事件，确保数据已完全更新
    setTimeout(() => {
      bus.emit('menu-updated')
    }, 100)
  } catch (error) {
    console.error('创建菜单失败:', error)
    ElMessage.error('创建失败，请重试')
  }
}

const updateData = async () => {
  try {
    await updateMenu(temp.value)
    dialogFormVisible.value = false
    ElMessage.success('更新成功')
    // 【核心】强制重新从后端拉取最新的树形数据
    await getMenuTree()
    // 延迟触发事件，确保数据已完全更新
    setTimeout(() => {
      bus.emit('menu-updated')
    }, 100)
  } catch (error) {
    console.error('更新菜单失败:', error)
    ElMessage.error('更新失败，请重试')
  }
}
</script>

<style scoped>
.app-container { padding: 20px; }
.filter-container { margin-bottom: 20px; }
</style>
