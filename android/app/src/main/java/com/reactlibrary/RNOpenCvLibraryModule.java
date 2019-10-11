package com.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import org.opencv.android.Utils;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class RNOpenCvLibraryModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    Mat originalImage;
    double ratio;
    int widthRatio;
    int heightRatio;
    MatOfPoint contour;
    Size size;
    Mat cannyImage;

    public RNOpenCvLibraryModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNOpenCvLibrary";
    }

    private ArrayList<MatOfPoint> getContours() {

        Mat grayImage = null;
        Mat cannedImage = null;
        Mat resizedImage = null;

        //Change image size to optimize image process
        this.ratio = this.originalImage.size().height/500;

        this.heightRatio = Double.valueOf(this.originalImage.size().height / ratio).intValue();
        this.widthRatio = Double.valueOf(this.originalImage.size().width / ratio).intValue();
        this.size = new Size(this.widthRatio, this.heightRatio);

        //changing to gray scaling and detect canny edges in resized Images
        resizedImage = new Mat(this.size, CvType.CV_8UC4);
        grayImage = new Mat(this.size, CvType.CV_8UC4);
        cannedImage = new Mat(this.size, CvType.CV_8UC1);

        Imgproc.resize(this.originalImage, resizedImage, this.size);
        Imgproc.cvtColor(resizedImage, grayImage, Imgproc.COLOR_RGBA2GRAY, 4);

        Imgproc.Canny(grayImage, cannedImage, 80, 100, 3, false);

        this.cannyImage = cannedImage;

        //detect contours
        Mat hierarchy = new Mat();
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(cannedImage, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        //Order contours Area greater till lowest
        Collections.sort(contours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint prev, MatOfPoint next) {
                return Double.valueOf(Imgproc.contourArea(next)).compareTo(Imgproc.contourArea(prev));
            }
        });

        return contours;
    }

    //String imageBase64
    private void turnIntoMap(Bitmap image) {

//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inDither = true;
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//
//        byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
//        Bitmap image = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        Mat imageMat = new Mat();
        Utils.bitmapToMat(image, imageMat);

        this.originalImage = imageMat;
    }

    private int argMax(double[] pts) {

        double maxSum = pts[0];
        int maxIndex = 0;
        for(int i = 1; i < pts.length; i++) {
            if(pts[i] > maxSum) {
                maxSum = pts[i]*this.ratio;
                maxIndex = i;
            }
        }

        return  maxIndex;
    }

    private int argMin(double[] pts) {

        double maxSum = pts[0];
        int maxIndex = 0;
        for(int i = 1; i < pts.length; i++) {
            if(pts[i] < maxSum) {
                maxSum = pts[i]*this.ratio;
                maxIndex = i;
            }
        }

        return  maxIndex;
    }

    private Point[] sortPoints(Point[] src) {

        ArrayList<Point> srcPoints = new ArrayList<>(Arrays.asList(src));

        Log.d("BEFORE", srcPoints+"");

        Point[] sortedPoints = { null, null, null, null };

        double[] arraySum = { 0, 0, 0, 0};
        double[] arrayDiff = { 0, 0, 0, 0};

        for(int i = 0; i < srcPoints.size(); i++ ) {
            arraySum[i] = src[i].x + src[i].y;
            arrayDiff[i] = src[i].y - src[i].x;
        }

        sortedPoints[0] = src[argMin(arraySum)]; //tl
        sortedPoints[2] = src[argMax(arraySum)]; //tr
        sortedPoints[1] = src[argMin(arrayDiff)]; //br
        sortedPoints[3] = src[argMax(arrayDiff)]; //bl

        ArrayList<Point> after = new ArrayList<>(Arrays.asList(sortedPoints));

        Log.d("AFTER", after+"");
        return sortedPoints;
    }

    private Point[] detectRect(ArrayList<MatOfPoint> contours) {

        for(MatOfPoint c : contours) {
            MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());

            double peri = Imgproc.arcLength(c2f, true) * 0.02;

            MatOfPoint2f approx = new MatOfPoint2f();

            Imgproc.approxPolyDP(c2f, approx, peri, true);

            Point[] points = approx.toArray();
            ArrayList<Point> srcPoints = new ArrayList<>(Arrays.asList(points));

//            Log.d("AREA", Imgproc.contourArea(c)+"");
//            Log.d("POINTS", srcPoints+"");
//            Log.d("POINTS_LENGTH", srcPoints.size()+"");

            if(srcPoints.size() == 4) {
//                Log.d("CHOOSED_AREA", Imgproc.contourArea(c)+"");
//                Log.d("CHOOSED_POINTS", srcPoints+"");
                this.contour = c;
                return  points;
            }
        }

        return null;
    }

    private Mat drawRect(Point[] points) {

        if(points != null) {

            double[] tl = {points[0].x*this.ratio, points[0].y*this.ratio};
            double[] tr = {points[2].x*this.ratio, points[2].y*this.ratio};
            double[] br = {points[1].x*this.ratio, points[1].y*this.ratio};
            double[] bl = {points[3].x*this.ratio, points[3].y*this.ratio};

            double widthA = Math.sqrt(Math.pow(br[0] - bl[0], 2) + Math.pow(br[1] - bl[1], 2));
            double widthB = Math.sqrt(Math.pow(tr[0] - tl[0], 2) + Math.pow(tr[1] - tl[1], 2));
            double maxWidth = widthA > widthB ? widthA : widthB;

            double heightA = Math.sqrt(Math.pow(tr[0] - br[0], 2) + Math.pow(tr[1] - br[1], 2));
            double heightB = Math.sqrt(Math.pow(tl[0] - bl[0], 2) + Math.pow(tl[1] - bl[1], 2));
            double maxHeight = heightA > heightB ? heightA : heightB;
            Mat dst = Mat.zeros(4,2,CvType.CV_32FC2);

            dst.put(0,0, (maxWidth - 1),0, (maxWidth - 1), (maxHeight - 1), 0, (maxHeight - 1));

            Mat pespective = Imgproc.getPerspectiveTransform(this.originalImage, dst);

            Mat finalImage = new Mat();

            Imgproc.warpPerspective(this.originalImage, finalImage, pespective, new Size(maxWidth, maxHeight));

            return  finalImage;

        }

        return this.originalImage;

    }

    @ReactMethod
    public  void stepsTogetCorner(String imageAsBase64, Callback errorCallback, Callback successCallback) {
        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/image2.jpg";

            File file = new File(path);

            if(file.exists()) {
                Log.d("EXIST", "SIM");
            }else {
                Log.d("EXIST", "NAO");
            }

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Bitmap bm = BitmapFactory.decodeStream(fis);


            //Converting image
            turnIntoMap(bm);

            ArrayList<MatOfPoint> contours = getContours();

            Point[] rectPoints = detectRect(contours);

            Mat finalImage;

            Point[] sortedPoints = rectPoints != null ? sortPoints(rectPoints) : null;

            finalImage = sortedPoints != null ? this.originalImage : drawRect(sortedPoints);

            Imgproc.rectangle(finalImage, new Point(sortedPoints[0].x*this.ratio, sortedPoints[0].y*this.ratio), new Point(sortedPoints[3].x*this.ratio, sortedPoints[3].y*this.ratio), new Scalar(0,255,0), 5 );

            //Bitmap imageConvert;
            Bitmap imageConvert = Bitmap.createBitmap(finalImage.cols(), finalImage.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(finalImage, imageConvert);

            //convert bitmap into base64 string
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            imageConvert.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

            successCallback.invoke(encoded);

        } catch (Exception e) {

            errorCallback.invoke(e.getMessage());
        }

    }
}