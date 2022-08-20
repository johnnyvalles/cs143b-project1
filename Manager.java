package com.johnnyvalles.project1;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Scanner;

public class Manager {
    private final int MAX_PCB = 4;
    private final int MAX_RCB = 2;
    
    private PCB[] pcb = null;
    private RCB[] rcb = null;
    private LinkedList<Integer>[] readyList;
    private int numDestroyed = 0;

    public Manager() {
        init();
    }

    public void init() {
        // create PCB array, all entries initially null
        pcb = new PCB[MAX_PCB];
        // create RCB array, all entries initially null
        rcb = new RCB[MAX_RCB];
        
        // create empty ready lists, no processes yet
        readyList = new LinkedList[3];
        readyList[0] = new LinkedList<Integer>();
        readyList[1] = new LinkedList<Integer>();
        readyList[2] = new LinkedList<Integer>();
        
        // initialize all RCB entries
        for (int i = 0; i < MAX_RCB; ++i) {
            rcb[i] = new RCB();
        }

        // create init process, RUNNING, NOPARENT
        pcb[0] = new PCB(ProcessState.RUNNING, -1, 0);

        // add init process to ready list
        readyList[0].add(0);
    }

    public int getRunningProcessIndex() {
        if (!readyList[2].isEmpty() && pcb[readyList[2].getFirst()].state == ProcessState.RUNNING) {
            return readyList[2].getFirst();
        }

        if (!readyList[1].isEmpty() && pcb[readyList[1].getFirst()].state == ProcessState.RUNNING) {
            return readyList[1].getFirst();
        }

        return readyList[0].getFirst(); // init process, can simply return 0 too
    }

    public int getHighestListIndex() {
        int listIndex;
        if (!readyList[2].isEmpty()) {
            listIndex = 2;
        } else if (!readyList[1].isEmpty()) {
            listIndex = 1;
        } else {
            listIndex = 0;
        }
        return listIndex;
    }

    public void addReadyProcessIndex(int i) {
        readyList[pcb[i].priority].add(i);
    }

