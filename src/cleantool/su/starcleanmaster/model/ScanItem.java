package cleantool.su.starcleanmaster.model;

import cleantool.su.starcleanmaster.util.ConstantUtil;

public class ScanItem {

    private int titleId;

    private int leftResId;

    private int rightResId;

    private int uiStatus;

    private int type;

    private long size;

    public ScanItem(int titleId, int leftResId, int type) {
        this.titleId = titleId;
        this.leftResId = leftResId;
        this.uiStatus = ConstantUtil.DEFAULT;
        this.type = type;
    }

    public int getTitleId() {
        return titleId;
    }

    public void setTitleId(int title) {
        this.titleId = title;
    }

    public int getLeftResId() {
        return leftResId;
    }

    public void setLeftResId(int leftResId) {
        this.leftResId = leftResId;
    }

    public int getRightResId() {
        return rightResId;
    }

    public void setRightResId(int rightResId) {
        this.rightResId = rightResId;
    }

    public int getUiStatus() {
        return uiStatus;
    }

    public void setUiStatus(int uiStatus) {
        this.uiStatus = uiStatus;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
