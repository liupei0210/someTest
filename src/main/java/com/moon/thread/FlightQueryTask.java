package com.moon.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class FlightQueryTask extends Thread implements FlightQuery {
    private  String origin;
    private String destination;
    private final List<String> flightList=new ArrayList<String>();
    public FlightQueryTask(String airline,String origin,String destination){
        super("["+airline+"]");
        this.origin=origin;
        this.destination=destination;
    }
    @Override
    public void run(){
        System.out.printf("%s-query from %s to %s \n",getName(),origin,destination);
        int randomVal= ThreadLocalRandom.current().nextInt(10);
        try{
            TimeUnit.SECONDS.sleep(randomVal);
            this.flightList.add(getName()+ "-" +randomVal);
            System.out.printf("The Flight:%s list query successfel.\n",getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public List<String> get() {
        return this.flightList;
    }
}
