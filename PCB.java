package com.johnnyvalles.project1;
import java.util.LinkedList;
import java.util.ListIterator;

enum ProcessState { READY, RUNNING, BLOCKED }

public class PCB {
    public ProcessState state;
    public int parent;
    public LinkedList<Integer> children;
    public LinkedList<Integer> resources;

    public PCB(ProcessState state, int parent) {
        this.state = state;
        this.parent = parent;
        this.children = new LinkedList<Integer>();
        this.resources = new LinkedList<Integer>();
    }

    @Override
    public String toString() {
        String state = "STATE: " + this.state;
        String parent = "PARENT: " + this.parent;
        String childrenStr = "CHILDREN: ";
        String resourcesStr = "RESOURCES: ";
        ListIterator<Integer> iter = this.children.listIterator();

        while (iter.hasNext()) {
            childrenStr += iter.next() + " ";
        }

        iter = this.resources.listIterator();
        while (iter.hasNext()) {
            resourcesStr += iter.next() + " ";
        }

        return state + "\n" + parent + "\n" + childrenStr + "\n" + resourcesStr + "\n";
    }

    public void addChild(int id) {
        this.children.add(id);
    }

    public void removeChild(int id) {
        int index = this.children.indexOf(id);

        if (index < 0) {
            System.out.println("No such child process exists.");
        } else {
            this.children.remove(index);
            System.out.println("Child process " + id + " removed.");
        }
    }

    public void addResource(int id) {
        this.children.add(id);
    }

    public void removeResource(int id) {
        int index = this.resources.indexOf(id);

        if (index < 0) {
            System.out.println("No such resource held by process.");
        } else {
            this.resources.remove(index);
            System.out.println("Resource " + id + " removed.");
        }
    }
}