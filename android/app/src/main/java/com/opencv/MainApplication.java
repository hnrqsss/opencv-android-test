package com.opencv;

import android.app.Application;

import com.facebook.react.ReactApplication;
import com.reactnative.ivpusic.imagepicker.PickerPackage;
import org.wonday.orientation.OrientationPackage;
import com.reactnativecommunity.cameraroll.CameraRollPackage;
import com.RNFetchBlob.RNFetchBlobPackage;
import fr.snapp.imagebase64.RNImgToBase64Package;
import com.rnfs.RNFSPackage;
import com.horcrux.svg.SvgPackage;
import org.reactnative.camera.RNCameraPackage;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;

import com.reactlibrary.RNOpenCvLibraryPackage;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Mat;

import java.util.Arrays;
import java.util.List;

import org.opencv.android.OpenCVLoader;

import android.util.Log;


public class MainApplication extends Application implements ReactApplication {

  Mat imageMat;

  private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
    @Override
    public boolean getUseDeveloperSupport() {
      return BuildConfig.DEBUG;
    }

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
              new MainReactPackage(),
            new PickerPackage(),
            new OrientationPackage(),
            new CameraRollPackage(),
            new RNFetchBlobPackage(),
            new RNImgToBase64Package(),
            new RNFSPackage(),
              new SvgPackage(),
              new RNCameraPackage(),
              new RNOpenCvLibraryPackage()
      );
    }

    @Override
    protected String getJSMainModuleName() {
      return "index";
    }
  };

  @Override
  public ReactNativeHost getReactNativeHost() {
    return mReactNativeHost;
  }

  private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
    @Override
    public void onManagerConnected(int status) {
      switch (status) {
        case LoaderCallbackInterface.SUCCESS:
        {
          Log.i("OpenCV", "OpenCV loaded successfully");
          imageMat=new Mat();
        } break;
        default:
        {
          super.onManagerConnected(status);
        } break;
      }
    }
  };

  @Override
  public void onCreate() {
    super.onCreate();
    SoLoader.init(this, /* native exopackage */ false);

    if (!OpenCVLoader.initDebug()) {
      Log.d("OpenCv", "Error while init");
    }
  }

  public void onResume()
  {
    if (!OpenCVLoader.initDebug()) {
      Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
      OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
    } else {
      Log.d("OpenCV", "OpenCV library found inside package. Using it!");
      mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }
  }

}