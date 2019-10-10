package com.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

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

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class RNOpenCvLibraryModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    Mat matImage;

    public RNOpenCvLibraryModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNOpenCvLibrary";
    }

    private ArrayList<MatOfPoint> getContours(Mat imageOriginal) {

        Mat grayImage = null;
        Mat cannedImage = null;
        Mat resizedImage = null;

        //Change image size to optimize image process
        double ratio = imageOriginal.size().height/500;
        int height = Double.valueOf(imageOriginal.size().height / ratio).intValue();
        int width = Double.valueOf(imageOriginal.size().width / ratio).intValue();
        Size size = new Size(width, height);

        //changing to gray scaling and detect canny edges in resized Images
        resizedImage = new Mat(size, CvType.CV_8UC4);
        grayImage = new Mat(size, CvType.CV_8UC4);
        cannedImage = new Mat(size, CvType.CV_8UC1);

        Imgproc.resize(imageOriginal, resizedImage, size);
        Imgproc.cvtColor(resizedImage, grayImage, Imgproc.COLOR_RGBA2GRAY, 4);
        Imgproc.GaussianBlur(grayImage, grayImage, new Size(5, 5), 0);
        Imgproc.Canny(grayImage, cannedImage, 80, 100, 3, false);

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


    private Mat turnIntoBase64IntoMap(String imageBase64) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
        Bitmap image = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        Mat imageMat = new Mat();
        Utils.bitmapToMat(image, imageMat);

        return imageMat;
    }

    private Mat cannyImage(Mat imageMat) {

        Mat imageEdges = new Mat(imageMat.size(), CvType.CV_8UC1);

        Imgproc.cvtColor(imageMat, imageEdges, Imgproc.COLOR_BGR2GRAY, 4);
        Imgproc.Canny(imageEdges, imageEdges, 80, 100, 3, false);

        return imageEdges;
    }

    private Mat reduceNoise(Mat imageCannyEdges) {

        Mat imageGaussian = new Mat();
        Imgproc.GaussianBlur(imageCannyEdges, imageGaussian, new Size(5,5), 0);

        Mat imageThreshold = new Mat();
        Imgproc.adaptiveThreshold(imageGaussian, imageThreshold, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV,11, 12);

        return imageCannyEdges;
    }

