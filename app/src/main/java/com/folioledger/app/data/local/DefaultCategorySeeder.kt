// File: app/src/main/java/com/folioledger/app/data/local/DefaultCategorySeeder.kt
package com.folioledger.app.data.local

import com.folioledger.app.data.local.dao.CategoryAndTagDao
import com.folioledger.app.data.local.entity.CategoryEntity
import com.folioledger.app.domain.model.CategoryType

object DefaultCategorySeeder {

    fun getDefaultCategories(): List<CategoryEntity> = listOf(
        // =====================================================================
        // EXPENSE MAIN CATEGORIES (IDs 1..9) & SUBCATEGORIES (IDs 101..199)
        // =====================================================================

        // 1. Food & Dining
        CategoryEntity(
            id = 1L,
            name = "Food & Dining",
            type = CategoryType.EXPENSE,
            parentId = null,
            iconName = "restaurant",
            colorHex = "#F97316",
            sortOrder = 1
        ),
        CategoryEntity(
            id = 101L,
            name = "Restaurants & Dining Out",
            type = CategoryType.EXPENSE,
            parentId = 1L,
            iconName = "dining",
            colorHex = "#F97316",
            sortOrder = 1
        ),
        CategoryEntity(
            id = 102L,
            name = "Coffee, Tea & Snacks",
            type = CategoryType.EXPENSE,
            parentId = 1L,
            iconName = "local_cafe",
            colorHex = "#F97316",
            sortOrder = 2
        ),
        CategoryEntity(
            id = 103L,
            name = "Food Delivery & Takeout",
            type = CategoryType.EXPENSE,
            parentId = 1L,
            iconName = "delivery_dining",
            colorHex = "#F97316",
            sortOrder = 3
        ),

        // 2. Groceries
        CategoryEntity(
            id = 2L,
            name = "Groceries",
            type = CategoryType.EXPENSE,
            parentId = null,
            iconName = "local_grocery_store",
            colorHex = "#10B981",
            sortOrder = 2
        ),
        CategoryEntity(
            id = 111L,
            name = "Vegetables & Fruits",
            type = CategoryType.EXPENSE,
            parentId = 2L,
            iconName = "eco",
            colorHex = "#10B981",
            sortOrder = 1
        ),
        CategoryEntity(
            id = 112L,
            name = "Dairy, Staples & Spices",
            type = CategoryType.EXPENSE,
            parentId = 2L,
            iconName = "kitchen",
            colorHex = "#10B981",
            sortOrder = 2
        ),
        CategoryEntity(
            id = 113L,
            name = "Meat, Poultry & Seafood",
            type = CategoryType.EXPENSE,
            parentId = 2L,
            iconName = "set_meal",
            colorHex = "#10B981",
            sortOrder = 3
        ),
        CategoryEntity(
            id = 114L,
            name = "Household Supplies",
            type = CategoryType.EXPENSE,
            parentId = 2L,
            iconName = "cleaning_services",
            colorHex = "#10B981",
            sortOrder = 4
        ),

        // 3. Housing & Utilities
        CategoryEntity(
            id = 3L,
            name = "Housing & Utilities",
            type = CategoryType.EXPENSE,
            parentId = null,
            iconName = "home",
            colorHex = "#3B82F6",
            sortOrder = 3
        ),
        CategoryEntity(
            id = 121L,
            name = "Rent & Society Maintenance",
            type = CategoryType.EXPENSE,
            parentId = 3L,
            iconName = "apartment",
            colorHex = "#3B82F6",
            sortOrder = 1
        ),
        CategoryEntity(
            id = 122L,
            name = "Electricity Bill",
            type = CategoryType.EXPENSE,
            parentId = 3L,
            iconName = "bolt",
            colorHex = "#3B82F6",
            sortOrder = 2
        ),
        CategoryEntity(
            id = 123L,
            name = "Water & Cooking Gas",
            type = CategoryType.EXPENSE,
            parentId = 3L,
            iconName = "water_drop",
            colorHex = "#3B82F6",
            sortOrder = 3
        ),
        CategoryEntity(
            id = 124L,
            name = "Broadband & Mobile Recharge",
            type = CategoryType.EXPENSE,
            parentId = 3L,
            iconName = "wifi",
            colorHex = "#3B82F6",
            sortOrder = 4
        ),

        // 4. Transportation
        CategoryEntity(
            id = 4L,
            name = "Transportation",
            type = CategoryType.EXPENSE,
            parentId = null,
            iconName = "directions_car",
            colorHex = "#06B6D4",
            sortOrder = 4
        ),
        CategoryEntity(
            id = 131L,
            name = "Fuel & EV Charging",
            type = CategoryType.EXPENSE,
            parentId = 4L,
            iconName = "local_gas_station",
            colorHex = "#06B6D4",
            sortOrder = 1
        ),
        CategoryEntity(
            id = 132L,
            name = "Metro, Bus & Train",
            type = CategoryType.EXPENSE,
            parentId = 4L,
            iconName = "train",
            colorHex = "#06B6D4",
            sortOrder = 2
        ),
        CategoryEntity(
            id = 133L,
            name = "Cab, Auto & Rideshare",
            type = CategoryType.EXPENSE,
            parentId = 4L,
            iconName = "local_taxi",
            colorHex = "#06B6D4",
            sortOrder = 3
        ),
        CategoryEntity(
            id = 134L,
            name = "Service, Parking & Tolls",
            type = CategoryType.EXPENSE,
            parentId = 4L,
            iconName = "build",
            colorHex = "#06B6D4",
            sortOrder = 4
        ),

        // 5. Shopping
        CategoryEntity(
            id = 5L,
            name = "Shopping",
            type = CategoryType.EXPENSE,
            parentId = null,
            iconName = "shopping_bag",
            colorHex = "#EC4899",
            sortOrder = 5
        ),
        CategoryEntity(
            id = 141L,
            name = "Clothing & Footwear",
            type = CategoryType.EXPENSE,
            parentId = 5L,
            iconName = "checkroom",
            colorHex = "#EC4899",
            sortOrder = 1
        ),
        CategoryEntity(
            id = 142L,
            name = "Electronics & Gadgets",
            type = CategoryType.EXPENSE,
            parentId = 5L,
            iconName = "devices",
            colorHex = "#EC4899",
            sortOrder = 2
        ),
        CategoryEntity(
            id = 143L,
            name = "Home, Furniture & Appliances",
            type = CategoryType.EXPENSE,
            parentId = 5L,
            iconName = "chair",
            colorHex = "#EC4899",
            sortOrder = 3
        ),
        CategoryEntity(
            id = 144L,
            name = "Personal Care & Grooming",
            type = CategoryType.EXPENSE,
            parentId = 5L,
            iconName = "spa",
            colorHex = "#EC4899",
            sortOrder = 4
        ),

        // 6. Health & Fitness
        CategoryEntity(
            id = 6L,
            name = "Health & Fitness",
            type = CategoryType.EXPENSE,
            parentId = null,
            iconName = "medical_services",
            colorHex = "#EF4444",
            sortOrder = 6
        ),
        CategoryEntity(
            id = 151L,
            name = "Doctor & Pharmacy",
            type = CategoryType.EXPENSE,
            parentId = 6L,
            iconName = "local_pharmacy",
            colorHex = "#EF4444",
            sortOrder = 1
        ),
        CategoryEntity(
            id = 152L,
            name = "Gym, Sports & Supplements",
            type = CategoryType.EXPENSE,
            parentId = 6L,
            iconName = "fitness_center",
            colorHex = "#EF4444",
            sortOrder = 2
        ),
        CategoryEntity(
            id = 153L,
            name = "Diagnostics & Lab Tests",
            type = CategoryType.EXPENSE,
            parentId = 6L,
            iconName = "biotech",
            colorHex = "#EF4444",
            sortOrder = 3
        ),

        // 7. Entertainment & Leisure
        CategoryEntity(
            id = 7L,
            name = "Entertainment & Leisure",
            type = CategoryType.EXPENSE,
            parentId = null,
            iconName = "movie",
            colorHex = "#8B5CF6",
            sortOrder = 7
        ),
        CategoryEntity(
            id = 161L,
            name = "Movies, OTT & Streaming",
            type = CategoryType.EXPENSE,
            parentId = 7L,
            iconName = "live_tv",
            colorHex = "#8B5CF6",
            sortOrder = 1
        ),
        CategoryEntity(
            id = 162L,
            name = "Games, Apps & Subscriptions",
            type = CategoryType.EXPENSE,
            parentId = 7L,
            iconName = "sports_esports",
            colorHex = "#8B5CF6",
            sortOrder = 2
        ),
        CategoryEntity(
            id = 163L,
            name = "Travel, Hotels & Vacations",
            type = CategoryType.EXPENSE,
            parentId = 7L,
            iconName = "flight",
            colorHex = "#8B5CF6",
            sortOrder = 3
        ),

        // 8. Financial & Taxes
        CategoryEntity(
            id = 8L,
            name = "Financial & Taxes",
            type = CategoryType.EXPENSE,
            parentId = null,
            iconName = "account_balance",
            colorHex = "#64748B",
            sortOrder = 8
        ),
        CategoryEntity(
            id = 171L,
            name = "Taxes & Government Fees",
            type = CategoryType.EXPENSE,
            parentId = 8L,
            iconName = "gavel",
            colorHex = "#64748B",
            sortOrder = 1
        ),
        CategoryEntity(
            id = 172L,
            name = "Insurance Premiums",
            type = CategoryType.EXPENSE,
            parentId = 8L,
            iconName = "verified_user",
            colorHex = "#64748B",
            sortOrder = 2
        ),
        CategoryEntity(
            id = 173L,
            name = "Loan EMI & Bank Charges",
            type = CategoryType.EXPENSE,
            parentId = 8L,
            iconName = "receipt_long",
            colorHex = "#64748B",
            sortOrder = 3
        ),

        // 9. Family & Education
        CategoryEntity(
            id = 9L,
            name = "Family & Education",
            type = CategoryType.EXPENSE,
            parentId = null,
            iconName = "school",
            colorHex = "#EAB308",
            sortOrder = 9
        ),
        CategoryEntity(
            id = 181L,
            name = "Childcare & Baby Supplies",
            type = CategoryType.EXPENSE,
            parentId = 9L,
            iconName = "child_care",
            colorHex = "#EAB308",
            sortOrder = 1
        ),
        CategoryEntity(
            id = 182L,
            name = "Books, Courses & Tuition",
            type = CategoryType.EXPENSE,
            parentId = 9L,
            iconName = "menu_book",
            colorHex = "#EAB308",
            sortOrder = 2
        ),
        CategoryEntity(
            id = 183L,
            name = "Gifts, Charity & Festivals",
            type = CategoryType.EXPENSE,
            parentId = 9L,
            iconName = "volunteer_activism",
            colorHex = "#EAB308",
            sortOrder = 3
        ),

        // =====================================================================
        // INCOME MAIN CATEGORIES (IDs 201..204) & SUBCATEGORIES (IDs 211..249)
        // =====================================================================

        // 201. Salary & Wages
        CategoryEntity(
            id = 201L,
            name = "Salary & Wages",
            type = CategoryType.INCOME,
            parentId = null,
            iconName = "payments",
            colorHex = "#10B981",
            sortOrder = 1
        ),
        CategoryEntity(
            id = 211L,
            name = "Monthly Paycheck",
            type = CategoryType.INCOME,
            parentId = 201L,
            iconName = "account_balance_wallet",
            colorHex = "#10B981",
            sortOrder = 1
        ),
        CategoryEntity(
            id = 212L,
            name = "Bonus & Performance Incentives",
            type = CategoryType.INCOME,
            parentId = 201L,
            iconName = "workspace_premium",
            colorHex = "#10B981",
            sortOrder = 2
        ),
        CategoryEntity(
            id = 213L,
            name = "Employer Reimbursements",
            type = CategoryType.INCOME,
            parentId = 201L,
            iconName = "Currency_exchange",
            colorHex = "#10B981",
            sortOrder = 3
        ),

        // 202. Business & Freelance
        CategoryEntity(
            id = 202L,
            name = "Business & Freelance",
            type = CategoryType.INCOME,
            parentId = null,
            iconName = "work",
            colorHex = "#0EA5E9",
            sortOrder = 2
        ),
        CategoryEntity(
            id = 221L,
            name = "Consulting & Client Projects",
            type = CategoryType.INCOME,
            parentId = 202L,
            iconName = "laptop_mac",
            colorHex = "#0EA5E9",
            sortOrder = 1
        ),
        CategoryEntity(
            id = 222L,
            name = "Business Revenue",
            type = CategoryType.INCOME,
            parentId = 202L,
            iconName = "storefront",
            colorHex = "#0EA5E9",
            sortOrder = 2
        ),

        // 203. Investments & Returns
        CategoryEntity(
            id = 203L,
            name = "Investments & Returns",
            type = CategoryType.INCOME,
            parentId = null,
            iconName = "trending_up",
            colorHex = "#22C55E",
            sortOrder = 3
        ),
        CategoryEntity(
            id = 231L,
            name = "Dividends & Mutual Funds",
            type = CategoryType.INCOME,
            parentId = 203L,
            iconName = "monitoring",
            colorHex = "#22C55E",
            sortOrder = 1
        ),
        CategoryEntity(
            id = 232L,
            name = "Savings & FD Interest",
            type = CategoryType.INCOME,
            parentId = 203L,
            iconName = "savings",
            colorHex = "#22C55E",
            sortOrder = 2
        ),
        CategoryEntity(
            id = 233L,
            name = "Rental Income",
            type = CategoryType.INCOME,
            parentId = 203L,
            iconName = "real_estate_agent",
            colorHex = "#22C55E",
            sortOrder = 3
        ),

        // 204. Gifts & Windfalls
        CategoryEntity(
            id = 204L,
            name = "Gifts & Windfalls",
            type = CategoryType.INCOME,
            parentId = null,
            iconName = "redeem",
            colorHex = "#A855F7",
            sortOrder = 4
        ),
        CategoryEntity(
            id = 241L,
            name = "Cashback & Card Rewards",
            type = CategoryType.INCOME,
            parentId = 204L,
            iconName = "stars",
            colorHex = "#A855F7",
            sortOrder = 1
        ),
        CategoryEntity(
            id = 242L,
            name = "Cash Gifts & Tax Refunds",
            type = CategoryType.INCOME,
            parentId = 204L,
            iconName = "card_giftcard",
            colorHex = "#A855F7",
            sortOrder = 2
        )
    )

    suspend fun seedIfEmpty(categoryAndTagDao: CategoryAndTagDao) {
        if (categoryAndTagDao.countCategories() == 0) {
            val allCategories = getDefaultCategories()
            val mainCategories = allCategories.filter { it.parentId == null }
            val subCategories = allCategories.filter { it.parentId != null }
            categoryAndTagDao.insertCategories(mainCategories)
            categoryAndTagDao.insertCategories(subCategories)
        }
    }
}