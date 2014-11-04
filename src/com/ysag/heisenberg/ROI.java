package com.ysag.heisenberg;

import org.opencv.core.*;

public class ROI {
    Mat img;
    final Point topLeft;
    final Point bottomRight;
    final int thickness;
    final Scalar color;

    public ROI(Mat img, double x1, double y1, double x2, double y2, int thickness, Scalar color) {
        this.img = img;
        topLeft = new Point(x1, y1);
        bottomRight = new Point(x2, y2);
        this.thickness = thickness;
        this.color = color;
    }

    public ROI(Mat img, double x1, double y1, double x2, double y2) {
        this(img, x1, y1, x2, y2, 1, new Scalar(0, 255, 0)); 
    }

    public void drawRect(Mat img) {
        Core.rectangle(img, topLeft, bottomRight, color, thickness);
    }

    public void drawRect() {
        drawRect(this.img);
    }
}



