package com.example.paladogproject;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Bullet extends Activity {

    //context
    private Context bulletContext;
    //날아가는 스피드
    private float speed = PxToDp(175);
    //필요 클래스
    private GameManager gameManager;
    private Character2 character2;
    private Monster monster;
    private ImageView enemybase;
    private ImageView arrow;
    //애니메이션 및 스레드
    private Thread thread;
    private AnimationDrawable arrowAnimation;
    private AnimationDrawable arrowSkillAnimation;
    private int animationFrame = 1000/15;

    //레이아웃선언
    private ConstraintLayout mainLinear;
    //변수
    private boolean isCharacterCollision = false; //몬스터 충돌감지
    private boolean isEnemybaseCollision = false; //적 진지 충돌감지
    private boolean attacking = false; //공격 제어(캐릭터2가 true로 제어함)
    private ArrayList<ImageView> arrowList; //화살 저장할 리스트

    public Bullet(Context context) {
        this.bulletContext = context;
        //변수 선언
        mainLinear = ((ConstraintLayout) ((Activity) bulletContext).findViewById(R.id.activity_main));

        enemybase = ((Activity) bulletContext).findViewById((int)4);

        arrowList = new ArrayList<>();

        //애니메이션
        /*이미지가 한 장인데 왜 굳이 애니메이션?
        이 코드는 skill클래스를 이식해서 수정했다. 그러기에 이게 편했다. 그리고 화살 스킬의 경우 오히려 애니메이션이
        아닌 것이 이상한데 이미지가 1장이다. 이렇게 해두면 후에 애니메이션으로 바꿀 수도 있겠지.
        스킬화살이 애니메이션이면 통일을 위해 일반화살도 애니메이션이다.
        */
        arrowAnimation = new AnimationDrawable();
        arrowSkillAnimation = new AnimationDrawable();

        BitmapDrawable arrowFrame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.arrow);

        BitmapDrawable arrowSkillFrame1 = (BitmapDrawable)context.getResources().getDrawable(R.drawable.arrow_skill);

        arrowAnimation.addFrame(arrowFrame1, animationFrame);

        arrowSkillAnimation.addFrame(arrowSkillFrame1, animationFrame);

        arrowAnimation.setOneShot(false);
        arrowSkillAnimation.setOneShot(false);

        //Bullet 클래스 스레드
        thread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(animationFrame); //애니메이션프레임
                        skillHandler.sendMessage(skillHandler.obtainMessage()); //핸들러
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        for(int i = 0; i< arrowList.size(); i++){
                            arrowList.get(i).setVisibility(View.GONE);
                        }//스레드가 종료될 때(이 클래스는 character2가 생성될 때, 함께 생성되고 사라질때, 함께 사라짐), 흔적 지우기
                        arrowList.clear();
                        return;
                    }
                }
            }
        });
    }

    private Handler skillHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (attacking) {//공격허가
                if (character2.getDamage() == 15) { //캐릭터가 공격 중일 때
                    if (gameManager == null) {
                        gameManager = character2.getGameManager(); //순서 상 onCreate에서 하면 못 받더라
                    }

                    if (arrow == null) { //화살이 비었다면
                            //스킬의 ImageView생성
                        arrow = new ImageView(bulletContext); //화살 생성
                        arrowList.add(arrow); //리스트에 화살 더하고
                        mainLinear.addView(arrow, 11); //메인뷰에 추가시켜주고
                        arrow.setBackgroundDrawable(arrowAnimation); //애니메이션 설정해주고
                        arrow.setX(character2.getCharacter2().getX() + character2.getCharacter2().getWidth() / 2); //캐릭터의 위치에 장전되도록
                        arrow.setY(character2.getCharacter2().getY() + character2.getCharacter2().getHeight() / 2); //이미지 자료가 그런식이라 이렇게 설정한다.
                    }
                    if (arrow != null) {
                        arrowAnimation.start(); //화살 설정 되면 발사
                        arrow.setX(arrow.getX() + speed); //움직여

                        if (enemybaseCollision((int) arrow.getX(), (int) enemybase.getX(), arrow.getWidth(), enemybase.getWidth())) { //적 진지 충돌 감지
                            isEnemybaseCollision = true;
                        } else {
                            isEnemybaseCollision = false;
                        }

                        if (gameManager.getMonsterIndex().size() > 0) {
                            for (int i = 0; i < gameManager.getMonsterIndex().size(); i++) { //적 몬스터 충돌 감지
                                if (characterCollision((int) arrow.getX(), (int) gameManager.getMonsterIndex().get(i).getMonster().getX(), arrow.getWidth(), gameManager.getMonsterIndex().get(i).getMonster().getWidth())) {
                                    monster = gameManager.getMonsterIndex().get(i); //충돌된 놈 저장
                                    isCharacterCollision = true; //충돌
                                }
                            }
                        }
                    }

                    if (isCharacterCollision) {//몬스터와 충돌시
                        if (arrowAnimation.isRunning()) {
                            arrowAnimation.stop(); //애니메이션 종료
                        }
                        monster.attacked(character2.getDamage()); //몬스터 데미지 주고

                        isCharacterCollision = false; //충돌 끄고
                        arrow.setVisibility(View.GONE); //화살 시야 없애고
                        arrow = null; //화살 비워주고
                        attacking = false; //공격 끝났으니 지워주고
                    } else if (isEnemybaseCollision) {//적 진지와 격돌시
                        if (arrowAnimation.isRunning()) { //위와 같음
                            arrowAnimation.stop();
                        }
                        gameManager.getEnemybase().attacked(character2.getDamage());

                        if (gameManager.getEnemybase().getHp() < gameManager.getEnemybase().getMaxHP() * 2 / 5) {//40퍼센트 이하
                            if (!gameManager.isPhase2()) gameManager.phase2();
                        }
                        if (gameManager.getEnemybase().getHp() <= 0) { // 0이하가 되면
                            isEnemybaseCollision = false; //충돌 풀고
                        } else {//맵 게이지에 있는 적진지 체력바 줄이기
                            gameManager.getMapGaze2().getLayoutParams().width = (int) (gameManager.getMapGaze2Width() * gameManager.getEnemybase().getHp() / gameManager.getEnemybase().getMaxHP());
                        }
                        isEnemybaseCollision = false;
                        arrow.setVisibility(View.GONE);
                        arrow = null;
                        attacking = false;
                    }
                } else if (character2.getDamage() == 40) { //캐릭터가 스킬 공격 중이라면
                    if (gameManager == null) { //위와 같음
                        gameManager = character2.getGameManager();
                    }
                    if (arrow == null) {
                            //스킬의 ImageView생성
                        arrow = new ImageView(bulletContext);
                        arrowList.add(arrow);
                        mainLinear.addView(arrow, 11);
                        arrow.setBackgroundDrawable(arrowSkillAnimation);
                        arrow.setX(character2.getCharacter2().getX() + character2.getCharacter2().getWidth() / 2);
                        arrow.setY(character2.getCharacter2().getY() + character2.getCharacter2().getHeight() / 2); //이미지 자료가 그런식이라 이렇게 설정한다.
                    }
                    if (arrow != null) {
                        arrowSkillAnimation.start();
                        arrow.setX(arrow.getX() + speed);

                        if (enemybaseCollision((int) arrow.getX(), (int) enemybase.getX(), arrow.getWidth(), enemybase.getWidth())) {
                            isEnemybaseCollision = true;
                        } else {
                            isEnemybaseCollision = false;
                        }
                        if (gameManager.getMonsterIndex().size() > 0) {
                            for (int i = 0; i < gameManager.getMonsterIndex().size(); i++) {
                                if (characterCollision((int) arrow.getX(), (int) gameManager.getMonsterIndex().get(i).getMonster().getX(), arrow.getWidth(), gameManager.getMonsterIndex().get(i).getMonster().getWidth())) {
                                    monster = gameManager.getMonsterIndex().get(i);//겹쳐있더라도 때리던 놈 때리도록
                                    isCharacterCollision = true;
                                }
                            }
                        }
                    }

                    if (isCharacterCollision) {//몬스터와 격돌시
                        if (arrowSkillAnimation.isRunning()) { //애니메이션 변경코드
                            arrowSkillAnimation.stop();
                        }

                        if (attacking) monster.attacked(character2.getDamage());

                        isCharacterCollision = false;
                        arrow.setVisibility(View.GONE);
                        arrow = null;
                        attacking = false;
                    } else if (isEnemybaseCollision) {//적 진지와 격돌시
                        if (arrowSkillAnimation.isRunning()) { //애니메이션 변경코드
                            arrowSkillAnimation.stop();
                        }

                        gameManager.getEnemybase().attacked(character2.getDamage());

                        if (gameManager.getEnemybase().getHp() < gameManager.getEnemybase().getMaxHP() * 2 / 5) {//40퍼센트 이하
                            if (!gameManager.isPhase2()) gameManager.phase2();
                        }
                        if (gameManager.getEnemybase().getHp() <= 0) { // 0이하가 되면
                            gameManager.getEnemybase().setHp(0); //0으로 설정
                            isEnemybaseCollision = false; //충돌 풀고
                            /*if (!gameManager.isGameWin()) {
                                gameManager.setGameWin(true);
                                gameManager.getMainActivity().gameEndWin(); //게임 승리 선언
                            }*/

                        } else {//맵 게이지에 있는 적진지 체력바 줄이기
                            gameManager.getMapGaze2().getLayoutParams().width = (int) (gameManager.getMapGaze2Width() * gameManager.getEnemybase().getHp() / gameManager.getEnemybase().getMaxHP());
                        }
                        isEnemybaseCollision = false;
                        arrow.setVisibility(View.GONE);
                        arrow = null;
                        attacking = false;
                    }
                } else {//아무 일도 안하고 있다면
                    if (arrow != null) arrow.setVisibility(View.GONE);
                    arrow = null;
                    attacking = false;
                }//이걸 추가 안하면, 향하던 몬스터가 죽었을 때, 화살이 갈 곳을 잃고 그 자리에 멈춤
            }
        }
    };

    private boolean characterCollision(int x1, int x2, int width1, int width2){
        if((x2 - x1) < width1/2 + width2/2 - PxToDp(2275)) { //공격 방향은 하나
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

    public void setCharacter2(Character2 character2) {
        this.character2 = character2;
    }

    public Thread getThread() {
        return thread;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }
}
