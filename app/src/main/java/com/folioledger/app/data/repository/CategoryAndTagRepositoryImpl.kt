// File: app/src/main/java/com/folioledger/app/data/repository/CategoryAndTagRepositoryImpl.kt
package com.folioledger.app.data.repository

import androidx.room.withTransaction
import com.folioledger.app.data.local.FolioDatabase
import com.folioledger.app.data.local.DefaultCategorySeeder
import com.folioledger.app.data.local.dao.CategoryAndTagDao
import com.folioledger.app.data.local.entity.BookmarkEntity
import com.folioledger.app.data.local.entity.CategoryEntity
import com.folioledger.app.data.local.entity.TagEntity
import com.folioledger.app.domain.model.CategoryNode
import com.folioledger.app.domain.model.CategoryType
import com.folioledger.app.domain.model.MinorMoney
import com.folioledger.app.domain.model.TransactionBookmark
import com.folioledger.app.domain.repository.CategoryAndTagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class CategoryAndTagRepositoryImpl(
    private val categoryAndTagDao: CategoryAndTagDao,
    private val database: FolioDatabase
) : CategoryAndTagRepository {

    override fun observeCategoryTree(type: CategoryType): Flow<List<CategoryNode>> {
        return categoryAndTagDao.observeCategoriesByType(type)
            .onStart { DefaultCategorySeeder.seedIfEmpty(categoryAndTagDao) }
            .map { entities -> buildCategoryTree(entities) }
    }

    override fun observeAllCategoryTrees(): Flow<List<CategoryNode>> {
        return categoryAndTagDao.observeAllCategories()
            .onStart { DefaultCategorySeeder.seedIfEmpty(categoryAndTagDao) }
            .map { entities -> buildCategoryTree(entities) }
    }

    override fun observeAllTags(): Flow<List<String>> {
        return categoryAndTagDao.observeAllTags()
            .map { tagEntities -> tagEntities.map { it.name } }
    }

    override fun observeBookmarks(): Flow<List<TransactionBookmark>> {
        return categoryAndTagDao.observeAllBookmarks()
            .map { bookmarkEntities ->
                bookmarkEntities.map { entity ->
                    TransactionBookmark(
                        id = entity.id,
                        title = entity.title,
                        type = entity.type,
                        amount = MinorMoney(entity.amountMinor),
                        accountId = entity.accountId,
                        transferToAccountId = entity.transferToAccountId,
                        categoryId = entity.categoryId,
                        payeeOrDescription = entity.payeeOrDescription,
                        note = entity.note,
                        tags = if (entity.tagsCsv.isBlank()) {
                            emptyList()
                        } else {
                            entity.tagsCsv.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                        }
                    )
                }
            }
    }

    override suspend fun upsertCategory(category: CategoryNode): Result<Long> = runCatching {
        val trimmedName = category.name.trim()
        require(trimmedName.isNotEmpty()) { "Category name cannot be blank." }

        val existing = if (category.id != 0L) {
            categoryAndTagDao.getCategoryByIdOnce(category.id)
        } else {
            null
        }

        val entity = CategoryEntity(
            id = category.id,
            name = trimmedName,
            type = category.type,
            parentId = category.parentId,
            iconName = category.iconName.ifBlank { "category" },
            colorHex = category.colorHex.ifBlank { "#10B981" },
            sortOrder = existing?.sortOrder ?: 0,
            isArchived = existing?.isArchived ?: false
        )
        categoryAndTagDao.insertCategory(entity)
    }

    override suspend fun deleteCategory(
        categoryId: Long,
        reassignToCategoryId: Long?
    ): Result<Unit> = runCatching {
        database.withTransaction {
            if (reassignToCategoryId != null && reassignToCategoryId != categoryId) {
                categoryAndTagDao.reassignTransactionsCategory(categoryId, reassignToCategoryId)
                categoryAndTagDao.reassignSplitsCategory(categoryId, reassignToCategoryId)
            }
            categoryAndTagDao.deleteCategoryById(categoryId)
        }
    }

    override suspend fun createTag(tagName: String): Result<Unit> = runCatching {
        val trimmed = tagName.trim()
        require(trimmed.isNotEmpty()) { "Tag name cannot be blank." }
        categoryAndTagDao.insertTag(TagEntity(name = trimmed))
    }

    override suspend fun renameTag(oldTagName: String, newTagName: String): Result<Unit> = runCatching {
        val oldTrimmed = oldTagName.trim()
        val newTrimmed = newTagName.trim()
        require(oldTrimmed.isNotEmpty() && newTrimmed.isNotEmpty()) {
            "Tag names cannot be blank."
        }
        categoryAndTagDao.renameTag(oldTrimmed, newTrimmed)
    }

    override suspend fun deleteTag(tagName: String): Result<Unit> = runCatching {
        val trimmed = tagName.trim()
        if (trimmed.isNotEmpty()) {
            categoryAndTagDao.deleteTagByName(trimmed)
        }
    }

    override suspend fun upsertBookmark(bookmark: TransactionBookmark): Result<Long> = runCatching {
        val trimmedTitle = bookmark.title.trim()
        require(trimmedTitle.isNotEmpty()) { "Bookmark title cannot be blank." }
        require(bookmark.amount.isPositive) { "Bookmark amount must be greater than zero." }

        val tagsCsv = bookmark.tags
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .joinToString(",")

        val entity = BookmarkEntity(
            id = bookmark.id,
            title = trimmedTitle,
            type = bookmark.type,
            amountMinor = bookmark.amount.amountMinor,
            accountId = bookmark.accountId,
            transferToAccountId = bookmark.transferToAccountId,
            categoryId = bookmark.categoryId,
            payeeOrDescription = bookmark.payeeOrDescription.trim(),
            note = bookmark.note.trim(),
            tagsCsv = tagsCsv
        )
        categoryAndTagDao.upsertBookmark(entity)
    }

    override suspend fun deleteBookmark(bookmarkId: Long): Result<Unit> = runCatching {
        categoryAndTagDao.deleteBookmarkById(bookmarkId)
    }

    private fun buildCategoryTree(entities: List<CategoryEntity>): List<CategoryNode> {
        val childrenByParentId = entities
            .filter { it.parentId != null }
            .groupBy { it.parentId }

        return entities
            .filter { it.parentId == null }
            .sortedWith(compareBy<CategoryEntity> { it.sortOrder }.thenBy { it.id })
            .map { mainCat ->
                val subcategories = childrenByParentId[mainCat.id]
                    .orEmpty()
                    .sortedWith(compareBy<CategoryEntity> { it.sortOrder }.thenBy { it.id })
                    .map { subCat ->
                        CategoryNode(
                            id = subCat.id,
                            name = subCat.name,
                            type = subCat.type,
                            parentId = subCat.parentId,
                            iconName = subCat.iconName,
                            colorHex = subCat.colorHex,
                            subcategories = emptyList()
                        )
                    }

                CategoryNode(
                    id = mainCat.id,
                    name = mainCat.name,
                    type = mainCat.type,
                    parentId = null,
                    iconName = mainCat.iconName,
                    colorHex = mainCat.colorHex,
                    subcategories = subcategories
                )
            }
    }
}
