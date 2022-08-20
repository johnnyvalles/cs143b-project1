package com.johnnyvalles.project1;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Scanner;

public class Manager {
    private final int MAX_PCB = 4;
    private final int MAX_RCB = 2;
    
    private PCB[] pcb = null;
    private RCB[] rcb = null;
    private LinkedList<Integer> readyList = null;
    private int numDestroyed = 0;

    public Manager() {
        init();
    }

    public void create() {
        // search for an free entry to use
        int index = -1;
        for (int i = 0; i < pcb.length; ++i) {
            if (pcb[i] == null) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            System.out.println("Cannot create process, PCB array is full.");
        } else {
            int parentIndex = readyList.get(0);
            pcb[index] = new PCB(ProcessState.READY, parentIndex);
            pcb[parentIndex].children.add(index);
            readyList.add(index);
            System.out.println("* process " + index + " created");      
        }
    }

    public boolean canDestroy(int index) {
        int currentIndex = readyList.get(0);

        if (currentIndex == index) {
            return true;
        }

        if (pcb[index] == null) {
            System.out.println("Cannot destroy non-existent process.");
            return false;
        } else if (pcb[index].parent != currentIndex) {
            System.out.println("Cannot destroy process that is not child of current running process.");
            return false;
        } else {
            return true;
        }
    }

    public void destroy(int j) {
        // create copy of j's children, source modified recursively
        LinkedList<Integer> children = new LinkedList();
        for (int k : pcb[j].children) {
             children.add(k);
        }
        for (int k : children) {
            destroy(k);
        }

        // remove j from parent's list of children
        pcb[pcb[j].parent].children.removeFirstOccurrence(j);

        // check if process is on RL or is on a resource waitlist
        if (pcb[j].state != ProcessState.BLOCKED) {
            // j is in the ready list, need to remove it
            readyList.removeFirstOccurrence((j));
            pcb[readyList.get(0)].state = ProcessState.RUNNING;
        } else {
            // j is not in the ready list
            // j is blocked
            // j is on the waitlist of some resource
            for (int i = 0; i < rcb.length; ++i) {
                rcb[i].waitlist.removeFirstOccurrence(j);
            }
        }

        // release all resources held by j
        for (int r : pcb[j].resources) {
            // check if r has a waitlist
            // cannot FREE the resourse, need to give to next in line
            if (!rcb[r].waitlist.isEmpty()) {
                // give resource to head of waitlist
                int waitlistHead = rcb[r].waitlist.removeFirst();
                pcb[waitlistHead].resources.add(r);
                pcb[waitlistHead].state = ProcessState.READY;
                readyList.add(waitlistHead);
            } else {
                // r has no waitlist, can be freed
                rcb[r].state = ResourceState.FREE;
            }
        }

        pcb[j] = null;
        numDestroyed++;
    }
    
    public void request(int r) {
        // check if valid request
        int currentProcIndex = readyList.get(0);
        if (currentProcIndex == 0) {
            System.err.println("Error: process 0 cannot request resources.");
        } else if (r < 0 && r > MAX_RCB - 1) {
            System.err.println("Error: resource requested does not exist.");
        } else if (pcb[currentProcIndex].resources.contains(r)) {
            System.err.println("Error: resource already held by this process.");
        } else {
            // proceed with resource acquisition
            if (rcb[r].state == ResourceState.FREE) {
                rcb[r].state = ResourceState.ALLOCATED;
                pcb[currentProcIndex].resources.add(r);
                System.out.println("resource " + r + " allocated");
            } else {
                pcb[currentProcIndex].state = ProcessState.BLOCKED;
                readyList.removeFirstOccurrence(currentProcIndex);
                rcb[r].waitlist.add(currentProcIndex);
                scheduler();
            }
        }
    }

    public void release(int r) {
        // check if valid request
        int currentProcIndex = readyList.get(0);
        if (currentProcIndex == 0) {
            System.err.println("Error: process 0 cannot release resources.");
        } else if (r < 0 && r > MAX_RCB - 1) {
            System.err.println("Error: resource to release does not exist");
        } else if (!pcb[currentProcIndex].resources.contains(r)) {
            System.err.println("Error: resource not held by this process.");
        } else {
            // proceed with resource release
            
            // remove r from resource list of process i
            pcb[currentProcIndex].resources.removeFirstOccurrence(r);

            if (rcb[r].waitlist.isEmpty()) {
                rcb[r].state = ResourceState.FREE;
            } else {
                int j = rcb[r].waitlist.removeFirst();
                readyList.add(j);
                pcb[j].state = ProcessState.READY;
                pcb[j].resources.add(r);                
            }

            System.out.println("resource " + r + " released");

        }
    }

