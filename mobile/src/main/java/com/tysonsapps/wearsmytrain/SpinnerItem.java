package com.tysonsapps.wearsmytrain;

/**
 * Created by jared on 8/5/14.
 */
public class SpinnerItem implements Comparable{
    private String displayText;
    private String id;

    public SpinnerItem(String displayText, String id){
        this.displayText = displayText;
        this.id = id;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString(){
        return displayText;
    }

    @Override
    public int compareTo(Object item) {
        int result = this.displayText.compareTo(((SpinnerItem)item).displayText);
        return result == 0 ? this.displayText.compareTo(((SpinnerItem)item).displayText) : result;
    }

    @Override
    public boolean equals(Object item){
        return ((SpinnerItem)item).getId().equals(this.id);
    }

}
