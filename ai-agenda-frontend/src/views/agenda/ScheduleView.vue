<template>
  <div class="app-container">
    <el-card shadow="sm" class="main-card">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        
        <!-- === 视图 1: 智能日历 === -->
        <el-tab-pane label="日历看板" name="calendar">
          <div class="calendar-container">
            <FullCalendar ref="fullCalendarRef" :options="calendarOptions" />
          </div>
        </el-tab-pane>

        <!-- === 视图 2: 列表管理 (经典的 CRUD) === -->
        <el-tab-pane label="列表管理" name="list">
          <div class="filter-form">
            <el-input v-model="listQuery.title" placeholder="搜索日程标题" style="width: 160px;" class="filter-item" clearable />
            <el-select v-model="listQuery.categoryId" placeholder="按分类筛选" clearable class="filter-item" style="width: 130px; margin: 0 10px;">
              <el-option v-for="cat in categoryList" :key="cat.id" :label="cat.name" :value="cat.id" />
            </el-select>
            <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
            <el-button type="success" icon="Plus" @click="handleCreate">新增</el-button>
          </div>

          <el-table :data="list" v-loading="listLoading" style="width: 100%; margin-top: 20px" border stripe>
            <el-table-column label="标题" prop="title" />
            
            <el-table-column label="分类" align="center" width="120">
              <template #default="scope">
                  <el-tag :color="scope.row.categoryColor" effect="dark" v-if="scope.row.categoryName">
                    {{ scope.row.categoryName }}
                  </el-tag>
                  <span v-else>未分类</span>
              </template>
            </el-table-column>

            <el-table-column label="开始时间" prop="startTime" width="180" :formatter="formatDateTime" />
            <el-table-column label="结束时间" prop="endTime" width="180" :formatter="formatDateTime" />
            
            <el-table-column label="状态" width="120" align="center">
              <template #default="scope">
                <el-switch
                  v-model="scope.row.status"
                  :active-value="1"
                  :inactive-value="0"
                  @change="handleStatusChange(scope.row)"
                  :disabled="scope.row.status === 2"
                />
                <el-tag v-if="scope.row.status === 2" type="info">已取消</el-tag>
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="180" align="center">
              <template #default="scope">
                <el-button size="small" type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页器 -->
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
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 新增/编辑日程对话框 -->
    <!-- 新增/编辑日程对话框 (最终增强版) -->
    <el-dialog :title="dialogStatus==='create'?'新增日程':'编辑日程'" v-model="dialogFormVisible" width="600px">
      <el-form ref="dataForm" :rules="rules" :model="temp" label-width="100px">
        <el-form-item label="日程标题" prop="title">
          <el-input v-model="temp.title" placeholder="例如：项目周会" />
        </el-form-item>
        
        <!-- 【升级】分类选择 -->
        <el-form-item label="日程分类" prop="categoryId">
          <el-select v-model="temp.categoryId" placeholder="请选择分类" style="width: 100%" clearable>
            <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="时间安排" required>
          <el-date-picker
            v-model="tempTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <el-row>
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-radio-group v-model="temp.priority">
                <el-radio :label="1">低</el-radio>
                <el-radio :label="2">中</el-radio>
                <el-radio :label="3">高</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 【升级】状态选择 (只在编辑时显示) -->
            <el-form-item label="日程状态" v-if="dialogStatus === 'update'">
              <el-select v-model="temp.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="未完成" :value="0" />
                <el-option label="已完成" :value="1" />
                <el-option label="已取消" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="备注详情">
          <el-input v-model="temp.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="dialogStatus==='create'?createData():updateData()">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import { 
  getScheduleList, 
  getCategoryList, 
  addSchedule, 
  updateSchedule, 
  deleteSchedule 
} from '@/api/schedule'

// --- 状态定义 ---
const activeTab = ref('calendar')
const fullCalendarRef = ref(null)
const list = ref([])
const total = ref(0)
const listLoading = ref(false)
const categoryList = ref([]) // 分类列表
// 升级 listQuery
const listQuery = ref({
  pageNum: 1,
  pageSize: 10,
  title: '',
  categoryId: null
})