    public void timeout() {
        pcb[readyList.get(0)].state = ProcessState.READY;
        readyList.add(readyList.remove());
        scheduler();
    }

    public void scheduler() {
        if (pcb[readyList.get(0)].state != ProcessState.RUNNING) {
            pcb[readyList.get(0)].state = ProcessState.RUNNING;
        }
        System.out.println("process " + readyList.get(0) + " running");
    }

    public void init() {
        // create PCB array, all entries initially null
        pcb = new PCB[MAX_PCB];
        // create RCB array, all entries initially null
        rcb = new RCB[MAX_RCB];
        // create empty ready list, no processes yet
        readyList = new LinkedList<Integer>();
        
        // initialize all RCB entries
        for (int i = 0; i < MAX_RCB; ++i) {
            rcb[i] = new RCB();
        }

        // create init process, RUNNING, NOPARENT
        pcb[0] = new PCB(ProcessState.RUNNING, -1);

        // add init process to ready list
        readyList.add(0);
    }

    public void shell() {
        Scanner in = new Scanner(System.in);
        String command = "";
        
        do {
            System.out.print("> ");
            command = in.nextLine();
            command = command.replaceAll("\\,+", " ");
            String[] tokens = command.split("\\s+");

            if (tokens.length > 0) {
                if (tokens[0].equals("cr")) {
                    // System.out.println("create() invoked.");
                    create();
                } else if (tokens[0].equals("de")) {
                    if (tokens.length != 2) {
                        System.out.println("Invalid use of command: de <i>");
                    } else {
                        try {
                            int index = Integer.parseInt(tokens[1]);
                            // System.out.println("destroy(" + index + ") invoked.");
                            if (canDestroy(index)) {
                                numDestroyed = 0;
                                destroy(index);
                                System.out.println(numDestroyed + " processes destroyed");
                            }
                        } catch (NumberFormatException ex) {
                            System.out.println("Invalid use of command: de <i>");
                        }
                    }
                } else if (tokens[0].equals("rq")) {
                    if (tokens.length != 2) {
                        System.out.println("Invalid use of command: rq <r>");
                    } else {
                        try {
                            int index = Integer.parseInt(tokens[1]);
                            // System.out.println("request(" + index + ") invoked.");
                            request(index);
    
                        } catch (NumberFormatException ex) {
                            System.out.println("Invalid use of command: rq <r>");
                        }
                    }
                } else if (tokens[0].equals("rl")) {
                    if (tokens.length != 2) {
                        System.out.println("Invalid use of command: rl <r>");
                    } else {
                        try {
                            int index = Integer.parseInt(tokens[1]);
                            // System.out.println("release(" + index + ") invoked.");
                            release(index);
    
                        } catch (NumberFormatException ex) {
                            System.out.println("Invalid use of command: rl <r>");
                        }
                    }
                } else if (tokens[0].equals("to")) {
                    // System.out.println("timeout() invoked.");
                    timeout();
                } else if (tokens[0].equals("in")) {
                    // System.out.println("init() invoked.");
                    init();

                } else if (tokens[0].equals("debug")) {
                    System.out.println(this);
                } else {
                    System.out.println("Unknown command provided.");
                }   
            }
        } while (!command.equals("quit"));
    }

    @Override
    public String toString() {        
        String rep = "\n---------------------------------------------------------\n";
        rep += "READY LIST: ";
        ListIterator<Integer> iterator = readyList.listIterator();
        while (iterator.hasNext()) {
            rep += iterator.next() + " ";
        }
        rep += "\n---------------------------------------------------------\n";

        rep += "PCB Array\n";
        for (int i = 0; i < MAX_PCB; ++i) {
            rep += i + "\t";
            
            if (pcb[i] == null) {
                rep += "NO PCB ENTRY\n";
            } else {
                rep += pcb[i].state + "\t";
                rep += pcb[i].parent + "\t[";

                ListIterator<Integer> iter = pcb[i].children.listIterator();
                while (iter.hasNext()) {
                    rep += iter.next() + " ";
                }
                rep += "]\t[";

                iter = pcb[i].resources.listIterator();
                while (iter.hasNext()) {
                    rep += iter.next() + " ";
                }
                rep += "]\n";
            }
        }
        rep += "---------------------------------------------------------\n";
        rep += "RCB Array\n";
        for (int i = 0; i < MAX_RCB; ++i) {
            rep += i + "\t";
            rep += rcb[i].state + "\t[";
            ListIterator<Integer> iter = rcb[i].waitlist.listIterator();
            while (iter.hasNext()) {
                rep += iter.next() + " ";
            }
            rep += "]\n";
        }
        rep += "---------------------------------------------------------\n";
        
        return rep;
    }
}