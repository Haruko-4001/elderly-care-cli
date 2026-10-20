package com.neusoft.yiyang.cli;

import com.neusoft.yiyang.service.BedService;
import com.neusoft.yiyang.service.CustomerService;
import com.neusoft.yiyang.service.NurseService;
import java.time.*;
import java.time.format.*;
import java.util.*;

/**
*系统管理员菜单-命令行交互
*JSON字段统一snake_case，与PDF数据库设计一致
*/
public class AdminCli{

    public static void start(Map<String,Object> user){
        String nickname=(String)user.get("nickname");
        System.out.println("\n欢迎您，"+nickname+"！您以「系统管理员」身份登录。");

        outer:
        while(true){
            switch(CliUtils.showMenu("管理员主菜单",new String[]{
                "客户管理","床位管理","护理管理","健康管家","用户管理"
            })){
                case 1->customerMenu();
                case 2->bedMenu();
                case 3->nurseMenu();
                case 4->stewardMenu();
                case 5->userMenu();
                case 0->{break outer;}
                default->System.out.println("⚠ 无效选择！");
            }
        }
    }

    //====================1.客户管理====================
    static void customerMenu(){
        while(true){
            switch(CliUtils.showMenu("客户管理",new String[]{
                "入住登记","退住登记","外出登记"
            })){
                case 1->doCheckin();
                case 2->doCheckout();
                case 3->doOutward();
                case 0->{return;}
                default->System.out.println("⚠ 无效选择！");
            }
        }
    }

