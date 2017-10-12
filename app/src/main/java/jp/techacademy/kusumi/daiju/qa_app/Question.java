package jp.techacademy.kusumi.daiju.qa_app;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by USER on 2017/10/04.
 */

public class Question implements Serializable {
    private String mTitle;
    private String mBody;
    private String mName;
    private String mUid;
    private String mQuestionUid;
    private int mGenreSelected;
    private String mGenreNo;

    private String mGenreName;
    private byte[] mBitmapArray;
    private ArrayList<Answer> mAnswerArrayList;

    public String getTitle() {
        return mTitle;
    }

    public String getBody() {
        return mBody;
    }

    public String getName() {
        return mName;
    }

    public String getUid() {
        return mUid;
    }

    public String getQuestionUid() {
        return mQuestionUid;
    }

    public int getGenreSelected() {
        return mGenreSelected;
    }
    public String getGenreNo() { return mGenreNo;}
    public String getGenreName() {
        return mGenreName;
    }

    public byte[] getImageBytes() {
        return mBitmapArray;
    }

    public ArrayList<Answer> getAnswers() {
        return mAnswerArrayList;
    }

    public Question(String title, String body, String name, String uid, String questionUid, int genreSelected, String genre, String genreName, byte[] bytes, ArrayList<Answer> answers) {
        mTitle = title;
        mBody = body;
        mName = name;
        mUid = uid;
        mQuestionUid = questionUid;
        mGenreSelected = genreSelected;
        mGenreNo = genre;
        mGenreName = genreName;
        mBitmapArray = bytes.clone();
        mAnswerArrayList = answers;
    }
}