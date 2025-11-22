#!/bin/bash

##############################################
# CropSeed Trace 项目智能启动脚本
# 功能：自动检测容器状态并启动服务
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

# 检查Docker是否安装
check_docker() {
    log_step "检查Docker环境..."
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
    
    log_info "Docker环境检查通过"
}

# 检查.env文件
check_env_file() {
    log_step "检查环境配置文件..."
    if [ ! -f ".env" ]; then
        log_warn ".env文件不存在，从.env.example创建..."
        if [ -f ".env.example" ]; then
            cp .env.example .env
            log_info "已创建.env文件，请修改其中的配置信息！"
            log_warn "修改完成后再次运行此脚本"
            exit 0
        else
            log_error ".env.example文件不存在"
            exit 1
        fi
    fi
    log_info "环境配置文件存在"
}

# 检查必要文件
check_required_files() {
    log_step "检查必要文件..."
    
    # 检查jar包目录
    if [ ! -d "./jar" ] || [ -z "$(ls -A ./jar/*.jar 2>/dev/null)" ]; then
        log_error "./jar 目录不存在或没有jar包"
        exit 1
    fi
    JAR_FILE=$(ls ./jar/*.jar | head -n 1)
    log_info "找到后端jar包: $(basename $JAR_FILE)"
    
    # 检查前端目录
    if [ ! -d "./dist" ]; then
        log_error "./dist 目录不存在"
        log_info "请将构建好的dist目录复制到当前目录下"
        exit 1
    fi
    log_info "找到前端dist目录"
    
    # 检查配置文件
    if [ ! -f "config/application.yml" ]; then
        log_error "config/application.yml配置文件不存在"
        exit 1
    fi
    log_info "配置文件检查通过"
}

# 检查容器状态
check_container_status() {
    local container_name=$1
    if docker ps -a --format '{{.Names}}' | grep -q "^${container_name}$"; then
        if docker ps --format '{{.Names}}' | grep -q "^${container_name}$"; then
            echo "running"
        else
            echo "stopped"
        fi
    else
        echo "not_exist"
    fi
}

# 停止并删除容器
stop_and_remove_container() {
    local container_name=$1
    local status=$(check_container_status "$container_name")
    
    if [ "$status" = "running" ]; then
        log_info "停止运行中的容器: $container_name"
        docker stop "$container_name" > /dev/null 2>&1
    fi
    
    if [ "$status" != "not_exist" ]; then
        log_info "删除容器: $container_name"
        docker rm "$container_name" > /dev/null 2>&1
    fi
}

# 清理旧容器
cleanup_containers() {
    log_step "检查并清理旧容器..."
    
    local containers=("cropseed-backend" "cropseed-frontend")
    local need_cleanup=false
    
    for container in "${containers[@]}"; do
        status=$(check_container_status "$container")
        if [ "$status" != "not_exist" ]; then
            need_cleanup=true
            break
        fi
    done
    
    if [ "$need_cleanup" = true ]; then
        log_warn "发现已存在的容器，准备清理..."
        read -p "是否停止并删除现有容器？(y/n): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            for container in "${containers[@]}"; do
                stop_and_remove_container "$container"
            done
            log_info "旧容器清理完成"
        else
            log_error "用户取消操作"
            exit 1
        fi
    else
        log_info "没有需要清理的容器"
    fi
}

# 创建必要的目录
create_directories() {
    log_step "创建必要的目录..."
    mkdir -p logs
    log_info "目录创建完成"
}

# 启动服务
start_services() {
    log_step "启动服务..."
    
    log_info "启动后端服务..."
    docker-compose up -d backend
    
    log_info "等待后端服务启动..."
    sleep 5
    
    log_info "启动前端服务..."
    docker-compose up -d frontend
    
    log_info "所有服务启动完成！"
}

# 显示服务状态
show_status() {
    log_step "服务状态："
    echo ""
    docker-compose ps
    echo ""
    
    log_info "访问地址："
    echo -e "  前端地址: ${GREEN}http://localhost:8086${NC}"
    echo -e "  后端地址: ${GREEN}http://localhost:8085${NC}"
    echo ""
    
    log_info "查看日志命令："
    echo "  docker-compose logs -f backend   # 查看后端日志"
    echo "  docker-compose logs -f frontend  # 查看前端日志"
    echo ""
}

# 主函数
main() {
    echo ""
    echo "=========================================="
    echo "  CropSeed Trace 项目启动脚本"
    echo "=========================================="
    echo ""
    
    check_docker
    check_env_file
    check_required_files
    cleanup_containers
    create_directories
    start_services
    show_status
    
    log_info "启动完成！"
}

# 执行主函数
main
