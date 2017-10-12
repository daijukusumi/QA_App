package jp.techacademy.kusumi.daiju.qa_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.valueOf;

/**
 * Created by USER on 2017/10/04.
 */

public class QuestionDetailListAdapter extends BaseAdapter implements View.OnClickListener {

    private final static int TYPE_QUESTION = 0;
    private final static int TYPE_ANSWER = 1;

    private LayoutInflater mLayoutInflater = null;
    private Question mQustion;
    private Boolean mFavoriteFlag = false;

    Button favoriteButton;
    TextView favoriteText;

    FirebaseUser user;

    DatabaseReference dataBaseReference;
    DatabaseReference mGenreRef;
    DatabaseReference mFavoriteRef;

    ArrayList<Answer> answerArrayList;

    Map<String,String> questionData;
    Map<String ,List> answerData;

    public QuestionDetailListAdapter(Context context, Question question) {
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mQustion = question;
    }

    @Override
    public int getCount() {
        return 1 + mQustion.getAnswers().size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_QUESTION;
        } else {
            return TYPE_ANSWER;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return mQustion;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_question_detail, parent, false);
            }
            String body = mQustion.getBody();
            String name = mQustion.getName();

            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);

            byte[] bytes = mQustion.getImageBytes();
            if (bytes.length != 0) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
                imageView.setImageBitmap(image);
            }

           favoriteButton = (Button) convertView.findViewById(R.id.favoriteButton);
           favoriteText = (TextView) convertView.findViewById(R.id.favoriteTextView);
           favoriteButton.setOnClickListener(this);


            // ログイン済みのユーザーを取得する
            user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                dataBaseReference = FirebaseDatabase.getInstance().getReference();
                mGenreRef = dataBaseReference.child(Const.ContentsPATH).child(valueOf(mQustion.getGenreNo()));
                mFavoriteRef = dataBaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQustion.getQuestionUid());
            }

            if (user == null) {
                // ログインしていなければ、ボタンとテキストを非表示
                favoriteButton.setVisibility(View.GONE);
                favoriteText.setVisibility(View.GONE);
            } else {
                //Firebaseのログインユーザーのお気に入りにアクセス

                mFavoriteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Object favoriteObject = dataSnapshot.getValue();

                        if (favoriteObject == null){
                            mFavoriteFlag = false;
                            checkFavorite();
                        } else {
                            mFavoriteFlag = true;
                            checkFavorite();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        } else {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_answer, parent, false);
            }

            Answer answer = mQustion.getAnswers().get(position - 1);
            String body = answer.getBody();
            String name = answer.getName();

            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);
        }

        return convertView;
    }

    @Override
    public void onClick(View view) {

        // ログイン済みのユーザーを取得する
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (mFavoriteFlag == false) {
            Map<String, String> data = new HashMap<String, String>();
            data.put("genre", mQustion.getGenreNo());
            mFavoriteRef.setValue(data);
            mFavoriteFlag = true;

        } else {
            mFavoriteRef.removeValue();
            mFavoriteFlag = false;
        }

        checkFavorite();

    }
    private void checkFavorite() {

        if (mFavoriteFlag == true) {
            favoriteButton.setBackgroundResource(android.R.drawable.checkbox_off_background);
            favoriteText.setText(R.string.defavorite_text);
        } else if(mFavoriteFlag == false){
            favoriteButton.setBackgroundResource(android.R.drawable.checkbox_on_background);
            favoriteText.setText(R.string.favorite_text);
        }
    }
}
