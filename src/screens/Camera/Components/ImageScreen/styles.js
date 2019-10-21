import {
    StyleSheet,
    Dimensions,
    StatusBar,
} from 'react-native'

const { width, height } = Dimensions.get('window')

export const styles = StyleSheet.create({

    container: {
        flex: 1,
    },

    image: {
        width,
        height: height - 150 - StatusBar.currentHeight,
    },

    buttonsContainer: {
        width: '100%',
        height: 150,
        backgroundColor: '#000',
        paddingHorizontal: 15,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between'
    },

    textButton: {
        color: '#FFF',
        fontSize: 16,
    }
})
