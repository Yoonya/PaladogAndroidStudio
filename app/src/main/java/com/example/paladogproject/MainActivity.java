package com.example.paladogproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static androidx.constraintlayout.widget.ConstraintLayout.*;

//이쪽에서는 게임과 끝 메인창과 스테이지 선택창의 UI를 담당한다.
public class MainActivity extends AppCompatActivity {
    //상태바와 네비게이션바 없애기에 사용될 것
    private View decorView;
    private int uiOption;
    //Context
    public static Context mContext;
    //핸들러
    private Handler gotoStageHandler; //메인창->스테이지선택창
    private Handler gotoStageHandler2; //게임창->스테이지선택창(게임에서 패배시)
    private Handler gotoMainHandler; //스테이지선택창->메인창
    private Handler gotoGameHandler;//스테이지선택창->게임창
    private Handler giveupHandler;//게임창->스테이지선택창(메뉴창 클릭해서 이동)
    private Handler pauseHandler;//게임창에서 메뉴창 클릭
    private Handler resumeHandler;//게임 메뉴창에서 게임으로 다시 이동하도록

    //효과음 및 음악
    private MediaPlayer stageStartSound; //게임 시작시 효과음
    private MediaPlayer mainBgm; //메인창, 스테이지선책창 브금
    private MediaPlayer playBtnClick; //메인창 플레이버튼 클릭 효과음
    private MediaPlayer stageSound; //게임 배경 브금
    private MediaPlayer stageLose; //게임 패배 효과음
    private MediaPlayer stageWin; //게임 승리 효과음
    //메인 타이틀과 스테이지선택 배경
    private ImageView opening;
    private ImageView stageSelect;
    //게임창
    private ImageView pauseUI; //메뉴창
    private ImageView enemybase; //적 진지
    private ImageView missionFailed; //게임 패배
    //게임 끝 애니메이션
    AnimationDrawable gameEndAnimation;
    //버튼
    private ImageButton playBtn; //메인 창 플레이 버튼
    private ImageButton map1Btn; //스테이지선택창 게임맵 1 버튼
    private ImageButton stageExitBtn; //스테이지선택창의 x버튼
    private ImageButton pauseBtn; //게임창의 메뉴버튼
    private ImageButton giveupBtn; //게임창의 메뉴버튼의 포기버튼
    private ImageButton resumeBtn; //게임창의 메뉴버튼의 게임재개 버튼
    private ImageButton okBtn; //게임 이겼을 때, 나오는 버튼
    private ImageButton endBtn; //게임 졌을 때, 나오는 버튼
    //클래스
    private GameManager gameManager; //게임매니저와 상호작용
    //레이아웃선언
    private ConstraintLayout mainLinear; //xml과 상호작용
    //boolean
    boolean bgmIsPlaying = false;
    /*이 변수는 개발 초기에 스마트폰으로 다른 창으로 이동했을 때, Mediaplayer가 에러가 걸려서 그것을
    제어하려고 선언했던 변수이다. 메인창, 스테이지선택창, 게임창의 메인브금들을 제어하여 다른 창을
    다녀와도 에러가 나지 않는다.
    하지만 후에 효과음이 계속 추가되면서 이 변수의 의의는 많이 없어졌다. 지금 이것까지 손 볼 겨를은 없다.*/

    //게임이 끝났을 때, 클리어 시간을 나타내기 위한 변수
    private int timerMinute;
    private int timerSecond;
    //게임이 끝났을 때, 애니메이션을 실행시키기 위해 애니메이션의 크기를 지정하기 위한 것
    private LinearLayout.LayoutParams layoutParams = //레이아웃크기
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        deleteStatusBar(); //게임 전체화면을 위해서 모바일 상태바들을 없애줍니다.
        mContext = this;
        //변수들에 값 설정
        mainLinear = (ConstraintLayout)findViewById(R.id.activity_main);

        mainBgm = MediaPlayer.create(this, R.raw.main);
        playBtnClick = MediaPlayer.create(this, R.raw.startbtn);

        opening = (ImageView)findViewById(R.id.opening);
        stageSelect =(ImageView)findViewById(R.id.stage);
        pauseUI = (ImageView)findViewById(R.id.pauseui);
        enemybase = (ImageView)findViewById(R.id.enemybase);
        missionFailed = new ImageView(this);
        missionFailed.setVisibility(View.GONE);
        missionFailed.setLayoutParams(layoutParams); //레이아웃 크기
        mainLinear.addView(missionFailed, 38);//다른거 다 덮어야함

