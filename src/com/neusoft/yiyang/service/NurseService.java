package com.neusoft.yiyang.service;

import com.neusoft.yiyang.cli.CliUtils;
import java.util.*;

/**
*护理管理业务逻辑
*JSON字段统一snake_case
*/
public class NurseService{
    public static final String LEVELS_FILE=CliUtils.DATA_DIR+"/nurse_levels.json";
    public static final String CONTENTS_FILE=CliUtils.DATA_DIR+"/nurse_contents.json";
    public static final String LEVEL_ITEMS_FILE=CliUtils.DATA_DIR+"/nurse_level_items.json";
    public static final String RECORDS_FILE=CliUtils.DATA_DIR+"/nurse_records.json";
    static final String CUSTOMERS_FILE=CliUtils.DATA_DIR+"/customers.json";

    //====================护理级别====================

    public static List<Map<String,Object>> getActiveLevels() throws Exception{
        List<Map<String,Object>> all=CliUtils.loadList(LEVELS_FILE);
        List<Map<String,Object>> actives=new ArrayList<>();
        for(Map<String,Object> l:all){
            if((Integer)l.get("is_deleted")==0) actives.add(l);
        }
        return actives;
    }

    public static void toggleLevelStatus(int levelId) throws Exception{
        List<Map<String,Object>> levels=CliUtils.loadList(LEVELS_FILE);
        for(Map<String,Object> l:levels){
            if((Integer)l.get("id")==levelId){
                int s=(Integer)l.get("level_status");
                l.put("level_status",s==1?0:1);
                break;
            }
        }
        CliUtils.saveList(LEVELS_FILE,levels);
    }

    public static List<Map<String,Object>> getLevelItems(int levelId) throws Exception{
        List<Map<String,Object>> lis=CliUtils.loadList(LEVEL_ITEMS_FILE);
        List<Map<String,Object>> contents=CliUtils.loadList(CONTENTS_FILE);

        Map<Integer,Map<String,Object>> contentMap=new HashMap<>();
        for(Map<String,Object> c:contents)
            contentMap.put((Integer)c.get("id"),c);

        List<Map<String,Object>> result=new ArrayList<>();
        for(Map<String,Object> li:lis){
            if((Integer)li.get("level_id")==levelId){
                Map<String,Object> item=contentMap.get(li.get("item_id"));
                if(item!=null&&(Integer)item.get("is_deleted")==0){
                    Map<String,Object> row=new LinkedHashMap<>();
                    row.put("relation_id",li.get("id"));
                    row.put("item_id",item.get("id"));
                    row.put("serial_number",item.get("serial_number"));
                    row.put("nursing_name",item.get("nursing_name"));
                    row.put("service_price",item.get("service_price"));
                    result.add(row);
                }
            }
        }
        return result;
    }

    public static void addLevelItem(int levelId,int itemId) throws Exception{
        List<Map<String,Object>> lis=CliUtils.loadList(LEVEL_ITEMS_FILE);
        for(Map<String,Object> li:lis){
            if((Integer)li.get("level_id")==levelId
                    &&(Integer)li.get("item_id")==itemId){
                throw new IllegalArgumentException("该护理级别已关联此项目！");
            }
        }
        int newId=lis.isEmpty()?1
            :(Integer)lis.get(lis.size()-1).get("id")+1;
        Map<String,Object> record=new LinkedHashMap<>();
        record.put("id",newId);
        record.put("level_id",levelId);
        record.put("item_id",itemId);
        lis.add(record);
        CliUtils.saveList(LEVEL_ITEMS_FILE,lis);
    }

    public static void removeLevelItem(int relationId) throws Exception{
        List<Map<String,Object>> lis=CliUtils.loadList(LEVEL_ITEMS_FILE);
        lis.removeIf(li->(Integer)li.get("id")==relationId);
        CliUtils.saveList(LEVEL_ITEMS_FILE,lis);
    }

    //====================护理项目====================

    public static List<Map<String,Object>> getActiveContents() throws Exception{
        List<Map<String,Object>> all=CliUtils.loadList(CONTENTS_FILE);
        List<Map<String,Object>> actives=new ArrayList<>();
        for(Map<String,Object> c:all){
            if((Integer)c.get("is_deleted")==0) actives.add(c);
        }
        return actives;
    }

    public static void addContent(Map<String,Object> content) throws Exception{
        List<Map<String,Object>> contents=CliUtils.loadList(CONTENTS_FILE);
        int newId=contents.isEmpty()?1
            :(Integer)contents.get(contents.size()-1).get("id")+1;
        content.put("id",newId);
        content.put("is_deleted",0);
        contents.add(content);
        CliUtils.saveList(CONTENTS_FILE,contents);
    }

    public static void updateContent(int id,Map<String,Object> updates) throws Exception{
        List<Map<String,Object>> contents=CliUtils.loadList(CONTENTS_FILE);
        for(Map<String,Object> c:contents){
            if((Integer)c.get("id")==id){
                c.putAll(updates);
                break;
            }
        }
        CliUtils.saveList(CONTENTS_FILE,contents);
    }

    public static void deleteContent(int id) throws Exception{
        List<Map<String,Object>> contents=CliUtils.loadList(CONTENTS_FILE);
        for(Map<String,Object> c:contents){
            if((Integer)c.get("id")==id){
                c.put("is_deleted",1);
                break;
            }
        }
        CliUtils.saveList(CONTENTS_FILE,contents);
    }

    //====================客户护理设置====================

    public static void setCustomerLevel(int customerId,int levelId) throws Exception{
        List<Map<String,Object>> customers=CliUtils.loadList(CUSTOMERS_FILE);
        for(Map<String,Object> c:customers){
            if((Integer)c.get("id")==customerId){
                c.put("level_id",levelId);
                break;
            }
        }
        CliUtils.saveList(CUSTOMERS_FILE,customers);
    }

    public static void removeCustomerLevel(int customerId) throws Exception{
        setCustomerLevel(customerId,-1);
    }

    //====================护理记录====================

    public static List<Map<String,Object>> getRecords() throws Exception{
        return CliUtils.loadList(RECORDS_FILE);
    }

    public static void removeRecord(int recordId) throws Exception{
        List<Map<String,Object>> records=CliUtils.loadList(RECORDS_FILE);
        records.removeIf(r->(Integer)r.get("id")==recordId);
        CliUtils.saveList(RECORDS_FILE,records);
    }

    public static String getLevelName(int levelId) throws Exception{
        List<Map<String,Object>> levels=CliUtils.loadList(LEVELS_FILE);
        for(Map<String,Object> l:levels){
            if((Integer)l.get("id")==levelId) return (String)l.get("level_name");
        }
        return "未设置";
    }
}