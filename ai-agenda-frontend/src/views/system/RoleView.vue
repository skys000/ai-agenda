<template>
  <div class="app-container">
    <!-- 1. 顶部工具栏 -->
    <el-card class="filter-container" shadow="sm">
      <div class="filter-form">
        <el-button class="filter-item" type="success" icon="Plus" @click="handleCreate">新增角色</el-button>
      </div>
    </el-card>

    <!-- 2. 主体表格 -->
    <el-card class="table-container" shadow="sm">
      <el-table :data="list" v-loading="listLoading" style="width: 100%" border>
        <el-table-column label="ID" prop="id" width="80" align="center" />
        <el-table-column label="角色名称" prop="roleName" align="center" />
        <el-table-column label="权限字符" prop="roleKey" align="center" />
        <el-table-column label="创建时间" prop="createTime" align="center" :formatter="formatDateTime" />
        <el-table-column label="操作" width="280" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
            <el-button size="small" type="warning" icon="Check" @click="handleAssign(scope.row)">分配权限</el-button>
            <el-button size="small" type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 新增/编辑角色对话框 -->
    <el-dialog :title="dialogStatus==='create' ? '新增角色' : '编辑角色'" v-model="dialogFormVisible" width="500px">
      <el-form ref="dataForm" :rules="rules" :model="temp" label-position="left" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="temp.roleName" />
        </el-form-item>
        <el-form-item label="权限字符" prop="roleKey">
          <el-input v-model="temp.roleKey" placeholder="例如: admin, user" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="dialogStatus==='create' ? createData() : updateData()">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog title="分配权限" v-model="dialogAssignVisible" width="500px">
      <el-tree
        ref="menuTreeRef"
        :data="menuTreeData"
        show-checkbox
        node-key="id"
        :props="{ label: 'menuName', children: 'children' }"
        :default-expand-all="true"
      />
      <template #footer>
        <el-button @click="dialogAssignVisible = false">取消</el-button>
        <el-button type="primary" @click="assignPermissions">保存权限</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getRoleList,
  addRole,
  updateRole,
  deleteRole,
  getAssignedMenus,
  assignMenus
} from '@/api/role'
import { getMenuTree } from '@/api/menu'

// --- 响应式数据 ---
const list = ref([])
const listLoading = ref(true)
const dialogFormVisible = ref(false)
const dialogAssignVisible = ref(false)
const dialogStatus = ref('')

const dataForm = ref(null) // 表单 ref
const menuTreeRef = ref(null) // 树形控件 ref

const menuTreeData = ref([])
const allMenuData = ref([]) // 扁平化的菜单数据
const currentRoleId = ref(null) // 当前操作的角色ID

const initTemp = () => ({ id: undefined, roleName: '', roleKey: '' })
const temp = ref(initTemp())

const rules = reactive({
  roleName: [{ required: true, message: '角色名称是必填项', trigger: 'blur' }],
  roleKey: [{ required: true, message: '权限字符是必填项', trigger: 'blur' }]
})

// --- 生命周期 ---
onMounted(() => {
  getList()
  fetchMenuTree()
})

// --- API 调用 ---
const getList = async () => {
  listLoading.value = true
  try {
    const res = await getRoleList()
    console.log('角色列表响应:', res) // 调试日志
    
    if (res && res.code === 200) {
      // 兼容多种数据格式
      if (Array.isArray(res.data)) {
        list.value = res.data
      } else if (res.data && Array.isArray(res.data.records)) {
        list.value = res.data.records
      } else {
        list.value = res.data ? [res.data] : []
      }
      
      console.log('解析后的角色列表:', list.value) // 调试日志
    } else {
      console.error('获取角色列表失败:', res?.msg || '未知错误')
      ElMessage.error('获取角色列表失败')
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('网络错误：无法获取角色列表')
  } finally {
    listLoading.value = false
  }
}

const fetchMenuTree = async () => {
  try {
    const res = await getMenuTree()
    console.log('菜单树响应:', res) // 调试日志
    
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
      
      menuTreeData.value = rawData
      allMenuData.value = flattenTree(menuTreeData.value)
      console.log('解析后的菜单树数据:', menuTreeData.value) // 调试日志
    } else {
      console.error('获取菜单树失败:', res?.msg || '未知错误')
      ElMessage.error('获取菜单树失败')
    }
  } catch (error) {
    console.error('加载菜单树失败:', error)
    ElMessage.error('网络错误：无法加载菜单树')
  }
}

