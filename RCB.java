package com.johnnyvalles.project1;
import java.util.LinkedList;

enum ResourceState { FREE, ALLOCATED }

public class RCB {
    public ResourceState state;
    public LinkedList<Integer> waitlist;

    public RCB() {
        state = ResourceState.FREE;
        waitlist = new LinkedList<Integer>();
    }
}
