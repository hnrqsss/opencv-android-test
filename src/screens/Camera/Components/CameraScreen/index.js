import React, { useRef, useCallback } from 'react'
import {
    View,
    TouchableOpacity,
} from 'react-native'
import { RNCamera as Camera } from 'react-native-camera'
import ImagePicker from 'react-native-image-crop-picker'


import { styles } from './styles'
import { Icon } from '../../../../Components'

function CameraScreen({
    takingPicture,
    cameraRef,
}) {

    function pickFromGallery() {
        ImagePicker.openPicker({
            cropping: false,
            multiple: false,
            includeBase64: true,
            width: 300,
            height: 400,
        }).then(image => {
            
            const { data } = image
            
            takingPicture(data)
            
        }).catch(error => console.log(error))
    }

    return (
        <View style={styles.container}>
            
            <View style={styles.squareContainer}>
                <View style={styles.squareMask} />
            </View>

            <Camera
                ref={cameraRef}
                autoFocus={false}
                style={styles.preview}
                captureAudio={false}
                androidCameraPermissionOptions={
                    {
                        title: 'Permission to use camera',
                        message: 'We need your permission to use your camera phone'
                    }
                }

            ></Camera>
        
            <View style={styles.buttonContainer}>
                <View style={styles.iconContainer}>
                    <TouchableOpacity
                        style={styles.galleryButton}
                        onPress={() => pickFromGallery()}
                    >
                        <Icon name='gallery' fill='#FFF' width={28} height={28} />
                    </TouchableOpacity>
                </View>
                
                <TouchableOpacity
                    onPress={() => takingPicture()}
                    style={styles.buttonBorder}
                >
                    <View style={styles.button} />
                </TouchableOpacity>

                <View style={styles.iconContainer} />
                
            </View>
        </View>
    )
}

export default CameraScreen