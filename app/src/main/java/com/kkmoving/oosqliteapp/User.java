package com.kkmoving.oosqliteapp;

import com.kkmoving.oosqlite.ColumnTag;
import com.kkmoving.oosqlite.Indexing;
import com.kkmoving.oosqlite.OOColumn;
import com.kkmoving.oosqlite.OOSqliteEntity;
import com.kkmoving.oosqlite.OOTableListener;
import com.kkmoving.oosqlite.TargetVersion;
import com.kkmoving.oosqlite.Transient;

import java.util.List;

/**
 * Created by kkmoving on 2016/1/4.
 */
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

    @TargetVersion(2)
    boolean active;

    public static void add(String name, int age) {
        User user = new User();
        user.name = name;
        user.age = age;

        User.insert(user);
    }

    public static List<User> queryAll() {
        return User.query(User.class, null);
    }

    public static void asyncQueryAll() {
        User.queryAsync(User.class, null, new LeQueryCallback() {
            @Override
            public void onQuerySuccess(List list) {

            }
        });
    }

    public static List<User> queryByNameOrderByAge(String name) {
        String selection = User.equalSelection(getNameColumn(), name);
        return User.query(User.class, selection, getAgeColumn(), true);
    }

    public static List<User> queryAgeOlderThan30() {
        OOColumn ageCol = getAgeColumn();
        String selection = ageCol.mName + ">30";

        return User.query(User.class, selection);
    }

    private static OOColumn getNameColumn() {
        return getColumn(User.class, NAME_COL_ID);
    }

    private static OOColumn getAgeColumn() {
        return getColumn(User.class, AGE_COL_ID);
    }

    public static OOTableListener createListener() {
        return new OOTableListener() {

            @Override
            public void onCreate() {
                User user = new User();
                user.name = "admin";

                User.insert(user);
            }

            @Override
            public void onUpgrade(int oldVersion, int newVersion) {
                if (oldVersion < 2 && newVersion >= 2) {
                    User user = new User();
                    user.name = "upgrade";

                    User.insert(user);
                }
            }

            @Override
            public void onDowngrade(int oldVersion, int newVersion) {

            }

            @Override
            public void onReady() {

            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("  ");
        sb.append(age).append("  ");
        sb.append(score).append("  ");
        return sb.toString();
    }
}
