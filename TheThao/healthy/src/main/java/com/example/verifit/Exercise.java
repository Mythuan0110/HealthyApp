package com.example.verifit;

public class Exercise {

    private String Name;
    private String BodyPart;


    public Exercise(String name, String bodypart)
    {
        this.Name = name;
        this.BodyPart = bodypart;
    }

    public String getBodyPart() {
        return BodyPart;
    }

    public void setBodyPart(String bodyPart) {
        BodyPart = bodyPart;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

}
