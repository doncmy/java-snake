// 导入音频处理相关类和文件IO类
import javax.sound.sampled.*;
import java.io.File;

// 音频播放器类
public class AudioPlayer {
    // 声明Clip对象用于存储和操作音频数据
    private Clip clip;

    // 构造函数：加载指定路径的音频文件
    public AudioPlayer(String filePath) {
        try {
            // 1. 通过AudioSystem获取音频输入流
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(filePath));
            
            // 2. 获取系统默认的Clip音频剪辑对象
            clip = AudioSystem.getClip();
            
            // 3. 打开音频流并将数据加载到Clip中
            clip.open(audioIn);
        } catch (Exception e) {
            // 捕获并处理音频加载异常
            System.out.println("音频加载失败: " + filePath);
            e.printStackTrace(); // 打印异常堆栈（实际开发建议使用日志框架）
        }
    }

    // 单次播放方法
    public void playOnce() {
        if (clip != null) {
            clip.stop();          // 停止当前播放（如果正在播放）
            clip.setFramePosition(0); // 重置播放位置到开头
            clip.start();        // 开始播放（只播放一次）
        }
    }

    // 循环播放方法
    public void playLoop() {
        if (clip != null) {
            clip.stop();          // 停止当前播放
            clip.setFramePosition(0); // 重置播放位置
            clip.loop(Clip.LOOP_CONTINUOUSLY); // 设置无限循环播放
        }
    }

    // 停止播放方法
    public void stop() {
        if (clip != null) {
            clip.stop();  // 停止播放
            // 注意：这里没有重置播放位置，下次play会从当前位置继续
        }
    }
}