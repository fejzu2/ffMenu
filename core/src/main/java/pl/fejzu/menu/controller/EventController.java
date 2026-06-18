package pl.fejzu.menu.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Consumer;

@Getter
@AllArgsConstructor
public class EventController<T>
{

    private Class<T> type;
    private Consumer<T> consumer;

    public void accept(T t) {
        this.consumer.accept(t);
    }
}