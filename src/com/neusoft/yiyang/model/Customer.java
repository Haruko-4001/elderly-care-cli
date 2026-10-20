package com.neusoft.yiyang.model;

/**
*客户（老人）数据模型
*/
public class Customer{
    private int id;
    private String customerName;
    private int customerAge;
    private int customerSex;     //0:男 1:女
    private String idcard;
    private String roomNo;
    private String buildingNo;   //固定606
    private String checkinDate;
    private String expirationDate;
    private String contactTel;
    private int bedId;
    private String psychosomaticState;
    private String attention;
    private String birthday;
    private String height;
    private String weight;
    private String bloodType;
    private String filepath;
    private int userId;          //关联健康管家，-1=无
    private int levelId;         //护理级别
    private String familyMember;
    private int isDeleted;       //0:显示 1:隐藏

    //床位使用详情（不入库，关联查询用）
    private int bedDetailsId;
    private String startDate;
    private String endDate;
    private String bedNo;

    public Customer(){}

    //入住登记用构造器
    public Customer(String customerName,int customerAge,int customerSex,
                    String idcard,String bloodType,String familyMember,
                    String contactTel,String buildingNo,String roomNo,
                    int bedId,String checkinDate,String expirationDate,
                    String birthday){
        this.customerName=customerName;
        this.customerAge=customerAge;
        this.customerSex=customerSex;
        this.idcard=idcard;
        this.bloodType=bloodType;
        this.familyMember=familyMember;
        this.contactTel=contactTel;
        this.buildingNo=buildingNo;
        this.roomNo=roomNo;
        this.bedId=bedId;
        this.checkinDate=checkinDate;
        this.expirationDate=expirationDate;
        this.birthday=birthday;
        this.isDeleted=0;
        this.userId=-1;
    }

    //=====Getter/Setter=====
    public int getId(){return id;}
    public void setId(int id){this.id=id;}

    public String getCustomerName(){return customerName;}
    public void setCustomerName(String v){this.customerName=v;}

    public int getCustomerAge(){return customerAge;}
    public void setCustomerAge(int v){this.customerAge=v;}

    public int getCustomerSex(){return customerSex;}
    public void setCustomerSex(int v){this.customerSex=v;}

    public String getIdcard(){return idcard;}
    public void setIdcard(String v){this.idcard=v;}

    public String getRoomNo(){return roomNo;}
    public void setRoomNo(String v){this.roomNo=v;}

    public String getBuildingNo(){return buildingNo;}
    public void setBuildingNo(String v){this.buildingNo=v;}

    public String getCheckinDate(){return checkinDate;}
    public void setCheckinDate(String v){this.checkinDate=v;}

    public String getExpirationDate(){return expirationDate;}
    public void setExpirationDate(String v){this.expirationDate=v;}

    public String getContactTel(){return contactTel;}
    public void setContactTel(String v){this.contactTel=v;}

    public int getBedId(){return bedId;}
    public void setBedId(int v){this.bedId=v;}

    public String getBloodType(){return bloodType;}
    public void setBloodType(String v){this.bloodType=v;}

    public int getUserId(){return userId;}
    public void setUserId(int v){this.userId=v;}

    public int getLevelId(){return levelId;}
    public void setLevelId(int v){this.levelId=v;}

    public String getFamilyMember(){return familyMember;}
    public void setFamilyMember(String v){this.familyMember=v;}

    public int getIsDeleted(){return isDeleted;}
    public void setIsDeleted(int v){this.isDeleted=v;}

    public String getBirthday(){return birthday;}
    public void setBirthday(String v){this.birthday=v;}

    public String getBedNo(){return bedNo;}
    public void setBedNo(String v){this.bedNo=v;}

    public String getStartDate(){return startDate;}
    public void setStartDate(String v){this.startDate=v;}

    public String getEndDate(){return endDate;}
    public void setEndDate(String v){this.endDate=v;}
}