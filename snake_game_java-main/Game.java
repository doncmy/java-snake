import javax.swing.JPanel;
import javax.imageio.ImageIO;
import java.util.*;
import java.util.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class Game extends JPanel {
    // 游戏计时器，控制游戏循环
    private Timer timer;
    // 贪吃蛇对象
    private Snake snake;
    // 苹果(食物)的位置
    private Point apple;
    // 当前得分
    private int points = 0;
    // 最高分
    private Map<GameMode, Integer> bestMap = new HashMap<>();
    // 游戏状态
    private GameStatus status;
    private Point shrinkItem = null; // 缩短道具的位置
    private static final int SHRINK_AMOUNT = 1; // 每次缩短的节数
    private Snake aiSnake; // AI蛇
    private boolean withAISnake = false;
    // 新增AI蛇分数变量
    private int aiSnakeScore = 0;

    // 字体定义
    private static Font FONT_M = new Font("ArcadeClassic", Font.PLAIN, 24);  // 中等字体
    private static Font FONT_M_ITALIC = new Font("ArcadeClassic", Font.ITALIC, 24);  // 中等斜体
    private static Font FONT_L = new Font("ArcadeClassic", Font.PLAIN, 84);  // 大字体
    private static Font FONT_XL = new Font("ArcadeClassic", Font.PLAIN, 150);  // 超大字体
    private static Font FONT_S = new Font("ArcadeClassic", Font.PLAIN, 16);  // 小号字体
    
    // 游戏区域尺寸
    private static int WIDTH = 760;
    private static int HEIGHT = 720;
    // 游戏循环延迟(毫秒)
    private static int DELAY = 50;         // 固定速度
    private boolean isPlayerAccelerating = false;

    // 音效播放器
    private AudioPlayer bgmPlayer = new AudioPlayer("snake_game_java-main/bgm.wav");
    private AudioPlayer eatPlayer = new AudioPlayer("snake_game_java-main/eat.wav");
    private AudioPlayer diePlayer = new AudioPlayer("snake_game_java-main/die.wav");
    // 背景音乐开关
    private boolean isBgmOn = true;

    // 粒子效果列表
    private List<Particle> particles = new ArrayList<>();

    // 持续加速模式相关变量
    private boolean isSpeedUpMode = false;
    private int speedUpLevel = 0;  // 当前加速等级
    private int speedUpInterval = 200; // 每200帧加速一次（约10秒）
    private int speedUpFrameCount = 0;  // 加速帧计数器
    private static final int BASE_DELAY = 120; // 普通模式速度
    private static final int MIN_DELAY = 15;  // 持续加速模式最低速度
    private int currentDelay = BASE_DELAY;  // 当前延迟时间

    // 菜单背景图片
    private Image menuBgImage = null;
    // 蛇头图片
    private Image snakeHeadImage = null;
    // 蛇身图片
    private Image snakeBodyImage = null;
    // AI蛇头图片
    private Image aiHeadImage = null;
    // AI蛇身图片
    private Image aiBodyImage = null;
    // 缩短道具图片
    private Image shrinkItemImage = null;
    // 苹果图片
    private Image appleImage = null;
    // 汉堡食物变量
    private Point burger = null;
    private Image burgerImage = null;

    // 无尽障碍模式相关变量
    private boolean isObstacleMode = false;
    private List<Obstacle> obstacles = new ArrayList<>();  // 障碍物列表
    private int obstacleInterval = 50; // 每50帧生成一个障碍物
    private int obstacleFrameCount = 0;  // 障碍物生成帧计数器
    private static final int OBSTACLE_SIZE = 16;  // 障碍物大小

    // 障碍物类型枚举
    private enum ObstacleType { STATIC, MOVING }  // 静态和移动两种类型
    
    // 障碍物内部类
    private class Obstacle {
        private Point pos;  // 位置
        private ObstacleType type;  // 类型
        private int dx, dy; // 仅MOVING类型用，移动方向
        
        // 构造函数
        public Obstacle(Point pos, ObstacleType type, int dx, int dy) {
            this.pos = pos;
            this.type = type;
            this.dx = dx;
            this.dy = dy;
        }
        
        // 获取位置
        public Point getPos() { return pos; }
        
        // 获取类型
        public ObstacleType getType() { return type; }
        
        // 移动方法
        public void move() {
            if (type == ObstacleType.MOVING) {
                pos.setX(pos.getX() + dx);
                pos.setY(pos.getY() + dy);
            }
        }
        
        // 边界反弹检测
        public void bounceIfNeeded(int minX, int minY, int maxX, int maxY) {
            if (type == ObstacleType.MOVING) {
                if (pos.getX() < minX || pos.getX() > maxX) dx = -dx;
                if (pos.getY() < minY || pos.getY() > maxY) dy = -dy;
            }
        }
        
        // 获取障碍物x坐标
        public int getX() { return pos.getX(); }
        
        // 获取障碍物y坐标
        public int getY() { return pos.getY(); }
    }

    // 网格大小
    private static final int gridSize = 20;

    // 缩短道具帧计数器
    private int shrinkItemFrameCount = 0;

    // 死亡动画计时器
    private int deathAnimFrame = 0;
    // 死亡动画持续帧数
    private static final int DEATH_ANIMATION_DURATION = 40; // 约0.7秒

    // 游戏开始时间
    private long startTime = 0;

    
    // 游戏模式枚举
    public enum GameMode {
        CLASSIC, AI, SPEEDUP, OBSTACLE  // 经典模式、AI对战、持续加速、无尽障碍
    }
    private GameMode currentMode = GameMode.CLASSIC;  // 当前游戏模式

    // 模式中英文映射
    private static final Map<GameMode, String> modeToChinese = new HashMap<>();
    private static final Map<String, GameMode> chineseToMode = new HashMap<>();
    static {
        // 初始化模式名称映射
        modeToChinese.put(GameMode.CLASSIC, "单人模式");
        modeToChinese.put(GameMode.AI, "AI对战模式");
        modeToChinese.put(GameMode.SPEEDUP, "持续加速模式");
        modeToChinese.put(GameMode.OBSTACLE, "无尽障碍模式");
        // 初始化反向映射
        for (Map.Entry<GameMode, String> entry : modeToChinese.entrySet()) {
            chineseToMode.put(entry.getValue(), entry.getKey());
        }
    }

    // 游戏构造函数
    public Game(boolean withAISnake, boolean isSpeedUpMode, boolean isObstacleMode) {
        this.withAISnake = withAISnake;  // 是否启用AI蛇
        this.isSpeedUpMode = isSpeedUpMode;  // 是否持续加速模式
        this.isObstacleMode = isObstacleMode;  // 是否障碍物模式
        addKeyListener(new KeyListener());  // 添加键盘监听
        setFocusable(true);  // 设置可获取焦点
        setBackground(Color.black);  // 设置背景色
        setDoubleBuffered(true);  // 启用双缓冲
        status = GameStatus.MODE_SELECT;  // 初始状态为模式选择
        repaint();  // 重绘界面
        bgmPlayer.playLoop();  // 循环播放背景音乐
        
        // 加载菜单背景图片
        try {
            menuBgImage = ImageIO.read(new File("snake_game_java-main/menu_bg.png"));
        } catch (IOException e) {
            System.out.println("菜单背景图片加载失败");
        }
        
        // 加载蛇头图片
        try {
            snakeHeadImage = ImageIO.read(new File("snake_game_java-main/snake_head.png"));
        } catch (IOException e) {
            System.out.println("蛇头图片加载失败");
        }
        
        // 加载玩家蛇身图片
        try {
            snakeBodyImage = ImageIO.read(new File("snake_game_java-main/snake_body.png"));
        } catch (IOException e) {
            System.out.println("玩家蛇身图片加载失败");
        }
        
        // 加载AI蛇头图片
        try {
            aiHeadImage = ImageIO.read(new File("snake_game_java-main/ai_head.png"));
        } catch (IOException e) {
            System.out.println("AI蛇头图片加载失败");
        }
        
        // 加载AI蛇身图片
        try {
            aiBodyImage = ImageIO.read(new File("snake_game_java-main/ai_body.png"));
        } catch (IOException e) {
            System.out.println("AI蛇身图片加载失败");
        }
        
        // 加载缩减道具图片
        try {
            shrinkItemImage = ImageIO.read(new File("snake_game_java-main/shrink_item.png"));
        } catch (IOException e) {
            System.out.println("缩减道具图片加载失败");
        }
        
        // 加载苹果图片
        try {
            appleImage = ImageIO.read(new File("snake_game_java-main/apple.png"));
        } catch (IOException e) {
            System.out.println("苹果图片加载失败");
        }
        
        // 加载汉堡图片
        try {
            burgerImage = ImageIO.read(new File("snake_game_java-main/hamburger.png"));
        } catch (IOException e) {
            System.out.println("汉堡图片加载失败");
        }
        
        // 初始化各模式最高分
        for (GameMode mode : GameMode.values()) {
            bestMap.put(mode, 0);
        }
        loadBestScores();  // 加载历史最高分
    }

    // 保留原有构造函数
    public Game(boolean withAISnake, boolean isSpeedUpMode) {
        this(withAISnake, isSpeedUpMode, false);  // 调用主构造函数
    }

    public Game(boolean withAISnake) {
        this(withAISnake, false, false);  // 调用主构造函数
    }

    public Game() {
        this(false, false, false);  // 调用主构造函数
    }

    // 绘制游戏组件
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);  // 调用父类绘制方法
        render(g);  // 调用自定义渲染方法
        Toolkit.getDefaultToolkit().sync();  // 同步图形状态，确保绘制完成
    }

    // 更新游戏状态
    private void update() {
        snake.move();  // 移动蛇
        
        // 检查是否吃到苹果或道具
        checkEatAppleAndItem();
        
        // 如果有AI蛇，移动AI蛇并确保不越界
        if (withAISnake && aiSnake != null) {
            moveAISnake();
            clampSnakeToBounds(aiSnake);
        }
        
        // 如果玩家正在加速，额外移动一次并生成粒子效果
        if (isPlayerAccelerating) {
            snake.move();
            spawnSpeedParticles();
            checkEatAppleAndItem();
        }
        
        // 更新所有粒子效果
        List<Particle> toRemove = new ArrayList<>();
        for (Particle p : particles) {
            p.update();
            if (!p.isAlive()) toRemove.add(p);
        }
        particles.removeAll(toRemove);
        
        // AI蛇吃苹果逻辑
        if (withAISnake && aiSnake != null && apple != null && aiSnake.getHead().intersects(apple, gridSize / 2)) {
            aiSnake.setNeedGrow();
            apple = null;
            spawnApple();
        }
        
        // AI蛇吃缩短道具逻辑
        if (withAISnake && aiSnake != null && shrinkItem != null && aiSnake.getHead().intersects(shrinkItem, gridSize / 2)) {
            shrinkSnake(aiSnake);
            shrinkItem = null;
            spawnEatParticles(aiSnake.getHead());
        }
        
        // 碰撞检测：玩家蛇头碰到AI蛇身体
        if (withAISnake && aiSnake != null) {
            for (int i = 1; i < aiSnake.getTail().size(); i++) {
                if (snake.getHead().equals(aiSnake.getTail().get(i))) {
                    setStatus(GameStatus.GAME_OVER);
                    return;
                }
            }
            
            // 头对头碰撞吞噬机制
            if (snake.getHead().equals(aiSnake.getHead())) {
                int playerLen = snake.getTail().size() + 1;
                int aiLen = aiSnake.getTail().size() + 1;
                if (playerLen > aiLen) {
                    // 玩家蛇更长，吞噬AI蛇
                    for (int i = 0; i < aiLen; i++) snake.setNeedGrow();
                    points += aiLen;
                    aiSnake = null;
                    return;
                } else if (aiLen > playerLen) {
                    // AI蛇更长，游戏结束
                    for (int i = 0; i < playerLen; i++) aiSnake.setNeedGrow();
                    aiSnakeScore = aiLen + playerLen;
                    setStatus(GameStatus.GAME_OVER);
                    return;
                }
            }
            
            // AI蛇头碰到玩家蛇身体
            for (int i = 1; i < snake.getTail().size(); i++) {
                if (aiSnake.getHead().equals(snake.getTail().get(i))) {
                    int aiLen = aiSnake.getTail().size() + 1;
                    for (int j = 0; j < aiLen; j++) snake.setNeedGrow();
                    points += aiLen;
                    aiSnake = null;
                    return;
                }
            }
        }
        
        // 如果苹果被吃掉，生成新苹果
        if (apple == null) {
            spawnApple();
        }
        
        // 随机生成缩短道具
        if (shrinkItem == null && Math.random() < 0.005) {
            spawnShrinkItem();
        }
        
        // 障碍物模式下的障碍物生成和移动
        if (isObstacleMode) {
            obstacleFrameCount++;
            if (obstacleFrameCount >= obstacleInterval) {
                obstacleFrameCount = 0;
                spawnObstacle();  // 生成新障碍物
            }
            
            // 移动所有移动型障碍物
            for (Obstacle o : obstacles) {
                if (o.getType() == ObstacleType.MOVING) {
                    o.move();
                    o.bounceIfNeeded(20, 40, WIDTH - OBSTACLE_SIZE + 20, HEIGHT - OBSTACLE_SIZE + 40);
                }
            }
        }
        
        // 随机生成汉堡
        if (burger == null && Math.random() < 0.01) {
            spawnBurger();
        }
        
        // 检查游戏是否结束
        checkForGameOver();
    }

    // 重置游戏
    private void reset() {
        points = 0;  // 重置分数
        aiSnakeScore = 0;  // 重置AI分数
        apple = null;  // 清空苹果
        shrinkItem = null;  // 清空缩短道具
        burger = null;//清空汉堡
        obstacles.clear();  // 清空障碍物
        
        // 初始化蛇的位置（居中）
        int startCol = (WIDTH / 2) / gridSize;
        int startRow = (HEIGHT / 2) / gridSize;
        int startX = 20 + startCol * gridSize;
        int startY = 40 + startRow * gridSize;
        snake = new Snake(startX, startY);
        
        // 重置加速相关状态
        isPlayerAccelerating = false;
        speedUpLevel = 0;
        speedUpFrameCount = 0;
        currentDelay = BASE_DELAY;
        obstacleFrameCount = 0;
        
        // 初始化AI蛇
        if (withAISnake) {
            int aiStartCol = (WIDTH / 2) / gridSize;
            int aiStartRow = ((HEIGHT / 2 - 100) / gridSize);
            int aiStartX = 20 + aiStartCol * gridSize;
            int aiStartY = 40 + aiStartRow * gridSize;
            aiSnake = new Snake(aiStartX, aiStartY);
            aiSnake.turn(Direction.RIGHT);
            aiSnake.setNeedGrowFalse();
        } else {
            aiSnake = null;
        }
        
        setStatus(GameStatus.RUNNING);  // 设置游戏状态为运行中
        repaint();  // 重绘界面
        particles.clear();  // 清空粒子效果
        
        // 记录游戏开始时间
        startTime = System.currentTimeMillis();
    }

    // 设置游戏状态
    private void setStatus(GameStatus newStatus) {
        if (status == newStatus) return;  // 防止重复切换
        
        switch(newStatus) {
            case RUNNING:
                if (timer != null) timer.cancel();  // 取消现有计时器
                timer = new Timer();  // 创建新计时器
                // 根据模式设置延迟时间
                timer.schedule(new GameLoop(), 0, isSpeedUpMode ? currentDelay : BASE_DELAY);
                bgmPlayer.playLoop();  // 播放背景音乐
                startTime = System.currentTimeMillis();  // 记录开始时间
                break;
                
            case PAUSED:
                if (timer != null) timer.cancel();  // 暂停游戏
                break;
                
            case DEATH_ANIMATION:
                if (timer != null) timer.cancel();  // 取消现有计时器
                diePlayer.playOnce();  // 播放死亡音效
                bgmPlayer.stop();  // 停止背景音乐
                deathAnimFrame = 0;  // 重置死亡动画帧数
                particles.clear();  // 清空现有粒子
                
                // 生成死亡粒子效果
                if (snake != null) {
                    try {
                        spawnDeathParticles(snake.getHead());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                // 设置死亡动画计时器
                timer = new Timer();
                timer.schedule(new GameLoop(), 0, 15); // 死亡动画帧率
                break;
                
            case GAME_OVER:
                if (timer != null) timer.cancel();  // 取消计时器
                writeScoreToFile(points);  // 保存分数
                
                // 更新最高分
                int best = bestMap.getOrDefault(currentMode, 0);
                if (points > best) {
                    bestMap.put(currentMode, points);
                    saveBestScores();  // 保存最高分
                }
                
                repaint();  // 重绘界面
                break;
                
            default:
                break;
        }
        
        status = newStatus;  // 更新状态
    }

    // 切换暂停状态
    private void togglePause() {
        setStatus(status == GameStatus.PAUSED ? GameStatus.RUNNING : GameStatus.PAUSED);
    }

    // 检查游戏是否结束
    private void checkForGameOver() {
        Point head = snake.getHead();
        
        // 检查是否撞墙
        boolean hitBoundary = !isPointInBounds(head);
        for (Point t : snake.getTail()) {
            if (!isPointInBounds(t)) {
                hitBoundary = true;
                break;
            }
        }
        
        // 检查是否撞到自己
        boolean ateItself = false;
        for (int i = 2; i < snake.getTail().size(); i++) {
            if (head.equals(snake.getTail().get(i))) {
                ateItself = true;
                break;
            }
        }
        
        // 检查是否撞到障碍物
        boolean hitObstacle = false;
        if (isObstacleMode) {
            for (Obstacle o : obstacles) {
                if (head.intersects(o.getPos(), OBSTACLE_SIZE)) {
                    hitObstacle = true;
                    break;
                }
                for (Point t : snake.getTail()) {
                    if (t.intersects(o.getPos(), OBSTACLE_SIZE)) {
                        hitObstacle = true;
                        break;
                    }
                }
                if (hitObstacle) break;
            }
        }
        
        // 如果发生碰撞且不在死亡动画/结束状态，进入死亡动画
        if ((hitBoundary || ateItself || hitObstacle) && status != GameStatus.DEATH_ANIMATION && status != GameStatus.GAME_OVER) {
            setStatus(GameStatus.DEATH_ANIMATION);
            return;
        }
    }

    // 绘制居中文本
    public void drawCenteredString(Graphics g, String text, Font font, int y) {
        // 计算文本宽度以确定居中位置
        FontMetrics metrics = g.getFontMetrics(font);
        int x = (WIDTH - metrics.stringWidth(text)) / 2;

        // 设置字体并绘制文本
        g.setFont(font);
        g.drawString(text, x, y);
    }

    // 渲染游戏画面
    private void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(53, 220, 8));  // 设置默认颜色
        g2d.setFont(FONT_M);  // 设置默认字体
        
        // 模式选择界面
        if (status == GameStatus.MODE_SELECT) {
            // 1. 绘制背景图（如果加载成功）
            if (menuBgImage != null) {
                g2d.drawImage(menuBgImage, 0, 0, WIDTH + 40, HEIGHT + 80, null);
            } else {
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, WIDTH + 40, HEIGHT + 80);
            }
            
            // 2. 绘制半透明黑色蒙版
            g2d.setColor(new Color(0, 0, 0, 120));
            g2d.fillRoundRect(100, 80, WIDTH - 160, HEIGHT - 120, 40, 40);
            
            // 3. 绘制标题
            String title = "蛇皇争锋：智控风暴";
            Font titleFont = FONT_L.deriveFont(Font.BOLD, 64f);
            g2d.setFont(titleFont);
            int titleX = (WIDTH + 40 - g2d.getFontMetrics().stringWidth(title)) / 2;
            int titleY = 200;
            
            // 绘制阴影
            g2d.setColor(new Color(0,0,0,180));
            g2d.drawString(title, titleX+4, titleY+6);
            
            // 绘制描边
            g2d.setColor(Color.WHITE);
            g2d.drawString(title, titleX-2, titleY-2);
            
            // 绘制正文
            g2d.setColor(new Color(53, 220, 8));
            g2d.drawString(title, titleX, titleY);
            
            // 4. 绘制模式选择按钮
            String[] options = {"按1：单人模式", "按2：AI对战模式", "按3：持续加速模式", "按4：无尽障碍模式", "按Q退出游戏"};
            int optFontSize = 36;
            Font optFont = FONT_M.deriveFont(Font.BOLD, optFontSize);
            g2d.setFont(optFont);
            int optY = 320;
            int optGap = 60;
            
            for (int i = 0; i < options.length; i++) {
                int optW = g2d.getFontMetrics().stringWidth(options[i]);
                int optX = (WIDTH + 40 - optW) / 2;
                
                // 绘制按钮底色
                g2d.setColor(new Color(30, 30, 30, 180));
                g2d.fillRoundRect(optX-30, optY-38, optW+60, 50, 30, 30);
                
                // 绘制按钮描边
                g2d.setColor(new Color(53, 220, 8, 180));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(optX-30, optY-38, optW+60, 50, 30, 30);
                
                // 绘制选项文字
                g2d.setColor(Color.WHITE);
                g2d.drawString(options[i], optX, optY);
                optY += optGap;
            }
            return;
        }
        
        // 游戏未开始时的界面
        if (status == GameStatus.NOT_STARTED) {
            drawCenteredString(g2d, "贪吃蛇", FONT_XL, 200);
            drawCenteredString(g2d, "游戏", FONT_XL, 350);
            drawCenteredString(g2d, "按任意键开始游戏", FONT_M_ITALIC, 430);
            return;
        }

        // 绘制游戏区域网格
        int gridSize = 20;
        g2d.setColor(new Color(200, 200, 200, 80)); // 浅灰色半透明
        for (int x = 20; x <= WIDTH + 20; x += gridSize) {
            g2d.drawLine(x, 40, x, HEIGHT + 40);
        }
        for (int y = 40; y <= HEIGHT + 40; y += gridSize) {
            g2d.drawLine(20, y, WIDTH + 20, y);
        }
        g2d.setColor(new Color(53, 220, 8)); // 恢复默认颜色

        // 获取蛇头位置
        Point p = snake.getHead();

        // 绘制游戏信息：时间、分数、蛇长度、最高分
        long durationSec = 0;
        if (startTime > 0 && (status == GameStatus.RUNNING || status == GameStatus.PAUSED || status == GameStatus.DEATH_ANIMATION)) {
            durationSec = (System.currentTimeMillis() - startTime) / 1000;
        }
        long min = durationSec / 60;
        long sec = durationSec % 60;
        String timeStr = "时间: " + min + ":" + String.format("%02d", sec);
        String scoreStr = "当前分数: " + String.format("%04d", points);
        int snakeLength = snake.getTail().size() + 1;
        String lengthStr = "蛇长度: " + snakeLength;
        String bestStr = modeToChinese.get(currentMode) + "最高分: " + String.format("%04d", bestMap.getOrDefault(currentMode, 0));
        
        // 计算信息总宽度并居中显示
        FontMetrics metrics = g2d.getFontMetrics(FONT_M);
        String[] infoArr = {timeStr, scoreStr, lengthStr, bestStr};
        int[] infoW = new int[infoArr.length];
        int totalW = 0;
        for (int i = 0; i < infoArr.length; i++) {
            infoW[i] = metrics.stringWidth(infoArr[i]);
            totalW += infoW[i];
        }
        int gap = (WIDTH + 40 - totalW) / (infoArr.length + 1); // 动态均分间隔
        int x = gap;
        int y = 30;
        for (int i = 0; i < infoArr.length; i++) {
            g2d.drawString(infoArr[i], x, y);
            x += infoW[i] + gap;
        }

        // 绘制苹果
        if (apple != null) {
            if (appleImage != null) {
                g2d.drawImage(appleImage, apple.getX(), apple.getY(), gridSize, gridSize, null);
            } else {
                g2d.setColor(Color.RED);
                g2d.fillOval(apple.getX(), apple.getY(), gridSize, gridSize);
                g2d.setColor(new Color(53, 220, 8));
            }
        }

        // 绘制缩短道具
        if (shrinkItem != null) {
            if (shrinkItemImage != null) {
                g2d.drawImage(shrinkItemImage, shrinkItem.getX(), shrinkItem.getY(), gridSize, gridSize, null);
            } else {
                g2d.setColor(Color.BLUE);
                g2d.fillOval(shrinkItem.getX(), shrinkItem.getY(), gridSize, gridSize);
            }
        }

        // 绘制汉堡
        if (burger != null) {
            if (burgerImage != null) {
                g2d.drawImage(burgerImage, burger.getX(), burger.getY(), gridSize, gridSize, null);
            } else {
                g2d.setColor(Color.YELLOW);
                g2d.fillOval(burger.getX(), burger.getY(), gridSize, gridSize);
                g2d.setColor(new Color(53, 220, 8));
            }
        }

        // 游戏结束界面
        if (status == GameStatus.GAME_OVER) {
            drawCenteredString(g2d, "游戏结束", FONT_L, 300);
            drawCenteredString(g2d, "本局得分: " + points, FONT_M, 350);
            drawCenteredString(g2d, "按1：继续游戏    按2：退出到主菜单", FONT_M_ITALIC, 400);
        }

        // 暂停界面
        if (status == GameStatus.PAUSED) {
            g2d.drawString("暂停", 600, 14);
        }

        // 绘制蛇头
        if (snakeHeadImage != null) {
            g2d.drawImage(snakeHeadImage, p.getX(), p.getY(), gridSize, gridSize, null);
        } else {
            g2d.setColor(Color.ORANGE);
            g2d.fillOval(p.getX(), p.getY(), gridSize, gridSize);
        }
        
        // 绘制蛇身
        for(int i = 0, size = snake.getTail().size(); i < size; i++) {
            Point t = snake.getTail().get(i);
            if (snakeBodyImage != null) {
                g2d.drawImage(snakeBodyImage, t.getX(), t.getY(), gridSize, gridSize, null);
            } else {
                g2d.setColor(new Color(74, 245, 14));
                g2d.drawOval(t.getX(), t.getY(), gridSize, gridSize);
            }
        }

        // 绘制游戏边界
        g2d.setColor(new Color(71, 128, 0));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(20, 40, WIDTH, HEIGHT);

        // 绘制AI蛇
        if (withAISnake && aiSnake != null && status != GameStatus.MODE_SELECT) {
            Point aiHead = aiSnake.getHead();
            if (aiHeadImage != null) {
                g2d.drawImage(aiHeadImage, aiHead.getX(), aiHead.getY(), gridSize, gridSize, null);
            } else {
                g2d.setColor(new Color(74, 245, 14));
                g2d.fillOval(aiHead.getX(), aiHead.getY(), gridSize, gridSize);
            }
            for (Point t : aiSnake.getTail()) {
                if (aiBodyImage != null) {
                    g2d.drawImage(aiBodyImage, t.getX(), t.getY(), gridSize, gridSize, null);
                } else {
                    g2d.setColor(Color.RED);
                    g2d.drawOval(t.getX(), t.getY(), gridSize, gridSize);
                }
            }
            g2d.setColor(new Color(53, 220, 8)); // 恢复默认颜色
        }
        
        // 绘制所有粒子效果
        for (Particle p1 : new ArrayList<>(particles)) {
            if (p1 != null) {
                p1.draw(g2d);
            }
        }

        // 持续加速模式下显示速度等级
        if (isSpeedUpMode) {
            g2d.drawString("速度等级: " + (speedUpLevel + 1), 320, 60);
        }

        // 绘制障碍物
        if (isObstacleMode) {
            for (Obstacle o : obstacles) {
                if (o.getType() == ObstacleType.MOVING) {
                    g2d.setColor(Color.RED);  // 移动障碍物为红色
                } else {
                    g2d.setColor(Color.GRAY);  // 静态障碍物为灰色
                }
                Point op = o.getPos();
                g2d.fillRect(op.getX(), op.getY(), OBSTACLE_SIZE, OBSTACLE_SIZE);
            }
            g2d.setColor(new Color(53, 220, 8));
        }
        // 显示音乐状态
        g2d.setFont(FONT_S);
        g2d.setColor(Color.WHITE);
        g2d.drawString("音乐: " + (isBgmOn ? "开" : "关") + " (M键切换)", 30, HEIGHT + 60);
    }

    // 生成新的苹果位置
    public void spawnApple() {
        int gridSize = 20;
        int gridCols = WIDTH / gridSize;
        int gridRows = HEIGHT / gridSize;
        Random rand = new Random();
        
        while (true) {
            int col = rand.nextInt(gridCols);
            int row = rand.nextInt(gridRows);
            int x = 20 + col * gridSize;
            int y = 40 + row * gridSize;
            boolean conflict = false;
            
            // 检查是否与障碍物重叠
            if (isObstacleAt(x, y)) conflict = true;
            
            // 检查是否与蛇头重叠
            if (snake.getHead().getX() == x && snake.getHead().getY() == y) conflict = true;
            
            // 检查是否与蛇身重叠
            for (Point t : snake.getTail()) {
                if (t.getX() == x && t.getY() == y) { conflict = true; break; }
            }
            
            // 如果没有冲突，设置苹果位置
            if (!conflict) {
                apple = new Point(x, y);
                break;
            }
        }
    }

    // 生成新的缩短道具位置
    public void spawnShrinkItem() {
        int gridSize = 20;
        int gridCols = WIDTH / gridSize;
        int gridRows = HEIGHT / gridSize;
        Random rand = new Random();
        while (true) {
            int col = rand.nextInt(gridCols);
            int row = rand.nextInt(gridRows);
            int x = 20 + col * gridSize;
            int y = 40 + row * gridSize;
            
            // 检查是否与障碍物重叠
            if (!isObstacleAt(x, y)) {
                shrinkItem = new Point(x, y);
                break;
            }
        }
    }

    // 键盘监听器内部类
    private class KeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            
            // 模式选择界面按键处理
            if (status == GameStatus.MODE_SELECT) {
                if (key == KeyEvent.VK_1) {
                    currentMode = GameMode.CLASSIC;
                    withAISnake = false;
                    isSpeedUpMode = false;
                    isObstacleMode = false;
                    reset();
                    setStatus(GameStatus.RUNNING);
                    return;
                } else if (key == KeyEvent.VK_2) {
                    currentMode = GameMode.AI;
                    withAISnake = true;
                    isSpeedUpMode = false;
                    isObstacleMode = false;
                    reset();
                    setStatus(GameStatus.RUNNING);
                    return;
                } else if (key == KeyEvent.VK_3) {
                    currentMode = GameMode.SPEEDUP;
                    withAISnake = false;
                    isSpeedUpMode = true;
                    isObstacleMode = false;
                    reset();
                    setStatus(GameStatus.RUNNING);
                    return;
                } else if (key == KeyEvent.VK_4) {
                    currentMode = GameMode.OBSTACLE;
                    withAISnake = false;
                    isSpeedUpMode = false;
                    isObstacleMode = true;
                    reset();
                    setStatus(GameStatus.RUNNING);
                    return;
                } else if (key == KeyEvent.VK_Q) {
                    System.exit(0);  // 退出游戏
                }
            }
            
            // 游戏结束时的按键处理
            if (status == GameStatus.GAME_OVER) {
                if (key == KeyEvent.VK_1) {
                    reset();  // 重新开始
                    return;
                } else if (key == KeyEvent.VK_2) {
                    // 返回主菜单
                    status = GameStatus.MODE_SELECT;
                    repaint();
                    return;
                }
            }
            
            // 游戏运行时的按键处理
            if (status == GameStatus.RUNNING) {
                switch(key) {
                    case KeyEvent.VK_LEFT: snake.turn(Direction.LEFT); break;
                    case KeyEvent.VK_RIGHT: snake.turn(Direction.RIGHT); break;
                    case KeyEvent.VK_UP: snake.turn(Direction.UP); break;
                    case KeyEvent.VK_DOWN: snake.turn(Direction.DOWN); break;
                    case KeyEvent.VK_SPACE:
                        isPlayerAccelerating = true;  // 加速
                        break;
                }
            }
            
            // 游戏未开始时的任意键开始
            if (status == GameStatus.NOT_STARTED) {
                setStatus(GameStatus.RUNNING);
            }
            
            // P键暂停/继续
            if (key == KeyEvent.VK_P) {
                togglePause();
            }
            // 背景音乐开关（M键）
            if (key == KeyEvent.VK_M) {
                isBgmOn = !isBgmOn;
                if (isBgmOn) {
                    bgmPlayer.playLoop();
                } else {
                    bgmPlayer.stop();
                }
                repaint();
                return;
            }
        }
        
        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();
            // 松开空格键停止加速
            if (key == KeyEvent.VK_SPACE && status == GameStatus.RUNNING) {
                isPlayerAccelerating = false;
            }
        }
    }

    // 游戏循环内部类
    private class GameLoop extends java.util.TimerTask {
        public void run() {
            // 死亡动画处理
            if (status == GameStatus.DEATH_ANIMATION) {
                deathAnimFrame++;
                
                // 更新所有粒子
                List<Particle> toRemove = new ArrayList<>();
                for (Particle p : particles) {
                    p.update();
                    if (!p.isAlive()) toRemove.add(p);
                }
                particles.removeAll(toRemove);
                
                // 死亡动画结束后进入游戏结束状态
                if (deathAnimFrame >= DEATH_ANIMATION_DURATION) {
                    setStatus(GameStatus.GAME_OVER);
                }
                repaint();
                return;
            }
            
            // 持续加速模式下的加速逻辑
            if (isSpeedUpMode) {
                speedUpFrameCount++;
                if (speedUpFrameCount >= speedUpInterval) {
                    speedUpFrameCount = 0;
                    if (currentDelay > MIN_DELAY) {
                        currentDelay -= 5; // 每次加速减少5ms
                        speedUpLevel++;
                        
                        // 重新设置计时器
                        if (timer != null) timer.cancel();
                        timer = new Timer();
                        timer.schedule(new GameLoop(), 0, currentDelay);
                    }
                }
            }
            
            update();  // 更新游戏状态
            repaint(); // 重绘界面
        }
    }

    // 缩短蛇身
    private void shrinkSnake() {
        // 最短为0（只剩蛇头）
        for (int i = 0; i < SHRINK_AMOUNT; i++) {
            if (snake.getTail().size() > 0) {
                snake.getTail().remove(snake.getTail().size() - 1);
            } else {
                break;
            }
        }
    }

    // 缩短指定蛇
    private void shrinkSnake(Snake s) {
        for (int i = 0; i < SHRINK_AMOUNT; i++) {
            if (s.getTail().size() > 0) {
                s.getTail().remove(s.getTail().size() - 1);
            } else {
                break;
            }
        }
    }

    // 将分数写入文件
    private void writeScoreToFile(int score) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("scores.txt", true))) {
            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timeStr = now.format(formatter);
            
            // 确定游戏模式
            String modeStr;
            if (isObstacleMode) {
                modeStr = "无尽障碍模式";
            } else if (isSpeedUpMode) {
                modeStr = "持续加速模式";
            } else if (withAISnake) {
                modeStr = "AI对战模式";
            } else {
                modeStr = "单人模式";
            }
            
            // 计算游戏时长
            long durationSec = 0;
            if (startTime > 0) {
                durationSec = (System.currentTimeMillis() - startTime) / 1000;
            }
            
            // 写入分数记录
            writer.write("时间: " + timeStr + "  分数: " + score + "  模式: " + modeStr + "  已玩时间: " + durationSec + "秒\n");
        } catch (IOException e) {
            System.out.println("写入分数文件失败: " + e.getMessage());
        }
    }

    // AI蛇移动逻辑
    private void moveAISnake() {
        if (apple == null) return;
        
        Point head = aiSnake.getHead();
        int dx = apple.getX() - head.getX();
        int dy = apple.getY() - head.getY();
        Direction moveDir;
        
        // 根据苹果位置决定移动方向
        if (Math.abs(dx) > Math.abs(dy)) {
            moveDir = dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else if (dy != 0) {
            moveDir = dy > 0 ? Direction.DOWN : Direction.UP;
        } else {
            moveDir = aiSnake.getDirection(); // 苹果就在头上
        }
        
        aiSnake.forceTurn(moveDir); // 无条件转向
        aiSnake.move();
    }

    // 检查蛇所有节点是否都在游戏区域内
    private boolean isSnakeAllInBounds(Snake s) {
        if (!isPointInBounds(s.getHead())) return false;
        for (Point p : s.getTail()) {
            if (!isPointInBounds(p)) return false;
        }
        return true;
    }
    
    // 检查点是否在游戏区域内
    private boolean isPointInBounds(Point p) {
        return p.getX() > 20 && p.getX() < WIDTH + 10 && p.getY() > 40 && p.getY() < HEIGHT + 30;
    }

    // 吃到食物时生成粒子效果
    private void spawnEatParticles(Point pos) {
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double speed = 1 + Math.random() * 2;
            Color color = new Color((float)Math.random(), (float)Math.random(), (float)Math.random());
            particles.add(new Particle(pos.getX(), pos.getY(), Math.cos(angle)*speed, Math.sin(angle)*speed, color, 8, 20 + (int)(Math.random()*10)));
        }
    }
    
    // 死亡粒子效果生成
    private void spawnDeathParticles(Point pos) {
        if (pos == null) {
            return;
        }
        try {
            int numParticles = 40; // 粒子数量
            int life = 40; // 粒子生命周期
            int size = 30; // 粒子大小
            Color color = Color.WHITE; // 粒子颜色
            
            Random rand = new Random();
            for (int i = 0; i < numParticles; i++) {
                double angle = 2 * Math.PI * i / numParticles + rand.nextDouble() * 0.2;
                double speed = 3 + rand.nextDouble() * 2;
                double vx = Math.cos(angle) * speed;
                double vy = Math.sin(angle) * speed;
                particles.add(new Particle(pos.getX(), pos.getY(), vx, vy, color, size, life));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 加速时生成流线粒子
    private void spawnSpeedParticles() {
        Point head = snake.getHead();
        double angle = 0;
        
        // 根据移动方向确定粒子角度
        switch (snake.getDirection()) {
            case UP: angle = Math.PI/2; break;            
            case DOWN: angle = -Math.PI/2; break;
            case LEFT: angle = 0; break;
            case RIGHT: angle = Math.PI; break;
        }
        
        // 生成粒子
        for (int i = 0; i < 2; i++) {
            double a = angle + (Math.random()-0.5)*0.5;
            double speed = 1 + Math.random();
            particles.add(new Particle(head.getX()+5, head.getY()+5, Math.cos(a)*speed, Math.sin(a)*speed, Color.CYAN, 6, 15));
        }
    }

    // 确保蛇在游戏区域内
    private void clampSnakeToBounds(Snake s) {
        if (s == null) return;
        
        // 调整蛇头位置
        if (!isPointInBounds(s.getHead())) {
            s.getHead().setX(Math.max(21, Math.min(s.getHead().getX(), WIDTH + 9)));
            s.getHead().setY(Math.max(41, Math.min(s.getHead().getY(), HEIGHT + 29)));
        }
        
        // 调整蛇身位置
        for (Point p : s.getTail()) {
            if (!isPointInBounds(p)) {
                p.setX(Math.max(21, Math.min(p.getX(), WIDTH + 9)));
                p.setY(Math.max(41, Math.min(p.getY(), HEIGHT + 29)));
            }
        }
    }

    // 生成障碍物
    private void spawnObstacle() {
        Random rand = new Random();
        int maxTry = 100;
        
        for (int i = 0; i < maxTry; i++) {
            int col = rand.nextInt(WIDTH / gridSize);
            int row = rand.nextInt(HEIGHT / gridSize);
            int x = 20 + col * gridSize;
            int y = 40 + row * gridSize;
            Point p = new Point(x, y);
            boolean conflict = false;
            
            // 检查是否与其他元素冲突
            if (apple != null && p.equals(apple)) conflict = true;
            if (shrinkItem != null && p.equals(shrinkItem)) conflict = true;
            if (snake.getHead().equals(p)) conflict = true;
            for (Point t : snake.getTail()) if (t.equals(p)) conflict = true;
            for (Obstacle o : obstacles) if (o.getPos().equals(p)) conflict = true;
            
            // 如果没有冲突，生成障碍物
            if (!conflict) {
                // 50%概率为移动障碍物
                if (Math.random() < 0.5) {
                    int dx = rand.nextBoolean() ? (rand.nextBoolean() ? gridSize : -gridSize) : 0;
                    int dy = dx == 0 ? (rand.nextBoolean() ? gridSize : -gridSize) : 0;
                    if (dx == 0 && dy == 0) dx = gridSize;
                    obstacles.add(new Obstacle(p, ObstacleType.MOVING, dx, dy));
                } else {
                    obstacles.add(new Obstacle(p, ObstacleType.STATIC, 0, 0));
                }
                break;
            }
        }
    }

    // 检查某点是否为障碍物
    private boolean isObstacleAt(int x, int y) {
        for (Obstacle o : obstacles) {
            if (o.getPos().getX() == x && o.getPos().getY() == y) return true;
        }
        return false;
    }

    // 检查是否吃到苹果或道具
    private void checkEatAppleAndItem() {
        // 吃苹果
        if (apple != null && snake.getHead().intersects(apple, gridSize / 2)) {
            snake.setNeedGrow();
            apple = null;
            points++;
            eatPlayer.playOnce();
            spawnEatParticles(snake.getHead());
        }
        
        // 吃缩短道具
        if (shrinkItem != null && snake.getHead().intersects(shrinkItem, gridSize / 2)) {
            shrinkSnake();
            shrinkItem = null;
            eatPlayer.playOnce();
            spawnEatParticles(snake.getHead());
        }
        
        // 吃汉堡
        if (burger != null && snake.getHead().getX() == burger.getX() && snake.getHead().getY() == burger.getY()) {
            points += 2;
            burger = null;
            eatPlayer.playOnce();
            snake.setNeedGrow();
            snake.addTail(); // 汉堡加两节
            spawnEatParticles(snake.getHead()); // 吃汉堡也有特效
        }
    }

    // 加载最高分
    private void loadBestScores() {
        File file = new File("scores.txt");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                for (Map.Entry<GameMode, String> entry : modeToChinese.entrySet()) {
                    String prefix = entry.getValue() + "最高分:";
                    if (line.startsWith(prefix)) {
                        try {
                            int score = Integer.parseInt(line.substring(prefix.length()).trim());
                            bestMap.put(entry.getKey(), score);
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (IOException ignored) {}
    }

    // 保存最高分
    private void saveBestScores() {
        File file = new File("scores.txt");
        List<String> oldLines = new ArrayList<>();
        // 读取原有内容，跳过前N行（模式数）
        int modeCount = GameMode.values().length;
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                for (int i = 0; i < modeCount; i++) reader.readLine(); // 跳过模式最高分行
                String line;
                while ((line = reader.readLine()) != null) {
                    oldLines.add(line);
                }
            } catch (IOException ignored) {}
        }
        // 写入新内容
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // 每个模式一行
            for (GameMode mode : GameMode.values()) {
                writer.write(modeToChinese.get(mode) + "最高分:" + bestMap.get(mode));
                writer.newLine();
            }
            // 写入原有记录
            for (String l : oldLines) {
                writer.write(l);
                writer.newLine();
            }
        } catch (IOException ignored) {}
    }

    // 生成汉堡
    public void spawnBurger() {
        int gridSize = 20;
        int gridCols = WIDTH / gridSize;
        int gridRows = HEIGHT / gridSize;
        Random rand = new Random();
        
        while (true) {
            int col = rand.nextInt(gridCols);
            int row = rand.nextInt(gridRows);
            int x = 20 + col * gridSize;
            int y = 40 + row * gridSize;
            boolean conflict = false;
            
            // 检查冲突
            if (isObstacleAt(x, y)) conflict = true;
            if (snake.getHead().getX() == x && snake.getHead().getY() == y) conflict = true;
            for (Point t : snake.getTail()) {
                if (t.getX() == x && t.getY() == y) { conflict = true; break; }
            }
            if (apple != null && apple.getX() == x && apple.getY() == y) conflict = true;
            
            // 如果没有冲突，设置汉堡位置
            if (!conflict) {
                burger = new Point(x, y);
                break;
            }
        }
    }
}