package com.neusoft.yiyang.service;

import com.neusoft.yiyang.cli.CliUtils;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
*客户管理业务逻辑
*JSON字段统一使用snake_case，与PDF数据库设计一致
*/
public class CustomerService{
    static final String CUSTOMERS_FILE=CliUtils.DATA_DIR+"/customers.json";
    static final String BEDS_FILE=CliUtils.DATA_DIR+"/beds.json";
    static final String ROOMS_FILE=CliUtils.DATA_DIR+"/rooms.json";
    static final String BACKDOWN_FILE=CliUtils.DATA_DIR+"/backdown.json";
    static final String OUTWARD_FILE=CliUtils.DATA_DIR+"/outward.json";
    static final String BED_DETAILS_FILE=CliUtils.DATA_DIR+"/bed_details.json";
    static final DateTimeFormatter DATE_FMT=DateTimeFormatter.ofPattern("yyyy-MM-dd");

    //====================入住相关====================

    public static Map<String,List<Integer>> getRoomsByFloor() throws Exception{
        Map<String,List<Integer>> map=new LinkedHashMap<>();
        List<Map<String,Object>> rooms=CliUtils.loadList(ROOMS_FILE);
        for(Map<String,Object> r:rooms){
            String floor=(String)r.get("room_floor");
            int roomNo=(Integer)r.get("room_no");
            map.computeIfAbsent(floor,k->new ArrayList<>()).add(roomNo);
        }
        return map;
    }

    public static List<Map<String,Object>> getFreeBeds(int roomNo) throws Exception{
        List<Map<String,Object>> beds=CliUtils.loadList(BEDS_FILE);
        List<Map<String,Object>> freeBeds=new ArrayList<>();
        for(Map<String,Object> b:beds){
            if((Integer)b.get("room_no")==roomNo
                    &&(Integer)b.get("bed_status")==1){
                freeBeds.add(b);
            }
        }
        return freeBeds;
    }

    public static int calcAge(String birthday){
        LocalDate birth=LocalDate.parse(birthday,DATE_FMT);
        return Period.between(birth,LocalDate.now()).getYears();
    }

    public static void checkin(Map<String,Object> customer) throws Exception{
        String checkinDate=(String)customer.get("checkin_date");
        String expirationDate=(String)customer.get("expiration_date");
        int bedId=(Integer)customer.get("bed_id");

        LocalDate in=LocalDate.parse(checkinDate,DATE_FMT);
        LocalDate out=LocalDate.parse(expirationDate,DATE_FMT);
        if(out.isBefore(in)){
            throw new IllegalArgumentException("合同到期时间不能小于入住时间！");
        }

        //保存客户
        List<Map<String,Object>> customers=CliUtils.loadList(CUSTOMERS_FILE);
        int newId=customers.isEmpty()?1
            :(Integer)customers.get(customers.size()-1).get("id")+1;
        customer.put("id",newId);
        customer.put("user_id",-1);
        customer.put("level_id",null);
        customer.put("is_deleted",0);
        customer.put("height",null);
        customer.put("weight",null);
        customer.put("psychosomatic_state",null);
        customer.put("attention",null);
        customer.put("filepath",null);
        customers.add(customer);
        CliUtils.saveList(CUSTOMERS_FILE,customers);

        //更新床位状态
        updateBedStatus(bedId,2);

        //创建床位使用详情记录
        List<Map<String,Object>> details=CliUtils.loadList(BED_DETAILS_FILE);
        int detailId=details.isEmpty()?1
            :(Integer)details.get(details.size()-1).get("id")+1;
        Map<String,Object> detail=new LinkedHashMap<>();
        detail.put("id",detailId);
        detail.put("bed_id",bedId);
        detail.put("customer_id",newId);
        detail.put("start_date",checkinDate);
        detail.put("end_date",null);
        details.add(detail);
        CliUtils.saveList(BED_DETAILS_FILE,details);
    }

    //====================退住相关====================

    public static List<Map<String,Object>> getActiveCustomers() throws Exception{
        List<Map<String,Object>> all=CliUtils.loadList(CUSTOMERS_FILE);
        List<Map<String,Object>> actives=new ArrayList<>();
        for(Map<String,Object> c:all){
            if((Integer)c.get("is_deleted")==0){
                actives.add(c);
            }
        }
        return actives;
    }

