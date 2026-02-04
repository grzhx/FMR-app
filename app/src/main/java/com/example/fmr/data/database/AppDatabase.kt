package com.example.fmr.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fmr.data.dao.*
import com.example.fmr.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 应用数据库
 * 家庭病历本的本地数据库
 */
@Database(
    entities = [
        FamilyMember::class,
        MedicalRecord::class,
        Document::class,
        Medication::class,
        MedicationSchedule::class,
        LabReport::class,
        LabResult::class,
        DailyGoal::class,
        FollowUp::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    // 基础DAO
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun medicalRecordDao(): MedicalRecordDao
    abstract fun documentDao(): DocumentDao
    
    // 用药助手DAO
    abstract fun medicationDao(): MedicationDao
    
    // 报告解读DAO
    abstract fun labReportDao(): LabReportDao
    
    // 生活管理DAO
    abstract fun lifestyleDao(): LifestyleDao
    
    companion object {
        private const val DATABASE_NAME = "family_medical_record.db"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * 数据库回调，用于初始化默认数据
     */
    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database)
                }
            }
        }
        
        /**
         * 初始化默认数据
         */
        private suspend fun populateDatabase(database: AppDatabase) {
            val familyMemberDao = database.familyMemberDao()
            
            // 添加一个默认的"本人"成员作为示例
            val defaultMember = FamilyMember(
                name = "我",
                gender = FamilyMember.GENDER_MALE,
                birthDate = "1990-01-01",
                relation = FamilyMember.RELATION_SELF,
                role = FamilyMember.ROLE_ADMIN,
                viewAll = true,
                editAll = true
            )
            familyMemberDao.insert(defaultMember)
        }
    }
}
