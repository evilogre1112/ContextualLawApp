package com.example.Simple_logic.DTO;

import java.util.List;

public record NERResponse( 
    String model,
    List<NEREntityResponse> entities){}
