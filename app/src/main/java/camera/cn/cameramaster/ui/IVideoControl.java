package camera.cn.cameramaster.ui;

/**
 * 视频接口
 */

public interface IVideoControl {

    /**
     * 播放视频
     */
    void play();

    /**
     * 暂停视频播放
     */
    void pause();

    /**
     * 继续播放视频，前提是暂停了视频
     */
    void resume();

    /**
     * 停止播放视频
     */
    void stop();

    /**
     * 进度条快进
     * @param timeStamp
     */
    void seekTo(int timeStamp);

    void setPlaySeekTimeListener(PlaySeekTimeListener mPlaySeekTimeListener);

    void setPlayStateListener(PlayStateListener mPlayStateListener);

    interface PlaySeekTimeListener
    {
        void onSeekTime(int allTime, int time);
    }

    /**
     * 播放状态
     */
    interface PlayStateListener{

        void onStartListener(int width, int height);

        void onCompletionListener();
    }
}
