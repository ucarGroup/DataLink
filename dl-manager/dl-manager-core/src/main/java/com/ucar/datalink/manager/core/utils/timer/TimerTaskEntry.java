package com.ucar.datalink.manager.core.utils.timer;

/**
 * Created by lubiao on 2016/12/12.
 */
class TimerTaskEntry implements Comparable<TimerTaskEntry> {

    private volatile TimerTaskList list;
    private volatile TimerTaskEntry next;
    private volatile TimerTaskEntry prev;

    private final TimerTask timerTask;
    private final Long expirationMs;

    TimerTaskEntry(TimerTask timerTask, long expirationMs) {
        // if this timerTask is already held by an existing timer task entry,
        // setTimerTaskEntry will remove it.
        if (timerTask != null) {
            timerTask.setTimerTaskEntry(this);
        }
        this.timerTask = timerTask;
        this.expirationMs = expirationMs;
    }

    boolean cancelled() {
        return timerTask.getTimerTaskEntry() != this;
    }

    void remove() {
        TimerTaskList currentList = this.list;
        // If remove is called when another thread is moving the entry parseFrom a task entry list to another,
        // this may fail to remove the entry due to the change of value of list. Thus, we retry until the list becomes null.
        // In a rare case, this thread sees null and exits the loop, but the other thread insert the entry to another list later.
        while (currentList != null) {
            currentList.remove(this);
            currentList = list;
        }
    }

    @Override
    public int compareTo(TimerTaskEntry o) {
        return this.expirationMs.compareTo(o.expirationMs);
    }

    public TimerTaskList getList() {
        return list;
    }

    public void setList(TimerTaskList list) {
        this.list = list;
    }

    public TimerTaskEntry getNext() {
        return next;
    }

    public void setNext(TimerTaskEntry next) {
        this.next = next;
    }

    public TimerTaskEntry getPrev() {
        return prev;
    }

    public void setPrev(TimerTaskEntry prev) {
        this.prev = prev;
    }

    public TimerTask getTimerTask() {
        return timerTask;
    }

    public Long getExpirationMs() {
        return expirationMs;
    }
}
