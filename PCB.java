package com.johnnyvalles.project1;
import java.util.LinkedList;
import java.util.ListIterator;

enum ProcessState { READY, RUNNING, BLOCKED }

public class PCB {
    public ProcessState state;
    public int parent;
    public int priority;
    public LinkedList<Integer> children;
    public LinkedList<Pair> resources;

    public PCB(ProcessState state, int parent, int priority) {
        this.state = state;
        this.parent = parent;
        this.priority = priority;
        this.children = new LinkedList<Integer>();
        this.resources = new LinkedList<Pair>();
    }

    public void removeChild(int id) {
        if (children.contains(id)) {
            children.remove(id);
            System.out.println("Child process " + id + " removed.");
        } else {
            System.out.println("No such child process exists.");
        }
    }

    public boolean canReleaseResource(int r) {
        ListIterator<Pair> iter = resources.listIterator();

        while (iter.hasNext()) {
            if (iter.next().first == r) {
                return true;
            }
        }

        return false;
    }

    public Pair getResourcePair(int r) {
        ListIterator<Pair> iterator = resources.listIterator();
       
        while (iterator.hasNext()) {
            Pair pair = iterator.next();

            if (pair.first == r) {
                // return resource pair reference
                return pair;
            }
        }

        return null;
    }
}