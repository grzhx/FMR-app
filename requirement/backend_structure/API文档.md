# FMR-Backend API文档

## 概述

本文档描述了家庭医疗档案管理系统（FMR-Backend）的RESTful API接口。

**基础URL**: `http://localhost:8080/api`

**响应格式**: JSON

## 统一响应格式

所有API响应都遵循以下格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1706500000000
}
```

### 响应状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 1001 | 成员不存在 |
| 1002 | 成员数量超限 |

---

## 首页模块

### 获取首页仪表盘数据

获取用户首页聚合数据，包括问候语、快捷入口、今日进度、最近动态。

**请求**

```
GET /home/dashboard
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| memberId | Long | 是 | 成员ID |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "greeting": "上午好，张三",
    "shortcuts": [
      {
        "id": "scan_med",
        "name": "扫药盒",
        "icon": "/icons/scan_med.png"
      },
      {
        "id": "upload_record",
        "name": "拍病历",
        "icon": "/icons/upload_record.png"
      }
    ],
    "todayProgress": {
      "medication": {
        "target": 3,
        "current": 1,
        "unit": "次"
      },
      "exercise": {
        "target": 6000,
        "current": 3500,
        "unit": "步"
      }
    },
    "recentUpdates": []
  }
}
```

---

## 家庭成员管理模块

### 添加家庭成员

添加新的家庭成员，一个家庭最多10人。

**请求**

```
POST /family/members
```

**请求体**

```json
{
  "familyId": 1,
  "name": "张三",
  "gender": 1,
  "birthDate": "1990-01-15",
  "relation": "本人",
  "role": 1,
  "avatarUrl": "https://example.com/avatar.jpg",
  "viewAll": true,
  "editAll": true
}
```

**参数说明**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| familyId | Long | 否 | 家庭ID |
| name | String | 是 | 成员姓名（1-50字符） |
| gender | Integer | 是 | 性别：1-男，2-女 |
| birthDate | Date | 是 | 出生日期 |
| relation | String | 是 | 与户主关系 |
| role | Integer | 否 | 角色：1-管理员，2-普通成员（默认2） |
| avatarUrl | String | 否 | 头像URL |
| viewAll | Boolean | 否 | 是否可查看所有人数据（默认false） |
| editAll | Boolean | 否 | 是否可编辑所有人数据（默认false） |

**响应示例**

```json
{
  "code": 200,
  "message": "添加成功",
  "data": 1
}
```

---

### 获取家庭成员列表

获取指定家庭的所有成员。

**请求**

```
GET /family/members
```

**参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| familyId | Long | 否 | 家庭ID |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "memberId": 1,
      "name": "张三",
      "gender": 1,
      "birthDate": "1990-01-15",
      "age": 36,
      "relation": "本人",
      "role": 1,
      "avatarUrl": "https://example.com/avatar.jpg",
      "createTime": "2026-01-01T10:00:00"
    }
  ]
}
```

---

### 获取成员详情

获取成员的详细档案信息。

**请求**

```
GET /family/members/{id}
```

**路径参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 成员ID |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "memberId": 1,
    "name": "张三",
    "gender": 1,
    "birthDate": "1990-01-15",
    "age": 36,
    "relation": "本人",
    "avatarUrl": "https://example.com/avatar.jpg",
    "height": 175.5,
    "weight": 70.0,
    "bmi": 22.7,
    "bloodType": "A",
    "allergies": ["青霉素", "花粉"],
    "chronicDiseases": ["高血压"]
  }
}
```

---

### 更新成员信息

更新家庭成员的基本信息。

**请求**

```
PUT /family/members/{id}
```

**路径参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 成员ID |

**请求体**

与添加成员相同。

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

### 删除成员

删除家庭成员（逻辑删除）。

**请求**

```
DELETE /family/members/{id}
```

**路径参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 成员ID |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

## 错误响应示例

### 参数校验失败

```json
{
  "code": 400,
  "message": "姓名不能为空",
  "data": null,
  "timestamp": 1706500000000
}
```

### 成员不存在

```json
{
  "code": 1001,
  "message": "成员不存在",
  "data": null,
  "timestamp": 1706500000000
}
```

### 成员数量超限

```json
{
  "code": 1002,
  "message": "家庭成员数量已达上限",
  "data": null,
  "timestamp": 1706500000000
}
```

---

## 在线文档

启动应用后，可访问Swagger UI查看交互式API文档：

```
http://localhost:8080/swagger-ui.html
```
