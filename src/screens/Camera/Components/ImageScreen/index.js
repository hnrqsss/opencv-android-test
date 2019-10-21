import React from 'react'
import {
    View,
    Text,
    Image,
    TouchableOpacity,
} from 'react-native'
import { styles } from './styles'

function ImageScreen({ 
    image,
    setImage,
}) {
    return (
        <View style={styles.container}>
            <Image 
                style={styles.image}
                resizeMode={'contain'}
                source={{ uri: `data:image/png;base64,${image}` }}
            />
            <View style={styles.buttonsContainer}>
                <TouchableOpacity onPress={() => {}}>
                    <Text style={styles.textButton} >Use photo</Text>
                </TouchableOpacity>
                <TouchableOpacity onPress={() => setImage(null)}>
                    <Text style={styles.textButton} >Repeat photo</Text>
                </TouchableOpacity>
            {/*
                <TouchableOpacity onPress={() => setImage(null)}>
                    <Text style={styles.textButton} >Repeat photo</Text>
                </TouchableOpacity> */}
            </View>
        </View>
    )
}

export default ImageScreen