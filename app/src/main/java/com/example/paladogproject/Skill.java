package com.example.paladogproject;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Skill extends Activity { //내 캐릭터가 사용하는 스킬의 클래스
    //context
    private Context skillContext;
    //stat
    private float damage = 50; //스킬뎀
    private float speed = PxToDp(105); //날아가는 속도
    //스킬
    private ImageView paladog;
    private ImageView enemybase;
    private ImageView skill1;
    private ImageView skillEffect;//스킬을 쏠 때, 생기는 이펙트
    //애니메이션
    private Thread thread;
    private AnimationDrawable skillAnimation;
    private AnimationDrawable skillEffectAnimation;
    private int animationFrame = 1000/15;
    //클래스
    private GameManager gameManager;
    private Monster monster;
    //레이아웃선언
    private ConstraintLayout mainLinear;
    //변수
    private boolean isCharacterCollision = false;
    private boolean isEnemybaseCollision = false;

    public Skill(Context context) {
        this.skillContext = context;
        //변수
        mainLinear = ((ConstraintLayout) ((Activity) skillContext).findViewById(R.id.activity_main));

        skill1 = new ImageView(skillContext);
        skillEffect = new ImageView(skillContext);

        paladog = ((Activity) skillContext).findViewById((int)3);
        enemybase = ((Activity) skillContext).findViewById((int)4);

        skill1.setX(paladog.getX()+paladog.getWidth()/2);
        skill1.setY(paladog.getY()+paladog.getHeight()/3); //이미지 자료가 그런식이라 이렇게 설정한다.
        skillEffect.setX(paladog.getX()+paladog.getWidth()*2/3); //이미지 자료가 그런식이라 이렇게 설정한다.
        skillEffect.setY(paladog.getY()+paladog.getHeight()/4);//이미지 자료가 그런식이라 이렇게 설정한다.

        mainLinear.addView(skillEffect, 10); //적어도 팔라독보다 위
        mainLinear.addView(skill1, 10); //적어도 팔라독보다 위

        //애니메이션
        skillAnimation = new AnimationDrawable();
        skillEffectAnimation = new AnimationDrawable();

        BitmapDrawable skillFrame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_skill1);
        BitmapDrawable skillFrame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_skill2);
        BitmapDrawable skillFrame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_skill3);
        BitmapDrawable skillFrame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_skill4);
        BitmapDrawable skillFrame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_skill5);

        BitmapDrawable skillEffectFrame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_skilleffect1);
        BitmapDrawable skillEffectFrame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_skilleffect2);
        BitmapDrawable skillEffectFrame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_skilleffect3);
        BitmapDrawable skillEffectFrame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_skilleffect4);
        BitmapDrawable skillEffectFrame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_skilleffect5);
        BitmapDrawable skillEffectFrame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.paladog_skilleffect6);

        skillAnimation.addFrame(skillFrame1, animationFrame);
        skillAnimation.addFrame(skillFrame2, animationFrame);
        skillAnimation.addFrame(skillFrame3, animationFrame);
        skillAnimation.addFrame(skillFrame4, animationFrame);
        skillAnimation.addFrame(skillFrame5, animationFrame);

        skillEffectAnimation.addFrame(skillEffectFrame1, animationFrame);
        skillEffectAnimation.addFrame(skillEffectFrame2, animationFrame);
        skillEffectAnimation.addFrame(skillEffectFrame3, animationFrame);
        skillEffectAnimation.addFrame(skillEffectFrame4, animationFrame);
        skillEffectAnimation.addFrame(skillEffectFrame5, animationFrame);
        skillEffectAnimation.addFrame(skillEffectFrame6, animationFrame);

        skill1.setBackgroundDrawable(skillAnimation);
        skillEffect.setBackgroundDrawable(skillEffectAnimation);
        skillAnimation.setOneShot(false);
        skillEffectAnimation.setOneShot(true);

        thread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(animationFrame);
                        skillHandler.sendMessage(skillHandler.obtainMessage());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        });

        thread.start();
    }

    Handler skillHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //애니메이션 시작
            skillAnimation.start();
            skillEffectAnimation.start();
            skill1.setX(skill1.getX() + speed); //날아가

            if (gameManager.getMonsterIndex().size() > 0) {
                if (monster == null) {//null일때만 탐지
                  for (int i = 0; i < gameManager.getMonsterIndex().size(); i++) {
                    if (characterCollision((int) skill1.getX(), (int) gameManager.getMonsterIndex().get(i).getMonster().getX(), skill1.getWidth(), gameManager.getMonsterIndex().get(i).getMonster().getWidth())) {
                        if(gameManager.getMonsterIndex().get(i).getMonster().getX()>paladog.getX()){ //이상하게 스킬 값이 벗어나서 팔라독 뒤에 있는 적은 안맞도록 설정한다.
                                monster = gameManager.getMonsterIndex().get(i);
                                isCharacterCollision = true;
                            }
                        }
                    }
                }
            }
            if (isCharacterCollision) {//몬스터와 충돌시
                if (skillAnimation.isRunning()) { //애니메이션 변경코드
                    skillAnimation.stop();
                }
                monster.attacked(damage);
                if (monster.getHp() <= 0) { // 0이하가 되면
                    monster.setHp(0); //0으로 설정
                }
                isCharacterCollision = false;
                thread.interrupt(); //스킬 정지하고 안보이게
                skill1.setVisibility(View.GONE);
            }

            if (enemybaseCollision((int) skill1.getX(), (int) enemybase.getX(), skill1.getWidth(), enemybase.getWidth())) { //위와 같은 방식
                isEnemybaseCollision = true;
            }
            else {
                isEnemybaseCollision = false;
            }
            if (isEnemybaseCollision) {//적 진지와 격돌시
                if (skillAnimation.isRunning()) { //애니메이션 변경코드
                    skillAnimation.stop();
                }
                gameManager.getEnemybase().attacked(damage);

                if(gameManager.getEnemybase().getHp() < gameManager.getEnemybase().getMaxHP()*2/5) {//40퍼센트 이하
                    if(!gameManager.isPhase2()) gameManager.phase2(); //페이지2시작
                }

                if (gameManager.getEnemybase().getHp() <= 0) { // 0이하가 되면
                    gameManager.getEnemybase().setHp(0); //0으로 설정
                }
                else{
                    gameManager.getMapGaze2().getLayoutParams().width = (int)(gameManager.getMapGaze2Width()*gameManager.getEnemybase().getHp()/gameManager.getEnemybase().getMaxHP()); //맵 게이지에 있는 적진지 체력바 줄이기
                }
                isEnemybaseCollision = false;
                thread.interrupt();
                skill1.setVisibility(View.GONE);
            }
        }
    };

    private boolean characterCollision(int x1, int x2, int width1, int width2){
        if((x2 - x1) < width1/2 + width2/2 -PxToDp(2275)) { //공격 방향은 하나
            return true;
        }
        return false;
    }
    private boolean enemybaseCollision(int x1, int x2, int width1, int width2){
        if((x2 - x1) < width1/2 + width2/2 - PxToDp(2975)) { //두 거리보다 가까워지면 충돌
            return true;
        }
        return false;
    }
    //px를 dp로 바꾸는거(여러 기기에 호환되도록)
    public static float PxToDp(float px){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return Math.round(dp);
    }
    public void setGameManager(GameManager gameManager) { this.gameManager = gameManager; }

    public ImageView getSkill1() { return skill1; }
}
