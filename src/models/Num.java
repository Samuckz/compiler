package models;

public class Num extends Token{
    private Integer value;

    public Num(Integer value) {
        super(Tag.NUM);
        this.value = value;
    }

    @Override
    public String toString() {
        return "Num{" +
                "value=" + value +
                '}';
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
