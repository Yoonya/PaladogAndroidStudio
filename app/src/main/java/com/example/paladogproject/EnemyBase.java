package com.example.paladogproject;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

public class EnemyBase  extends Activity {// 적 진지를 담당하는 클래스
    //context
    private Context enemyBaseContext;
    //stat
    private float maxHP = 500; //최대 체력
    private float hp = 500; // 현재 체력
    //이미지뷰
    private ImageView enemybase;
    //효과음
    private MediaPlayer mediaHitted;
    private MediaPlayer mediaAngry;
    private MediaPlayer mediaDie;
    //레이아웃선언
    private ConstraintLayout mainLinear;

    public EnemyBase(Context context) {
        this.enemyBaseContext = context;
        //변수
        mainLinear = ((ConstraintLayout) ((Activity) enemyBaseContext).findViewById(R.id.activity_main));

        enemybase = ((Activity) enemyBaseContext).findViewById(R.id.enemybase);
        mediaHitted = MediaPlayer.create(enemyBaseContext, R.raw.hit);
        mediaAngry = MediaPlayer.create(enemyBaseContext, R.raw.boss_angry);
        mediaDie = MediaPlayer.create(enemyBaseContext, R.raw.boss_dead);
    }
    public void attacked(float damage){
        hp -= damage;
        mediaHitted.start();
    }

    public float getMaxHP() {
        return maxHP;
    }

    public float getHp() {
        return hp;
    }

    public void setHp(float hp) {
        this.hp = hp;
    }

    public ImageView getEnemybase() {
        return enemybase;
    }

    public ConstraintLayout getMainLinear() {
        return mainLinear;
    }

    public void setMainLinear(ConstraintLayout mainLinear) {
        this.mainLinear = mainLinear;
    }

    public MediaPlayer getMediaDie() {
        return mediaDie;
    }

    public MediaPlayer getMediaAngry() { return mediaAngry; }

}
