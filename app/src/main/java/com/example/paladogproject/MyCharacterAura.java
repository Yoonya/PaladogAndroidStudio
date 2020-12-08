package com.example.paladogproject;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

public class MyCharacterAura extends Activity {//내 캐릭터의 아우라 클래스
    //context
    private Context MyCharacterAuraContext;
    //이미지
    private ImageView paladogAura;
    private ImageView paladog;
    //애니메이션
    private AnimationDrawable auraAnimation;
    private int animationFrame = 1000/15;
    private Thread thread;
    //클래스
    private GameManager gameManager;
    private Character character;
    //레이아웃선언
    private ConstraintLayout mainLinear;

    public MyCharacterAura(Context context) {

        this.MyCharacterAuraContext = context;
        //변수
        mainLinear = ((ConstraintLayout) ((Activity) MyCharacterAuraContext).findViewById(R.id.activity_main));

        paladogAura = new ImageView(MyCharacterAuraContext);

        paladogAura.setId((int)5); //캐릭터 아우라가 쓰게 하기 위해서 아이디 설정, 안해두면 캐릭터아우라가 최초 생성될 때, onCreate에서 초기화가 안된다.
        paladogAura.setVisibility(View.GONE);
        mainLinear.addView(paladogAura, 9); //이 index는 팔라독 바로 위 쪽이다.

        //애니메이션
        auraAnimation = new AnimationDrawable();

        BitmapDrawable auraFrame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.eff_aura1);
        BitmapDrawable auraFrame2 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.eff_aura2);
        BitmapDrawable auraFrame3 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.eff_aura3);
        BitmapDrawable auraFrame4 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.eff_aura4);
        BitmapDrawable auraFrame5 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.eff_aura5);
        BitmapDrawable auraFrame6 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.eff_aura6);

        auraAnimation.addFrame(auraFrame1, animationFrame);
        auraAnimation.addFrame(auraFrame2, animationFrame);
        auraAnimation.addFrame(auraFrame3, animationFrame);
        auraAnimation.addFrame(auraFrame4, animationFrame);
        auraAnimation.addFrame(auraFrame5, animationFrame);
        auraAnimation.addFrame(auraFrame6, animationFrame);

        paladogAura.setBackgroundDrawable(auraAnimation);

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

   private Handler auraHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            auraAnimation.start();//스타트
            //순서상 여기서 선언할 수 밖에 없는데, 이러면 최초실행시 위에 뜨는게 살짝 보인다.(왜냐하면 팔라독을 따라가는 것이기 때문에 맨 처음은 팔라독이 설정되어 있지 않기 때문에)
            //ocCreate에서 최초 지점을 팔라독 위치와 똑같이 설정은 안됨. 최초 팔라독이 GONE이기 때문
            //신경쓰이긴 하지만, 그러려면 gameManager를 건드려야 해서 일단 놔둠(gameManager의 코드가 늘어남)
            paladogAura.setX(paladog.getX() - paladog.getWidth()*2/5); //최초 위치
            paladogAura.setY(paladog.getY() + paladog.getHeight()*3/5); //이미지 생김새상 이렇게 해야맞음
            if(paladog.getVisibility() == View.VISIBLE) paladogAura.setVisibility(View.VISIBLE);
            if(paladog.getVisibility() == View.GONE) paladogAura.setVisibility(View.GONE); //보이게 안보이게
        }
    };

    public void setPaladog(ImageView paladog) {
        this.paladog = paladog;
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public Thread getThread() {
        return thread;
    }

}
