<template>
    <div class="profile-container">
        <el-row :gutter="20">
            <!-- 左侧：头像和基本信息卡片 -->
            <el-col :span="8">
                <el-card class="profile-card">
                    <div class="avatar-section">
                        <el-upload class="avatar-uploader" :show-file-list="false" :before-upload="beforeAvatarUpload"
                            :http-request="handleAvatarUpload">
                            <el-avatar :size="120" :src="userProfile.avatar || defaultAvatar" class="user-avatar">
                                <el-icon :size="60">
                                    <User />
                                </el-icon>
                            </el-avatar>
                            <div class="avatar-overlay">
                                <el-icon>
                                    <Camera />
                                </el-icon>
                                <span>更换头像</span>
                            </div>
                        </el-upload>
                    </div>
                    <div class="user-info">
                        <h2 class="username">{{ userProfile.realName || userProfile.username }}</h2>
                        <p class="user-role">
                            <el-tag v-for="role in userProfile.roles" :key="role" size="small" type="primary"
                                class="role-tag">
                                {{ role }}
                            </el-tag>
                        </p>
                    </div>
                    <el-divider />
                    <div class="info-list">
                        <div class="info-item">
                            <el-icon>
                                <User />
                            </el-icon>
                            <span class="label">用户名</span>
                            <span class="value">{{ userProfile.username }}</span>
                        </div>
                        <div class="info-item">
                            <el-icon>
                                <Phone />
                            </el-icon>
                            <span class="label">手机号</span>
                            <span class="value">{{ userProfile.phone || '未设置' }}</span>
                        </div>
                        <div class="info-item">
                            <el-icon>
                                <Message />
                            </el-icon>
                            <span class="label">邮箱</span>
                            <span class="value">{{ userProfile.email || '未设置' }}</span>
                        </div>
                        <div class="info-item">
                            <el-icon>
                                <Calendar />
                            </el-icon>
                            <span class="label">注册时间</span>
                            <span class="value">{{ userProfile.createTime || '-' }}</span>
                        </div>
                        <div class="info-item">
                            <el-icon>
                                <Clock />
                            </el-icon>
                            <span class="label">最后登录</span>
                            <span class="value">{{ userProfile.lastLoginTime || '-' }}</span>
                        </div>
                    </div>
                </el-card>
            </el-col>

            <!-- 右侧：表单区域 -->
            <el-col :span="16">
                <el-card class="form-card">
                    <template #header>
                        <el-tabs v-model="activeTab">
                            <el-tab-pane label="基本信息" name="profile">
                                <template #label>
                                    <span><el-icon>
                                            <User />
                                        </el-icon> 基本信息</span>
                                </template>
                            </el-tab-pane>
                            <el-tab-pane label="修改密码" name="password">
                                <template #label>
                                    <span><el-icon>
                                            <Lock />
                                        </el-icon> 修改密码</span>
                                </template>
                            </el-tab-pane>
                        </el-tabs>
                    </template>

                    <!-- 基本信息表单 -->
                    <div v-show="activeTab === 'profile'">
                        <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="100px"
                            class="profile-form">
                            <el-form-item label="用户名">
                                <el-input v-model="userProfile.username" disabled>
                                    <template #prefix>
                                        <el-icon>
                                            <User />
                                        </el-icon>
                                    </template>
                                </el-input>
                                <div class="form-tip">用户名不支持修改</div>
                            </el-form-item>
                            <el-form-item label="真实姓名" prop="realName">
                                <el-input v-model="profileForm.realName" placeholder="请输入真实姓名">
                                    <template #prefix>
                                        <el-icon>
                                            <Postcard />
                                        </el-icon>
                                    </template>
                                </el-input>
                            </el-form-item>
                            <el-form-item label="手机号码" prop="phone">
                                <el-input v-model="profileForm.phone" placeholder="请输入手机号码" maxlength="11">
                                    <template #prefix>
                                        <el-icon>
                                            <Phone />
                                        </el-icon>
                                    </template>
                                </el-input>
                            </el-form-item>
                            <el-form-item label="邮箱地址" prop="email">
                                <el-input v-model="profileForm.email" placeholder="请输入邮箱地址">
                                    <template #prefix>
                                        <el-icon>
                                            <Message />
                                        </el-icon>
                                    </template>
                                </el-input>
                            </el-form-item>
                            <el-form-item>
                                <el-button type="primary" @click="handleUpdateProfile" :loading="profileLoading">
                                    <el-icon>
                                        <Check />
                                    </el-icon>
                                    保存修改
                                </el-button>
                                <el-button @click="resetProfileForm">
                                    <el-icon>
                                        <Refresh />
                                    </el-icon>
                                    重置
                                </el-button>
                            </el-form-item>
                        </el-form>
                    </div>

                    <!-- 修改密码表单 -->
                    <div v-show="activeTab === 'password'">
                        <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px"
                            class="password-form">
                            <el-form-item label="当前密码" prop="oldPassword">
                                <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码"
                                    show-password>
                                    <template #prefix>
                                        <el-icon>
                                            <Lock />
                                        </el-icon>
                                    </template>
                                </el-input>
                            </el-form-item>
                            <el-form-item label="新密码" prop="newPassword">
                                <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码"
                                    show-password>
                                    <template #prefix>
                                        <el-icon>
                                            <Key />
                                        </el-icon>
                                    </template>
                                </el-input>
                                <div class="password-strength">
                                    <span>密码强度：</span>
                                    <el-progress :percentage="passwordStrength.percentage"
                                        :color="passwordStrength.color" :show-text="false" :stroke-width="6" />
                                    <span :style="{ color: passwordStrength.color }">{{ passwordStrength.text }}</span>
                                </div>
                            </el-form-item>
                            <el-form-item label="确认密码" prop="confirmPassword">
                                <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码"
                                    show-password>
                                    <template #prefix>
                                        <el-icon>
                                            <Key />
                                        </el-icon>
                                    </template>
                                </el-input>
                            </el-form-item>
                            <el-form-item>
                                <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
                                    <el-icon>
                                        <Check />
                                    </el-icon>
                                    确认修改
                                </el-button>
                                <el-button @click="resetPasswordForm">
                                    <el-icon>
                                        <Refresh />
                                    </el-icon>
                                    重置
                                </el-button>
                            </el-form-item>
                        </el-form>
                    </div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import {
    User, Phone, Message, Calendar, Clock, Lock, Key,
    Camera, Check, Refresh, Postcard
} from '@element-plus/icons-vue';
import { getProfile, updateProfile, changePassword } from '@/api/user';
import { uploadFile } from '@/api/file';

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 当前选中的Tab
const activeTab = ref('profile');

