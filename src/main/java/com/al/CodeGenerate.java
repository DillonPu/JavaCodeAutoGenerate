package com.al;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class CodeGenerate {

    private static Logger log = LoggerFactory.getLogger(CodeGenerate.class);

    // 固定
    private static String projectPath = System.getProperty("user.dir"); // 获取当前工程目录
    private static PackageConfig pc = new PackageConfig();

    // 自定义值
    private static String author = "阿来小同学";
    private static String ip = "localhost";
    private static String port = "3306";
    private static String dbname = "al"; // 数据库名
    private static String url = "jdbc:mysql://localhost:3306/al?useUnicode=true&useSSL=false&characterEncoding=utf8";
    private static String driver = "com.mysql.jdbc.Driver";
    private static String username = "root";
    private static String password = "root";

    private static String[] dbTableNames = {}; // 无值：为数据库中所有表生成代码；有值 如：{"table1","table2"}，只生成数组中的表
    private static String javaPackageName = "com.test"; // 顶级包名
    private static String javaModuleName = "codeServer"; // 模块名
    private static String mapperPackageName = ""; // mapper文件所在包名

    private static final String templatePath = "/templates/mapper.xml.ftl"; // 如果模板引擎是 freemarker
    // private static final templatePath = "/templates/mapper.xml.vm"; //如果模板引擎是 velocity

    static {
        url = "jdbc:mysql://"
                +ip+":"
                +port+"/"
                +dbname+"?useUnicode=true&useSSL=false&characterEncoding=utf8";
    }

    /**
     * 读取控制台内容
     * @param tip 提示内容
     * @return
     */
    public static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append("请输入" + tip + "：");
        System.out.println(help.toString());
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotEmpty(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }

    /**
     * 获取数据库表及其字段
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static Map<String, Object> getDatabaseNameToTableNameAndColumnName() throws ClassNotFoundException, SQLException {
        Map<String, Object> tableNameMap = new HashMap<String, Object>();

        //加载驱动
        Class.forName(driver);

        //获得数据库连接
        Connection connection = DriverManager.getConnection(url, username, password);
        //获得元数据
        DatabaseMetaData metaData = connection.getMetaData();
        //获得表信息
        ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"});

        while (tables.next()) {
            Map<String, String> columnNameMap = new HashMap<String, String>(); //保存字段名

            //获得表名
            String table_name = tables.getString("TABLE_NAME");
            //通过表名获得所有字段名
            ResultSet columns = metaData.getColumns(null, null, table_name, "%");
            //获得所有字段名
            while (columns.next()) {
                //获得字段名
                String column_name = columns.getString("COLUMN_NAME");
                //获得字段类型
                String type_name = columns.getString("TYPE_NAME");

                columnNameMap.put(column_name, type_name);
            }

            tableNameMap.put(table_name, columnNameMap);

        }

        return tableNameMap;
    }

    /**
     * 获取数据库所有表名
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static String[] getTableNames() throws SQLException, ClassNotFoundException {

        Map<String, Object> map = getDatabaseNameToTableNameAndColumnName();
        Set<String> tableNames = map.keySet();

        log.info("数据库表名：{}",tableNames);

        String[] names = new String[tableNames.size()];
        names = tableNames.toArray(names);
        return names;

    }

    /**
     * 配置完毕，运行即可，生成mybatisx 代码
     * @param args
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        gc.setOutputDir(projectPath + "/src/main/java");
        gc.setAuthor(author);
        gc.setOpen(false);
        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(url);
        dsc.setDriverName(driver);
        dsc.setUsername(username);
        dsc.setPassword(password);
        mpg.setDataSource(dsc);

        // 包配置
        pc.setParent(javaPackageName);
        pc.setModuleName(javaModuleName);

        mpg.setPackageInfo(pc);

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };

        // 自定义输出配置
        List<FileOutConfig> focList = new ArrayList<FileOutConfig>();
        // 自定义配置会被优先输出
        focList.add(new FileOutConfig(templatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
                return projectPath + "/src/main/resources/mapper/" + mapperPackageName
                        + "/" + tableInfo.getEntityName() + "Mapper" + StringPool.DOT_XML;
            }
        });

        /*
        cfg.setFileCreate(new IFileCreate() {
            @Override
            public boolean isCreate(ConfigBuilder configBuilder, FileType fileType, String filePath) {
                // 判断自定义文件夹是否需要创建
                checkDir("调用默认方法创建的目录");
                return false;
            }
        });
        */
        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();

        // 配置自定义输出模板
        //指定自定义模板路径，注意不要带上.ftl/.vm, 会根据使用的模板引擎自动识别
        // templateConfig.setEntity("templates/entity2.java");
        // templateConfig.setService();
        // templateConfig.setController();

        templateConfig.setXml(null);
        mpg.setTemplate(templateConfig);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setEntityLombokModel(true);
        strategy.setRestControllerStyle(true);

        // 公共父类
        // 写于父类中的公共字段
        strategy.setSuperEntityColumns("id");

        String[] dbTableName = dbTableNames.length>0 ? dbTableNames : getTableNames();

        strategy.setInclude(dbTableName);
        strategy.setControllerMappingHyphenStyle(true);
        strategy.setTablePrefix(pc.getModuleName() + "_");
        mpg.setStrategy(strategy);
        mpg.setTemplateEngine(new FreemarkerTemplateEngine());
        mpg.execute();
    }


}
