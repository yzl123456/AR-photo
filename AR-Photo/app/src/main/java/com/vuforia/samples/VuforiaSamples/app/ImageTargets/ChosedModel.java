package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

/**
 * Created by 泽林 on 2018/6/15.
 */

/**
 * 表示选择的模型
 * author:应泽林 qq:376712116
 */
public class ChosedModel {
    private String name;

    private int imageId;


    public ChosedModel(String name, int imageId) {
        this.name = name;
        this.imageId = imageId;
    }

    public String getName() {
        return name;
    }

    public int getImageId() {
        return imageId;
    }
}
