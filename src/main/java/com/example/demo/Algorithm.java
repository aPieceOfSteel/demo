package com.example.demo;

import java.math.BigInteger;
import java.util.*;

/**
 * 随机 轮训 一致性哈希 算法
 */
public class Algorithm {


    /**
     * 假设节点有三个
     */
    private static List<String>  node =new ArrayList<>();
    /**
     * 加权轮训节点
     */
    private static List<Map<String,Object>>  weightNode =new ArrayList<>();
    private static int currentIndex;
    private static int totalServer;
    private static int currentWeight;
    private static int maxWeight;
    private static int gcdWeight;


    //key表示服务器的hash值，value表示服务器
    private static SortedMap<Integer, String> sortedMap = new TreeMap<Integer, String>();

    static{
        //=============================
        node.add("192.168.1");
        node.add("192.168.2");
        node.add("192.168.3");
        //=============================
        Map<String,Object> map = new HashMap<>();
        map.put("ip","192.168.1");
        map.put("weight",5);
        weightNode.add(map);

        Map<String,Object> map1 = new HashMap<>();
        map1.put("ip","192.168.2");
        map1.put("weight",10);
        weightNode.add(map1);

        Map<String,Object> map2 = new HashMap<>();
        map2.put("ip","192.168.3");
        map2.put("weight",15);
        weightNode.add(map2);

        //=============================

        totalServer = node.size();
        currentIndex = totalServer - 1;
        maxWeight = maxWeight();
        gcdWeight = serverGcd();

        //=============================

        for (int i=0; i<node.size(); i++) {
            int hash = getHash(node.get(i));
            System.out.println("[" + node.get(i) + "]加入集合中, 其Hash值为" + hash);
            sortedMap.put(hash, node.get(i));
        }
    }

    //=============================

    /**
     * 随机算法
     */
    public static void random(){
        //得到一共有多少个节点
        int nodeSize=node.size();
        Random ra =new Random();
        int i = ra.nextInt(nodeSize);
        System.out.println("随机下标为: "+i+" 获取节点为："+ node.get(i));
    }

    //=============================
    /**
     * 普通轮训算法
     */
    public static void loop(){
        currentIndex = (currentIndex + 1) % totalServer;
        System.out.println("轮训获取节点为："+ node.get(currentIndex));

    }

    //=============================
    /**
     * 加权轮训算法
     */
    public  static void round() {
        while (true) {
            currentIndex = (currentIndex + 1) % totalServer;
            if (currentIndex == 0) {
                currentWeight = currentWeight - gcdWeight;
                if (currentWeight <= 0) {
                    currentWeight = maxWeight;
                    if(currentWeight == 0) {
                        System.out.println("currentWeight==0");
                        break;
                    }
                }
            }

            if(Integer.parseInt(weightNode.get(currentIndex).get("weight").toString()) >= currentWeight) {
                System.out.println("加权轮训获取节点为："+ weightNode.get(currentIndex));
                break;
            }
        }
    }


    /**
     * 返回所有服务器的权重的最大公约数
     *
     * @return
     */
    private static int serverGcd() {
        int comDivisor = 0;
        for (int i = 0; i < totalServer - 1; i++) {
            if (comDivisor == 0) {
                //我只是图个方便用的map 大家可以封装一个类
                comDivisor = gcd(Integer.parseInt(weightNode.get(i).get("weight").toString()), Integer.parseInt(weightNode.get(i + 1).get("weight").toString()));
            } else {
                comDivisor = gcd(comDivisor, Integer.parseInt(weightNode.get(i + 1).get("weight").toString()));
            }
        }
        return comDivisor;
    }

    /**
     * 获得服务器中的最大权重
     *
     * @return
     */
    private static int maxWeight() {
        int max =Integer.parseInt( weightNode.get(0).get("weight").toString());
        int tmp;
        for (int i = 1; i < totalServer; i++) {
            tmp =Integer.parseInt(weightNode.get(i).get("weight").toString());
            if (max < tmp) {
                max = tmp;
            }
        }
        return max;
    }

    /**
     * 求两个数的最大公约数 4和6最大公约数是2
     *
     * @param num1
     * @param num2
     * @return
     */
    private static int gcd(int num1, int num2) {
        BigInteger i1 = new BigInteger(String.valueOf(num1));
        BigInteger i2 = new BigInteger(String.valueOf(num2));
        return i1.gcd(i2).intValue();
    }


    //=============================


    //使用FNV1_32_HASH算法计算服务器的Hash值,这里不使用重写hashCode的方法，最终效果没区别
    private static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++){
            hash = (hash ^ str.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        // 如果算出来的值为负数则取其绝对值
        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }


    //得到应当路由到的结点
    private static String getServer(String key) {
        //得到该key的hash值
        int hash = getHash(key);
        //得到大于该Hash值的所有Map
        SortedMap<Integer, String> subMap = sortedMap.tailMap(hash);
        if(subMap.isEmpty()){
            //如果没有比该key的hash值大的，则从第一个node开始
            Integer i = sortedMap.firstKey();
            //返回对应的服务器
            return sortedMap.get(i);
        }else{
            //第一个Key就是顺时针过去离node最近的那个结点
            Integer i = subMap.firstKey();
            //返回对应的服务器
            return subMap.get(i);
        }
    }

    /**
     * 一致性哈希
     */
    public  static void consistentHashing(){
        String[] keys = {"张三的查询请求", "李四的修改请求", "王五的删除请求","赵六的新增请求"};
        for(int i=0; i<keys.length; i++){
            System.out.println("[" + keys[i] + "]的hash值为" +
                    getHash(keys[i]) + ", 被路由到结点[" + getServer(keys[i]) + "]");
        }
    }



    public static void main(String[] args) {
        for (int i = 0; i <10 ; i++) {
            //随机
            //random();
            //轮训
             loop();
            //加权轮训
            //round();
            //一致性哈希
            // consistentHashing();
        }
    }


}