    //----------入住登记----------
    static void doCheckin(){
        System.out.println("\n══════ 入住登记 ══════");
        System.out.print("老人姓名: ");
        String name=CliUtils.SC.nextLine().trim();
        if(name.isEmpty()){System.out.println("❌ 姓名不能为空！");return;}

        System.out.print("出生日期 (yyyy-MM-dd): ");
        String birthday=CliUtils.SC.nextLine().trim();
        int age;
        try{age=CustomerService.calcAge(birthday);}
        catch(Exception e){System.out.println("❌ 日期格式错误！");return;}
        System.out.println("   计算年龄: "+age+" 岁");

        System.out.print("性别 (0:男 1:女): ");
        int sex=CliUtils.readInt();
        if(sex!=0&&sex!=1){System.out.println("❌ 请输入0或1！");return;}

        System.out.print("身份证号: ");
        String idcard=CliUtils.SC.nextLine().trim();
        System.out.print("血型 (A/B/AB/O): ");
        String bloodType=CliUtils.SC.nextLine().trim();
        System.out.print("家属姓名: ");
        String familyMember=CliUtils.SC.nextLine().trim();
        System.out.print("联系电话: ");
        String contactTel=CliUtils.SC.nextLine().trim();

        try{
            Map<String,List<Integer>> roomsByFloor=CustomerService.getRoomsByFloor();
            System.out.println("\n--- 选择楼层 ---");
            List<String> floors=new ArrayList<>(roomsByFloor.keySet());
            for(int i=0;i<floors.size();i++)
                System.out.printf("  %d. %s层%n",i+1,floors.get(i));
            System.out.print("请选择楼层: ");
            int fIdx=CliUtils.readInt()-1;
            if(fIdx<0||fIdx>=floors.size()){System.out.println("❌ 楼层选择无效！");return;}

            System.out.println("\n--- 选择房间 ---");
            List<Integer> roomNos=roomsByFloor.get(floors.get(fIdx));
            for(int i=0;i<roomNos.size();i++)
                System.out.printf("  %d. %d%n",i+1,roomNos.get(i));
            System.out.print("请选择房间: ");
            int rIdx=CliUtils.readInt()-1;
            if(rIdx<0||rIdx>=roomNos.size()){System.out.println("❌ 房间选择无效！");return;}
            int selectedRoom=roomNos.get(rIdx);

            List<Map<String,Object>> freeBeds=CustomerService.getFreeBeds(selectedRoom);
            if(freeBeds.isEmpty()){System.out.println("❌ 该房间没有空闲床位！");return;}
            System.out.println("\n--- 选择床位 ---");
            for(int i=0;i<freeBeds.size();i++)
                System.out.printf("  %d. %s%n",i+1,freeBeds.get(i).get("bed_no"));
            System.out.print("请选择床位: ");
            int bIdx=CliUtils.readInt()-1;
            if(bIdx<0||bIdx>=freeBeds.size()){System.out.println("❌ 床位选择无效！");return;}
            int bedId=(Integer)freeBeds.get(bIdx).get("id");
            String bedNo=(String)freeBeds.get(bIdx).get("bed_no");

            System.out.println("\n--- 合同信息 ---");
            System.out.print("入住日期 (yyyy-MM-dd): ");
            String checkinDate=CliUtils.SC.nextLine().trim();
            System.out.print("合同到期日 (yyyy-MM-dd): ");
            String expirationDate=CliUtils.SC.nextLine().trim();

            System.out.println("\n══════ 请确认 ══════");
            System.out.printf("  姓名: %s  年龄: %d  性别: %s%n",name,age,sex==0?"男":"女");
            System.out.printf("  身份证: %s  血型: %s%n",idcard,bloodType);
            System.out.printf("  家属: %s  电话: %s%n",familyMember,contactTel);
            System.out.printf("  房间: %d  床位: %s%n",selectedRoom,bedNo);
            System.out.printf("  入住: %s  到期: %s%n",checkinDate,expirationDate);
            System.out.print("\n确认提交？(y/n): ");
            if(!CliUtils.SC.nextLine().trim().equalsIgnoreCase("y")){
                System.out.println("已取消。");return;
            }

            Map<String,Object> customer=new LinkedHashMap<>();
            customer.put("customer_name",name);
            customer.put("customer_age",age);
            customer.put("customer_sex",sex);
            customer.put("idcard",idcard);
            customer.put("blood_type",bloodType);
            customer.put("family_member",familyMember);
            customer.put("contact_tel",contactTel);
            customer.put("building_no","606");
            customer.put("room_no",String.valueOf(selectedRoom));
            customer.put("bed_id",bedId);
            customer.put("checkin_date",checkinDate);
            customer.put("expiration_date",expirationDate);
            customer.put("birthday",birthday);

            CustomerService.checkin(customer);
            System.out.println("\n✅ 入住登记成功！"+name+" → "+selectedRoom+"房间 "+bedNo);

        }catch(Exception e){System.out.println("❌ "+e.getMessage());}
        CliUtils.pause();
    }

