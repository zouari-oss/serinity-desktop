package com.serinity.forumcontrol.Test;

import com.serinity.forumcontrol.Utils.MyDataBase;


public class DBtest {
    public static void main(String[] args) {
        MyDataBase db = MyDataBase.getInstance();
        System.out.println("Connection = " + db.getCnx());
    }
}
