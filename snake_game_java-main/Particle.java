// 导入必要的AWT包
import java.awt.*;

// 粒子效果类，用于游戏中的各种视觉效果
public class Particle {
    // 粒子当前位置坐标
    private double x, y;
    // 粒子速度分量
    private double vx, vy;
    // 粒子颜色
    private Color color;
    // 粒子透明度(0.0-1.0)
    private float alpha;
    // 粒子剩余生命周期(帧数)
    private int life;
    // 粒子最大生命周期(帧数)
    private int maxLife;
    // 粒子大小(像素)
    private int size;

    // 粒子构造函数
    public Particle(double x, double y, double vx, double vy, Color color, int size, int life) {
        this.x = x;      // 初始化x坐标
        this.y = y;      // 初始化y坐标
        this.vx = vx;    // 初始化x方向速度
        this.vy = vy;    // 初始化y方向速度
        this.color = color;  // 设置粒子颜色
        this.size = size;    // 设置粒子大小
        this.life = life;    // 设置粒子生命周期
        this.maxLife = life; // 记录最大生命周期(用于计算透明度)
        this.alpha = 1.0f;   // 初始透明度设为完全不透明
    }

    // 判断粒子是否还存活
    public boolean isAlive() {
        return life > 0;  // 当生命周期大于0时返回true
    }

    // 更新粒子状态(每帧调用)
    public void update() {
        x += vx;  // 根据速度更新x坐标
        y += vy;  // 根据速度更新y坐标
        life--;   // 生命周期减1
        
        // 计算当前透明度(随生命周期线性减小)
        alpha = Math.max(0, (float)life / maxLife);
    }

    // 绘制粒子
    public void draw(Graphics2D g) {
        if (!isAlive()) return;  // 如果粒子已死亡则不绘制
        
        // 保存原始混合模式
        Composite old = g.getComposite();
        
        // 设置带透明度的混合模式
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        // 设置粒子颜色
        g.setColor(color);
        
        // 绘制圆形粒子
        g.fillOval((int)x, (int)y, size, size);
        
        // 恢复原始混合模式
        g.setComposite(old);
    }
}