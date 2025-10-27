<template>
    <div class="login-container">
        <div class="login-form">
            <div class="login-header">
                <img src="@/assets/logo.png" alt="logo" class="logo" />
                <h2 class="title">农作物种质资源数字化溯源系统</h2>
                <p class="subtitle">Crop Seed Germplasm Digital Traceability System</p>
            </div>

            <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form-content"
                @keyup.enter="handleLogin">
                <el-form-item prop="username">
                    <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" size="large" />
                </el-form-item>

                <el-form-item prop="password">
                    <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock"
                        size="large" show-password />
                </el-form-item>

                <el-form-item prop="captcha" v-if="showCaptcha">
                    <div class="captcha-container">
                        <el-input v-model="loginForm.captcha" placeholder="请输入验证码" prefix-icon="Picture" size="large" />
                        <img :src="captchaUrl" alt="验证码" class="captcha-image" @click="refreshCaptcha" />
                    </div>
                </el-form-item>

                <el-form-item>
                    <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" size="large" :loading="loading" @click="handleLogin" class="login-button">
                        {{ loading ? '登录中...' : '登录' }}
                    </el-button>
                </el-form-item>
            </el-form>

            <div class="login-footer">
                <p class="copyright">© 2025 农作物种质资源数字化溯源系统. All rights reserved.</p>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const loginFormRef = ref()

// 加载状态
const loading = ref(false)

// 验证码相关
const showCaptcha = ref(false)
const captchaUrl = ref('')

// 登录表单
const loginForm = reactive({
    username: 'admin',
    password: 'admin123',
    captcha: '',
    rememberMe: false
})

// 表单验证规则
const loginRules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
    ],
    captcha: [
        { required: true, message: '请输入验证码', trigger: 'blur' }
    ]
}

// 处理登录
const handleLogin = async () => {
    if (!loginFormRef.value) return

    try {
        await loginFormRef.value.validate()
        loading.value = true

        await userStore.loginAction(loginForm)

        ElMessage.success('登录成功')
        router.push('/')
    } catch (error) {
        console.error('登录失败:', error)
        ElMessage.error(error.message || '登录失败')
    } finally {
        loading.value = false
    }
}

// 刷新验证码
const refreshCaptcha = () => {
    captchaUrl.value = `/api/captcha?t=${Date.now()}`
}

// 初始化
onMounted(() => {
    // 如果已登录，直接跳转到首页
    if (userStore.token) {
        router.push('/')
    }
})
</script>

<style lang="scss" scoped>
.login-container {
    height: 100vh;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;

    .login-form {
        width: 400px;
        background: rgba(255, 255, 255, 0.95);
        border-radius: 10px;
        padding: 40px;
        box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
        backdrop-filter: blur(10px);

        .login-header {
            text-align: center;
            margin-bottom: 30px;

            .logo {
                width: 60px;
                height: 60px;
                margin-bottom: 20px;
            }

            .title {
                font-size: 24px;
                font-weight: 600;
                color: #333;
                margin-bottom: 10px;
            }

            .subtitle {
                font-size: 14px;
                color: #666;
                margin: 0;
            }
        }

        .login-form-content {
            .captcha-container {
                display: flex;
                gap: 10px;

                .captcha-image {
                    width: 120px;
                    height: 40px;
                    border-radius: 4px;
                    cursor: pointer;
                    border: 1px solid #dcdfe6;
                }
            }

            .login-button {
                width: 100%;
                height: 45px;
                font-size: 16px;
                font-weight: 500;
            }
        }

        .login-footer {
            text-align: center;
            margin-top: 30px;

            .copyright {
                font-size: 12px;
                color: #999;
                margin: 0;
            }
        }
    }
}

// 响应式设计
@media (max-width: 768px) {
    .login-container {
        padding: 20px;

        .login-form {
            width: 100%;
            max-width: 400px;
            padding: 30px 20px;
        }
    }
}
</style>