    //----------退住登记----------
    static void doCheckout(){
        System.out.println("\n══════ 退住登记 ══════");
        try{
            List<Map<String,Object>> customers=CustomerService.getActiveCustomers();
            if(customers.isEmpty()){
                System.out.println("当前没有在住客户。");CliUtils.pause();return;
            }

            System.out.println("\n--- 在住客户列表 ---");
            CliUtils.printTableHeader("ID","姓名","房间","床位","入住日期");
            for(Map<String,Object> c:customers){
                int bedId=(Integer)c.get("bed_id");
                CliUtils.printTableRow(
                    c.get("id"),c.get("customer_name"),
                    c.get("room_no"),CustomerService.getBedNo(bedId),
                    c.get("checkin_date"));
            }

            System.out.print("\n请输入要退住的客户ID (0:取消): ");
            int cid=CliUtils.readInt();
            if(cid==0)return;

            Map<String,Object> target=null;
            for(Map<String,Object> c:customers){
                if((Integer)c.get("id")==cid){target=c;break;}
            }
            if(target==null){System.out.println("❌ 客户ID无效！");CliUtils.pause();return;}

            int bedId=(Integer)target.get("bed_id");
            System.out.println("\n--- 客户信息 ---");
            System.out.printf("  姓名: %s  房间: %s  床位: %s  入住: %s%n",
                target.get("customer_name"),target.get("room_no"),
                CustomerService.getBedNo(bedId),target.get("checkin_date"));

            System.out.println("\n退住类型:");
            System.out.println("  1.正常退住  2.死亡退住  3.保留床位");
            System.out.print("请选择: ");
            int type=CliUtils.readInt();
            if(type<1||type>3){System.out.println("❌ 选择无效！");CliUtils.pause();return;}

            System.out.print("退住日期 (yyyy-MM-dd): ");
            String backdownDate=CliUtils.SC.nextLine().trim();

            String deathDate=null;
            if(type==2){
                System.out.print("死亡时间 (yyyy-MM-dd HH:mm): ");
                deathDate=CliUtils.SC.nextLine().trim();
            }

            System.out.println("\n══════ 请确认 ══════");
            System.out.printf("  客户: %s  类型: %s%n",
                target.get("customer_name"),CustomerService.getBackdownTypeText(type));
            System.out.printf("  退住日期: %s%n",backdownDate);
            if(type==2) System.out.printf("  死亡时间: %s%n",deathDate);
            if(type!=3) System.out.println("  ⚠ 床位将被释放");
            else System.out.println("  ℹ 床位将保留");

            System.out.print("\n确认退住？(y/n): ");
            if(!CliUtils.SC.nextLine().trim().equalsIgnoreCase("y")){
                System.out.println("已取消。");return;
            }

            CustomerService.checkout(cid,type,backdownDate,deathDate);
            System.out.println("\n✅ 退住登记成功！");

        }catch(Exception e){System.out.println("❌ "+e.getMessage());}
        CliUtils.pause();
    }

    //----------外出登记----------
    static void doOutward(){
        System.out.println("\n══════ 外出登记 ══════");
        try{
            List<Map<String,Object>> customers=CustomerService.getActiveCustomers();
            if(customers.isEmpty()){
                System.out.println("当前没有在住客户。");CliUtils.pause();return;
            }

            System.out.println("\n--- 在住客户列表 ---");
            CliUtils.printTableHeader("ID","姓名","房间","床位","状态");
            for(Map<String,Object> c:customers){
                int bedId=(Integer)c.get("bed_id");
                CliUtils.printTableRow(
                    c.get("id"),c.get("customer_name"),
                    c.get("room_no"),CustomerService.getBedNo(bedId),"在住");
            }

            System.out.print("\n请输入外出客户ID (0:取消): ");
            int cid=CliUtils.readInt();
            if(cid==0)return;

            Map<String,Object> target=null;
            for(Map<String,Object> c:customers){
                if((Integer)c.get("id")==cid){target=c;break;}
            }
            if(target==null){System.out.println("❌ 客户ID无效！");CliUtils.pause();return;}

            System.out.println("\n--- 客户: "+target.get("customer_name")+" ---");
            System.out.print("外出日期 (yyyy-MM-dd): ");
            String outwardDate=CliUtils.SC.nextLine().trim();
            System.out.print("预计回院日期 (yyyy-MM-dd): ");
            String returnDate=CliUtils.SC.nextLine().trim();
            System.out.print("外出事由: ");
            String reason=CliUtils.SC.nextLine().trim();

            System.out.println("\n══════ 请确认 ══════");
            System.out.printf("  客户: %s%n",target.get("customer_name"));
            System.out.printf("  外出: %s → 回院: %s%n",outwardDate,returnDate);
            System.out.printf("  事由: %s%n",reason);

            System.out.print("\n确认提交？(y/n): ");
            if(!CliUtils.SC.nextLine().trim().equalsIgnoreCase("y")){
                System.out.println("已取消。");return;
            }

            CustomerService.outwardRegister(cid,outwardDate,returnDate,reason);
            System.out.println("\n✅ 外出登记成功！床位已标记为「外出」状态");

        }catch(Exception e){System.out.println("❌ "+e.getMessage());}
        CliUtils.pause();
    }

    //====================2.床位管理====================
    static void bedMenu(){
        while(true){
            switch(CliUtils.showMenu("床位管理",new String[]{
                "床位示意图","床位管理（详情/调换）"
            })){
                case 1->bedOverview();
                case 2->bedDetail();
                case 0->{return;}
                default->System.out.println("⚠ 无效选择！");
            }
        }
    }

