package com.song.sunset.utils.preinstall;

/**
 * @author songmingwen
 * @description
 * @since 2018/10/9
 */
public class DefaultPreinstallHandler implements PreinstallHandler {
    @Override
    public String getPreinstallInfo() {
        return "dev";
    }

    @Override
    public void setNextHandler(PreinstallHandler preinstallHandler) {
    }
}
