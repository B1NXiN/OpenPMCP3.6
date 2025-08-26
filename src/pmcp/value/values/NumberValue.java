package pmcp.value.values;

import java.util.function.Supplier;

import pmcp.mode.Mode;
import pmcp.utils.category.NumberCategory;
import pmcp.value.Value;


public class NumberValue<T extends Number> extends Value<T> {
    public T minimum;
    public T maximum;
    public T increment;
    public String catString;
    public NumberCategory category;

    public NumberValue(Mode mode, String name, T value, T min, T max, T inc) {
        super(name, value, () -> true, () -> true, () -> true);
        this.minimum = min;
        this.maximum = max;
        this.increment = inc;
        this.category = NumberCategory.NONE;
        this.catString = "";
        mode.addValues(this);
    }

    public NumberValue(Mode mode, String name, T value, T min, T max, T inc, NumberCategory category, String catString) {
        super(name, value, () -> true, () -> true, () -> true);
        this.minimum = min;
        this.maximum = max;
        this.increment = inc;
        this.category = category;
        this.catString = catString;
        mode.addValues(this);
    }

    public NumberValue(Mode mode, String name, T value, T min, T max, T inc, Supplier<Boolean> visitable) {
        super(name, value, visitable, () -> true, () -> true);
        this.minimum = min;
        this.maximum = max;
        this.increment = inc;
        this.category = NumberCategory.NONE;
        this.catString = "";
        mode.addValues(this);
    }

    public NumberValue(Mode mode, String name, T value, T min, T max, T inc, NumberCategory category, String catString, Supplier<Boolean> visitable) {
        super(name, value, visitable, () -> true, () -> true);
        this.minimum = min;
        this.maximum = max;
        this.increment = inc;
        this.category = category;
        this.catString = catString;
        mode.addValues(this);
    }

    public Number getMinimum() {
        return minimum;
    }

    public Number getMaximum() {
        return maximum;
    }

    public Number getIncrement() {
        return increment;
    }

}

