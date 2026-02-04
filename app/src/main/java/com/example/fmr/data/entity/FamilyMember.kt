package com.example.fmr.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 家庭成员实体类
 * 对应数据库表 t_family_member
 */
@Entity(tableName = "t_family_member")
data class FamilyMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "family_id")
    val familyId: Long = 1, // 默认家庭ID为1
    
    @ColumnInfo(name = "user_id")
    val userId: Long? = null,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "gender")
    val gender: Int, // 1-男，2-女
    
    @ColumnInfo(name = "birth_date")
    val birthDate: String, // 格式：YYYY-MM-DD
    
    @ColumnInfo(name = "relation")
    val relation: String, // SELF, SPOUSE, CHILD, PARENT, OTHER
    
    @ColumnInfo(name = "role")
    val role: Int = 2, // 1-管理员，2-普通成员
    
    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String? = null,
    
    @ColumnInfo(name = "view_all")
    val viewAll: Boolean = false,
    
    @ColumnInfo(name = "edit_all")
    val editAll: Boolean = false,
    
    @ColumnInfo(name = "status")
    val status: Int = 1, // 1-正常，2-停用，3-删除
    
    @ColumnInfo(name = "create_time")
    val createTime: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "update_time")
    val updateTime: Long = System.currentTimeMillis()
) {
    companion object {
        // 性别常量
        const val GENDER_MALE = 1
        const val GENDER_FEMALE = 2
        
        // 角色常量
        const val ROLE_ADMIN = 1
        const val ROLE_MEMBER = 2
        
        // 状态常量
        const val STATUS_ACTIVE = 1
        const val STATUS_INACTIVE = 2
        const val STATUS_DELETED = 3
        
        // 关系常量
        const val RELATION_SELF = "SELF"
        const val RELATION_SPOUSE = "SPOUSE"
        const val RELATION_CHILD = "CHILD"
        const val RELATION_PARENT = "PARENT"
        const val RELATION_OTHER = "OTHER"
    }
    
    /**
     * 获取性别显示文本
     */
    fun getGenderText(): String = when (gender) {
        GENDER_MALE -> "男"
        GENDER_FEMALE -> "女"
        else -> "未知"
    }
    
    /**
     * 获取关系显示文本
     */
    fun getRelationText(): String = when (relation) {
        RELATION_SELF -> "本人"
        RELATION_SPOUSE -> "配偶"
        RELATION_CHILD -> "子女"
        RELATION_PARENT -> "父母"
        RELATION_OTHER -> "其他"
        else -> relation
    }
    
    /**
     * 计算年龄
     */
    fun calculateAge(): Int {
        return try {
            val parts = birthDate.split("-")
            val birthYear = parts[0].toInt()
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            currentYear - birthYear
        } catch (e: Exception) {
            0
        }
    }
}
