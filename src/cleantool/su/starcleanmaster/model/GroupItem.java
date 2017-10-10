package cleantool.su.starcleanmaster.model;

public class GroupItem {

    private int title;

    private boolean isCheck;

    private boolean isExpand;

    private long size;

    public GroupItem(int title, long size) {
        this.title = title;
        this.isCheck = true;
        this.size = size;
    }

    public GroupItem(int title, long size, boolean isCheck) {
        this.title = title;
        this.size = size;
        this.isCheck = isCheck;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean isExpand) {
        this.isExpand = isExpand;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
