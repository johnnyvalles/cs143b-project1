# CS 143B Project 1: Process & Resource Manager
The manager supports the creation and management of data structures representing processes and resources. Interaction with the manager is carried out via a presentation shell.

At any given moment, there is one process running (i.e., simulates a uniprocessor system). The currently running process is able to create and destroy processes and request and release resources in units.

Processes are represented using three priority levels (0, 1, 2). After the system has been initialized, there exists a single process (init process, priority 0) that is the ancestor of all future processes. Scheduling of processes is carried out in favor of higher priorities.

Resources represent physical or virtual components with limited availability in the system (e.g., files, network I/O, Disk I/O, mutexes) and may cause a process to block.

# The Presentation Shell
| Command     | Description |
| ----------- | ----------- |
| `cr <p>`    | create a new process with priority `p`.|
| `de <i>`    | destroy process `i`.|
| `rq <r> <k>`| request `k` units of resource `r`.|
| `rl <r> <k>`| release `k` units of resource `r`.|
| `to`        | trigger timeout and preempt running process and schedule another.|
| `in`        | reset (initialize) all ready lists, PCB entries, and RCB entries.|
| `ls`        | print manager's state (e.g., ready lists, PCBs, RCBs). |

# Building & Running the Manager
Running the manager using the instructor provided input file:
*  `javac *.java && java ManagerDriver < /path/to/input.txt > /path/to/output.txt`

Running the manager and interacting with the presentation shell:
* `javac *.java && java ManagerDriver`