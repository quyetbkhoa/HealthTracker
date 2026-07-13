package com.quyetbkhoa.healthtracker.data.local.food

data class DefaultFood(
    val entity: FoodEntity,
    val vietnameseName: String,
    val englishName: String
)

object DefaultFoods {
    val values = listOf(
        food(1, 130.0, 150.0, "Cơm trắng", "White rice"),
        food(2, 111.0, 400.0, "Phở bò", "Beef pho"),
        food(3, 265.0, 80.0, "Bánh mì", "Vietnamese baguette"),
        food(4, 138.0, 300.0, "Bún bò Huế", "Hue beef noodle soup"),
        food(5, 109.0, 350.0, "Bún riêu", "Crab noodle soup"),
        food(6, 148.0, 300.0, "Cơm tấm", "Broken rice plate"),
        food(7, 165.0, 100.0, "Ức gà nướng", "Grilled chicken breast"),
        food(8, 239.0, 100.0, "Thịt heo nạc", "Lean pork"),
        food(9, 250.0, 100.0, "Thịt bò xào", "Stir-fried beef"),
        food(10, 208.0, 100.0, "Cá hồi", "Salmon"),
        food(11, 128.0, 100.0, "Cá basa", "Basa fish"),
        food(12, 99.0, 100.0, "Tôm luộc", "Boiled shrimp"),
        food(13, 155.0, 50.0, "Trứng gà luộc", "Boiled egg"),
        food(14, 61.0, 100.0, "Sữa chua không đường", "Plain yogurt"),
        food(15, 42.0, 250.0, "Sữa tươi không đường", "Unsweetened milk"),
        food(16, 76.0, 100.0, "Đậu hũ", "Tofu"),
        food(17, 35.0, 150.0, "Salad rau", "Vegetable salad"),
        food(18, 30.0, 100.0, "Rau luộc", "Boiled vegetables"),
        food(19, 23.0, 100.0, "Rau muống", "Water spinach"),
        food(20, 34.0, 100.0, "Bông cải xanh", "Broccoli"),
        food(21, 89.0, 120.0, "Chuối", "Banana"),
        food(22, 52.0, 150.0, "Táo", "Apple"),
        food(23, 47.0, 180.0, "Cam", "Orange"),
        food(24, 60.0, 200.0, "Xoài", "Mango"),
        food(25, 30.0, 250.0, "Dưa hấu", "Watermelon"),
        food(26, 160.0, 100.0, "Bơ", "Avocado"),
        food(27, 389.0, 50.0, "Yến mạch", "Oats"),
        food(28, 86.0, 200.0, "Khoai lang", "Sweet potato"),
        food(29, 77.0, 150.0, "Khoai tây luộc", "Boiled potato"),
        food(30, 567.0, 30.0, "Hạnh nhân", "Almonds"),
        food(31, 585.0, 30.0, "Đậu phộng", "Peanuts"),
        food(32, 536.0, 25.0, "Sô-cô-la đen", "Dark chocolate"),
        food(33, 49.0, 300.0, "Canh rau", "Vegetable soup"),
        food(34, 180.0, 200.0, "Cháo thịt", "Pork congee"),
        food(35, 190.0, 200.0, "Miến gà", "Chicken glass noodle soup"),
        food(36, 288.0, 100.0, "Chả giò", "Fried spring rolls")
    )

    val foods = values.map(DefaultFood::entity)

    val translations = values.flatMap { food ->
        listOf(
            translation(food, "vi", food.vietnameseName),
            translation(food, "en", food.englishName)
        )
    }

    private fun food(
        id: Long,
        caloriesPer100Grams: Double,
        defaultServingGrams: Double,
        vietnameseName: String,
        englishName: String
    ) = DefaultFood(
        entity = FoodEntity(
            id = id,
            caloriesPer100Grams = caloriesPer100Grams,
            defaultServingGrams = defaultServingGrams,
            displayOrder = id.toInt()
        ),
        vietnameseName = vietnameseName,
        englishName = englishName
    )

    private fun translation(
        food: DefaultFood,
        languageTag: String,
        name: String
    ) = FoodTranslationEntity(
        foodId = food.entity.id,
        languageTag = languageTag,
        name = name,
        normalizedName = FoodNameNormalizer.normalize(name)
    )
}
