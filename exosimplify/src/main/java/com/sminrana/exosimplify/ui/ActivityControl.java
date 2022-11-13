package com.sminrana.exosimplify.ui;

public interface ActivityControl {
    /**
     * Called just before the app gets killed
     * Cleanup notification and unbind any services
     * in your activity when you implement this.
     */
    void onAppKill();
}
