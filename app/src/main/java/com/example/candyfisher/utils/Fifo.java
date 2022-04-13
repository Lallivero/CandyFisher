package com.example.candyfisher.utils;

import java.util.LinkedList;


public class Fifo extends LinkedList<Tilt> {

    public void push(Tilt t){
        addLast(t);
        if(size() > 2) {
            removeFirst();
        }
    }
}
