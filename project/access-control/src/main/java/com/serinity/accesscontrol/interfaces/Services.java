package com.serinity.accesscontrol.interfaces;


import java.util.List;

public interface Services<T> {

    void add(T t);
    List<T> getAll();
    void update(T t);
    void delete(T t);

}
