import React, { useRef, useCallback } from 'react'
import {
    View,
    TouchableOpacity,
} from 'react-native'
import { RNCamera as Camera } from 'react-native-camera'
import { styles } from './styles'
import { Icon } from '../../../../Components'

function CameraScreen({
    takingPicture,
    cameraRef,
}) {

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