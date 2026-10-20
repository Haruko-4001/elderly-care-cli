# 东软颐养中心管理系统 · 命令行版

以「东软颐养中心」养老机构为业务背景的**纯 Java 命令行管理系统**：前端以交互式命令行菜单代替，后端不依赖数据库，使用 **JSON 文件作为伪数据库** 完成数据的增删改查与持久化。

项目以面向对象分层结构组织，业务覆盖客户入住／退住／外出、床位布局与调换、护理级别与护理项目、护理记录、健康管家分派与用户权限管理等场景。

## 功能特性

系统提供两类角色，登录后进入各自的菜单：

**系统管理员（role_id = 1）**

| 模块 | 功能 |
| --- | --- |
| 客户管理 | 入住登记、退住登记、外出登记 |
| 床位管理 | 床位示意图、床位详情、调换床位 |
| 护理管理 | 护理级别管理、护理项目管理、客户护理设置、护理记录 |
| 健康管家 | 设置服务对象、服务关注 |
| 用户管理 | 查看用户列表、添加用户、修改用户角色 |

**健康管家／护工（role_id = 2）**

| 模块 | 功能 |
| --- | --- |
| 日常护理 | 为本人服务对象登记护理记录 |
| 服务对象护理记录 | 查看本人负责客户的护理记录 |
| 外出申请 | 提交客户外出申请 |
| 退住申请 | 提交客户退住（退住方式、退住日期）申请 |

业务规则要点：

- 床位状态与入住／退住联动，退住后自动释放床位；
- 床位调换要求双方床位状态合法，避免重复占用；
- 护理级别与护理项目为多对多关系，通过 `nurse_level_items` 关联；
- 所有数据表统一使用 `snake_case` 字段命名，与数据库设计文档保持一致。

## 技术栈

- Java 17+（源码使用 `switch` 表达式箭头语法）
- Jackson 2.15.4（`jackson-core` / `jackson-databind` / `jackson-annotations`），用于 JSON 序列化与反序列化
- JSON 文件伪数据库，数据文件位于 `data/`
- 构建：`javac` + Windows 批处理脚本（无 Maven / Gradle 依赖）

## 目录结构

```
elderly-care-cli/
├── src/com/neusoft/yiyang/
│   ├── Main.java                  # 程序入口、角色登录与主菜单
│   ├── cli/                       # 命令行交互层
│   │   ├── AdminCli.java          # 系统管理员菜单
│   │   ├── StewardCli.java        # 健康管家菜单
│   │   └── CliUtils.java          # JSON 读写、菜单渲染、输入校验等公共工具
│   ├── model/
│   │   └── Customer.java          # 客户实体
│   ├── service/                   # 业务逻辑层
│   │   ├── CustomerService.java   # 入住、退住、外出、床位状态联动
│   │   ├── BedService.java        # 床位总览、分房统计、床位调换
│   │   └── NurseService.java      # 护理级别、护理项目、护理记录
│   └── dao/                       # 预留数据访问层（暂无实现，逻辑内聚在 service）
├── data/                          # JSON 伪数据库（初始数据集）
│   ├── users.json                 # 用户与角色
│   ├── roles.json  role_menus.json
│   ├── customers.json             # 客户档案
│   ├── rooms.json  beds.json  bed_details.json
│   ├── nurse_levels.json  nurse_contents.json  nurse_level_items.json
│   ├── nurse_records.json
│   ├── customer_nurse_items.json  outward.json  backdown.json
│   └── menus.json  meals.json  foods.json  preferences.json
├── lib/                           # Jackson 依赖 jar
├── compile.bat                    # 编译（输出到 out/）
├── run.bat                        # 运行
├── count.bat                      # 统计各源文件行数
└── README.md
```

## 环境要求

- JDK 17 或更高版本（已在 JDK 25 上验证编译通过）
- Windows（`compile.bat` / `run.bat` 为批处理脚本；其他平台可手动执行等价命令）

## 编译与运行

**Windows 一键运行**

```bat
compile.bat
run.bat
```

**手动编译（跨平台）**

```bash
javac -encoding UTF-8 \
  -cp "lib/jackson-core-2.15.4.jar:lib/jackson-databind-2.15.4.jar:lib/jackson-annotations-2.15.4.jar" \
  -d out \
  src/com/neusoft/yiyang/*.java \
  src/com/neusoft/yiyang/model/*.java \
  src/com/neusoft/yiyang/service/*.java \
  src/com/neusoft/yiyang/cli/*.java

java -cp "out:lib/jackson-core-2.15.4.jar:lib/jackson-databind-2.15.4.jar:lib/jackson-annotations-2.15.4.jar" \
  com.neusoft.yiyang.Main
```

> Windows 下 `-cp` 分隔符为 `;`，Linux / macOS 为 `:`。
> 程序以当前工作目录为基准读写 `data/`，请在项目根目录下运行。

## 内置测试账号

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 系统管理员 | `admin` | `admin` |
| 系统管理员 | `admin1` / `admin2` | 同账号名 |
| 健康管家 | `steward1` / `steward2` | `123456` |

> 账号数据为演示用途，明文存放于 `data/users.json`，请勿用于生产环境。

## 数据说明

- 所有数据表以 JSON 数组形式存储，每行一个对象，字段名与数据库设计文档一致；
- `data/` 下文件既是初始数据集，也是运行时的持久化目标，程序退出后修改会被保留；
- 如需恢复初始数据，用 Git 还原即可：`git checkout -- data/`。

## 许可

本项目为课程实践作品，可自由参考学习。
