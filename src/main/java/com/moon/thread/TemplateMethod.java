package com.moon.thread;
//线程start方法和run方法运用了类似如下的模板设计模式
public class TemplateMethod {
    public final void pint(String message){
        System.out.println("#######");
        wrapPrint(message);
        System.out.println("#######");
    }
    protected void wrapPrint(String message){}
    public static void main(String[] args){
        TemplateMethod t1=new TemplateMethod(){
            @Override
            protected void wrapPrint(String message){
                System.out.println(message);
            }
        };
        t1.pint("Hello Thread!");
    }
}
