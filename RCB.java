import java.util.LinkedList;

public class RCB {
    public int state;
    public final int inventory;
    public LinkedList<Pair> waitlist;

    public RCB(int inventory) {
        state = inventory;
        this.inventory = inventory;
        waitlist = new LinkedList<Pair>();
    }

    public boolean removeProcFromWaitList(int proc) {
        int index = -1;

        for (int i = 0; i < waitlist.size(); ++i) {
            if (waitlist.get(i).first == proc) {
                index = i;
                break;
            }
        }

        if (index > -1) {
            waitlist.remove(index);
            return true;
        } else {
            return false;
        }
    }
}