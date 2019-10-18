import React, { Component } from 'react';
import {
  View,
  Text,
  Platform,
  Image,
  TouchableOpacity,
  StyleSheet,
  PermissionsAndroid,
  Alert,
} from 'react-native';
import { RNCamera as Camera } from 'react-native-camera';

import CameraRoll from "@react-native-community/cameraroll";

import OpenCV from './src/NativeModules/OpenCV';
import CircleWithinCircle from './src/assets/svg/Icon';
import RNFS from 'react-native-fs'


class App extends Component {
  constructor(props) {
    super(props);

    this.takePicture = this.takePicture.bind(this);
    this.checkForBlurryImage = this.checkForBlurryImage.bind(this);
    this.proceedWithCheckingBlurryImage = this.proceedWithCheckingBlurryImage.bind(this);
    this.repeatPhoto = this.repeatPhoto.bind(this);
    this.usePhoto = this.usePhoto.bind(this);
  }

  state = {
    cameraPermission: false,
    photoAsBase64: {
      content: '',
      isPhotoPreview: false,
      photoPath: '',
    },
  };

  async checkForBlurryImage(imageAsBase64) {
    return new Promise((resolve, reject) => {
      if (Platform.OS === 'android') {

        OpenCV.stepsTogetCorner(imageAsBase64, (err) => {
          Alert.alert(
            'Atenção',
            'Nenhuma imagem detectada',
            [
              {text: 'Ok', onPress: () => {}},
            ],
            {cancelable: false},
          )
        }, async image => {
          this.setState({
            ...this.state,
            photoAsBase64: { content: image, isPhotoPreview: true, photoPath: image },
          }, () => {
            //const path = RNFS.DocumentDirectoryPath + '/image.jpg'
            //RNFS.writeFile(path, image, 'base64')
              //.then(res => {
                //console.log(res, path)
              //}).catch(err => console.log('err', err))
          
          })
            // console.log(image)
        })
        // OpenCV.checkForBlurryImage(imageAsBase64, error => {
        //   // error handling
        // }, msg => {
        //   resolve(msg);
        // });


      } else {
        // OpenCV.checkForBlurryImage(imageAsBase64, (error, dataArray) => {
        //   resolve(dataArray[0]);
        // });
      }
    });
  }

  requestExternalStoragePermission = async () => {
    try {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE,
        {
          title: 'My App Storage Permission',
          message: 'My App needs access to your storage ' +
            'so you can save your photos',
        },
      );
      return granted;
    } catch (err) {
      console.error('Failed to request permission ', err);
      return null;
    }
  };

  proceedWithCheckingBlurryImage() {
    const { content, photoPath } = this.state.photoAsBase64;

    this.checkForBlurryImage(content).then(blurryPhoto => {
      console.log(blurryPhoto, 'blurry')
      if (blurryPhoto) {
        this.refs.toast.show('Photo is blurred!',DURATION.FOREVER);
        return this.repeatPhoto();
      }
      this.refs.toast.show('Photo is clear!', DURATION.FOREVER);
      this.setState({ photoAsBase64: { ...this.state.photoAsBase64, isPhotoPreview: true, photoPath } });
    }).catch(err => {
      console.log('err', err)
    });
  }

  async takePicture() {
    if (this.camera) {
      const options = { quality: 0.5, base64: true };
      const data = await this.camera.takePictureAsync(options);
      this.setState({
        ...this.state,
        photoAsBase64: { content: data.base64, isPhotoPreview: false, photoPath: data.uri },
      });
      this.proceedWithCheckingBlurryImage();
    }
  }


  repeatPhoto() {
    this.setState({
      ...this.state,
      photoAsBase64: {
        content: '',
        isPhotoPreview: false,
        photoPath: '',
      },
    });
  }


  usePhoto() {
    // do something, e.g. navigate
  }


  render() {
    if (this.state.photoAsBase64.isPhotoPreview) {
      return (
        <View style={styles.container}>
          
          <Image
            source={{ uri: `data:image/png;base64,${this.state.photoAsBase64.content}` }}
            style={styles.imagePreview}
            resizeMode='contain'
          />
          <View style={styles.repeatPhotoContainer}>
            <TouchableOpacity onPress={this.repeatPhoto}>
              <Text style={styles.photoPreviewRepeatPhotoText}>
                Repeat photo
              </Text>
            </TouchableOpacity>
          </View>
          <View style={styles.usePhotoContainer}>
            <TouchableOpacity onPress={this.usePhoto}>
              <Text style={styles.photoPreviewUsePhotoText}>
                Use photo
              </Text>
            </TouchableOpacity>
          </View>
        </View>
      );
    }

    return (
      <View style={styles.container}>
        <Camera
          ref={cam => {
            this.camera = cam;
          }}
          autoFocus={false}
          style={styles.preview}
          permissionDialogTitle={'Permission to use camera'}
          permissionDialogMessage={'We need your permission to use your camera phone'}
        >
          <View style={styles.takePictureContainer}>
            <TouchableOpacity onPress={this.takePicture}>
              <View>
                <CircleWithinCircle />
              </View>
            </TouchableOpacity>
          </View>
        </Camera>
      </View>
    );
  }
}

export default App