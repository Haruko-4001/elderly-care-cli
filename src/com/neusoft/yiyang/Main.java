package com.neusoft.yiyang;

import com.neusoft.yiyang.cli.*;
import java.util.*;

/**
*东软颐养中心管理系统-主程序入口
*命令行版：前端用命令行代替，数据库用JSON伪数据库代替
*/
public class Main{
    //===配置===
    static final String USERS_FILE=CliUtils.DATA_DIR+"/users.json";

    //===全局状态===
    static Map<String,Object> loginUser=null;

    public static void main(String[] args){
        printBanner();

        while(true){
            System.out.println("\n=====主菜单=====");
            System.out.println("1.管理员登录");
            System.out.println("2.健康管家登录");
            System.out.println("0.退出系统");
            System.out.print("请选择:");

            String choice=CliUtils.SC.nextLine().trim();
            switch(choice){
                case"1"->login(1,"管理员");
                case"2"->login(2,"健康管家");
                case"0"->{
                    System.out.println("感谢使用，再见！");
                    return;
                }
                default->System.out.println("⚠无效选择，请重新输入！");
            }
        }
    }

    //==========登录==========
    static void login(int roleId,String roleName){
        System.out.println("\n---"+roleName+"登录---");
        System.out.print("请输入账号:");
        String username=CliUtils.SC.nextLine().trim();
        System.out.print("请输入密码:");
        String password=CliUtils.SC.nextLine().trim();

        try{
            List<Map<String,Object>> users=CliUtils.loadList(USERS_FILE);
            for(Map<String,Object> user:users){
                String uname=(String)user.get("username");
                String pwd=(String)user.get("password");
                int rid=(Integer)user.get("role_id");
                int del=user.get("is_deleted")!=null
                    ?(Integer)user.get("is_deleted"):0;

                if(uname.equals(username)&&pwd.equals(password)
                        &&rid==roleId&&del==0){
                    loginUser=user;
                    System.out.println("\n✅登录成功！欢迎，"+
                        user.get("nickname")+"（"+roleName+"）");

                    //跳转到对应角色的功能菜单
                    if(roleId==1){
                        AdminCli.start(user);       //管理员菜单
                    }else{
                        StewardCli.start(user);      //健康管家菜单
                    }
                    //退出菜单后回到登录界面
                    loginUser=null;
                    return;
                }
            }
            System.out.println("❌账号或密码错误！");
        }catch(Exception e){
            System.out.println("❌系统错误："+e.getMessage());
        }
    }

    //==========界面==========
    static void printBanner(){
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║      东软颐养中心管理系统        ║");
        System.out.println("║         命令行版v1.0             ║");
        System.out.println("╚══════════════════════════════════╝");
    }
}