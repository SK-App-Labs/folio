// File: app/src/main/java/com/folioledger/app/data/local/FolioDatabase.kt
package com.folioledger.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.folioledger.app.data.local.dao.AccountDao
import com.folioledger.app.data.local.dao.BudgetAndRecurringDao
import com.folioledger.app.data.local.dao.CategoryAndTagDao
import com.folioledger.app.data.local.dao.TransactionDao
import com.folioledger.app.data.local.entity.AccountEntity
import com.folioledger.app.data.local.entity.BookmarkEntity
import com.folioledger.app.data.local.entity.BudgetEntity
import com.folioledger.app.data.local.entity.CategoryEntity
import com.folioledger.app.data.local.entity.FolioTypeConverters
import com.folioledger.app.data.local.entity.InstallmentPlanEntity
import com.folioledger.app.data.local.entity.RecurringRuleEntity
import com.folioledger.app.data.local.entity.TagEntity
import com.folioledger.app.data.local.entity.TransactionEntity
import com.folioledger.app.data.local.entity.TransactionSplitEntity
import com.folioledger.app.data.local.entity.TransactionTagCrossRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        TransactionSplitEntity::class,
        TagEntity::class,
        TransactionTagCrossRef::class,
        BookmarkEntity::class,
        BudgetEntity::class,
        RecurringRuleEntity::class,
        InstallmentPlanEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(FolioTypeConverters::class)
abstract class FolioDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun categoryAndTagDao(): CategoryAndTagDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetAndRecurringDao(): BudgetAndRecurringDao

    companion object {
        const val DATABASE_NAME: String = "folio_ledger.db"

        @Volatile
        private var INSTANCE: FolioDatabase? = null

        private val databaseWriteScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun getInstance(context: Context): FolioDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { db ->
                    INSTANCE = db
                }
            }
        }

        private fun buildDatabase(appContext: Context): FolioDatabase {
            return Room.databaseBuilder(
                appContext,
                FolioDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        databaseWriteScope.launch {
                            INSTANCE?.let { folioDb ->
                                DefaultCategorySeeder.seedIfEmpty(folioDb.categoryAndTagDao())
                            }
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        databaseWriteScope.launch {
                            INSTANCE?.let { folioDb ->
                                DefaultCategorySeeder.seedIfEmpty(folioDb.categoryAndTagDao())
                            }
                        }
                    }
                })
                .build()
        }
    }
}