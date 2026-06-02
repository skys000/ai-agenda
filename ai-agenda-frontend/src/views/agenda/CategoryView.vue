<template>
  <div class="app-container">
    <el-card class="filter-container" shadow="sm">
      <el-button type="success" icon="Plus" @click="handleCreate">新增分类</el-button>
    </el-card>

    <el-card class="table-container" shadow="sm">
      <el-table :data="list" v-loading="listLoading" style="width: 100%" border>
        <el-table-column label="ID" prop="id" width="80" align="center" />
        <el-table-column label="分类名称" prop="name" align="center" />
        <el-table-column label="颜色" prop="color" align="center">
          <template #default="scope">
            <div style="display: flex; align-items: center; justify-content: center;">
              <!-- 用一个 div 模拟颜色块 -->
              <div :style="{ 
                  width: '24px', 
                  height: '24px', 
                  backgroundColor: scope.row.color, 
                  borderRadius: '4px', 
                  border: '1px solid #dcdfe6' 
                }" 
              ></div> 
              <span style="margin-left: 10px">{{ scope.row.color }}</span> 
            </div>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" align="center" :formatter="formatDateTime" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialogStatus==='create' ? '新增分类' : '编辑分类'" v-model="dialogFormVisible" width="500px">
      <el-form ref="dataForm" :rules="rules" :model="temp" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="temp.name" />
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <el-color-picker v-model="temp.color" show-alpha />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="dialogStatus==='create' ? createData() : updateData()">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getCategoryList,
  addCategory,
  updateCategory,
  deleteCategory
} from '@/api/category'

const list = ref([])
const listLoading = ref(true)
const dialogFormVisible = ref(false)
const dialogStatus = ref('')
const dataForm = ref(null)

const initTemp = () => ({ id: undefined, name: '', color: '#409EFF' })
const temp = ref(initTemp())

const rules = reactive({
  name: [{ required: true, message: '分类名称是必填项', trigger: 'blur' }]
})

onMounted(() => { getList() })

const getList = async () => {
  listLoading.value = true
  try {
    // 分类列表通常不分页，一次性加载
    const res = await getCategoryList()
    console.log('分类列表响应:', res) // 调试日志
    
    if (res && res.code === 200) {
      // 兼容多种数据格式
      if (Array.isArray(res.data)) {
        list.value = res.data
      } else if (res.data && Array.isArray(res.data.records)) {
        list.value = res.data.records
      } else {
        list.value = res.data ? [res.data] : []
      }
      
      console.log('解析后的分类列表:', list.value) // 调试日志
    } else {
      console.error('获取分类列表失败:', res?.msg || '未知错误')
      ElMessage.error('获取分类列表失败')
    }
  } catch (error) {
    console.error('加载分类列表失败:', error)
    ElMessage.error('网络错误：无法加载分类列表')
  } finally {
    listLoading.value = false
  }
}

// --- CRUD 操作 (与 UserView 模板基本一致) ---

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
  ElMessageBox.confirm(`确定删除分类 "${row.name}" 吗?`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteCategory(row.id)
        ElMessage.success('删除成功')
        getList()
      } catch (error) {
        console.error('删除分类失败:', error)
        ElMessage.error('删除失败，请重试')
      }
    }).catch(() => {})
}

const createData = () => {
  dataForm.value.validate(async (valid) => {
    if (valid) {
      try {
        await addCategory(temp.value)
        dialogFormVisible.value = false
        ElMessage.success('创建成功')
        getList()
      } catch (error) {
        console.error('创建分类失败:', error)
        ElMessage.error('创建失败，请重试')
      }
    }
  })
}

const updateData = () => {
  dataForm.value.validate(async (valid) => {
    if (valid) {
      try {
        await updateCategory(temp.value)
        dialogFormVisible.value = false
        ElMessage.success('更新成功')
        getList()
      } catch (error) {
        console.error('更新分类失败:', error)
        ElMessage.error('更新失败，请重试')
      }
    }
  })
}

// 工具函数
const formatDateTime = (row, col, val) => val ? val.replace('T', ' ') : ''
</script>

<style scoped>
.app-container { padding: 20px; }
.filter-container { margin-bottom: 20px; }
.table-container { margin-bottom: 20px; }
</style>