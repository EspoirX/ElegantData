# ElegantData

优雅地处理数据

## 使用方法

#### 一、创建一个接口：
```java
@ElegantEntity(fileName = "UserInfo_Preferences")
public interface SharedPreferencesInfo extends ISharedPreferencesInfoDao {
    String keyUserName = "";
}
//或者
@ElegantEntity(fileName = "CacheFile.txt", fileType = ElegantEntity.TYPE_FILE)
public interface FileCacheInfo extends IFileCacheInfoDao {
    int keyPassword = 0;
}
```
加上@ElegantEntity注解，并且定义 fileName 文件名，fileType 文件类型，fileType 默认为 SP 文件。

定义好接口后 ReBuild 一下，这时候会生成一个 IXXXDao 接口，如上所示，然后继承它。

#### 二、定义一个抽象类继承 ElegantDataBase 

```java
@ElegantDataMark
public abstract class AppDataBase extends ElegantDataBase {

    public abstract SharedPreferencesInfo getSharedPreferencesInfo();

    public abstract FileCacheInfo getFileCacheInfo();
}
```
让这个类加上 @ElegantDataMark 注解，并且定义在第一步中定义的接口的抽象方法。

#### 三、使用单例模式去构建
```java
@ElegantDataMark
public abstract class AppDataBase extends ElegantDataBase {

    public abstract SharedPreferencesInfo getSharedPreferencesInfo();

    public abstract FileCacheInfo getFileCacheInfo();


    private static AppDataBase spInstance;
    private static AppDataBase fileInstance;
    private static final Object sLock = new Object();

    //使用SP文件
    public static AppDataBase withSp() {
        synchronized (sLock) {
            if (spInstance == null) {
                spInstance = ElegantData
                        .preferenceBuilder(ElegantApplication.getContext(), AppDataBase.class)
                        .build();
            }
            return spInstance;
        }
    }

    //使用File文件
    public static AppDataBase withFile() {
        synchronized (sLock) {
            if (fileInstance == null) {
                String path = Environment.getExternalStorageDirectory() + "/ElegantFolder";
                fileInstance = ElegantData
                        .fileBuilder(ElegantApplication.getContext(), path, AppDataBase.class)
                        .build();
            }
            return fileInstance;
        }
    }
}
```
如果使用 SP 文件，调用 ElegantData#preferenceBuilder 方法去构建实例。  
如果是 File 文件，则使用 ElegantData#fileBuilder 去构建。  
两个方法都需要传入上下文和 AppDataBase 的 class。唯一不一样的是使用 File 文件需要先创建文件夹，所以在第二个参数传入的是创建文件夹的路径。

#### 四、使用

经过上面三个步骤后，ReBuild 一下，然后就可以开始使用了。

```java
//使用 SP 文件存入数据
AppDataBase.withSp().getSharedPreferencesInfo().putKeyUserName("小明");

//使用 File 文件存入数据
AppDataBase.withFile().getFileCacheInfo().putKeyPassword(123456789);


String userName = AppDataBase.withSp().getSharedPreferencesInfo().getKeyUserName();
Log.i("MainActivity", "userName = " + userName);

int password = AppDataBase.withFile().getFileCacheInfo().getKeyPassword();
Log.i("MainActivity", "password = " + password);
```

## @IgnoreField

被 @IgnoreField 注解标记的字段，将不会被解析:
```java
@ElegantEntity(fileName = "UserInfo_Preferences")
public interface SharedPreferencesInfo extends ISharedPreferencesInfoDao {
    String keyUserName = "";
    
    @IgnoreField
    int keyUserSex = 0;
}
```
Rebuild 后，keyUserSex 会被忽略，相关字段的方法不会被生成。

## @NameField

被 @IgnoreField 注解标记的字段，可以重命名：
```java
@ElegantEntity(fileName = "UserInfo_Preferences")
public interface SharedPreferencesInfo extends ISharedPreferencesInfoDao {
    String keyUserName = "";
    
    @NameField(value = "sex")
    int keyUserSex = 0;
}
```
字段 keyUserSex 解析后生成的 put 和 get 方法是 putSex 和 getSex , 而不是 putUserSex 和 getUserSex。

## @EntityClass
@EntityClass 注解用来标注实体类，如果你需要往文件中存入实体类，那么需要加上这个注解，否则会出错。
```java
@ElegantEntity(fileName = "UserInfo_Preferences")
public interface SharedPreferencesInfo extends ISharedPreferencesInfoDao {
    String keyUserName = "";

    @EntityClass(value = SimpleJsonParser.class)
    User user = null;
}
```
如上所示，@EntityClass 注解需要传入一个 json 解析器，存入实体类的原理是把实体类通过解析器变成 json 字符串存入文件，取出来的时候
通过解析器解析 json 字符串变成实体类。

```java
public class SimpleJsonParser extends JsonParser<User> {

    private Gson mGson;

    public SimpleJsonParser(Class<User> clazz) {
        super(clazz);
        mGson = new Gson();
    }

    @Override
    public String convertObject(User object) {
        return mGson.toJson(object);
    }

    @Override
    public User onParse(@NonNull String json)   {
        return mGson.fromJson(json, User.class);
    }
}
```

json 解析器需要实现两个方法，convertObject 方法作用是把实体类变成 json 字符串，onParse 方法作用是把 json 字符串变成 实体类。