// 用户信息
const userProfile = ref({
    id: null,
    username: '',
    realName: '',
    phone: '',
    email: '',
    avatar: '',
    status: 1,
    roles: [],
    createTime: '',
    lastLoginTime: '',
    lastLoginIp: ''
});

// 基本信息表单
const profileFormRef = ref();
const profileLoading = ref(false);
const profileForm = reactive({
    realName: '',
    phone: '',
    email: ''
});

// 基本信息验证规则
const profileRules = {
    realName: [
        { max: 50, message: '真实姓名长度不能超过50个字符', trigger: 'blur' }
    ],
    phone: [
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
    ],
    email: [
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
    ]
};

// 密码表单
const passwordFormRef = ref();
const passwordLoading = ref(false);
const passwordForm = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
});

// 密码验证规则
const validateConfirmPassword = (rule, value, callback) => {
    if (value !== passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'));
    } else {
        callback();
    }
};

const passwordRules = {
    oldPassword: [
        { required: true, message: '请输入当前密码', trigger: 'blur' }
    ],
    newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur' }
    ],
    confirmPassword: [
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
    ]
};

// 密码强度计算
const passwordStrength = computed(() => {
    const password = passwordForm.newPassword;
    if (!password) {
        return { percentage: 0, color: '#909399', text: '' };
    }

    let strength = 0;
    if (password.length >= 6) strength += 20;
    if (password.length >= 10) strength += 10;
    if (/[a-z]/.test(password)) strength += 20;
    if (/[A-Z]/.test(password)) strength += 20;
    if (/[0-9]/.test(password)) strength += 15;
    if (/[^a-zA-Z0-9]/.test(password)) strength += 15;

    if (strength < 40) {
        return { percentage: strength, color: '#F56C6C', text: '弱' };
    } else if (strength < 70) {
        return { percentage: strength, color: '#E6A23C', text: '中' };
    } else {
        return { percentage: strength, color: '#67C23A', text: '强' };
    }
});

// 获取用户信息
const loadUserProfile = async () => {
    try {
        const response = await getProfile();
        userProfile.value = response.data;
        // 同步到表单
        profileForm.realName = response.data.realName || '';
        profileForm.phone = response.data.phone || '';
        profileForm.email = response.data.email || '';
    } catch (error) {
        console.error('获取用户信息失败:', error);
        ElMessage.error('获取用户信息失败');
    }
};

