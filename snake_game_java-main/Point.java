/**
 * 表示二维平面上的一个点，包含x和y坐标
 * 提供基本的点操作和几何判断方法
 */
public class Point {
    private int x; // x坐标
    private int y; // y坐标

    /**
     * 通过指定坐标构造点
     * @param x 横坐标值
     * @param y 纵坐标值
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * 复制构造函数，通过另一个点创建新点
     * @param p 要复制的点对象
     */
    public Point(Point p) {
        this.x = p.getX();
        this.y = p.getY();
    }

    /**
     * 按照指定方向和距离移动点
     * @param d 移动方向(UP/DOWN/LEFT/RI可扩展性与模块化设计GHT)
     * @param value 移动距离(像素值)
     */
    public void move(Direction d, int value) {
        switch(d) {
            case UP: this.y -= value; break;    // 向上移动，y坐标减少
            case DOWN: this.y += value; break;  // 向下移动，y坐标增加
            case RIGHT: this.x += value; break; // 向右移动，x坐标增加
            case LEFT: this.x -= value; break;  // 向左移动，x坐标减少
        }
    }

    /**
     * 获取x坐标
     * @return x坐标值
     */
    public int getX() {
        return x;
    }

    /**
     * 获取y坐标
     * @return y坐标值
     */
    public int getY() {
        return y;
    }

    /**
     * 设置x坐标（链式调用支持）
     * @param x 新的x坐标值
     * @return 当前对象
     */
    public Point setX(int x) {
        this.x = x;
        return this;
    }

    /**
     * 设置y坐标（链式调用支持）
     * @param y 新的y坐标值
     * @return 当前对象
     */
    public Point setY(int y) {
        this.y = y;
        return this;
    }

    /**
     * 判断两个点是否相同
     * @param p 要比较的点
     * @return 如果坐标相同返回true，否则false
     */
    public boolean equals(Point p) {
        return this.x == p.getX() && this.y == p.getY();
    }

    /**
     * 转换为字符串表示
     * @return 坐标字符串，格式为"(x, y)"
     */
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    /**
     * 判断两个点是否相交（使用默认容差10）
     * @param p 要检查的点
     * @return 如果在容差范围内相交返回true
     */
    public boolean intersects(Point p) {
        return intersects(p, 10); // 默认10像素容差
    }

    /**
     * 判断两个点是否在指定容差范围内相交
     * @param p 要检查的点
     * @param tolerance 相交判断的容差范围(像素)
     * @return 如果在容差范围内相交返回true
     */
    public boolean intersects(Point p, int tolerance) {
        int diffX = Math.abs(x - p.getX()); // x坐标差值
        int diffY = Math.abs(y - p.getY()); // y坐标差值
        return this.equals(p) || (diffX <= tolerance && diffY <= tolerance);
    }
}