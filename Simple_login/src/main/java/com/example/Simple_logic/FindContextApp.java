package com.example.Simple_logic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//nhãn import
//bắt đầu bằng jakarta.persistence... , giúp java biết class này sẽ ánh xạ xuống cơ sở dữ liệu nào trong csdl
// Các công cụ hay dùng :
// @Entity Đánh dấu class là một thực thể
// @Table: Chỉ định tên bảng thật trong MySQL
// @Id: đánh dấu cột khoá chính
// @Column: chỉ định tên cột
// Cách import tất cả import jakarta.presistence.*;
// bắt đầu bằng org.springframework.data..., cung cấp sẵn các hàm thao tác với Db như tìm kiếm, thêm, sữa, xoá mà k cần tự viết mã SQL
// Công cụ hay dùng JpaRepository
// cách import import org.springframework.data.jpa.repository.JpaRepository;
// bắt đầu bằng org.springframework.web.bind.annotation..., làm nhiệm vụ mở các cổng mạng, đón luồn dữ liệu JSON từ Front-end gữi tới và trả về kết quả
// Các công cụ hay dùng :
// @Restcontroller : chuyên trả về dữ liệu JSON 
// @CrossOrigin : ó cho phép frontend ở một origin khác gọi API của backend,API này cho phép request từ origin khác
// @PostMapping, @GetMapping: Xác định loại hành động (lấy hoặc gữi dữ liệu) 
// @RequestBody: Ra lệnh bóc gói JSON từ front-end gán vào biến java 
// Cách import , import org.springframework.web.bind.annotation.*;
// Ngoại lệ import org.springframework.beans.factory.annotation.Autowired;
// @Autowired dùng để bơm Repository vào Controller 
@SpringBootApplication
public class FindContextApp  {	
	public static void main(String[] args) {
		SpringApplication.run(FindContextApp.class, args);
	}

}
