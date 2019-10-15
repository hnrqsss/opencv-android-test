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
import org.opencv.utils.Converters;

import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    List<MatOfPoint> finalPoints;
    Point[] sortPoints;

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

        resizedImage = new Mat(this.size, CvType.CV_8UC4);
        grayImage = new Mat(this.size, CvType.CV_8UC4);
        cannedImage = new Mat(this.size, CvType.CV_8UC1);

        Imgproc.resize(this.originalImage, resizedImage, this.size);
        Imgproc.cvtColor(resizedImage, grayImage, Imgproc.COLOR_RGBA2GRAY, 4);
        Imgproc.adaptiveThreshold(grayImage, grayImage, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 13, 12);
        Imgproc.Canny(grayImage.clone(), cannedImage, 50, 150, 3, false);

        this.cannyImage = cannedImage;

        //detect contours
        Mat hierarchy = new Mat();
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(cannedImage, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        hierarchy.release();

        //Order contours Area greater till lowest
        Collections.sort(contours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint prev, MatOfPoint next) {
                return Double.valueOf(Imgproc.contourArea(next)).compareTo(Imgproc.contourArea(prev));
            }
        });

        ArrayList<MatOfPoint> srcContours = new ArrayList<>(Arrays.asList(contour));

        Log.d("OPEN_CV_ARRAY", srcContours+"");

        return contours;
    }

    //String imageBase64
    private void turnIntoMap(Bitmap image) {

//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inDither = true;
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//
//        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
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

    private void sortPoints(Point[] src) {

        ArrayList<Point> srcPoints = new ArrayList<>(Arrays.asList(src));

        //Log.d("BEFORE", srcPoints+"");

        Point[] sortedPoints = { null, null, null, null };

        Comparator<Point> sumComparator = new Comparator<Point>() {
            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.valueOf(lhs.y + lhs.x).compareTo(rhs.y + rhs.x);
            }
        };

        Comparator<Point> diffComparator = new Comparator<Point>() {

            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.valueOf(lhs.y - lhs.x).compareTo(rhs.y - rhs.x);
            }
        };

        //top left = minimal sum
        sortedPoints[0] = Collections.min(srcPoints, sumComparator);

        // bottom-right  = maximal sum
        sortedPoints[2] = Collections.max(srcPoints, sumComparator);

        // top-right  = minimal diference
        sortedPoints[1] = Collections.min(srcPoints, diffComparator);

        // bottom-left  = maximal diference
        sortedPoints[3] = Collections.max(srcPoints, diffComparator);


        //Log.d("AFTER", sortedPoints+"");
        this.sortPoints = sortedPoints;
    }

    private Point[] detectRect(ArrayList<MatOfPoint> contours) {

        for(MatOfPoint c : contours) {
            MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());

            double peri = Imgproc.arcLength(c2f, true) * 0.02;

            MatOfPoint2f approx = new MatOfPoint2f();

            Imgproc.approxPolyDP(c2f, approx, peri, true);

            Point[] points = approx.toArray();
            ArrayList<Point> srcPoints = new ArrayList<>(Arrays.asList(points));

            Log.d("AREA", Imgproc.contourArea(c)+"");
            Log.d("POINTS", srcPoints+"");
            Log.d("POINTS_LENGTH", srcPoints.size()+"");

            if(srcPoints.size() == 4) {
                Log.d("CHOOSED_AREA", Imgproc.contourArea(c)+"");
                Log.d("CHOOSED_POINTS", srcPoints+"");
                this.contour = c;
                return  points;
            }
        }

        return null;
    }

    private ArrayList<MatOfPoint> defaultPoints(Point[] points) {

        for(Point p : points) {
            p.x = p.x * this.ratio;
            p.y = p.y * this.ratio;
        }

        MatOfPoint mPoints = new MatOfPoint();
        mPoints.fromArray(points);

        ArrayList<MatOfPoint> finalPoints = new ArrayList<MatOfPoint>();
        finalPoints.add(mPoints);
        return finalPoints;
    }

    private Mat changePerspective() {

        Point[] points = this.sortPoints;


        if(points != null) {

            Mat image = this.originalImage.clone();

            Point tl = new Point(points[0].x*this.ratio, points[0].y*this.ratio);
            Point tr = new Point(points[1].x*this.ratio, points[1].y*this.ratio);
            Point br = new Point(points[2].x*this.ratio, points[2].y*this.ratio);
            Point bl = new Point(points[3].x*this.ratio, points[3].y*this.ratio);

            double widthA = Math.sqrt(Math.pow(br.x - bl.x, 2) + Math.pow(br.y - bl.y, 2));
            double widthB = Math.sqrt(Math.pow(tr.x - tl.x, 2) + Math.pow(tr.y - tl.y, 2));

            double dw = Math.max(widthA, widthB);
            int maxWidth = Double.valueOf(dw).intValue();

            double heightA = Math.sqrt(Math.pow(tr.x - br.x, 2) + Math.pow(tr.y - br.y, 2));
            double heightB = Math.sqrt(Math.pow(tl.x - bl.x, 2) + Math.pow(tl.y - bl.y, 2));

            double dh = Math.max(heightA, heightB);
            int maxHeight = Double.valueOf(dh).intValue();

            ArrayList<Point> srcPoints = new ArrayList<>();
            ArrayList<Point> target = new ArrayList<>();

            srcPoints.add(tl);
            srcPoints.add(tr);
            srcPoints.add(br);
            srcPoints.add(bl);

            target.add(new Point(0,0));
            target.add(new Point(maxWidth - 1,0));
            target.add(new Point(maxWidth - 1,maxHeight - 1));
            target.add(new Point(0,maxHeight - 1));

            Mat M = Imgproc.getPerspectiveTransform(Converters.vector_Point_to_Mat(srcPoints, CvType.CV_32F),
                    Converters.vector_Point_to_Mat(target, CvType.CV_32F));

            Mat output = new Mat(maxHeight, maxWidth, CvType.CV_8UC4);

            Imgproc.warpPerspective(image, output, M, new Size(maxWidth, maxHeight));

            return output;

        }

        return this.originalImage;
    }

    @ReactMethod
    public  void stepsTogetCorner(String imageAsBase64, Callback errorCallback, Callback successCallback) {
        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/image5.jpg";

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

            sortPoints(rectPoints);

            Mat perspective = changePerspective();

            //To show rect
            //ArrayList<MatOfPoint> screenCnt = defaultPoints(rectPoints);
            //Imgproc.drawContours(this.originalImage, screenCnt, -1, new Scalar(0,255,0), 5);

            //Bitmap imageConvert;
            Bitmap imageConvert = Bitmap.createBitmap(perspective.cols(), perspective.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(perspective, imageConvert);

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

    private void saveImage(Bitmap image) {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();

        String fname = "Image.jpg";
        File file = new File (myDir, fname);

        Log.d("SAVE", "AQUI");

        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            Log.d("SAVE", "OK");
        } catch (Exception e) {
            Log.d("SAVE_ERROR", e.getMessage());
        }
    }
}