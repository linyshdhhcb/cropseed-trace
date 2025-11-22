#!/bin/bash

##############################################
# CropSeed Trace 项目环境检查脚本
# 功能：检查部署环境是否满足要求
# 作者：linyi
# 日期：2025-01-22
##############################################

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 日志函数
log_info() {
    echo -e "${GREEN}[✓]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[!]${NC} $1"
}

log_error() {
    echo -e "${RED}[✗]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[CHECK]${NC} $1"
}

# 检查计数
PASS_COUNT=0
FAIL_COUNT=0
WARN_COUNT=0

# 检查Docker
check_docker() {
    log_step "检查Docker..."
    if command -v docker &> /dev/null; then
        DOCKER_VERSION=$(docker --version | awk '{print $3}' | sed 's/,//')
        log_info "Docker已安装: $DOCKER_VERSION"
        ((PASS_COUNT++))
        
        # 检查Docker是否运行
        if docker info &> /dev/null; then
            log_info "Docker服务运行正常"
            ((PASS_COUNT++))
        else
            log_error "Docker服务未运行，请启动Docker"
            ((FAIL_COUNT++))
        fi
    else
        log_error "Docker未安装"
        ((FAIL_COUNT++))
    fi
}

# 检查Docker Compose
check_docker_compose() {
    log_step "检查Docker Compose..."
    if command -v docker-compose &> /dev/null; then
        COMPOSE_VERSION=$(docker-compose --version | awk '{print $4}' | sed 's/,//')
        log_info "Docker Compose已安装: $COMPOSE_VERSION"
        ((PASS_COUNT++))
    else
        log_error "Docker Compose未安装"
        ((FAIL_COUNT++))
    fi
}

# 检查端口占用
check_ports() {
    log_step "检查端口占用..."
    
    PORTS=(80 8085 3306 6379 9000 9001)
    PORT_NAMES=("前端" "后端" "MySQL" "Redis" "MinIO" "MinIO控制台")
    
    for i in "${!PORTS[@]}"; do
        PORT=${PORTS[$i]}
        NAME=${PORT_NAMES[$i]}
        
        if netstat -tuln 2>/dev/null | grep -q ":${PORT} " || ss -tuln 2>/dev/null | grep -q ":${PORT} "; then
            log_warn "端口 ${PORT} (${NAME}) 已被占用"
            ((WARN_COUNT++))
        else
            log_info "端口 ${PORT} (${NAME}) 可用"
            ((PASS_COUNT++))
        fi
    done
}

# 检查磁盘空间
check_disk_space() {
    log_step "检查磁盘空间..."
    
    AVAILABLE=$(df -BG . | tail -1 | awk '{print $4}' | sed 's/G//')
    REQUIRED=10
    
    if [ "$AVAILABLE" -gt "$REQUIRED" ]; then
        log_info "可用磁盘空间: ${AVAILABLE}GB (推荐至少${REQUIRED}GB)"
        ((PASS_COUNT++))
    else
        log_warn "可用磁盘空间不足: ${AVAILABLE}GB (推荐至少${REQUIRED}GB)"
        ((WARN_COUNT++))
    fi
}

# 检查内存
check_memory() {
    log_step "检查系统内存..."
    
    TOTAL_MEM=$(free -g | grep Mem | awk '{print $2}')
    REQUIRED_MEM=4
    
    if [ "$TOTAL_MEM" -ge "$REQUIRED_MEM" ]; then
        log_info "系统内存: ${TOTAL_MEM}GB (推荐至少${REQUIRED_MEM}GB)"
        ((PASS_COUNT++))
    else
        log_warn "系统内存不足: ${TOTAL_MEM}GB (推荐至少${REQUIRED_MEM}GB)"
        ((WARN_COUNT++))
    fi
}

