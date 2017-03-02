package com.kkmoving.oosqliteapp;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.kkmoving.oosqlite.OOSqliteEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kkmoving on 2016/1/4.
 */
public class DemoView extends LinearLayout {

    private List<User> mDataList;
    private BaseAdapter mAdapter;
    private ListView mListView;

    public DemoView(final Context context) {
        super(context);

        setOrientation(LinearLayout.VERTICAL);

        OperationView operationView = new OperationView(context);
        addView(operationView);

        mDataList = new ArrayList(10);
        mListView = new ListView(context) {

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        };
        mListView.setBackgroundColor(0xFF699CC);
        mListView.setAdapter(mAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return mDataList.size();
            }

            @Override
            public Object getItem(int position) {
                return mDataList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ResultItemView itemView = null;
                if (convertView == null) {
                    itemView = new ResultItemView(getContext());
                } else {
                    itemView = (ResultItemView) convertView;
                }
                User model = (User) getItem(position);
                itemView.updateModel(model);
                return itemView;
            }
        });
        addView(mListView);
    }

    private void refreshResult() {
        User.queryAsync(User.class, null, new OOSqliteEntity.QueryCallback() {
            @Override
            public void onQuerySuccess(List list) {
                mDataList = list;

                post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    class OperationView extends LinearLayout {

        public OperationView(Context context) {
            super(context);

            setOrientation(LinearLayout.VERTICAL);

            LinearLayout queryLayout = new LinearLayout(context);
            addView(queryLayout);

            Button button = new Button(context);
            button.setText("Query all");
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshResult();
                }
            });
            queryLayout.addView(button);

            button = new Button(context);
            button.setText("Query 30+");
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDataList = User.queryAgeOlderThan30();
                    mAdapter.notifyDataSetChanged();
                }
            });
            queryLayout.addView(button);

            LinearLayout addLayout = new LinearLayout(context);
            addView(addLayout);

            final EditText nameEdit = new EditText(context);
            nameEdit.setHint("Input name");

            final EditText ageEdit = new EditText(context);
            ageEdit.setHint("Input age");

            button = new Button(context);
            button.setText("Add");
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = nameEdit.getText().toString();
                    int age = Integer.parseInt(ageEdit.getText().toString());
                    User.add(name, age);

                    refreshResult();

                    nameEdit.setText("");
                    ageEdit.setText("");
                    nameEdit.requestFocus();
                }
            });

            addLayout.addView(nameEdit);
            addLayout.addView(ageEdit);
            addLayout.addView(button);


        }
    }

    class ResultItemView extends FrameLayout {

        TextView mTextView;

        User mUser;

        public ResultItemView(Context context) {
            super(context);

            mTextView = new TextView(context);
            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
            addView(mTextView, layoutParams);

            LinearLayout actionLayout = new LinearLayout(context);
            actionLayout.setOrientation(LinearLayout.VERTICAL);
            layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
            addView(actionLayout, layoutParams);

            Button button = new Button(context);
            button.setText("Update");
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mUser.score += 1;
                    User.update(mUser);

                    refreshResult();
                }
            });
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            actionLayout.addView(button, btnParams);

            button = new Button(context);
            button.setText("Delete");
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    User.delete(mUser);

                    refreshResult();
                }
            });
            btnParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            actionLayout.addView(button, btnParams);
        }

        void updateModel(User appModel) {
            mUser = appModel;
            mTextView.setText(mUser.toString());
        }
    }
}
