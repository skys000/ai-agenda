<template>
  <div class="login-container">
    <section class="brand-panel">
      <div class="brand-mark">
        <el-icon><Calendar /></el-icon>
      </div>
      <h1>智能日程管家</h1>
      <p>面向个人日程管理场景的 AI 对话助手，支持自然语言创建日程、多会话记录和后台管理。</p>
      <div class="feature-list">
        <span>Spring AI</span>
        <span>Function Calling</span>
        <span>Vue3</span>
      </div>
    </section>

    <el-card class="login-card" shadow="always">
      <h2>账号登录</h2>
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
            class="login-button"
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

const loginForm = reactive({
  username: '',
  password: '',
});

const loginRules = reactive({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
});

const handleLogin = async () => {
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const res = await loginApi(loginForm);
        if (res.data && res.data.token) {
          localStorage.setItem('token', res.data.token);
          localStorage.setItem('username', loginForm.username);
          ElMessage.success('登录成功！');
          router.replace('/agenda/schedule');
        } else {
          ElMessage.error("系统异常：未获取到令牌");
        }
      } catch (error) {
        if (error.response) {
          const errorMsg = error.response.data?.msg || error.response.data?.message || '登录失败，请检查用户名和密码';
          ElMessage.error(errorMsg);
        } else if (error.request) {
          ElMessage.error('网络连接失败，请检查网络连接');
        } else {
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
  gap: 72px;
  height: 100vh;
  padding: 48px;
  background:
    radial-gradient(circle at 20% 20%, rgba(22, 119, 255, 0.18), transparent 32%),
    linear-gradient(135deg, #eef4ff 0%, #f7f9fc 52%, #eefaf4 100%);
}
.brand-panel {
  width: 460px;
  color: #172033;
}
.brand-mark {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  border-radius: 18px;
  color: #fff;
  font-size: 30px;
  background: #1677ff;
  box-shadow: 0 18px 40px rgba(22, 119, 255, 0.28);
}
.brand-panel h1 {
  margin: 0 0 16px;
  font-size: 44px;
  line-height: 1.18;
}
.brand-panel p {
  margin: 0;
  font-size: 16px;
  line-height: 1.8;
  color: #4b5563;
}
.feature-list {
  display: flex;
  gap: 12px;
  margin-top: 28px;
}
.feature-list span {
  padding: 8px 12px;
  border: 1px solid rgba(22, 119, 255, 0.18);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  color: #1f4f8f;
  font-size: 13px;
}
.login-card {
  width: 420px;
  padding: 28px 24px 18px;
  border-radius: 12px;
}
.login-card h2 {
  margin: 0 0 28px;
  text-align: left;
  font-size: 24px;
  color: #111827;
}
.login-button {
  width: 100%;
  height: 42px;
  font-weight: 600;
}
</style>
