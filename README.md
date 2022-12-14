
# DBMS与File System的对比
## Introduction
通过这次project，我们对比文件系统与数据库系统在对数据操作方面的差异，证明数据库系统在查询、存储大数据、管理等方面的优势。
## Group Info and Contribution
#### Group Members

- **张通 11911611**
- **唐云龙 11911607**
- **徐思婷 11911635**

#### Contribution
- **张通 (33.333%):** 
建表；写Java；导数据；多平台测试；搞高并发；写report
- **唐云龙 (33.333%):** 
教pandas；写Python；洗数据；导数据；写report
- **徐思婷 (33.333%):** 
 洗数据；写C++/Python；测速；写report
## Database design
#### Diagram of table structure

![Table structure](https://img-blog.csdnimg.cn/20210405212841234.png?x-oss-process=image,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzIxNTcyOTUx,size_16,color_FFFFFF,t_70#pic_center)


#### Explanations for some Details of our Database
##### The tables design in database
我们建了10个表，基本符合范式。department,course,teacher,academy,student是基本表，在course下根据班型细分了class，然后根据班的时间，上课地点细分了class_detail。先修课是将课程分成几个kind,满足先修课要求即每一个kind至少修满一门课。
##### The logical processing of the prerequisite courses
1. 把分为几个kind，大部分先修课内容具有如下形式：
<kbd>$(A_1\lor A_2)\land (A_3\lor A_4\lor A_5)\land (A_6\lor A_7)\land ...$</kbd>
这里每一个括号里面通过$\lor$连接的课程都分到同一个kind里面，
2. python先对“且”进行切分，例如将<kbd>$(A_1\lor A_2)\land (A_3\lor A_4\lor A_5)\land (A_6\lor A_7)$</kbd>切分为<kbd>$(A_1\lor A_2)$</kbd>、<kbd>$(A_3\lor A_4\lor A_5)$</kbd>、 <kbd>$(A_6\lor A_7)$</kbd>三个部分，这三个部分就对应三个kind，
3. 然后在一个group内部，我们根据“或”进行切分，例如将之前切分出来的<kbd>$(A_3\lor A_4\lor A_5)$</kbd>切为$A_3、A_4、A_5$三个部分，赋予它们赋予相同的kind值。
4. 对于不满足形如<kbd>$(A_1\lor A_2)\land (A_3\lor A_4\lor A_5)\land (A_6\lor A_7)\land ...$</kbd>的先修课，我们的解决办法是把所有不满足该形式的条目，依据逻辑运算规则，全都手动改成如上形式，再用python统一切割。
用于切割“且”的主要代码如下（切割“或”的方法类似）：
```python
pre_course['p_and1']=pre_course.prerequisite
.apply(lambda x:None if x is None 
else x.split('并且')[0])
pre_course['p_and2']=pre_course.prerequisite
.apply(lambda x:None if x is None or len(x.split('并且'))<2 
else x.split('并且')[1])
pre_course['p_and3']=pre_course.prerequisite
.apply(lambda x:None if x is None or len(x.split('并且'))<3 
else x.split('并且')[2])
pre_course['p_and4']=pre_course.prerequisite
.apply(lambda x:None if x is None or len(x.split('并且'))<4 
else x.split('并且')[3])
```
5. 最终效果由下图所示：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210409143132169.png#pic_center)

##### The generation of the teacher's Emails
1. 由于从南科大的官网上爬取教师信息存在困难（如不是所有的老师都在上面），我们采用了自动生成邮箱的方法。
2. 我们引入了Pinyin包，它可以根据教师的中文名生成对应的汉语拼音，然后加上邮箱后缀<kbd>@sustech.edu.cn</kbd>。
3. 另外，我们采取了在汉语拼音后面加上数字的方法以避免重复。
4. 实现代码如下：
```python
from xpinyin import Pinyin
b=Pinyin()
a=pd.read_csv(r'teacher.csv')
for i in range(374):
    a.e_mail[i]=""
for i in range(374):
    ha=b.get_pinyin(a.name[i],"")+"@sustech.edu.cn"
    if (a['e_mail']==ha).any():
        ha=b.get_pinyin(a.name[i],"")+"1@sustech.edu.cn"
        if (a['e_mail']==ha).any():
            ha=b.get_pinyin(a.name[i],"")+"2@sustech.edu.cn"
            if (a['e_mail']==ha).any():
                ha=b.get_pinyin(a.name[i],"")
                +"3@sustech.edu.cn"
    a.e_mail[i]=ha
a.e_mail.duplicated().any()
a.to_csv("teacher1.csv",index=0)
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210409151502546.png?x-oss-process=image,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzIxNTcyOTUx,size_16,color_FFFFFF,t_70)

##### 对classlist的处理
1. 在处理Classlist的时候遇到了两个问题，一个是weeklist如何处理，还有一个是部分location的缺失问题。
2. 对于前一个问题，我们发现weeklist只有三种类型：每周有课、单周有课和双周有课三种情况。所以我们并没有采用把weeklist当成数组或者字符串读入的做法，而是把它转化成三种类型：<kbd>1-15周</kbd>，<kbd>1-15周，单周</kbd>，<kbd>2-14周，双周</kbd>。
3. 实现处理的python代码如下：
```python
ind=0
df_1=pd.DataFrame()
for i in df['classlist']:
    i=eval(i)
    for j in i:
        j.update({"id":ind})
        if j['weekList']==['1', '2', '3', '4', '5', '6', 
        '7', '8', '9', '10', '11', '12', '13', '14', '15']:
            j.update({'weeks':"1-15周"})
        elif j['weekList']==['1', '3', '5', '7', '9', '11', 
        '13', '15']:
            j.update({'weeks':"1-15周，单周"})
        else:
            j.update({'weeks':"2-14周，双周"})
        sub_dict=pd.DataFrame(j)
        df_1=pd.concat([df_1,sub_dict])
    ind=ind+1
df_1
locs=['id','location','classTime','weekday','weeks']
df_1=df_1[locs]
df_1=df_1.drop_duplicates()
```
4. 对于后一个问题，我们用"待定"补全了缺失的location
```python
df_1['location']=df_1.location
.apply(lambda x:"待定" if x=="" else x)
```
5. 效果如下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210409150413473.png?x-oss-process=image,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzIxNTcyOTUx,size_16,color_FFFFFF,t_70)

## Import data
分别用Python和Java导入数据
#### Python?
首先使用的方法是用python生成sql语句
```python
source_path="prerequisite.csv"
target_path='inserts.sql'
def w_sql(path):
    df=pd.read_csv(path)
    df1=df
    col=df.columns.values
    table_name=path.strip('.csv')
    tit='INSERT INTO '+table_name+"("
    for i in col:
        tit=tit+i+","
    tit=tit[:-1]+") VALUES("
    sql=""
    for i in df1.index:
        if i!=0:
            sql=sql+tit
            for j in range(0,len(col)):
                if type(df1.iloc[i,j])==type("1"):
                    sql=sql+"'"+str(df1.iloc[i,j])+"'"+","
                else:
                    sql=sql+str(df1.iloc[i,j])+","
            sql=sql[:-1]+")\n"
    return sql
f = open(target_path,'w')
f.write(w_sql(source_path))
f.close()
```
生成的SQL语句如下所示
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210409151654965.png?x-oss-process=image,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzIxNTcyOTUx,size_16,color_FFFFFF,t_70)
对于较小的csv文件，这种方法可以很快生成sql语句，但对于较大的如student.csv速度极慢。
#### Java!
显然，使用IO读写文件来import data比较缓慢，当然使用java的jdbc来导数据最方便了！
##### 优化策略
1. 用java改进生成sql脚本，使用jdbc直接导读入的数据
大约30分钟，计1800000ms
   
2. 使用Statement加批量的方法
可以把一定量的sql语句加载到缓存里打包，然后一个包一个包地执行，这样就会快一些。
 ![image.png](https://img-blog.csdnimg.cn/img_convert/12172307657d437d9c472c4b81f0e247.png)
3. 使用事务，不逐条提交log
    尽管使用了批量，每执行一个语句就会提交一个log，如果使用事务的话就可以把所有的SQL语句省去log，只生成一条log，就会更快一些
    ![image.png](https://img-blog.csdnimg.cn/img_convert/ad9a36f68c47c57bf8f2778d92ffb3c5.png)

4. 多线程+jdbc连接池
   我们建立了一个连接池，建立连接池都需要一定的时间，可以在不同场合都可以调用这个连接池，另外开了12个线程每个线程都从连接池中获得connection，每个connection里面再启用一个事务，再在事务里面通过批量导入的方法，批量导入等量数据，就可以进一步提速。
    ![image.png](https://img-blog.csdnimg.cn/img_convert/32b2225520c9ca5398f088214b3b5d43.png)
    于是我们获得了导入速度的不断提升（横坐标），如图所示，纵坐标单位为毫秒（ms）
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210411161239710.png?x-oss-process=image,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzIxNTcyOTUx,size_16,color_FFFFFF,t_70)
##   Task 3: Compare database and file
#### Data and Environment
Data: student.csv(约400w)
Operating system: freebsd ,debian
Platform: Postgresql,Mraiadb
Programming language: Java, Python
hardware environment:
||VMware Virtual Machine|Raspberry pie 4b|
|--|--|--|
|Memory|1GB|8GB|
|Disk capacity|20GB|2TB|

#### Experiment Design
 ##### 跨平台跨系统比较数据库性能
   对比debian和freebsd分别在postgresql和mysql上的性能差异
   建表语句
   ```sql
   create table if not exists student(
    student_id integer primary key ,
    name varchar(20) not null ,
    gender char ,
    academy varchar(20));
   ```
- **导入**
student.csv

| Task: Import |PostgreSQL|Mariadb|
|--|--|--|
| debian|56 sec 69 ms |1 min 11 sec 8 ms|
|freebsd|1 min 0 sec 15 ms|1 min 25 sec 171 ms|


   - **查询**
   select * from student where gender='F'
   
   | Task: Retrieve |PostgreSQL|Mariadb|
|--|--|--|
| debian|63 ms |93 ms|
|freebsd|78 ms|110 ms|

 - **删除**

  | Task: Delete |PostgreSQL|Mariadb|
|--|--|--|
| debian|32 ms |32 ms|
|freebsd|32 ms|16 ms|
实验结果基本符合预期
##### 对比数据库和python、Java、C++文件系统对数据进行操作时的差异
我们分别用Python、Java、C++实现了对数据表的存取以及增删查改功能，并用它们执行相同的增删查改及存取任务，以用于和DBMS比较。
测试结果如下

| Tasks | Python|Java |C++|DBMS(postgre)|
|--|--|--|--|--|
|Read in|3907 ms|3192 ms|17242 ms|3192 ms|
| Search name by id  |181 ms  |≈ 0 ms|13 ms|≈ 0 ms|
|Get those who gender is male|383 ms|4733 ms|137 ms|59 ms|
|  Insert a student |  255 ms| ≈ 0 ms |≈ 0 ms|≈ 0 ms|
|Update a student's name|291ms|≈ 0 ms|137 ms|≈ 0 ms|
|Delete a student | 502 ms |≈ 0ms |22 ms|≈ 0 ms|
|Save|6976 ms|1210 ms|29067 ms|-|

为了便于观察，我们对执行时间取了对数
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210411150347343.png?x-oss-process=image,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzIxNTcyOTUx,size_16,color_FFFFFF,t_70)

根据图表对比可见，

 1. 在**Read in** 和 **Save** 两个读入与存储数据的操作中，Java表现最优，分别用时为3192ms和1210ms，读数据的速度赶得上DBMS，C++表现最次，分别用时为17242ms和29067ms；
 2. 在增删改查四部分操作中，我们分别进行了 - 根据id进行查询、根据性别进行查询、插入一个学生、更新一个学生的姓名、删除一个学生 - 五个操作；综合来看，DBMS运行速度最快，而在三种语言中，Java基本表现最优，但在查询性别中耗时最长；C++其次，Python运行速度最慢。

通过对Python、Java、C++三种语言进行存取数据表格student以及增删改查的实现，我们进行了执行时间的对比。运行时间结果由于代码实现方式的不同、语言本身的差异而有所不同。综上所述，DBMS最优。
##### 高并发导入数据
   见import data部分
##### Linux/Unix，Windows和DBMS文件系统权限对比
1. Linux/unix
Linux系统为每个文件和目录都设有默认的权限，每个文件中可分有拥有者（u）、同群组的用户（g）和其他组用户（o）。
<kbd>r</kbd> 表示文件可以被读（read）,4
<kbd>w</kbd> 表示文件可以被写（write）,2
<kbd>x</kbd> 表示文件可以被执行,1
并且权限数值存在时以1表示，否则就用0来表示（实际上就是用二进制的方式来表示），就可以对于某个文件或目录中的权限数值计算
撤销执行权限
![image.png](https://img-blog.csdnimg.cn/img_convert/0c48239c88e826997b979b60b9265443.png)
赋予执行权限
![image.png](https://img-blog.csdnimg.cn/img_convert/0c48239c88e826997b979b60b9265443.png)
1. windows
进入文件目录，右键文件的属性进行更改
![image.png](https://img-blog.csdnimg.cn/img_convert/fe67ca6f2b99f38c7b6bab362eaf9985.png)
![image.png](https://img-blog.csdnimg.cn/img_convert/6f1de89ed74302b0e53be29e5099a389.png)
1. DBMS
和linux类似，dbms只需一行命令即可完成对用户权限的更改，且规则更加简单
管理员赋予stone创建数据库权限
![image.png](https://img-blog.csdnimg.cn/img_convert/f85beb5e32b08ff20297f49196d7a0c6.png)
建库
![image.png](https://img-blog.csdnimg.cn/img_convert/27a601c9855b2a5851edbec15675280c.png)
##### 索引优化
在建表之初，我们已经显式地构造了较多索引，这里主要在展示dbms在索引方面的优化。
   在Java中，我们定义了csv类，以hashmap储存索引，O（1）地查找数据，但占用内存过多
   在Postgresql中有丰富的数据结构，如B tree，能够针对不同的表单结构，产生高效的索引，比如对于>，<>等不等的约束，hash只能用于等值过滤,且键值对必须唯一确定
  ![在这里插入图片描述](https://img-blog.csdnimg.cn/20210411181202678.png)

   建立索引前
   ```sql
   select * from student where gender='F'and academy='阿兹卡班';
   ```
  ![在这里插入图片描述](https://img-blog.csdnimg.cn/20210411181239787.png)

   建立索引后
   ```sql
   create index ga on student(gender,academy);
   ```
  ![在这里插入图片描述](https://img-blog.csdnimg.cn/20210411181319296.png)

##### 事务
事务能够提高update速度，误删误改数据后回滚，我们也可以通过设置锁来防止数据脏读，我们通过java模仿了简单的事务。
```java
public csv(String[] index,FileReader file,String pkey,String path);//begin
public void  select(String item, String aim);
public void  delete(String item);
public void change(String pitem, String item, String aim);
public void insert(String[] all);
public void save();//commit
public  void threadinsert(int num, String name);//高并发上传至数据库
```
dbms中，我们对处理过后的student表进行了事务修改，前三条sql语句根据学号查询分别将学号为11000012、11000020的同学修改了姓名，同时将学号为11000005的同学修改了书院，在此设置保存点a，再继续修改学号为11000001的同学的书院，再进行rollback操作，最后进行commit操作，发现只有前三条修改语句执行了，a点之后的语句并未执行。
![在这里插入图片描述](https://img-blog.csdnimg.cn/2021041115250972.png?x-oss-process=image,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzIxNTcyOTUx,size_16,color_FFFFFF,t_70)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210411152519648.png?x-oss-process=image,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzIxNTcyOTUx,size_16,color_FFFFFF,t_70)
##### 大数据
当数据量过大时，使用java数据只能全部存于内存或者将一部分写入文件中，IO流的速度是访问速度的瓶颈，而dbms能够将一部分索引存到了硬盘中，使用如B tree等数据结构合理存储索引，且可以直接读取数据，不用重新建立索引。
    ![image.png](https://img-blog.csdnimg.cn/img_convert/8722c204286bc9dcf429f2d55f55e592.png)

### Challenges
1. 高并发导入数据使用自增ID会导致死锁，于stackoverflow查阅后改变表结构遂解决
2. 多线程灵异现象，java主程序结束后thread仍在跑，导致计时有误，查阅资料后加join（）即可
3. mariadb无法导入数据，强制更改字符类型utf8mb4
4. 建立Teacher表时，发现存在在不同系、但姓名相同的老师，故teacher.name不能用来作为teacher这个表的主键，于是采用邮箱作为主键，且以数字后缀区分同名不同人的情况；
5. 根据性别查询操作时，将数据循环打印出来耗时过长，不具可比性，考虑到打印操作不属于查询任务本身，故删去打印操作进行计时；
6. 对于weeklist的处理，由于Dataframe没有数组形式，用datagrip进行join操作解决问题；
7. 存在一条course相同且班级相同的生物课，导致在导入时报错。解决办法：手动把该课程改成两个班.

### Conclusion
#### 不同平台下的比较
在不同系统不同操作下，Postgresql都相较Mariadb有一定性能上的优势，查阅资料，应该是算法的优势。

#### DBMS的优势
1. 高并发的优势
   DBMS对高并发情况下有大量优化，且结合事务的隔离级别，能够防止脏读等问题
2. 多用户与权限管理
   在计算机系统中，同一文件同一时间只能被同一进程使用，而DBMS能够通过授权给不同对象以访问的权限。同时，权限管理能有效减少数据的非法修改，结合前端，能够让用户只能使用到少量的数据
3. 大数据的管理
    在处理少量数据的情况下，文件系统有一定的优势，但在极大数据量下，由于维护索引的复杂性，且各种语言IO都需要付出较多时间代价，毫无疑问，DBMS有极大的优势
4. 丰富的索引与key
   关系型数据库内有各种类型的索引，能够提高访问速度.同时key的设置能减少数据冗余
### Reflection
1. 忽视了git等工具的使用，导致版本管理紊乱
2. 分工合作的合理性有问题，中间交接存在了重复性劳动



