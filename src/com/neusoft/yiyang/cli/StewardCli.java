package com.neusoft.yiyang.cli;

import com.neusoft.yiyang.service.CustomerService;
import com.neusoft.yiyang.service.NurseService;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
*健康管家（护工）菜单-命令行交互
*JSON字段严格使用snake_case
*/
public class StewardCli{
    static final DateTimeFormatter FM=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void start(Map<String,Object> user){
        String nickname=(String)user.get("nickname");
        System.out.println("\n欢迎您，"+nickname+"！您以「健康管家」身份登录。");

        outer:
        while(true){
            switch(CliUtils.showMenu("健康管家主菜单",new String[]{
                "日常护理","服务对象护理记录","外出申请","退住申请"
            })){
                case 1->dailyNurse(user);
                case 2->myRecords(user);
                case 3->outwardApply(user);
                case 4->backdownApply(user);
                case 0->{break outer;}
                default->System.out.println("⚠ 无效选择！");
            }
        }
    }

    static List<Map<String,Object>> myCustomers(Map<String,Object> user) throws Exception{
        int stewardId=(Integer)user.get("id");
        List<Map<String,Object>> all=CustomerService.getActiveCustomers();
        List<Map<String,Object>> mine=new ArrayList<>();
        for(Map<String,Object> c:all){
            Object uid=c.get("user_id");
            if(uid!=null&&(Integer)uid==stewardId){
                mine.add(c);
            }
        }
        return mine;
    }

    //==========日常护理==========
    static void dailyNurse(Map<String,Object> user){
        System.out.println("\n══════ 日常护理 ══════");
        try{
            List<Map<String,Object>> customers=myCustomers(user);
            if(customers.isEmpty()){
                System.out.println("您当前没有被分配的服务对象。");
                CliUtils.pause();return;
            }

            System.out.println("\n--- 您的服务对象 ---");
            for(int i=0;i<customers.size();i++){
                Map<String,Object> c=customers.get(i);
                Object lid=c.get("level_id");
                String levelName=(lid==null||(Integer)lid==-1)
                    ?"未设置":NurseService.getLevelName((Integer)lid);
                System.out.printf("  %d. %s (房间%s) [%s]%n",
                    i+1,c.get("customer_name"),c.get("room_no"),levelName);
            }

            System.out.print("\n请选择客户序号 (0:返回): ");
            int idx=CliUtils.readInt()-1;
            if(idx<0)return;
            if(idx>=customers.size()){
                System.out.println("❌ 选择无效！");CliUtils.pause();return;
            }

            Map<String,Object> customer=customers.get(idx);
            int customerId=(Integer)customer.get("id");
            Object lid=customer.get("level_id");

            if(lid==null||(Integer)lid==-1){
                System.out.println("❌ 该客户尚未设置护理级别，无法进行护理！");
                CliUtils.pause();return;
            }

            List<Map<String,Object>> items=NurseService.getLevelItems((Integer)lid);
            if(items.isEmpty()){
                System.out.println("❌ 该护理级别下没有配置项目！");
                CliUtils.pause();return;
            }

            System.out.println("\n--- 护理项目 ["+customer.get("customer_name")+"] ---");
            for(int i=0;i<items.size();i++){
                Map<String,Object> it=items.get(i);
                System.out.printf("  %d. %s (¥%s)%n",
                    i+1,it.get("nursing_name"),it.get("service_price"));
            }

            System.out.print("\n请选择要执行的护理项目序号 (0:返回): ");
            int iidx=CliUtils.readInt()-1;
            if(iidx<0)return;
            if(iidx>=items.size()){
                System.out.println("❌ 选择无效！");CliUtils.pause();return;
            }

            Map<String,Object> selectedItem=items.get(iidx);
            List<Map<String,Object>> records=NurseService.getRecords();
            int newId=records.isEmpty()?1
                :(Integer)records.get(records.size()-1).get("id")+1;

            Map<String,Object> record=new LinkedHashMap<>();
            record.put("id",newId);
            record.put("customer_id",customerId);
            record.put("customer_name",customer.get("customer_name"));
            record.put("content_id",selectedItem.get("item_id"));
            record.put("content_name",selectedItem.get("nursing_name"));
            record.put("steward_id",user.get("id"));
            record.put("steward_name",user.get("nickname"));
            record.put("nurse_time",LocalDateTime.now().format(FM));

            records.add(record);
            CliUtils.saveList(NurseService.RECORDS_FILE,records);

            System.out.println("\n✅ 护理记录已生成！");
            System.out.println("   客户: "+customer.get("customer_name"));
            System.out.println("   项目: "+selectedItem.get("nursing_name"));
            System.out.println("   时间: "+record.get("nurse_time"));

        }catch(Exception e){System.out.println("❌ "+e.getMessage());}
        CliUtils.pause();
    }

