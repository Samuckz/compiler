package models;

import config.Environment;

public class Token {
    private Integer tag;

    public Token(Integer tag) {
        this.tag = tag;
    }

    public Token(char tokenName) {
        this.tag = Environment.getCurrentId();
        Environment.incrementCurrentId();
        System.out.println("New token: " + tokenName + "\ntag: " + this.tag);
    }

    @Override
    public String toString(){
        return "Token: " + this.tag;
    }

    public Integer getTag() {
        return tag;
    }

    public void setTag(Integer tag) {
        this.tag = tag;
    }
}