        playBtn = (ImageButton)findViewById(R.id.playbtn);
        map1Btn = (ImageButton)findViewById(R.id.map1_btn);
        stageExitBtn = (ImageButton)findViewById(R.id.exitbtn);
        pauseBtn = (ImageButton)findViewById(R.id.pausebtn);
        giveupBtn = (ImageButton)findViewById(R.id.giveupebtn);
        resumeBtn = (ImageButton)findViewById(R.id.resumebtn);
        okBtn = (ImageButton)findViewById(R.id.okbtn);
        endBtn = (ImageButton)findViewById(R.id.endbtn);

        //게임엔딩 애니메이션
        gameEndAnimation = new AnimationDrawable();
        BitmapDrawable dieFrame1 = (BitmapDrawable)getResources().getDrawable(R.drawable.paladog_die1);
        BitmapDrawable dieFrame2 = (BitmapDrawable)getResources().getDrawable(R.drawable.paladog_die2);
        BitmapDrawable dieFrame3 = (BitmapDrawable)getResources().getDrawable(R.drawable.paladog_die3);
        BitmapDrawable dieFrame4 = (BitmapDrawable)getResources().getDrawable(R.drawable.paladog_die4);
        BitmapDrawable dieFrame5 = (BitmapDrawable)getResources().getDrawable(R.drawable.paladog_die5);
        BitmapDrawable dieFrame6 = (BitmapDrawable)getResources().getDrawable(R.drawable.paladog_die6);
        BitmapDrawable dieFrame7 = (BitmapDrawable)getResources().getDrawable(R.drawable.paladog_die7);
        BitmapDrawable dieFrame8 = (BitmapDrawable)getResources().getDrawable(R.drawable.paladog_die8);
        BitmapDrawable dieFrame9 = (BitmapDrawable)getResources().getDrawable(R.drawable.paladog_die9);
        BitmapDrawable dieFrame10 = (BitmapDrawable)getResources().getDrawable(R.drawable.paladog_die10);
        BitmapDrawable dieFrame11 = (BitmapDrawable)getResources().getDrawable(R.drawable.paladog_die11);
        BitmapDrawable dieFrame12 = (BitmapDrawable)getResources().getDrawable(R.drawable.paladog_die12);
        BitmapDrawable dieFrame13 = (BitmapDrawable)getResources().getDrawable(R.drawable.paladog_die13);
        BitmapDrawable dieFrame14 = (BitmapDrawable)getResources().getDrawable(R.drawable.paladog_die14);
        BitmapDrawable dieFrame15 = (BitmapDrawable)getResources().getDrawable(R.drawable.paladog_die15);
        BitmapDrawable dieFrame16 = (BitmapDrawable)getResources().getDrawable(R.drawable.paladog_die16);
        BitmapDrawable dieFrame17 = (BitmapDrawable)getResources().getDrawable(R.drawable.paladog_die17);
        BitmapDrawable dieFrame18 = (BitmapDrawable)getResources().getDrawable(R.drawable.paladog_die18);
        BitmapDrawable dieFrame19 = (BitmapDrawable)getResources().getDrawable(R.drawable.paladog_die19);
        gameEndAnimation.addFrame(dieFrame1, 200); //어차피 애니메이션 하나인데 그냥 상수로 프레임 선언
        gameEndAnimation.addFrame(dieFrame2, 200);
        gameEndAnimation.addFrame(dieFrame3, 200);
        gameEndAnimation.addFrame(dieFrame4, 200);
        gameEndAnimation.addFrame(dieFrame5, 200);
        gameEndAnimation.addFrame(dieFrame6, 200);
        gameEndAnimation.addFrame(dieFrame7, 200);
        gameEndAnimation.addFrame(dieFrame8, 200);
        gameEndAnimation.addFrame(dieFrame9, 200);
        gameEndAnimation.addFrame(dieFrame10, 200);
        gameEndAnimation.addFrame(dieFrame11, 200);
        gameEndAnimation.addFrame(dieFrame12, 200);
        gameEndAnimation.addFrame(dieFrame13, 200);
        gameEndAnimation.addFrame(dieFrame14, 200);
        gameEndAnimation.addFrame(dieFrame15, 200);
        gameEndAnimation.addFrame(dieFrame16, 200);
        gameEndAnimation.addFrame(dieFrame17, 200);
        gameEndAnimation.addFrame(dieFrame18, 200);
        gameEndAnimation.addFrame(dieFrame19, 200);
        missionFailed.setBackgroundDrawable(gameEndAnimation);
        gameEndAnimation.setOneShot(true); //반복하지 않도록
        //메인 타이틀 음악시작
        mainBgm.start();
        mainBgm.setLooping(true); //브금 반복
        bgmIsPlaying = true;

