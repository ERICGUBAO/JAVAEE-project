#!/bin/bash
# 一键部署脚本 - 在服务器上运行
# 1. SSH 到服务器: ssh root@1.14.106.53
# 2. 运行这个脚本: bash deploy.sh

set -e

echo "=== 安装 Java 17 ==="
yum install -y java-17-openjdk || apt update && apt install -y openjdk-17-jdk

echo "=== 安装 MySQL（如果没有）==="
if ! command -v mysql &>/dev/null; then
    yum install -y mysql-server || apt install -y mysql-server
    systemctl start mysqld || service mysql start
fi

echo "=== 创建数据库 ==="
mysql -u root -p'799462asd@' -e "CREATE DATABASE IF NOT EXISTS attendance_system DEFAULT CHARSET utf8mb4;" 2>/dev/null || \
mysql -u root -e "CREATE DATABASE IF NOT EXISTS attendance_system DEFAULT CHARSET utf8mb4; ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '799462asd@'; FLUSH PRIVILEGES;"

echo "=== 导入表结构（需要先传 JAR，JAR 启动时 JPA 会自动建表）==="

echo "=== 修改 application.properties ==="
# JAR 里的配置需要改 MySQL 地址为 localhost
# 启动时用外部配置覆盖

echo "=== 启动应用 ==="
cat > /root/app.properties << 'EOF'
spring.datasource.url=jdbc:mysql://localhost:3306/attendance_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=799462asd@
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.open-in-view=false
file.upload.path=./uploads/
spring.cache.type=simple
EOF

nohup java -jar /root/app.jar --spring.config.additional-location=/root/app.properties > /root/app.log 2>&1 &

echo "=== 等待启动 ==="
sleep 10
tail -20 /root/app.log

echo ""
echo "=== 部署完成！==="
echo "访问: http://1.14.106.53:8080"
echo "查看日志: tail -f /root/app.log"
