package com.example.app_yolo11.model

data class SeaSnails(
    val id : String = "",
    val name: String = "", // tên
    val scientificName: String = "",//tên khoa học
    val family: String = "", // họ
    val description: String ="",// mô tả nhận dạng
    val habitat: String = "", // môi trường sống
    val behavior: String = "", // tập tính
    val distribution: String ="", // phân bố
    val conservationStatus:String="", // tình trạng bảo tồn
    val imageUrl: String = "",// ảnh
    val value: String ="" // giá trị kinh tế
)