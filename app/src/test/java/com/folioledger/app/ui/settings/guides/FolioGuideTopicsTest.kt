package com.folioledger.app.ui.settings.guides

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FolioGuideTopicsTest {

    @Test
    fun `all guide topic IDs are unique and non-blank`() {
        val topics = FolioGuideTopics.allTopics
        assertTrue("Topics list should not be empty", topics.isNotEmpty())

        val ids = topics.map { it.id }
        val uniqueIds = ids.toSet()
        assertEquals("Duplicate guide topic IDs found", ids.size, uniqueIds.size)
        assertTrue("All topic IDs must be non-blank", ids.all { it.isNotBlank() })
    }

    @Test
    fun `every guide topic has title, summary, and non-empty bullet points`() {
        for (topic in FolioGuideTopics.allTopics) {
            assertTrue("Topic ${topic.id} has blank title", topic.title.isNotBlank())
            assertTrue("Topic ${topic.id} has blank summary", topic.summary.isNotBlank())
            assertTrue("Topic ${topic.id} has empty bullet points", topic.bulletPoints.isNotEmpty())
            assertTrue(
                "Topic ${topic.id} has blank bullet point",
                topic.bulletPoints.all { it.isNotBlank() }
            )
        }
    }

    @Test
    fun `every specific guide category tab has at least one topic`() {
        val specificCategories = GuideCategoryTab.entries.filter { it != GuideCategoryTab.ALL }
        for (category in specificCategories) {
            val count = FolioGuideTopics.allTopics.count { it.category == category }
            assertTrue("Category $category should have at least one topic", count > 0)
        }
    }
}