    public static void checkout(int customerId,int type,String backdownDate,
            String deathDate) throws Exception{
        List<Map<String,Object>> customers=CliUtils.loadList(CUSTOMERS_FILE);
        Map<String,Object> target=null;
        for(Map<String,Object> c:customers){
            if((Integer)c.get("id")==customerId){target=c;break;}
        }
        if(target==null) throw new IllegalArgumentException("客户不存在！");

        //退住记录
        List<Map<String,Object>> backdowns=CliUtils.loadList(BACKDOWN_FILE);
        int newId=backdowns.isEmpty()?1
            :(Integer)backdowns.get(backdowns.size()-1).get("id")+1;
        Map<String,Object> record=new LinkedHashMap<>();
        record.put("id",newId);
        record.put("customer_id",customerId);
        record.put("customer_name",target.get("customer_name"));
        record.put("backdown_date",backdownDate);
        record.put("backdown_type",type);
        record.put("death_date",deathDate);
        record.put("create_time",LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        backdowns.add(record);
        CliUtils.saveList(BACKDOWN_FILE,backdowns);

        //处理床位
        if(type!=3){
            int bedId=(Integer)target.get("bed_id");
            updateBedStatus(bedId,1);
            //更新bed_details end_date
            List<Map<String,Object>> details=CliUtils.loadList(BED_DETAILS_FILE);
            for(Map<String,Object> d:details){
                if(d.get("customer_id")!=null&&(Integer)d.get("customer_id")==customerId
                        &&d.get("end_date")==null){
                    d.put("end_date",backdownDate);
                    break;
                }
            }
            CliUtils.saveList(BED_DETAILS_FILE,details);
        }
        target.put("is_deleted",1);
        CliUtils.saveList(CUSTOMERS_FILE,customers);
    }

    //====================外出相关====================

    public static void outwardRegister(int customerId,String outwardDate,
            String returnDate,String reason) throws Exception{
        LocalDate out=LocalDate.parse(outwardDate,DATE_FMT);
        LocalDate back=LocalDate.parse(returnDate,DATE_FMT);
        if(back.isBefore(out)){
            throw new IllegalArgumentException("回院时间不能小于外出时间！");
        }

        Map<String,Object> customer=null;
        List<Map<String,Object>> customers=CliUtils.loadList(CUSTOMERS_FILE);
        for(Map<String,Object> c:customers){
            if((Integer)c.get("id")==customerId&&(Integer)c.get("is_deleted")==0){
                customer=c;break;
            }
        }
        if(customer==null) throw new IllegalArgumentException("客户不存在或已退住！");

        List<Map<String,Object>> outwards=CliUtils.loadList(OUTWARD_FILE);
        int newId=outwards.isEmpty()?1
            :(Integer)outwards.get(outwards.size()-1).get("id")+1;
        Map<String,Object> record=new LinkedHashMap<>();
        record.put("id",newId);
        record.put("customer_id",customerId);
        record.put("customer_name",customer.get("customer_name"));
        record.put("outward_date",outwardDate);
        record.put("return_date",returnDate);
        record.put("reason",reason);
        record.put("status",1);  //1=已通过
        record.put("create_time",LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        outwards.add(record);
        CliUtils.saveList(OUTWARD_FILE,outwards);

        int bedId=(Integer)customer.get("bed_id");
        updateBedStatus(bedId,3);
    }

    //====================查询====================

    public static String getBedNo(int bedId) throws Exception{
        List<Map<String,Object>> beds=CliUtils.loadList(BEDS_FILE);
        for(Map<String,Object> b:beds){
            if((Integer)b.get("id")==bedId) return (String)b.get("bed_no");
        }
        return "未知";
    }

    public static String getBackdownTypeText(int type){
        return switch(type){
            case 1->"正常退住";
            case 2->"死亡退住";
            case 3->"保留床位";
            default->"未知";
        };
    }

    //====================工具====================

    public static void updateBedStatus(int bedId,int status) throws Exception{
        List<Map<String,Object>> beds=CliUtils.loadList(BEDS_FILE);
        for(Map<String,Object> b:beds){
            if((Integer)b.get("id")==bedId){
                b.put("bed_status",status);
                break;
            }
        }
        CliUtils.saveList(BEDS_FILE,beds);
    }
}