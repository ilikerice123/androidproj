package com.example.charlesbai321.myapplication.Data;

/**
 * Created by charlesbai321 on 06/02/18.
 */

public class Category implements Displayable {
    String s;
    int time_spent;

    public Category(String s, int time_spent){
        this.time_spent = time_spent;
        this.s = s;
    }

    @Override
    public int getTimeSpent() {
        return time_spent;
    }

    @Override
    public String getTitleName() {
        return this.s;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Category){
            return ((Category) o).getTitleName().equals(this.s);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return this.s.hashCode();
    }
}