// 更新个人信息
const handleUpdateProfile = async () => {
    try {
        await profileFormRef.value.validate();
        profileLoading.value = true;

        await updateProfile(profileForm);
        ElMessage.success('个人信息更新成功');
        // 重新加载用户信息
        await loadUserProfile();
    } catch (error) {
        console.error('更新个人信息失败:', error);
    } finally {
        profileLoading.value = false;
    }
};

// 重置个人信息表单
const resetProfileForm = () => {
    profileForm.realName = userProfile.value.realName || '';
    profileForm.phone = userProfile.value.phone || '';
    profileForm.email = userProfile.value.email || '';
};

// 修改密码
const handleChangePassword = async () => {
    try {
        await passwordFormRef.value.validate();
        passwordLoading.value = true;

        await changePassword({
            oldPassword: passwordForm.oldPassword,
            newPassword: passwordForm.newPassword
        });

        ElMessage.success('密码修改成功，请重新登录');
        // 清空表单
        resetPasswordForm();
    } catch (error) {
        console.error('修改密码失败:', error);
    } finally {
        passwordLoading.value = false;
    }
};

// 重置密码表单
const resetPasswordForm = () => {
    passwordForm.oldPassword = '';
    passwordForm.newPassword = '';
    passwordForm.confirmPassword = '';
    passwordFormRef.value?.resetFields();
};

// 头像上传前验证
const beforeAvatarUpload = (file) => {
    const isImage = file.type.startsWith('image/');
    const isLt2M = file.size / 1024 / 1024 < 2;

    if (!isImage) {
        ElMessage.error('只能上传图片文件！');
        return false;
    }
    if (!isLt2M) {
        ElMessage.error('图片大小不能超过 2MB！');
        return false;
    }
    return true;
};

// 上传头像
const handleAvatarUpload = async (options) => {
    try {
        const response = await uploadFile(options.file, 'avatar');
        const avatarUrl = response.data;

        // 更新头像
        await updateProfile({ avatar: avatarUrl });
        userProfile.value.avatar = avatarUrl;
        ElMessage.success('头像更新成功');
    } catch (error) {
        console.error('上传头像失败:', error);
        ElMessage.error('上传头像失败');
    }
};

// 初始化
onMounted(() => {
    loadUserProfile();
});
</script>

<style lang="scss" scoped>
.profile-container {
    padding: 20px;

    .profile-card {
        text-align: center;

        .avatar-section {
            position: relative;
            display: inline-block;
            margin-bottom: 20px;

            .avatar-uploader {
                cursor: pointer;
                position: relative;

                &:hover .avatar-overlay {
                    opacity: 1;
                }
            }

            .user-avatar {
                border: 4px solid #fff;
                box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
            }

            .avatar-overlay {
                position: absolute;
                top: 0;
                left: 50%;
                transform: translateX(-50%);
                width: 120px;
                height: 120px;
                border-radius: 50%;
                background: rgba(0, 0, 0, 0.5);
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                opacity: 0;
                transition: opacity 0.3s;
                color: #fff;

                .el-icon {
                    font-size: 24px;
                    margin-bottom: 5px;
                }

                span {
                    font-size: 12px;
                }
            }
        }

        .user-info {
            .username {
                margin: 0 0 10px;
                font-size: 20px;
                font-weight: 600;
                color: #303133;
            }

            .user-role {
                margin: 0;

                .role-tag {
                    margin: 0 4px;
                }
            }
        }

        .info-list {
            text-align: left;

            .info-item {
                display: flex;
                align-items: center;
                padding: 12px 0;
                border-bottom: 1px solid #f0f0f0;

                &:last-child {
                    border-bottom: none;
                }

                .el-icon {
                    color: #409eff;
                    margin-right: 10px;
                    font-size: 16px;
                }

                .label {
                    width: 80px;
                    color: #909399;
                    font-size: 14px;
                }

                .value {
                    flex: 1;
                    color: #303133;
                    font-size: 14px;
                    text-align: right;
                }
            }
        }
    }

    .form-card {
        :deep(.el-card__header) {
            padding: 0;
            border-bottom: none;

            .el-tabs__header {
                margin: 0;
            }

            .el-tabs__nav-wrap::after {
                display: none;
            }
        }

        .profile-form,
        .password-form {
            max-width: 500px;
            padding: 20px 0;

            .form-tip {
                font-size: 12px;
                color: #909399;
                margin-top: 5px;
            }

            .password-strength {
                display: flex;
                align-items: center;
                margin-top: 8px;
                font-size: 12px;
                color: #909399;

                .el-progress {
                    width: 100px;
                    margin: 0 8px;
                }
            }
        }
    }
}
</style>
