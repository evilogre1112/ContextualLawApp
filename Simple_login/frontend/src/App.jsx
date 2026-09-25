import React from 'react'

const App = () => {
  return (
    <div style = {{padding: '50px'}} >
      <h2> đăng nhập hệ thống</h2>
      <input type ="email" placeholder='Nhập email..'/>
      <br/><br/>
      <input type ="password" placeholder="Nhập mật khẩu.."/>
      <br/> <br/> 
      <button>Đăng nhập</button>
      <br/> <br/> 
    </div>
  )
}

export default App