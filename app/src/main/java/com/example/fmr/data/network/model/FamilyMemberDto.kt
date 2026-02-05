package com.example.fmr.data.network.model

import com.example.fmr.data.entity.FamilyMember
import com.google.gson.annotations.SerializedName

/**
 * 添加家庭成员请求DTO
 */
data class AddMemberRequest(
    @SerializedName("familyId")
    val familyId: Long? = null,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("gender")
    val gender: Int,
    
    @SerializedName("birthDate")
    val birthDate: String,
    
    @SerializedName("relation")
    val relation: String,
    
    @SerializedName("role")
    val role: Int? = null,
    
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,
    
    @SerializedName("viewAll")
    val viewAll: Boolean? = null,
    
    @SerializedName("editAll")
    val editAll: Boolean? = null
)

/**
 * 家庭成员响应DTO
 */
data class FamilyMemberDto(
    @SerializedName("memberId")
    val memberId: Long,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("gender")
    val gender: Int,
    
    @SerializedName("birthDate")
    val birthDate: String,
    
    @SerializedName("age")
    val age: Int?,
    
    @SerializedName("relation")
    val relation: String,
    
    @SerializedName("role")
    val role: Int?,
    
    @SerializedName("avatarUrl")
    val avatarUrl: String?,
    
    @SerializedName("createTime")
    val createTime: String?
) {
    /**
     * 转换为本地实体
     */
    fun toEntity(): FamilyMember {
        return FamilyMember(
            id = memberId,
            name = name,
            gender = gender,
            birthDate = birthDate,
            relation = relation,
            role = role ?: FamilyMember.ROLE_MEMBER,
            avatarUrl = avatarUrl
        )
    }
}

/**
 * 成员详情响应DTO
 */
data class MemberProfileDto(
    @SerializedName("memberId")
    val memberId: Long,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("gender")
    val gender: Int,
    
    @SerializedName("birthDate")
    val birthDate: String,
    
    @SerializedName("age")
    val age: Int?,
    
    @SerializedName("relation")
    val relation: String,
    
    @SerializedName("avatarUrl")
    val avatarUrl: String?,
    
    @SerializedName("height")
    val height: Double?,
    
    @SerializedName("weight")
    val weight: Double?,
    
    @SerializedName("bmi")
    val bmi: Double?,
    
    @SerializedName("bloodType")
    val bloodType: String?,
    
    @SerializedName("allergies")
    val allergies: List<String>?,
    
    @SerializedName("chronicDiseases")
    val chronicDiseases: List<String>?
) {
    /**
     * 转换为本地实体
     */
    fun toEntity(): FamilyMember {
        return FamilyMember(
            id = memberId,
            name = name,
            gender = gender,
            birthDate = birthDate,
            relation = relation,
            avatarUrl = avatarUrl
        )
    }
}

/**
 * 更新成员请求DTO
 */
data class UpdateMemberRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("gender")
    val gender: Int,
    
    @SerializedName("birthDate")
    val birthDate: String,
    
    @SerializedName("relation")
    val relation: String,
    
    @SerializedName("role")
    val role: Int? = null,
    
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null
)

/**
 * 更新健康档案请求DTO
 */
data class UpdateHealthProfileRequest(
    @SerializedName("height")
    val height: Double? = null,
    
    @SerializedName("weight")
    val weight: Double? = null,
    
    @SerializedName("bloodType")
    val bloodType: String? = null,
    
    @SerializedName("allergies")
    val allergies: List<String>? = null,
    
    @SerializedName("chronicDiseases")
    val chronicDiseases: List<String>? = null
)

/**
 * FamilyMember扩展函数：转换为请求DTO
 */
fun FamilyMember.toAddRequest(): AddMemberRequest {
    return AddMemberRequest(
        familyId = familyId,
        name = name,
        gender = gender,
        birthDate = birthDate,
        relation = relation,
        role = role,
        avatarUrl = avatarUrl,
        viewAll = viewAll,
        editAll = editAll
    )
}

fun FamilyMember.toUpdateRequest(): UpdateMemberRequest {
    return UpdateMemberRequest(
        name = name,
        gender = gender,
        birthDate = birthDate,
        relation = relation,
        role = role,
        avatarUrl = avatarUrl
    )
}
