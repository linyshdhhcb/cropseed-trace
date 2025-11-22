#!/bin/bash

##############################################
# CropSeed Trace 项目停止脚本
# 功能：优雅停止所有服务
# 作者：linyi
# 日期：2025-11-22
##############################################

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# 停止服务
stop_services() {
    log_step "停止服务..."
    
    if ! docker-compose ps | grep -q "Up"; then
        log_warn "没有运行中的服务"
        return
    fi
    
    log_info "正在优雅停止所有服务..."
    docker-compose stop
    
    log_info "服务已停止"
}

# 删除容器
remove_containers() {
    log_step "删除容器..."
    
    read -p "是否删除容器？(y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker-compose rm -f
        log_info "容器已删除"
    else
        log_info "跳过删除容器"
    fi
}


# 显示状态
show_status() {
    log_step "当前状态："
    echo ""
    docker-compose ps
    echo ""
}

# 主函数
main() {
    echo ""
    echo "=========================================="
    echo "  CropSeed Trace 项目停止脚本"
    echo "=========================================="
    echo ""
    
    stop_services
    remove_containers
    show_status
    
    log_info "操作完成！"
}

# 执行主函数
main