        gameManager = new GameManager(this); //게임매니저 설정
        gameManager.setMainActivity(this);

        //버튼 클릭 이벤트은 전부 -> 효과음 + 핸들러 호출

        //메인 타이틀 버튼 클릭 이벤트
        playBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    playBtn.setImageResource(R.drawable.play_btn_pushed); //누름 표시
                    playBtnClick.start(); //이거 효과음
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    playBtn.setImageResource(R.drawable.play_btn);
                    gotoStageHandler.sendMessageDelayed(gotoStageHandler.obtainMessage(), 0); //핸들러 명령
                }
                return false;
            }
        });
        //stageselect할 때, 버튼 클릭 이벤트
        map1Btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    map1Btn.setImageResource(R.drawable.map1_btn_pushed);
                    playBtnClick.start();
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    map1Btn.setImageResource(R.drawable.map1_btn);
                    gotoGameHandler.sendMessageDelayed(gotoGameHandler.obtainMessage(), 0);
                }
                return false;
            }
        });
        //stageExit할 때, 버튼 클릭 이벤트
        stageExitBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    stageExitBtn.setImageResource(R.drawable.exit_btn_pushed);
                    playBtnClick.start();
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    stageExitBtn.setImageResource(R.drawable.exit_btn);
                    gotoMainHandler.sendMessageDelayed(gotoMainHandler.obtainMessage(), 0);
                }
                return false;
            }
        });

        //일시정지버튼(아직은 메뉴버튼)
        pauseBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    pauseBtn.setImageResource(R.drawable.pause_pushed);
                    playBtnClick.start();
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    pauseBtn.setImageResource(R.drawable.pause_btn);
                    pauseHandler.sendMessageDelayed(pauseHandler.obtainMessage(), 0);
                }
                return false;
            }
        });
        //포기하기버튼
        giveupBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    giveupBtn.setImageResource(R.drawable.giveup_btn_pushed);
                    playBtnClick.start();
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    giveupBtn.setImageResource(R.drawable.giveup_btn);
                    giveupHandler.sendMessageDelayed(giveupHandler.obtainMessage(), 0);
                }
                return false;
            }
        });
        //게임재개버튼
        resumeBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    resumeBtn.setImageResource(R.drawable.resume_btn_pushed);
                    playBtnClick.start();
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    resumeBtn.setImageResource(R.drawable.resume_btn);
                    resumeHandler.sendMessageDelayed(resumeHandler.obtainMessage(), 0);
                }
                return false;
            }
        });
        //result ok버튼
        okBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    okBtn.setImageResource(R.drawable.okbtn_pushed);
                    playBtnClick.start();
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    okBtn.setImageResource(R.drawable.okbtn);
                    gotoStageHandler2.sendMessageDelayed(gotoStageHandler2.obtainMessage(), 0);
                }
                return false;
            }
        });
        //죽었을 때 end버튼
        endBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    endBtn.setImageResource(R.drawable.nextbtn_pushed);
                    playBtnClick.start();
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    endBtn.setImageResource(R.drawable.nextbtn);
                    gotoStageHandler2.sendMessageDelayed(gotoStageHandler2.obtainMessage(), 0);
                }
                return false;
            }
        });
        //핸들러 설정, *UI바꾸는건 handler를 이용해야한다*
        //메인창->스테이지선택창
        gotoStageHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                playBtn.setVisibility(View.GONE);
                opening.setVisibility(View.GONE);
                stageSelect.setVisibility(View.VISIBLE);
                map1Btn.setVisibility(View.VISIBLE);
                stageExitBtn.setVisibility(View.VISIBLE);
            }
        };
        //게임창->스테이지선택창(게임에서 패배시)
        gotoStageHandler2 = new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                //게임정보 리셋 및 창 변경 및 브금 시작 등
                timerSecond = 0;
                timerMinute = 0;
                gameManager.getResult().setVisibility(View.GONE);
                //gameManager.resultNew.setVisibility(View.VISIBLE);
                gameManager.getResultMinute().setVisibility(View.GONE);
                gameManager.getResultSecond().setVisibility(View.GONE);
                okBtn.setVisibility(View.GONE);
                endBtn.setVisibility(View.GONE);
                missionFailed.setVisibility(View.GONE);
                gameEndAnimation.stop();

                stageSelect.setVisibility(View.VISIBLE);
                map1Btn.setVisibility(View.VISIBLE);
                stageExitBtn.setVisibility(View.VISIBLE);

                mainBgm = MediaPlayer.create(mContext, R.raw.main);
                mainBgm.start();
                mainBgm.setLooping(true);
                bgmIsPlaying = true;

                gameManager.reset();
            }
        };
        //스테이지선택창->게임창
        gotoGameHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                mainBgm.reset();
                mainBgm.release();
                bgmIsPlaying = false;

                stageSelect.setVisibility(View.GONE);
                map1Btn.setVisibility(View.GONE);
                stageExitBtn.setVisibility(View.GONE);

                //게임 시작 시 한번 실행되는 효과음
                stageStartSound = MediaPlayer.create(mContext, R.raw.start_battle);
                stageStartSound.start();
                //스테이지 판이 시작하면 한번 재생되도록 하기 위해
                stageStartSound.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        stageStartSound.reset();
                        stageStartSound.release(); // 다 끝나면 release해줘야함
                    }
                });
                //브금 시작
                stageSound = MediaPlayer.create(mContext, R.raw.stage);
                stageSound.start();
                stageSound.setLooping(true);
                bgmIsPlaying = true;
                //게임시작
                gameManager.start();
            }
        };
        //스테이지선택창->메인창
        gotoMainHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                playBtn.setVisibility(View.VISIBLE);
                opening.setVisibility(View.VISIBLE);
                stageSelect.setVisibility(View.GONE);
                map1Btn.setVisibility(View.GONE);
                stageExitBtn.setVisibility(View.GONE);
            }
        };

        //일시정지버튼으로 인해 게임창에서 메뉴창이 생성되는 과정
        pauseHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                pauseUI.setVisibility(View.VISIBLE);
                giveupBtn.setVisibility(View.VISIBLE);
                resumeBtn.setVisibility(View.VISIBLE);
            }
        };

        //포기버튼으로 인해 스테이지선택창으로
        giveupHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                stageSound.reset();
                stageSound.release();
                bgmIsPlaying = false;

                stageSelect.setVisibility(View.VISIBLE);
                map1Btn.setVisibility(View.VISIBLE);
                stageExitBtn.setVisibility(View.VISIBLE);

                mainBgm = MediaPlayer.create(mContext, R.raw.main);
                mainBgm.start();
                mainBgm.setLooping(true);
                bgmIsPlaying = true;
                //항상 게임은 리셋
                gameManager.reset();
            }
        };

        //resume버튼으로 인해 게임이 다시 재개
        resumeHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                pauseUI.setVisibility(View.GONE);
                giveupBtn.setVisibility(View.GONE);
                resumeBtn.setVisibility(View.GONE);
            }
        };

    }
    //게임 승리를 호출하는 메소드
    public void gameEndWin(){
        gameEndWinHandler.sendMessage(gameEndWinHandler.obtainMessage());
    }
    //게임 패배를 호출하는 메소드
    public void gameEndLose(){
        gameEndLoseHandler.sendMessage(gameEndLoseHandler.obtainMessage());
    }
    private Handler gameEndLoseHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) { //UI를 건들이는 것이라 따로 핸들링
            //캐릭터 동작 중이면 모든 것 정지
            if(gameManager.getMyCharacter().getIdleAnimation().isRunning()) gameManager.getMyCharacter().getIdleAnimation().stop();
            if(gameManager.getMyCharacter().getRightWalkAnimation().isRunning()) gameManager.getMyCharacter().getRightWalkAnimation().stop();
            if(gameManager.getMyCharacter().getLeftWalkAnimation().isRunning()) gameManager.getMyCharacter().getLeftWalkAnimation().stop();
            if(gameManager.getMyCharacter().getAttackAnimation().isRunning()) gameManager.getMyCharacter().getAttackAnimation().stop();
            //게임패배 애니메이션 시작
            missionFailed.setVisibility(View.VISIBLE);
            gameEndAnimation.start();

            stageSound.reset();
            stageSound.release();
            bgmIsPlaying = false;
            gameManager.getMyCharacter().getMediaDie().start(); //내 캐릭터 죽는 효과음

            stageLose = MediaPlayer.create(mContext, R.raw.stage_fail);//게임 패배시 나오는 효과음
            stageLose.start();

            //모든 버튼 사용 불가
            gameManager.getRightBtn().setEnabled(false);
            gameManager.getLeftBtn().setEnabled(false);
            pauseBtn.setEnabled(false);
            gameManager.getCharacter1Btn().setEnabled(false);
            gameManager.getCharacter2Btn().setEnabled(false);
            gameManager.getSkillBtn().setEnabled(false);

            stageLose.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stageLose.reset();
                    stageLose.release(); // 다 끝나면 release해줘야함
                    endBtn.setVisibility(View.VISIBLE);//효과음이 끝났을 때, 버튼이 나오도록 *연출*
                }
            });

        }
    };

    private Handler gameEndWinHandler = new Handler(){ //다시시작, 시작 부분바꿔야할것
        @Override
        public void handleMessage(Message msg) { //UI를 건들이는 것이라 따로 핸들링
            enemybase.setImageResource(R.drawable.enemybase_dead); //죽은 이미지 변경

            stageSound.reset();
            stageSound.release();
            bgmIsPlaying = false;
            gameManager.getEnemybase().getMediaDie().start(); //적 진지 파괴 음악

            stageWin = MediaPlayer.create(mContext, R.raw.stage_clear); //게임 승리 브금
            stageWin.start();

            //동작 불가
            gameManager.getRightBtn().setEnabled(false);
            gameManager.getLeftBtn().setEnabled(false);
            pauseBtn.setEnabled(false);
            gameManager.getCharacter1Btn().setEnabled(false);
            gameManager.getCharacter2Btn().setEnabled(false);
            gameManager.getSkillBtn().setEnabled(false);

            //게임매니저에서 측정 중이던 플레이 타임 받아오기
            timerMinute = gameManager.getTimerMinute();
            timerSecond = gameManager.getTimerSecond();

            stageWin.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stageWin.reset();
                    stageWin.release(); // 다 끝나면 release해줘야함
                    //승리 브금이 끝났을 때, UI가 뜨도록 *연출*
                    gameManager.getResult().setVisibility(View.VISIBLE);
                    //gameManager.resultNew.setVisibility(View.VISIBLE);
                    gameManager.getResultMinute().setVisibility(View.VISIBLE);
                    gameManager.getResultSecond().setVisibility(View.VISIBLE);
                    gameManager.getResultMinute().setText(""+timerMinute);
                    gameManager.getResultSecond().setText(""+timerSecond);
                    okBtn.setVisibility(View.VISIBLE);
                }
            });

        }
    };

    @Override
    protected void onStop() { //홈 화면으로 나가도 노래가 계속 재생되던 것을 고침
        super.onStop();
        if (bgmIsPlaying && ((opening.getVisibility() == View.VISIBLE)
                || (stageSelect.getVisibility() == View.VISIBLE))){ //mainBgm release상태라 오류 발생해서 추가
            if(mainBgm.isPlaying()) mainBgm.pause();
         }
        else if(bgmIsPlaying && ((opening.getVisibility() == View.GONE)
                && (stageSelect.getVisibility() == View.GONE))) {
            if(stageSound.isPlaying()) stageSound.pause();
        }
    }

    @Override
    protected void onRestart(){ //홈 화면을 돌아왔더니 상태바와 네비게이션바가 재생성되어 고침
        super.onRestart();
        deleteStatusBar();
        if(((opening.getVisibility() == View.VISIBLE) //멈췄던 bgm들 다시 시작
                || (stageSelect.getVisibility() == View.VISIBLE)))
            mainBgm.start();
        else if((opening.getVisibility() == View.GONE)
                && (stageSelect.getVisibility() == View.GONE))
            stageSound.start();
    }

    private void deleteStatusBar() { //상태바와 네비게이션바 없애기(가져온것)
        decorView = getWindow().getDecorView();
        uiOption = decorView.getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            uiOption |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOption);
    }
}
