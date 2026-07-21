package com.quyetbkhoa.healthtracker.data.local.activity

import com.quyetbkhoa.healthtracker.domain.model.OTHER_ACTIVITY_TYPE_ID

object DefaultActivities {
    val values = listOf(
        ActivityTypeEntity(1, "Đi bộ nhẹ", 2.8, "🚶", displayOrder = 1),
        ActivityTypeEntity(2, "Đi bộ nhanh", 4.3, "🚶", displayOrder = 2),
        ActivityTypeEntity(3, "Chạy bộ nhẹ", 7.0, "🏃", displayOrder = 3),
        ActivityTypeEntity(4, "Chạy bộ nhanh", 10.0, "🏃", displayOrder = 4),
        ActivityTypeEntity(5, "Đạp xe nhẹ", 4.0, "🚲", displayOrder = 5),
        ActivityTypeEntity(6, "Đạp xe trung bình", 6.8, "🚴", displayOrder = 6),
        ActivityTypeEntity(7, "Bơi lội", 6.0, "🏊", displayOrder = 7),
        ActivityTypeEntity(8, "Nhảy dây", 10.0, "🪢", displayOrder = 8),
        ActivityTypeEntity(9, "Leo cầu thang", 8.8, "🪜", displayOrder = 9),
        ActivityTypeEntity(10, "Tập gym", 5.0, "🏋️", displayOrder = 10),
        ActivityTypeEntity(11, "Yoga", 2.5, "🧘", displayOrder = 11),
        ActivityTypeEntity(12, "Aerobic", 6.5, "🤸", displayOrder = 12),
        ActivityTypeEntity(13, "Đá bóng", 7.0, "⚽", displayOrder = 13),
        ActivityTypeEntity(14, "Cầu lông", 5.5, "🏸", displayOrder = 14),
        ActivityTypeEntity(15, "Bóng rổ", 6.5, "🏀", displayOrder = 15),
        ActivityTypeEntity(16, "Khiêu vũ", 4.5, "💃", displayOrder = 16),
        ActivityTypeEntity(17, "Đi bộ đường dài", 6.0, "🥾", displayOrder = 17),
        ActivityTypeEntity(18, "Dọn dẹp nhà cửa", 3.3, "🧹", displayOrder = 18),
        ActivityTypeEntity(OTHER_ACTIVITY_TYPE_ID, "Khác", 4.0, "✨", displayOrder = 19)
    )
}
