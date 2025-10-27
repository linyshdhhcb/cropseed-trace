<template>
    <div class="error-container">
        <div class="error-content">
            <div class="error-image">
                <img src="@/assets/404.png" alt="404" />
            </div>
            <div class="error-info">
                <h1 class="error-code">404</h1>
                <h2 class="error-title">页面不存在</h2>
                <p class="error-description">
                    抱歉，您访问的页面不存在或已被删除
                </p>
                <div class="error-actions">
                    <el-button type="primary" @click="goHome">
                        返回首页
                    </el-button>
                    <el-button @click="goBack">
                        返回上页
                    </el-button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

// 返回首页
const goHome = () => {
    router.push('/')
}

// 返回上页
const goBack = () => {
    router.go(-1)
}
</script>

<style lang="scss" scoped>
.error-container {
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f5f5;

    .error-content {
        text-align: center;

        .error-image {
            margin-bottom: 30px;

            img {
                width: 300px;
                height: 200px;
                object-fit: contain;
            }
        }

        .error-info {
            .error-code {
                font-size: 72px;
                font-weight: 700;
                color: #409EFF;
                margin: 0 0 20px 0;
            }

            .error-title {
                font-size: 32px;
                font-weight: 600;
                color: #333;
                margin: 0 0 20px 0;
            }

            .error-description {
                font-size: 16px;
                color: #666;
                margin: 0 0 30px 0;
            }

            .error-actions {
                .el-button {
                    margin: 0 10px;
                }
            }
        }
    }
}

// 响应式设计
@media (max-width: 768px) {
    .error-container {
        padding: 20px;

        .error-content {
            .error-image {
                img {
                    width: 200px;
                    height: 150px;
                }
            }

            .error-info {
                .error-code {
                    font-size: 48px;
                }

                .error-title {
                    font-size: 24px;
                }

                .error-description {
                    font-size: 14px;
                }
            }
        }
    }
}
</style>