// 弹窗相关
const dialogFormVisible = ref(false)
const dialogStatus = ref('')
const dataForm = ref(null)
const temp = ref({ id: undefined, title: '', startTime: '', endTime: '', priority: 2, description: '', categoryId: null })
const tempTimeRange = ref([]) // 用于绑定 DatePicker 数组

// 表单校验
const rules = reactive({
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }]
})

// --- FullCalendar 配置 ---
const calendarOptions = reactive({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth,timeGridWeek,timeGridDay'
  },
  locale: 'zh-cn',
  buttonText: { today: '今天', month: '月', week: '周', day: '日' },
  // 添加中文文本配置
  allDayText: '全天',
  allDayHtml: '全天',
  // 设置时间显示格式，显示分钟
  slotLabelFormat: {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  },
  // 设置事件时间显示格式
  eventTimeFormat: {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  },
  editable: true, // 允许拖拽
  selectable: true, // 允许选中日期
  dayMaxEvents: true, // 事件太多时折叠
  
  // 【核心修复】指定时区为 'local' 
  timeZone: 'local', 
  
  // 设置时间范围，显示完整24小时
  slotMinTime: '00:00:00', // 从0点开始
  slotMaxTime: '24:00:00', // 到24点结束
  
  // 并且确保事件源的时间字符串不带 'Z' 或时区偏移 
  events: [], // 事件数据源
  
  // 点击日期空白处 -> 新增
  dateClick: (info) => {
    handleCreate()
    // 自动填充点击的日期
    const clickedDate = info.dateStr
    tempTimeRange.value = [`${clickedDate} 09:00:00`, `${clickedDate} 10:00:00`]
  },
  
  // 点击具体事件 -> 编辑
  eventClick: (info) => {
    const eventObj = info.event
    // 把 FullCalendar 的事件对象转换回我们的 temp 对象
    temp.value = {
      id: parseInt(eventObj.id),
      title: eventObj.title,
      startTime: formatDate(eventObj.start),
      endTime: eventObj.end ? formatDate(eventObj.end) : formatDate(eventObj.start),
      priority: eventObj.extendedProps.priority || 2,
      description: eventObj.extendedProps.description || '',
      categoryId: eventObj.extendedProps.categoryId || null,
      status: eventObj.extendedProps.status || 0
    }
    // 回显时间选择器
    tempTimeRange.value = [temp.value.startTime, temp.value.endTime]
    
    dialogStatus.value = 'update'
    dialogFormVisible.value = true
  },
  
  eventDrop: async (info) => {
    const eventObj = info.event
    const newStartTime = formatDate(eventObj.start)
    const newEndTime = eventObj.end ? formatDate(eventObj.end) : formatDate(eventObj.start)
    
    const updatedSchedule = {
      id: parseInt(eventObj.id),
      title: eventObj.title,
      startTime: newStartTime,
      endTime: newEndTime,
      priority: eventObj.extendedProps.priority || 2,
      description: eventObj.extendedProps.description || '',
      categoryId: eventObj.extendedProps.categoryId || null,
      status: eventObj.extendedProps.status || 0
    }
    
    try {
      await updateSchedule(updatedSchedule)
      ElMessage.success('日程时间已更新')
    } catch (error) {
      console.error('拖动更新失败:', error)
      ElMessage.error('更新失败，已恢复原位置')
      info.revert()
    }
  },
  
  eventResize: async (info) => {
    const eventObj = info.event
    const newStartTime = formatDate(eventObj.start)
    const newEndTime = eventObj.end ? formatDate(eventObj.end) : formatDate(eventObj.start)
    
    const updatedSchedule = {
      id: parseInt(eventObj.id),
      title: eventObj.title,
      startTime: newStartTime,
      endTime: newEndTime,
      priority: eventObj.extendedProps.priority || 2,
      description: eventObj.extendedProps.description || '',
      categoryId: eventObj.extendedProps.categoryId || null,
      status: eventObj.extendedProps.status || 0
    }
    
    try {
      await updateSchedule(updatedSchedule)
      ElMessage.success('日程时间已更新')
    } catch (error) {
      console.error('调整大小更新失败:', error)
      ElMessage.error('更新失败，已恢复原大小')
      info.revert()
    }
  }
})