//    private Mat findContours(Mat imageGreyScale, Mat imageDefault) {
//
//        Mat hierarchy = new Mat();
//        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//        Imgproc.findContours(imageGreyScale, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        double maxArea = 0;
//        MatOfPoint2f approx = new MatOfPoint2f();
//        Rect rect;
//        MatOfPoint points = new MatOfPoint();
//        MatOfPoint2f contour2f;
//        double peri;
//
//        for(int i = 0; i < contours.size(); i++) {
//
//            double area = Imgproc.contourArea(contours.get(i));
//
//            if (area > maxArea) {
//                maxArea = area;
//
//                contour2f = new MatOfPoint2f(contours.get(i).toArray());
//                peri = Imgproc.arcLength(contour2f, true) * 0.02;
//                Imgproc.approxPolyDP(contour2f, approx, peri, true);
//
//                double lenDouble = approx.size().height;
//                int len = (int) Math.floor(lenDouble);
//                Log.d("OPEN_CV_LEN", ""+len);
//
//                if(len == 4) {
//                    points = new MatOfPoint(approx.toArray());
//
//                    Log.d("OPEN_CV_POINTS", "");
//                }
//            }
//        }
//
//        int pointsLen = (int) Math.floor(points.size().height);
//
//        if(pointsLen == 4 ) {
//            rect = Imgproc.boundingRect(points);
//            Imgproc.rectangle(imageDefault, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar(0,255,0), 3);
//        }
//
//        return imageDefault;
//    }

    private boolean insideArea(Point[] rp, Size size) {

        int width = Double.valueOf(size.width).intValue();
        int height = Double.valueOf(size.height).intValue();

        int minimumSize = width / 10;

        boolean isANormalShape = rp[0].x != rp[1].x && rp[1].y != rp[0].y && rp[2].y != rp[3].y && rp[3].x != rp[2].x;
        boolean isBigEnough = ((rp[1].x - rp[0].x >= minimumSize) && (rp[2].x - rp[3].x >= minimumSize)
                && (rp[3].y - rp[0].y >= minimumSize) && (rp[2].y - rp[1].y >= minimumSize));

        double leftOffset = rp[0].x - rp[3].x;
        double rightOffset = rp[1].x - rp[2].x;
        double bottomOffset = rp[0].y - rp[1].y;
        double topOffset = rp[2].y - rp[3].y;

        boolean isAnActualRectangle = ((leftOffset <= minimumSize && leftOffset >= -minimumSize)
                && (rightOffset <= minimumSize && rightOffset >= -minimumSize)
                && (bottomOffset <= minimumSize && bottomOffset >= -minimumSize)
                && (topOffset <= minimumSize && topOffset >= -minimumSize));

        return isANormalShape && isAnActualRectangle && isBigEnough;
    }

    private Point[] sortPoints(Point[] src) {

        ArrayList<Point> srcPoints = new ArrayList<>(Arrays.asList(src));

        Point[] result = { null, null, null, null };

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

        // top-left corner = minimal sum
        result[0] = Collections.min(srcPoints, sumComparator);

        // bottom-right corner = maximal sum
        result[2] = Collections.max(srcPoints, sumComparator);

        // top-right corner = minimal diference
        result[1] = Collections.min(srcPoints, diffComparator);

        // bottom-left corner = maximal diference
        result[3] = Collections.max(srcPoints, diffComparator);

        return result;
    }

    private Point[] detectRect(Mat originalImage, ArrayList<MatOfPoint> contours) {


        double ratio = originalImage.size().height/500;
        int height = Double.valueOf(originalImage.size().height / ratio).intValue();
        int width = Double.valueOf(originalImage.size().width / ratio).intValue();

        Size size = new Size(width, height);

        for(MatOfPoint c : contours) {
            MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());

            double peri = Imgproc.arcLength(c2f, true) * 0.02;

            MatOfPoint2f approx = new MatOfPoint2f();

            Imgproc.approxPolyDP(c2f, approx, peri, true);

            Point[] points = approx.toArray();

            Log.d("OPEN_CV_POINTS" ,""+points.length);

            Point[] sortedPoints = sortPoints(points);

            if(insideArea(sortedPoints, size)) {
                return sortedPoints;
            }

        }

        return null;
    }

    private Mat drawRect(Point[] points, Mat originalImage) {

        double ratio = originalImage.size().height / 500;
        int width = Double.valueOf(originalImage.size().width / ratio).intValue();
        int height = Double.valueOf(originalImage.size().height / ratio).intValue();

        if(points != null) {

            Point[] originalPoints = new Point[4];
            originalPoints[0] = new Point(width - points[3].y, points[3].x); //tl
            originalPoints[1] = new Point(width - points[0].y, points[0].x); //tr
            originalPoints[2] = new Point(width - points[1].y, points[1].x); //br
            originalPoints[3] = new Point(width - points[2].y, points[2].x); //bl

        }

        return originalImage;

    }

    @ReactMethod
    public  void stepsTogetCorner(String imageAsBase64, Callback errorCallback, Callback successCallback) {
        try {

            //Converting image
            Mat rgbaImage = turnIntoBase64IntoMap(imageAsBase64);

            ArrayList<MatOfPoint> contours = getContours(rgbaImage);

            Point[] rectPoints = detectRect(rgbaImage, contours);

            Mat finalImage = drawRect(rectPoints, rgbaImage);


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

//    @ReactMethod
//    public  void stepsTogetCorner(String imageAsBase64, Callback errorCallback, Callback successCallback) {
//        try {
//
//            //Convert image base64 into bitmap
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inDither = true;
//            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//
//            byte[] decodedString = Base64.decode(imageAsBase64, Base64.DEFAULT);
//            Bitmap image = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//
//            //convert bitmap to mat
//            Mat imageToMat = new Mat();
//            Utils.bitmapToMat(image, imageToMat);
//
//            //convert mat into greyscale
////            Mat imageToGreyScale = new Mat();
////            Imgproc.cvtColor(imageToMat, imageToGreyScale, Imgproc.COLOR_BGR2GRAY);
//
//            //Convert mat greyscale into canny
////            Mat imageToCanny = new Mat();
////            Imgproc.Canny(imageToGreyScale, imageToCanny, 80, 100, 3, true);
//
//            //convert mat canny into bitmap image
////            Bitmap imageConvert = Bitmap.createBitmap(imageToCanny.cols(),imageToCanny.rows(),  Bitmap.Config.ARGB_8888);
////            Utils.matToBitmap(imageToCanny, imageConvert);
//
//            //find rect
//            Mat src = new Mat();
//            Imgproc.cvtColor(imageToMat, src, Imgproc.COLOR_BGR2RGB);
//
//            Mat blurred = src.clone();
//            Imgproc.medianBlur(src, blurred, 9);
//
//            Mat gray0 = new Mat(blurred.size(), CvType.CV_8U);
//            Mat gray = new Mat();
//
//            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//
//            List<Mat> blurredChannel = new ArrayList<Mat>();
//            blurredChannel.add(blurred);
//            List<Mat> gray0Channel = new ArrayList<Mat>();
//            gray0Channel.add(gray0);
//
//            MatOfPoint2f approxCurve;
//
//            double maxArea = 0;
//            int maxId = -1;
//
//            for(int c = 0; c < 3; c++) {
//                int ch[] = {c, 0};
//
//                Core.mixChannels(blurredChannel, gray0Channel, new MatOfInt(ch));
//
//                int thresholdLevel = 1;
//
//                for(int t = 0; t < thresholdLevel; t++) {
//                    if(t == 0) {
//                        Imgproc.Canny(gray0, gray, 10, 20, 3, true);
//                        Imgproc.dilate(gray, gray, new Mat(), new Point(-1, -1), 1);
//
//                    } else {
//                        Imgproc.adaptiveThreshold(gray0, gray, thresholdLevel,
//                                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
//                                Imgproc.THRESH_BINARY,
//                                (src.width() + src.height())/200, t);
//                    }
//
//                    Imgproc.findContours(gray, contours, new Mat(),
//                            Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
//
//
//                    for(MatOfPoint contour : contours) {
//                        MatOfPoint2f temp = new MatOfPoint2f(contour.toArray());
//
//                        double area = Imgproc.contourArea(contour);
//                        approxCurve = new MatOfPoint2f();
//                        Imgproc.approxPolyDP(temp, approxCurve,
//                                Imgproc.arcLength(temp, true)* 0.02, true);
//
//                        if(approxCurve.total() == 4 && area >= maxArea) {
//                            double maxCosine = 0;
//
//                            List<Point> curves = approxCurve.toList();
//
//                            for(int j = 2; j < 5; j++) {
//
//                                double cosine = Math.abs(angle(curves.get(j % 4),
//                                        curves.get(j - 2), curves.get(j - 1)));
//
//                                maxCosine = Math.max(maxCosine, cosine);
//                            }
//
//                            if (maxCosine < 0.3) {
//                                maxArea = area;
//                                maxId = contours.indexOf(contour);
//                            }
//                        }
//                    }
//                }
//            }
//
//            if (maxId >= 0) {
//                Rect rect = Imgproc.boundingRect(contours.get(maxId));
//
//                Imgproc.rectangle(src, rect.tl(), rect.br(), new Scalar(255, 0, 0,
//                        .8), 4);
//
//
//                int mDetectedWidth = rect.width;
//                int mDetectedHeight = rect.height;
//
//                Log.d( "Camera Image", "Rectangle width :"+mDetectedWidth+ " Rectangle height :"+mDetectedHeight);
//
//            }
//
//            Bitmap imageConvert;
//            imageConvert = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(src, imageConvert);
//
//            //convert bitmap into base64 string
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            imageConvert.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//            byte[] byteArray = byteArrayOutputStream .toByteArray();
//            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
//
//            successCallback.invoke(encoded);
//
//        } catch (Exception e) {
//
//            errorCallback.invoke(e.getMessage());
//        }
//
//    }

    private static double angle(Point p1, Point p2, Point p0) {
        double dx1 = p1.x - p0.x;
        double dy1 = p1.y - p0.y;
        double dx2 = p2.x - p0.x;
        double dy2 = p2.y - p0.y;
        return (dx1 * dx2 + dy1 * dy2)
                / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2)
                + 1e-10);
    }

    @ReactMethod
    public void checkForBlurryImage(String imageAsBase64, Callback errorCallback, Callback successCallback) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = true;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            byte[] decodedString = Base64.decode(imageAsBase64, Base64.DEFAULT);
            Bitmap image = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            matImage = new Mat();

