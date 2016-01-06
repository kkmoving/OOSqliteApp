# OOSqliteApp
Object-Oriented Android database framework based on Sqlite.

#Integration
Gradle dependency

    dependencies {
      compile 'com.kkmoving.open:oosqlite:1.0'
    }

#How to use
Define your entity

     public class User extends OOSqliteEntity {   
          private static final int NAME_COL_ID = 1;
          private static final int AGE_COL_ID = 2; 

          @Indexing
          @ColumnTag(NAME_COL_ID)    
          String name;  
  
          @ColumnTag(AGE_COL_ID)
          int age;    

          @Transient    
          boolean choosen;        

          float score;        
     }

Init database

    public class DemoApplication extends Application {
        @Override    
        public void onCreate() {
            super.onCreate();        
            DatabaseManager.init(this);    
        }
    }

    public class DatabaseManager {    

        public static void init(Context context) {        
            OODatabase database = new OODatabase("app.db", 1); 
            database.registerEntity(User.class);

            OOSqliteManager.getInstance().register(database);
            OOSqliteManager.getInstance().initAllDatabase(context);    
        }   
    }

Create

    User.insert(user);

Retrieve

    User.query(User.class, null);

Update

    User.update(mUser);

Delete

    User.delete(mUser);


For detail: http://www.jianshu.com/p/f14a5d72a373
