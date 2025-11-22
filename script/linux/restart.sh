#!/bin/bash

##############################################
# CropSeed Trace 项目重启脚本
# 功能：快速重启服务
# 作者：linyi
# 日期：2025-11-22
##############################################

set -e

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
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# 主函数
main() {
    echo ""
    echo "=========================================="
    echo "  CropSeed Trace 项目重启脚本"
    echo "=========================================="
    echo ""
    
    log_step "重启服务..."
    docker-compose restart
    
    log_info "服务重启完成！"
    
    echo ""
    docker-compose ps
    echo ""
}

# 执行主函数
main
