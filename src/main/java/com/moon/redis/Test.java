package com.moon.redis;

import redis.clients.jedis.Jedis;

public class Test {
    public static void main(String[] args) {
        Jedis jedis=new Jedis("127.0.0.1");
        System.out.println(jedis.ping());
    }
}
