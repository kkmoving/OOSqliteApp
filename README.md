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

    User.update(user);

Delete

    User.delete(user);


For more detail: http://www.jianshu.com/p/f14a5d72a373


#License

Copyright 2016 kkmoving

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
   
   http://www.apache.org/licenses/LICENSE-2.0
   
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
