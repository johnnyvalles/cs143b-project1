package com.johnnyvalles.project1;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Scanner;

public class Manager {
    private final int MAX_PCB = 16;
    private final int MAX_RCB = 4;
    
    private PCB[] pcb = null;
    private RCB[] rcb = null;
    private LinkedList<Integer>[] readyList;
    private int numDestroyed = 0;

    public Manager() {
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
        
        rcb[0] = new RCB(1);
        rcb[1] = new RCB(1);
        rcb[2] = new RCB(2);
        rcb[3] = new RCB(3);

        // create init process, RUNNING, NOPARENT
        pcb[0] = new PCB(ProcessState.RUNNING, -1, 0);

        // add init process to ready list
        readyList[0].add(0);
        System.out.print("0 ");
    }

    public void create(int p) {
        if (!isValidPriority(p)) {
            // System.err.println("Error: cannot create process with priority " + p);
            System.err.print("-1 ");
        } else {
            // index of a free PCB
            int childIndex = getFreePCBIndex();
            if (childIndex == -1) {
                // System.err.println("Error: PCB array is full.");
                System.err.print("-1 ");
            } else {
                int parentIndex = getRunningProcessIndex();
                pcb[childIndex] = new PCB(ProcessState.READY, parentIndex, p);
                pcb[parentIndex].children.add(childIndex);
                readyList[p].add(childIndex);
                // System.out.println("* process " + childIndex + " created");      

                if (pcb[childIndex].priority > pcb[parentIndex].priority) {
                    scheduler();
                } else {
                    // System.out.print(parentIndex + " ");
                }
            }
        }
    }

    public boolean canDestroy(int index) {
        int currentIndex = readyList[getNonEmptyHighPriorityListIndex()].getFirst();

        if (currentIndex == index) {
            return true;
        }

        if (pcb[index] == null) {
            // System.out.println("Error: cannot destroy non-existent process.");
            System.out.print("-1 ");
            return false;
        } else if (pcb[index].parent != currentIndex) {
            // System.out.println("Cannot destroy process that is not child of current running process.");
            System.err.print("-1 ");
            return false;
        } else {
            return true;
        }
    }

    public void destroy(int j) {
        // create copy of j's children, source modified recursively
        LinkedList<Integer> children = new LinkedList<Integer>();
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
            readyList[0].removeFirstOccurrence((j));
            readyList[1].removeFirstOccurrence((j));
            readyList[2].removeFirstOccurrence((j));
            // pcb[readyList[getNonEmptyHighPriorityListIndex()].getFirst()].state = ProcessState.RUNNING;
        } else {
            // j is not in the ready list
            // j is blocked
            // j is on the waitlist of some resource
            for (int i = 0; i < rcb.length; ++i) {
                rcb[i].removeProcFromWaitList(j);
            }
        }

        // release all resources held by j
        LinkedList<Pair> tempRes = new LinkedList<Pair>();
        for (Pair pair : pcb[j].resources) {
            tempRes.add(new Pair(pair.first, pair.second));
        }

        for (Pair pair : tempRes) {
            // check if r has a waitlist
            // cannot FREE the resourse, need to give to next in line
            release(j, pair.first, pair.second);
        }