    static void bedOverview(){
        System.out.println("\n══════ 床位示意图 (606栋) ══════");
        try{
            List<Map<String,Object>> stats=BedService.getBedStatsByRoom();
            String currentFloor="";
            for(Map<String,Object> s:stats){
                String floor=(String)s.get("floor");
                if(!floor.equals(currentFloor)){
                    currentFloor=floor;
                    System.out.println("\n▸ "+floor+"F");
                    CliUtils.printTableHeader("房间号","总床位","空闲","有人","外出");
                }
                CliUtils.printTableRow(
                    s.get("room_no"),s.get("total"),
                    s.get("free"),s.get("occupied"),s.get("outgoing"));
            }
        }catch(Exception e){System.out.println("❌ "+e.getMessage());}
        CliUtils.pause();
    }

    static void bedDetail(){
        while(true){
            switch(CliUtils.showMenu("床位管理",new String[]{
                "查看所有床位详情","调换床位"
            })){
                case 1->showBedDetails();
                case 2->doSwapBed();
                case 0->{return;}
                default->System.out.println("⚠ 无效选择！");
            }
        }
    }

    static void showBedDetails(){
        System.out.println("\n══════ 全部床位详情 ══════");
        try{
            List<Map<String,Object>> beds=BedService.getAllBedsWithStatus();
            CliUtils.printTableHeader("床位号","房间","状态","入住人");
            for(Map<String,Object> b:beds){
                CliUtils.printTableRow(
                    b.get("bed_no"),b.get("room_no"),
                    b.get("_status_name"),b.get("_customer_name"));
            }
        }catch(Exception e){System.out.println("❌ "+e.getMessage());}
        CliUtils.pause();
    }

    static void doSwapBed(){
        System.out.println("\n══════ 床位调换 ══════");
        System.out.println("⚠ 调换要求：两个床位必须都是「有人」状态");
        try{
            List<Map<String,Object>> beds=BedService.getAllBedsWithStatus();
            List<Map<String,Object>> occupiedBeds=new ArrayList<>();
            for(Map<String,Object> b:beds){
                if((Integer)b.get("bed_status")==2) occupiedBeds.add(b);
            }

            if(occupiedBeds.size()<2){
                System.out.println("❌ 需要至少2个有人的床位才能调换！");
                CliUtils.pause();return;
            }

            System.out.println("\n--- 可调换的床位 ---");
            CliUtils.printTableHeader("床位ID","床位号","房间","入住人");
            for(Map<String,Object> b:occupiedBeds){
                CliUtils.printTableRow(
                    b.get("id"),b.get("bed_no"),
                    b.get("room_no"),b.get("_customer_name"));
            }

            System.out.print("\n请输入床位A的ID: ");
            int id1=CliUtils.readInt();
            System.out.print("请输入床位B的ID: ");
            int id2=CliUtils.readInt();

            BedService.swapBeds(id1,id2);
            System.out.println("\n✅ 床位调换成功！两位客户的床位已互换");

        }catch(Exception e){System.out.println("❌ "+e.getMessage());}
        CliUtils.pause();
    }

    //====================3.护理管理====================
    static void nurseMenu(){
        while(true){
            switch(CliUtils.showMenu("护理管理",new String[]{
                "护理级别管理","护理项目管理","客户护理设置","护理记录"
            })){
                case 1->levelManage();
                case 2->contentManage();
                case 3->customerNurseSet();
                case 4->viewRecords();
                case 0->{return;}
                default->System.out.println("⚠ 无效选择！");
            }
        }
    }

