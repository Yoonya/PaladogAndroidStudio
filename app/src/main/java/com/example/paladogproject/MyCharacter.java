package com.example.paladogproject;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

public class MyCharacter extends Activity { //내 캐릭터(팔라독) 클래스
    //context
    private Context myCharacterContext;
    //stat
    private float maxHP = 500;//최대체력
    private float hp = 500;//현재체력
    private float speed = PxToDp(53);
    //내 캐릭터
    private ImageView paladog;
    //애니메이션
    private AnimationDrawable rightWalkAnimation;
    private AnimationDrawable leftWalkAnimation;
    private AnimationDrawable idleAnimation;
    private AnimationDrawable attackAnimation;
    private int animationFrame = 1000/15;
    //클래스
    private GameManager gameManager;
    private MyCharacterAura myCharacterAura;
    //효과음
    private MediaPlayer mediaHitted;
    private MediaPlayer mediaDie;
    //레이아웃선언
    private ConstraintLayout mainLinear;
    private LinearLayout.LayoutParams layoutParams = //팔라독의 크기를 설정
            new LinearLayout.LayoutParams(650, 650, 1f);

    public MyCharacter(Context context) {
        this.myCharacterContext = context;
        //변수
        mainLinear = ((ConstraintLayout) ((Activity) myCharacterContext).findViewById(R.id.activity_main));

        paladog = new ImageView(myCharacterContext);

        //내 캐릭터 설정
        paladog.setX(PxToDp(700)); //최초 위치
        paladog.setY(PxToDp(700));
        paladog.setLayoutParams(layoutParams); //레이아웃 크기
        paladog.setVisibility(View.GONE);

        mainLinear.addView(paladog, 9); //배경 위

        mediaHitted = MediaPlayer.create(myCharacterContext, R.raw.hit);
        mediaDie = MediaPlayer.create(myCharacterContext, R.raw.paladog_die);

        //아우라 설정
        myCharacterAura = new MyCharacterAura(myCharacterContext);
        myCharacterAura.setPaladog(paladog);
        myCharacterAura.setGameManager(gameManager);
        myCharacterAura.getThread().start();

        //캐릭터 애니메이션
        idleAnimation = new AnimationDrawable();
        rightWalkAnimation = new AnimationDrawable();
        leftWalkAnimation = new AnimationDrawable();
        attackAnimation = new AnimationDrawable();

        BitmapDrawable idleFrame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_idle1);
        BitmapDrawable idleFrame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_idle2);
        BitmapDrawable idleFrame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_idle3);
        BitmapDrawable idleFrame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_idle4);
        BitmapDrawable idleFrame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_idle5);
        BitmapDrawable idleFrame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_idle6);
        BitmapDrawable idleFrame7 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_idle7);
        BitmapDrawable idleFrame8 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_idle8);
        BitmapDrawable idleFrame9 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_idle9);
        BitmapDrawable idleFrame10 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_idle10);

        BitmapDrawable rightwalkFrame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_rightwalk1);
        BitmapDrawable rightwalkFrame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_rightwalk2);
        BitmapDrawable rightwalkFrame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_rightwalk3);
        BitmapDrawable rightwalkFrame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_rightwalk4);
        BitmapDrawable rightwalkFrame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_rightwalk5);
        BitmapDrawable rightwalkFrame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_rightwalk6);
        BitmapDrawable rightwalkFrame7 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_rightwalk7);
        BitmapDrawable rightwalkFrame8 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_rightwalk8);
        BitmapDrawable rightwalkFrame9 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_rightwalk9);

        BitmapDrawable leftwalkFrame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_leftwalk1);
        BitmapDrawable leftwalkFrame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_leftwalk2);
        BitmapDrawable leftwalkFrame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_leftwalk3);
        BitmapDrawable leftwalkFrame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_leftwalk4);
        BitmapDrawable leftwalkFrame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_leftwalk5);
        BitmapDrawable leftwalkFrame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_leftwalk6);
        BitmapDrawable leftwalkFrame7 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_leftwalk7);
        BitmapDrawable leftwalkFrame8 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_leftwalk8);
        BitmapDrawable leftwalkFrame9 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_leftwalk9);


        BitmapDrawable attackFrame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_attack1);
        BitmapDrawable attackFrame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_attack2);
        BitmapDrawable attackFrame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_attack3);
        BitmapDrawable attackFrame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_attack4);
        BitmapDrawable attackFrame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_attack5);
        BitmapDrawable attackFrame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_attack6);
        BitmapDrawable attackFrame7 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_attack7);
        BitmapDrawable attackFrame8 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_attack8);
        BitmapDrawable attackFrame9 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_idle1);

        idleAnimation.addFrame(idleFrame1, animationFrame);
        idleAnimation.addFrame(idleFrame2, animationFrame);
        idleAnimation.addFrame(idleFrame3, animationFrame);
        idleAnimation.addFrame(idleFrame4, animationFrame);
        idleAnimation.addFrame(idleFrame5, animationFrame);
        idleAnimation.addFrame(idleFrame6, animationFrame);
        idleAnimation.addFrame(idleFrame7, animationFrame);
        idleAnimation.addFrame(idleFrame8, animationFrame);
        idleAnimation.addFrame(idleFrame9, animationFrame);
        idleAnimation.addFrame(idleFrame10, animationFrame);

        rightWalkAnimation.addFrame(rightwalkFrame1, animationFrame);
        rightWalkAnimation.addFrame(rightwalkFrame2, animationFrame);
        rightWalkAnimation.addFrame(rightwalkFrame3, animationFrame);
        rightWalkAnimation.addFrame(rightwalkFrame4, animationFrame);
        rightWalkAnimation.addFrame(rightwalkFrame5, animationFrame);
        rightWalkAnimation.addFrame(rightwalkFrame6, animationFrame);
        rightWalkAnimation.addFrame(rightwalkFrame7, animationFrame);
        rightWalkAnimation.addFrame(rightwalkFrame8, animationFrame);
        rightWalkAnimation.addFrame(rightwalkFrame9, animationFrame);

        leftWalkAnimation.addFrame(leftwalkFrame1, animationFrame);
        leftWalkAnimation.addFrame(leftwalkFrame2, animationFrame);
        leftWalkAnimation.addFrame(leftwalkFrame3, animationFrame);
        leftWalkAnimation.addFrame(leftwalkFrame4, animationFrame);
        leftWalkAnimation.addFrame(leftwalkFrame5, animationFrame);
        leftWalkAnimation.addFrame(leftwalkFrame6, animationFrame);
        leftWalkAnimation.addFrame(leftwalkFrame7, animationFrame);
        leftWalkAnimation.addFrame(leftwalkFrame8, animationFrame);
        leftWalkAnimation.addFrame(leftwalkFrame9, animationFrame);

        attackAnimation.addFrame(attackFrame1, animationFrame);
        attackAnimation.addFrame(attackFrame2, animationFrame);
        attackAnimation.addFrame(attackFrame3, animationFrame);
        attackAnimation.addFrame(attackFrame4, animationFrame);
        attackAnimation.addFrame(attackFrame5, animationFrame);
        attackAnimation.addFrame(attackFrame6, animationFrame);
        attackAnimation.addFrame(attackFrame7, animationFrame);
        attackAnimation.addFrame(attackFrame8, animationFrame);
        attackAnimation.addFrame(attackFrame9, animationFrame);


        //기본 default로 idleAnimation으로 설정
        paladog.setBackgroundDrawable(idleAnimation);
        //모든 애니메이션은 반복하도록 설정
        idleAnimation.setOneShot(false);
        leftWalkAnimation.setOneShot(false);
        rightWalkAnimation.setOneShot(false);
        attackAnimation.setOneShot(true);//얘는 반복안함
    }
    public void attacked(float damage){ //맞음
        hp -= damage;
        mediaHitted.start();
    }
    //px를 dp로 바꾸는거(여러 기기에 호환되도록)
    public static float PxToDp(float px){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return Math.round(dp);
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

    public float getSpeed() {
        return speed;
    }

    public ImageView getPaladog() {
        return paladog;
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public AnimationDrawable getRightWalkAnimation() { return rightWalkAnimation; }

    public AnimationDrawable getLeftWalkAnimation() { return leftWalkAnimation; }

    public AnimationDrawable getIdleAnimation() { return idleAnimation; }

    public AnimationDrawable getAttackAnimation() { return attackAnimation; }

    public MediaPlayer getMediaDie() { return mediaDie; }
}

