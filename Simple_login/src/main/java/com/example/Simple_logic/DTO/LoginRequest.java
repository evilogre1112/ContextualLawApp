package com.example.Simple_logic.DTO;

public class LoginRequest {
        private String email;
        private String password;
        // Get
        public String getEmail() {return email;}
        public String getPassword(){return password;}
        // Set 
        public void setEmail(String email){this.email = email;} 
        public void setPassword(String password){this.password = password;} 
}
