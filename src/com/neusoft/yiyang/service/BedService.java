package com.neusoft.yiyang.service;

import com.neusoft.yiyang.cli.CliUtils;
import java.util.*;

/**
*床位管理业务逻辑
*JSON字段统一snake_case
*/
public class BedService{
    static final String BEDS_FILE=CliUtils.DATA_DIR+"/beds.json";
    static final String ROOMS_FILE=CliUtils.DATA_DIR+"/rooms.json";
    static final String CUSTOMERS_FILE=CliUtils.DATA_DIR+"/customers.json";

    public static List<Map<String,Object>> getAllBedsWithStatus() throws Exception{
        List<Map<String,Object>> beds=CliUtils.loadList(BEDS_FILE);
        List<Map<String,Object>> customers=CliUtils.loadList(CUSTOMERS_FILE);

        //bed_id→customer_name映射
        Map<Integer,String> customerMap=new HashMap<>();
        for(Map<String,Object> c:customers){
            if((Integer)c.get("is_deleted")==0){
                int bid=(Integer)c.get("bed_id");
                customerMap.put(bid,(String)c.get("customer_name"));
            }
        }

        for(Map<String,Object> b:beds){
            int status=(Integer)b.get("bed_status");
            b.put("_status_name",statusText(status));
            b.put("_customer_name",
                customerMap.getOrDefault((Integer)b.get("id"),"—"));
        }
        return beds;
    }

    public static List<Map<String,Object>> getBedStatsByRoom() throws Exception{
        List<Map<String,Object>> beds=CliUtils.loadList(BEDS_FILE);
        List<Map<String,Object>> rooms=CliUtils.loadList(ROOMS_FILE);

        Map<Integer,String> roomFloorMap=new HashMap<>();
        for(Map<String,Object> r:rooms){
            roomFloorMap.put((Integer)r.get("room_no"),
                (String)r.get("room_floor"));
        }

        Map<Integer,int[]> stats=new LinkedHashMap<>();
        for(Map<String,Object> b:beds){
            int roomNo=(Integer)b.get("room_no");
            int status=(Integer)b.get("bed_status");
            stats.putIfAbsent(roomNo,new int[4]);
            stats.get(roomNo)[0]++;
            if(status==1) stats.get(roomNo)[1]++;
            else if(status==2) stats.get(roomNo)[2]++;
            else if(status==3) stats.get(roomNo)[3]++;
        }

        List<Map<String,Object>> result=new ArrayList<>();
        for(Map.Entry<Integer,int[]> entry:stats.entrySet()){
            int roomNo=entry.getKey();
            int[] s=entry.getValue();
            Map<String,Object> info=new LinkedHashMap<>();
            info.put("floor",roomFloorMap.getOrDefault(roomNo,"?"));
            info.put("room_no",roomNo);
            info.put("total",s[0]);
            info.put("free",s[1]);
            info.put("occupied",s[2]);
            info.put("outgoing",s[3]);
            result.add(info);
        }
        return result;
    }

    public static void swapBeds(int bedId1,int bedId2) throws Exception{
        if(bedId1==bedId2)
            throw new IllegalArgumentException("不能与自身调换！");

        List<Map<String,Object>> beds=CliUtils.loadList(BEDS_FILE);
        List<Map<String,Object>> customers=CliUtils.loadList(CUSTOMERS_FILE);

        Map<String,Object> bed1=null,bed2=null;
        for(Map<String,Object> b:beds){
            if((Integer)b.get("id")==bedId1) bed1=b;
            if((Integer)b.get("id")==bedId2) bed2=b;
        }
        if(bed1==null||bed2==null)
            throw new IllegalArgumentException("床位不存在！");
        if((Integer)bed1.get("bed_status")!=2)
            throw new IllegalArgumentException("床位"+bed1.get("bed_no")+"非有人状态！");
        if((Integer)bed2.get("bed_status")!=2)
            throw new IllegalArgumentException("床位"+bed2.get("bed_no")+"非有人状态！");

        Map<String,Object> cust1=null,cust2=null;
        for(Map<String,Object> c:customers){
            if((Integer)c.get("is_deleted")==0){
                if((Integer)c.get("bed_id")==bedId1) cust1=c;
                if((Integer)c.get("bed_id")==bedId2) cust2=c;
            }
        }
        if(cust1==null||cust2==null)
            throw new IllegalArgumentException("找不到对应客户！");

        int tmpBedId=(Integer)cust1.get("bed_id");
        String tmpRoom=(String)cust1.get("room_no");
        cust1.put("bed_id",cust2.get("bed_id"));
        cust1.put("room_no",String.valueOf(bed2.get("room_no")));
        cust2.put("bed_id",tmpBedId);
        cust2.put("room_no",tmpRoom);

        CliUtils.saveList(CUSTOMERS_FILE,customers);
    }

    public static String statusText(int status){
        return switch(status){
            case 1->"空闲 ○";
            case 2->"有人 ●";
            case 3->"外出 ◐";
            default->"未知";
        };
    }
}