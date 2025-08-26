package pmcp.value.values;

import pmcp.mode.Mode;
import pmcp.value.Value;

import java.util.function.Supplier;

public class StringValue<V extends String> extends Value<V> {

    public StringValue(Mode mode, String name, V string) {
        super(name, string, () -> true, () -> true, () -> true);
        mode.addValues(this);
    }

    public StringValue(Mode mode, String name, V string, Supplier<Boolean> visitable) {
        super(name, string, visitable, () -> true, () -> true);
        mode.addValues(this);
    }

    public StringValue(Mode mode, String name, V string, Supplier<Boolean> visitable, Supplier<Boolean> visitable2) {
        super(name, string, visitable, visitable2, () -> true);
        mode.addValues(this);
    }

    public StringValue(Mode mode, String name, V string, Supplier<Boolean> visitable, Supplier<Boolean> visitable2, Supplier<Boolean> visitable3) {
        super(name, string, visitable, visitable2, visitable3);
        mode.addValues(this);
    }

}
