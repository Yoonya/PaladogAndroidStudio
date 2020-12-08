package com.example.paladogproject;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class GameManager extends Activity { //게임창의 전반적인 것을 담당하는 클래스
    private Context gameManagerContext;
    private ConstraintLayout mainLinear;
    //게임창
    private ImageView gameBackground1; //배경
    private ImageView gameBackground2; //배경의 입체감을 위해 3개 준비
    private ImageView gameBackground3;
    private ImageView gameUI1; //게임 ui 1, 2번
    private ImageView gameUI2;
    private ImageView gazebackground1; //각각 체력바, 마나바, 자원바
    private ImageView gazebackground2;
    private ImageView gazebackground3;
    private ImageView resourceGaze; //차오르는 자원
    private ImageView manaGaze; //차오르는 마나
    private ImageView mapGaze1; //내 캐릭터 체력이기도하고 맵이기도 함
    private ImageView mapGaze2; //적 체력이기도 하고 맵이기도 함
    private ImageView location; //맵에서 내 캐릭터 위치
    private ImageView pauseUI; //메뉴창
    private ImageView result; //결과창
    private ImageView resultNew; //최고기록 결과창
    private TextView maxResource; //최대자원
    private TextView resource; //현재자원
    private TextView maxMana; ///최대마나
    private TextView mana; //현재마나
    private TextView timer;// 플레이타임
    private TextView resultMinute; //결과시간 그 순간 분, 초 저장
    private TextView resultSecond;
    //버튼
    private ImageButton character1Btn; //캐릭터1생성버튼
    private ImageButton character2Btn; //캐릭터2생성버튼
    private ImageButton skillBtn; //스킬사용버튼
    private ImageButton rightBtn; //우클릭이동버튼
    private ImageButton leftBtn; //좌클릭 이동버튼
    private ImageButton pauseBtn; //메뉴창호출버튼
    private ImageButton giveupBtn; //포기버튼
    private ImageButton resumeBtn; //메뉴창에서 재개버튼

    //효과음
    private MediaPlayer btnClick;
    private MediaPlayer paladogWalk;
    private MediaPlayer paladogAttack;
    private MediaPlayer characterCreate;
    private MediaPlayer phase2Media;

    //클래스
    private MainActivity mainActivity;
    private GameManager gameManager;
    private MyCharacter myCharacter;
    private Skill skill1;
    private EnemyBase enemybase;
    private Character1 character1;
    private Character2 character2;
    private Monster monster;
    private ArrayList<Monster> monsterIndex = new ArrayList<>();
    private ArrayList<Character1> character1Index = new ArrayList<>();
    private ArrayList<Character2> character2Index = new ArrayList<>();
    private ArrayList<Skill> skillIndex = new ArrayList<>();
    private Random random = new Random();

    //몬스터 생성 서비스
    private ScheduledExecutorService createMonsterservice; //몬스터 생성
    private ScheduledFuture createMonsterFuture;
    private ScheduledExecutorService changeDelayService; //몬스터 생성시간을 주기적으로 바꿈->랜덤생성
    private ScheduledFuture changeDelayServiceFuture;
    private ScheduledExecutorService createMonsterPhase2service;//페이즈2 서비스
    private ScheduledFuture createMonsterPhase2Future;
    private Runnable createMonster;
    //캐릭터가 버튼을 클릭하는지 확인
    private TextView isOnTouchLeft;
    private TextView isOnTouchRight;
    //변수이름 그대로
    private boolean paladogWalkSound = false;
    //마나와 자원 관리 서비스
    private ScheduledExecutorService regenService;
    private ScheduledFuture regenServiceFuture;
    private Runnable regen;
    //플레이타임 관리 서비스
    private ScheduledExecutorService timerCountService;
    private ScheduledFuture timerCountServiceFuture;
    private Runnable timerCount;
    //변수
    private float manaCount=0; //최초 마나, 자원
    private float resourceCount=0;
    private float maxManaCount = 50; //최대 얻을 수 있는 마나, 자원
    private float maxResourceCount = 50;
    private float manaGazeWidth; //width는 게이지가 차오르는 것처럼 보이기 위해 쓸 것
    private float resourceGazeWidth;
    private float initManaGazeWidth; //리셋하기 위한 초기값
    private float initResourceGazeWidth;
    private float mapGaze1Width;
    private float mapGaze2Width;
    private float initmapGaze1Width; //리셋하기 위한 초기값
    private float initmapGaze2Width;
    private float character1Cost = 10; //캐릭터 생성 비용들
    private float character2Cost = 20;
    private float skill1Cost = 10; //스킬 사용 비용
    private int timerSecond = 0; //최초 플레이타임
    private int timerMinute = 0;
    private boolean isPhase2 = false; //현재 페이즈1
    private boolean gameWin = false;
    private boolean gameLose = false;
    //스레드
    private int animationFrame = 1000/10;
    private Thread thread;

    public GameManager(Context context){
        this.gameManagerContext = context;
        mainLinear = ((ConstraintLayout) ((Activity) gameManagerContext).findViewById(R.id.activity_main));
        gameManager = this;
        //변수들에 값 설정
        gameBackground1 = ((Activity) gameManagerContext).findViewById(R.id.background1);
        gameBackground2 = ((Activity) gameManagerContext).findViewById(R.id.background2);
        gameBackground3 = ((Activity) gameManagerContext).findViewById(R.id.background3);
        gameUI1 = ((Activity) gameManagerContext).findViewById(R.id.gameui1);
        gameUI2 = ((Activity) gameManagerContext).findViewById(R.id.gameui2);
        gazebackground1 = ((Activity) gameManagerContext).findViewById(R.id.gazebackground1);
        gazebackground2 = ((Activity) gameManagerContext).findViewById(R.id.gazebackground2);
        gazebackground3 = ((Activity) gameManagerContext).findViewById(R.id.gazebackground3);
        pauseUI = ((Activity) gameManagerContext).findViewById(R.id.pauseui);
        maxResource = ((Activity) gameManagerContext).findViewById(R.id.maxresource);
        maxMana = ((Activity) gameManagerContext).findViewById(R.id.maxmana);
        resource = ((Activity) gameManagerContext).findViewById(R.id.resource);
        mana = ((Activity) gameManagerContext).findViewById(R.id.mana);
        resourceGaze = ((Activity) gameManagerContext).findViewById(R.id.resourceGaze);
        timer =  ((Activity) gameManagerContext).findViewById(R.id.timer);
        result = ((Activity) gameManagerContext).findViewById(R.id.resultFrame);
        resultNew =  ((Activity) gameManagerContext).findViewById(R.id.resultNewFrame);
        resultMinute = ((Activity) gameManagerContext).findViewById(R.id.resultMinute);
        resultSecond = ((Activity) gameManagerContext).findViewById(R.id.resultSecond);
        manaGaze = ((Activity) gameManagerContext).findViewById(R.id.manaGaze);
        mapGaze1 = ((Activity) gameManagerContext).findViewById(R.id.mapGaze1);
        mapGaze2 = ((Activity) gameManagerContext).findViewById(R.id.mapGaze2);
        location = ((Activity) gameManagerContext).findViewById(R.id.location);

        character1Btn = ((Activity) gameManagerContext).findViewById(R.id.character1_btn);
        character2Btn = ((Activity) gameManagerContext).findViewById(R.id.character2_btn);
        skillBtn = ((Activity) gameManagerContext).findViewById(R.id.skill_btn);
        rightBtn = ((Activity) gameManagerContext).findViewById(R.id.rightbtn);
        leftBtn = ((Activity) gameManagerContext).findViewById(R.id.leftbtn);
        pauseBtn = ((Activity) gameManagerContext).findViewById(R.id.pausebtn);
        giveupBtn = ((Activity) gameManagerContext).findViewById(R.id.giveupebtn);
        resumeBtn = ((Activity) gameManagerContext).findViewById(R.id.resumebtn);

        btnClick = MediaPlayer.create(gameManagerContext, R.raw.charcter_create);
        paladogAttack = MediaPlayer.create(gameManagerContext, R.raw.paladog_attack);
        characterCreate = MediaPlayer.create(gameManagerContext, R.raw.character_active);

        phase2Media = MediaPlayer.create(gameManagerContext, R.raw.boss_angry);

        //마나, 자원 맵 게이지 색 설정
        manaGaze.setColorFilter(Color.parseColor("#ff00ffff"), PorterDuff.Mode.SRC_IN);
        resourceGaze.setColorFilter(Color.parseColor("#ffffcc00"), PorterDuff.Mode.SRC_IN);
        mapGaze1.setColorFilter(Color.parseColor("#ff00ffff"), PorterDuff.Mode.SRC_IN);
        mapGaze2.setColorFilter(Color.parseColor("#ffff0000"), PorterDuff.Mode.SRC_IN);

        //클래스 초기화
        myCharacter = new MyCharacter(gameManagerContext);
        myCharacter.getIdleAnimation().start();
        myCharacter.getPaladog().setId((int)3);//적 진지와 함께 가장 많이 쓰는 이 둘을 아이디 지정했다.
        myCharacter.setGameManager(gameManager);

        enemybase = new EnemyBase(gameManagerContext);
        enemybase.getEnemybase().setId((int)4);

        //터치 상태를 확인하기 추가, 왜인지 boolean값이 안통해서 이것으로 충당 - 비효율
        isOnTouchLeft = new TextView(gameManagerContext);
        isOnTouchRight = new TextView(gameManagerContext);
        isOnTouchLeft.setId((int)1);
        isOnTouchRight.setId((int)2);
        isOnTouchLeft.setText("false");
        isOnTouchRight.setText("false");
        isOnTouchLeft.setVisibility(View.GONE);
        isOnTouchRight.setVisibility(View.GONE);
        mainLinear.addView(isOnTouchLeft);
        mainLinear.addView(isOnTouchRight);

        //왼쪽버튼 설정, RepeatListener를 수정함
        leftBtn.setOnTouchListener(new View.OnTouchListener() {
            private Handler repeatHandler = new Handler(); // 자체 핸들러 설정
            private View downView; //View를 받을 변수
            //눌렀을 때, 실제 동작하는 곳. onTouch에서 눌렀을 때, 이것을 불러올 것이다.
            private View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    leftBtn.setImageResource(R.drawable.leftbutton_btn_pushed); //눌렸을 때, 버튼 눌린거로 바꾸기
                    if(myCharacter.getIdleAnimation().isRunning()) myCharacter.getIdleAnimation().stop(); // idleAnimation 멈추고
                    if(myCharacter.getAttackAnimation().isRunning()) myCharacter.getAttackAnimation().stop(); // idleAnimation 멈추고
                    myCharacter.getPaladog().setBackgroundDrawable(myCharacter.getLeftWalkAnimation()); // leftWalkAnimation으로 재설정
                    myCharacter.getLeftWalkAnimation().start(); // Animation 시작
                    if(myCharacter.getPaladog().getX()> PxToDp(-700)) { //-200은 현재 화면왼쪽끝
                        myCharacter.getPaladog().setX(myCharacter.getPaladog().getX() - myCharacter.getSpeed()); //캐릭터 이동
                        location.setX(location.getX() -  PxToDp(7)); // 나중에 고쳐야할것 pxtodp라던지
                    }
                    else{//오른쪽 끝으로 가면 배경이 이동, 이 상태에서 왼쪽 끝으로 오면 맵이 다시 뒤로
                        if(gameBackground1.getX()<PxToDp(-700)) { //화면 왼쪽 맨 끝에 캐릭터가 있고, 배경이 오른쪽 끝이라면 게임 배경을 이동
                            gameBackground1.setX(gameBackground1.getX() + myCharacter.getSpeed());
                            gameBackground2.setX(gameBackground2.getX() + myCharacter.getSpeed() / 2);
                            enemybase.getEnemybase().setX(enemybase.getEnemybase().getX() + myCharacter.getSpeed()); //적 진지도 함께 이동
                            location.setX(location.getX() - PxToDp(7));
                        }
                    }

                }
            };
            //RepeatListener를 구현한다.
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN://눌렀을 때
                        repeatHandler.removeCallbacks(repeatRunnable); //실행되고 있는 것 중지
                        repeatHandler.postDelayed(repeatRunnable, 50); //불러오기
                        downView = v;//이 뷰를 저장
                        clickListener.onClick(v);//clickListener와 이 뷰를 연결
                        if(!paladogWalkSound) { //효과음
                            paladogWalk = MediaPlayer.create(gameManagerContext, R.raw.paladog_walk);
                            paladogWalk.start();
                            paladogWalk.setLooping(true);
                            paladogWalkSound = true;
                        }
                        isOnTouchLeft.setText("true");
                        break;

                    case MotionEvent.ACTION_UP://뗏을 때
                        repeatHandler.removeCallbacks(repeatRunnable);
                        leftBtn.setImageResource(R.drawable.leftbutton_btn);//눌린버튼 제자리로
                        myCharacter.getLeftWalkAnimation().stop();//움직이는 Animation 정지하고 다시 idle상태로
                        myCharacter.getPaladog().setBackgroundDrawable(myCharacter.getIdleAnimation());
                        myCharacter.getIdleAnimation().start();
                        if(paladogWalkSound) {
                            paladogWalk.stop();
                            paladogWalk.release();
                            paladogWalkSound = false;
                        }
                        isOnTouchLeft.setText("false");
                        break;

                    case MotionEvent.ACTION_CANCEL:
                        repeatHandler.removeCallbacks(repeatRunnable);
                        break;
                }
                return false;
            }
            Runnable repeatRunnable = new Runnable() {
                @Override
                public void run() {//자기 자신을 계속 실행하면서 onClick계속 수행, 마치 재귀함수
                    repeatHandler.postDelayed(this, 50);
                    clickListener.onClick(downView);
                }
            };
        });

        //위와 같다.
        rightBtn.setOnTouchListener(new View.OnTouchListener() {
            private Handler repeatHandler = new Handler();
            private View downView;
            private View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(myCharacter.getIdleAnimation().isRunning()) myCharacter.getIdleAnimation().stop(); // idleAnimation 멈추고
                    if(myCharacter.getAttackAnimation().isRunning()) myCharacter.getAttackAnimation().stop(); // idleAnimation 멈추고
                    myCharacter.getPaladog().setBackgroundDrawable(myCharacter.getRightWalkAnimation());
                    rightBtn.setImageResource(R.drawable.rightbutton_btn_pushed);
                    myCharacter.getRightWalkAnimation().start();
                    if(myCharacter.getPaladog().getX()<PxToDp(6300)) { //화면 오른쪽끝
                        myCharacter.getPaladog().setX(myCharacter.getPaladog().getX() + myCharacter.getSpeed());
                        location.setX(location.getX() +  PxToDp(7));
                    }
                    else { //배경움직이기
                        if(gameBackground1.getX()>PxToDp(-10500)) {//배경끝
                            gameBackground1.setX(gameBackground1.getX() - myCharacter.getSpeed());
                            gameBackground2.setX(gameBackground2.getX() - myCharacter.getSpeed() / 2);
                            enemybase.getEnemybase().setX(enemybase.getEnemybase().getX() - myCharacter.getSpeed());
                            location.setX(location.getX() + PxToDp(7));
                        }
                    }

                }
            };
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        repeatHandler.removeCallbacks(repeatRunnable); //runnable 실행이 끝나기 전에 종료하고싶다.
                        repeatHandler.postDelayed(repeatRunnable, 50); //다시 보냄
                        downView = v;
                        clickListener.onClick(v);
                        if(!paladogWalkSound) {
                            paladogWalk = MediaPlayer.create(gameManagerContext, R.raw.paladog_walk);
                            paladogWalk.start();
                            paladogWalk.setLooping(true);
                            paladogWalkSound = true;
                        }
                        isOnTouchRight.setText("true");
                        break;

                    case MotionEvent.ACTION_UP:
                        repeatHandler.removeCallbacks(repeatRunnable);
                        rightBtn.setImageResource(R.drawable.rightbutton_btn);
                        myCharacter.getRightWalkAnimation().stop();
                        myCharacter.getPaladog().setBackgroundDrawable(myCharacter.getIdleAnimation());
                        myCharacter.getIdleAnimation().start();
                        if(paladogWalkSound) {
                            paladogWalk.stop();
                            paladogWalk.release();
                            paladogWalkSound = false;
                        }
                        isOnTouchRight.setText("false");
                        break;

                    case MotionEvent.ACTION_CANCEL:
                        repeatHandler.removeCallbacks(repeatRunnable);
                        break;
                }
                return false;
            }
            Runnable repeatRunnable = new Runnable() { //쓰레드 런
                @Override
                public void run() {
                    repeatHandler.postDelayed(this, 50); //반복 시간 딜레이에서 실행
                    clickListener.onClick(downView);
                }
            };
        });

        //캐릭터 및 스킬버튼
        character1Btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) { //캐릭터1버튼
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(resourceCount >= character1Cost) { //필요코스트보다 자원이 많을때
                        character1Btn.setImageResource(R.drawable.character1_btn_pushed);//버튼 클릭이미지*연출*
                        characterCreate.start();//효과음
                        character1 = new Character1(gameManagerContext); //캐릭터 생성
                        character1.setGameManager(gameManager);
                        character1Index.add(character1);
                        resourceCount -= character1Cost; //자원 절감
                    }
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(resourceCount>=character1Cost) character1Btn.setImageResource(R.drawable.character1_btn);
                    else character1Btn.setImageResource(R.drawable.character1_btn_cool); //만약 자원이 필요 캐릭터 비용보다 부족하면 흑백
                }
                return false;
            }
        });

        character2Btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {//위와 같음
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(resourceCount >= character2Cost) {
                        character2Btn.setImageResource(R.drawable.character2_btn_pushed);
                        characterCreate.start();
                        character2 = new Character2(gameManagerContext);
                        character2.setGameManager(gameManager);
                        character2Index.add(character2);
                        resourceCount -= character2Cost;
                    }
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(resourceCount>=character2Cost) character2Btn.setImageResource(R.drawable.character2_btn);
                    else character2Btn.setImageResource(R.drawable.character2_btn_cool);
                }
                return false;
            }
        });

        skillBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {//스킬사용버튼
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(manaCount >= skill1Cost) {
                        if(myCharacter.getIdleAnimation().isRunning()) myCharacter.getIdleAnimation().stop(); //동작 정지
                        if(myCharacter.getRightWalkAnimation().isRunning()) myCharacter.getRightWalkAnimation().stop();
                        if(myCharacter.getLeftWalkAnimation().isRunning()) myCharacter.getLeftWalkAnimation().stop();
                        if(myCharacter.getAttackAnimation().isRunning()) myCharacter.getAttackAnimation().stop();
                        myCharacter.getPaladog().setBackgroundDrawable(myCharacter.getAttackAnimation());
                        myCharacter.getAttackAnimation().start(); //공격 애니메이션 시작
                        skillBtn.setImageResource(R.drawable.skill_btn_pushed);
                        paladogAttack.start();//효과음
                        skill1 = new Skill(gameManagerContext); //스킬 생성
                        skillIndex.add(skill1);
                        skill1.setGameManager(gameManager);
                        manaCount -= skill1Cost;
                    }
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(manaCount>=skill1Cost) skillBtn.setImageResource(R.drawable.skill_btn);
                    else skillBtn.setImageResource(R.drawable.skill_btn_not); //마찬가지

                }
                return false;
            }
        });

       thread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(animationFrame);//아무래도 애니메이션프레임이 낮다보니 이것도 낮아져서 작업속도도 느린 모양
                        for(int i = 0; i< character1Index.size(); i++) { //캐릭터1관리
                            character1Index.get(i).characterTransitionHandler.sendMessage(character1Index.get(i).characterTransitionHandler.obtainMessage());
                        }
                        for(int i = 0; i< character2Index.size(); i++) {//캐릭터2관리
                            character2Index.get(i).characterTransitionHandler.sendMessage(character2Index.get(i).characterTransitionHandler.obtainMessage());
                        }
                        for (int i = 0; i < monsterIndex.size(); i++) {//몬스터관리
                            monsterIndex.get(i).monsterHandler.sendMessage(monsterIndex.get(i).monsterHandler.obtainMessage());
                        }
                        if (enemybase.getHp() <= 0) { //적 진지가 0이하가 되면
                            enemybase.setHp(0); //0으로 설정
                            if (!gameWin) { //중복 실행 방지
                                gameWin = true;
                                mainActivity.gameEndWin(); //게임 승리 선언
                            }
                        }
                        if (myCharacter.getHp() <= 0) {
                            myCharacter.setHp(0);
                            if(!gameLose){//중복 실행방지
                                gameLose = true;
                                mainActivity.gameEndLose(); //게임 패배 실행
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        });

        thread.start();

    }

    public  void start(){ //게임 시작 메소드
        //승 패 초기화
        gameLose = false;
        gameWin = false;
        //전부 보이게해라
        gameBackground1.setVisibility(View.VISIBLE);
        gameBackground2.setVisibility(View.VISIBLE);
        gameBackground3.setVisibility(View.VISIBLE);
        gameUI1.setVisibility(View.VISIBLE);
        gameUI2.setVisibility(View.VISIBLE);
        gazebackground1.setVisibility(View.VISIBLE);
        gazebackground2.setVisibility(View.VISIBLE);
        gazebackground3.setVisibility(View.VISIBLE);
        character1Btn.setVisibility(View.VISIBLE);
        character2Btn.setVisibility(View.VISIBLE);
        skillBtn.setVisibility(View.VISIBLE);
        rightBtn.setVisibility(View.VISIBLE);
        leftBtn.setVisibility(View.VISIBLE);
        pauseBtn.setVisibility(View.VISIBLE);
        enemybase.getEnemybase().setVisibility(View.VISIBLE);
        myCharacter.getPaladog().setVisibility(View.VISIBLE);
        maxResource.setVisibility(View.VISIBLE);
        resource.setVisibility(View.VISIBLE);
        maxMana.setVisibility(View.VISIBLE);
        mana.setVisibility(View.VISIBLE);
        timer.setVisibility(View.VISIBLE);
        resourceGaze.setVisibility(View.VISIBLE);
        manaGaze.setVisibility(View.VISIBLE);
        mapGaze1.setVisibility(View.VISIBLE);
        mapGaze2.setVisibility(View.VISIBLE);
        location.setVisibility(View.VISIBLE);
        //마나와 자원게이지가 차는 것처럼 만들기위해 width가 필요하다. 위치 상 안될 이유가 없는데 안되서 핸들러를 사용해서 해결
        //location 핸들러 따로 추가하기 좀 그래서 이 안에 같이 넣음
        getWidthHandler.sendMessage(getWidthHandler.obtainMessage());
        //같은 방식으로 맵게이지(체력바) 핸들러
        getMapWidthHandler.sendMessage(getMapWidthHandler.obtainMessage());

        //플레이타임 타이머 시작
        timerCount = new Runnable() {
            @Override
            public void run() {
                try {
                    timerHandler.sendMessage(timerHandler.obtainMessage());
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        };

        timerCountService = Executors.newSingleThreadScheduledExecutor();
        timerCountServiceFuture = timerCountService.scheduleAtFixedRate(timerCount, 0, 1, TimeUnit.SECONDS); //초당 서비스

        //마나, 자원 관리 시작
        regen = new Runnable() {
            @Override
            public void run() {
                try {
                    manaRegenHandler.sendMessage(manaRegenHandler.obtainMessage());
                    resourceRegenHandler.sendMessage(resourceRegenHandler.obtainMessage());
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        };

        regenService = Executors.newSingleThreadScheduledExecutor();
        regenServiceFuture = regenService.scheduleAtFixedRate(regen, 0, 1, TimeUnit.SECONDS); //초당 서비스

        //몬스터 생성 시작
        createMonster = new Runnable() {
            @Override
            public void run() {
                try {
                    createMonsterHandler.sendMessage(createMonsterHandler.obtainMessage());
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        };

        createMonsterservice = Executors.newSingleThreadScheduledExecutor();
        createMonsterFuture = createMonsterservice.scheduleAtFixedRate(createMonster, 0, 10, TimeUnit.SECONDS); //10초당 서비스

        if(!createMonsterFuture.isCancelled()) {
            //몬스터의 리스폰 시간을 주기적으로 random으로 설정
            Runnable changeDelay = new Runnable() {
                @Override
                public void run() {
                    int ran = random.nextInt(10000) + 10000; //최소 10초의 텀을 가짐
                    createMonsterFuture.cancel(true); //서비스 취소 후
                    createMonsterFuture = createMonsterservice.scheduleAtFixedRate(createMonster, 0, ran, TimeUnit.MILLISECONDS); //서비스 랜덤으로 재설정
                }
            };
            changeDelayService = Executors.newSingleThreadScheduledExecutor();
            changeDelayServiceFuture = changeDelayService.scheduleAtFixedRate(changeDelay, 10, 20, TimeUnit.SECONDS);//그 랜덤을 10초 후에 20초마다 재설정->랜덤생성
        }

    }

    Handler getWidthHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){ //구성보면 이것도 set이긴한데 get
            manaGazeWidth = manaGaze.getWidth();
            resourceGazeWidth = resourceGaze.getWidth();
            initManaGazeWidth = manaGazeWidth; //게임시작전에 후에 리셋하기 위한 초기값 설정해두기
            initResourceGazeWidth = resourceGazeWidth; //이때의 width들은 꽉 차 있을 때의 width들임
            //따로 핸들러만들면 복잡해지니 여기 선언
            location.setX(mainLinear.getWidth()*11/30); //최초 위치
        }
    };
    Handler setWidthHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            manaGaze.getLayoutParams().width = (int)initManaGazeWidth;//리셋할 때는 저장해두었던 초기값으로 재설정
            resourceGaze.getLayoutParams().width = (int)initResourceGazeWidth;
        }
    };
    Handler getMapWidthHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            mapGaze1Width = mapGaze1.getWidth();
            mapGaze2Width = mapGaze2.getWidth();
            initmapGaze1Width = mapGaze1Width; //게임시작전에 리셋하기 위한 초기값 설정해두기
            initmapGaze2Width = mapGaze2Width;

        }
    };
    Handler setMapWidthHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            mapGaze1.getLayoutParams().width = (int)initmapGaze1Width;//리셋할 때는 저장해두었던 초기값으로 재설정
            mapGaze2.getLayoutParams().width = (int)initmapGaze2Width;
        }
    };
    Handler manaRegenHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) { //마나리젠
            if(manaCount < maxManaCount) {//한도 안 찰 때만 실행
                manaCount++;
                mana.setText("" + (int)manaCount); //수치로 보여주고
                manaGaze.getLayoutParams().width = (int)(manaGazeWidth*manaCount/maxManaCount); //이것이 차오르도록 보이게 함, 계산식 = 너비*현재마나/최대마나
            }
            if(manaCount>=10) skillBtn.setImageResource(R.drawable.skill_btn); //버튼 활성화를 시각적으로
            else skillBtn.setImageResource(R.drawable.skill_btn_not);
        }
    };
    Handler resourceRegenHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) { //자원리젠
            if(resourceCount < maxResourceCount) {
                resourceCount++;
                resource.setText("" + (int)resourceCount);
                resourceGaze.getLayoutParams().width = (int)(resourceGazeWidth*resourceCount/maxResourceCount);
            }
            if(resourceCount>=10) character1Btn.setImageResource(R.drawable.character1_btn); //버튼 활성화를 시각적으로
            else character1Btn.setImageResource(R.drawable.character1_btn_cool);
            if(resourceCount>=20) character2Btn.setImageResource(R.drawable.character2_btn);
            else  character2Btn.setImageResource(R.drawable.character2_btn_cool);
        }
    };
    Handler timerHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) { //플레이타임
            timerSecond++;
            if(timerSecond==60){
                timerSecond = 0;
                timerMinute++;
            }
            timer.setText(timerMinute+":"+timerSecond);
        }
    };
    Handler createMonsterHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) { //몬스터생성
            monster = new Monster(gameManagerContext);
            monster.setGameManager(gameManager);
            monsterIndex.add(monster);
        }
    };

    public void reset(){//게임 초기화 메소드
        //리셋할 요소 : 캐릭터, 몬스터, 배경, 여러가지 UI
        //일단 게임 끝났을 때, 했던 버튼 비활성화 전부 활성화
        rightBtn.setEnabled(true);
        leftBtn.setEnabled(true);
        pauseBtn.setEnabled(true);
        character1Btn.setEnabled(true);
        character2Btn.setEnabled(true);
        skillBtn.setEnabled(true);
        //일단 전부 스테이지선택창으로 돌아가기 때문에 게임창 전부 안보이게
        gameBackground1.setVisibility(View.GONE);
        gameBackground2.setVisibility(View.GONE);
        gameBackground3.setVisibility(View.GONE);
        gameUI1.setVisibility(View.GONE);
        gameUI2.setVisibility(View.GONE);
        gazebackground1.setVisibility(View.GONE);
        gazebackground2.setVisibility(View.GONE);
        gazebackground3.setVisibility(View.GONE);
        character1Btn.setVisibility(View.GONE);
        character2Btn.setVisibility(View.GONE);
        skillBtn.setVisibility(View.GONE);
        rightBtn.setVisibility(View.GONE);
        leftBtn.setVisibility(View.GONE);
        pauseBtn.setVisibility(View.GONE);
        pauseUI.setVisibility(View.GONE);
        giveupBtn.setVisibility(View.GONE);
        resumeBtn.setVisibility(View.GONE);
        maxResource.setVisibility(View.GONE);
        resource.setText("0"); //그 외 이런 갖가지 변수 초기화
        resource.setVisibility(View.GONE);
        resourceCount = 0;
        maxMana.setVisibility(View.GONE);
        mana.setText("0");
        mana.setVisibility(View.GONE);
        timer.setText("0:0");
        timer.setVisibility(View.GONE);
        timerMinute = 0;
        timerSecond = 0;
        manaCount = 0;
        resourceGaze.setVisibility(View.GONE);
        manaGaze.setVisibility(View.GONE);
        mapGaze1.setVisibility(View.GONE);
        mapGaze2.setVisibility(View.GONE);
        location.setVisibility(View.GONE);
        //마나, 자원 리셋 핸들러, 스타트 때 했던 것 반대
        setWidthHandler.sendMessage(setWidthHandler.obtainMessage());
        //맵게이지 핸들러
        setMapWidthHandler.sendMessage(setMapWidthHandler.obtainMessage());

        myCharacter.getPaladog().setVisibility(View.GONE);
        enemybase.getEnemybase().setVisibility(View.GONE);
        enemybase.getEnemybase().setImageResource(R.drawable.enemybase);

        myCharacter.setHp(myCharacter.getMaxHP()); //피 만땅 만들어주기
        enemybase.setHp(enemybase.getMaxHP());

        createMonsterservice = null; //꼼꼼하게 서비스 다 없애주고
        createMonsterFuture.cancel(false);
        changeDelayService = null;
        changeDelayServiceFuture.cancel(false);
        regenService = null;
        regenServiceFuture.cancel(false);
        timerCountService = null;
        timerCountServiceFuture.cancel(false);

        if(createMonsterPhase2service != null) {
            createMonsterPhase2service = null;
            if (!createMonsterPhase2Future.isCancelled())
                createMonsterPhase2Future.cancel(false);
            isPhase2 = false;
        }

        for(int i = 0; i< monsterIndex.size(); i++){//몬스터 제거
            monster = monsterIndex.get(i);
            monster.getMonster().setVisibility(View.GONE);
            monster.setHp(0);
            monster.setMediaDie1(null); //이걸 안해두면 리셋할 때 단체로 죽어서 큰 소리로 죽는 소리 남
            monster = null;
        }
        monsterIndex.clear();

        if(character1Index.size() > 0){//캐릭터 제거
            for(int i = 0; i< character1Index.size(); i++){
                character1 = character1Index.get(i);
                character1.getCharacter1().setVisibility(View.GONE);
                character1.setHp(0);
                character1.setMediaDie1(null);//이걸 안해두면 리셋할 때 단체로 죽어서 큰 소리로 죽는 소리 남
                character1 = null;
            }
            character1Index.clear();
        }

        if(character2Index.size() > 0){
            for(int i = 0; i< character2Index.size(); i++){
                character2 = character2Index.get(i);
                character2.getCharacter2().setVisibility(View.GONE);
                character2.setHp(0);
                character2.setMediaDie1(null);//이걸 안해두면 리셋할 때 단체로 죽어서 큰 소리로 죽는 소리 남
                character2 = null;
            }
            character2Index.clear();
        }

        if(skillIndex.size() > 0){//혹시나 날아가던 스킬 제거, bullet은 캐릭터2가 죽을때, 사라짐
            for(int i = 0; i< skillIndex.size(); i++){
                skillIndex.get(i).getSkill1().setVisibility(View.GONE);
            }
            skillIndex.clear();
        }

        gameBackground1.setX(PxToDp(0)); //초기 위치들 재설정
        gameBackground2.setX(PxToDp(0));
        gameBackground3.setX(PxToDp(0));
        enemybase.getEnemybase().setX(PxToDp(16625));
        myCharacter.getPaladog().setX(PxToDp(700));
    }

    public  void phase2(){
        phase2Handler.sendMessage(phase2Handler.obtainMessage());
    } //페이지 2메소드

    private Handler phase2Handler = new Handler(){
        @Override
        public void handleMessage(Message msg) { //페이지2핸들러
            //별거 없다. 그냥 몬스터 생성 서비스 하나 추가된다.
            isPhase2 = true;
            enemybase.getEnemybase().setImageResource(R.drawable.enemybase_angry); //적 진지 이미지바뀜
            enemybase.getMediaAngry().start(); //효과음
            Runnable createPhase2Monster = new Runnable() {
                @Override
                public void run() {
                    try {
                        createMonsterHandler.sendMessage(createMonsterHandler.obtainMessage());
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };

            createMonsterPhase2service = Executors.newSingleThreadScheduledExecutor();
            createMonsterPhase2Future = createMonsterPhase2service.scheduleAtFixedRate(createPhase2Monster, 0, 20, TimeUnit.SECONDS);//20초마다 몬스터 추가 생성
        }
    };
    //px를 dp로 바꾸는거(여러 기기에 호환되도록)
    public static float PxToDp(float px){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return Math.round(dp);
    }
    public ImageView getMapGaze1() {
        return mapGaze1;
    }

    public ImageView getMapGaze2() {
        return mapGaze2;
    }

    public ImageView getResult() {
        return result;
    }

    public TextView getResultMinute() {
        return resultMinute;
    }

    public TextView getResultSecond() {
        return resultSecond;
    }

    public ImageButton getCharacter1Btn() {
        return character1Btn;
    }

    public ImageButton getCharacter2Btn() {
        return character2Btn;
    }

    public ImageButton getSkillBtn() {
        return skillBtn;
    }

    public ImageButton getRightBtn() {
        return rightBtn;
    }

    public ImageButton getLeftBtn() {
        return leftBtn;
    }

    public MyCharacter getMyCharacter() {
        return myCharacter;
    }

    public EnemyBase getEnemybase() {
        return enemybase;
    }

    public ArrayList<Monster> getMonsterIndex() {
        return monsterIndex;
    }

    public ArrayList<Character1> getCharacter1Index() {
        return character1Index;
    }

    public ArrayList<Character2> getCharacter2Index() {
        return character2Index;
    }

    public float getMapGaze1Width() {
        return mapGaze1Width;
    }

    public float getMapGaze2Width() {
        return mapGaze2Width;
    }

    public int getTimerSecond() {
        return timerSecond;
    }

    public int getTimerMinute() {
        return timerMinute;
    }

    public boolean isPhase2() {
        return isPhase2;
    }

    public MainActivity getMainActivity() { return mainActivity; }

    public void setMainActivity(MainActivity mainActivity) { this.mainActivity = mainActivity; }

    public boolean isGameWin() { return gameWin; }

    public void setGameWin(boolean gameWin) { this.gameWin = gameWin; }

    public boolean isGameLose() { return gameLose; }

    public void setGameLose(boolean gameLose) { this.gameLose = gameLose; }
}
