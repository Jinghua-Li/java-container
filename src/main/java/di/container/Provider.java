package di.container;

public interface Provider<T> {
    T getT(Context context);
}
