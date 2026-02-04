# FMR-Backend 家庭医疗档案管理系统

## 项目简介

FMR-Backend（Family Medical Records Backend）是一个家庭医疗档案管理系统的后端服务，旨在帮助家庭用户管理家庭成员的健康档案、病历记录、用药信息、检查报告等医疗数据。

## 技术栈

- **框架**: Spring Boot 3.4.2
- **语言**: Java 17
- **数据库**: MySQL 8.0
- **ORM**: MyBatis-Plus 3.5.5
- **API文档**: SpringDoc OpenAPI (Swagger)
- **工具库**: Hutool, Lombok

## 项目结构

```
src/main/java/com/example/fmrbackend/
├── FmrBackendApplication.java      # 应用启动类
├── common/                         # 公共模块
│   ├── exception/                  # 异常处理
│   │   ├── BusinessException.java  # 业务异常
│   │   └── GlobalExceptionHandler.java # 全局异常处理器
│   └── result/                     # 统一响应
│       ├── Result.java             # 响应封装类
│       └── ResultCode.java         # 响应状态码
├── config/                         # 配置类
│   ├── MybatisPlusConfig.java      # MyBatis-Plus配置
│   └── SwaggerConfig.java          # Swagger配置
├── controller/                     # 控制器层
│   ├── FamilyController.java       # 家庭成员管理
│   └── HomeController.java         # 首页仪表盘
├── dto/                            # 数据传输对象
│   ├── request/                    # 请求DTO
│   │   └── AddMemberRequest.java
│   └── response/                   # 响应DTO
│       ├── FamilyMemberDTO.java
│       ├── HomeDashboardDTO.java
│       └── MemberProfileDTO.java
├── entity/                         # 实体类
│   ├── BaseEntity.java             # 基础实体
│   ├── DailyGoal.java              # 每日目标
│   ├── FamilyMember.java           # 家庭成员
│   ├── LabReport.java              # 检查报告
│   ├── MedicalRecord.java          # 病历记录
│   └── Medication.java             # 药品信息
├── mapper/                         # 数据访问层
│   ├── DailyGoalMapper.java
│   ├── FamilyMemberMapper.java
│   ├── LabReportMapper.java
│   ├── MedicalRecordMapper.java
│   └── MedicationMapper.java
└── service/                        # 服务层
    ├── DashboardService.java
    ├── FamilyMemberService.java
    └── impl/
        ├── DashboardServiceImpl.java
        └── FamilyMemberServiceImpl.java
```

## 功能模块

### 1. 首页模块
- 个性化问候语（根据时间段显示）
- 快捷入口（扫药盒、拍病历、报告解读、复诊提醒）
- 今日进度（服药进度、运动进度）
- 最近动态

### 2. 家庭成员管理模块
- 添加家庭成员（最多10人）
- 查看成员列表
- 查看成员详情档案
- 更新成员信息
- 删除成员（逻辑删除）

### 3. 病例文书导入模块
- 病历记录管理
- 检查报告管理
- 药品信息管理

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE fmr_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行SQL脚本：
```bash
mysql -u root -p fmr_db < sql/schema.sql
```

### 配置文件

修改 `src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fmr_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

### 启动应用

```bash
# 使用Maven启动
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/FMR-backend-0.0.1-SNAPSHOT.jar
```

### 访问API文档

启动应用后，访问 Swagger UI：
```
http://localhost:8080/swagger-ui.html
```

## API接口

### 首页接口
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/home/dashboard | 获取首页仪表盘数据 |

### 家庭成员接口
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/family/members | 添加家庭成员 |
| GET | /api/family/members | 获取成员列表 |
| GET | /api/family/members/{id} | 获取成员详情 |
| PUT | /api/family/members/{id} | 更新成员信息 |
| DELETE | /api/family/members/{id} | 删除成员 |

## 数据库表结构

| 表名 | 描述 |
|------|------|
| t_family_member | 家庭成员表 |
| t_medical_record | 病历记录表 |
| t_medication | 药品信息表 |
| t_lab_report | 检查报告表 |
| t_daily_goal | 每日目标表 |

## 开发规范

### 代码规范
- 使用Lombok简化代码
- 统一使用Result封装响应
- 业务异常使用BusinessException
- 使用@Valid进行参数校验

### 命名规范
- 实体类：驼峰命名
- 数据库表：t_前缀 + 下划线命名
- API路径：RESTful风格

## 许可证

Apache License 2.0