//      Bitmap image = decodeSampledBitmapFromFile(imageurl, 2000, 2000);
            int l = CvType.CV_8UC1; //8-bit grey scale image

            Utils.bitmapToMat(image, matImage);
            Mat matImageGrey = new Mat();
            Imgproc.cvtColor(matImage, matImageGrey, Imgproc.COLOR_BGR2GRAY);

            Bitmap destImage;
            destImage = Bitmap.createBitmap(image);
            Mat dst2 = new Mat();
            Utils.bitmapToMat(destImage, dst2);
            Mat laplacianImage = new Mat();
            dst2.convertTo(laplacianImage, l);
            Imgproc.Laplacian(matImageGrey, laplacianImage, CvType.CV_8U);
            Mat laplacianImage8bit = new Mat();
            laplacianImage.convertTo(laplacianImage8bit, l);

            Bitmap bmp = Bitmap.createBitmap(laplacianImage8bit.cols(), laplacianImage8bit.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(laplacianImage8bit, bmp);
            int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
            bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
            int maxLap = -16777216; // 16m
            for (int pixel : pixels) {
                if (pixel > maxLap)
                    maxLap = pixel;
            }

//            int soglia = -6118750;
            int soglia = -8118750;
            if (maxLap <= soglia) {
                System.out.println("is blur image");
            }

            successCallback.invoke(maxLap <= soglia);
        } catch (Exception e) {
            errorCallback.invoke(e.getMessage());
        }
    }
}