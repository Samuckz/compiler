package models;

public class Decimal extends Token {

    private final double value;

    public Decimal(double value) {
        super(Tag.REAL);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Decimal{value=" + value + '}';
    }
}
