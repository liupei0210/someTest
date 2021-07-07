package com.moon.junit;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class Test1 {
    private class Person implements Cloneable{
        private int age;
        private String name;

        public Person(int age, String name) {
            this.age = age;
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        protected Person clone() {
            return new Person(this.age,this.name);
        }

        @Override
        public String toString(){
            return this.name+":"+this.age;
        }
    }
    @Test
    public void Test(){
        Person me=new Person(29,"LiuPei");
        List<Person> people=new ArrayList<>();
        people.add(me);
        Person shadow=people.get(0).clone();
        shadow.setAge(18);
//        shadow.setName("");
        System.out.println(people.get(0).toString());
        System.out.println(shadow.toString());
    }
    @Test
    public void test1(){
        String s="int.a";
        System.out.println(s.split("\\.").length);
    }
}
