package com.example.Simple_logic.DTO;

public class ThanhPhanRespone {
    private String loai_thanh_phan;
    private int stt;
    private String noi_dung;
    
    public ThanhPhanRespone() {
    }
    public String getLoai_thanh_phan() {
        return loai_thanh_phan;
    }
    public void setLoai_thanh_phan(String loai_thanh_phan) {
        this.loai_thanh_phan = loai_thanh_phan;
    }
    public int getStt() {
        return stt;
    }
    public void setStt(int stt) {
        this.stt = stt;
    }
    public String getNoi_dung() {
        return noi_dung;
    }
    public void setNoi_dung(String noi_dung) {
        this.noi_dung = noi_dung;
    }
    
}
