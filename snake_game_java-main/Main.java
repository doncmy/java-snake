import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * 贪吃蛇游戏主窗口类，继承自JFrame
 * 负责创建游戏窗口和初始化游戏界面
 */
public class Main extends JFrame {
    
    /**
     * 主窗口构造函数
     */
	
    public Main() {
        initUI(); // 初始化用户界面
    }

    /**
     * 初始化游戏窗口界面
     */
    private void initUI() {
        add(new Game());
        // 设置窗口标题
        setTitle("蛇皇争锋：智控风暴");
        // 设置窗口大小(宽度800像素，高度600像素)
        setSize(800, 800);
        // 窗口居中显示
        setLocationRelativeTo(null);
        // 禁止调整窗口大小
        setResizable(false);
        // 设置关闭按钮行为(退出程序)
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * 程序主入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 使用事件调度线程创建并显示GUI
        EventQueue.invokeLater(() -> {
            Main ex = new Main();  // 创建主窗口实例
            ex.setVisible(true);   // 显示窗口
        });
    }
}