# 检查必要文件
check_files() {
    log_step "检查必要文件..."
    
    # 检查.env文件
    if [ -f ".env" ]; then
        log_info ".env配置文件存在"
        ((PASS_COUNT++))
    else
        log_warn ".env配置文件不存在（首次部署会自动创建）"
        ((WARN_COUNT++))
    fi
    
    # 检查docker-compose.yml
    if [ -f "docker-compose.yml" ]; then
        log_info "docker-compose.yml存在"
        ((PASS_COUNT++))
    else
        log_error "docker-compose.yml不存在"
        ((FAIL_COUNT++))
    fi
    
    # 检查Dockerfile
    if [ -f "Dockerfile.backend" ] && [ -f "Dockerfile.web" ]; then
        log_info "Dockerfile文件存在"
        ((PASS_COUNT++))
    else
        log_error "Dockerfile文件缺失"
        ((FAIL_COUNT++))
    fi
    
    # 检查配置文件
    if [ -f "config/application.yml" ]; then
        log_info "application.yml配置文件存在"
        ((PASS_COUNT++))
    else
        log_error "config/application.yml配置文件不存在"
        ((FAIL_COUNT++))
    fi
}

# 检查项目构建文件
check_build_files() {
    log_step "检查项目构建文件..."
    
    # 检查jar目录和jar包
    if [ -d "./jar" ] && [ -n "$(ls -A ./jar/*.jar 2>/dev/null)" ]; then
        JAR_FILE=$(ls ./jar/*.jar | head -n 1)
        JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
        log_info "jar目录存在，jar包: $(basename $JAR_FILE) (${JAR_SIZE})"
        ((PASS_COUNT++))
    else
        log_warn "./jar 目录不存在或没有jar包"
        log_info "请将构建好的jar包放入 ./jar/ 目录"
        ((WARN_COUNT++))
    fi
    
    # 检查dist目录
    if [ -d "./dist" ]; then
        DIST_SIZE=$(du -sh ./dist | cut -f1)
        log_info "dist目录存在 (${DIST_SIZE})"
        ((PASS_COUNT++))
    else
        log_warn "./dist 目录不存在"
        log_info "请将构建好的dist目录复制到当前目录"
        ((WARN_COUNT++))
    fi
}

# 检查网络
check_network() {
    log_step "检查网络连接..."
    
    if ping -c 1 -W 2 8.8.8.8 &> /dev/null; then
        log_info "网络连接正常"
        ((PASS_COUNT++))
    else
        log_warn "网络连接异常（可能影响Docker镜像拉取）"
        ((WARN_COUNT++))
    fi
}

# 显示总结
show_summary() {
    echo ""
    echo "=========================================="
    echo "  检查总结"
    echo "=========================================="
    echo ""
    echo -e "通过: ${GREEN}${PASS_COUNT}${NC}"
    echo -e "警告: ${YELLOW}${WARN_COUNT}${NC}"
    echo -e "失败: ${RED}${FAIL_COUNT}${NC}"
    echo ""
    
    if [ $FAIL_COUNT -eq 0 ]; then
        if [ $WARN_COUNT -eq 0 ]; then
            echo -e "${GREEN}✓ 环境检查完全通过，可以开始部署！${NC}"
            echo ""
            echo "运行以下命令开始部署："
            echo "  ./start.sh"
        else
            echo -e "${YELLOW}! 环境检查基本通过，但有一些警告${NC}"
            echo "  建议处理警告后再部署"
        fi
    else
        echo -e "${RED}✗ 环境检查失败，请先解决上述问题${NC}"
        echo "  必须处理所有失败项才能部署"
    fi
    echo ""
}

# 主函数
main() {
    echo ""
    echo "=========================================="
    echo "  CropSeed Trace 环境检查"
    echo "=========================================="
    echo ""
    
    check_docker
    check_docker_compose
    check_ports
    check_disk_space
    check_memory
    check_files
    check_build_files
    check_network
    show_summary
}

# 执行主函数
main
