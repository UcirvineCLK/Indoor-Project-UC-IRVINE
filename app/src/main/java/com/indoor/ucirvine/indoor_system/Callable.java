package com.indoor.ucirvine.indoor_system;

/**
 * Created by Administrator on 2016-06-27.
 */
public class Callable extends Thread {
    private Thread t;
    private String threadName;

    Callable(String name){
        threadName = name;
        System.out.println("Creating " + threadName );
    }

    public void run(){
        System.out.println("Running " + threadName );

    }

    public void start ()
    {
        System.out.println("Starting " +  threadName );
        if (t == null)
        {
            t = new Thread (this, threadName);
            t.start ();
        }
    }


}
