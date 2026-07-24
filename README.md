# HealthTracker

HealthTracker là ứng dụng Android giúp theo dõi lượng calorie nạp vào, hoạt động thể chất và tiến độ mục tiêu sức khỏe hằng ngày. Ứng dụng hoạt động với dữ liệu lưu cục bộ, hỗ trợ tiếng Việt và tiếng Anh.

## Tính năng chính

- Thiết lập hồ sơ cá nhân và mục tiêu tăng, giảm hoặc duy trì cân nặng.
- Tính BMR, TDEE và mục tiêu calorie hằng ngày.
- Ghi lại bữa ăn theo buổi và tra cứu danh sách thực phẩm có sẵn.
- Ghi lại hoạt động thể chất và lượng calorie tiêu thụ.
- Theo dõi calorie đã nạp, đã đốt và còn lại trên dashboard.
- Xem nhật ký ăn uống, lịch sử vận động và biểu đồ thống kê.
- Nhắc giờ ăn và vận động bằng thông báo.
- Thêm nhanh bữa ăn hoặc hoạt động từ widget màn hình chính.
- Tùy chỉnh giao diện sáng, tối, hồng hoặc theo hệ thống.
- Tùy chỉnh cỡ chữ; hỗ trợ tiếng Việt và tiếng Anh.

## Công nghệ sử dụng

- [Kotlin](https://kotlinlang.org/) và Kotlin Coroutines/Flow
- [Jetpack Compose](https://developer.android.com/compose) với Material 3
- Navigation Compose
- Room Database
- Preferences DataStore
- Hilt và KSP
- Glance App Widget

Project được tổ chức theo kiến trúc phân lớp:

```text
app/src/main/java/com/quyetbkhoa/healthtracker/
├── core/           # Navigation, design system và thành phần UI dùng chung
├── data/           # Room, DataStore, dữ liệu mẫu và repository implementation
├── di/             # Dependency injection modules
├── domain/         # Model, repository interface và use case
├── platform/       # Thông báo và lịch nhắc
├── presentation/   # Screen và ViewModel theo từng tính năng
└── widget/         # Widget thêm nhanh trên màn hình chính
```

## Yêu cầu môi trường

- Android Studio có hỗ trợ Android Gradle Plugin 9.1.1
- JDK 17 trở lên
- Android SDK API 37
- Thiết bị hoặc máy ảo chạy Android 7.0 (API 24) trở lên

## Cài đặt và chạy

Clone repository và mở thư mục project bằng Android Studio:

```bash
git clone <repository-url>
cd HealthTracker
```

Sau khi Gradle Sync hoàn tất, chọn thiết bị và chạy cấu hình `app`.

Bạn cũng có thể build APK debug từ terminal:

```powershell
.\gradlew.bat assembleDebug
```

APK được tạo tại:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Project không yêu cầu API key hoặc dịch vụ backend. Dữ liệu người dùng được lưu trên thiết bị bằng Room và DataStore.

## Quyền ứng dụng

HealthTracker khai báo các quyền sau:

- `POST_NOTIFICATIONS`: hiển thị thông báo nhắc ăn uống và vận động trên Android 13 trở lên.
- `SCHEDULE_EXACT_ALARM`: lên lịch nhắc đúng thời điểm đã chọn.
- `RECEIVE_BOOT_COMPLETED`: khôi phục lịch nhắc sau khi thiết bị khởi động lại.

Người dùng có thể bật, tắt và thay đổi thời gian nhắc trong phần cài đặt của ứng dụng.