    static void levelManage(){
        System.out.println("\n══════ 护理级别管理 ══════");
        try{
            List<Map<String,Object>> levels=NurseService.getActiveLevels();
            CliUtils.printTableHeader("ID","级别名称","状态");
            for(Map<String,Object> l:levels){
                CliUtils.printTableRow(l.get("id"),l.get("level_name"),
                    (Integer)l.get("level_status")==1?"启用":"停用");
            }

            System.out.println("\n操作:  1.启用/停用级别  2.配置护理项目  0.返回");
            System.out.print("请选择: ");
            int op=CliUtils.readInt();
            if(op==0)return;

            System.out.print("请输入护理级别ID: ");
            int lid=CliUtils.readInt();

            if(op==1){
                NurseService.toggleLevelStatus(lid);
                System.out.println("✅ 级别状态已切换！");
            }else if(op==2){
                System.out.println("\n--- 当前关联的护理项目 ---");
                List<Map<String,Object>> items=NurseService.getLevelItems(lid);
                CliUtils.printTableHeader("关系ID","编号","项目名称","价格");
                for(Map<String,Object> it:items){
                    CliUtils.printTableRow(it.get("relation_id"),it.get("serial_number"),
                        it.get("nursing_name"),it.get("service_price"));
                }

                System.out.println("\n操作: 1.添加项目  2.移除项目  0.返回");
                System.out.print("请选择: ");
                int aop=CliUtils.readInt();
                if(aop==1){
                    List<Map<String,Object>> contents=NurseService.getActiveContents();
                    System.out.println("\n--- 可选护理项目 ---");
                    CliUtils.printTableHeader("ID","编号","名称","价格");
                    for(Map<String,Object> c:contents){
                        CliUtils.printTableRow(c.get("id"),c.get("serial_number"),
                            c.get("nursing_name"),c.get("service_price"));
                    }
                    System.out.print("\n请输入要添加的项目ID: ");
                    NurseService.addLevelItem(lid,CliUtils.readInt());
                    System.out.println("✅ 项目已关联到该级别！");
                }else if(aop==2){
                    System.out.print("请输入要移除的关系ID: ");
                    NurseService.removeLevelItem(CliUtils.readInt());
                    System.out.println("✅ 项目已移除！");
                }
            }
        }catch(Exception e){System.out.println("❌ "+e.getMessage());}
        CliUtils.pause();
    }

    static void contentManage(){
        System.out.println("\n══════ 护理项目管理 ══════");
        try{
            List<Map<String,Object>> contents=NurseService.getActiveContents();
            CliUtils.printTableHeader("ID","编号","名称","价格","周期","次数/天");
            for(Map<String,Object> c:contents){
                CliUtils.printTableRow(c.get("id"),c.get("serial_number"),
                    c.get("nursing_name"),c.get("service_price"),
                    c.get("execution_cycle"),c.get("execution_times"));
            }

            System.out.println("\n操作: 1.新增  2.编辑  3.删除  0.返回");
            System.out.print("请选择: ");
            int op=CliUtils.readInt();
            if(op==0)return;

            if(op==1){
                Map<String,Object> content=new LinkedHashMap<>();
                System.out.print("编号 (如NC007): ");
                content.put("serial_number",CliUtils.SC.nextLine().trim());
                System.out.print("项目名称: ");
                content.put("nursing_name",CliUtils.SC.nextLine().trim());
                System.out.print("服务价格: ");
                content.put("service_price",CliUtils.SC.nextLine().trim());
                System.out.print("说明: ");
                content.put("message",CliUtils.SC.nextLine().trim());
                content.put("status",1);
                System.out.print("执行周期 (如:每天/每周): ");
                content.put("execution_cycle",CliUtils.SC.nextLine().trim());
                System.out.print("执行次数/天: ");
                content.put("execution_times",CliUtils.SC.nextLine().trim());
                NurseService.addContent(content);
                System.out.println("✅ 护理项目已添加！");
            }else{
                System.out.print("请输入项目ID: ");
                int cid=CliUtils.readInt();
                if(op==2){
                    Map<String,Object> updates=new LinkedHashMap<>();
                    System.out.print("新名称 (回车跳过): ");
                    String v=CliUtils.SC.nextLine().trim();
                    if(!v.isEmpty()) updates.put("nursing_name",v);
                    System.out.print("新价格 (回车跳过): ");
                    v=CliUtils.SC.nextLine().trim();
                    if(!v.isEmpty()) updates.put("service_price",v);
                    System.out.print("新执行周期 (回车跳过): ");
                    v=CliUtils.SC.nextLine().trim();
                    if(!v.isEmpty()) updates.put("execution_cycle",v);
                    if(!updates.isEmpty()){
                        NurseService.updateContent(cid,updates);
                        System.out.println("✅ 更新成功！");
                    }
                }else if(op==3){
                    System.out.print("确认删除？(y/n): ");
                    if(CliUtils.SC.nextLine().trim().equalsIgnoreCase("y")){
                        NurseService.deleteContent(cid);
                        System.out.println("✅ 已删除！");
                    }
                }
            }
        }catch(Exception e){System.out.println("❌ "+e.getMessage());}
        CliUtils.pause();
    }