onMounted(async () => {
  try {
    await Promise.all([
      getList(),
      fetchCategoryList(),
      fetchCalendarEvents()
    ])
  } catch (error) {
    ElMessage.error('页面初始化失败，请检查网络连接或后端服务')
    console.error('初始化加载错误:', error)
  }
})

const fetchCalendarEvents = async () => {
  try {
    const res = await getScheduleList({ pageSize: 100 })
    
    let records = [];
    // 检查返回的数据结构，兼容多种格式
    if (res && res.data) {
      if (res.data.records) {
        records = res.data.records;
      } else if (Array.isArray(res.data)) {
        records = res.data;
      } else {
        records = [res.data];
      }
    } else if (Array.isArray(res)) {
      records = res;
    }
    
    
    // 映射后端数据 -> FullCalendar 事件格式
    calendarOptions.events = records.map(item => ({
      id: item.id ? item.id.toString() : Math.random().toString(),
      title: item.title || '无标题',
      start: item.startTime || item.start_time,
      end: item.endTime || item.end_time,
      backgroundColor: getEventColor(item),
      borderColor: getEventColor(item),
      extendedProps: {
        priority: item.priority || 2,
        description: item.description || '',
        categoryId: item.categoryId || item.category_id,
        status: item.status || 0
      }
    }))
  } catch (e) { 
    console.error('获取日历事件失败:', e)
    ElMessage.error('获取日历数据失败，请检查网络连接')
  }
}

// 检查 getList 方法
const getList = async () => {
  listLoading.value = true
  try {
    const res = await getScheduleList(listQuery.value)
    
    if (res && res.code === 200) {
      // 检查返回的数据结构，兼容不同格式
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
      
      // 确保列表数据有分类信息
      list.value = list.value.map(item => ({
        ...item,
        categoryName: item.categoryName || item.category_name,
        categoryColor: item.categoryColor || item.category_color
      }))
      
    } else {
      ElMessage.error(res?.msg || '获取日程列表失败')
    }
  } catch (err) {
    console.error('getList API 请求失败:', err)
    ElMessage.error('网络错误：无法获取日程列表')
  } finally {
    listLoading.value = false
  }
}

// 检查 getCategoryList 方法
const fetchCategoryList = async () => {
  try {
    const res = await getCategoryList()
    
    if (res && res.code === 200) {
      categoryList.value = Array.isArray(res.data) ? res.data : [res.data]
    } else {
      ElMessage.error(res?.msg || '获取分类列表失败')
    }
  } catch (err) {
    console.error('getCategoryList API 请求失败:', err)
    ElMessage.error('网络错误：无法获取分类列表')
  }
}

// 3. 提交新增
const createData = () => {
  dataForm.value.validate(async (valid) => {
    if (valid && validateTime()) {
      syncTimeFromRange()
      try {
        await addSchedule(temp.value)
        dialogFormVisible.value = false
        ElMessage.success('添加成功')
        refreshAll()
      } catch (error) {
        console.error('添加日程失败:', error)
        ElMessage.error('添加失败，请重试')
      }
    }
  })
}

// 4. 提交修改
const updateData = () => {
  dataForm.value.validate(async (valid) => {
    if (valid && validateTime()) {
      syncTimeFromRange()
      try {
        await updateSchedule(temp.value)
        dialogFormVisible.value = false
        ElMessage.success('修改成功')
        refreshAll()
      } catch (error) {
        console.error('修改日程失败:', error)
        ElMessage.error('修改失败，请重试')
      }
    }
  })
}

