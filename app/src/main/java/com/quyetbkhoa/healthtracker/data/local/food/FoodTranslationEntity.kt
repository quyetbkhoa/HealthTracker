package com.quyetbkhoa.healthtracker.data.local.food

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "food_translations",
    primaryKeys = ["foodId", "languageTag"],
    foreignKeys = [
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("foodId"),
        Index(value = ["languageTag", "normalizedName"])
    ]
)
data class FoodTranslationEntity(
    val foodId: Long,
    val languageTag: String,
    val name: String,
    val normalizedName: String
)
