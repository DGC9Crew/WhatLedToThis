/*

hello curious person looking at my code

all i can tell you is that it is not the best

i dont use the best practices, and its overall a mess

if something doesnt make sense, you aren't imaginating things. a lot of stuff has no logic behind it

but if you still want to go look at it, go right ahead

also, happy code day


 */
package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.FadeFilter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.system.AppSettings;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.ui.Picture;
import com.jme3.util.SkyFactory;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class Main extends SimpleApplication implements ActionListener, PhysicsCollisionListener, AnimEventListener {

    private BulletAppState bulletAppState;
    Node levels_phys;
    Node levels_death;
    Node levels_finish;
    Node levels_cannon;
    Node levels_non;
    FadeFilter fade;
    AudioNode levelComplete;
    AudioNode nuclearbomb;
    boolean isPlayedFinish = false;
    CharacterControl character;
    Node model;
    AnimChannel animationChannel;
    AnimChannel shootingChannel;
    AnimControl animationControl;
    RigidBodyControl currentPhysicsthing;
    boolean onGround;
    Node cannons;
    Picture transPhoto;
    float shootTimer = 0;
    float transPhotoTimer = 0;
    Vector3f walkDirection = new Vector3f(0, 0, 0);

    float airTime;
    boolean left, right;

    Node music;

    int currentLevel = 1;
    int currentLevelIncreasing = 1;

    int levelTransitionStage = 0;

    boolean ableToMove = true;

    public static void main(String[] args) throws IOException {
        AppSettings settings = new AppSettings(true);
        BufferedImage[] icons = new BufferedImage[]{
            ImageIO.read(Main.class.getResource("256x.jpg")),
            ImageIO.read(Main.class.getResource("128x.jpg")),
            ImageIO.read(Main.class.getResource("32x.jpg")),
            ImageIO.read(Main.class.getResource("16x.jpg"))
        };
        settings.setIcons(icons);
        settings.setResolution(780, 550);
        settings.setTitle("\"What led to this?\"");
        Main app = new Main();
        app.setSettings(settings);
        app.setShowSettings(false);
        app.setPauseOnLostFocus(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {

        ScreenshotAppState screenShotState = new ScreenshotAppState();
        this.stateManager.attach(screenShotState);
        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);
        // bulletAppState.setDebugEnabled(true);
        setupKeys();

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);

        fade = new FadeFilter(3);

        fpp.addFilter(fade);

        viewPort.addProcessor(fpp);
        cannons = new Node("Cannnons");
        rootNode.attachChild(cannons);

        fade.setDuration(1);

        transPhoto = new Picture("Trans");
        transPhoto.setImage(assetManager, "Textures/level1done.png", true);
        transPhoto.setWidth(settings.getWidth());
        transPhoto.setHeight(settings.getHeight());
        transPhoto.setLocalTranslation(0, 0, -5);

        viewPort.setBackgroundColor(ColorRGBA.LightGray);

        music = new Node("music");
        rootNode.attachChild(music);
        AudioNode level1track = new AudioNode(assetManager, "Sounds/bunker.wav", DataType.Stream);
        level1track.setLooping(true);
        level1track.setPositional(false);
        level1track.setVolume(2);
        music.attachChild(level1track);
        AudioNode level2track = new AudioNode(assetManager, "Sounds/brokencities.wav", DataType.Stream);
        level2track.setLooping(true);
        level2track.setPositional(false);
        level2track.setVolume(2);
        music.attachChild(level2track);
        AudioNode level3track = new AudioNode(assetManager, "Sounds/unknown.wav", DataType.Stream);
        level3track.setLooping(true);
        level3track.setPositional(false);
        level3track.setVolume(2);
        music.attachChild(level3track);
        AudioNode level4track = new AudioNode(assetManager, "Sounds/nightmare.wav", DataType.Stream);
        level4track.setLooping(true);
        level4track.setPositional(false);
        level4track.setVolume(1);
        music.attachChild(level4track);
        AudioNode level5track = new AudioNode(assetManager, "Sounds/refugee.wav", DataType.Stream);
        level5track.setLooping(true);
        level5track.setPositional(false);
        level5track.setVolume(2);
        music.attachChild(level5track);

        AudioNode credits = new AudioNode(assetManager, "Sounds/creds.wav", DataType.Stream);
        credits.setLooping(true);
        credits.setPositional(false);
        credits.setVolume(2);
        music.attachChild(credits);

        levelComplete = new AudioNode(assetManager, "Sounds/lvlcomp.wav", DataType.Buffer);
        levelComplete.setLooping(false);
        levelComplete.setPositional(false);
        levelComplete.setVolume(1f);

        nuclearbomb = new AudioNode(assetManager, "Sounds/nuke.wav", DataType.Buffer);
        nuclearbomb.setLooping(false);
        nuclearbomb.setPositional(false);
        nuclearbomb.setVolume(1);

        createTerrain();
        createCharacter();
        setupChaseCamera();
        setupAnimationController();

        setDisplayFps(false);
        setDisplayStatView(false);

        //fade.fadeOut();
        bulletAppState.getPhysicsSpace().addCollisionListener(this);

        //levelTransitionStage = 1;
    }

    private PhysicsSpace getPhysicsSpace() {
        return bulletAppState.getPhysicsSpace();
    }

    private void setupKeys() {
        inputManager.addMapping("wireframe", new KeyTrigger(KeyInput.KEY_T));
        inputManager.addListener(this, "wireframe");
        inputManager.addMapping("CharLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("CharRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("CharUp", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("CharDown", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("CharSpace", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_R));

        inputManager.addListener(this, "Reset");
        inputManager.addListener(this, "CharLeft");

        inputManager.addListener(this, "CharRight");
        inputManager.addListener(this, "CharUp");
        inputManager.addListener(this, "CharDown");
        inputManager.addListener(this, "CharSpace");

    }

    private void createTerrain() {
        levels_phys = new Node("PhysLevels");
        levels_death = new Node("DeathLevels");
        levels_finish = new Node("DeathLevels");
        levels_cannon = new Node("DeathLevels");
        levels_non = new Node("Noncol");
        rootNode.attachChild(levels_phys);
        rootNode.attachChild(levels_death);
        rootNode.attachChild(levels_finish);
        rootNode.attachChild(levels_cannon);
        rootNode.attachChild(levels_non);
        //init lv1

        //Node level1 = (Node) assetManager.loadModel("Models/level2_clonetest.j3o");
        Node level1 = (Node) assetManager.loadModel("Models/level1_test1.j3o");
        RigidBodyControl level1Phy = new RigidBodyControl(CollisionShapeFactory.createMeshShape(level1.getChild("Collidables")), 0);
        level1.addControl(level1Phy);
        levels_phys.attachChild(level1);
        bulletAppState.getPhysicsSpace().add(level1Phy);

        currentPhysicsthing = level1Phy;

        levels_non.attachChild(level1.getChild("NonCol"));

        Node currentDeath = (Node) levels_phys.getChild(0);
        Node level_death = (Node) currentDeath.getChild("Death");

        for (int i = 0; i < level_death.getQuantity(); i++) {
            CollisionShape bull
                    = CollisionShapeFactory.createMeshShape(level_death.getChild(i));
            GhostControl col = new GhostControl(bull);

            col.setPhysicsLocation(level_death.getChild(i).getLocalTranslation());
            level_death.getChild(i).addControl(col);

            bulletAppState.getPhysicsSpace().add(col);

        }

        levels_death.attachChild(level_death);
        Node currentFinish = (Node) levels_phys.getChild(0);
        Node level_finish = (Node) currentFinish.getChild("Finish");

        for (int i = 0; i < level_finish.getQuantity(); i++) {
            CollisionShape bull
                    = CollisionShapeFactory.createMeshShape(level_finish.getChild(i));
            GhostControl col = new GhostControl(bull);

            col.setPhysicsLocation(level_finish.getChild(i).getLocalTranslation());
            level_finish.getChild(i).addControl(col);

            bulletAppState.getPhysicsSpace().add(col);

        }
        levels_finish.attachChild(level_finish);

        Node currentCannon = (Node) levels_phys.getChild(0);
        levels_cannon = (Node) currentCannon.getChild("Cannons");

        AudioNode song = (AudioNode) music.getChild(currentLevelIncreasing - 1);
        song.play();

        rootNode.attachChild(levels_cannon);

    }

    private void createCharacter() {
        CapsuleCollisionShape capsule = new CapsuleCollisionShape(.4f, 1.25f);
        character = new CharacterControl(capsule, 0.01f);
        character.setGravity(new Vector3f(0, -60, 0));

        model = (Node) assetManager.loadModel("Models/player.j3o");
        model.setLocalScale(0.3f);
        AmbientLight aa = new AmbientLight();
        aa.setColor(ColorRGBA.White.mult(1));

        model.addLight(aa);

        model.addControl(character);
        character.setPhysicsLocation(new Vector3f(0, 0, 0));
        rootNode.attachChild(model);
        getPhysicsSpace().add(character);
    }

    private void setupChaseCamera() {
        flyCam.setEnabled(false);

        //flyCam.setMoveSpeed(50);
        cam.setLocation(new Vector3f(character.getPhysicsLocation().x, character.getPhysicsLocation().y, 10));

    }

    private void setupAnimationController() {
        animationControl = model.getChild("Cube.001").getControl(AnimControl.class);
        animationControl.addListener(this);
        animationChannel = animationControl.createChannel();

    }

    public void onGroundUpdate() {

        try {
            CollisionResults results = new CollisionResults();

            Ray ray = new Ray(model.getLocalTranslation(), new Vector3f(0, -1, 0));
            Node currentPlat = (Node) levels_phys.getChild(currentLevel - 1);
            currentPlat.collideWith(ray, results);

            //println("----- Collisions? " + results.size() + "-----");
            if (results.size() > 0) {
                CollisionResult closest = results.getClosestCollision();
                if (closest.getDistance() < 1.4f) {
                    onGround = true;
                } else {
                    onGround = false;
                }
                //System.out.println(onGround);
                //System.out.println(closest.getDistance());
            } else {
                //System.out.println("NOTHING");
                onGround = false;

            }
        } catch (Exception e) {

        }

    }

    @Override
    public void simpleUpdate(float tpf) {

        onGroundUpdate();

        //System.out.println(fade.getValue());
        if (levelTransitionStage == 1 && !isPlayedFinish) {
            if (currentLevelIncreasing == 4) {
                nuclearbomb.playInstance();
            } else {
                levelComplete.playInstance();
            }
            isPlayedFinish = true;
        }
        if (levelTransitionStage == 2) {
            transPhotoTimer += tpf;
            if (transPhotoTimer > 8) {
                transPhotoTimer = 0;
                levelTransitionStage = 3;
                fade.fadeOut();
            }
        }

        if (fade.getValue() == 0) {
            switch (levelTransitionStage) {
                case 1:
                    bulletAppState.getPhysicsSpace().remove(levels_phys.getChild(currentLevel - 1));
                    levels_phys.detachChildAt(currentLevel - 1);
                    levels_non.detachAllChildren();
                    Node oldDeath = (Node) levels_death.getChild(currentLevel - 1);
                    Node oldFinsih = (Node) levels_finish.getChild(currentLevel - 1);

                    for (int f = 0; f < oldDeath.getQuantity(); f++) {
                        oldDeath.getChild(f).getControl(GhostControl.class).setEnabled(false);
                    }
                    for (int f = 0; f < oldFinsih.getQuantity(); f++) {
                        oldFinsih.getChild(f).getControl(GhostControl.class).setEnabled(false);
                    }
                    levels_cannon.detachAllChildren();
                    for (int f = 0; f < cannons.getQuantity(); f++) {
                        cannons.getChild(f).getControl(GhostControl.class).setEnabled(false);
                    }
                    cannons.detachAllChildren();
                    levels_death.detachChildAt(currentLevel - 1);
                    levels_finish.detachChildAt(currentLevel - 1);
                    shootTimer = 0;

                    fade.fadeIn();
                    switch (currentLevelIncreasing) {
                        case 1:
                            transPhoto.setImage(assetManager, "Textures/level1done.png", true);
                            break;
                        case 2:
                            transPhoto.setImage(assetManager, "Textures/level2done.png", true);
                            break;
                        case 3:
                            transPhoto.setImage(assetManager, "Textures/level3done.png", true);
                            break;
                        case 4:
                            transPhoto.setImage(assetManager, "Textures/level4done.png", true);
                            break;
                        case 5:
                            transPhoto.setImage(assetManager, "Textures/level5done.png", true);
                            break;
                        default:
                            transPhoto.setImage(assetManager, "Textures/nothing.png", true);
                            this.stop();
                    }
                    guiNode.attachChild(transPhoto);
                    levelTransitionStage = 2;
                    break;
                case 3:
                    moveToNextLevel();
                    guiNode.detachChild(transPhoto);
                    fade.fadeIn();
                    levelTransitionStage = 0;
                    break;

            }
        }

        if (levelTransitionStage == 0) {
            shootTimer += tpf;
            if (shootTimer > 2) {
                //System.out.println(levels_cannon.getChildren());
                try {
                    for (int i = 0; i < levels_cannon.getQuantity(); i++) {

                        Box b = new Box(.75f, .75f, .75f);
                        Geometry geom = new Geometry("Box", b);

                        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                        mat.setColor("Color", ColorRGBA.Cyan);
                        geom.setMaterial(mat);

                        CollisionShape bull
                                = CollisionShapeFactory.createMeshShape(geom);
                        GhostControl col = new GhostControl(bull);
                        geom.setLocalTranslation(levels_cannon.getChild(i).getLocalTranslation());
                        col.setPhysicsLocation(levels_cannon.getChild(i).getLocalTranslation());
                        geom.addControl(col);
                        cannons.attachChild(geom);

                        Vector3f a = new Vector3f(0, 1, 0);

                        geom.addControl(new ShootControl(levels_cannon.getChild(i).getLocalRotation().mult(a)));
                        geom.setLocalRotation(levels_cannon.getChild(i).getLocalRotation());
                        bulletAppState.getPhysicsSpace().add(col);

                    }
                    shootTimer = 0;
                } catch (Exception e) {
                    System.out.println("NO CANNONS");
                }
            }
        }

        Vector3f camDir = new Vector3f(cam.getDirection().getX(), 0, cam.getDirection().getZ()).normalizeLocal().multLocal(.15f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.15f);
        camDir.y = 0;
        camLeft.y = 0;
        walkDirection.set(0, 0, 0);
        if (left) {

            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }

        if (!character.onGround()) {
            airTime = airTime + tpf;

        } else {
            airTime = 0;

        }
        if (walkDirection.length() == 0) {
            if (!"stand".equals(animationChannel.getAnimationName())) {
                animationChannel.setAnim("stand");
 
            }
        } else {
            character.setViewDirection(walkDirection);
            if (!"run".equals(animationChannel.getAnimationName())) {

                animationChannel.setAnim("run", 1f);
            }
        }

        character.setWalkDirection(walkDirection);

        cam.setLocation(new Vector3f(character.getPhysicsLocation().x, character.getPhysicsLocation().y, 20));
        //System.out.println(character.getPhysicsLocation());
    }

    public void onAction(String binding, boolean value, float tpf) {
        if (ableToMove) {
            if (binding.equals("CharLeft")) {
                if (value) {
                    left = true;
                } else {
                    left = false;
                }
            } else if (binding.equals("CharRight")) {
                if (value) {
                    right = true;
                } else {
                    right = false;
                }

            } else if (binding.equals("CharSpace") && value) {
                if (character.onGround() && onGround) {
                    character.jump(new Vector3f(0, 25, 0));

                } else {

                }

            }
        }

    }

    public void collision(PhysicsCollisionEvent pce) {
        PhysicsCollisionObject a = pce.getObjectA();
        PhysicsCollisionObject b = pce.getObjectB();

        //System.out.println(levels_death.getChildren());
        Node currentDeath = (Node) levels_death.getChild(currentLevel - 1);

        for (int i = 0; i < currentDeath.getQuantity(); i++) {

            if (a == currentDeath.getChild(i).getControl(GhostControl.class) && b == character || a == character && b == currentDeath.getChild(i).getControl(GhostControl.class)) {
                character.setPhysicsLocation(new Vector3f(0, 0, 0));

            }
            Node currentFinish = (Node) levels_finish.getChild(currentLevel - 1);
            for (int f = 0; f < currentFinish.getQuantity(); f++) {

                if (a == currentFinish.getChild(f).getControl(GhostControl.class) && b == character || a == character && b == currentFinish.getChild(f).getControl(GhostControl.class)) {
                    //character.setPhysicsLocation(new Vector3f(0, 0, 0));
                    levelTransitionStage = 1;
                    fade.fadeOut();
                    AudioNode song = (AudioNode) music.getChild(currentLevelIncreasing - 1);
                    song.stop();

                    //moveToNextLevel();
                    left = false;
                    right = false;
                    ableToMove = false;

                }

            }
        }
        for (int f = 0; f < cannons.getQuantity(); f++) {

            if (a == cannons.getChild(f).getControl(GhostControl.class) && b == character || a == character && b == cannons.getChild(f).getControl(GhostControl.class)) {
                character.setPhysicsLocation(new Vector3f(0, 0, 0));

            }

        }

    }

    public void moveToNextLevel() {
        isPlayedFinish = false;
        currentLevelIncreasing += 1;
        AudioNode song = (AudioNode) music.getChild(currentLevelIncreasing - 1);
        song.play();
        switch (currentLevelIncreasing) {
            case 1:
                Node level1 = (Node) assetManager.loadModel("Models/level1_test1.j3o");
                levels_phys.attachChild(level1);
                System.out.println("LOADING 1");
                break;
            case 2:
                Node level2 = (Node) assetManager.loadModel("Models/level2_test1.j3o");
                levels_phys.attachChild(level2);
                System.out.println("LOADING 2");
                break;
            case 3:
                Node level3 = (Node) assetManager.loadModel("Models/level3_test1.j3o");
                levels_phys.attachChild(level3);
                System.out.println("LOADING 3");
                break;
            case 4:
                Node level4 = (Node) assetManager.loadModel("Models/level4_test1.j3o");
                levels_phys.attachChild(level4);
                System.out.println("LOADING 4");
                break;
            case 5:
                Node level5 = (Node) assetManager.loadModel("Models/level5_test1.j3o");
                levels_phys.attachChild(level5);
                System.out.println("LOADING 5");
                break;
            default:
                Node end = (Node) assetManager.loadModel("Models/credits.j3o");
                levels_phys.attachChild(end);
                System.out.println("LOADING END");
                break;

        }

        Node currentPhys = (Node) levels_phys.getChild(currentLevel - 1);
        RigidBodyControl levelPhy = new RigidBodyControl(CollisionShapeFactory.createMeshShape(currentPhys.getChild("Collidables")), 0);
        currentPhys.addControl(levelPhy);

        bulletAppState.getPhysicsSpace().add(levelPhy);

        bulletAppState.getPhysicsSpace().add(levels_phys.getChild(currentLevel - 1));

        levels_non.attachChild(currentPhys.getChild("NonCol"));

        Node currentDeath = (Node) levels_phys.getChild(0);
        Node level_death = (Node) currentDeath.getChild("Death");

        for (int i = 0; i < level_death.getQuantity(); i++) {
            CollisionShape bull
                    = CollisionShapeFactory.createMeshShape(level_death.getChild(i));
            GhostControl col = new GhostControl(bull);

            col.setPhysicsLocation(level_death.getChild(i).getLocalTranslation());
            level_death.getChild(i).addControl(col);

            bulletAppState.getPhysicsSpace().add(col);

        }

        levels_death.attachChild(level_death);
        Node currentFinish = (Node) levels_phys.getChild(0);
        Node level_finish = (Node) currentFinish.getChild("Finish");

        for (int i = 0; i < level_finish.getQuantity(); i++) {
            CollisionShape bull
                    = CollisionShapeFactory.createMeshShape(level_finish.getChild(i));
            GhostControl col = new GhostControl(bull);

            col.setPhysicsLocation(level_finish.getChild(i).getLocalTranslation());
            level_finish.getChild(i).addControl(col);

            bulletAppState.getPhysicsSpace().add(col);

        }
        levels_finish.attachChild(level_finish);

        Node currentCannon = (Node) levels_phys.getChild(0);
        levels_cannon = (Node) currentCannon.getChild("Cannons");

        rootNode.attachChild(levels_cannon);

        character.setLinearVelocity(new Vector3f(0, 0, 0));
        character.warp(new Vector3f(0, 0, 0));
        ableToMove = true;

    }

    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        if (channel == shootingChannel) {
            channel.setAnim("stand");
        }
    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
    }

}
