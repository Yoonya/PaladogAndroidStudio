package com.example.paladogproject;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

public class CharacterAura extends Activity { //생성하는 캐릭터들의 아우라를 담당하는 클래스
    //context
    private Context characterAuraContext;
    //이미지
    private ImageView paladogAura; //내 캐릭터의 아우라가 필요
    private ImageView characterAura;

    //애니메이션
    private AnimationDrawable auraAnimation;
    private int animationFrame = 1000/15;
    private Thread thread;
    //클래스
    private GameManager gameManager;
    private Character1 character1;
    private Character2 character2;
    //레이아웃선언
    private ConstraintLayout mainLinear;

    public CharacterAura(Context context) {
        this.characterAuraContext = context;
        //변수
        mainLinear = ((ConstraintLayout) ((Activity) characterAuraContext).findViewById(R.id.activity_main));
        //ImageView생성
        paladogAura = ((Activity) characterAuraContext).findViewById((int)5);
        characterAura = new ImageView(characterAuraContext);
        characterAura.setVisibility(View.GONE); //일단 안보이게

        mainLinear.addView(characterAura, 9); //최소 배경 위에 그려짐

        //애니메이션
        auraAnimation = new AnimationDrawable();

        BitmapDrawable auraFrame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.eff_smallaura1);
        BitmapDrawable auraFrame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.eff_smallaura2);
        BitmapDrawable auraFrame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.eff_smallaura3);
        BitmapDrawable auraFrame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.eff_smallaura4);
        BitmapDrawable auraFrame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.eff_smallaura5);
        BitmapDrawable auraFrame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.eff_smallaura6);

        auraAnimation.addFrame(auraFrame1, animationFrame);
        auraAnimation.addFrame(auraFrame2, animationFrame);
        auraAnimation.addFrame(auraFrame3, animationFrame);
        auraAnimation.addFrame(auraFrame4, animationFrame);
        auraAnimation.addFrame(auraFrame5, animationFrame);
        auraAnimation.addFrame(auraFrame6, animationFrame);

        characterAura.setBackgroundDrawable(auraAnimation);

        auraAnimation.setOneShot(false);

        thread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(animationFrame);
                        auraHandler.sendMessage(auraHandler.obtainMessage());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        });

    }

    Handler auraHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            auraAnimation.start(); //애니메이션 시작
            if(character2 == null) { //character1쪽 이라면
                if (character1.getCharacter1().getVisibility() == View.GONE)
                    characterAura.setVisibility(View.GONE); //먼저 캐릭터가 없으면 보이지말아야한다.

                characterAura.setX(character1.getCharacter1().getX()); //위치
                characterAura.setY(character1.getCharacter1().getY() + character1.getCharacter1().getHeight() * 1 / 2); //이미지 생김새상 이렇게 해야맞음

                //팔라독의 오오라 범위 안에 들어갈 때
                if (character1.getCharacter1().getX() + character1.getCharacter1().getWidth() / 2 >= paladogAura.getX()
                        && character1.getCharacter1().getX() + character1.getCharacter1().getWidth() / 2 <= (paladogAura.getX() + paladogAura.getWidth())) {
                    //이미지의 생김새상 더해진게 많아서 좀 더러운데 간략해보면 캐릭터의 x좌표가 팔라독아우라의 x좌표보다 크고
                    //&&캐릭터의 x좌표가 팔라독아우라의 x좌표 끝 부분보다 작다면   라는 뜻이다.
                    if (character1.getHp() > 0) { //살아있을 때만
                        characterAura.setVisibility(View.VISIBLE);
                        character1.setOnAura(true); //캐릭터 aura 상태를 체크해줌, 체크해야 스킬 씀
                    }
                    else {//죽으면 끄고
                        characterAura.setVisibility(View.GONE);
                        character1.setOnAura(false);
                    }
                }
                else {//범위 밖이어도 끄고
                    characterAura.setVisibility(View.GONE);
                    character1.setOnAura(false);
                }
            }
            else if(character1 == null) { //character2쪽 이라면
                //위와 같다.
                if (character2.getCharacter2().getVisibility() == View.GONE)
                    characterAura.setVisibility(View.GONE);

                characterAura.setX(character2.getCharacter2().getX() + character2.getCharacter2().getWidth()/10); //위치
                characterAura.setY(character2.getCharacter2().getY() + character2.getCharacter2().getHeight() * 1 / 2); //이미지 생김새상 이렇게 해야맞음

                //팔라독의 오오라 범위 안에 들어갈 때
                if (character2.getCharacter2().getX() + character2.getCharacter2().getWidth() / 2 >= paladogAura.getX() && character2.getCharacter2().getX() + character2.getCharacter2().getWidth() / 2 <= (paladogAura.getX() + paladogAura.getWidth())) {
                    if (character2.getHp() > 0) { //살아있을 때만
                        characterAura.setVisibility(View.VISIBLE);
                        character2.setOnAura(true); //캐릭터 aura 상태를 체크해줌
                    } else {
                        characterAura.setVisibility(View.GONE);
                        character2.setOnAura(false);
                    }
                } else {
                    characterAura.setVisibility(View.GONE);
                    character2.setOnAura(false);
                }
            }
        }
    };

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void setCharacter1(Character1 character1) {
        this.character1 = character1;
    }

    public void setCharacter2(Character2 character2) {
        this.character2 = character2;
    }

    public ConstraintLayout getMainLinear() {
        return mainLinear;
    }

    public void setMainLinear(ConstraintLayout mainLinear) {
        this.mainLinear = mainLinear;
    }

    public Thread getThread() { return thread; }
}
