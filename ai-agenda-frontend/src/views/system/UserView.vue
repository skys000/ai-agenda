<template>
  <div class="app-container">
    <!-- 1. 顶部工具栏 -->
    <el-card class="filter-container" shadow="sm" :body-style="{ padding: '20px' }">
      <div class="filter-form">
        <el-input v-model="listQuery.username" placeholder="按用户名搜索" style="width: 200px;" class="filter-item" @keyup.enter="handleSearch" clearable />
        <el-button class="filter-item" type="primary" icon="Search" @click="handleSearch">搜索</el-button>
        <el-button class="filter-item" type="success" icon="Plus" @click="handleCreate">新增用户</el-button>
      </div>
    </el-card>

    <!-- 2. 主体表格 -->
    <el-card class="table-container" shadow="sm" :body-style="{ padding: '20px' }">
      <el-table :data="list" v-loading="listLoading" style="width: 100%" border>
        <el-table-column label="ID" prop="id" width="80" align="center" />
        <el-table-column label="用户名" prop="username" align="center" />
        <el-table-column label="昵称" prop="nickname" align="center" />
        <el-table-column label="邮箱" prop="email" align="center" />
        <el-table-column label="创建时间" prop="createTime" align="center" :formatter="formatDateTime" />
        <el-table-column label="操作" width="280" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
            <el-button size="small" type="warning" icon="User" @click="handleAssignRole(scope.row)">分配角色</el-button>
            <el-button size="small" type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 3. 底部分页器 -->
    <div class="pagination-container">
      <el-pagination
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="listQuery.pageNum"
        :page-sizes="[10, 20, 50]"
        :page-size="listQuery.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
    </div>

    <!-- 4. 新增/编辑对话框 -->
    <el-dialog :title="dialogStatus==='create' ? '新增用户' : '编辑用户'" v-model="dialogFormVisible" width="500px">
      <el-form ref="dataForm" :rules="rules" :model="temp" label-position="left" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="temp.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="temp.password" 
            type="password" 
            :placeholder="dialogStatus === 'create' ? '请输入密码' : '不填则不修改'" 
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="temp.nickname" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="temp.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="dialogStatus==='create' ? createData() : updateData()">确定</el-button>
      </template>
    </el-dialog>

    <!-- 5. 分配角色对话框 (新增) -->
    <el-dialog title="分配角色" v-model="dialogAssignRoleVisible" width="800px">
      <!-- 【核心修改】外面套一个 div，并用 flex 居中 -->
      <div class="transfer-container">
        <el-transfer
          v-model="assignedRoleIds"
          :data="allRoles"
          :props="{ key: 'id', label: 'roleName' }"
          :titles="['可选角色', '已选角色']"
          filterable
        />
      </div>
      <template #footer>
        <el-button @click="dialogAssignRoleVisible = false">取消</el-button>
        <el-button type="primary" @click="assignUserRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getUserList,
  addUser,
  updateUser,
  deleteUser,
  getAssignedRoles,
  assignRoles
} from '@/api/user'
import { getRoleList } from '@/api/role'

// --- 响应式数据 ---
const list = ref([])
const total = ref(0)
const listLoading = ref(true)
const listQuery = ref({
  pageNum: 1,
  pageSize: 10,
  username: ''
})

const dialogFormVisible = ref(false)
const dialogStatus = ref('')
const dataForm = ref(null) // 表单引用

// --- 新增：分配角色相关的响应式数据 ---
const dialogAssignRoleVisible = ref(false)
const allRoles = ref([]) // 所有的角色列表
const assignedRoleIds = ref([]) // 用户已分配的角色ID列表
const currentUserId = ref(null) // 当前操作的用户ID

// 表单数据模型
const initTemp = () => ({
  id: undefined,
  username: '',
  password: '',
  nickname: '',
  email: ''
})
const temp = ref(initTemp())

// 表单校验规则
const rules = reactive({
  username: [{ required: true, message: '用户名是必填项', trigger: 'blur' }],
  password: [{ required: true, message: '密码是必填项', trigger: 'blur' }]
})


// --- 生命周期钩子 ---
onMounted(() => {
  getList()
  getAllRoles() // 页面加载时就获取所有角色信息
})

