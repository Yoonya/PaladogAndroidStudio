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

public class Character1 extends Activity {//캐릭터1 클래스
    //context
    private Context characterContext;
    //스탯
    private float maxHP = 100; //최대체력
    private float hp = 100; //체력
    private float damage = 10; //공격력
    private float skillDamage = 30; //스킬공격력
    private float skillPercent = 30; // 스킬 쓸 확률
    private float skillRanNum; //설정된 확률
    private float speed = PxToDp(35); //속도
    private float coolTime = 3;//공격 쿨 타임
    //트리거 및 이벤트
    private Runnable attack; //공격 이벤트관리
    private Runnable die; //죽음 이벤트 관리
    private ScheduledExecutorService attackService; //타이머보다 좋다고 해서 씀, 각각 service와 future가 한 세트
    private ScheduledFuture attackServiceFuture; //위와 이건 몬스터를 쿨타임마다 공격하기 위한 관리
    private ScheduledExecutorService attackService2;//이건 적 진지 공격 관리
    private ScheduledFuture attackServiceFuture2;
    private ScheduledExecutorService dieService;//이건 죽을 때 관리
    private ScheduledFuture dieServiceFuture;
    //내 캐릭터
    private ImageView character1;
    //애니메이션
    private AnimationDrawable walkAnimation;
    private AnimationDrawable attackAnimation;
    private AnimationDrawable dieAnimation;
    private AnimationDrawable skillAnimation;
    private int animationFrame = 1000/10;
    private int dieCount = 10;// 캐릭터 죽을 때 쓸 변수
    //사용할 클래스 및 UI
    private GameManager gameManager;
    private Monster monster;
    private ImageView gameBackground1;
    private ImageView paladog;
    private ImageView enemybase;
    private TextView leftTouchCheck; //맵 스크롤 때, 캐릭터의 위치를 제어하기 위해 추가
    private TextView rightTouchCheck; //이거 boolean값으로 하면되는데, 당시에 왜인지 boolean이 안되서 텍스트뷰에 id추가해서 사용했다.
    private CharacterAura characterAura; //캐릭터에게 아우라 이펙트 위에 있을 때, 생기는 개인 이펙트
    private Character1 me;
    //효과음
    private MediaPlayer mediaHitted;
    private MediaPlayer mediaDie1;
    private MediaPlayer mediaSkill1;
    //레이아웃선언
    private ConstraintLayout mainLinear;
    //변수
    private boolean isCharacterCollision = false; //몬스터 충돌감지
    private boolean isEnemybaseCollision = false; //적 진지 충돌감지
    private boolean changeAttackService = false;//어택서비스스케줄을 종료할 때, 사용한다.
    private boolean isOnAura = false;//이펙트 받은 상태냐 안받은 상태냐

