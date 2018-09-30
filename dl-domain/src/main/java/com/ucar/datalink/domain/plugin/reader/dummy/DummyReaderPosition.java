package com.ucar.datalink.domain.plugin.reader.dummy;

import com.ucar.datalink.domain.Position;

/**
 * Created by lubiao on 2017/2/23.
 */
public class DummyReaderPosition extends Position {
    private long count;

    public DummyReaderPosition() {

    }

    public DummyReaderPosition(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DummyReaderPosition that = (DummyReaderPosition) o;

        return count == that.count;

    }

    @Override
    public int hashCode() {
        return (int) (count ^ (count >>> 32));
    }
}
