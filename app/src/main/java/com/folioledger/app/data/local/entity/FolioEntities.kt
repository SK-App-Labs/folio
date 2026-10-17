// File: app/src/main/java/com/folioledger/app/data/local/entity/FolioEntities.kt
package com.folioledger.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import com.folioledger.app.domain.model.AccountGroup
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.RepeatFrequency
import com.folioledger.app.domain.model.TransactionType

class FolioTypeConverters {

    @TypeConverter
    fun fromAccountGroup(value: AccountGroup?): String? = value?.name

    @TypeConverter
    fun toAccountGroup(value: String?): AccountGroup? =
        value?.let { runCatching { AccountGroup.valueOf(it) }.getOrDefault(AccountGroup.OTHER) }

    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String? = value?.name

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? =
        value?.let { runCatching { TransactionType.valueOf(it) }.getOrDefault(TransactionType.EXPENSE) }

    @TypeConverter
    fun fromCategoryType(value: CategoryType?): String? = value?.name

    @TypeConverter
    fun toCategoryType(value: String?): CategoryType? =
        value?.let { runCatching { CategoryType.valueOf(it) }.getOrDefault(CategoryType.EXPENSE) }

    @TypeConverter
    fun fromRepeatFrequency(value: RepeatFrequency?): String? = value?.name

    @TypeConverter
    fun toRepeatFrequency(value: String?): RepeatFrequency? =
        value?.let { runCatching { RepeatFrequency.valueOf(it) }.getOrDefault(RepeatFrequency.MONTHLY) }
}

@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentAccountId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["groupType"]),
        Index(value = ["parentAccountId"]),
        Index(value = ["isArchived"])
    ]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val groupType: AccountGroup,
    val currencyCode: String,
    val openingBalanceMinor: Long = 0L,
    val creditLimitMinor: Long? = null,
    val settlementDayOfMonth: Int? = null,
    val paymentDueDayOfMonth: Int? = null,
    val parentAccountId: Long? = null,
    val includeInNetWorth: Boolean = true,
    val isArchived: Boolean = false,
    val colorHex: String = "#10B981",
    val iconName: String = "account_balance_wallet",
    val sortOrder: Int = 0,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["type"]),
        Index(value = ["parentId"]),
        Index(value = ["isArchived"])
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val type: CategoryType,
    val parentId: Long? = null,
    val iconName: String,
    val colorHex: String,
    val sortOrder: Int = 0,
    val isArchived: Boolean = false
)

@Entity(
    tableName = "installment_plans",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["categoryId"])
    ]
)
data class InstallmentPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val accountId: Long,
    val categoryId: Long? = null,
    val totalAmountMinor: Long,
    val monthsCount: Int,
    val startDateEpochDay: Long,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "recurring_rules",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["transferToAccountId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["transferToAccountId"]),
        Index(value = ["categoryId"]),
        Index(value = ["isActive", "nextOccurrenceEpochDay"])
    ]
)
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val type: TransactionType,
    val amountMinor: Long,
    val currencyCode: String,
    val accountId: Long,
    val transferToAccountId: Long? = null,
    val categoryId: Long? = null,
    val frequency: RepeatFrequency,
    val startDateEpochDay: Long,
    val nextOccurrenceEpochDay: Long,
    val endDateEpochDay: Long? = null,
    val payeeOrDescription: String = "",
    val note: String = "",
    val isActive: Boolean = true
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["transferToAccountId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = InstallmentPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["installmentPlanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RecurringRuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["recurringRuleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["ledgerDateEpochDay"]),
        Index(value = ["timestampEpochMillis"]),
        Index(value = ["accountId"]),
        Index(value = ["transferToAccountId"]),
        Index(value = ["categoryId"]),
        Index(value = ["type"]),
        Index(value = ["installmentPlanId"]),
        Index(value = ["recurringRuleId"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: TransactionType,
    val timestampEpochMillis: Long,
    val ledgerDateEpochDay: Long,
    val amountMinor: Long,
    val currencyCode: String,
    val accountId: Long,
    val transferToAccountId: Long? = null,
    val transferFeeMinor: Long = 0L,
    val categoryId: Long? = null,
    val isSplit: Boolean = false,
    val payeeOrDescription: String = "",
    val note: String = "",
    val originalTransactionId: Long? = null,
    val installmentPlanId: Long? = null,
    val installmentIndex: Int? = null,
    val installmentTotalCount: Int? = null,
    val recurringRuleId: Long? = null
)

@Entity(
    tableName = "transaction_splits",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["transactionId"]),
        Index(value = ["categoryId"])
    ]
)
data class TransactionSplitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val transactionId: Long,
    val categoryId: Long,
    val amountMinor: Long,
    val note: String = ""
)

@Entity(
    tableName = "tags",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val colorHex: String = "#64748B"
)

@Entity(
    tableName = "transaction_tags",
    primaryKeys = ["transactionId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["transactionId"]),
        Index(value = ["tagId"])
    ]
)
data class TransactionTagCrossRef(
    val transactionId: Long,
    val tagId: Long
)

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["transferToAccountId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["transferToAccountId"]),
        Index(value = ["categoryId"]),
        Index(value = ["sortOrder"])
    ]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val type: TransactionType,
    val amountMinor: Long,
    val accountId: Long,
    val transferToAccountId: Long? = null,
    val categoryId: Long? = null,
    val payeeOrDescription: String = "",
    val note: String = "",
    val tagsCsv: String = "",
    val sortOrder: Int = 0
)

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["categoryId"], unique = true)
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val categoryId: Long? = null,
    val limitAmountMinor: Long,
    val alertThresholdPercent: Int = 80,
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
)

data class TransactionWithDetails(
    @Embedded
    val transaction: TransactionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val splits: List<TransactionSplitEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TransactionTagCrossRef::class,
            parentColumn = "transactionId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)