package com.grendel.waves;

import java.io.IOException;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.MathUtils;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.widget.Toast;

public class playGame extends BaseExample /*implements IOnSceneTouchListener*/ {
	
	// ===========================================================
	// Constants
	// ===========================================================

		private static final int CAMERA_WIDTH = 480;
		private static final int CAMERA_HEIGHT = 320;
		private static final int DEMO_VELOCITY = 100;
		private static final int ENEMY_MAX = 100;
		private static final int BULLET_MAX = 5;
		private static final int STAR_MAX = 50;
		private static final int SHRAPNEL_MAX = 100;

	// ===========================================================
	// Fields
	// ===========================================================
	
		private Camera mCamera;
	
		// Bitmaps
		private BitmapTextureAtlas mShipBitmap;
		private BitmapTextureAtlas mEnemyOneBitmap, mEnemyTwoBitmap;
		private BitmapTextureAtlas mStarBitmap;
		private BitmapTextureAtlas mBulletBitmap;
		private BitmapTextureAtlas mShrapnelOneBitmap;
		private BitmapTextureAtlas mShrapnelTwoBitmap;
		private BitmapTextureAtlas mShrapnelThreeBitmap;
		
		// Textures
		private TextureRegion mShipTextureRegion;
		private TextureRegion mBulletTextureRegion;
		private TextureRegion mStarTextureRegion;
		private TextureRegion mEnemyOneTextureRegion, mEnemyTwoTextureRegion;
		public TextureRegion mShrapnelOneTextureRegion;
		public TextureRegion mShrapnelTwoTextureRegion;
		public TextureRegion mShrapnelThreeTextureRegion;
		
		// Sprites
		Sprite ship;
		Sprite[] star = new Sprite[STAR_MAX];
		Bullet[] bullets = new Bullet[BULLET_MAX];
		Enemy[] enemyOne = new Enemy[ENEMY_MAX];
		Enemy[] enemyTwo = new Enemy[ENEMY_MAX];
		Shrapnel[] shrapnelOne = new Shrapnel[SHRAPNEL_MAX];
		Shrapnel[] shrapnelTwo = new Shrapnel[SHRAPNEL_MAX];
		Shrapnel[] shrapnelThree = new Shrapnel[SHRAPNEL_MAX];
		
		// On-screen controls
		private BitmapTextureAtlas mOnScreenControlTexture;
		private TextureRegion mOnScreenControlBaseTextureRegion;
		private TextureRegion mOnScreenControlKnobTextureRegion;
		private boolean mPlaceOnScreenControlsAtDifferentVerticalLocations = false;
		
		// Sounds
		private Sound mExplosionSound;
		
		// Text
		private BitmapTextureAtlas mFontTexture;
		private BitmapTextureAtlas mFinalFontTexture;
		private Font mFont;
		private Font finalFont;
		
		// Timers
		public TimerHandler enemyOneSpawner;
		public TimerHandler enemyTwoSpawner;
		
		// Scene
		final Scene scene = new Scene();
		
		// Counters
		public float shipX, shipY;
		public static float analogRegisterX;
		public static float analogRegisterY;
		public static float angleRegisterX;
		public static float angleRegisterY;
		public int numberBullets = 0;
		public int numberEnemyOne = 0;
		public int numberEnemyTwo = 0;
		public int numberShrapnelOne = 0;
		public int numberShrapnelTwo = 0;
		public int numberShrapnelThree = 0;
		public float currentTime = 5.0f;
		public int score = 0;
		public int gameOver = 0;
		
		public int finalScoreIsShowing = 0;
		public float xs;
		public float ys;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

