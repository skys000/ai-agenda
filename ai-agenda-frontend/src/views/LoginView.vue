<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>智能日程管家 - 登录</span>
        </div>
      </template>
      <el-form 
        ref="loginFormRef" 
        :model="loginForm" 
        :rules="loginRules" 
        label-width="80px"
        @keyup.enter="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input 
            type="password" 
            v-model="loginForm.password" 
            placeholder="请输入密码" 
            prefix-icon="Lock"
            show-password 
          />
        </el-form-item>
        <el-form-item>
          <el-button 
            style="width: 100%;"
            type="primary" 
            @click="handleLogin" 
            :loading="loading"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { loginApi } from '@/api/user';

const router = useRouter();
const loginFormRef = ref(null);
const loading = ref(false);

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: '',
});

// 表单校验规则
const loginRules = reactive({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
});

// 登录按钮点击事件
const handleLogin = async () => {
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        console.log("正在请求登录...");
        const res = await loginApi(loginForm);
        console.log("登录接口返回:", res);

        // 【核心修复点】
        // 你的 request.js 返回的是 res (整个响应对象)
        // 后端结构是: { code: 200, data: { token: "..." } }
        // 所以 Token 在 res.data.token
        
        if (res.data && res.data.token) {
          // 1. 存入 Token
          localStorage.setItem('token', res.data.token);
          
          // 2. 存入用户名
          localStorage.setItem('username', loginForm.username);
          console.log("Token 和用户名已写入 LocalStorage:", {
            token: localStorage.getItem('token'),
            username: localStorage.getItem('username')
          });
          
          // 3. 提示成功
          ElMessage.success('登录成功！');
          
          // 4. 跳转 (使用 replace 防止回退到登录页)
          router.replace('/agenda/schedule');
        } else {
          console.error("登录成功但没有获取到Token!", res);
          ElMessage.error("系统异常：未获取到令牌");
        }
        
      } catch (error) {
        console.error('登录流程出错:', error);
        
        // 根据错误类型显示不同的提示信息
        if (error.response) {
          // 后端返回的错误
          const errorMsg = error.response.data?.msg || error.response.data?.message || '登录失败，请检查用户名和密码';
          ElMessage.error(errorMsg);
        } else if (error.request) {
          // 网络错误
          ElMessage.error('网络连接失败，请检查网络连接');
        } else {
          // 其他错误
          ElMessage.error('登录失败，请重试');
        }
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
  /* background-image: url('@/assets/login-bg.svg'); /* 你可以准备一张背景图 */
  background-size: cover;
}
.login-card {
  width: 450px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.card-header {
  text-align: center;
  font-size: 20px;
  font-weight: bold;
}
</style>