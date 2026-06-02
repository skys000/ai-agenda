<template>
  <div class="app-container">
    <el-card class="filter-container" shadow="sm">
      <div class="filter-form">
        <el-input v-model="listQuery.sessionId" placeholder="按会话ID搜索" style="width: 250px;" class="filter-item" @keyup.enter="handleSearch" clearable />
        <el-button class="filter-item" type="primary" icon="Search" @click="handleSearch">搜索</el-button>
      </div>
    </el-card>

    <el-card class="table-container" shadow="sm">
      <el-table
        :data="list"
        v-loading="listLoading"
        style="width: 100%"
        border
        @sort-change="handleSortChange"
      >
        <el-table-column type="expand">
          <template #default="props">
            <div class="message-detail">
              <pre>{{ props.row.content }}</pre>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用户ID" prop="userId" width="100" align="center" sortable="custom" />
        <el-table-column label="会话ID" prop="sessionId" sortable="custom" />
        <el-table-column label="角色" prop="role" width="120" align="center" sortable="custom">
          <template #default="scope">
            <el-tag :type="scope.row.role === 'user' ? 'success' : 'primary'">
              {{ scope.row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内容摘要" prop="content" :formatter="formatContent" />
        <el-table-column label="发送时间" prop="createTime" width="180" align="center" :formatter="formatDateTime" sortable="custom" />
      </el-table>
    </el-card>
    
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAiMessageList } from '@/api/ai-message'

const list = ref([])
const total = ref(0)
const listLoading = ref(true)
const listQuery = ref({
  pageNum: 1,
  pageSize: 10,
  sessionId: '',
  sortField: 'createTime', // 默认按创建时间
  sortOrder: 'desc'      // 默认降序
})

onMounted(() => { getList() })

const getList = async () => {
  listLoading.value = true
  try {
    const res = await getAiMessageList(listQuery.value)
    
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
      console.error('获取AI会话消息失败:', res?.msg || '未知错误')
      ElMessage.error('获取AI会话消息失败')
    }
  } catch (error) {
    console.error('加载AI会话消息失败:', error)
    ElMessage.error('网络错误：无法加载AI会话消息')
  } finally {
    listLoading.value = false
  }
}

// 事件处理 (与 UserView 模板一致)
const handleSearch = () => { listQuery.value.pageNum = 1; getList() }
const handleSizeChange = (val) => { listQuery.value.pageSize = val; getList() }
const handleCurrentChange = (val) => { listQuery.value.pageNum = val; getList() }

// 【新增】排序事件处理函数
const handleSortChange = ({ prop, order }) => {
  if (order === 'ascending') {
    listQuery.value.sortOrder = 'asc'
  } else if (order === 'descending') {
    listQuery.value.sortOrder = 'desc'
  } else {
    // 如果取消排序，恢复默认
    listQuery.value.sortField = 'createTime'
    listQuery.value.sortOrder = 'desc'
  }
  
  if (prop) {
    listQuery.value.sortField = prop
  }

  // 重新从第一页开始加载数据
  listQuery.value.pageNum = 1
  getList()
}

// 工具函数
const formatDateTime = (row, col, val) => val ? val.replace('T', ' ') : ''
const formatContent = (row, col, val) => {
  // 生成内容摘要，超过50个字符就截断
  if (!val) return ''
  return val.length > 50 ? val.substring(0, 50) + '...' : val
}
</script>

<style scoped>
.app-container { padding: 20px; }
.filter-container { margin-bottom: 20px; }
.table-container { margin-bottom: 20px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.message-detail {
  padding: 10px 20px;
  background-color: #f4f4f5;
  border-radius: 4px;
}
.message-detail pre {
  white-space: pre-wrap; /* 保持换行 */
  word-wrap: break-word; /* 单词过长时换行 */
  margin: 0;
  font-family: 'Courier New', Courier, monospace;
}
</style>