// --- 事件处理 ---
const handleCreate = () => {
  temp.value = initTemp()
  dialogStatus.value = 'create'
  dialogFormVisible.value = true
  nextTick(() => dataForm.value.clearValidate())
}

const handleUpdate = (row) => {
  temp.value = Object.assign({}, row)
  dialogStatus.value = 'update'
  dialogFormVisible.value = true
  nextTick(() => dataForm.value.clearValidate())
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除角色 "${row.roleName}" 吗? 这将清除该角色的所有权限配置。`, '危险操作', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRole(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除角色失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  })
}

const createData = () => {
  dataForm.value.validate(async (valid) => {
    if (valid) {
      try {
        await addRole(temp.value)
        dialogFormVisible.value = false
        ElMessage.success('创建成功')
        getList()
      } catch (error) {
        console.error('创建角色失败:', error)
        ElMessage.error('创建失败，请重试')
      }
    }
  })
}

const updateData = () => {
  dataForm.value.validate(async (valid) => {
    if (valid) {
      try {
        await updateRole(temp.value)
        dialogFormVisible.value = false
        ElMessage.success('更新成功')
        getList()
      } catch (error) {
        console.error('更新角色失败:', error)
        ElMessage.error('更新失败，请重试')
      }
    }
  })
}

// --- 权限分配相关 ---
const handleAssign = async (row) => {
  currentRoleId.value = row.id
  try {
    const res = await getAssignedMenus(row.id)
    console.log('已分配菜单响应:', res) // 调试日志
    
    if (res && res.code === 200) {
      const assignedMenuIds = Array.isArray(res.data) ? res.data : [res.data]
      const leafNodeIds = assignedMenuIds.filter(id => {
        const node = allMenuData.value.find(menu => menu.id === id)
        return node && (!node.children || node.children.length === 0)
      })
      dialogAssignVisible.value = true
      nextTick(() => {
        menuTreeRef.value.setCheckedKeys(leafNodeIds, false)
        console.log('设置选中菜单项:', leafNodeIds) // 调试日志
      })
    } else {
      console.error('获取权限数据失败:', res?.msg || '未知错误')
      ElMessage.error('获取权限数据失败')
    }
  } catch (error) {
    console.error('获取权限数据失败:', error)
    ElMessage.error('网络错误：无法获取权限数据')
  }
}

const assignPermissions = async () => {
  const checkedKeys = menuTreeRef.value.getCheckedKeys()
  const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
  const allCheckedIds = [...checkedKeys, ...halfCheckedKeys]
  
  console.log('分配权限数据:', {
    roleId: currentRoleId.value,
    menuIds: allCheckedIds
  }) // 调试日志

  try {
    await assignMenus({
      roleId: currentRoleId.value,
      menuIds: allCheckedIds
    })
    ElMessage.success('权限分配成功')
    dialogAssignVisible.value = false
  } catch (error) {
    console.error('分配权限失败:', error)
    ElMessage.error('分配失败，请重试')
  }
}

// --- 工具函数 ---
const formatDateTime = (row, column, cellValue) => {
  if (!cellValue) return ''
  return cellValue.replace('T', ' ')
}

const flattenTree = (tree) => {
  let result = []
  tree.forEach(node => {
    result.push({ id: node.id, children: node.children }) // 只存需要的信息
    if (node.children && node.children.length > 0) {
      result = result.concat(flattenTree(node.children))
    }
  })
  return result
}
</script>

<style scoped>
.app-container { padding: 20px; }
.filter-container { margin-bottom: 20px; }
.table-container { margin-bottom: 20px; }
</style>