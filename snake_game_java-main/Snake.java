import java.awt.Image;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import java.util.*;

/**
 * 贪吃蛇类，实现蛇的移动、转向和生长逻辑
 */
public class Snake {
    // 当前移动方向
    private Direction direction;
    
    // 蛇头位置
    private Point head;
    
    // 蛇身节点列表(ArrayList实现动态数组)
    private ArrayList<Point> tail;
    
    // 每次移动的步长(像素)
    private static final int MOVE_STEP = 20;
    
    // 是否需要增长标记(吃到食物时设为true)
    private boolean needGrow = false;
    
    /**
     * 构造函数，初始化蛇头和蛇身
     * @param x 蛇头初始x坐标(必须对齐网格)
     * @param y 蛇头初始y坐标(必须对齐网格)
     * 默认长度为3节(head+2节body)，全部对齐网格中心
     */
    public Snake(int x, int y) {
        // 初始化蛇头位置
        this.head = new Point(x, y);
        // 初始化蛇身列表
        this.tail = new ArrayList<>();
        
        // 添加两节初始蛇身(位于蛇头左侧)
        this.tail.add(new Point(x - MOVE_STEP, y));        // 第1节身体
        this.tail.add(new Point(x - 2 * MOVE_STEP, y));    // 第2节身体
        
        // 默认向右移动
        this.direction = Direction.RIGHT;
    }

    /**
     * 移动蛇的方法
     * 1. 将当前蛇头位置添加到蛇身首部
     * 2. 如果不是增长状态，移除蛇尾最后一节
     * 3. 根据当前方向移动蛇头
     */
    public void move() {
        // 将当前蛇头位置添加到蛇身首部(作为新的第1节)
        tail.add(0, new Point(head.getX(), head.getY()));
        
        // 如果不是增长状态，移除蛇尾最后一节
        if (!needGrow) {
            tail.remove(tail.size() - 1);
        } else {
            // 如果是增长状态，保持长度，重置标记
            needGrow = false;
        }
        
        // 根据当前方向移动蛇头
        head.move(direction, MOVE_STEP);
    }
    
    /**
     * 增加蛇身长度
     * 在蛇尾添加一个新节点(位置与最后一节相同)
     */
    public void addTail() {
        // 获取蛇尾最后一节(如果蛇身为空则使用蛇头位置)
        Point last = tail.size() > 0 ? tail.get(tail.size() - 1) : head;
        
        // 在尾部添加一节(初始位置与最后一节相同)
        tail.add(new Point(last.getX(), last.getY()));
    }
    
    /**
     * 改变蛇的移动方向(有转向限制)
     * @param d 新的方向
     * 限制条件：不能直接反向移动(如左转右，上转下)
     * 只能垂直方向转水平方向，或水平方向转垂直方向
     */
    public void turn(Direction d) {
        // 检查转向是否合法(新方向必须与原方向垂直)
        if ((d.isX() && direction.isY()) || (d.isY() && direction.isX())) {
           direction = d;  // 更新方向
        }
    }
    
    /**
     * 无条件改变蛇的移动方向(AI专用)
     * @param d 新的方向
     */
    public void forceTurn(Direction d) {
        this.direction = d;  // 直接更新方向
    }
    
    /**
     * 获取蛇身节点列表
     * @return 包含所有蛇身节点的ArrayList
     */
    public ArrayList<Point> getTail() {
        return this.tail;
    }
    
    /**
     * 获取蛇头位置
     * @return 表示蛇头位置的Point对象
     */
    public Point getHead() {
        return this.head;
    }

    /**
     * 获取当前移动方向
     * @return 当前方向枚举值
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * 设置需要增长标记
     * 下次移动时蛇身将增长一节
     */
    public void setNeedGrow() {
        this.needGrow = true;
    }

    /**
     * 重置需要增长标记
     */
    public void setNeedGrowFalse() {
        this.needGrow = false;
    }

    /**
     * 检查蛇是否包含某个点
     * @param p 要检查的点
     * @return 如果点与蛇头或任意蛇身节点重合返回true，否则false
     */
    public boolean contains(Point p) {
        // 检查蛇头
        if (getHead().equals(p)) return true;
        
        // 检查所有蛇身节点
        for (Point body : getTail()) {
            if (body.equals(p)) return true;
        }
        
        return false;
    }
}