    public void create(int p) {
        if (p < 1 || p > 2) {
            System.err.println("Cannot create process with priority 0.");
            return;
        }

        // search for free entry
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
            int parentIndex = getRunningProcessIndex();
            pcb[index] = new PCB(ProcessState.READY, parentIndex, p);
            pcb[parentIndex].children.add(index);
            addReadyProcessIndex(index);

            System.out.println("* process " + index + " created");      

            if (pcb[index].priority > pcb[parentIndex].priority) {
                scheduler();
            }
        }
    }

    // public boolean canDestroy(int index) {
    //     int currentIndex = readyList.get(0);

    //     if (currentIndex == index) {
    //         return true;
    //     }

    //     if (pcb[index] == null) {
    //         System.out.println("Cannot destroy non-existent process.");
    //         return false;
    //     } else if (pcb[index].parent != currentIndex) {
    //         System.out.println("Cannot destroy process that is not child of current running process.");
    //         return false;
    //     } else {
    //         return true;
    //     }
    // }

    // public void destroy(int j) {
    //     // create copy of j's children, source modified recursively
    //     LinkedList<Integer> children = new LinkedList();
    //     for (int k : pcb[j].children) {
    //          children.add(k);
    //     }
    //     for (int k : children) {
    //         destroy(k);
    //     }

    //     // remove j from parent's list of children
    //     pcb[pcb[j].parent].children.removeFirstOccurrence(j);

    //     // check if process is on RL or is on a resource waitlist
    //     if (pcb[j].state != ProcessState.BLOCKED) {
    //         // j is in the ready list, need to remove it
    //         readyList.removeFirstOccurrence((j));
    //         pcb[readyList.get(0)].state = ProcessState.RUNNING;
    //     } else {
    //         // j is not in the ready list
    //         // j is blocked
    //         // j is on the waitlist of some resource
    //         for (int i = 0; i < rcb.length; ++i) {
    //             rcb[i].waitlist.removeFirstOccurrence(j);
    //         }
    //     }

    //     // release all resources held by j
    //     for (int r : pcb[j].resources) {
    //         // check if r has a waitlist
    //         // cannot FREE the resourse, need to give to next in line
    //         if (!rcb[r].waitlist.isEmpty()) {
    //             // give resource to head of waitlist
    //             int waitlistHead = rcb[r].waitlist.removeFirst();
    //             pcb[waitlistHead].resources.add(r);
    //             pcb[waitlistHead].state = ProcessState.READY;
    //             readyList.add(waitlistHead);
    //         } else {
    //             // r has no waitlist, can be freed
    //             rcb[r].state = ResourceState.FREE;
    //         }
    //     }

    //     pcb[j] = null;
    //     numDestroyed++;
    // }
    
    // public void request(int r) {
    //     // check if valid request
    //     int currentProcIndex = readyList.get(0);
    //     if (currentProcIndex == 0) {
    //         System.err.println("Error: process 0 cannot request resources.");
    //     } else if (r < 0 && r > MAX_RCB - 1) {
    //         System.err.println("Error: resource requested does not exist.");
    //     } else if (pcb[currentProcIndex].resources.contains(r)) {
    //         System.err.println("Error: resource already held by this process.");
    //     } else {
    //         // proceed with resource acquisition
    //         if (rcb[r].state == ResourceState.FREE) {
    //             rcb[r].state = ResourceState.ALLOCATED;
    //             pcb[currentProcIndex].resources.add(r);
    //             System.out.println("resource " + r + " allocated");
    //         } else {
    //             pcb[currentProcIndex].state = ProcessState.BLOCKED;
    //             readyList.removeFirstOccurrence(currentProcIndex);
    //             rcb[r].waitlist.add(currentProcIndex);
    //             scheduler();
    //         }
    //     }
    // }

    // public void release(int r) {
    //     // check if valid request
    //     int currentProcIndex = readyList.get(0);
    //     if (currentProcIndex == 0) {
    //         System.err.println("Error: process 0 cannot release resources.");
    //     } else if (r < 0 && r > MAX_RCB - 1) {
    //         System.err.println("Error: resource to release does not exist");
    //     } else if (!pcb[currentProcIndex].resources.contains(r)) {
    //         System.err.println("Error: resource not held by this process.");
    //     } else {
    //         // proceed with resource release
            
    //         // remove r from resource list of process i
    //         pcb[currentProcIndex].resources.removeFirstOccurrence(r);

    //         if (rcb[r].waitlist.isEmpty()) {
    //             rcb[r].state = ResourceState.FREE;
    //         } else {
    //             int j = rcb[r].waitlist.removeFirst();
    //             readyList.add(j);
    //             pcb[j].state = ProcessState.READY;
    //             pcb[j].resources.add(r);                
    //         }

    //         System.out.println("resource " + r + " released");

    //     }
    // }

    public void timeout() {
        int listIndex;
        if (!readyList[2].isEmpty()) {
            listIndex = 2;
        } else if (!readyList[1].isEmpty()) {
            listIndex = 1;
        } else {
            listIndex = 0;
        }
        pcb[readyList[listIndex].getFirst()].state = ProcessState.READY;
        readyList[listIndex].add(readyList[listIndex].removeFirst());
        scheduler();
    }

    public void scheduler() {
        int listIndex;
        if (!readyList[2].isEmpty()) {
            listIndex = 2;
        } else if (!readyList[1].isEmpty()) {
            listIndex = 1;
        } else {
            listIndex = 0;
        }

        int currentProcess = 0;
        for (int i = listIndex - 1; i >= 0; --i) {
            if (!readyList[i].isEmpty()) {
                currentProcess = readyList[i].getFirst();
                break;
            }
        }
        pcb[currentProcess].state = ProcessState.READY;

        int j = readyList[listIndex].getFirst();
        pcb[j].state = ProcessState.RUNNING;
        System.out.println("* process " + j + " running");
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
                    if (tokens.length != 2) {
                        System.err.println("Invalid use of command: cr <p>");
                    } else {
                        try {
                            int priority = Integer.parseInt(tokens[1]);
                            create(priority);
                        } catch (NumberFormatException ex) {
                            System.err.println("Invalid use of command: cr <p>");
                        }
                    }
                } else if (tokens[0].equals("de")) {
                    // if (tokens.length != 2) {
                    //     System.out.println("Invalid use of command: de <i>");
                    // } else {
                    //     try {
                    //         int index = Integer.parseInt(tokens[1]);
                    //         // System.out.println("destroy(" + index + ") invoked.");
                    //         if (canDestroy(index)) {
                    //             numDestroyed = 0;
                    //             destroy(index);
                    //             System.out.println(numDestroyed + " processes destroyed");
                    //         }
                    //     } catch (NumberFormatException ex) {
                    //         System.out.println("Invalid use of command: de <i>");
                    //     }
                    // }
                } else if (tokens[0].equals("rq")) {
                    // if (tokens.length != 2) {
                    //     System.out.println("Invalid use of command: rq <r>");
                    // } else {
                    //     try {
                    //         int index = Integer.parseInt(tokens[1]);
                    //         // System.out.println("request(" + index + ") invoked.");
                    //         request(index);
    
                    //     } catch (NumberFormatException ex) {
                    //         System.out.println("Invalid use of command: rq <r>");
                    //     }
                    // }
                } else if (tokens[0].equals("rl")) {
                    // if (tokens.length != 2) {
                    //     System.out.println("Invalid use of command: rl <r>");
                    // } else {
                    //     try {
                    //         int index = Integer.parseInt(tokens[1]);
                    //         // System.out.println("release(" + index + ") invoked.");
                    //         release(index);
    
                    //     } catch (NumberFormatException ex) {
                    //         System.out.println("Invalid use of command: rl <r>");
                    //     }
                    // }
                } else if (tokens[0].equals("to")) {
                    // // System.out.println("timeout() invoked.");
                    timeout();
                } else if (tokens[0].equals("in")) {
                    // // System.out.println("init() invoked.");
                    init();
                } else if (tokens[0].equals("debug")) {
                    System.out.println(this);
                } else {
                    System.out.println("Unknown command provided.");
                }   
            }
        } while (!command.equals("quit"));

        in.close();
    }

    @Override
    public String toString() {        
        String rep = "\n---------------------------------------------------------\n";
        rep += "Ready Lists\n";
        for (int i = 2; i >= 0; --i) {
            rep += "\tPRIORITY " + i + ": ";

            ListIterator<Integer> iterator = readyList[i].listIterator();

            while (iterator.hasNext()) {
                rep += iterator.next() + " ";
            }
            rep += "\n";
        }
        rep += "---------------------------------------------------------\n";
        rep += "PCB Array\n";
        for (int i = 0; i < MAX_PCB; ++i) {
            rep += i + "\t";
            
            if (pcb[i] == null) {
                rep += "NO PROCESS\n";
            } else {
                rep += pcb[i].priority + "\t";
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