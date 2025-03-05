package qdu.edu.cn.adapters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.taosdata.jdbc.TSDBDriver;

import qdu.edu.cn.core.Adapter;

public class TdengineAdapter implements Adapter {

    private Connection connection = null;
    private String url = null;
    private String host = null;
    private String port = null;

    public Connection getConnection() {
        if (connection != null) {
            return connection;
        }
        this.url = String.format("jdbc:TAOS://%s:%s?user=root&password=taosdata", host, port);
        try {
            // Class.forName("com.taosdata.jdbc.TSDBDriver");
            Properties connProps = new Properties();
            connProps.setProperty(TSDBDriver.PROPERTY_KEY_CHARSET, "UTF-8");
            connProps.setProperty(TSDBDriver.PROPERTY_KEY_LOCALE, "en_US.UTF-8");
            connProps.setProperty(TSDBDriver.PROPERTY_KEY_TIME_ZONE, "UTC-8");
            connection = DriverManager.getConnection(url, connProps);
            if (connection == null) {
                return null;
            }
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            connection = null;
            return null;
        }
    }

    @Override
    public void initConnect(String host, String port, String user, String password) {
        this.host = host;
        this.port = port;
        connection = getConnection();
        // 创建数据库
        try {
            Statement stm = connection.createStatement();
            stm.executeUpdate("create database if not exists test;");
            stm.executeUpdate("use test;");
            // // 创建超级表
            stm.executeUpdate("create stable metrics (ts timestamp, valuess float) TAGS (farm nchar(6), device nchar(6), s nchar(4));");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return;
    }

    @Override
    public long insert(String data) {
        String[] rows = data.split(System.lineSeparator()); // 分割行
        StringBuffer sqls = new StringBuffer();
        String sqlFormat = "(\"%s\",%s,%s,\"%s\",\"%s\",\"%s\")";
        String sql_head = "INSERT INTO metrics (tbname, ts, valuess, farm, device, s) VALUES ";
        // String sqlFormat = "%s USING metrics TAGS (\"%s\",\"%s\",\"%s\") VALUES (%s) "; // 更改了插入SQL语句格式
        long costTime = 0L;
        for (String row : rows) {
            String[] sensors = row.split(","); // 分割列
            if (sensors.length < 3) { // 过滤空行
                continue;
            }
            String timestamp = sensors[0];
            String farmId = sensors[1];
            String deviceId = sensors[2];
            // StringBuffer values = new StringBuffer();
            int length = sensors.length;
            // System.out.println(length);
            StringBuffer sql = new StringBuffer();
            for (int index = 3; index < length; index++) {
                String sensorName = "s" + (index - 2);
                String value = sensors[index];
                String tbname = farmId + deviceId + sensorName;
                sql.append(String.format(sqlFormat, tbname, timestamp, value, farmId, deviceId, sensorName)); // 使用批量插入格式
                sql.append(" ");
                // values.setLength(0);
            }
            if (sqls.length() + sql.length() + sql_head.length() > 1048576) {
                String SQL = sql_head + sqls.toString();
                sqls = sql;
                long startTime = System.nanoTime();
                connection = getConnection();
                if (connection != null) {
                    try {
                        Statement stmt = connection.createStatement();
                        // System.out.println(SQL);
                        try {
                            // System.out.println(SQL);
                            stmt.execute(SQL); // 执行批量插入
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        // sqls.setLength(0);
                        stmt.close();
                        long endTime = System.nanoTime();
                        costTime += (endTime - startTime) / 1000 / 1000;
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            } else {
                sqls.append(sql);
                sql.setLength(0);
            }
        }
        if (sqls.length() != 0) { // 执行剩余数据的插入
            String SQL = sql_head + sqls.toString();
            if (connection != null) {
                try {
                    long startTime = System.nanoTime();
                    Statement stmt = connection.createStatement();
                    try {
                        stmt.execute(SQL); 
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    stmt.close();
                    long endTime = System.nanoTime();
                    costTime += (endTime - startTime) / 1000 / 1000;
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return costTime;
    }

    private void closeStatement(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