    static void customerNurseSet(){
        System.out.println("\n══════ 客户护理设置 ══════");
        try{
            List<Map<String,Object>> customers=CustomerService.getActiveCustomers();
            if(customers.isEmpty()){
                System.out.println("当前没有在住客户。");CliUtils.pause();return;
            }

            System.out.println("\n--- 在住客户 ---");
            CliUtils.printTableHeader("ID","姓名","房间","护理级别");
            for(Map<String,Object> c:customers){
                Object lid=c.get("level_id");
                String levelName=(lid==null||(Integer)lid==-1)
                    ?"未设置":NurseService.getLevelName((Integer)lid);
                CliUtils.printTableRow(c.get("id"),c.get("customer_name"),
                    c.get("room_no"),levelName);
            }

            System.out.println("\n操作: 1.设置护理级别  2.移除护理级别  0.返回");
            System.out.print("请选择: ");
            int op=CliUtils.readInt();
            if(op==0)return;

            System.out.print("请输入客户ID: ");
            int cid=CliUtils.readInt();

            if(op==1){
                System.out.println("\n--- 可选护理级别 ---");
                List<Map<String,Object>> levels=NurseService.getActiveLevels();
                for(Map<String,Object> l:levels){
                    System.out.printf("  %d. %s (%s)%n",l.get("id"),l.get("level_name"),
                        (Integer)l.get("level_status")==1?"启用":"停用");
                }
                System.out.print("请选择级别ID: ");
                NurseService.setCustomerLevel(cid,CliUtils.readInt());
                System.out.println("✅ 护理级别设置成功！");
            }else if(op==2){
                NurseService.removeCustomerLevel(cid);
                System.out.println("✅ 护理级别已移除！");
            }
        }catch(Exception e){System.out.println("❌ "+e.getMessage());}
        CliUtils.pause();
    }

    static void viewRecords(){
        System.out.println("\n══════ 护理记录 ══════");
        try{
            List<Map<String,Object>> records=NurseService.getRecords();
            if(records.isEmpty()){
                System.out.println("暂无护理记录。");CliUtils.pause();return;
            }

            CliUtils.printTableHeader("ID","客户","护理内容","记录人","时间");
            for(Map<String,Object> r:records){
                String content=r.get("content_name")!=null
                    ?String.valueOf(r.get("content_name")):"—";
                CliUtils.printTableRow(r.get("id"),r.get("customer_name"),
                    content,r.get("steward_name"),r.get("nurse_time"));
            }

            System.out.println("\n操作: 1.移除记录  0.返回");
            System.out.print("请选择: ");
            if(CliUtils.readInt()==1){
                System.out.print("请输入记录ID: ");
                NurseService.removeRecord(CliUtils.readInt());
                System.out.println("✅ 记录已移除！");
            }
        }catch(Exception e){System.out.println("❌ "+e.getMessage());}
        CliUtils.pause();
    }

    //====================4.健康管家管理====================
    static void stewardMenu(){
        while(true){
            switch(CliUtils.showMenu("健康管家管理",new String[]{
                "设置服务对象","服务关注"
            })){
                case 1->assignSteward();
                case 2->serviceAttention();
                case 0->{return;}
                default->System.out.println("⚠ 无效选择！");
            }
        }
    }

