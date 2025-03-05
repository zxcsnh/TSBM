package qdu.edu.cn.core;
public interface Adapter {
    public long insert(String data);
    //在创建多线程插入时，是使用多个对象还是只使用一个对象
    public void initConnect(String host, String port, String user, String password);
}
