// File: app/src/main/java/com/folioledger/app/data/local/FolioDatabase.kt
package com.folioledger.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.folioledger.app.BuildConfig
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

import androidx.room.migration.Migration
import java.io.File

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
    version = 2,
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

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE accounts ADD COLUMN initialUnbilledMinor INTEGER DEFAULT NULL")
            }
        }

        @Volatile
        private var INSTANCE: FolioDatabase? = null

        private val databaseWriteScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun getInstance(context: Context): FolioDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    val appContext = context.applicationContext
                    DatabaseBackupManager.createPreMigrationBackup(appContext)
                    val db = runCatching {
                        buildDatabase(appContext).also {
                            it.openHelper.writableDatabase
                        }
                    }.getOrElse {
                        DatabaseBackupManager.restorePreMigrationBackup(appContext)
                        buildDatabase(appContext)
                    }
                    db.also { INSTANCE = it }
                }
            }
        }

        private fun buildDatabase(appContext: Context): FolioDatabase {
            val builder = Room.databaseBuilder(
                appContext,
                FolioDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_1_2)
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

            if (BuildConfig.DEBUG) {
                builder.fallbackToDestructiveMigration(dropAllTables = true)
            }

            return builder.build()
        }
    }
}

object DatabaseBackupManager {
    private const val DB_NAME = FolioDatabase.DATABASE_NAME
    private const val BAK_SUFFIX = ".pre_migration_bak"

    fun createPreMigrationBackup(context: Context) {
        runCatching {
            val dbFile = context.getDatabasePath(DB_NAME)
            if (dbFile.exists() && dbFile.length() > 0L) {
                val parentDir = dbFile.parentFile ?: return@runCatching
                val bakFile = File(parentDir, "$DB_NAME$BAK_SUFFIX")
                dbFile.copyTo(bakFile, overwrite = true)

                val walFile = File(parentDir, "$DB_NAME-wal")
                if (walFile.exists()) {
                    walFile.copyTo(File(parentDir, "$DB_NAME-wal$BAK_SUFFIX"), overwrite = true)
                }

                val shmFile = File(parentDir, "$DB_NAME-shm")
                if (shmFile.exists()) {
                    shmFile.copyTo(File(parentDir, "$DB_NAME-shm$BAK_SUFFIX"), overwrite = true)
                }
            }
        }
    }

    fun restorePreMigrationBackup(context: Context): Boolean {
        return runCatching {
            val dbFile = context.getDatabasePath(DB_NAME)
            val parentDir = dbFile.parentFile ?: return@runCatching false
            val bakFile = File(parentDir, "$DB_NAME$BAK_SUFFIX")
            if (bakFile.exists()) {
                bakFile.copyTo(dbFile, overwrite = true)

                val walBak = File(parentDir, "$DB_NAME-wal$BAK_SUFFIX")
                if (walBak.exists()) {
                    walBak.copyTo(File(parentDir, "$DB_NAME-wal"), overwrite = true)
                }

                val shmBak = File(parentDir, "$DB_NAME-shm$BAK_SUFFIX")
                if (shmBak.exists()) {
                    shmBak.copyTo(File(parentDir, "$DB_NAME-shm"), overwrite = true)
                }
                true
            } else {
                false
            }
        }.getOrDefault(false)
    }
}