package models;

public class Decimal extends Token {

    private final double value;

    public Decimal(double value) {
        super(Tag.DECIMAL);
        this.value = value;
    }
    
}
