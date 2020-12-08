package com.example.paladogproject;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Handler;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class Monster extends Activity { //몬스터 클래스, 대부분의 것이 캐릭터와 같다. 대부분의 것도 다르지만
    //context
    private Context monsterContext;
    //스탯이 캐릭터와 다르게 이런 식인 이유는 몬스터는 1과 2를 동시에 처리했다.
    // 이유는 캐릭터 1과 2는 원거리딜, 근거리딜 차이가 확실하지만 얘들은 별 차이가 없어서 이게 편하다.

    //monsterstat -> 아래 스탯 중에서 받아옴, 실제로 쓸 변수들
    private float maxHP;
    private float hp;
    private float damage;
    private float speed;
    private float coolTime;
    //monster1stat - 일반몬스터
    private float maxHP1 = 100;
    private float hp1 = 100;
    private float damage1 = 10;
    private float speed1 = PxToDp(35);
    private float coolTime1 = 3;
    //monster2stat - 강화몬스터
    private float maxHP2 = 150;
    private float hp2 = 150;
    private float damage2 = 15;
    private float speed2 = PxToDp(53);
    private float coolTime2 = 2;
    //내 캐릭터
    private ImageView monster;

    //애니메이션
    private AnimationDrawable walkAnimation;
    private AnimationDrawable attackAnimation;
    private AnimationDrawable dieAnimation;
    private int animationFrame = 1000/10;
    private int dieCount = 10;// 몬스터 죽을 때 투명도 처리
    //트리거 및 이벤트
    private Thread thread;
    private Runnable attack;
    private Runnable die;
    private ScheduledExecutorService attackService; //캐릭터 칠때
    private ScheduledFuture attackServiceFuture;
    private ScheduledExecutorService attackService2;//팔라독 칠때
    private ScheduledFuture attackServiceFuture2;
    private ScheduledExecutorService dieService;
    private ScheduledFuture dieServiceFuture;
    //사용할 클래스 및 UI
    private GameManager gameManager;
    private Character1 character1;
    private Character2 character2;
    private ImageView gameBackground1;
    private ImageView paladog;
    private TextView leftTouchCheck; //마찬가지로 맵 스크롤의 영향을 받는다.
    private TextView rightTouchCheck;
    private Monster me;
    //레이아웃선언
    private ConstraintLayout mainLinear;
    //변수
    private boolean isPaladogCollision = false;
    private boolean isCharacter1Collision = false;
    private boolean isCharacter2Collision = false;
    private boolean changeAttackService = false;
    //효과음
    private MediaPlayer mediaHitted;
    private MediaPlayer mediaDie1;
    private MediaPlayer mediaAttack1;


    public Monster(Context context){
        this.monsterContext = context;
        //mainlayout을 가져옴
        mainLinear = ((ConstraintLayout) ((Activity) monsterContext).findViewById(R.id.activity_main));

        gameBackground1 = ((Activity) monsterContext).findViewById(R.id.background1);
        paladog = ((Activity) monsterContext).findViewById((int)3); //순서 상의 문제인지 gameManager.myCharacter.paladog을 집어넣으면 null이 받아진다.
        leftTouchCheck = ((Activity) monsterContext).findViewById((int)1);
        rightTouchCheck = ((Activity) monsterContext).findViewById((int)2);
        me = this;

        Random random = new Random();

        //캐릭터의 ImageView생성
        monster = new ImageView(monsterContext);

        //몬스터 설정
        monster.setX(PxToDp(18200)+gameBackground1.getX()); //생성 위치
        monster.setY(PxToDp(700));
        monster.setVisibility(View.VISIBLE);

        mainLinear.addView(monster, 10); //최소 배경 위

        mediaHitted = MediaPlayer.create(monsterContext, R.raw.hit);
        mediaDie1 = MediaPlayer.create(monsterContext, R.raw.monster_die);
        mediaAttack1 = MediaPlayer.create(monsterContext, R.raw.monster_attack);
        //몬스터 애니메이션
        walkAnimation = new AnimationDrawable();
        attackAnimation = new AnimationDrawable();
        dieAnimation = new AnimationDrawable();

        BitmapDrawable walk1Frame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_walk1);
        BitmapDrawable walk1Frame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_walk2);
        BitmapDrawable walk1Frame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_walk3);
        BitmapDrawable walk1Frame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_walk4);
        BitmapDrawable walk1Frame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_walk5);
        BitmapDrawable walk1Frame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_walk6);
        BitmapDrawable walk1Frame7 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_walk7);
        BitmapDrawable walk1Frame8 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_walk8);
        BitmapDrawable walk1Frame9 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_walk9);

        BitmapDrawable attack1Frame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_attack1);
        BitmapDrawable attack1Frame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_attack2);
        BitmapDrawable attack1Frame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_attack3);
        BitmapDrawable attack1Frame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_attack4);
        BitmapDrawable attack1Frame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_attack5);
        BitmapDrawable attack1Frame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_attack6);

        BitmapDrawable die1Frame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_die1);
        BitmapDrawable die1Frame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_die2);
        BitmapDrawable die1Frame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_die3);
        BitmapDrawable die1Frame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_die4);
        BitmapDrawable die1Frame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_die5);
        BitmapDrawable die1Frame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_die6);
        BitmapDrawable die1Frame7 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_die7);
        BitmapDrawable die1Frame8 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_die8);
        BitmapDrawable die1Frame9 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_die9);
        BitmapDrawable die1Frame10 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster1_die10);

        BitmapDrawable walk2Frame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_walk1);
        BitmapDrawable walk2Frame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_walk2);
        BitmapDrawable walk2Frame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_walk3);
        BitmapDrawable walk2Frame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_walk4);
        BitmapDrawable walk2Frame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_walk5);
        BitmapDrawable walk2Frame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_walk6);
        BitmapDrawable walk2Frame7 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_walk7);
        BitmapDrawable walk2Frame8 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_walk8);
        BitmapDrawable walk2Frame9 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_walk9);

        BitmapDrawable attack2Frame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_attack1);
        BitmapDrawable attack2Frame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_attack2);
        BitmapDrawable attack2Frame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_attack3);
        BitmapDrawable attack2Frame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_attack4);
        BitmapDrawable attack2Frame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_attack5);
        BitmapDrawable attack2Frame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_attack6);

        BitmapDrawable die2Frame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_die1);
        BitmapDrawable die2Frame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_die2);
        BitmapDrawable die2Frame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_die3);
        BitmapDrawable die2Frame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_die4);
        BitmapDrawable die2Frame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_die5);
        BitmapDrawable die2Frame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_die6);
        BitmapDrawable die2Frame7 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_die7);
        BitmapDrawable die2Frame8 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_die8);
        BitmapDrawable die2Frame9 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_die9);
        BitmapDrawable die2Frame10 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.monster2_die10);

        //이 아래에서 자신이 어떤 몬스터로 생성될지 결정된다.

        if(random.nextInt(100)+1 <= 30){ //30퍼센트의 확률로 몬스터2
            maxHP = maxHP2;
            hp = hp2;
            damage = damage2;
            speed = speed2;
            coolTime = coolTime2;

            walkAnimation.addFrame(walk2Frame1, animationFrame);
            walkAnimation.addFrame(walk2Frame2, animationFrame);
            walkAnimation.addFrame(walk2Frame3, animationFrame);
            walkAnimation.addFrame(walk2Frame4, animationFrame);
            walkAnimation.addFrame(walk2Frame5, animationFrame);
            walkAnimation.addFrame(walk2Frame6, animationFrame);
            walkAnimation.addFrame(walk2Frame7, animationFrame);
            walkAnimation.addFrame(walk2Frame8, animationFrame);
            walkAnimation.addFrame(walk2Frame9, animationFrame);

            attackAnimation.addFrame(attack2Frame1, animationFrame);
            attackAnimation.addFrame(attack2Frame2, animationFrame);
            attackAnimation.addFrame(attack2Frame3, animationFrame);
            attackAnimation.addFrame(attack2Frame4, animationFrame);
            attackAnimation.addFrame(attack2Frame5, animationFrame);
            attackAnimation.addFrame(attack2Frame6, animationFrame);

            dieAnimation.addFrame(die2Frame1, animationFrame);
            dieAnimation.addFrame(die2Frame2, animationFrame);
            dieAnimation.addFrame(die2Frame3, animationFrame);
            dieAnimation.addFrame(die2Frame4, animationFrame);
            dieAnimation.addFrame(die2Frame5, animationFrame);
            dieAnimation.addFrame(die2Frame6, animationFrame);
            dieAnimation.addFrame(die2Frame7, animationFrame);
            dieAnimation.addFrame(die2Frame8, animationFrame);
            dieAnimation.addFrame(die2Frame9, animationFrame);
            dieAnimation.addFrame(die2Frame10, animationFrame);
        }
        else{ //70퍼센트 확률로 몬스터1
            maxHP = maxHP1;
            hp = hp1;
            damage = damage1;
            speed = speed1;
            coolTime = coolTime1;

            walkAnimation.addFrame(walk1Frame1, animationFrame);
            walkAnimation.addFrame(walk1Frame2, animationFrame);
            walkAnimation.addFrame(walk1Frame3, animationFrame);
            walkAnimation.addFrame(walk1Frame4, animationFrame);
            walkAnimation.addFrame(walk1Frame5, animationFrame);
            walkAnimation.addFrame(walk1Frame6, animationFrame);
            walkAnimation.addFrame(walk1Frame7, animationFrame);
            walkAnimation.addFrame(walk1Frame8, animationFrame);
            walkAnimation.addFrame(walk1Frame9, animationFrame);

            attackAnimation.addFrame(attack1Frame1, animationFrame);
            attackAnimation.addFrame(attack1Frame2, animationFrame);
            attackAnimation.addFrame(attack1Frame3, animationFrame);
            attackAnimation.addFrame(attack1Frame4, animationFrame);
            attackAnimation.addFrame(attack1Frame5, animationFrame);
            attackAnimation.addFrame(attack1Frame6, animationFrame);

            dieAnimation.addFrame(die1Frame1, animationFrame);
            dieAnimation.addFrame(die1Frame2, animationFrame);
            dieAnimation.addFrame(die1Frame3, animationFrame);
            dieAnimation.addFrame(die1Frame4, animationFrame);
            dieAnimation.addFrame(die1Frame5, animationFrame);
            dieAnimation.addFrame(die1Frame6, animationFrame);
            dieAnimation.addFrame(die1Frame7, animationFrame);
            dieAnimation.addFrame(die1Frame8, animationFrame);
            dieAnimation.addFrame(die1Frame9, animationFrame);
            dieAnimation.addFrame(die1Frame10, animationFrame);
        }

        walkAnimation.setOneShot(false);
        attackAnimation.setOneShot(true); // 반복하지않음
        dieAnimation.setOneShot(true);
        monster.setBackgroundDrawable(walkAnimation);
        walkAnimation.start();

    }

    public Handler monsterHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) { //UI를 건들이는 것이라 따로 핸들링
            //대부분 캐릭터와 같다.
            //죽었을 때
            if (hp <= 0) {
               died(); // 기능정지
                die = new Runnable() { //die애니메이션
                    @Override
                    public void run() {
                        if(!dieAnimation.isRunning()) {
                            monster.setBackgroundDrawable(dieAnimation);
                            dieAnimation.start();
                        }
                        if(dieCount > 5) monster.setX(monster.getX() + PxToDp(350));
                        dieCount--;
                        if (dieCount <= 0) {
                            gameManager.getMonsterIndex().remove(me); //몬스터 리스트에서 제거
                            if(!dieServiceFuture.isCancelled()) dieServiceFuture.cancel(false);
                        }
                    }
                };
                if (dieService == null) {
                    dieService = Executors.newSingleThreadScheduledExecutor();
                    dieServiceFuture = dieService.scheduleAtFixedRate(die, 0, animationFrame, TimeUnit.MILLISECONDS);
                }

            }
            else { //죽지만 않으면 실행될 것들 = 죽으면 전부 안하게 됨
                //팔라독과 부딪힐 때

                if (paladogCollision((int) monster.getX(), (int) paladog.getX(), monster.getWidth(), paladog.getWidth())) {
                    isPaladogCollision = true;
                } else {
                    isPaladogCollision = false;
                }

                //캐릭터1와 부딪힐 때
                if (gameManager.getCharacter1Index().size() > 0) {
                    if (character1 == null) {//null일 때만 검사
                     for (int i = 0; i < gameManager.getCharacter1Index().size(); i++) { //gameManager와 연결해서 그쪽에 있는 캐릭터들을 하나씩 가져와서 검사하는 수 밖에 없다.
                         if (characterCollision((int) monster.getX(), (int) gameManager.getCharacter1Index().get(i).getCharacter1().getX(), monster.getWidth(), gameManager.getCharacter1Index().get(i).getCharacter1().getWidth())) {
                                character1 = gameManager.getCharacter1Index().get(i); //공격할 놈을 가져온다.
                                isCharacter1Collision = true;
                            }
                        }
                    }
                }
                //캐릭터2와 부딪힐 때
                if (gameManager.getCharacter2Index().size() > 0) {
                    if (character2 == null) {
                         for (int i = 0; i < gameManager.getCharacter2Index().size(); i++) { //gameManager와 연결해서 그쪽에 있는 캐릭터들을 하나씩 가져와서 검사하는 수 밖에 없다.
                            if (characterCollision((int) monster.getX(), (int) gameManager.getCharacter2Index().get(i).getCharacter2().getX(), monster.getWidth(), gameManager.getCharacter2Index().get(i).getCharacter2().getWidth())) {
                                character2 = gameManager.getCharacter2Index().get(i); //공격할 놈을 가져온다.
                                isCharacter2Collision = true;
                            }
                        }
                    }
                }

                if (isCharacter1Collision  && gameManager.getEnemybase().getHp() > 0 && gameManager.getMyCharacter().getHp() > 0) { // 충돌 상태시에(isCharacterCollision으로 합쳐도 될 것같은데, 당시에 어떤 이유로 오류났었음 고로 분리)
                    if (walkAnimation.isRunning()) { //애니메이션 변경코드
                        walkAnimation.stop();
                    }
                    if (!attackAnimation.isRunning()) {
                        monster.setBackgroundDrawable(attackAnimation);
                        attack = new Runnable() { //attack 이벤트 할 곳
                            @Override
                            public void run() {
                                if (attackAnimation.isRunning())
                                    attackAnimation.stop(); // 이미 실행하고 있는 애니메이션 종료하고 시작
                                attackAnimation.start();

                                mediaAttack1.start();

                                if (character1.getHp() <= 0) { //캐릭터가 죽으면
                                    character1.setHp(0);
                                    isCharacter1Collision = false; //충돌 끄고
                                    changeAttackService = true;
                                    character1 = null; //캐릭터 리스트서 삭제후 null로
                                }
                                else{
                                    character1.attacked(damage);
                                }

                            }
                        };
                        if (attackService == null) {
                            attackService = Executors.newSingleThreadScheduledExecutor();
                            attackServiceFuture = attackService.scheduleAtFixedRate(attack, 0, (long) coolTime * 1000, TimeUnit.MILLISECONDS); //캐릭터의 쿨타임마다 공격하도록
                        }
                        if (changeAttackService) {//attackservice보다 늦게 실행되어야 해서 추가
                            attackService = null;
                            changeAttackService = false;
                        }
                    }
                }
                else if (isCharacter2Collision  && gameManager.getEnemybase().getHp() > 0 && gameManager.getMyCharacter().getHp() > 0) { // 충돌 상태시에
                    if (walkAnimation.isRunning()) { //애니메이션 변경코드
                        walkAnimation.stop();
                    }
                    if (!attackAnimation.isRunning()) {
                        monster.setBackgroundDrawable(attackAnimation);
                        attack = new Runnable() { //attack 이벤트 할 곳
                            @Override
                            public void run() {
                                if (attackAnimation.isRunning())
                                    attackAnimation.stop(); // 이미 실행하고 있는 애니메이션 종료하고 시작
                                attackAnimation.start();

                                mediaAttack1.start();

                                if (character2.getHp() <= 0) {
                                    character2.setHp(0);
                                    isCharacter2Collision = false;
                                    changeAttackService = true;
                                    character2 = null;
                                }
                                else{
                                    character2.attacked(damage);
                                }

                            }
                        };
                        if (attackService == null) {
                            attackService = Executors.newSingleThreadScheduledExecutor();
                            attackServiceFuture = attackService.scheduleAtFixedRate(attack, 0, (long) coolTime * 1000, TimeUnit.MILLISECONDS); //캐릭터의 쿨타임마다 공격하도록
                        }
                        if (changeAttackService) {//attackservice보다 늦게 실행되어야 해서 추가
                            attackService = null;
                            changeAttackService = false;
                        }
                    }
                }
                else if (isPaladogCollision  && gameManager.getEnemybase().getHp() > 0 && gameManager.getMyCharacter().getHp() > 0) { // 충돌 상태시에
                    if (walkAnimation.isRunning()) { //애니메이션 변경코드
                        walkAnimation.stop();
                    }
                    if (!attackAnimation.isRunning()) {
                        monster.setBackgroundDrawable(attackAnimation);
                        attack = new Runnable() { //attack 이벤트 할 곳
                            @Override
                            public void run() {
                                if (attackAnimation.isRunning())
                                    attackAnimation.stop(); // 이미 실행하고 있는 애니메이션 종료하고 시작
                                attackAnimation.start();
                                if (isPaladogCollision) {
                                    mediaAttack1.start();
                                    gameManager.getMyCharacter().attacked(damage);
                                    if (gameManager.getMyCharacter().getHp() <= 0) {
                                        isPaladogCollision = false;
                                    }
                                    else{ //맵 게이지에 있는 팔라독 체력바 줄이기
                                        gameManager.getMapGaze1().getLayoutParams().width = (int)(gameManager.getMapGaze1Width()*gameManager.getMyCharacter().getHp()/gameManager.getMyCharacter().getMaxHP());
                                    }
                                }
                            }
                        };
                        //팔라독의 경우 단독개체이기 때문에 이게 나은 것 같다.
                        attackService2 = Executors.newSingleThreadScheduledExecutor();
                        attackServiceFuture2 = attackService2.scheduleAtFixedRate(attack, 0, (long) coolTime * 1000, TimeUnit.MILLISECONDS); //캐릭터의 쿨타임마다 공격하도록
                    }
                }
                else {//충돌이 아닐 시에 애니메이션 변경
                    if (attackAnimation.isRunning()) {
                        if (attackService != null) attackServiceFuture.cancel(false);
                        if (attackService2 != null) attackServiceFuture2.cancel(false);
                        attackAnimation.stop();
                    }
                    if (!walkAnimation.isRunning()) {
                        monster.setBackgroundDrawable(walkAnimation);
                        walkAnimation.start();//걸어
                    }
                    monster.setX(monster.getX() - speed); //정상적으로 이동
                }
            }
            //충돌되든 말든 몬스터는 맵스크롤에 영향을 받도록 따로 빼둔다.
            if (paladog.getX() <PxToDp(-700)) { //-200은 현재 화면왼쪽끝
                if (gameBackground1.getX() < PxToDp(-700)) {
                    if (leftTouchCheck.getText().toString() == "true") { //몬스터도 맵스크롤 처럼 이동되도록
                        monster.setX(monster.getX() + gameManager.getMyCharacter().getSpeed()+speed);
                    }
                }
            }
            if (paladog.getX() > PxToDp(6300)) { //화면 오른쪽끝
                if (gameBackground1.getX() > PxToDp(-10500)) {//배경끝
                    if (rightTouchCheck.getText().toString() == "true") {
                        monster.setX(monster.getX() - gameManager.getMyCharacter().getSpeed()-speed);
                    }
                }
            }
        }
    };

    //2 객체의 x좌표와, 가로 길이를 받는다. 수평게임이기 때문에 이것이면 충분할것
    private boolean paladogCollision(int x1, int x2, int width1, int width2){
        if(x1 > x2){
            if((x1 - x2) < width1/2 + width2/2 - PxToDp(2223)) { //이미지의 크기 구성상 이렇게 계산
                return true;
            }
        }
        else if (x1 < x2){
            if((x2 - x1)-PxToDp(473) < width1/2 + width2/2 - PxToDp(2223)) { //두 거리보다 가까워지면 충돌
                return true;
            }
        }
        return false;
    }

    private boolean characterCollision(int x1, int x2, int width1, int width2){
        if(x1 > x2){
            if((x1 - x2) < width1/2 + width2/2 - PxToDp(2800)) { //이미지의 크기 구성상 이렇게 계산
                return true;
            }
        }
        else if (x1 < x2){
            if((x2 - x1)-PxToDp(473) < width1/2 + width2/2 - PxToDp(2800)) { //두 거리보다 가까워지면 충돌
                return true;
            }
        }
        return false;
    }

    public void attacked(float damage){
        hp -= damage;
        mediaHitted.start();
    }

    public void died(){ //죽었을 때, 기능정지
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

    }
    //px를 dp로 바꾸는거(여러 기기에 호환되도록)
    public static float PxToDp(float px){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return Math.round(dp);
    }
    public float getHp() { return hp; }

    public ImageView getMonster() { return monster; }

    public void setHp(float hp) { this.hp = hp; }

    public void setGameManager(GameManager gameManager) { this.gameManager = gameManager; }

    public void setMediaDie1(MediaPlayer mediaDie1) { this.mediaDie1 = mediaDie1; }
}
