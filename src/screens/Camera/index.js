import React, { useState, useEffect, useRef } from 'react'

import {
    View,
    ActivityIndicator,
    Platform,
    Alert,
} from 'react-native'

import CameraScreen from './Components/CameraScreen'
import { styles } from './styles'
import ImageScreen from './Components/ImageScreen'

import OpenCV from '../../NativeModules/OpenCV'

function Camera() {

    const [ isTakingPicture, setIsTakingPicture ] = useState(false)
    const [image, setImage] = useState(null)
    let cameraRef = useRef(null)
    
    async function takingPicture() {
        try {
            
            setIsTakingPicture(true)

            const options = { quality: 0.8, base64: true }
            
            if(cameraRef.current) {
            
                const data = await cameraRef.current.takePictureAsync(options)
                
                scanImage(data.base64)

            } else {
                setIsTakingPicture(false)
            }
        
        } catch(error) {
            console.log('err',error)
            setIsTakingPicture(false)
        }
    }

    function scanImage(image) {
        if(Platform.OS === 'android') {
            OpenCV.stepsTogetCorner(image, (err) => {
                setImage(null)
                setIsTakingPicture(false)
                Alert.alert(
                    'Atenção',
                    'Nenhuma imagem detectada',
                    [
                        {text: 'Ok', onPress: () => {}},
                    ],
                    {cancelable: false},
                )
            },  (data) => {
                setImage(data)
                setIsTakingPicture(false)
            })
        }
    }

    return(
        <View style={styles.container}>
            
            { isTakingPicture && (
                <View style={styles.loadingContainer}>
                    <ActivityIndicator size='large' color='orange' />
                </View>
            )}

            { (image === null) && <CameraScreen 
                takingPicture={takingPicture}
                cameraRef={cameraRef}
            />}
            
            {(!isTakingPicture && image !== null) && <ImageScreen image={image} setImage={setImage} />}

        </View>
    )
}

export default Camera