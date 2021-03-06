package jp.techacademy.kusumi.daiju.qa_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private int mGenreSelected;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mGenreSelectedRef;
    private DatabaseReference mFavoriteRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    ArrayList<Answer> mAnswerArrayList;
    private QuestionsListAdapter mAdapter;
    private FirebaseUser user;
    Map<String, String> mFavoriteMap = new HashMap<>();
    private Boolean mFavoriteFlag;
    ProgressDialog mProgress;

    private ValueEventListener mFavoriteEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String questionId;
            String genre;

            if (map != null) {
                for (Object key : map.keySet()) {
                    questionId = key.toString();
                    HashMap tempGenre = (HashMap) dataSnapshot.child(key.toString()).getValue();
                    genre = tempGenre.get("genre").toString();
                    mFavoriteMap.put(questionId, genre);
                }
            }

            for (int i = 1; i <= 4; i++) {
                mGenreSelected = i;
                mGenreSelectedRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenreSelected));
                mGenreSelectedRef.addChildEventListener(mEventListener);
            }
            mGenreSelected = 100;
        }


        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            //mFavoriteMapでgenreが一致するノードで、質問IDが一致するところでGetValue

            if ((mFavoriteFlag == true && mFavoriteMap.get(dataSnapshot.getKey()) != null) || mFavoriteFlag == false) {
                HashMap map = (HashMap) dataSnapshot.getValue();
                String title = (String) map.get("title");
                String genre =(String) map.get("genre");
                String body = (String) map.get("body");
                String name = (String) map.get("name");
                String uid = (String) map.get("uid");
                String imageString = (String) map.get("image");
                String genreName = (String) map.get("genreName");

                byte[] bytes;
                if (imageString != null) {
                    bytes = Base64.decode(imageString, Base64.DEFAULT);
                } else {
                    bytes = new byte[0];
                }

                mAnswerArrayList = new ArrayList<Answer>();
                HashMap answerMap = (HashMap) map.get("answers");
                if (answerMap != null) {
                    for (Object key : answerMap.keySet()) {
                        HashMap temp = (HashMap) answerMap.get((String) key);
                        String answerBody = (String) temp.get("body");
                        String answerName = (String) temp.get("name");
                        String answerUid = (String) temp.get("uid");
                        Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                        mAnswerArrayList.add(answer);
                    }
                }
                Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenreSelected, genre, genreName, bytes, mAnswerArrayList);
                mQuestionArrayList.add(question);
                mAdapter.notifyDataSetChanged();

            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            // 変更があったQuestionを探す
            for (Question question: mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {

                 // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.getAnswers().clear();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // ログイン済みのユーザーを取得する
        user = FirebaseAuth.getInstance().getCurrentUser();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGenreSelected == 0 || mGenreSelected == 100) {
                    Snackbar.make(view, "ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // ジャンルを渡して質問作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), QuestionSendActivity.class);
                    intent.putExtra("genre", mGenreSelected);
                    startActivity(intent);
                }
            }
        });

        // ナビゲーションドロワーの設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_all_questions) {
                    mToolbar.setTitle("全ての質問");
                    mGenreSelected = 0;
                } else if (id == R.id.nav_favorite) {
                    mToolbar.setTitle("お気に入り");
                    mGenreSelected = 100;
                } else if (id == R.id.nav_hobby) {
                    mToolbar.setTitle("趣味");
                    mGenreSelected = 1;
                } else if (id == R.id.nav_life) {
                    mToolbar.setTitle("生活");
                    mGenreSelected = 2;
                } else if (id == R.id.nav_health) {
                    mToolbar.setTitle("健康");
                    mGenreSelected = 3;
                } else if (id == R.id.nav_compter) {
                    mToolbar.setTitle("コンピューター");
                    mGenreSelected = 4;
                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);

                // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
                mQuestionArrayList.clear();
                mAdapter.setQuestionArrayList(mQuestionArrayList);
                mListView.setAdapter(mAdapter);

                //お気に入りの一覧表示の場合の処理
                callGenreMethod();

                return true;
            }

        });

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.notifyDataSetChanged();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Questionのインスタンスを渡して質問詳細画面を起動する
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();

        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mQuestionArrayList.clear();
        mAdapter.setQuestionArrayList(mQuestionArrayList);
        mListView.setAdapter(mAdapter);

        callGenreMethod();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void callGenreMethod() {
        //プログレスバーのテキストを設定
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("データ取得中...");
        mProgress.show();

        // 選択したジャンルにリスナーを登録する
        if (mGenreSelectedRef != null) {
            mGenreSelectedRef.removeEventListener(mEventListener);
        }

        if (mGenreSelected == 0) {
            mFavoriteFlag = false;
            for (int i = 1; i <= 4; i++) {
                mGenreSelected = i;
                mGenreSelectedRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenreSelected));
                mGenreSelectedRef.addChildEventListener(mEventListener);
            }
            mGenreSelected = 0;
        } else if (mGenreSelected == 100) {
            mFavoriteFlag = true;
            //ログイン中のユーザーのユーザーIDを取得
            mFavoriteMap = new HashMap<>();
            if (user != null) {
                mFavoriteRef = mDatabaseReference.child(Const.FavoritesPATH).child(user.getUid());
                mFavoriteRef.addListenerForSingleValueEvent(mFavoriteEventListener);
            }

        } else {
            mFavoriteFlag = false;
            mGenreSelectedRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenreSelected));
            mGenreSelectedRef.addChildEventListener(mEventListener);
        }

        mProgress.dismiss();
    }

}