    static void assignSteward(){
        System.out.println("\n══════ 设置服务对象 ══════");
        try{
            List<Map<String,Object>> users=CliUtils.loadList("data/users.json");
            System.out.println("\n--- 健康管家列表 ---");
            for(Map<String,Object> u:users){
                if((Integer)u.get("role_id")==2&&(Integer)u.get("is_deleted")==0){
                    System.out.printf("  %d. %s (%s)%n",
                        u.get("id"),u.get("nickname"),u.get("username"));
                }
            }
            System.out.print("\n请选择健康管家ID: ");
            int stewardId=CliUtils.readInt();

            List<Map<String,Object>> customers=CustomerService.getActiveCustomers();
            System.out.println("\n--- 在住客户 ---");
            CliUtils.printTableHeader("ID","姓名","房间","当前管家");
            for(Map<String,Object> c:customers){
                Object uid=c.get("user_id");
                String stewardName="未分配";
                if(uid!=null&&(Integer)uid!=-1){
                    for(Map<String,Object> u:users){
                        if((Integer)u.get("id")==(Integer)uid){
                            stewardName=(String)u.get("nickname");break;
                        }
                    }
                }
                CliUtils.printTableRow(c.get("id"),c.get("customer_name"),
                    c.get("room_no"),stewardName);
            }

            System.out.print("\n请输入要分配的客户ID (0:取消): ");
            int cid=CliUtils.readInt();
            if(cid==0)return;

            List<Map<String,Object>> allCustomers=CliUtils.loadList("data/customers.json");
            for(Map<String,Object> c:allCustomers){
                if((Integer)c.get("id")==cid){
                    c.put("user_id",stewardId);
                    break;
                }
            }
            CliUtils.saveList("data/customers.json",allCustomers);
            System.out.println("✅ 服务对象分配成功！");

        }catch(Exception e){System.out.println("❌ "+e.getMessage());}
        CliUtils.pause();
    }

    static void serviceAttention(){
        System.out.println("\n══════ 服务关注 ══════");
        try{
            List<Map<String,Object>> customers=CustomerService.getActiveCustomers();
            if(customers.isEmpty()){
                System.out.println("当前没有在住客户。");CliUtils.pause();return;
            }

            System.out.println("\n--- 在住客户 ---");
            for(int i=0;i<customers.size();i++){
                Map<String,Object> c=customers.get(i);
                Object lid=c.get("level_id");
                String levelName=(lid==null||(Integer)lid==-1)
                    ?"未设置":NurseService.getLevelName((Integer)lid);
                System.out.printf("  %d. %s (%s房间) [%s]%n",
                    i+1,c.get("customer_name"),c.get("room_no"),levelName);
            }

            System.out.print("\n请选择客户序号查看详情 (0:返回): ");
            int idx=CliUtils.readInt()-1;
            if(idx<0)return;
            if(idx>=customers.size()){
                System.out.println("❌ 选择无效！");CliUtils.pause();return;
            }

            Map<String,Object> customer=customers.get(idx);
            Object lid=customer.get("level_id");

            System.out.println("\n--- "+customer.get("customer_name")+" 的护理服务 ---");
            if(lid==null||(Integer)lid==-1){
                System.out.println("该客户未设置护理级别。");
            }else{
                System.out.println("护理级别: "+NurseService.getLevelName((Integer)lid));
                System.out.println();

                List<Map<String,Object>> items=NurseService.getLevelItems((Integer)lid);
                CliUtils.printTableHeader("编号","项目名称","价格","周期","次数/天");
                for(Map<String,Object> it:items){
                    List<Map<String,Object>> contents=CliUtils.loadList("data/nurse_contents.json");
                    Map<String,Object> full=null;
                    for(Map<String,Object> c:contents){
                        if((Integer)c.get("id")==(Integer)it.get("item_id")){
                            full=c;break;
                        }
                    }
                    if(full!=null){
                        CliUtils.printTableRow(full.get("serial_number"),
                            it.get("nursing_name"),it.get("service_price"),
                            full.get("execution_cycle"),full.get("execution_times"));
                    }
                }
            }
        }catch(Exception e){System.out.println("❌ "+e.getMessage());}
        CliUtils.pause();
    }

