#!/usr/bin/env bash
set -euo pipefail

echo "=========================================="
echo "同步上游到 3.0.37 并更新 Mod 版本号"
echo "=========================================="

# 检查是否有未提交的改动
if ! git diff-index --quiet HEAD --; then
    echo "❌ 有未提交的改动，请先提交"
    git status
    exit 1
fi

# 添加上游仓库（如果未添加）
if ! git remote get-url upstream &>/dev/null; then
    echo "✓ 添加上游仓库..."
    git remote add upstream https://github.com/59799517/simple_sq_music_plus.git
fi

# 获取上游最新代码
echo "✓ 获取上游 3.0 分支的最新代码..."
git fetch upstream 3.0

# 查看上游当前版本
echo ""
echo "上游仓库 pom.xml 版本号："
git show upstream/3.0:pom.xml | grep -A 1 "<artifactId>simple-MusicServer</artifactId>" | grep "<version>" || echo "未找到"

echo ""
echo "本地当前版本号："
grep -A 1 "<artifactId>simple-MusicServer</artifactId>" pom.xml | grep "<version>" || echo "未找到"

# 尝试合并（可能有冲突）
echo ""
echo "✓ 尝试合并上游代码（会提示冲突，如有）..."
if ! git merge upstream/3.0 --no-edit 2>&1; then
    echo "⚠️  检测到合并冲突！"
    echo ""
    echo "冲突文件："
    git status | grep "both modified"
    echo ""
    echo "请按以下步骤处理："
    echo "1. 手工编辑冲突文件，保留你需要的代码"
    echo "2. git add <冲突文件>"
    echo "3. git commit -m 'merge: 合并上游 3.0.37'"
    echo "4. git push -f origin 3.0"
    exit 1
fi

echo "✓ 合并成功！"

# 推送到 origin
echo "✓ 推送到 origin/3.0..."
git push -f origin 3.0

echo ""
echo "=========================================="
echo "✅ 更新完成！"
echo "=========================================="
echo ""
echo "接下来的步骤："
echo "1. 等待 GitHub Actions 自动构建新镜像（~15 分钟）"
echo "2. 构建完成后运行升级命令："
echo "   docker compose pull && docker compose up -d"
echo ""