		@Override
		public Engine onLoadEngine() {
			this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
			final Engine engine = new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera).setNeedsSound(true));
	
			try {
				if(MultiTouch.isSupported(this)) {
					engine.setTouchController(new MultiTouchController());
					if(MultiTouch.isSupportedDistinct(this)) {
						Toast.makeText(this, "Go!", Toast.LENGTH_LONG).show();
					} else {
						this.mPlaceOnScreenControlsAtDifferentVerticalLocations = true;
						Toast.makeText(this, "MultiTouch detected, but your device has problems distinguishing between fingers.\n\nControls are placed at different vertical locations.", Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(this, "Sorry your device does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)\n\nControls are placed at different vertical locations.", Toast.LENGTH_LONG).show();
				}
			} catch (final MultiTouchException e) {
				Toast.makeText(this, "Sorry your Android Version does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)\n\nControls are placed at different vertical locations.", Toast.LENGTH_LONG).show();
			}
	
			return engine;
		}
	
		@Override
		public void onLoadResources() {
			BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
			SoundFactory.setAssetBasePath("mfx/");
	
			// Ship
			this.mShipBitmap = new BitmapTextureAtlas(32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			this.mShipTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mShipBitmap, this, "face_box.png", 0, 0);
			
			// EnemyOne
			this.mEnemyOneBitmap = new BitmapTextureAtlas(32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			this.mEnemyOneTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mEnemyOneBitmap, this, "bad_guy.png", 0, 0);
			
			// EnemyTwo
			this.mEnemyTwoBitmap = new BitmapTextureAtlas(32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			this.mEnemyTwoTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mEnemyTwoBitmap, this, "bad_guy2.png", 0, 0);
			
			// Star
			this.mStarBitmap = new BitmapTextureAtlas(16, 16, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			this.mStarTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mStarBitmap, this, "star.png", 0, 0);
			
			// ShrapnelOne
			this.mShrapnelOneBitmap = new BitmapTextureAtlas(8, 8, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			this.mShrapnelOneTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mShrapnelOneBitmap, this, "shrapnelOne.png", 0, 0);
			
			// ShrapnelTwo
			this.mShrapnelTwoBitmap = new BitmapTextureAtlas(8, 8, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			this.mShrapnelTwoTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mShrapnelTwoBitmap, this, "shrapnelTwo.png", 0, 0);
			
			// ShrapnelThree
			this.mShrapnelThreeBitmap = new BitmapTextureAtlas(8, 8, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			this.mShrapnelThreeTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mShrapnelThreeBitmap, this, "shrapnelThree.png", 0, 0);
			
			// Bullet
			this.mBulletBitmap = new BitmapTextureAtlas(32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			this.mBulletTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBulletBitmap, this, "bullet.png", 0, 0);
	
			// Controls
			this.mOnScreenControlTexture = new BitmapTextureAtlas(256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);
			this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);
			
			// Sound
			try {
				this.mExplosionSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "explosion.ogg");
			} catch (final IOException e) {
				Debug.e(e);
			}
			
			// Text
			this.mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			this.mFinalFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			this.mFont = new Font(this.mFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 18, true, Color.WHITE);
			this.finalFont = new Font(this.mFinalFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 24, true, Color.WHITE);
			this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
			this.mEngine.getTextureManager().loadTexture(this.mFinalFontTexture);
			this.mEngine.getFontManager().loadFont(this.mFont);
			this.mEngine.getFontManager().loadFont(this.finalFont);
	
			// Load
			this.mEngine.getTextureManager().loadTextures(this.mShipBitmap, this.mOnScreenControlTexture);
			this.mEngine.getTextureManager().loadTextures(this.mStarBitmap);
			this.mEngine.getTextureManager().loadTextures(this.mBulletBitmap);
			this.mEngine.getTextureManager().loadTextures(this.mEnemyOneBitmap, this.mEnemyTwoBitmap);
			this.mEngine.getTextureManager().loadTextures(this.mShrapnelOneBitmap, this.mShrapnelTwoBitmap, this.mShrapnelThreeBitmap);
		}
	
		@Override
		public Scene onLoadScene() {
			
			// ----------
			// INITIALIZE
			// ----------
			
				// content frame
				this.mEngine.registerUpdateHandler(new FPSLogger());
				scene.setBackground(new ColorBackground(0.0f, 0.0f, 0.0f));
				final int centerX = (CAMERA_WIDTH - this.mShipTextureRegion.getWidth()) / 2;
				final int centerY = (CAMERA_HEIGHT - this.mShipTextureRegion.getHeight()) / 2;
				
				// text
				final ChangeableText elapsedText = new ChangeableText(centerX - (centerX / 20), ( centerY * 2 ), this.mFont, "SCORE:", 100);
				final ChangeableText finalScore = new ChangeableText(centerX - 20, centerY, finalFont, "SCORE: " + score, 100);;
				scene.attachChild(elapsedText);
				
				// stars
				spawnStars( scene );

			// MAIN SHIP SPRITE
				ship = new Sprite(centerX, centerY, this.mShipTextureRegion);
				final PhysicsHandler shipPhysicsHandler = new PhysicsHandler(ship);
				ship.registerUpdateHandler(shipPhysicsHandler);
				scene.attachChild(ship);
				shipX = ship.getX();
				shipY = ship.getY();
			
			// SET TIMERS
				createTimers();

			// VELOCITY CONTROL (left)
				final int x1 = 0;
				final int y1 = CAMERA_HEIGHT - this.mOnScreenControlBaseTextureRegion.getHeight();
				final AnalogOnScreenControl velocityOnScreenControl = new AnalogOnScreenControl(x1, y1, this.mCamera, this.mOnScreenControlBaseTextureRegion, 
																								this.mOnScreenControlKnobTextureRegion, 0.1f, new IAnalogOnScreenControlListener() {
					@Override
					public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
						shipPhysicsHandler.setVelocity(pValueX * 175, pValueY * 175);
					}
		
					@Override
					public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl) {
						/* Nothing. */
					}
				});
				velocityOnScreenControl.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
				velocityOnScreenControl.getControlBase().setAlpha(0.5f);
		
				scene.setChildScene(velocityOnScreenControl);
		
		
			// ROTATION CONTROL (right)
				final int y2 = (this.mPlaceOnScreenControlsAtDifferentVerticalLocations) ? 0 : y1;
				final int x2 = CAMERA_WIDTH - this.mOnScreenControlBaseTextureRegion.getWidth();
				final AnalogOnScreenControl rotationOnScreenControl = new AnalogOnScreenControl(x2, y2, this.mCamera, this.mOnScreenControlBaseTextureRegion, 
																								this.mOnScreenControlKnobTextureRegion, 0.1f, new IAnalogOnScreenControlListener() {
					@Override
					public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
						if(pValueX == x1 && pValueY == x1) {
							ship.setRotation(x1);
						} else {
							ship.setRotation(MathUtils.radToDeg((float)Math.atan2(pValueX, -pValueY)));
							analogRegisterX = (float) MathUtils.radToDeg((float)Math.atan(pValueX) * 4);
							analogRegisterY = (float) MathUtils.radToDeg((float)Math.atan(pValueY) * 4);
							angleRegisterX = pValueX;
							angleRegisterY = -pValueY;
						}
					}
		
					@Override
					public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl) {
						/* Nothing. */
					}
				});
				rotationOnScreenControl.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
				rotationOnScreenControl.getControlBase().setAlpha(0.5f);
		
				velocityOnScreenControl.setChildScene(rotationOnScreenControl);
				
			// ----------
			// STEPS AND COLLISION CHECKING
			// ----------
				
				scene.registerUpdateHandler(new IUpdateHandler() {
					@Override
					public void reset() { }
	
					@Override
					public void onUpdate(final float pSecondsElapsed) {
						
						// BULLETS
						// ----------
							for ( int bulletIndex = 0; bulletIndex < numberBullets; bulletIndex++ ){
								
								// BULLET -><- ENEMYONE
									for ( int eOneIndex = 0; eOneIndex < numberEnemyOne; eOneIndex++ ){
										if( bullets[ bulletIndex ].collidesWith(enemyOne[ eOneIndex ]) ) {
											
											explosionOne( enemyOne[eOneIndex], scene );
											score += 5;
											
											scene.detachChild( enemyOne[eOneIndex] );
											scene.unregisterUpdateHandler( bullets[bulletIndex].mPhysicsHandler );
											scene.detachChild( bullets[bulletIndex] );
											
											for ( int eOneAnchor = eOneIndex; eOneAnchor < (ENEMY_MAX - 1); eOneAnchor++ ){
												enemyOne[eOneAnchor] = enemyOne[eOneAnchor + 1];
											}
											for ( int bulletAnchor = bulletIndex; bulletAnchor < (BULLET_MAX - 1); bulletAnchor++ ){ //error here
												bullets[bulletAnchor] = bullets[bulletAnchor + 1];
											}
											
											numberBullets--;
											numberEnemyOne--;
										}
									}
									
								// BULLET -><- ENEMYTWO
									for ( int eTwoIndex = 0; eTwoIndex < numberEnemyTwo; eTwoIndex++ ){
										if( bullets[bulletIndex].collidesWith(enemyTwo[eTwoIndex]) ) {
											
											explosionTwo( enemyTwo[eTwoIndex], scene );
											score += 10;
											
											scene.detachChild( enemyTwo[eTwoIndex] );
											scene.unregisterUpdateHandler( bullets[bulletIndex].mPhysicsHandler );
											scene.detachChild( bullets[bulletIndex] );
											
											for ( int x = eTwoIndex; x < (ENEMY_MAX - 1); x++ ){
												enemyTwo[x] = enemyTwo[x + 1];
											}
											for ( int y = bulletIndex; y < (BULLET_MAX - 1); y++ ){
												bullets[y] = bullets[y + 1];
											}
											
											numberBullets--;
											numberEnemyTwo--;
										}
									}
								
								// BOUNDS AND GAME OVER CHECKING
									if ( bullets[bulletIndex].destroyMe == 1 || gameOver == 1 ){
										scene.unregisterUpdateHandler( bullets[bulletIndex].mPhysicsHandler );
										scene.detachChild( bullets[bulletIndex] );
										for ( int bulletAnchor = bulletIndex; bulletAnchor < (BULLET_MAX - 1); bulletAnchor++ ){ // here too
											bullets[bulletAnchor] = bullets[bulletAnchor + 1];
										}
										numberBullets--;
									}
								
							}
						
						// ENEMYONE
						// ----------
							for ( int eOneIndex = 0; eOneIndex < numberEnemyOne; eOneIndex++ ){
								
								// ENEMYONE -><- SHIP
								if( enemyOne[eOneIndex].collidesWith(ship) && enemyOne[eOneIndex].gracePeriod != 1 ) {
									/*scene.detachChild( enemyOne[eOneIndex] );
									
									for ( int eOneAnchor = eOneIndex; eOneAnchor < (ENEMY_MAX - 1); eOneAnchor++ ){
										enemyOne[eOneAnchor] = enemyOne[eOneAnchor + 1];
									}
									
									numberEnemyOne--;*/
									
									/* DESTROY SHIP! */
									xs = ship.getX();
									ys = ship.getY();
									
									
									scene.unregisterUpdateHandler(shipPhysicsHandler);
									scene.detachChild(ship);
									//Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.antialpha.com/?score=" + score));
									//startActivity(browserIntent);
									
									gameOver = 1;
									finalScore.setText("SCORE: " + score);
									if ( finalScoreIsShowing == 0 ){
										explosionThree( xs, ys, scene );
										scene.attachChild(finalScore);
										finalScoreIsShowing = 1;
									}
									
								}
							}
							
						// ENEMYTWO
						// ----------
							for ( int i = 0; i < numberEnemyTwo; i++ ){
								
								// UPDATE MOVEMENT
								shipX = ship.getX();
								shipY = ship.getY();
								if ( shipX > enemyTwo[i].getX() && shipY > enemyTwo[i].getY() )
									enemyTwo[i].mPhysicsHandler.setVelocity(shipX/5, shipY/5);
								else if ( shipX < enemyTwo[i].getX() && shipY > enemyTwo[i].getY() )
									enemyTwo[i].mPhysicsHandler.setVelocity(-shipX/5, shipY/5);
								else if ( shipX > enemyTwo[i].getX() && shipY < enemyTwo[i].getY() )
									enemyTwo[i].mPhysicsHandler.setVelocity(shipX/5, -shipY/5);
								else 
									enemyTwo[i].mPhysicsHandler.setVelocity(-shipX/5, -shipY/5);
								
								// ENEMYTWO -><- SHIP
								if( enemyTwo[i].collidesWith(ship) && enemyTwo[i].gracePeriod != 1 ) {
									/*scene.detachChild(enemyTwo[i]);
									
									for ( int x = i; x < (ENEMY_MAX - 1); x++ ){
										enemyTwo[x] = enemyTwo[x + 1];
									}
									
									numberEnemyTwo--;*/
									
									/* DESTROY SHIP! */
									xs = ship.getX();
									ys = ship.getY();
									
									scene.unregisterUpdateHandler(shipPhysicsHandler);
									scene.detachChild(ship);
									//Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.antialpha.com/highscores.php/?score=" + score));
									//startActivity(browserIntent);
									
									gameOver = 1;
									finalScore.setText("SCORE: " + score);
									if ( finalScoreIsShowing == 0 ){
										explosionThree( xs, ys, scene );
										scene.attachChild(finalScore);
										finalScoreIsShowing = 1;
									}
								}
								
							}
							
						// SHRAPNEL
						// ----------
							for ( int sIndex = 0; sIndex < numberShrapnelOne; sIndex++ ){
								
								// TIME TO DEATH
								if ( shrapnelOne[sIndex].destroyMe == 1 ){
									scene.unregisterUpdateHandler( shrapnelOne[sIndex].mPhysicsHandler );
									scene.detachChild( shrapnelOne[sIndex] );
									
									for ( int sAnchor = sIndex; sAnchor < (SHRAPNEL_MAX - 1); sAnchor++ ){
										shrapnelOne[sAnchor] = shrapnelOne[sAnchor + 1];
									}
									
									numberShrapnelOne--;
								}
							}
							
							for ( int sIndex = 0; sIndex < numberShrapnelTwo; sIndex++ ){
								
								// TIME TO DEATH
								if ( shrapnelTwo[sIndex].destroyMe == 1 ){
									scene.unregisterUpdateHandler( shrapnelTwo[sIndex].mPhysicsHandler );
									scene.detachChild( shrapnelTwo[sIndex] );
									
									for ( int sAnchor = sIndex; sAnchor < (SHRAPNEL_MAX - 1); sAnchor++ ){
										shrapnelTwo[sAnchor] = shrapnelTwo[sAnchor + 1];
									}
									
									numberShrapnelTwo--;
								}
							}
							
						// SCORE
						// ----------
							if ( gameOver != 1 )
								elapsedText.setText("SCORE: " + score);
							else
								scene.detachChild(elapsedText);
					}
				});
		
			return scene;
		}
	
		@Override
		public void onLoadComplete() {
	
		}

	// ===========================================================
	// Methods
	// ===========================================================
		
		/**
		 * Create an Enemy at a specified location
		 * @param pX is the X Position of your Sprite
		 * @param pY is the Y Position of your Sprite
		 */
		
		private void explosionOne( Enemy enemyOne , Scene scene ){
			
			if ( numberShrapnelOne + 20 < SHRAPNEL_MAX ){
				for ( int sIndex = numberShrapnelOne; sIndex < (numberShrapnelOne + 20); sIndex++ ){
					shrapnelOne[sIndex] = new Shrapnel( enemyOne.getX() + 3 , enemyOne.getY() + 3 , this.mShrapnelOneTextureRegion );
					scene.attachChild( shrapnelOne[sIndex] );
				}
				numberShrapnelOne += 20;
			}
			this.mExplosionSound.play();
			
		}
		
		private void explosionTwo( Enemy enemyTwo, Scene scene ){
			
			if ( numberShrapnelTwo + 20 < SHRAPNEL_MAX ){
				for ( int sIndex = numberShrapnelTwo; sIndex < (numberShrapnelTwo + 20); sIndex++ ){
					shrapnelTwo[sIndex] = new Shrapnel( enemyTwo.getX() + 3 , enemyTwo.getY() + 3 , this.mShrapnelTwoTextureRegion );
					scene.attachChild( shrapnelTwo[sIndex] );
				}
				numberShrapnelTwo += 20;
			}
			this.mExplosionSound.play();
			
		}
		
		private void explosionThree( float xos , float yos , Scene scene ){
			
			if ( numberShrapnelThree + 50 < SHRAPNEL_MAX ){
				for ( int sIndex = numberShrapnelTwo; sIndex < (numberShrapnelTwo + 50); sIndex++ ){
					shrapnelThree[sIndex] = new Shrapnel( xos + 3 , yos + 3 , this.mShrapnelThreeTextureRegion );
					scene.attachChild( shrapnelThree[sIndex] );
				}
				numberShrapnelThree += 50;
			}
			this.mExplosionSound.play();
			
		}
		
		
		
		private void createEnemyOneSprite(float pX, float pY, Scene scene) {
			
			if ( numberEnemyOne < ENEMY_MAX && gameOver != 1 ){
				enemyOne[ numberEnemyOne ] = new Enemy( pX, pY, this.mEnemyOneTextureRegion );
				scene.attachChild( enemyOne[ numberEnemyOne ] );
				numberEnemyOne++;
			}
			
		}
		
		private void createEnemyTwoSprite(float pX, float pY, Scene scene) {
			if ( numberEnemyTwo < ENEMY_MAX && gameOver != 1 ){
				enemyTwo[numberEnemyTwo] = new Enemy(pX, pY, mEnemyTwoTextureRegion);
				scene.attachChild(enemyTwo[numberEnemyTwo]);
				numberEnemyTwo++;
			}
		}

		private void createBulletSprite(float pX, float pY, Scene scene) {
			
			if ( numberBullets < BULLET_MAX && gameOver != 1 ){
				bullets[ numberBullets ] = new Bullet( pX + 6, pY + 6, this.mBulletTextureRegion );
				scene.attachChild( bullets[ numberBullets ] );
				numberBullets++;
			}
			
		}
		
		private void spawnStars(Scene pscene) {
			
			for ( int starIndex = 0; starIndex < STAR_MAX; starIndex++ ){
				star[ starIndex ] = new Sprite( MathUtils.random(30.0f, (CAMERA_WIDTH - 30.0f)), MathUtils.random(30.0f, (CAMERA_WIDTH - 30.0f)), this.mStarTextureRegion);
				pscene.attachChild( star[starIndex] );
			}
			
		}
		
		/*@Override
		public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
			* Removing entities can only be done safely on the UpdateThread.
			 * Doing it while updating/drawing can
			 * cause an exception with a suddenly missing entity.
			 * Alternatively, there is a possibility to run the TouchEvents on the UpdateThread by default, by doing:
			 * engineOptions.getTouchOptions().setRunOnUpdateThread(true);
			 * when creating the Engine in onLoadEngine();
			this.runOnUpdateThread(new Runnable() {
				@Override
				public void run() {

				}
			});
			return false;
		}*/
		
		
		/**
		 * Creates a Timer Handler used to Spawn Sprites
		 */
		private void createTimers() {
			
			// BULLETS
				final TimerHandler bulletSpawner;
				
				this.getEngine().registerUpdateHandler(bulletSpawner = new TimerHandler(0.3f, true, new ITimerCallback()
				{
				    @Override
				    public void onTimePassed(final TimerHandler pTimerHandler)
				    {  	
						shipX = ship.getX();
						shipY = ship.getY();
						createBulletSprite(shipX, shipY, scene);
				    }
				}));
			
			// ENEMYONE
				this.getEngine().registerUpdateHandler(enemyOneSpawner = new TimerHandler( currentTime, true, new ITimerCallback()
				{			
				    @Override
				    public void onTimePassed(final TimerHandler pTimerHandler)
				    {  	
				    	final float x = MathUtils.random(30.0f, (CAMERA_WIDTH - 30.0f));
						final float y = MathUtils.random(30.0f, (CAMERA_HEIGHT - 30.0f));
						createEnemyOneSprite(x, y, scene);
				    }
				}));
			
			
			// ENEMYTWO
				this.getEngine().registerUpdateHandler(enemyTwoSpawner = new TimerHandler(3.0f, true, new ITimerCallback()
				{
				    @Override
				    public void onTimePassed(final TimerHandler pTimerHandler)
				    {  	
				    	final float xos = MathUtils.random(30.0f, (CAMERA_WIDTH - 30.0f));
						final float yos = MathUtils.random(30.0f, (CAMERA_HEIGHT - 30.0f));
						createEnemyTwoSprite(xos, yos, scene);
				    }
				}));
			
			// INCREASE DIFFICULTY
				final TimerHandler advance;
				
				this.getEngine().registerUpdateHandler(advance = new TimerHandler(10, true, new ITimerCallback()
				{			
				    @Override
				    public void onTimePassed(final TimerHandler pTimerHandler)
				    {  	
				    	currentTime = ( currentTime * 2 ) / 3;
				    	scene.unregisterUpdateHandler(enemyOneSpawner);
				    	scene.registerUpdateHandler(enemyOneSpawner = new TimerHandler( currentTime, true, new ITimerCallback()
						{
						    @Override
						    public void onTimePassed(final TimerHandler pTimerHandler)
						    {  	
						    	final float x = MathUtils.random(30.0f, (CAMERA_WIDTH - 30.0f));
								final float y = MathUtils.random(30.0f, (CAMERA_HEIGHT - 30.0f));
								createEnemyOneSprite(x, y, scene);
						    }
						}));
				    }
				}));
			
		}
	
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
		private static class Enemy extends Sprite {
			
			private final PhysicsHandler mPhysicsHandler;
			int gracePeriod = 1;
			float previous = 0;
	
			public Enemy(final float pX, final float pY, final TextureRegion pTextureRegion) {
				super(pX, pY, pTextureRegion);
				Random rand = new Random();
				this.mPhysicsHandler = new PhysicsHandler(this);
				this.registerUpdateHandler(this.mPhysicsHandler);
				this.mPhysicsHandler.setVelocity(rand.nextInt(50), rand.nextInt(50));
				
				final TimerHandler grace;
				registerUpdateHandler(grace = new TimerHandler(2.0f, false, new ITimerCallback()
				{
				    @Override
				    public void onTimePassed(final TimerHandler pTimerHandler)
				    {  	
				    	gracePeriod = 0;
				    }
				}));
			}
	
			@Override
			protected void onManagedUpdate(final float pSecondsElapsed) {
				
				if(this.mX < 0) {
					this.mPhysicsHandler.setVelocityX(DEMO_VELOCITY);
				} else if(this.mX + this.getWidth() > CAMERA_WIDTH) {
					this.mPhysicsHandler.setVelocityX(-DEMO_VELOCITY);
				}
	
				if(this.mY < 0) {
					this.mPhysicsHandler.setVelocityY(DEMO_VELOCITY);
				} else if(this.mY + this.getHeight() > CAMERA_HEIGHT) {
					this.mPhysicsHandler.setVelocityY(-DEMO_VELOCITY);
				}
				
				this.setRotation(previous);
				previous++;
				if ( previous > 360 ){
					previous = 0;
				}
	
				super.onManagedUpdate(pSecondsElapsed);
			}
		}
		
		private static class Bullet extends Sprite {
			private final PhysicsHandler mPhysicsHandler;
			
			int destroyMe = 0;
	
			public Bullet(final float pX, final float pY, final TextureRegion pTextureRegion) {
				super(pX, pY, pTextureRegion);
				this.mPhysicsHandler = new PhysicsHandler(this);
				this.registerUpdateHandler(this.mPhysicsHandler);
				if ( analogRegisterX  == 0 || analogRegisterY == 0 ) {
					this.mPhysicsHandler.setVelocity( 0, -50 );
				}
				else {
					this.mPhysicsHandler.setVelocity( analogRegisterX, analogRegisterY );
					this.setRotation(MathUtils.radToDeg((float)Math.atan2(angleRegisterX, angleRegisterY)));
				}
				
				destroyMe = 0;
			}
	
			@Override
			protected void onManagedUpdate(final float pSecondsElapsed) {
				
				if(this.mX < 0) {
					destroyMe = 1;
				} else if(this.mX + this.getWidth() > CAMERA_WIDTH) {
					destroyMe = 1;
				}
	
				if(this.mY < 0) {
					destroyMe = 1;
				} else if(this.mY + this.getHeight() > CAMERA_HEIGHT) {
					destroyMe = 1;
				}
	
				super.onManagedUpdate(pSecondsElapsed);
			}
		}
		
		private static class Shrapnel extends Sprite {
			private final PhysicsHandler mPhysicsHandler;
			
			int deathTime = 50;
			int currentTime = 0;
			int destroyMe = 0;
			
			public Shrapnel(final float pX, final float pY, final TextureRegion pTextureRegion) {
				super(pX, pY, pTextureRegion);
				Random rand = new Random();
				this.mPhysicsHandler = new PhysicsHandler(this);
				this.registerUpdateHandler(this.mPhysicsHandler);
				int blarg = rand.nextInt(16);
				
				// direction
				if ( blarg % 4 == 0 )
					this.mPhysicsHandler.setVelocity(rand.nextInt(50), rand.nextInt(50));
				else if ( blarg % 4 == 1 )
					this.mPhysicsHandler.setVelocity(-rand.nextInt(50), -rand.nextInt(50));
				else if ( blarg % 4 == 2 )
					this.mPhysicsHandler.setVelocity(-rand.nextInt(50), rand.nextInt(50));
				else
					this.mPhysicsHandler.setVelocity(rand.nextInt(50), -rand.nextInt(50));
			}
	
			@Override
			protected void onManagedUpdate(final float pSecondsElapsed) {
			
				if ( currentTime < deathTime ){
					currentTime++;
				}
				else {
					destroyMe = 1;
				}
				
				super.onManagedUpdate(pSecondsElapsed);
			}
		}
	
}
