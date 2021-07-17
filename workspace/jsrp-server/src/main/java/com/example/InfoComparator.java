package com.example;

import java.util.Comparator;
 
public class InfoComparator implements Comparator<Object> {
 
    public int compare(Object obj1, Object obj2) {
         
        Info info1 = (Info)obj1;
        Info info2 = (Info)obj2;
         
        if (info1.getPriority() > info2.getPriority()) {
            return 1;
        } else if(info1.getPriority() < info2.getPriority()) {
            return -1;
        } else{
            return 0;
        }
 
    }
 
}