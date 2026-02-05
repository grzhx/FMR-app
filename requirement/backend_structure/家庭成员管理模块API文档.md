# 家庭成员管理模块 API 文档

## 概述

本文档描述了家庭医疗档案管理系统（FMR-Backend）的家庭成员管理相关接口，包括成员的增删改查和健康档案管理。

**基础URL**: `http://localhost:8080/api`

**认证方式**: JWT Token（所有接口需要携带 Authorization Header）

---

## 接口列表

### 1. 添加家庭成员

添加新的家庭成员，一个家庭最多支持10人。

**请求**

```
POST /api/family/members
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**

```json
{
  "familyId": 1,
  "name": "张三",
  "gender": 1,
  "birthDate": "1990-01-15",
  "relation": "SELF",
  "role": 1,
  "avatarUrl": "https://example.com/avatar.jpg",
  "viewAll": true,
  "editAll": true
}
```

**参数说明**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| familyId | Long | 是 | 家庭ID |
| name | String | 是 | 成员姓名（1-50字符） |
| gender | Integer | 是 | 性别：1-男，2-女 |
| birthDate | String | 是 | 出生日期（格式：YYYY-MM-DD） |
| relation | String | 是 | 与户主关系：SELF/SPOUSE/CHILD/PARENT/OTHER |
| role | Integer | 否 | 角色：1-管理员，2-普通成员（默认2） |
| avatarUrl | String | 否 | 头像URL |
| viewAll | Boolean | 否 | 是否可查看所有人数据（默认false） |
| editAll | Boolean | 否 | 是否可编辑所有人数据（默认false） |

**成功响应**

```json
{
  "code": 200,
  "message": "添加成功",
  "data": 10001,
  "timestamp": 1706500000000
}
```

**错误响应**

| 错误码 | 说明 |
|--------|------|
| 400 | 参数校验失败 |
| 3001 | 家庭成员已达上限（10人） |

---

### 2. 获取家庭成员列表

获取指定家庭的所有成员列表。

**请求**

```
GET /api/family/members?familyId=1
Authorization: Bearer {token}
```

**参数说明**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| familyId | Long | 否 | 家庭ID（不传则返回当前用户所属家庭） |

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "memberId": 10001,
      "name": "张三",
      "gender": 1,
      "birthDate": "1990-01-15",
      "age": 36,
      "relation": "SELF",
      "avatarUrl": "https://example.com/avatar.jpg",
      "role": 1
    },
    {
      "memberId": 10002,
      "name": "李四",
      "gender": 2,
      "birthDate": "1992-05-20",
      "age": 33,
      "relation": "SPOUSE",
      "avatarUrl": null,
      "role": 2
    }
  ],
  "timestamp": 1706500000000
}
```

---

### 3. 获取成员详情（含健康档案）

获取成员的详细档案信息，包括基本信息和健康档案。

**请求**

```
GET /api/family/members/{id}
Authorization: Bearer {token}
```

**路径参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 成员ID |

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "memberId": 10001,
    "name": "张三",
    "gender": 1,
    "birthDate": "1990-01-15",
    "age": 36,
    "relation": "SELF",
    "avatarUrl": "https://example.com/avatar.jpg",
    "height": 175.00,
    "weight": 70.50,
    "bmi": 23.02,
    "bloodType": "A",
    "allergies": ["青霉素", "花粉"],
    "chronicDiseases": ["高血压", "糖尿病"]
  },
  "timestamp": 1706500000000
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| memberId | Long | 成员ID |
| name | String | 姓名 |
| gender | Integer | 性别：1-男，2-女 |
| birthDate | String | 出生日期 |
| age | Integer | 年龄（自动计算） |
| relation | String | 与户主关系 |
| avatarUrl | String | 头像URL |
| height | BigDecimal | 身高（cm） |
| weight | BigDecimal | 体重（kg） |
| bmi | BigDecimal | BMI指数（自动计算） |
| bloodType | String | 血型（A/B/AB/O/OTHER） |
| allergies | Array | 过敏史列表 |
| chronicDiseases | Array | 慢性病列表 |

**错误响应**

| 错误码 | 说明 |
|--------|------|
| 3002 | 成员不存在 |

---

### 4. 更新成员基本信息

更新家庭成员的基本信息。

**请求**

```
PUT /api/family/members/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

**路径参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 成员ID |

**请求体**

```json
{
  "name": "张三",
  "gender": 1,
  "birthDate": "1990-01-15",
  "relation": "SELF",
  "role": 1,
  "avatarUrl": "https://example.com/new-avatar.jpg"
}
```

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1706500000000
}
```

**错误响应**

| 错误码 | 说明 |
|--------|------|
| 400 | 参数校验失败 |
| 3002 | 成员不存在 |

---

### 5. 删除成员

删除家庭成员（逻辑删除）。

**请求**

```
DELETE /api/family/members/{id}
Authorization: Bearer {token}
```

**路径参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 成员ID |

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1706500000000
}
```

**错误响应**

| 错误码 | 说明 |
|--------|------|
| 3002 | 成员不存在 |

---

### 6. 更新健康档案

更新成员的健康基础档案信息（身高、体重、血型、过敏史、慢性病等）。

**请求**

```
PUT /api/family/members/{id}/health-profile
Authorization: Bearer {token}
Content-Type: application/json
```

**路径参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 成员ID |

**请求体**

```json
{
  "height": 175.00,
  "weight": 70.50,
  "bloodType": "A",
  "allergies": ["青霉素", "花粉"],
  "chronicDiseases": ["高血压", "糖尿病"]
}
```

**参数说明**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| height | BigDecimal | 否 | 身高（cm），范围：50-250 |
| weight | BigDecimal | 否 | 体重（kg），范围：1-500 |
| bloodType | String | 否 | 血型：A/B/AB/O/OTHER |
| allergies | Array | 否 | 过敏史列表 |
| chronicDiseases | Array | 否 | 慢性病列表 |

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1706500000000
}
```

**错误响应**

| 错误码 | 说明 |
|--------|------|
| 400 | 参数校验失败 |
| 3002 | 成员不存在 |

---

## 错误码汇总

### 家庭成员相关错误码

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 3001 | 家庭成员已达上限 | 提示用户删除部分成员后再添加 |
| 3002 | 成员不存在 | 检查成员ID是否正确 |

### 通用错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权访问 |
| 500 | 服务器内部错误 |

---

## 数据字典

### 关系类型（relation）

| 值 | 说明 |
|------|------|
| SELF | 本人 |
| SPOUSE | 配偶 |
| CHILD | 子女 |
| PARENT | 父母 |
| OTHER | 其他 |

### 角色类型（role）

| 值 | 说明 |
|------|------|
| 1 | 管理员 |
| 2 | 普通成员 |

### 血型（bloodType）

| 值 | 说明 |
|------|------|
| A | A型 |
| B | B型 |
| AB | AB型 |
| O | O型 |
| OTHER | 其他/未知 |

---

## 在线文档

启动应用后，可访问 Swagger UI 查看交互式 API 文档：

```
http://localhost:8080/swagger-ui.html
```

或使用 Knife4j 增强文档：

```
http://localhost:8080/doc.html
