package server.core.di;

public interface IInjector {
    <T> T getInstance(Class<T> clz);
}