    //====================5.用户管理====================
    static void userMenu(){
        while(true){
            switch(CliUtils.showMenu("用户管理",new String[]{
                "查看用户列表","添加用户","修改用户角色"
            })){
                case 1->viewUsers();
                case 2->addUser();
                case 3->changeRole();
                case 0->{return;}
                default->System.out.println("⚠ 无效选择！");
            }
        }
    }

    static void viewUsers(){
        System.out.println("\n══════ 用户列表 ══════");
        try{
            List<Map<String,Object>> users=CliUtils.loadList("data/users.json");
            CliUtils.printTableHeader("ID","账号","昵称","角色","电话","状态");
            for(Map<String,Object> u:users){
                int roleId=(Integer)u.get("role_id");
                int deleted=(Integer)u.get("is_deleted");
                CliUtils.printTableRow(u.get("id"),u.get("username"),
                    u.get("nickname"),roleId==1?"管理员":"健康管家",
                    u.get("phone_number"),deleted==0?"正常":"已删除");
            }
        }catch(Exception e){System.out.println("❌ "+e.getMessage());}
        CliUtils.pause();
    }

    static void addUser(){
        System.out.println("\n══════ 添加用户 ══════");
        try{
            Map<String,Object> newUser=new LinkedHashMap<>();
            System.out.print("账号: ");
            newUser.put("username",CliUtils.SC.nextLine().trim());
            System.out.print("密码: ");
            newUser.put("password",CliUtils.SC.nextLine().trim());
            System.out.print("昵称: ");
            newUser.put("nickname",CliUtils.SC.nextLine().trim());
            System.out.print("性别 (0:男 1:女): ");
            newUser.put("sex",CliUtils.readInt());
            System.out.print("电话: ");
            newUser.put("phone_number",CliUtils.SC.nextLine().trim());
            System.out.print("角色 (1:管理员 2:健康管家): ");
            newUser.put("role_id",CliUtils.readInt());
            newUser.put("is_deleted",0);

            List<Map<String,Object>> users=CliUtils.loadList("data/users.json");
            newUser.put("id",users.isEmpty()?1
                :(Integer)users.get(users.size()-1).get("id")+1);
            users.add(newUser);
            CliUtils.saveList("data/users.json",users);
            System.out.println("✅ 用户添加成功！");

        }catch(Exception e){System.out.println("❌ "+e.getMessage());}
        CliUtils.pause();
    }

    static void changeRole(){
        System.out.println("\n══════ 修改用户角色 ══════");
        try{
            List<Map<String,Object>> users=CliUtils.loadList("data/users.json");
            CliUtils.printTableHeader("ID","账号","昵称","当前角色");
            for(Map<String,Object> u:users){
                if((Integer)u.get("is_deleted")==0){
                    CliUtils.printTableRow(u.get("id"),u.get("username"),
                        u.get("nickname"),
                        (Integer)u.get("role_id")==1?"管理员":"健康管家");
                }
            }

            System.out.print("\n请输入用户ID: ");
            int uid=CliUtils.readInt();
            System.out.print("新角色 (1:管理员 2:健康管家): ");
            int newRole=CliUtils.readInt();
            if(newRole!=1&&newRole!=2){
                System.out.println("❌ 角色无效！");CliUtils.pause();return;
            }

            for(Map<String,Object> u:users){
                if((Integer)u.get("id")==uid){
                    u.put("role_id",newRole);
                    System.out.println("✅ 角色已修改！");
                    break;
                }
            }
            CliUtils.saveList("data/users.json",users);
        }catch(Exception e){System.out.println("❌ "+e.getMessage());}
        CliUtils.pause();
    }
}