    public Character1(Context context){
        this.characterContext = context;
        //변수들
        mainLinear = ((ConstraintLayout) ((Activity) characterContext).findViewById(R.id.activity_main));

        gameBackground1 = ((Activity) characterContext).findViewById(R.id.background1);
        paladog = ((Activity) characterContext).findViewById((int)3);
        enemybase = ((Activity) characterContext).findViewById((int)4);
        leftTouchCheck = ((Activity) characterContext).findViewById((int)1); //당시에 어떤 이유로 boolean이 안되었다. 순서 문제인가?
        rightTouchCheck = ((Activity) characterContext).findViewById((int)2);
        me = this;
        //캐릭터의 ImageView생성
        character1 = new ImageView(characterContext);

        mediaHitted = MediaPlayer.create(characterContext, R.raw.hit);
        mediaDie1 = MediaPlayer.create(characterContext, R.raw.character1_die);
        mediaSkill1 = MediaPlayer.create(characterContext, R.raw.charcter1_skill);

        //캐릭터 설정
        character1.setX(PxToDp(-1750)+gameBackground1.getX()); //최초 생성위치
        character1.setY(PxToDp(700));
        character1.setVisibility(View.VISIBLE);

        mainLinear.addView(character1, 10); //설정한 것을 mainlayout에 추가, 뒤에 인덱스는 최소 배경의 앞이다.

        characterAura = new CharacterAura(characterContext); //캐릭터 아우라 설정
        characterAura.setCharacter1(this);
        characterAura.setGameManager(gameManager);
        characterAura.getThread().start();

        //캐릭터 애니메이션
        walkAnimation = new AnimationDrawable();
        attackAnimation = new AnimationDrawable();
        dieAnimation = new AnimationDrawable();
        skillAnimation = new AnimationDrawable();

        BitmapDrawable walkFrame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_walk1);
        BitmapDrawable walkFrame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_walk2);
        BitmapDrawable walkFrame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_walk3);
        BitmapDrawable walkFrame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_walk4);
        BitmapDrawable walkFrame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_walk5);

        BitmapDrawable attackFrame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_attack1);
        BitmapDrawable attackFrame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_attack2);
        BitmapDrawable attackFrame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_attack3);
        BitmapDrawable attackFrame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_attack4);
        BitmapDrawable attackFrame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_attack5);
        BitmapDrawable attackFrame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_attack6);

        BitmapDrawable dieFrame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_die1);
        BitmapDrawable dieFrame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_die2);
        BitmapDrawable dieFrame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_die3);
        BitmapDrawable dieFrame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_die4);
        BitmapDrawable dieFrame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_die5);
        BitmapDrawable dieFrame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_die6);
        BitmapDrawable dieFrame7 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_die7);
        BitmapDrawable dieFrame8 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_die8);
        BitmapDrawable dieFrame9 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_die9);
        BitmapDrawable dieFrame10 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_die10);

        BitmapDrawable skillFrame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_skill1);
        BitmapDrawable skillFrame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_skill2);
        BitmapDrawable skillFrame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_skill3);
        BitmapDrawable skillFrame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_skill4);
        BitmapDrawable skillFrame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_skill5);
        BitmapDrawable skillFrame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_skill6);
        BitmapDrawable skillFrame7 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_skill7);
        BitmapDrawable skillFrame8 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.character1_skill8);

        walkAnimation.addFrame(walkFrame1, animationFrame);
        walkAnimation.addFrame(walkFrame2, animationFrame);
        walkAnimation.addFrame(walkFrame3, animationFrame);
        walkAnimation.addFrame(walkFrame4, animationFrame);
        walkAnimation.addFrame(walkFrame5, animationFrame);

        attackAnimation.addFrame(attackFrame1, animationFrame);
        attackAnimation.addFrame(attackFrame2, animationFrame);
        attackAnimation.addFrame(attackFrame3, animationFrame);
        attackAnimation.addFrame(attackFrame4, animationFrame);
        attackAnimation.addFrame(attackFrame5, animationFrame);
        attackAnimation.addFrame(attackFrame6, animationFrame);

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
        skillAnimation.addFrame(skillFrame8, animationFrame);

        walkAnimation.setOneShot(false);
        attackAnimation.setOneShot(true); // 반복하지않음
        dieAnimation.setOneShot(true);
        skillAnimation.setOneShot(true);
        character1.setBackgroundDrawable(walkAnimation);
        walkAnimation.start();

    }

    public Handler characterTransitionHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) { //UI를 건들이는 것이라 따로 핸들링
            if (hp <= 0) {//즉 죽었을 때
                died(); //죽음 메소드
                //죽었을 때를 관리하는 타이머이다. 지금보면 있든없든 상관없어 보이는데, 잘 안되서 추가했던 기억이 있다. 일단 냅두자
                die = new Runnable() { //die애니메이션
                    @Override
                    public void run() {
                        if (!dieAnimation.isRunning()) {
                            character1.setBackgroundDrawable(dieAnimation);
                            dieAnimation.start(); //죽음 애니메이션 시작
                        }
                        if (dieCount > 5) character1.setX(character1.getX() - PxToDp(350)); //죽으면서 뒤로 날아가도록 했다.*연출*
                        dieCount--;
                        if (dieCount <= -5) {
                            gameManager.getCharacter1Index().remove(me);
                            if(!dieServiceFuture.isCancelled()) dieServiceFuture.cancel(false);
                        }
                    }
                };
                if (dieService == null) {
                    dieService = Executors.newSingleThreadScheduledExecutor();
                    dieServiceFuture = dieService.scheduleAtFixedRate(die, 0, animationFrame, TimeUnit.MILLISECONDS);
                }
            }
            else {//살아있을 때, 행동
                if (enemybaseCollision((int) character1.getX(), (int) enemybase.getX(), character1.getWidth(), enemybase.getWidth())) { //적 진지 충돌감지
                    isEnemybaseCollision = true;
                }
                else {
                    isEnemybaseCollision = false;
                }
                if (gameManager.getMonsterIndex().size() > 0) { //몬스터 충돌감지
                    for (int i = 0; i < gameManager.getMonsterIndex().size(); i++) {
                        if (monster == null) {//설정된 몬스터가 없을 때만 충돌감지 하도록
                        if (characterCollision((int) character1.getX(), (int) gameManager.getMonsterIndex().get(i).getMonster().getX(), character1.getWidth(), gameManager.getMonsterIndex().get(i).getMonster().getWidth())) {
                                monster = gameManager.getMonsterIndex().get(i);//겹쳐있더라도 때리던 놈 때리도록 하는 효과도 있음
                                isCharacterCollision = true;
                            }
                        }
                    }
                }
                if (isCharacterCollision && gameManager.getEnemybase().getHp() > 0 && gameManager.getMyCharacter().getHp() > 0) {//몬스터와 격돌시 그리고 게임이 끝나지 않을 때
                    if (walkAnimation.isRunning()) { //애니메이션 변경코드
                        walkAnimation.stop();
                    }
                    if (!attackAnimation.isRunning() && !skillAnimation.isRunning()) { //공격중이 아니라면
                        if(isOnAura){ //순서상 아래있는 animation.stop보다 먼저 해야하는 것을 확인 조금 어지럽지만 이렇게 한다.
                            //+여기에 하지 않으면 죽은 후에 좀비가 되던데?
                            Random ran = new Random();
                            skillRanNum = ran.nextFloat()*100 + 1; //스킬 발생확률
                            if(skillRanNum <= skillPercent){
                                character1.setBackgroundDrawable(skillAnimation);
                            }
                            else{
                                character1.setBackgroundDrawable(attackAnimation);
                            }
                        }
                        else {
                            character1.setBackgroundDrawable(attackAnimation);
                        }
                        attack = new Runnable() { //attack 이벤트 할 곳

                            @Override
                            public void run() { //attack 관리
                                if (hp > 0) {//혹시 모를 방지턱
                                    if (attackAnimation.isRunning())
                                        attackAnimation.stop(); // 이미 실행하고 있는 애니메이션 종료하고 시작
                                    if (skillAnimation.isRunning())
                                        skillAnimation.stop(); // 이미 실행하고 있는 애니메이션 종료하고 시작
                                    if (isOnAura) {
                                        Random ran = new Random(); //한번 아우라에 들어간 랜덤값은 바뀌지 않더라, 랜덤값을 바꾸려면 아우라를 줬다 안줬다해야함
                                        skillRanNum = ran.nextFloat() * 100 + 1; //스킬 발생확률
                                        if (skillRanNum <= skillPercent) {
                                            character1.setBackgroundDrawable(skillAnimation);
                                            skillAnimation.start();//스킬공격
                                        } else {
                                            character1.setBackgroundDrawable(attackAnimation);
                                            attackAnimation.start(); //공격
                                        }
                                    } else {
                                        character1.setBackgroundDrawable(attackAnimation);
                                        attackAnimation.start(); //공격
                                    }

                                    if (monster.getHp() <= 0) { // 0이하가 되면
                                        monster.setHp(0); //0으로 설정
                                        isCharacterCollision = false; //죽였으니 충돌 끄기
                                        changeAttackService = true;//attackservice보다 늦게 실행되어야 해서 추가(당시에 오류가 걸렸었다.)
                                        monster = null; //몬스터 삭제
                                    } else {
                                        if (attackAnimation.isRunning())
                                            monster.attacked(damage); //데미지 줌
                                        if (skillAnimation.isRunning()) {
                                            mediaSkill1.start();
                                            monster.attacked(skillDamage);
                                        } //효과음이랑 스킬뎀
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
                else if(isEnemybaseCollision && gameManager.getEnemybase().getHp() > 0 && gameManager.getMyCharacter().getHp() > 0){ // 적 진지와 격돌시
                    if (walkAnimation.isRunning()) { //위와 같다
                        walkAnimation.stop();
                    }
                    if (!attackAnimation.isRunning() && !skillAnimation.isRunning()) { //공격중이 아니라면
                        if(isOnAura){ //순서상 아래있는 animation.stop보다 먼저 해야하는 것을 확인 조금 어지럽지만 이렇게 한다.
                            //+여기에 하지 않으면 죽은 후에 좀비가 되던데?
                            Random ran = new Random();
                            skillRanNum = ran.nextFloat()*100 + 1; //스킬 발생확률
                            if(skillRanNum <= skillPercent){
                                character1.setBackgroundDrawable(skillAnimation);
                            }
                            else{
                                character1.setBackgroundDrawable(attackAnimation);
                            }
                        }
                        else {
                            character1.setBackgroundDrawable(attackAnimation);
                        }
                        attack = new Runnable() { //attack 이벤트 할 곳
                            @Override
                            public void run() {
                                if (attackAnimation.isRunning())
                                    attackAnimation.stop(); // 이미 실행하고 있는 애니메이션 종료하고 시작
                                if (skillAnimation.isRunning())
                                    skillAnimation.stop(); // 이미 실행하고 있는 애니메이션 종료하고 시작

                                if (isOnAura) {
                                    if (hp > 0) {
                                        Random ran = new Random();
                                        skillRanNum = ran.nextFloat() * 100 + 1; //스킬 발생확률
                                        if (skillRanNum <= skillPercent) {
                                            character1.setBackgroundDrawable(skillAnimation);
                                            skillAnimation.start();
                                        } else {
                                            character1.setBackgroundDrawable(attackAnimation);
                                            attackAnimation.start(); //공격
                                        }
                                    }
                                }else {
                                        character1.setBackgroundDrawable(attackAnimation);
                                        attackAnimation.start(); //공격
                                    }

                                    if (attackAnimation.isRunning())
                                        gameManager.getEnemybase().attacked(damage); //데미지 줌
                                    if (skillAnimation.isRunning()) {
                                        mediaSkill1.start();
                                        gameManager.getEnemybase().attacked(skillDamage);
                                    }

                                    if (gameManager.getEnemybase().getHp() < gameManager.getEnemybase().getMaxHP() * 2 / 5) {//적 진지가 hp 40퍼센트 이하
                                        if (!gameManager.isPhase2())
                                            gameManager.phase2(); //게임 페이즈2 시작
                                    }

                                    if (gameManager.getEnemybase().getHp() <= 0) { //적 진지가 0이하가 되면
                                        isEnemybaseCollision = false; //충돌 풀고
                                    } else {//맵 게이지에 있는 적진지 체력바 줄이기
                                        gameManager.getMapGaze2().getLayoutParams().width = (int) (gameManager.getMapGaze2Width() * gameManager.getEnemybase().getHp() / gameManager.getEnemybase().getMaxHP());
                                    }

                                }

                        };
                        attackService2 = Executors.newSingleThreadScheduledExecutor(); //적 진지 공격, 이건 딱히 null설정 안해도 됨
                        attackServiceFuture2 = attackService2.scheduleAtFixedRate(attack, 0, (long) coolTime * 1000, TimeUnit.MILLISECONDS); //캐릭터의 쿨타임마다 공격하도록

                    }
                }
                else {//충돌 상태가 아니라면, 즉 공격 중이 아니라면
                    if (attackAnimation.isRunning() || skillAnimation.isRunning()) { //공격 관련 모든거 중단
                        if(attackService != null){
                            attackServiceFuture.cancel(false);
                        }
                        if(attackService2 != null){
                            attackServiceFuture2.cancel(false);
                        }
                        if(attackAnimation.isRunning()) attackAnimation.stop();
                        if(skillAnimation.isRunning()) skillAnimation.stop();
                    }
                    if (!walkAnimation.isRunning()) {//걷기 애니메이션 시작
                        character1.setBackgroundDrawable(walkAnimation);
                        walkAnimation.start();
                    }
                    character1.setX(character1.getX() + speed); //움직여
                }

            }//죽었을 때, 살았을 때 끝. 이 아래는 죽든 말든 해야 할 일
            if (paladog.getX() < PxToDp(-700)) { //-200은 현재 화면왼쪽끝
                if (gameBackground1.getX() < PxToDp(-700)) {
                    if (leftTouchCheck.getText().toString() == "true") { //몬스터도 캐릭터의 움직임에 따라 맵스크롤 처럼 이동되도록
                        character1.setX(character1.getX() + gameManager.getMyCharacter().getSpeed()+speed);
                    }
                }
            }
            if (paladog.getX() > PxToDp(6300)) { //화면 오른쪽끝
                if (gameBackground1.getX() > PxToDp(-10500)) {//배경끝
                    if (rightTouchCheck.getText().toString() == "true") {//이거 없으면 잘 기억이 안나는데 어쨌든 끔찍함
                        character1.setX(character1.getX() - gameManager.getMyCharacter().getSpeed()-speed);
                    }
                }
            }
        }
    };

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

    private boolean enemybaseCollision(int x1, int x2, int width1, int width2){
        if(x1 > x2){
            if((x1 - x2) < width1/2 + width2/2- PxToDp(3150)) { //이미지의 크기 구성상 이렇게 계산
                return true;
            }
        }
        else if (x1 < x2){
            if((x2 - x1)-PxToDp(473) < width1/2 + width2/2-PxToDp(3150)) { //두 거리보다 가까워지면 충돌
                return true;
            }
        }
        return false;
    }

    public void attacked(float damage){ //공격 받을 때
        hp -= damage;
        mediaHitted.start();
    }

    public void died(){ //죽을 때, 모든 동작 정지 리스트에서 없애 주는건 공격한 적이 해줄 것
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
    //px를 dp로 바꾸는거(여러 기기에 호환되도록)
    public static float PxToDp(float px){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return Math.round(dp);
    }
    public float getHp() {
        return hp;
    }

    public void setHp(float hp) {
        this.hp = hp;
    }

    public ImageView getCharacter1() {
        return character1;
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public ConstraintLayout getMainLinear() {
        return mainLinear;
    }

    public void setMainLinear(ConstraintLayout mainLinear) {
        this.mainLinear = mainLinear;
    }

    public void setOnAura(boolean onAura) {
        isOnAura = onAura;
    }

    public void setMediaDie1(MediaPlayer mediaDie1) { this.mediaDie1 = mediaDie1; }

}
