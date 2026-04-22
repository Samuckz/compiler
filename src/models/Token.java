package models;

public class Token {
    private int tag;

    public Token(int tag) {
        this.tag = tag;
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
