import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;


public class FileManipulation {
    public static void main(String[] args) throws IOException, SQLException, InterruptedException {

        String [] index = {"student_id","name","gender","academy"};
        //String[]index={"student_id","class_id"};
        String path="C:\\Users\\stone\\IdeaProjects\\happy\\src\\student.csv";
        FileReader file = new FileReader(path); // 读取的CSV文件
        String key="student_id";

csv csv1=new csv(index,file,key,path);
//csv1.threadinsert(1,"student");

        long startTime = System.currentTimeMillis();
        String val=csv1.getval("11911610","name");
       //csv1.delete("11911611");
        //csv1.save();
//csv1.select("gender","F");
        long endTime   = System.currentTimeMillis();
       System.out.println("总耗时为："+(endTime-startTime)+"毫秒");
//csv1.change("11211111","name","sb");
//csv1.insert(index);
//csv1.save();
}}
class csv{
    String[]index;
    int size=0;
    int point;
    String path;
    String pkey;
    static int maxsize=9999999;
    HashMap<String,Integer>hashMap=new HashMap<>();
    String[][]data;
    int pkeyindex;
    HashMap<String,Integer>getindex=new HashMap<>();
    public csv(String[] index,FileReader file,String pkey,String path) throws IOException {
        this.index = index;
        this.pkey=pkey;
        this.path=path;
        data=new String[maxsize][index.length];
        BufferedReader br = new BufferedReader(file);
        String line;
        for(int i=0;i<index.length;i++){
            getindex.put(index[i],i);
        }int pkeyindex=getindex.get(pkey);
        while ((line = br.readLine()) != null&&size<maxsize) {
            data[size]=line.split(",");
            hashMap.put(data[size][pkeyindex],size);
            size++;
    }point=size;
}

    public  void threadinsert(int num, String name) throws IOException, SQLException, InterruptedException {
        int slice=size/num;
        ConnectionPool Pool=new ConnectionPool();
        LinkedList<MyThread>myThreads=new LinkedList<>();
       for(int i=0;i<num-1;i++){
           myThreads.add(new MyThread(i*slice,(i+1)*slice,Pool.getConnection(),name,data,index.length));
           myThreads.getLast().start();
       }
        myThreads.add(new MyThread((num-1)*slice,size,Pool.getConnection(),name,data,index.length));
        myThreads.getLast().start();
        for(int i=0;i<myThreads.size();i++){
            myThreads.get(i).join();
        }
    }

    public void  select(String item, String aim) {
        System.out.println(pkey);
        int index=getindex.get(item);
        for(int i=0;i<size;i++){
            if(data[i][index].equals(aim)&&(!data[i][pkeyindex].equals(""))){
                System.out.println(data[i][pkeyindex]);
            }
        }

    }

    public void  delete(String item){
        data[hashMap.get(item)][pkeyindex]="";
        hashMap.remove(pkey);
        size--;
}
    public static void setMaxsize(int maxsize) {
        csv.maxsize = maxsize;
    }

    public String[] getIndex() {
        return index;
    }

    public void setIndex(String[] index) {
        this.index = index;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public static int getMaxsize() {
        return maxsize;
    }

    public HashMap<String, Integer> getHashMap() {
        return hashMap;
    }

    public void setHashMap(HashMap<String, Integer> hashMap) {
        this.hashMap = hashMap;
    }

    public String[][] getData() {
        return data;
    }

    public void setData(String[][] data) {
        this.data = data;
    }

    public int getPkeyindex() {
        return pkeyindex;
    }

    public void setPkeyindex(int pkeyindex) {
        this.pkeyindex = pkeyindex;
    }

    public HashMap<String, Integer> getGetindex() {
        return getindex;
    }

    public void setGetindex(HashMap<String, Integer> getindex) {
        this.getindex = getindex;
    }

    public String getval(String pkey, String key) {
        return data[hashMap.get(pkey)][getindex.get(key)];
    }

    public void change(String pitem, String item, String aim) {
        int i=hashMap.get(pitem);
        data[hashMap.get(pitem)][getindex.get(item)]=aim;
        if (item==pkey){hashMap.remove(data[hashMap.get(pitem)][getindex.get(item)]);hashMap.put(aim,i);}
    }
    public void insert(String[] all) {
        if(!hashMap.containsKey(all[pkeyindex])){
            data[point]=all;
            hashMap.put(all[pkeyindex],point);
            point++;
            size++;
        }
    }
    public void save()throws IOException{
        BufferedWriter bw = new BufferedWriter(new FileWriter(path)) ;
        for(int i=0;i<point;i++){
            if(!data[i][pkeyindex].equals("")){
                for(int j=0;j<index.length-1;j++){
                    bw.write(data[i][j]+",");
                }bw.write(data[i][index.length-1]+"\n");
            }
        }
        bw.close();
    }

}
class MyThread extends Thread{
    public  int i,j;public  String table;
    public Connection con;
    public String[][]data;
    public int num;
    public PreparedStatement pstm =null;
    public MyThread(int i,int j,Connection con,String table,String[][]data,int num){
        this.i=i;
        this.j=j;
        this.table=table;
        this.con=con;
        this.data=data;
        this.num=num;
    }
    public void run() {

        String sql = "INSERT INTO " +table+" values(?,?,?,?)";
        try {pstm = con.prepareStatement(sql);
            //con.setAutoCommit(false);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        for(int e=i;e<j;e++){
            for(int k=0;k<num;k++){
                try {
                    pstm.setString(k+1,data[e][k]);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            try {pstm.executeUpdate();
                //pstm.addBatch();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }/*
        try {
            pstm.executeBatch();
            pstm.clearBatch();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            con.commit();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }*/

    }

    }
