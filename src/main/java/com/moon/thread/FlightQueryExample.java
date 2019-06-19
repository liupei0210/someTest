package com.moon.thread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class FlightQueryExample {
    private static List<String> flightCompany= Arrays.asList("CSA","CEA","HNA");
    public static void main(String[] args){
        List<String> results=search("SH","BJ");
        System.out.println("================result=================");
        results.forEach(System.out::println);
    }
    private static List<String> search(String origin,String destination){
        final List<String> result=new ArrayList<>();
        List<FlightQueryTask> tasks=flightCompany.stream().map(f->createSearchTask(f,origin,destination)).collect(toList());
        tasks.forEach(Thread::start);
        tasks.forEach(t->{
            try{
                t.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        tasks.stream().map(FlightQueryTask::get).forEach(result::addAll);
        return result;
    }
    private static FlightQueryTask createSearchTask(String flight,String origin,String dest){
        return new FlightQueryTask(flight,origin,dest);
    }
}
