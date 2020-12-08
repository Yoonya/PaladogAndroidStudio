package com.example.paladogproject;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Character2 extends Activity { //전반적인 것인 캐릭터1과 같다
    //context
    private Context character2Context;
    //stat
    private float maxHP = 75;
    private float hp = 75;
    private float damage; //직접 데미지를 주는 것은 bullet클래스이고 bullet 클래스에 데미지를 넘겨줄 것이기 때문에 넘겨줄 변수를 설정한다.
    private float defaultDamage = 15;
    private float skillDamage = 40;
    private float skillPercent = 30; // 스킬 쓸 확률
    private float skillRanNum;
    private float speed = PxToDp(35);
    private float coolTime = 3;
    private  float attackRange = PxToDp(3500); //공격범위로 충돌감지 범위에 붙힐 것이다.

    //트리거 및 이벤트
    private Runnable attack;
    private Runnable die;
    private ScheduledExecutorService attackService;
    private ScheduledFuture attackServiceFuture;
    private ScheduledExecutorService attackService2;
    private ScheduledFuture attackServiceFuture2;
    private ScheduledExecutorService dieService;
    private ScheduledFuture dieServiceFuture;
    //내 캐릭터
    private ImageView character2;
    //애니메이션
    private AnimationDrawable walkAnimation;
    private AnimationDrawable attackAnimation;
    private AnimationDrawable dieAnimation;
    private AnimationDrawable skillAnimation;
    private int animationFrame = 1000/10;
    private int dieCount = 10;// 캐릭터 죽을 때 투명도 처리
    //사용할 클래스 및 UI
    private GameManager gameManager;
    private Monster monster;
    private ImageView gameBackground1;
    private ImageView paladog;
    private ImageView enemybase;
    private TextView leftTouchCheck;
    private TextView rightTouchCheck;
    private CharacterAura characterAura;
    private Character2 me;
    private Bullet bullet; //캐릭터2는 총알과 함께한다
    //효과음
    private MediaPlayer mediaHitted;
    private MediaPlayer mediaDie1;
    private MediaPlayer mediaSkill1;

    //레이아웃선언
    private ConstraintLayout mainLinear;
    //변수
    private boolean isCharacterCollision = false;
    private boolean isEnemybaseCollision = false;
    private boolean changeAttackService = false;
    private boolean isOnAura = false;

    public Character2(Context context){
        this.character2Context = context;
        //mainlayout을 가져옴
        mainLinear = ((ConstraintLayout) ((Activity) character2Context).findViewById(R.id.activity_main));

        gameBackground1 = ((Activity) character2Context).findViewById(R.id.background1);
        paladog = ((Activity) character2Context).findViewById((int)3);
        enemybase = ((Activity) character2Context).findViewById((int)4);
        leftTouchCheck = ((Activity) character2Context).findViewById((int)1);
        rightTouchCheck = ((Activity) character2Context).findViewById((int)2);
        me = this;
        //캐릭터의 ImageView생성
        character2 = new ImageView(character2Context);

        mediaHitted = MediaPlayer.create(character2Context, R.raw.hit);
        mediaDie1 = MediaPlayer.create(character2Context, R.raw.character2_die);
        mediaSkill1 = MediaPlayer.create(character2Context, R.raw.character2_skill);


        //캐릭터 설정
        character2.setX(PxToDp(-1750)+gameBackground1.getX()); //최초 생성위치
        character2.setY(PxToDp(700));
        //character1.setLayoutParams(layoutParams); //레이아웃 크기
        character2.setVisibility(View.VISIBLE);//일단 보이게

        mainLinear.addView(character2, 10); //설정한 것을 mainlayout에 추가, 이 index는 적 진지 바로 위 쪽이다.

        characterAura = new CharacterAura(character2Context);
        characterAura.setCharacter2(this);
        characterAura.setGameManager(gameManager);
        characterAura.getThread().start();

        bullet = new Bullet(character2Context); //총알 선언
        bullet.setCharacter2(this); //자기 자신 설정해주고
        bullet.getThread().start(); //스레드 시작시켜주고

        //캐릭터 애니메이션
        walkAnimation = new AnimationDrawable();
        attackAnimation = new AnimationDrawable();
        dieAnimation = new AnimationDrawable();
        skillAnimation = new AnimationDrawable();

        BitmapDrawable walkFrame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_idle);
        BitmapDrawable walkFrame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_walk1);
        BitmapDrawable walkFrame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_walk2);
        BitmapDrawable walkFrame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_walk3);
        BitmapDrawable walkFrame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_walk4);
        BitmapDrawable walkFrame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_walk5);
        BitmapDrawable walkFrame7 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_walk6);
        BitmapDrawable walkFrame8 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_walk7);

        BitmapDrawable attackFrame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_attack1);
        BitmapDrawable attackFrame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_attack2);
        BitmapDrawable attackFrame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_attack3);
        BitmapDrawable attackFrame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_attack4);
        BitmapDrawable attackFrame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_attack5);
        BitmapDrawable attackFrame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_attack6);
        BitmapDrawable attackFrame7 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_idle);

        BitmapDrawable dieFrame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_die1);
        BitmapDrawable dieFrame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_die2);
        BitmapDrawable dieFrame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_die3);
        BitmapDrawable dieFrame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_die4);
        BitmapDrawable dieFrame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_die5);
        BitmapDrawable dieFrame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_die6);
        BitmapDrawable dieFrame7 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_die7);
        BitmapDrawable dieFrame8 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_die8);
        BitmapDrawable dieFrame9 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_die9);
        BitmapDrawable dieFrame10 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_die10);
        //공격이랑 모션 같음
        BitmapDrawable skillFrame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_attack1);
        BitmapDrawable skillFrame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_attack2);
        BitmapDrawable skillFrame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_attack3);
        BitmapDrawable skillFrame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_attack4);
        BitmapDrawable skillFrame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_attack5);
        BitmapDrawable skillFrame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_attack6);
        BitmapDrawable skillFrame7 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character2_idle);

        walkAnimation.addFrame(walkFrame1, animationFrame);
        walkAnimation.addFrame(walkFrame2, animationFrame);
        walkAnimation.addFrame(walkFrame3, animationFrame);
        walkAnimation.addFrame(walkFrame4, animationFrame);
        walkAnimation.addFrame(walkFrame5, animationFrame);
        walkAnimation.addFrame(walkFrame6, animationFrame);
        walkAnimation.addFrame(walkFrame7, animationFrame);
        walkAnimation.addFrame(walkFrame8, animationFrame);

        attackAnimation.addFrame(attackFrame1, animationFrame);
        attackAnimation.addFrame(attackFrame2, animationFrame);
        attackAnimation.addFrame(attackFrame3, animationFrame);
        attackAnimation.addFrame(attackFrame4, animationFrame);
        attackAnimation.addFrame(attackFrame5, animationFrame);
        attackAnimation.addFrame(attackFrame6, animationFrame);
        attackAnimation.addFrame(attackFrame7, animationFrame);

        dieAnimation.addFrame(dieFrame1, animationFrame);
        dieAnimation.addFrame(dieFrame2, animationFrame);
        dieAnimation.addFrame(dieFrame3, animationFrame);
        dieAnimation.addFrame(dieFrame4, animationFrame);
        dieAnimation.addFrame(dieFrame5, animationFrame);
        dieAnimation.addFrame(dieFrame6, animationFrame);
        dieAnimation.addFrame(dieFrame7, animationFrame);
        dieAnimation.addFrame(dieFrame8, animationFrame);
        dieAnimation.addFrame(dieFrame9, animationFrame);
        dieAnimation.addFrame(dieFrame10, animationFrame);

        skillAnimation.addFrame(skillFrame1, animationFrame);
        skillAnimation.addFrame(skillFrame2, animationFrame);
        skillAnimation.addFrame(skillFrame3, animationFrame);
        skillAnimation.addFrame(skillFrame4, animationFrame);
        skillAnimation.addFrame(skillFrame5, animationFrame);
        skillAnimation.addFrame(skillFrame6, animationFrame);
        skillAnimation.addFrame(skillFrame7, animationFrame);

        walkAnimation.setOneShot(false);
        attackAnimation.setOneShot(true); // 반복하지않음
        dieAnimation.setOneShot(true);
        skillAnimation.setOneShot(true);
        character2.setBackgroundDrawable(walkAnimation);
        walkAnimation.start();

    }

    public Handler characterTransitionHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) { //UI를 건들이는 것이라 따로 핸들링
            if (hp <= 0) {
                died();
                bullet.getThread().interrupt();
                die = new Runnable() { //die애니메이션
                    @Override
                    public void run() {
                        if (!dieAnimation.isRunning()) {
                            character2.setBackgroundDrawable(dieAnimation);
                            dieAnimation.start();
                        }
                        if (dieCount > 5) character2.setX(character2.getX() - PxToDp(350));
                        dieCount--;
                        if (dieCount <= 0) {
                            gameManager.getCharacter2Index().remove(me);
                            if(!dieServiceFuture.isCancelled()) dieServiceFuture.cancel(false);
                        }
                    }
                };
                if (dieService == null) {
                    dieService = Executors.newSingleThreadScheduledExecutor();
                    dieServiceFuture = dieService.scheduleAtFixedRate(die, 0, animationFrame, TimeUnit.MILLISECONDS);
                }
            }
            else {
                if (enemybaseCollision((int) character2.getX(), (int) enemybase.getX(), character2.getWidth(), enemybase.getWidth())) {
                    isEnemybaseCollision = true;
                }
                else {
                    isEnemybaseCollision = false;
                }
                if (gameManager.getMonsterIndex().size() > 0) {
                    for (int i = 0; i < gameManager.getMonsterIndex().size(); i++) {
                        if (monster == null) {
                        if (characterCollision((int) character2.getX(), (int) gameManager.getMonsterIndex().get(i).getMonster().getX(), character2.getWidth(), gameManager.getMonsterIndex().get(i).getMonster().getWidth())) {
                                monster = gameManager.getMonsterIndex().get(i);//겹쳐있더라도 때리던 놈 때리도록
                                isCharacterCollision = true;
                            }
                        }
                    }
                }
                if (isCharacterCollision && gameManager.getEnemybase().getHp() > 0 && gameManager.getMyCharacter().getHp() > 0) {//몬스터와 격돌시
                    if (walkAnimation.isRunning()) { //애니메이션 변경코드
                        walkAnimation.stop();
                    }
                    if (!attackAnimation.isRunning() && !skillAnimation.isRunning()) { //공격중이 아니라면
                        if(isOnAura){ //순서상 아래있는 animation.stop보다 먼저 해야하는 것을 확인 조금 어지럽지만 이렇게 한다.
                            Random ran = new Random();
                            skillRanNum = ran.nextFloat()*100 + 1; //스킬 발생확률
                            if(skillRanNum <= skillPercent){
                                character2.setBackgroundDrawable(skillAnimation);
                            }
                            else{
                                character2.setBackgroundDrawable(attackAnimation);
                            }
                        }
                        else {
                            character2.setBackgroundDrawable(attackAnimation);
                        }
                        attack = new Runnable() { //attack 이벤트 할 곳
                            @Override
                            public void run() {
                                if (hp > 0) {
                                    if (attackAnimation.isRunning())
                                        attackAnimation.stop(); // 이미 실행하고 있는 애니메이션 종료하고 시작
                                    if (skillAnimation.isRunning())
                                        skillAnimation.stop(); // 이미 실행하고 있는 애니메이션 종료하고 시작
                                    if (isOnAura) {
                                        Random ran = new Random();
                                        skillRanNum = ran.nextFloat() * 100 + 1; //스킬 발생확률
                                        if (skillRanNum <= skillPercent) {
                                            character2.setBackgroundDrawable(skillAnimation);
                                            damage = skillDamage; //스킬확률 일 때, 스킬데미지로 설정
                                            skillAnimation.start();//스킬
                                            bullet.setAttacking(true); //bullet에게 공격 허가
                                        } else {
                                            character2.setBackgroundDrawable(attackAnimation);
                                            damage = defaultDamage;//아니면 일반 데미지로 설정
                                            attackAnimation.start(); //공격
                                            bullet.setAttacking(true);//bullet에게 공격 허가
                                        }
                                    } else {
                                        character2.setBackgroundDrawable(attackAnimation);
                                        damage = defaultDamage;//아니면 일반 데미지로 설정
                                        attackAnimation.start(); //공격
                                        bullet.setAttacking(true);//bullet에게 공격 허가
                                    }

                                    if (monster.getHp() <= 0) { // 0이하가 되면        //몬스터는 여기서 관리하고 적진지는 총알에서 관리? -> 적 진지같이 게임이 끝나는경우 실시간으로 데미지를 주는 화살쪽에서
                                        monster.setHp(0); //0으로 설정                 //관리하는 것이 좋다고 판단, 몬스터는 죽이고 다시 이동해야하기 때문에 주체인 캐릭터2가 관리하는게 좋다고 판단
                                        isCharacterCollision = false; //죽였으니 충돌 끄기
                                        changeAttackService = true;//attackservice보다 늦게 실행되어야 해서 추가
                                        monster = null; //몬스터 삭제
                                    } else {
                                        if (skillAnimation.isRunning()) mediaSkill1.start();
                                    }
                                }
                            }
                        };
                        if (attackService == null) {//이걸 설정하지 않으면 중첩되서 멀티킬 시전함, 그것도 반복될때마다 +1을 더 죽여서 이상함
                            attackService = Executors.newSingleThreadScheduledExecutor();
                            attackServiceFuture = attackService.scheduleAtFixedRate(attack, 0, (long) coolTime * 1000, TimeUnit.MILLISECONDS); //캐릭터의 쿨타임마다 공격하도록
                        }
                        if (changeAttackService) {//attackservice보다 늦게 실행되어야 해서 추가
                            attackService = null;
                            changeAttackService = false;
                        }
                    }
                }
                else if(isEnemybaseCollision && gameManager.getEnemybase().getHp() > 0 && gameManager.getMyCharacter().getHp() > 0){ // 적 진지와 격돌시, 적 진지가 죽지 않았을 때
                    if (walkAnimation.isRunning()) { //위와 같음
                        walkAnimation.stop();
                    }
                    if (!attackAnimation.isRunning() && !skillAnimation.isRunning()) { //공격중이 아니라면
                        if(isOnAura){ //순서상 아래있는 animation.stop보다 먼저 해야하는 것을 확인 조금 어지럽지만 이렇게 한다.
                            Random ran = new Random();
                            skillRanNum = ran.nextFloat()*100 + 1; //스킬 발생확률
                            if(skillRanNum <= skillPercent){
                                character2.setBackgroundDrawable(skillAnimation);
                            }
                            else{
                                character2.setBackgroundDrawable(attackAnimation);
                            }
                        }
                        else {
                            character2.setBackgroundDrawable(attackAnimation);
                        }
                        attack = new Runnable() { //attack 이벤트 할 곳
                            @Override
                            public void run() {
                                if (hp > 0) {
                                    if (attackAnimation.isRunning())
                                        attackAnimation.stop(); // 이미 실행하고 있는 애니메이션 종료하고 시작
                                    if (skillAnimation.isRunning())
                                        skillAnimation.stop(); // 이미 실행하고 있는 애니메이션 종료하고 시작

                                    if (isOnAura) {
                                        Random ran = new Random();
                                        skillRanNum = ran.nextFloat() * 100 + 1; //스킬 발생확률
                                        if (skillRanNum <= skillPercent) {
                                            character2.setBackgroundDrawable(skillAnimation);
                                            damage = skillDamage; //스킬확률 일 때, 스킬데미지로 설정
                                            skillAnimation.start();//스킬
                                            bullet.setAttacking(true); //bullet에게 공격 허가
                                        } else {
                                            character2.setBackgroundDrawable(attackAnimation);
                                            damage = defaultDamage;//아니면 일반 데미지로 설정
                                            attackAnimation.start(); //공격
                                            bullet.setAttacking(true);//bullet에게 공격 허가
                                        }
                                    } else {
                                        character2.setBackgroundDrawable(attackAnimation);
                                        damage = defaultDamage;//아니면 일반 데미지로 설정
                                        attackAnimation.start(); //공격
                                        bullet.setAttacking(true);//bullet에게 공격 허가
                                    }

                                    if (skillAnimation.isRunning()) mediaSkill1.start();

                                }
                            }
                        };
                        attackService2 = Executors.newSingleThreadScheduledExecutor();
                        attackServiceFuture2 = attackService2.scheduleAtFixedRate(attack, 0, (long) coolTime * 1000, TimeUnit.MILLISECONDS); //캐릭터의 쿨타임마다 공격하도록

                    }
                }
                else {
                    if (attackAnimation.isRunning() || skillAnimation.isRunning()) {
                       if(attackService != null){
                           attackServiceFuture.cancel(false);
                       }
                        if(attackService2 != null){
                            attackServiceFuture2.cancel(false);
                        }
                        if(attackAnimation.isRunning()) attackAnimation.stop();
                        if(skillAnimation.isRunning()) skillAnimation.stop();
                    }
                    if (!walkAnimation.isRunning()) {
                        character2.setBackgroundDrawable(walkAnimation);
                        walkAnimation.start();
                    }
                    character2.setX(character2.getX() + speed);
                }

            }
            if (paladog.getX() <PxToDp(-700)) { //-200은 현재 화면왼쪽끝
                if (gameBackground1.getX() <PxToDp(-700)) {
                    if (leftTouchCheck.getText().toString() == "true") { //몬스터도 맵스크롤 처럼 이동되도록
                        character2.setX(character2.getX() + gameManager.getMyCharacter().getSpeed()+speed);
                    }
                }
            }
            if (paladog.getX() > PxToDp(6300)) { //화면 오른쪽끝
                if (gameBackground1.getX() > PxToDp(-10500)) {//배경끝
                    if (rightTouchCheck.getText().toString() == "true") {//이게 없으면
                        character2.setX(character2.getX() - gameManager.getMyCharacter().getSpeed()-speed);
                    }
                }
            }
        }
    };

    private boolean characterCollision(int x1, int x2, int width1, int width2){
        if(x1 > x2){//충돌 범위에 공격 범위를 붙힌다.
            if((x1 - x2) < width1/2 + width2/2 - PxToDp(2800)+ attackRange) { //이미지의 크기 구성상 이렇게 계산
                return true;
            }
        }
        else if (x1 < x2){
            if((x2 - x1)-PxToDp(473) < width1/2 + width2/2 - PxToDp(2800)+ attackRange) { //두 거리보다 가까워지면 충돌
                return true;
            }
        }
        return false;
    }

    private boolean enemybaseCollision(int x1, int x2, int width1, int width2){
        if(x1 > x2){
            if((x1 - x2) < width1/2 + width2/2- PxToDp(3150)+ attackRange) { //이미지의 크기 구성상 이렇게 계산
                return true;
            }
        }
        else if (x1 < x2){
            if((x2 - x1)-PxToDp(473) < width1/2 + width2/2- PxToDp(3150)+ attackRange) { //두 거리보다 가까워지면 충돌
                return true;
            }
        }
        return false;
    }

    public void attacked(float damage){
        hp -= damage;
        mediaHitted.start();
    }
    //px를 dp로 바꾸는거(여러 기기에 호환되도록)
    public static float PxToDp(float px){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return Math.round(dp);
    }
    public void died(){
        if(mediaDie1 != null) mediaDie1.start();
        if(attackService != null) {
            if(!attackServiceFuture.isCancelled())
                attackServiceFuture.cancel(false);
        }
        if(attackService2 != null) {
            if(!attackServiceFuture2.isCancelled())
                attackServiceFuture2.cancel(false);
        }
        if(walkAnimation.isRunning()) walkAnimation.stop();
        if(attackAnimation.isRunning()) attackAnimation.stop();
        if(skillAnimation.isRunning()) skillAnimation.stop();
    }


    public float getHp() { return hp; }

    public void setHp(float hp) { this.hp = hp; }

    public float getDamage() { return damage; }

    public ImageView getCharacter2() { return character2; }

    public GameManager getGameManager() { return gameManager; }

    public void setGameManager(GameManager gameManager) { this.gameManager = gameManager; }

    public void setOnAura(boolean onAura) { isOnAura = onAura; }

    public AnimationDrawable getAttackAnimation() { return attackAnimation; }

    public AnimationDrawable getSkillAnimation() { return skillAnimation; }

    public void setMediaDie1(MediaPlayer mediaDie1) { this.mediaDie1 = mediaDie1; }

}
