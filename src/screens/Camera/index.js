import React, { useState, useRef, useCallback } from 'react'

import {
    View,
    ActivityIndicator
} from 'react-native'

import CameraScreen from './Components/CameraScreen'

import { styles } from './styles'

function Camera() {

    const [ isTakingPicture, setIsTakingPicture ] = useState(false)
    const [image, setImage] = useState(null)

    
    return(
        <View style={styles.container}>
            { isTakingPicture && <ActivityIndicator size='large' color='orange' />}
            
            { (!isTakingPicture && image === null) && <CameraScreen 
                image={image}
                setImage={setImage}
                isTakingPicture={isTakingPicture}
                setIsTakingPicture={setIsTakingPicture}
            />}    

        </View>
    )
}

export default Camera