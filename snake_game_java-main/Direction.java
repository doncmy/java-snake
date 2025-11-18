/**
 * 方向枚举，表示物体（如贪吃蛇）可能的移动方向
 */
public enum Direction {
    UP,     // 向上移动
    DOWN,   // 向下移动
    LEFT,   // 向左移动
    RIGHT;  // 向右移动
    
    /**render
     * 判断当前方向是否为水平方向（左或右）
     * @return 如果是水平方向返回true，否则返回false
     */
    public boolean isX() {
        return this == LEFT || this == RIGHT;
    }
    
    
    /**
     * 判断当前方向是否为垂直方向（上或下）
     * @return 如果是垂直方向返回true，否则返回false
     */
    public boolean isY() {
        return this == UP || this == DOWN;
    }
}