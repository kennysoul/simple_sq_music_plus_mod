
[check_update.sh](check_update.sh)脚本使用说明

[sync_upstream.sh](sync_upstream.sh) 上游同步脚本：

- 同步上游 `59799517/simple_sq_music_plus` 的 `3.0` 分支到本地 `3.0`
- 可选自动推送到 `origin/3.0`（你的 GitHub 仓库）

```bash
# 同步到本地并推送到 origin
bash script/sync_upstream.sh --push

# 只同步到本地，不推送
bash script/sync_upstream.sh --no-push
```


如果用[docker-compose.yml](../docker-compose.yml)执行的则啥都不用改直接运行即可


如果改了配置文件则需要修改docker-compose.yml文件

### 数据库配置
- DB_IP="mysql" 数据库地址可以写服务名称
- DB_PORT="3306" 数据库端口（内部端口）
- DB_NAME="sqmusicv3" 数据库名称
- DB_USERNAME="root" 数据库用户名
- DB_PASSWORD="sqmusicv3password" 数据库密码

### 音乐目录配置
- MUSIC_DIR_HOST="$(pwd)/../music"  映射本地的路径
- MUSIC_DIR_CONTAINER="/music" 容器内部路径

###  容器名称配置
CONTAINER_MYSQL="sqmusic_mod_mysql"  
CONTAINER_WEB="sqmusic_mod_web"
CONTAINER_MAIN="sqmusic_mod_main"

###  全局的网关名称 
NETWORK_NAME="sq-mod-network"

### 镜像来源（默认）
- IMAGE_MAIN="ghcr.io/kennysoul/simple_sq_music_plus_mod"
- IMAGE_WEB="ghcr.io/kennysoul/simple_sq_music_plus_web_mod"

### 环境变量覆盖（无需改脚本）

脚本支持通过环境变量临时覆盖默认配置，例如：

```bash
# 临时切换音乐目录与 Web 端口（容器名/网络名保持修改版）
MUSIC_DIR_HOST=/mnt/hdd/sqmusic \
WEB_PORT_MAPPING=8098:80 \
CONTAINER_MAIN=sqmusic_mod_main \
CONTAINER_WEB=sqmusic_mod_web \
CONTAINER_MYSQL=sqmusic_mod_mysql \
NETWORK_NAME=sq-mod-network \
bash script/check_update.sh
```

### .env 文件支持（推荐）

脚本会自动读取仓库根目录 `.env`（也可通过 `ENV_FILE` 指定路径）：

```bash
# 从示例生成本地配置
cp .env.example .env

# 执行更新检查（自动读取 .env）
bash script/check_update.sh
```

```bash
# 指定其他配置文件
ENV_FILE=/path/to/custom.env bash script/check_update.sh
```

优先级：`命令行环境变量 > .env > 脚本默认值`

常用可覆盖变量：
- `DB_IP` `DB_PORT` `DB_NAME` `DB_USERNAME` `DB_PASSWORD`
- `MUSIC_DIR_HOST` `MUSIC_DIR_CONTAINER`
- `WEB_PORT_MAPPING`（如 `8098:80`）
- `MAIN_PORT_MAPPING`（如 `8099:8099`，默认不映射）
- `CONTAINER_MAIN` `CONTAINER_WEB` `CONTAINER_MYSQL`
- `NETWORK_NAME`
- `RELEASE_REPO` `IMAGE_MAIN` `IMAGE_WEB`