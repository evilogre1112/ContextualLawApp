# 📖 Hướng Dẫn Sử Dụng Spring Data Neo4j Repository (`Neo4jRepository`)

> Tài liệu tổng hợp các phương thức có sẵn trong `Neo4jRepository<T, ID>` mà Spring Data cung cấp tự động, giúp thao tác với cơ sở dữ liệu đồ thị Neo4j mà **không cần viết thủ công câu lệnh Cypher**.

---

## 📁 Danh Sách Các Repository Trong Dự Án

| Repository | Thực Thể (Node Label) | Mục Đích Sử Dụng |
| :--- | :--- | :--- |
| [`VanBanRepo`](file:///d:/ContextualLawApp/Simple_login/src/main/java/com/example/Simple_logic/DAO/VanBanRepo.java) | `VanBan` (`:Văn bản`) | Quản lý văn bản pháp luật gốc, metadata |
| [`ThanhPhanRepo`](file:///d:/ContextualLawApp/Simple_login/src/main/java/com/example/Simple_logic/DAO/ThanhPhanRepo.java) | `ThanhPhan` (`:Thành phần`) | Quản lý chung các thành phần, Vector Search |
| [`ChuongRepo`](file:///d:/ContextualLawApp/Simple_login/src/main/java/com/example/Simple_logic/DAO/ChuongRepo.java) | `Chuong` (`:Chương`) | Truy vấn các Chương theo Văn bản |
| [`MucRepo`](file:///d:/ContextualLawApp/Simple_login/src/main/java/com/example/Simple_logic/DAO/MucRepo.java) | `Muc` (`:Mục`) | Truy vấn các Mục theo Chương / Văn bản |
| [`DieuRepo`](file:///d:/ContextualLawApp/Simple_login/src/main/java/com/example/Simple_logic/DAO/DieuRepo.java) | `Dieu` (`:Điều`) | Truy vấn các Điều theo Chương / Mục / Văn bản |
| [`KhoanRepo`](file:///d:/ContextualLawApp/Simple_login/src/main/java/com/example/Simple_logic/DAO/KhoanRepo.java) | `Khoan` (`:Khoản`) | Truy vấn các Khoản theo Điều / Văn bản |
| [`DiemRepo`](file:///d:/ContextualLawApp/Simple_login/src/main/java/com/example/Simple_logic/DAO/DiemRepo.java) | `Diem` (`:Điểm`) | Truy vấn các Điểm theo Khoản / Văn bản |
| [`NerRepo`](file:///d:/ContextualLawApp/Simple_login/src/main/java/com/example/Simple_logic/DAO/NerRepo.java) | `NER` (`:NER`) | Quản lý thực thể NER đã trích xuất |

---

## 📑 Bảng Tra Cứu Nhanh Các Hàm Có Sẵn

| Nhóm | Phương Thức | Kiểu Trả Về | Chức Năng |
| :--- | :--- | :--- | :--- |
| **Thêm / Sửa** | `save(S entity)` | `S` | Thêm mới hoặc cập nhật 1 node |
| | `saveAll(Iterable<S> entities)` | `List<S>` | Thêm mới hoặc cập nhật hàng loạt node |
| **Tìm Kiếm** | `findById(ID id)` | `Optional<T>` | Tìm 1 node theo ID khóa chính |
| | `findAll()` | `List<T>` | Lấy toàn bộ danh sách node |
| | `findAllById(Iterable<ID> ids)` | `List<T>` | Lấy danh sách node theo tập hợp nhiều ID |
| **Kiểm Tra & Đếm**| `existsById(ID id)` | `boolean` | Kiểm tra node có tồn tại theo ID hay không |
| | `count()` | `long` | Đếm tổng số lượng node trong nhãn (Label) |
| **Phân Trang / Sắp Xếp** | `findAll(Sort sort)` | `List<T>` | Lấy danh sách có sắp xếp theo thuộc tính |
| | `findAll(Pageable pageable)` | `Page<T>` | Lấy danh sách có phân trang và sắp xếp |
| **Xóa** | `deleteById(ID id)` | `void` | Xóa 1 node theo ID |
| | `delete(T entity)` | `void` | Xóa node theo đối tượng truyền vào |
| | `deleteAllById(Iterable<ID> ids)` | `void` | Xóa nhiều node theo danh sách ID |
| | `deleteAll(Iterable<T> entities)`| `void` | Xóa nhiều node theo danh sách đối tượng |
| | `deleteAll()` | `void` | Xóa toàn bộ node thuộc nhãn đó |

---

## 🛠️ Chi Tiết Cách Sử Dụng Từng Nhóm Hàm

*(Các ví dụ dưới đây minh họa trực tiếp với `VanBanRepo`, `VanBan` và `ThanhPhan` trong dự án)*

### 1. Thêm mới và Cập nhật (Save / Update)

Spring Data Neo4j tự động phân biệt Thêm mới hoặc Cập nhật dựa trên `@Id`:
- Nếu ID chưa tồn tại trong Neo4j: Tạo node mới (`CREATE`).
- Nếu ID đã có trong Neo4j: Cập nhật các thuộc tính mới (`MERGE` / `SET`).

```java
// 1. Lưu 1 văn bản đơn lẻ
VanBan vb = new VanBan();
vb.setId("VB_01");
vb.setTen("Luật Hôn nhân và Gia đình 2014");
vb.setSoHieu("52/2014/QH13");
vanBanRepo.save(vb);

// 2. Lưu danh sách nhiều thành phần cùng lúc (Batching tối ưu)
List<ThanhPhan> list = List.of(dieu1, dieu2, dieu3);
thanhPhanRepo.saveAll(list);
```

---

### 2. Tìm kiếm và Đọc dữ liệu (Read / Query)

```java
// 1. Tìm theo ID (Trả về Optional để tránh lỗi NullPointerException)
Optional<VanBan> opt = vanBanRepo.findById("VB_01");
if (opt.isPresent()) {
    VanBan vb = opt.get();
    System.out.println("Tên luật: " + vb.getTen());
}

// Cách viết ngắn gọn hơn với lambda:
VanBan vb = vanBanRepo.findById("VB_01")
        .orElseThrow(() -> new RuntimeException("Không tìm thấy văn bản"));

// 2. Lấy tất cả văn bản
List<VanBan> tatCa = vanBanRepo.findAll();

// 3. Lấy nhiều văn bản theo danh sách ID
List<String> listId = List.of("VB_01", "VB_02", "VB_03");
List<VanBan> ketQua = vanBanRepo.findAllById(listId);
```

---

### 3. Kiểm tra tồn tại và Đếm số lượng (Check & Count)

Rất hữu ích khi cần kiểm tra trước khi tạo mới để tránh trùng lặp:

```java
// 1. Kiểm tra văn bản đã tồn tại chưa (Rất nhanh, không cần load cả entity lên bộ nhớ)
boolean daCo = vanBanRepo.existsById("VB_01");
if (!daCo) {
    vanBanRepo.save(vbMoi);
}

// 2. Đếm tổng số lượng văn bản hiện có trong CSDL
long tongSoVanBan = vanBanRepo.count();
System.out.println("Tổng số văn bản hiện có: " + tongSoVanBan);
```

---

### 4. Phân trang và Sắp xếp (Paging & Sorting)

Khi số lượng văn bản lớn (hàng nghìn node), **không nên dùng `findAll()`** vì dễ tràn RAM. Hãy dùng phân trang:

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

// 1. Sắp xếp: Lấy toàn bộ văn bản sắp xếp theo ngày ban hành giảm dần
List<VanBan> dsSapXep = vanBanRepo.findAll(
    Sort.by(Sort.Direction.DESC, "ngayBanHanh")
);

// 2. Phân trang: Lấy trang số 0, mỗi trang 10 văn bản, sắp xếp theo tên A-Z
Pageable pageable = PageRequest.of(0, 10, Sort.by("ten").ascending());
Page<VanBan> trang1 = vanBanRepo.findAll(pageable);

System.out.println("Tổng số trang: " + trang1.getTotalPages());
System.out.println("Tổng số bản ghi: " + trang1.getTotalElements());
List<VanBan> danhSachTrang1 = trang1.getContent();
```

---

### 5. Xóa dữ liệu (Delete)

> ⚠️ **Lưu ý trong Neo4j:** Nếu một Node đang có quan hệ (Relationship) nối với Node khác, lệnh xóa mặc định có thể cần xử lý quan hệ trước hoặc cấu hình Cascade Delete.

```java
// 1. Xóa theo ID
vanBanRepo.deleteById("VB_01");

// 2. Xóa theo Entity đối tượng
vanBanRepo.delete(vb);

// 3. Xóa nhiều theo danh sách ID
vanBanRepo.deleteAllById(List.of("VB_01", "VB_02"));

// 4. Xóa toàn bộ văn bản (Cẩn trọng khi dùng!)
vanBanRepo.deleteAll();
```

---

## 🚀 Tính Năng Mở Rộng: Tự Động Tạo Hàm Theo Tên (Derived Query Methods)

Ngoài các hàm có sẵn ở trên, ta chỉ cần **khai báo tên hàm** trong Repository, Spring Data Neo4j sẽ **tự động dịch thành câu truy vấn Cypher** tương ứng mà không cần viết `@Query`:

```java
public interface VanBanRepo extends Neo4jRepository<VanBan, String> {

    // 1. Tìm chính xác theo số hiệu: WHERE n.soHieu = $soHieu
    Optional<VanBan> findBySoHieu(String soHieu);

    // 2. Tìm chứa từ khóa (không phân biệt hoa thường): WHERE toLower(n.ten) CONTAINS toLower($keyword)
    List<VanBan> findByTenContainingIgnoreCase(String keyword);

    // 3. Tìm theo loại văn bản và cơ quan ban hành: WHERE n.loai = $loai AND n.coQuan = $coQuan
    List<VanBan> findByLoaiVanBanAndCoQuanBanHanh(String loai, String coQuan);

    // 4. Đếm số lượng theo ngành:
    long countByNganh(String nganh);

    // 5. Kiểm tra tồn tại theo số hiệu:
    boolean existsBySoHieu(String soHieu);
}
```

---

## 📌 Khi Nào Dùng Hàm Có Sẵn vs Khi Nào Dùng `@Query`?

| Trường Hợp | Lựa Chọn Phù Hợp |
| :--- | :--- |
| CRUD cơ bản: Thêm, sửa, xóa, tìm theo ID, phân trang | 👉 Dùng **hàm có sẵn** (`save`, `findById`, `findAll`...) |
| Tìm kiếm theo 1-2 thuộc tính đơn giản (`soHieu`, `ten`...) | 👉 Dùng **Derived Query** (`findBySoHieu`...) |
| Vector Search (Duyệt `db.index.vector.queryNodes`) | 👉 Bắt buộc dùng **`@Query`** Cypher |
| Đi sâu vào đồ thị đa tầng (Ví dụ: `Khoan` $\rightarrow$ `Điều` $\rightarrow$ `Chương`) | 👉 Bắt buộc dùng **`@Query`** Cypher để tối ưu hiệu năng |
