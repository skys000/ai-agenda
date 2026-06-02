<template>
  <div class="chat-container">
    <div class="header">
      <div class="title">AI 日程助手</div>
      <div class="controls">
        <span class="label">流式输出:</span>
        <el-switch v-model="isStream" active-color="#13ce66" />
      </div>
    </div>

    <el-scrollbar ref="scrollbarRef" class="chat-content">
      <div v-for="(item, index) in messageList" :key="index" :class="['message-item', item.role]">
        <el-avatar :size="40" :icon="item.role === 'user' ? 'User' : 'Cpu'" class="avatar" />
        <div class="content-box">
          <div class="role-name">{{ item.role === 'user' ? '我' : 'Agenda AI' }}</div>
          
          <!-- 核心：使用 v-html 和我们自己的渲染函数 -->
          <div class="text markdown-body" v-html="renderMarkdown(item.content)"></div>

        </div>
      </div>
    </el-scrollbar>

    <div class="input-area">
      <el-input v-model="userInput" placeholder="输入指令..." @keyup.enter="handleSend">
        <template #append>
          <el-button @click="handleSend" type="primary">发送</el-button>
        </template>
      </el-input>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { chatWithAI, streamChatWithAI } from '@/api/ai'
// 1. 引入 markdown-it
import MarkdownIt from 'markdown-it'

// 2. 创建一个 markdown-it 实例
const md = new MarkdownIt({
  html: true,
  linkify: true,
  breaks: true,
})

// 3. 创建一个渲染函数
const renderMarkdown = (content) => {
  // 当内容为空时，返回一个“正在输入”的光标效果，体验更好
  if (!content) return '<span class="blinking-cursor"></span>'
  return md.render(content)
}

// (下面的 JS 逻辑保持不变，因为我们的流式接收逻辑是完美的)
const userInput = ref('')
const isStream = ref(true) 
const scrollbarRef = ref(null)
const sessionId = ref('sess_' + Math.floor(Math.random() * 10000))

const messageList = ref([
  { role: 'assistant', content: '**你好！** 我是你的智能日程管家，有什么可以帮你的吗？' }
])

const scrollToBottom = () => {
  nextTick(() => {
    if (scrollbarRef.value) {
      scrollbarRef.value.setScrollTop(9999999)
    }
  })
}

const handleSend = async () => {
  if (!userInput.value.trim()) return
  const userMsg = userInput.value
  messageList.value.push({ role: 'user', content: userMsg })
  userInput.value = ''
  scrollToBottom()

  if (isStream.value) {
    await sendStreamMessage(userMsg)
  } else {
    await sendNormalMessage(userMsg)
  }
}

const sendNormalMessage = async (msg) => {
  // 非流式模式下也显示光标
  messageList.value.push({ role: 'assistant', content: '' })
  const lastIndex = messageList.value.length - 1
  
  try {
    // 检查用户是否登录
    const token = localStorage.getItem('token')
    if (!token) {
      throw new Error('用户未登录，请先登录')
    }
    
    const res = await chatWithAI({ msg, sessionId: sessionId.value })
    
    if (res && res.code === 200) {
      // 模拟流式效果，延迟显示完整内容
      const fullContent = res.data
      let currentIndex = 0
      const interval = setInterval(() => {
        if (currentIndex < fullContent.length) {
          messageList.value[lastIndex].content = fullContent.substring(0, currentIndex + 1)
          currentIndex++
          scrollToBottom()
        } else {
          clearInterval(interval)
        }
      }, 30) // 每30ms显示一个字符，模拟打字效果
    } else {
      messageList.value[lastIndex].content = `抱歉，AI 服务暂时不可用: ${res?.msg || '未知错误'}`
    }
  } catch (error) { 
    messageList.value[lastIndex].content = `抱歉，连接 AI 失败了: ${error.message}`
  } finally {
    scrollToBottom()
  }
}

const sendStreamMessage = async (msg) => {
  messageList.value.push({ role: 'assistant', content: '' })
  const lastIndex = messageList.value.length - 1
  
  try {
    // 获取token
    const token = localStorage.getItem('token')
    if (!token) {
      throw new Error('用户未登录，请先登录')
    }
    
    // 使用配置好的API，携带认证token
    const response = await fetch(`/api/ai/stream?msg=${encodeURIComponent(msg)}&sessionId=${sessionId.value}`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })
    
    if (!response.ok) {
      if (response.status === 403) {
        throw new Error('访问被拒绝，请检查用户权限或重新登录')
      }
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      buffer += decoder.decode(value, { stream: true })
      let lines = buffer.split('\n')
      buffer = lines.pop()
      
      for (const line of lines) {
        // 不要使用 trim()，保留原始空格
        const originalLine = line
        if (!originalLine || !originalLine.startsWith('data:')) continue
        
        // 【修复空格问题】直接截取 data: 后面的内容，保留所有空格
        let content = originalLine.substring(5)
        
        if (content === '[DONE]') continue
        
        // 直接追加，保留所有空格
        messageList.value[lastIndex].content += content
        scrollToBottom()
      }
    }
  } catch (error) {
    messageList.value[lastIndex].content = '连接失败'
  }
}
</script>

<style scoped>
/* (样式可以保持不变，或者用下面的，更简洁) */
.chat-container {
  height: 90vh; display: flex; flex-direction: column;
  max-width: 900px; margin: 20px auto; border: 1px solid #e0e0e0;
  border-radius: 16px; background: #f7f7f7; box-shadow: 0 8px 32px rgba(0,0,0,0.1);
  overflow: hidden;
}
.header {
  padding: 16px 24px; background: #fff;
  display: flex; justify-content: space-between; align-items: center;
  border-bottom: 1px solid #e0e0e0;
}
.markdown-body :deep(ul) {
  padding-left: 20px;
  margin: 5px 0;
}
.markdown-body :deep(li) {
  margin-bottom: 5px;
}
.chat-content { flex: 1; padding: 24px; }
.input-area { padding: 20px; background: #fff; border-top: 1px solid #e0e0e0; }
.message-item {
  display: flex; margin-bottom: 24px;
  width: 100%; align-items: flex-start;
}
.message-item.assistant { flex-direction: row; }
.message-item.assistant .content-box { margin-left: 12px; align-items: flex-start; }
.message-item.user { flex-direction: row-reverse; }
.message-item.user .content-box { margin-right: 12px; align-items: flex-end; }
.content-box { max-width: 80%; display: flex; flex-direction: column; }
.role-name { font-size: 13px; color: #888; margin-bottom: 6px; }
.text {
  padding: 12px 18px; border-radius: 18px;
  font-size: 15px; line-height: 1.7; text-align: left !important;
  word-break: break-word;
}
.message-item.assistant .text {
  background: #fff; color: #333; border-top-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.message-item.user .text {
  background: #95ec69; color: #000; border-top-right-radius: 4px;
}
/* 给 markdown-it 渲染出的 HTML 加一点样式 */
.markdown-body :deep(p) { margin: 0; }
.markdown-body :deep(strong) { color: #1a1a1a; }
/* 正在输入的光标效果 */
.markdown-body :deep(.blinking-cursor) {
  display: inline-block;
  width: 2px;
  height: 1.2em;
  background-color: #666;
  margin-left: 2px;
  vertical-align: middle;
  animation: blink 1s infinite;
  border-radius: 1px;
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
</style>