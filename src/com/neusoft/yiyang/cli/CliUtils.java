package com.neusoft.yiyang.cli;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.*;
import java.util.*;

/**
*命令行交互工具类
*/
public class CliUtils{
    public static final ObjectMapper MAPPER=new ObjectMapper();
    public static final Scanner SC=new Scanner(System.in);
    public static final String DATA_DIR="data";

    //==========JSON读写==========

    public static List<Map<String,Object>> loadList(String filePath) throws Exception{
        String json=Files.readString(Path.of(filePath));
        return MAPPER.readValue(json,
            new TypeReference<List<Map<String,Object>>>(){});
    }

    public static void saveList(String filePath,List<?> list) throws Exception{
        String json=MAPPER.writerWithDefaultPrettyPrinter()
            .writeValueAsString(list);
        Files.writeString(Path.of(filePath),json);
    }

    //==========界面通用方法==========

    public static void printDivider(){
        System.out.println("────────────────────────────────────");
    }

    /**暂停等待回车*/
    public static void pause(){
        System.out.print("\n按回车键继续...");
        SC.nextLine();
    }

    /**读取整数输入，出错返回-1*/
    public static int readInt(){
        try{
            return Integer.parseInt(SC.nextLine().trim());
        }catch(NumberFormatException e){
            return -1;
        }
    }

    /**打印菜单并返回选择*/
    public static int showMenu(String title,String[] options){
        System.out.println("\n===== "+title+" =====");
        for(int i=0;i<options.length;i++){
            System.out.printf("  %d. %s%n",i+1,options[i]);
        }
        System.out.println("  0. 返回上级");
        System.out.print("请选择: ");
        return readInt();
    }

    /**打印表格头部*/
    public static void printTableHeader(String... headers){
        for(String h:headers){
            System.out.printf("%-20s",h);
        }
        System.out.println();
        printDivider();
    }

    /**打印表格行*/
    public static void printTableRow(Object... cols){
        for(Object c:cols){
            System.out.printf("%-20s",String.valueOf(c!=null?c:""));
        }
        System.out.println();
    }
}