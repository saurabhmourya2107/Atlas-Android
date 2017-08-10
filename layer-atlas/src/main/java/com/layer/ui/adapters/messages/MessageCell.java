package com.layer.ui.adapters.messages;

import com.layer.ui.messagetypes.CellFactory;

public class MessageCell {

    protected final boolean mMe;
    protected final CellFactory mCellFactory;

    public MessageCell(boolean me, CellFactory CellFactory) {
        mMe = me;
        mCellFactory = CellFactory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageCell messageCell = (MessageCell) o;

        if (mMe != messageCell.mMe) return false;
        return mCellFactory.equals(messageCell.mCellFactory);

    }

    @Override
    public int hashCode() {
        int result = (mMe ? 1 : 0);
        result = 31 * result + mCellFactory.hashCode();
        return result;
    }

}