        pcb[j] = null;
        numDestroyed++;
        scheduler();
    }

    public void request(int i, int r, int k) {
        if (rcb[r].state >= k && rcb[r].waitlist.isEmpty()) {
            rcb[r].state -= k;

            // keep running count in single pair instance
            // first see if process already holds some units of resource r
            int pairIndex = -1;
            for (int index = 0; index < pcb[i].resources.size(); ++index) {
                if (pcb[i].resources.get(index).first == r) {
                    pairIndex = index;
                    break;
                }
            }

            if (pairIndex >= 0) {
                // process i already holds units of resource r and wants more
                pcb[i].resources.get(pairIndex).second += k;
            } else {
                // process i does not hold any units of resource r
                pcb[i].resources.add(new Pair(r, k));

            }
        } else {
            pcb[i].state = ProcessState.BLOCKED;
            readyList[getNonEmptyHighPriorityListIndex()].removeFirst();
            rcb[r].waitlist.add(new Pair(i, k));
            scheduler();
        }
    }

    public void release(int i, int r, int k) {
        // see if process already holds some units of resource r
        int pairIndex = -1;
        for (int index = 0; index < pcb[i].resources.size(); ++index) {
            if (pcb[i].resources.get(index).first == r) {
                pairIndex = index;
                break;
            }
        }

        if (pairIndex >= 0) {
            // process i already holds units of resource r and wants to release some
            if (pcb[i].resources.get(pairIndex).second < k) {
                // System.err.println("Error: attempting to release more units that what are actually held.");
                System.out.print("-1 ");
            } else {
                pcb[i].resources.get(pairIndex).second -= k;

                if (pcb[i].resources.get(pairIndex).second == 0) {
                    pcb[i].resources.remove(pairIndex);
                }

                rcb[r].state += k;
                while (!rcb[r].waitlist.isEmpty() && rcb[r].state > 0) {
                    Pair next = rcb[r].waitlist.get(0);
                    if (rcb[r].state >= next.second) {
                        rcb[r].state -= next.second;
                        pcb[next.first].resources.add(new Pair(r, next.second));
                        pcb[next.first].state = ProcessState.READY;
                        rcb[r].waitlist.removeFirst();
                        readyList[pcb[next.first].priority].add(next.first);
                    } else {
                        break;
                    }
                }
                scheduler();
            }
        } else {
            // process i does not hold any units of resource r
            // System.err.println("Error: cannot release a resource that is not held by this process.");
            System.err.print("-1 ");
            return;
        }
    }

    public void timeout() {
        int listIndex = getNonEmptyHighPriorityListIndex();
        pcb[readyList[listIndex].getFirst()].state = ProcessState.READY;
        readyList[listIndex].add(readyList[listIndex].removeFirst());
        scheduler();
    }

    public void scheduler() {
        int listIndex = getNonEmptyHighPriorityListIndex();
        int currentRunningProcess = getRunningProcessIndex();

        if (currentRunningProcess == -1) {
            // System.err.println("Error: no running process.");
            pcb[readyList[listIndex].getFirst()].state = ProcessState.RUNNING;
        } else {
            pcb[currentRunningProcess].state = ProcessState.READY;
        }

        int j = readyList[listIndex].getFirst();
        pcb[j].state = ProcessState.RUNNING;
        // System.out.println("* process " + j + " running");
        System.out.print(j + " ");
    }


    public void shell() {
        Scanner in = new Scanner(System.in);
        String command = "";
        
        do {
            command = in.nextLine();
            command = command.replaceAll("\\,+", " ");
            String[] tokens = command.split("\\s+");

            if (tokens.length > 0) {
                if (tokens[0].equals("cr")) {
                    if (tokens.length != 2) {
                        System.err.println("-1 ");
                    } else {
                        try {
                            int priority = Integer.parseInt(tokens[1]);
                            create(priority);
                        } catch (NumberFormatException ex) {
                            System.err.println("-1 ");
                        }
                    }
                } else if (tokens[0].equals("de")) {
                    if (tokens.length != 2) {
                        System.out.println("-1 ");
                    } else {
                        try {
                            int index = Integer.parseInt(tokens[1]);
                            if (canDestroy(index)) {
                                numDestroyed = 0;
                                destroy(index);
                            }
                        } catch (NumberFormatException ex) {
                            System.out.println("-1 ");
                        }
                    }
                } else if (tokens[0].equals("rq")) {
                    if (tokens.length != 3) {
                        System.out.println("-1 ");
                    } else {
                        try {
                            int i = getRunningProcessIndex();
                            int r = Integer.parseInt(tokens[1]);
                            int k = Integer.parseInt(tokens[2]);
                            request(i, r, k);
    
                        } catch (NumberFormatException ex) {
                            System.out.println("-1 ");
                        }
                    }
                } else if (tokens[0].equals("rl")) {
                    if (tokens.length != 3) {
                        System.out.println("-1 ");
                    } else {
                        try {
                            int i = getRunningProcessIndex();
                            int r = Integer.parseInt(tokens[1]);
                            int k = Integer.parseInt(tokens[2]);
                            release(i, r, k);
    
                        } catch (NumberFormatException ex) {
                            System.out.println("-1 ");
                        }
                    }
                } else if (tokens[0].equals("to")) {
                    timeout();
                } else if (tokens[0].equals("in")) {
                    init();
                } else if (tokens[0].equals("debug")) {
                    System.out.println(this);
                } else {
                    System.out.println("-1 ");
                }   
            }
        } while (in.hasNext());

        in.close();
    }

    public void debug_shell() {
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
                    if (tokens.length != 3) {
                        System.out.println("Invalid use of command: rq <r> <k>");
                    } else {
                        try {
                            int i = getRunningProcessIndex();
                            int r = Integer.parseInt(tokens[1]);
                            int k = Integer.parseInt(tokens[2]);
                            request(i, r, k);
    
                        } catch (NumberFormatException ex) {
                            System.out.println("Invalid use of command: rq <r>");
                        }
                    }
                } else if (tokens[0].equals("rl")) {
                    if (tokens.length != 3) {
                        System.out.println("Invalid use of command: rl <r> <k>");
                    } else {
                        try {
                            int i = getRunningProcessIndex();
                            int r = Integer.parseInt(tokens[1]);
                            int k = Integer.parseInt(tokens[2]);
                            // System.out.println("release(" + index + ") invoked.");
                            release(i, r, k);
    
                        } catch (NumberFormatException ex) {
                            System.out.println("Invalid use of command: rl <r>");
                        }
                    }
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
        } while (in.hasNext() && !command.equals("quit"));

        in.close();
    }

    // Helper Methods
    private int getFreePCBIndex() {
        for (int i = 0; i < MAX_PCB; ++i) {
            if (pcb[i] == null) {
                return i;
            }
        }
        return -1;
    }

    private boolean isValidPriority(int p) {
        return p > 0 && p < 3;
    }

    public int getNonEmptyHighPriorityListIndex() {
        return readyList[2].isEmpty() ? (readyList[1].isEmpty() ? 0 : 1) : 2;
    }

    public int getRunningProcessIndex() {
        if (!readyList[2].isEmpty() && pcb[readyList[2].getFirst()].state == ProcessState.RUNNING) {
            return readyList[2].getFirst();
        } else if (!readyList[1].isEmpty() && pcb[readyList[1].getFirst()].state == ProcessState.RUNNING) {
            return readyList[1].getFirst();
        } else if (!readyList[0].isEmpty() && pcb[readyList[0].getFirst()].state == ProcessState.RUNNING) {
            return readyList[0].getFirst();
        } else {
            return -1; // no process is currently running, need to schedule
        }
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

                ListIterator<Integer> iter1 = pcb[i].children.listIterator();
                while (iter1.hasNext()) {
                    rep += iter1.next() + " ";
                }
                rep += "]\t[";

                ListIterator<Pair> iter2 = pcb[i].resources.listIterator();
                while (iter2.hasNext()) {
                    Pair pair = iter2.next();
                    rep += "(" + pair.first + ", " + pair.second + ")  ";
                }
                rep += "]\n";
            }
        }
        rep += "---------------------------------------------------------\n";
        rep += "RCB Array\n";
        for (int i = 0; i < MAX_RCB; ++i) {
            rep += i + "\t";
            rep += rcb[i].state + "\t[";
            ListIterator<Pair> iter = rcb[i].waitlist.listIterator();
            while (iter.hasNext()) {
                Pair pair = iter.next();
                rep += "(" + pair.first + ", " + pair.second + ")  ";
            }
            rep += "]\n";
        }
        rep += "---------------------------------------------------------\n";
        
        return rep;
    }
}