import React, { useState } from 'react';
import axios from 'axios';

const App = () => {
  // 1. Tạo hộp lưu trữ dữ liệu (State) cho 2 ô nhập liệu
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  // 2. Viết hàm gửi dữ liệu xuống Spring Boot
  const handleLogin = async () => {
    try {
      // Gọi API POST tới cổng 8080 của Spring Boot
      const response = await axios.post('http://localhost:8080/api/getLogin', {
        email: email,
        password: password
      });
      alert("Đăng nhập thành công!");
      console.log(response.data); // Xem kết quả trả về trong F12 (Console)
    } catch (error) {
      alert("Đăng nhập thất bại: Sai email hoặc mật khẩu.");
      console.error(error);
    }
  };

  return (
    <div style={{ padding: '50px' }}>
      <h2>Đăng nhập hệ thống</h2>
      <input 
        type="email" 
        placeholder="Nhập email..." 
        value={email}
        onChange={(e) => setEmail(e.target.value)} 
      />
      <br /><br />
      <input 
        type="password" 
        placeholder="Nhập mật khẩu..." 
        value={password}
        onChange={(e) => setPassword(e.target.value)} 
      />
      <br /><br />
      <button onClick={handleLogin}>Đăng nhập</button>
    </div>
  );
};

export default App;