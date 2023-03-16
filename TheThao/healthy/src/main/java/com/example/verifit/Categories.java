package com.example.verifit;

public class Categories {
    private String matheloai;
    private String tentheloai;
    private String image;

    public Categories() {
    }

    public Categories(String matheloai, String tentheloai, String image) {
        this.matheloai = matheloai;
        this.tentheloai = tentheloai;
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMatheloai() {
        return matheloai;
    }

    public void setMatheloai(String matheloai) {
        this.matheloai = matheloai;
    }

    public String getTentheloai() {
        return tentheloai;
    }

    public void setTentheloai(String tentheloai) {
        this.tentheloai = tentheloai;
    }
}
