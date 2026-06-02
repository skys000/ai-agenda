<template>
  <div class="profile-container">
    <!-- 个人中心悬浮框 -->
    <el-card class="profile-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </div>
      </template>

      <!-- 用户信息展示 -->
      <div class="user-info">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentUser.nickname }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentUser.email }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag v-for="roleId in userRoles" :key="roleId" style="margin-right: 5px;">{{ getRoleName(roleId) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentUser.createTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="primary" icon="Edit" @click="handleEdit">编辑信息</el-button>
      </div>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog title="编辑个人信息" v-model="dialogFormVisible" width="500px">
      <el-form ref="dataForm" :rules="rules" :model="temp" label-position="left" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="temp.username" disabled />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="temp.password" 
            type="password" 
            placeholder="不填则不修改密码" 
            show-password
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
        <el-button type="primary" @click="updateData">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { getUserList, updateUser, getAssignedRoles } from '@/api/user'
import { getRoleList } from '@/api/role'

// 当前用户信息
const currentUser = ref({})
const userRoles = ref([]) // 用户角色信息
const allRoles = ref([]) // 所有角色列表

// 对话框相关
const dialogFormVisible = ref(false)
const dataForm = ref(null)

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
  nickname: [{ required: true, message: '昵称是必填项', trigger: 'blur' }],
  email: [
    { required: true, message: '邮箱是必填项', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
})

// 生命周期钩子
onMounted(() => {
  loadCurrentUser()
  getAllRoles() // 加载所有角色信息
})

// 加载当前用户信息
const loadCurrentUser = async () => {
  try {
    // 从localStorage中获取用户名
    const currentUsername = localStorage.getItem('username')
    
    if (!currentUsername) {
      ElMessage.error('未找到用户信息，请重新登录')
      return
    }
    
    const res = await getUserList({ pageNum: 1, pageSize: 10, username: currentUsername })
    
    if (res && res.code === 200) {
      let userData = null
      
      if (res.data && res.data.records && res.data.records.length > 0) {
        userData = res.data.records.find(user => user.username === currentUsername)
      } else if (Array.isArray(res.data) && res.data.length > 0) {
        userData = res.data.find(user => user.username === currentUsername)
      }
      
      if (userData) {
        currentUser.value = userData
        // 获取用户角色信息
        await loadUserRoles(userData.id)
      } else {
        ElMessage.error('获取当前用户信息失败')
      }
    } else {
      ElMessage.error('获取用户信息失败')
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('网络错误：无法加载用户信息')
  }
}

// 加载用户角色信息
const loadUserRoles = async (userId) => {
  try {
    const res = await getAssignedRoles(userId)
    
    if (res && res.code === 200) {
      userRoles.value = Array.isArray(res.data) ? res.data : [res.data]
    } else {
      console.error('获取用户角色信息失败:', res?.msg || '未知错误')
      userRoles.value = []
    }
  } catch (error) {
    console.error('加载用户角色信息失败:', error)
    userRoles.value = []
  }
}

// 获取所有角色信息
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
      allRoles.value = []
    }
  } catch (error) {
    console.error('加载角色列表失败:', error)
    allRoles.value = []
  }
}

// 编辑个人信息
const handleEdit = () => {
  temp.value = Object.assign({}, currentUser.value)
  temp.value.password = '' // 编辑时不回显密码
  dialogFormVisible.value = true
  // 移除对 password 的校验规则，因为编辑时非必填
  delete rules.password
  // 清除上次的校验结果
  nextTick(() => {
    if (dataForm.value) {
      dataForm.value.clearValidate()
    }
  })
}

// 提交编辑信息
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
        loadCurrentUser() // 重新加载用户信息
      } catch (error) {
        console.error('更新用户失败:', error)
        ElMessage.error('更新失败，请重试')
      }
    }
  })
}

// 工具函数
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return dateTime.replace('T', ' ')
}

// 获取角色名称（根据角色ID从角色列表中查找）
const getRoleName = (roleId) => {
  const role = allRoles.value.find(r => r.id === roleId)
  return role ? role.roleName || role.name || `角色${roleId}` : `角色${roleId}`
}
</script>

<style scoped>
.profile-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 400px;
  padding: 40px 20px;
}

.profile-card {
  width: 600px;
  max-width: 90vw;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.profile-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.user-info {
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-top: 20px;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  color: #606266;
}

:deep(.el-descriptions__content) {
  color: #303133;
}
</style>