// --- API 调用方法 ---
const getList = async () => {
  listLoading.value = true
  try {
    const res = await getUserList(listQuery.value)
    
    if (res && res.code === 200) {
      // 兼容多种数据格式
      if (res.data && res.data.records) {
        list.value = res.data.records
        total.value = res.data.total
      } else if (Array.isArray(res.data)) {
        list.value = res.data
        total.value = res.data.length
      } else {
        list.value = res.data ? [res.data] : []
        total.value = list.value.length
      }
      
    } else {
      console.error('获取用户列表失败:', res?.msg || '未知错误')
      ElMessage.error('获取用户列表失败')
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('网络错误：无法加载用户列表')
  } finally {
    listLoading.value = false
  }
}

const getAllRoles = async () => {
  try {
    const res = await getRoleList()
    
    if (res && res.code === 200) {
      // 兼容多种数据格式
      if (Array.isArray(res.data)) {
        allRoles.value = res.data
      } else if (res.data && Array.isArray(res.data.records)) {
        allRoles.value = res.data.records
      } else {
        allRoles.value = res.data ? [res.data] : []
      }
      
    } else {
      console.error('获取角色列表失败:', res?.msg || '未知错误')
      ElMessage.error('获取角色列表失败')
    }
  } catch (error) {
    console.error('加载角色列表失败:', error)
    ElMessage.error('网络错误：无法加载角色列表')
  }
}

// --- 事件处理方法 ---
const handleSearch = () => {
  listQuery.value.pageNum = 1
  getList()
}

const handleCreate = () => {
  temp.value = initTemp() // 确保表单干净
  dialogStatus.value = 'create'
  dialogFormVisible.value = true
  // 移除对 password 的校验规则，因为新增时才需要
  rules.password = [{ required: true, message: '密码是必填项', trigger: 'blur' }]
  // 清除上次的校验结果
  nextTick(() => {
    dataForm.value.clearValidate()
  })
}

const handleUpdate = (row) => {
  temp.value = Object.assign({}, row)
  temp.value.password = '' // 编辑时不回显密码
  dialogStatus.value = 'update'
  dialogFormVisible.value = true
  // 移除对 password 的校验规则，因为编辑时非必填
  delete rules.password
  // 清除上次的校验结果
  nextTick(() => {
    dataForm.value.clearValidate()
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要永久删除用户 "${row.username}" 吗?`, '危险操作', {
    confirmButtonText: '狠心删除',
    cancelButtonText: '我再想想',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功!')
      getList()
    } catch (error) {
      console.error('删除用户失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

const createData = () => {
  dataForm.value.validate(async (valid) => {
    if (valid) {
      try {
        await addUser(temp.value)
        dialogFormVisible.value = false
        ElMessage.success('用户创建成功')
        getList()
      } catch (error) {
        console.error('创建用户失败:', error)
        ElMessage.error('创建失败，用户名可能已存在')
      }
    }
  })
}

const updateData = () => {
  dataForm.value.validate(async (valid) => {
    if (valid) {
      // 1. 创建一个副本，防止污染原始表单数据
      const tempData = Object.assign({}, temp.value)
      
      // 2. 如果密码框是空的，就从提交对象中删除 password 属性
      // 这样 MyBatis-Plus 就不会把密码更新成空字符串
      if (!tempData.password) {
        delete tempData.password
      }
      
      try {
        // 3. 提交处理过的副本
        await updateUser(tempData)
        dialogFormVisible.value = false
        ElMessage.success('用户信息更新成功')
        getList()
      } catch (error) {
        console.error('更新用户失败:', error)
        ElMessage.error('更新失败，请重试')
      }
    }
  })
}

// --- 分页处理 ---
const handleSizeChange = (val) => {
  listQuery.value.pageSize = val
  getList()
}

const handleCurrentChange = (val) => {
  listQuery.value.pageNum = val
  getList()
}

// --- 新增：分配角色相关的方法 ---
const handleAssignRole = async (row) => {
  currentUserId.value = row.id
  try {
    // 1. 调用回显接口
    const res = await getAssignedRoles(row.id)
    
    if (res && res.code === 200) {
      assignedRoleIds.value = Array.isArray(res.data) ? res.data : [res.data]
      
      // 2. 打开弹窗
      dialogAssignRoleVisible.value = true
    } else {
      console.error('获取用户角色信息失败:', res?.msg || '未知错误')
      ElMessage.error('获取用户角色信息失败')
    }
  } catch (error) {
    console.error('获取用户角色信息失败:', error)
    ElMessage.error('网络错误：无法获取用户角色信息')
  }
}

const assignUserRoles = async () => {
  try {
    await assignRoles({
      userId: currentUserId.value,
      roleIds: assignedRoleIds.value
    })
    ElMessage.success('角色分配成功')
    dialogAssignRoleVisible.value = false
  } catch (error) {
    console.error('分配角色失败:', error)
    ElMessage.error('分配失败，请重试')
  }
}

// --- 工具函数 ---
const formatDateTime = (row, column, cellValue) => {
  if (!cellValue) return ''
  return cellValue.replace('T', ' ')
}

</script>

<style scoped>
/* 新增穿梭框容器样式 */
.transfer-container {
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
}

/* 穿梭框样式微调 */
:deep(.el-transfer-panel) {
  width: 220px;
}
.app-container {
  padding: 20px;
}
.filter-container {
  margin-bottom: 20px;
}
.table-container {
  margin-bottom: 20px;
}
.filter-item {
  margin-right: 10px;
}
.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