// 5. 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该日程吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteSchedule(row.id)
      ElMessage.success('删除成功')
      refreshAll()
    } catch (error) {
      console.error('删除日程失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  })
}

// --- 辅助函数 ---

const refreshAll = () => {
  fetchCalendarEvents()
  getList()
}

// 校验时间选择器
const validateTime = () => {
  if (!tempTimeRange.value || tempTimeRange.value.length < 2) {
    ElMessage.warning('请选择时间范围')
    return false
  }
  return true
}

// 将 DatePicker 的数组同步到 temp 对象
const syncTimeFromRange = () => {
  temp.value.startTime = tempTimeRange.value[0]
  temp.value.endTime = tempTimeRange.value[1]
}

const handleTabClick = (tab) => {
  if (tab.paneName === 'calendar') {
    // 切换回来时可能需要重新渲染日历以防样式错乱
    setTimeout(() => {
      const calendarApi = fullCalendarRef.value.getApi()
      calendarApi.updateSize()
    }, 100)
  }
}

// 【升级】日历事件颜色，加入状态判断
const getEventColor = (item) => {
  if (item.status === 1) return '#909399' // 已完成-灰色
  if (item.status === 2) return '#C0C4CC' // 已取消-更浅的灰色
  if (item.priority === 3) return '#F56C6C' // 高-红
  if (item.priority === 2) return '#409EFF' // 中-蓝
  return '#67C23A' // 低-绿
}
const getPriorityText = (p) => p === 3 ? '高' : (p === 2 ? '中' : '低')
const getPriorityTag = (p) => p === 3 ? 'danger' : (p === 2 ? 'primary' : 'success')

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  
  // 手动拼接，确保不受时区影响
  const year = d.getFullYear()
  const month = (d.getMonth() + 1).toString().padStart(2, '0')
  const day = d.getDate().toString().padStart(2, '0')
  const hours = d.getHours().toString().padStart(2, '0')
  const minutes = d.getMinutes().toString().padStart(2, '0')
  const seconds = d.getSeconds().toString().padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}
const formatDateTime = (row, col, val) => val ? val.replace('T', ' ') : ''

// 【新增】处理状态变更的方法
const handleStatusChange = async (row) => {
  const newStatus = row.status === 1 ? '完成' : '未完成'
  try {
    await updateSchedule({ id: row.id, status: row.status })
    ElMessage.success(`状态已更新为: ${newStatus}`)
    // 刷新日历，让已完成的事件变色
    fetchCalendarEvents()
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
    // 失败时把开关拨回去
    row.status = row.status === 1 ? 0 : 1
  }
}

// 其他 CRUD 辅助
const handleSearch = () => { 
  listQuery.value.pageNum = 1; 
  getList() 
}
const handleCurrentChange = (val) => { listQuery.value.pageNum = val; getList() }
const handleSizeChange = (val) => {
  listQuery.value.pageSize = val
  listQuery.value.pageNum = 1  // 改变每页数量后回到第一页
  getList()
}
const handleCreate = () => {
  temp.value = { id: undefined, title: '', startTime: '', endTime: '', priority: 2, description: '', categoryId: null }
  tempTimeRange.value = [] // 重置时间选择器
  dialogStatus.value = 'create'
  dialogFormVisible.value = true
}
const handleUpdate = (row) => {
  temp.value = { 
    ...row,
    categoryId: row.categoryId || null  // 确保分类ID被正确设置
  }
  tempTimeRange.value = [row.startTime, row.endTime]
  dialogStatus.value = 'update'
  dialogFormVisible.value = true
}
</script>

<style scoped>
.app-container { padding: 20px; }
.main-card { min-height: 100vh; }
.calendar-container {
  height: 900px;
  min-height: 900px;
  padding: 10px;
  overflow: visible;
}
.filter-form { 
  margin-bottom: 20px; 
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}
.filter-item {
  margin-right: 0;
}
.pagination-container { margin-top: 20px; text-align: right; }
</style>