    //==========护理记录查询==========
    static void myRecords(Map<String,Object> user){
        System.out.println("\n══════ 服务对象护理记录 ══════");
        try{
            int stewardId=(Integer)user.get("id");
            List<Map<String,Object>> records=NurseService.getRecords();
            boolean found=false;

            CliUtils.printTableHeader("ID","客户","护理内容","时间");
            for(Map<String,Object> r:records){
                Object sid=r.get("steward_id");
                if(sid!=null&&(Integer)sid==stewardId){
                    Object cn=r.get("content_name");
                    CliUtils.printTableRow(r.get("id"),r.get("customer_name"),
                        cn!=null?cn:"—",r.get("nurse_time"));
                    found=true;
                }
            }
            if(!found) System.out.println("暂无护理记录。");
        }catch(Exception e){System.out.println("❌ "+e.getMessage());}
        CliUtils.pause();
    }

    //==========外出申请==========
    static void outwardApply(Map<String,Object> user){
        System.out.println("\n══════ 外出申请 ══════");
        try{
            List<Map<String,Object>> customers=myCustomers(user);
            if(customers.isEmpty()){
                System.out.println("您当前没有被分配的服务对象。");CliUtils.pause();return;
            }

            for(int i=0;i<customers.size();i++){
                Map<String,Object> c=customers.get(i);
                System.out.printf("  %d. %s (房间%s)%n",
                    i+1,c.get("customer_name"),c.get("room_no"));
            }

            System.out.print("\n请选择客户序号 (0:返回): ");
            int idx=CliUtils.readInt()-1;
            if(idx<0)return;
            if(idx>=customers.size()){
                System.out.println("❌ 选择无效！");CliUtils.pause();return;
            }

            Map<String,Object> customer=customers.get(idx);
            System.out.println("\n客户: "+customer.get("customer_name"));
            System.out.print("外出日期 (yyyy-MM-dd): ");
            String outDate=CliUtils.SC.nextLine().trim();
            System.out.print("预计回院日期 (yyyy-MM-dd): ");
            String returnDate=CliUtils.SC.nextLine().trim();
            System.out.print("外出事由: ");
            String reason=CliUtils.SC.nextLine().trim();

            CustomerService.outwardRegister((Integer)customer.get("id"),
                outDate,returnDate,reason);
            System.out.println("\n✅ 外出申请已提交，床位已标记为「外出」状态");

        }catch(Exception e){System.out.println("❌ "+e.getMessage());}
        CliUtils.pause();
    }

    //==========退住申请==========
    static void backdownApply(Map<String,Object> user){
        System.out.println("\n══════ 退住申请 ══════");
        try{
            List<Map<String,Object>> customers=myCustomers(user);
            if(customers.isEmpty()){
                System.out.println("您当前没有被分配的服务对象。");CliUtils.pause();return;
            }

            for(int i=0;i<customers.size();i++){
                Map<String,Object> c=customers.get(i);
                System.out.printf("  %d. %s (房间%s)%n",
                    i+1,c.get("customer_name"),c.get("room_no"));
            }

            System.out.print("\n请选择客户序号 (0:返回): ");
            int idx=CliUtils.readInt()-1;
            if(idx<0)return;
            if(idx>=customers.size()){
                System.out.println("❌ 选择无效！");CliUtils.pause();return;
            }

            Map<String,Object> customer=customers.get(idx);
            System.out.println("\n客户: "+customer.get("customer_name"));

            System.out.println("\n退住类型:");
            System.out.println("  1.正常退住  2.死亡退住  3.保留床位");
            System.out.print("请选择: ");
            int type=CliUtils.readInt();
            if(type<1||type>3){
                System.out.println("❌ 选择无效！");CliUtils.pause();return;
            }

            System.out.print("退住日期 (yyyy-MM-dd): ");
            String backdownDate=CliUtils.SC.nextLine().trim();
            String deathDate=null;
            if(type==2){
                System.out.print("死亡时间 (yyyy-MM-dd HH:mm): ");
                deathDate=CliUtils.SC.nextLine().trim();
            }

            System.out.println("\n══════ 请确认 ══════");
            System.out.printf("  客户: %s  类型: %s%n",
                customer.get("customer_name"),CustomerService.getBackdownTypeText(type));
            System.out.printf("  退住日期: %s%n",backdownDate);
            System.out.print("\n确认提交？(y/n): ");
            if(!CliUtils.SC.nextLine().trim().equalsIgnoreCase("y")){
                System.out.println("已取消。");return;
            }

            CustomerService.checkout((Integer)customer.get("id"),type,backdownDate,deathDate);
            System.out.println("\n✅ 退住申请已提交！");

        }catch(Exception e){System.out.println("❌ "+e.getMessage());}
        CliUtils.pause();
    }
}